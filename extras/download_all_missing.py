import undetected_chromedriver as uc
from selenium.webdriver.common.by import By
import time
import requests
import os
import re
import json
from pathlib import Path

def safe_filename(name):
    """Convert name to safe filename format"""
    return name.lower().replace(" ", "_").replace("/", "_").replace(":", "").replace("?", "").replace("&", "").replace("-", "_").replace("'", "")

def get_missing_enemies():
    """Get all missing enemies from all games"""
    games = [
        ("p3fes", "../app/src/main/assets/data/enemies/p3fes_enemies.json"),
        ("p3p", "../app/src/main/assets/data/enemies/p3p_enemies.json"),
        ("p3r", "../app/src/main/assets/data/enemies/p3r_enemies.json"),
        ("p4", "../app/src/main/assets/data/enemies/p4_enemies.json"),
        ("p4g", "../app/src/main/assets/data/enemies/p4g_enemies.json"),
        ("p5", "../app/src/main/assets/data/enemies/p5_enemies.json"),
        ("p5r", "../app/src/main/assets/data/enemies/p5r_enemies.json"),
    ]
    
    all_missing = {}
    
    for game_id, json_path in games:
        with open(json_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        if isinstance(data, list):
            names = []
            for enemy in data:
                if isinstance(enemy, dict):
                    if game_id in ['p5', 'p5r'] and 'persona_name' in enemy:
                        names.append(enemy.get('persona_name', ''))
                    else:
                        names.append(enemy.get('name', ''))
        else:
            names = [name for name in data.keys() if name]
        
        # Get unique base names
        unique_base_names = set()
        for name in names:
            base_name = re.sub(r'\s+[A-Z]$', '', name)
            unique_base_names.add(base_name)
        
        # Check downloaded folder
        download_folder = Path(f"downloaded_enemies/{game_id}")
        downloaded_safe_names = set()
        if download_folder.exists():
            for img in download_folder.glob("*.*"):
                downloaded_safe_names.add(img.stem)
        
        # Find missing
        missing = []
        for name in unique_base_names:
            safe_name = safe_filename(name)
            if safe_name not in downloaded_safe_names:
                missing.append(name)
        
        if missing:
            all_missing[game_id] = sorted(missing)
    
    return all_missing

def search_wiki_for_image(driver, enemy_name, game_prefix):
    """Search Megaten Wiki API for enemy image"""
    
    base_url = "https://megatenwiki.com/api.php"
    
    # Try different name variations
    name_variations = [
        enemy_name,
        enemy_name + " (Shadow)",
        enemy_name + " Shadow",
    ]
    
    # For compound names, try splitting
    if "/" in enemy_name:
        parts = [p.strip() for p in enemy_name.split("/")]
        name_variations.extend(parts)
    
    if "&" in enemy_name:
        parts = [p.strip() for p in enemy_name.split("&")]
        name_variations.extend(parts)
    
    # Try without hyphens
    if "-" in enemy_name:
        name_variations.append(enemy_name.replace("-", " "))
    
    for name_var in name_variations:
        print(f"    Trying: {name_var}")
        
        clean_name = name_var.strip().replace(" ", "_")
        
        try:
            # STEP 1: Get images list from API
            api_url = f"{base_url}?action=parse&page={clean_name}&prop=images&format=json"
            driver.get(api_url)
            time.sleep(3)
            
            page_source = driver.page_source
            
            # Check for Cloudflare challenge
            if "Just a moment" in page_source or "Checking your browser" in page_source:
                print("      [⏳] Cloudflare challenge detected, waiting...")
                time.sleep(10)
                page_source = driver.page_source
            
            # Check for 403
            if "403" in page_source:
                print("      [!] 403 BLOCKED")
                continue
            
            # Extract JSON from <pre> tag
            try:
                pre_element = driver.find_element(By.TAG_NAME, "pre")
                json_text = pre_element.text
                data = json.loads(json_text)
            except:
                continue
            
            if "error" in data:
                continue
            
            if "parse" not in data or "images" not in data["parse"]:
                continue
            
            images_list = data["parse"]["images"]
            
            # STEP 2: Filter for relevant images
            keywords = ["Model", "Render", "Portrait", "Shadow", "Graphic", "Artwork", "Sprite"]
            game_prefixes = [game_prefix, "P3", "P3R", "P3FES", "P3P", "P4", "P4G", "P5", "P5R", "P3D", "P4D", "P5D", "SMT"]
            
            for img_name in images_list:
                # Check if image matches our criteria
                for prefix in game_prefixes:
                    for keyword in keywords:
                        if prefix in img_name and keyword in img_name:
                            # STEP 3: Get image URL
                            api_url_2 = f"{base_url}?action=query&titles=File:{img_name}&prop=imageinfo&iiprop=url&format=json"
                            driver.get(api_url_2)
                            time.sleep(2)
                            
                            try:
                                pre_element = driver.find_element(By.TAG_NAME, "pre")
                                json_text = pre_element.text
                                data_2 = json.loads(json_text)
                                
                                pages = data_2.get("query", {}).get("pages", {})
                                for page_id, page_data in pages.items():
                                    if "imageinfo" in page_data:
                                        image_url = page_data["imageinfo"][0]["url"]
                                        print(f"      ✓ Found: {image_url}")
                                        return image_url
                            except:
                                continue
            
        except Exception as e:
            continue
    
    return None

def download_image_with_selenium(driver, url, output_path):
    """Download image using Selenium to bypass Cloudflare"""
    try:
        import base64
        
        # Navigate to the image URL
        driver.get(url)
        time.sleep(2)
        
        # Get the image as base64
        script = """
        var img = document.querySelector('img');
        if (!img) return null;
        var canvas = document.createElement('canvas');
        canvas.width = img.naturalWidth;
        canvas.height = img.naturalHeight;
        var ctx = canvas.getContext('2d');
        ctx.drawImage(img, 0, 0);
        return canvas.toDataURL('image/png').split(',')[1];
        """
        
        base64_data = driver.execute_script(script)
        
        if base64_data:
            image_data = base64.b64decode(base64_data)
            with open(output_path, 'wb') as f:
                f.write(image_data)
            return True
        else:
            print(f"      Error: Could not extract image")
    except Exception as e:
        print(f"      Error: {e}")
    return False

# Get all missing enemies
print("Scanning for missing enemies...")
all_missing = get_missing_enemies()

total_missing = sum(len(enemies) for enemies in all_missing.values())
print(f"\nFound {total_missing} missing enemies across {len(all_missing)} games")

for game_id, count in [(g, len(all_missing[g])) for g in all_missing]:
    print(f"  {game_id.upper()}: {count} missing")

# Setup Chrome driver
print("\nStarting Chrome driver...")
try:
    options = uc.ChromeOptions()
    driver = uc.Chrome(options=options, version_main=145)
    print("Chrome driver started successfully!")
    
    # Test Chrome is working - navigate to wiki and wait for Cloudflare
    print("Testing Chrome connection and bypassing Cloudflare...")
    driver.get("https://megatenwiki.com")
    time.sleep(5)  # Wait for Cloudflare challenge
    print("Chrome is working!")
except Exception as e:
    print(f"ERROR: Failed to start Chrome driver: {e}")
    import traceback
    traceback.print_exc()
    exit(1)

downloaded_count = 0
failed_count = 0

try:
    for game_id, missing_enemies in all_missing.items():
        print(f"\n{'='*70}")
        print(f"Downloading {game_id.upper()} missing enemies ({len(missing_enemies)} total)")
        print(f"{'='*70}")
        
        output_folder = f"downloaded_enemies/{game_id}"
        os.makedirs(output_folder, exist_ok=True)
        
        # Determine game prefix
        game_prefix_map = {
            "p3fes": "P3FES",
            "p3p": "P3P",
            "p3r": "P3R",
            "p4": "P4",
            "p4g": "P4G",
            "p5": "P5",
            "p5r": "P5R"
        }
        game_prefix = game_prefix_map.get(game_id, game_id.upper())
        
        for enemy_name in missing_enemies:
            safe_name = safe_filename(enemy_name)
            output_path = os.path.join(output_folder, f"{safe_name}.png")
            
            # Skip if already exists
            if os.path.exists(output_path):
                print(f"\n{enemy_name}: Already exists, skipping")
                continue
            
            print(f"\n{enemy_name}:")
            
            # Search for image
            image_url = search_wiki_for_image(driver, enemy_name, game_prefix)
            
            if image_url:
                # Download
                if download_image_with_selenium(driver, image_url, output_path):
                    print(f"  ✓ Downloaded successfully")
                    downloaded_count += 1
                else:
                    print(f"  ✗ Download failed")
                    failed_count += 1
            else:
                print(f"  ✗ No image found")
                failed_count += 1
            
            time.sleep(0.5)
    
finally:
    driver.quit()
    print("\n" + "="*70)
    print("Download complete!")
    print(f"Successfully downloaded: {downloaded_count}")
    print(f"Failed: {failed_count}")
    print("="*70)
