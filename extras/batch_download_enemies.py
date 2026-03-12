import os
import time
import json
from pathlib import Path
import cloudscraper  # Cloudflare bypass library

# Create a cloudscraper session that can bypass Cloudflare
session = cloudscraper.create_scraper(
    browser={
        'browser': 'chrome',
        'platform': 'windows',
        'mobile': False
    }
)

def api_download_enemy(enemy_name, game_prefix="P3", save_folder="wiki_rips"):
    if not os.path.exists(save_folder):
        os.makedirs(save_folder)

    # WE ARE TARGETING THE HIGH-QUALITY MEGATEN WIKI
    base_url = "https://megatenwiki.com/api.php"
    clean_name = enemy_name.strip().replace(" ", "_")

    try:
        print(f"--> API Fetching: {enemy_name}")
        params_1 = {
            "action": "parse",
            "page": clean_name,
            "prop": "images",
            "format": "json"
        }
        
        # STEP 1: Ask the API for images (Using cloudscraper to bypass Cloudflare)
        res_1 = session.get(base_url, params=params_1, timeout=15)
        
        if res_1.status_code == 403:
            print("    [!] 403 BLOCKED: Megaten Wiki caught us. Turn OFF NordVPN entirely for 5 mins.")
            return "STOP"
            
        try:
            data_1 = res_1.json()
        except ValueError:
            print("    [!] CRASH: Received HTML instead of JSON. Captcha triggered.")
            return "STOP"
            
        if "error" in data_1:
            print(f"    [!] 404 DEAD LINK: API says '{clean_name}' doesn't exist.")
            return "CONTINUE"

        image_list = data_1["parse"]["images"]
        
        # Look for the specific game's model (Megaten Wiki logic)
        target_file = None
        
        # Priority 1: Exact game match + Model/Render/Portrait/Sprite
        for img in image_list:
            if img.startswith(f"{game_prefix}_") and any(x in img for x in ["Model", "Render", "Portrait", "Sprite"]):
                target_file = img
                break
                
        # Priority 2: Matches Game Prefix but NOT a tiny UI icon
        if not target_file:
            for img in image_list:
                if img.startswith(f"{game_prefix}_") and "Icon" not in img:
                    target_file = img
                    break
                    
        # Priority 3: Desperation fallback just in case
        if not target_file:
            for img in image_list:
                if any(x in img for x in ["Model", "Render"]) and "Icon" not in img:
                    target_file = img
                    break

        if not target_file:
            print(f"    [!] FAILED: Found the page, but no {game_prefix} model image found in the API list.")
            return "CONTINUE"

        # STEP 2: Ask the API for the direct URL
        params_2 = {
            "action": "query",
            "titles": f"File:{target_file}",
            "prop": "imageinfo",
            "iiprop": "url",
            "format": "json"
        }
        
        try:
            res_2 = session.get(base_url, params=params_2, timeout=15).json()
        except:
            print(f"    [!] FAILED: Could not get image URL for {target_file}")
            return "CONTINUE"
        
        pages = res_2.get("query", {}).get("pages", {})
        page_id = list(pages.keys())[0] if pages else "-1"

        if page_id == "-1" or "imageinfo" not in pages[page_id]:
            print(f"    [!] FAILED: API could not resolve the file URL for {target_file}.")
            return "CONTINUE"

        img_url = pages[page_id]["imageinfo"][0]["url"]

        # STEP 3: Download the raw file
        safe_name = enemy_name.lower().replace(" ", "_").replace("/", "_").replace(":", "").replace("?", "")
        ext = ".png" if ".png" in img_url.lower() else ".jpg"
        final_path = os.path.join(save_folder, f"{safe_name}{ext}")

        # Download the image
        img_response = session.get(img_url, timeout=15)
        
        if img_response.status_code == 403:
            print(f"    [!] 403 on image download: {img_url}")
            return "CONTINUE"
            
        img_data = img_response.content
        with open(final_path, 'wb') as f:
            f.write(img_data)

        print(f"    ✅ SAVED: {target_file} as {safe_name}{ext}")
        time.sleep(2) # Be more polite with delays
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
AUTO_YES = False 

print("=" * 70)
print("BATCH ENEMY IMAGE DOWNLOADER (Megaten Wiki API + Chrome Bypass)")
print("=" * 70)

for game_id, json_path, game_prefix in games:
    print(f"\n{'='*70}")
    print(f"Processing {game_id.upper()}")
    print('='*70)
    
    try:
        with open(json_path, "r", encoding="utf-8") as f:
            data = json.load(f)
        
        if isinstance(data, list):
            names = [enemy.get("name", "") for enemy in data if isinstance(enemy, dict)]
        else:
            names = list(data.keys())
        
        missing = []
        for name in names:
            if not name:
                continue
            safe_name = name.lower().replace(" ", "_").replace("/", "_").replace(":", "").replace("?", "")
            if safe_name not in app_images:
                missing.append(name)
        
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
                response = input(f"\nDownload images for {game_id}? (y/n/s/all for skip/auto-yes): ").lower().strip()
                if response == 'all':
                    AUTO_YES = True
                    response = 'y'
            except EOFError:
                print("\n    [!] Terminal input dropped (EOFError). Auto-approving the rest to prevent crash.")
                AUTO_YES = True
                response = 'y'
        
        if response == 'n':
            print("Exiting...")
            break
        elif response == 's':
            print("Skipping this game...")
            continue
        elif response == 'y':
            print(f"\nDownloading {len(missing)} images...")
            
            for i, name in enumerate(missing, 1):
                print(f"\n[{i}/{len(missing)}] {name}")
                
                try:
                    game_folder = f"{output_folder}/{game_id}"
                    result = api_download_enemy(name, game_prefix=game_prefix, save_folder=game_folder)
                    
                    if result == "STOP":
                        print("\n⚠️  CLOUDFLARE BLOCK DETECTED - Stopping the entire script.")
                        print("Try turning off the VPN completely and running again.")
                        exit() 
                        
                except Exception as e:
                    print(f"  ❌ Failed: {e}")
            
            print(f"\n✅ Completed {game_id}")
            time.sleep(3) 
    
    except FileNotFoundError:
        print(f"  ⚠️  Data file not found: {json_path}")
    except Exception as e:
        print(f"  ❌ File Error: {e}")

print("\n" + "=" * 70)
print("BATCH DOWNLOAD COMPLETE")
print("=" * 70)
print(f"\nImages saved to: {output_folder}/")