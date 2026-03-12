import undetected_chromedriver as uc
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import time
import requests
import os
import re

def safe_filename(name):
    """Convert name to safe filename format"""
    return name.lower().replace(" ", "_").replace("/", "_").replace(":", "").replace("?", "").replace("&", "")

def search_wiki_for_image(driver, enemy_name, game_prefix):
    """Search Megaten Wiki for enemy image with multiple strategies"""
    
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
        # Also try with (Shadow) suffix
        for part in parts:
            name_variations.append(part.strip() + " (Shadow)")
    
    for name_var in name_variations:
        print(f"  Trying: {name_var}")
        
        # Search for the page
        search_url = f"https://megatenwiki.com/wiki/{name_var.replace(' ', '_')}"
        
        try:
            driver.get(search_url)
            time.sleep(2)
            
            # Look for images with various keywords
            keywords = ["Model", "Render", "Portrait", "Shadow", "Graphic", "Artwork", "Sprite"]
            game_prefixes = [game_prefix, "P3", "P3R", "P4", "P4G", "P5", "P5R", "P3D", "P4D", "P5D"]
            
            # Find all image links
            images = driver.find_elements(By.CSS_SELECTOR, "a.image img")
            
            for img in images:
                src = img.get_attribute("src")
                if not src or "megatenwiki.com/images" not in src:
                    continue
                
                # Check if it matches our criteria
                for prefix in game_prefixes:
                    for keyword in keywords:
                        pattern = f"{prefix}.*{keyword}"
                        if re.search(pattern, src, re.IGNORECASE):
                            # Get full resolution URL
                            full_url = src
                            if "/thumb/" in full_url:
                                full_url = re.sub(r'/thumb(/.*)/\d+px-.*', r'\1', full_url)
                            
                            print(f"    ✓ Found: {full_url}")
                            return full_url
            
        except Exception as e:
            print(f"    Error searching {name_var}: {e}")
            continue
    
    return None

def download_image(url, output_path):
    """Download image from URL"""
    try:
        response = requests.get(url, timeout=10)
        if response.status_code == 200:
            with open(output_path, 'wb') as f:
                f.write(response.content)
            return True
    except Exception as e:
        print(f"    Error downloading: {e}")
    return False

# Missing enemies for P4 and P4G
missing_enemies = {
    "p4": [
        "Contrarian King",
        "Escapist Soldier",
        "Extreme Vessel",
        "Intolerant Officer",
        "Judgement Sword",
        "Lost Okina",
        "Momentary Child",
        "Rainy Brother 3"
    ],
    "p4g": [
        "Contrarian King",
        "Escapist Soldier",
        "Extreme Vessel",
        "Intolerant Officer",
        "Judgement Sword",
        "Kusumi-no-Okami",
        "Lost Okina",
        "Momentary Child"
    ]
}

# Setup Chrome driver
print("Starting Chrome driver...")
options = uc.ChromeOptions()
options.add_argument('--headless=new')
driver = uc.Chrome(options=options, version_main=145)

try:
    for game_id, enemies in missing_enemies.items():
        print(f"\n{'='*60}")
        print(f"Downloading {game_id.upper()} missing enemies")
        print(f"{'='*60}")
        
        output_folder = f"downloaded_enemies/{game_id}"
        os.makedirs(output_folder, exist_ok=True)
        
        game_prefix = "P4G" if game_id == "p4g" else "P4"
        
        for enemy_name in enemies:
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
                if download_image(image_url, output_path):
                    print(f"  ✓ Downloaded successfully")
                else:
                    print(f"  ✗ Download failed")
            else:
                print(f"  ✗ No image found")
            
            time.sleep(1)
    
finally:
    driver.quit()
    print("\n" + "="*60)
    print("Download complete!")
