import os
import time
import requests

def api_download_enemy(enemy_name, game_prefix="P3", save_folder="wiki_rips"):
    # game_prefix can be "P3" for FES/Portable, "P3R" for Reload, "PQ2", etc.
    if not os.path.exists(save_folder):
        os.makedirs(save_folder)

    # Professional API Header
    headers = {'User-Agent': 'PersonaCompanionApp/1.0 (Data Extraction Tool)'}
    base_url = "https://megatenwiki.com/api.php"
    clean_name = enemy_name.strip().replace(" ", "_")

    try:
        # STEP 1: Ask the API for the list of images on the page
        print(f"--> API Fetching: {enemy_name}")
        params_1 = {
            "action": "parse",
            "page": clean_name,
            "prop": "images", # We ONLY want the image array, saves data!
            "format": "json"
        }
        res_1 = requests.get(base_url, params=params_1, headers=headers, timeout=10).json()

        if "error" in res_1:
            print(f"    [!] 404 DEAD LINK: API says '{clean_name}' doesn't exist.")
            return

        image_list = res_1["parse"]["images"]
        
        # Look for the specific game's model (e.g., "P3_Cowardly_Maya_Model.png")
        target_file = None
        for img in image_list:
            # We want the main model, not a Slash_Icon or a Confuse_Icon
            if img.startswith(f"{game_prefix}_") and ("Model" in img or "Render" in img):
                target_file = img
                break
                
        # Fallback: If there's no specific game prefix, just grab the first main image
        if not target_file:
            for img in image_list:
                if "Model" in img or "Render" in img:
                    target_file = img
                    break

        if not target_file:
            print(f"    [!] FAILED: Found the page, but no 3D Model image matched '{game_prefix}'.")
            return

        # STEP 2: Ask the API for the direct, uncompressed URL of that file
        params_2 = {
            "action": "query",
            "titles": f"File:{target_file}",
            "prop": "imageinfo",
            "iiprop": "url",
            "format": "json"
        }
        res_2 = requests.get(base_url, params=params_2, headers=headers, timeout=10).json()

        # Navigate the JSON tree to get the URL
        pages = res_2["query"]["pages"]
        page_id = list(pages.keys())[0]

        if page_id == "-1":
            print(f"    [!] FAILED: API could not resolve the file URL.")
            return

        img_url = pages[page_id]["imageinfo"][0]["url"]

        # STEP 3: Download the raw file!
        safe_name = enemy_name.lower().replace(" ", "_")
        ext = ".png" if ".png" in img_url.lower() else ".jpg"
        final_path = os.path.join(save_folder, f"{safe_name}{ext}")

        img_data = requests.get(img_url, headers=headers, timeout=15).content
        with open(final_path, 'wb') as f:
            f.write(img_data)

        print(f"    ✅ SAVED: {safe_name}{ext}")
        time.sleep(1) # Still good practice to be polite to the API

    except Exception as e:
        print(f"    ❌ CRASH: Error -> {e}")

# --- Test it on your missing list ---
# missing = ["Glorious Hand", "Luxury Hand", "Opulent Hand", "Cowardly Maya"]
# for enemy in missing:
#     # Using "P3" prefix since you are pulling the P3FES data
#     api_download_enemy(enemy, game_prefix="P3", save_folder="p3fes_enemies")