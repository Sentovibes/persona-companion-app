import os
import time
import json
import re
from pathlib import Path
import shutil
import undetected_chromedriver as uc
from selenium.webdriver.common.by import By

def api_download_enemy_selenium(driver, enemy_name, game_prefix="P3", save_folder="were_missing"):
    if not os.path.exists(save_folder):
        os.makedirs(save_folder)

    base_url = "https://megatenwiki.com/api.php"
    
    # Generate multiple name variations to try
    name_variations = [enemy_name]
    
    print(f"--> API Fetching: {enemy_name}")
    
    # Add "Shadow" suffix for Arcana bosses
    if any(arcana in enemy_name for arcana in ["Priestess", "Emperor", "Empress", "Hierophant", "Hermit", "Lovers", "Chariot", "Justice", "Fortune", "Strength", "Hanged Man"]):
        name_variations.append(f"{enemy_name} (Shadow)")
        name_variations.append(f"{enemy_name} Shadow")
    
    # Handle compound names with "/" or "&"
    if "/" in enemy_name:
        parts = [p.strip() for p in enemy_name.split("/")]
        name_variations.extend(parts)
        # Also try each part with Shadow
        for part in parts:
            if any(arcana in part for arcana in ["Priestess", "Emperor", "Empress", "Hierophant", "Hermit", "Lovers", "Chariot", "Justice", "Fortune", "Strength", "Hanged Man", "Elizabeth", "Theodore"]):
                name_variations.append(f"{part} (Shadow)")
                name_variations.append(f"{part} Shadow")
    
    if "&" in enemy_name:
        parts = [p.strip() for p in enemy_name.split("&")]
        name_variations.extend(parts)
        # Also try each part with Shadow
        for part in parts:
            if any(arcana in part for arcana in ["Priestess", "Emperor", "Empress", "Hierophant", "Hermit", "Lovers", "Chariot", "Justice", "Fortune", "Strength", "Hanged Man"]):
                name_variations.append(f"{part} (Shadow)")
                name_variations.append(f"{part} Shadow")
    
    # Try each variation
    for attempt, try_name in enumerate(name_variations, 1):
        clean_name = try_name.strip().replace(" ", "_")
        
        if attempt > 1:
            print(f"    [Attempt {attempt}] Trying: {try_name}")

        try:
            # STEP 1: Get images list
            api_url_1 = f"{base_url}?action=parse&page={clean_name}&prop=images&format=json"
            driver.get(api_url_1)
            time.sleep(3)
            
            page_source = driver.page_source
            
            # Check for Cloudflare challenge
            if "Just a moment" in page_source or "Checking your browser" in page_source:
                print("    [⏳] Cloudflare challenge detected, waiting...")
                time.sleep(10)
                page_source = driver.page_source
            
            if "403" in page_source and "parse" not in page_source:
                print("    [!] 403 BLOCKED after waiting")
                return "STOP"
            
            try:
                pre_element = driver.find_element(By.TAG_NAME, "pre")
                json_text = pre_element.text
                data_1 = json.loads(json_text)
            except:
                continue  # Try next variation
                
            if "error" in data_1:
                continue  # Try next variation

            image_list = data_1["parse"]["images"]
            
            # Look for the specific game's model/sprite
            target_file = None
            
            # Priority 1: Exact game match + Model/Render/Portrait/Sprite/Shadow/Graphic/Artwork
            for img in image_list:
                if img.startswith(f"{game_prefix}_") and any(x in img for x in ["Model", "Render", "Portrait", "Sprite", "Shadow", "Graphic", "Artwork"]):
                    target_file = img
                    break
            
            # Priority 2: Any game prefix match (P3D, P4, P5, etc.)
            if not target_file:
                for img in image_list:
                    if any(img.startswith(f"{prefix}_") for prefix in ["P3", "P3D", "P3R", "P4", "P4G", "P5", "P5R"]) and any(x in img for x in ["Model", "Render", "Portrait", "Sprite", "Shadow", "Graphic", "Artwork"]):
                        target_file = img
                        break
                    
            # Priority 3: Matches Game Prefix but NOT a tiny UI icon
            if not target_file:
                for img in image_list:
                    if img.startswith(f"{game_prefix}_") and "Icon" not in img:
                        target_file = img
                        break
                        
            # Priority 4: Desperation fallback
            if not target_file:
                for img in image_list:
                    if any(x in img for x in ["Model", "Render", "Shadow", "Portrait", "Graphic", "Artwork", "Sprite"]) and "Icon" not in img:
                        target_file = img
                        break

            if not target_file:
                continue  # Try next variation

            # STEP 2: Get the direct URL
            api_url_2 = f"{base_url}?action=query&titles=File:{target_file}&prop=imageinfo&iiprop=url&format=json"
            driver.get(api_url_2)
            time.sleep(2)
            
            try:
                pre_element = driver.find_element(By.TAG_NAME, "pre")
                json_text = pre_element.text
                res_2 = json.loads(json_text)
            except:
                continue
            
            pages = res_2.get("query", {}).get("pages", {})
            page_id = list(pages.keys())[0] if pages else "-1"

            if page_id == "-1" or "imageinfo" not in pages[page_id]:
                continue

            img_url = pages[page_id]["imageinfo"][0]["url"]

            # STEP 3: Download the image
            safe_name = enemy_name.lower().replace(" ", "_").replace("/", "_").replace("&", "_").replace(":", "").replace("?", "")
            ext = ".png" if ".png" in img_url.lower() else ".jpg"
            final_path = os.path.join(save_folder, f"{safe_name}{ext}")

            driver.get(img_url)
            time.sleep(2)
            
            import requests
            cookies = driver.get_cookies()
            session_cookies = {cookie['name']: cookie['value'] for cookie in cookies}
            user_agent = driver.execute_script("return navigator.userAgent;")
            
            headers = {
                'User-Agent': user_agent,
                'Referer': f'https://megatenwiki.com/wiki/{clean_name}',
            }
            response = requests.get(img_url, cookies=session_cookies, headers=headers, timeout=15)
            
            if response.status_code == 403:
                continue
                
            with open(final_path, 'wb') as f:
                f.write(response.content)

            print(f"    ✅ SAVED: {target_file} as {safe_name}{ext} (found as '{try_name}')")
            time.sleep(2)
            return "CONTINUE"

        except Exception as e:
            continue  # Try next variation
    
    # All variations failed
    print(f"    [!] FAILED: Tried {len(name_variations)} variations, none found")
    return "CONTINUE"


# --- MAIN SCRIPT ---

games = [
    ("p3fes", "../app/src/main/assets/data/enemies/p3fes_enemies.json", "P3"),
    ("p3p", "../app/src/main/assets/data/enemies/p3p_enemies.json", "P3"), 
    ("p3r", "../app/src/main/assets/data/enemies/p3r_enemies.json", "P3R"),
    ("p4", "../app/src/main/assets/data/enemies/p4_enemies.json", "P4"),
    ("p4g", "../app/src/main/assets/data/enemies/p4g_enemies.json", "P4G"),
    ("p5", "../app/src/main/assets/data/enemies/p5_enemies.json", "P5"),
    ("p5r", "../app/src/main/assets/data/enemies/p5r_enemies.json", "P5R"),
]

print("=" * 70)
print("DOWNLOADING MISSING ENEMIES TO WERE_MISSING FOLDER")
print("=" * 70)
print("\nInitializing Chrome browser...")

options = uc.ChromeOptions()
# options.add_argument('--headless=new')  # Disabled - Cloudflare blocks headless
driver = uc.Chrome(options=options, use_subprocess=True, version_main=145)

try:
    for game_id, json_path, game_prefix in games:
        print(f"\n{'='*70}")
        print(f"Processing {game_id.upper()}")
        print('='*70)
        
        try:
            with open(json_path, "r", encoding="utf-8") as f:
                data = json.load(f)
            
            # Get enemy names
            if isinstance(data, list):
                names = []
                for enemy in data:
                    if isinstance(enemy, dict):
                        if game_id in ['p5', 'p5r'] and 'persona_name' in enemy:
                            names.append(enemy.get('persona_name', ''))
                        else:
                            names.append(enemy.get('name', ''))
            else:
                names = list(data.keys())
            
            # Get unique base names and find missing
            unique_base = set()
            for name in names:
                if name:
                    base_name = re.sub(r'\s+[A-Z]$', '', name)
                    unique_base.add(base_name)
            
            # Check what's already downloaded
            download_folder = Path(f"downloaded_enemies/{game_id}")
            downloaded = set()
            if download_folder.exists():
                for img in download_folder.glob("*.*"):
                    downloaded.add(img.stem)
            
            # Find missing
            missing = []
            for enemy in unique_base:
                safe_name = enemy.lower().replace(" ", "_").replace("/", "_").replace(":", "").replace("?", "")
                if safe_name not in downloaded:
                    missing.append(enemy)
            
            print(f"Found {len(missing)} missing enemies")
            
            if not missing:
                print("All enemies have images!")
                continue
            
            print(f"\nMissing enemies (showing first 10):")
            for i, name in enumerate(sorted(missing)[:10], 1):
                print(f"  {i}. {name}")
            
            if len(missing) > 10:
                print(f"  ... and {len(missing) - 10} more")
            
            response = input(f"\nDownload missing images for {game_id}? (y/n/s): ").lower().strip()
            
            if response == 'n':
                print("Exiting...")
                break
            elif response == 's':
                print("Skipping...")
                continue
            elif response == 'y':
                print(f"\nDownloading {len(missing)} images...")
                
                game_folder = f"were_missing_enemies/{game_id}"
                
                for i, name in enumerate(sorted(missing), 1):
                    print(f"\n[{i}/{len(missing)}] {name}")
                    
                    try:
                        result = api_download_enemy_selenium(driver, name, game_prefix=game_prefix, save_folder=game_folder)
                        
                        if result == "STOP":
                            print("\n⚠️  CLOUDFLARE BLOCK - Stopping.")
                            break
                            
                    except Exception as e:
                        print(f"  ❌ Failed: {e}")
                
                print(f"\n✅ Completed {game_id}")
                time.sleep(3)
        
        except FileNotFoundError:
            print(f"  ⚠️  Data file not found: {json_path}")
        except Exception as e:
            print(f"  ❌ File Error: {e}")

finally:
    driver.quit()
    print("\n" + "=" * 70)
    print("DOWNLOAD COMPLETE")
    print("=" * 70)
    print(f"\nImages saved to: were_missing_enemies/")
