import os
import time
import json
import re
from pathlib import Path
import shutil
import undetected_chromedriver as uc
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

def api_download_enemy_selenium(driver, enemy_name, game_prefix="P3", save_folder="wiki_rips"):
    if not os.path.exists(save_folder):
        os.makedirs(save_folder)

    base_url = "https://megatenwiki.com/api.php"
    clean_name = enemy_name.strip().replace(" ", "_")

    try:
        print(f"--> API Fetching: {enemy_name}")
        
        # STEP 1: Get images list
        api_url_1 = f"{base_url}?action=parse&page={clean_name}&prop=images&format=json"
        driver.get(api_url_1)
        time.sleep(3)  # Initial wait
        
        # Get the JSON response from the page
        page_source = driver.page_source
        
        # Check for Cloudflare challenge and wait for it to complete
        if "Just a moment" in page_source or "Checking your browser" in page_source:
            print("    [⏳] Cloudflare challenge detected, waiting...")
            time.sleep(10)  # Wait for challenge to complete
            page_source = driver.page_source
        
        # Check for 403 after waiting
        if "403" in page_source and "parse" not in page_source:
            print("    [!] 403 BLOCKED after waiting")
            return "STOP"
        
        # Extract JSON from <pre> tag (how browsers display JSON)
        try:
            pre_element = driver.find_element(By.TAG_NAME, "pre")
            json_text = pre_element.text
            data_1 = json.loads(json_text)
        except:
            print(f"    [!] Could not parse JSON response")
            return "CONTINUE"
            
        if "error" in data_1:
            print(f"    [!] 404 DEAD LINK: API says '{clean_name}' doesn't exist.")
            return "CONTINUE"

        image_list = data_1["parse"]["images"]
        
        # Look for the specific game's model
        target_file = None
        
        # Priority 1: Exact game match + Model/Render/Portrait/Sprite/Shadow/Graphic/Artwork
        for img in image_list:
            if img.startswith(f"{game_prefix}_") and any(x in img for x in ["Model", "Render", "Portrait", "Sprite", "Shadow", "Graphic", "Artwork"]):
                target_file = img
                break
                
        # Priority 2: Matches Game Prefix but NOT a tiny UI icon
        if not target_file:
            for img in image_list:
                if img.startswith(f"{game_prefix}_") and "Icon" not in img:
                    target_file = img
                    break
                    
        # Priority 3: Desperation fallback
        if not target_file:
            for img in image_list:
                if any(x in img for x in ["Model", "Render", "Shadow", "Portrait", "Graphic", "Artwork"]) and "Icon" not in img:
                    target_file = img
                    break

        if not target_file:
            print(f"    [!] FAILED: No {game_prefix} model image found.")
            return "CONTINUE"

        # STEP 2: Get the direct URL
        api_url_2 = f"{base_url}?action=query&titles=File:{target_file}&prop=imageinfo&iiprop=url&format=json"
        driver.get(api_url_2)
        time.sleep(2)
        
        try:
            pre_element = driver.find_element(By.TAG_NAME, "pre")
            json_text = pre_element.text
            res_2 = json.loads(json_text)
        except:
            print(f"    [!] FAILED: Could not get image URL")
            return "CONTINUE"
        
        pages = res_2.get("query", {}).get("pages", {})
        page_id = list(pages.keys())[0] if pages else "-1"

        if page_id == "-1" or "imageinfo" not in pages[page_id]:
            print(f"    [!] FAILED: API could not resolve file URL.")
            return "CONTINUE"

        img_url = pages[page_id]["imageinfo"][0]["url"]

        # STEP 3: Download the image using Selenium's session
        safe_name = enemy_name.lower().replace(" ", "_").replace("/", "_").replace(":", "").replace("?", "")
        ext = ".png" if ".png" in img_url.lower() else ".jpg"
        final_path = os.path.join(save_folder, f"{safe_name}{ext}")

        # Navigate to image and download using Selenium session
        driver.get(img_url)
        time.sleep(2)
        
        # Get cookies from Selenium and use them with requests
        import requests
        cookies = driver.get_cookies()
        session_cookies = {cookie['name']: cookie['value'] for cookie in cookies}
        
        # Get user agent from Selenium
        user_agent = driver.execute_script("return navigator.userAgent;")
        
        # Download with the same session cookies and user agent
        headers = {
            'User-Agent': user_agent,
            'Referer': f'https://megatenwiki.com/wiki/{clean_name}',
        }
        response = requests.get(img_url, cookies=session_cookies, headers=headers, timeout=15)
        
        if response.status_code == 403:
            print(f"    [!] 403 on image download")
            return "CONTINUE"
            
        with open(final_path, 'wb') as f:
            f.write(response.content)

        print(f"    ✅ SAVED: {target_file} as {safe_name}{ext}")
        time.sleep(2)
        return "CONTINUE"

    except Exception as e:
        print(f"    ❌ CRASH: Error -> {e}")
        return "CONTINUE"


# --- MAIN BATCH SCRIPT ---

games = [
    ("p3fes", "../app/src/main/assets/data/enemies/p3fes_enemies.json", "P3"),
    ("p3p", "../app/src/main/assets/data/enemies/p3p_enemies.json", "P3"), 
    ("p3r", "../app/src/main/assets/data/enemies/p3r_enemies.json", "P3R"),
    ("p4", "../app/src/main/assets/data/enemies/p4_enemies.json", "P4"),
    ("p4g", "../app/src/main/assets/data/enemies/p4g_enemies.json", "P4G"),
    ("p5", "../app/src/main/assets/data/enemies/p5_enemies.json", "P5"),
    ("p5r", "../app/src/main/assets/data/enemies/p5r_enemies.json", "P5R"),
]

app_images_dir = Path("../app/src/main/assets/images/enemies")
app_images = {img.stem for img in app_images_dir.glob("*.*")}

output_folder = "downloaded_enemies"
AUTO_YES = True  # Auto-approve all downloads (set to False for manual approval) 
downloaded_enemies = {}  # Track all downloaded enemies: safe_name -> (game_folder, file_path)

# Load existing downloads from all game folders
print("\nScanning for existing downloads...")
if os.path.exists(output_folder):
    for game_folder in os.listdir(output_folder):
        game_path = os.path.join(output_folder, game_folder)
        if os.path.isdir(game_path):
            for img_file in os.listdir(game_path):
                if img_file.endswith(('.png', '.jpg', '.jpeg')):
                    safe_name = Path(img_file).stem
                    file_path = os.path.join(game_path, img_file)
                    downloaded_enemies[safe_name] = (game_folder, file_path)
    print(f"Found {len(downloaded_enemies)} existing downloads")
else:
    print("No existing downloads found") 

print("=" * 70)
print("BATCH ENEMY IMAGE DOWNLOADER (Selenium + Undetected Chrome)")
print("=" * 70)
print("\nInitializing Chrome browser...")

# Initialize undetected Chrome
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
            
            if isinstance(data, list):
                names = []
                for enemy in data:
                    if isinstance(enemy, dict):
                        # For P5/P5R, use persona_name if available, otherwise use name
                        if game_id in ['p5', 'p5r'] and 'persona_name' in enemy:
                            names.append(enemy.get('persona_name', ''))
                        else:
                            names.append(enemy.get('name', ''))
            else:
                names = list(data.keys())
            
            missing = []
            seen_base_names = set()  # Track base names to avoid downloading variants
            
            for name in names:
                if not name:
                    continue
                    
                # Remove variant suffix (A, B, C, D, etc.) to get base name
                import re
                base_name = re.sub(r'\s+[A-Z]$', '', name)  # Strip A-Z suffix
                safe_base_name = base_name.lower().replace(" ", "_").replace("/", "_").replace(":", "").replace("?", "")
                
                # Skip if we already have the image or already queued the base name
                if safe_base_name not in app_images and base_name not in seen_base_names:
                    missing.append(base_name)
                    seen_base_names.add(base_name)
            
            print(f"Found {len(missing)} missing enemies")
            
            if not missing:
                print("All enemies have images!")
                continue
            
            print(f"\nMissing enemies (showing first 10):")
            for i, name in enumerate(missing[:10], 1):
                print(f"  {i}. {name}")
            
            if len(missing) > 10:
                print(f"  ... and {len(missing) - 10} more")
            
            if AUTO_YES:
                response = 'y'
            else:
                try:
                    response = input(f"\nDownload images for {game_id}? (y/n/s/all): ").lower().strip()
                    if response == 'all':
                        AUTO_YES = True
                        response = 'y'
                except EOFError:
                    print("\n    [!] Auto-approving...")
                    AUTO_YES = True
                    response = 'y'
            
            if response == 'n':
                print("Exiting...")
                break
            elif response == 's':
                print("Skipping...")
                continue
            elif response == 'y':
                print(f"\nDownloading {len(missing)} images...")
                
                for i, name in enumerate(missing, 1):
                    print(f"\n[{i}/{len(missing)}] {name}")
                    
                    try:
                        game_folder = f"{output_folder}/{game_id}"
                        
                        # Get base name (remove B/C/D suffix) for checking
                        base_name = re.sub(r'\s+[B-Z]$', '', name)
                        safe_name = base_name.lower().replace(" ", "_").replace("/", "_").replace(":", "").replace("?", "")
                        
                        # Check if base name already exists in current game folder
                        found_in_current = False
                        for ext in ['.png', '.jpg', '.jpeg']:
                            current_path = os.path.join(game_folder, f"{safe_name}{ext}")
                            if os.path.exists(current_path):
                                print(f"    ⏭️  Base image already in {game_id}, skipping")
                                downloaded_enemies[safe_name] = (game_id, current_path)
                                found_in_current = True
                                break
                        
                        if found_in_current:
                            continue
                        
                        # Check if base name already downloaded in a previous game
                        if safe_name in downloaded_enemies:
                            prev_game, prev_path = downloaded_enemies[safe_name]
                            
                            # Determine file extension from previous download
                            prev_ext = Path(prev_path).suffix
                            dest_path = os.path.join(game_folder, f"{safe_name}{prev_ext}")
                            
                            # Copy from previous game folder
                            if not os.path.exists(game_folder):
                                os.makedirs(game_folder)
                            
                            shutil.copy2(prev_path, dest_path)
                            print(f"    📋 Copied base image from {prev_game} (no download needed)")
                            downloaded_enemies[safe_name] = (game_id, dest_path)
                            continue
                        
                        # Download new enemy (use base name for download)
                        result = api_download_enemy_selenium(driver, base_name, game_prefix=game_prefix, save_folder=game_folder)
                        
                        if result == "STOP":
                            print("\n⚠️  CLOUDFLARE BLOCK - Stopping.")
                            break
                        elif result == "CONTINUE":
                            # Check if file was actually downloaded
                            for ext in ['.png', '.jpg', '.jpeg']:
                                file_path = os.path.join(game_folder, f"{safe_name}{ext}")
                                if os.path.exists(file_path):
                                    downloaded_enemies[safe_name] = (game_id, file_path)
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
    print("BATCH DOWNLOAD COMPLETE")
    print("=" * 70)
    print(f"\nImages saved to: {output_folder}/")
