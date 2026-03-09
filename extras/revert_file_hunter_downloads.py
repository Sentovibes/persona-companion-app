import requests
import json
import os
import time

HEADERS = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    'Accept': 'application/json',
    'Referer': 'https://megamitensei.fandom.com/'
}

# The absolute path to your app's data folder
BASE_DIR = r"C:\Users\omare\Music\Persona-Companion-App\persona-companion-app\app\src\main\assets\data"

# Translates your JSON names to the exact Fandom database names
NAME_MAP = {
    "Jack-o'-Lantern": "Pyro Jack", 
    "Bugs": "Bugbear",
    "Kushinada": "Kushinada-Hime",
    "Yatagarasu": "Yatagarasu",
    "Ho-Oh": "Ho-Ou",
    "Hitokoto-Nushi": "Hitokotonushi",
    "Niddhoggr": "Nidhogg",
    "Take-Minakata": "Takeminakata",
    "Ongyo-Ki": "Ongyo-ki",
    "Yomotsu-Ikusa": "Yomotsu-ikusa",
    "Baal Zebul": "Baal_Zebul",
    "Cu Chulainn": "Cu_Chulainn",
    "Scathach": "Scathach"
}

def score_universal_render(title, base_name):
    """Tier List Scorer: Finds the best possible transparent render."""
    title_lower = title.lower()
    score = 0
    
    # 1. ABSOLUTE BANS (Instant Rejection)
    if not title_lower.endswith('.png'):
        return -10000 
        
    toxic_terms = [
        'dx2', 'liberation', 'anime', 'manga', 'episode', 'screenshot', 
        'card', 'tcg', 'sprite', 'concept', 'bg', 'background', 'censored',
        'movie', 'stage', 'p4a', 'arena', 'd2', 'teppen', 'star_ocean', 
        'identity_v', 'collab', 'icon', 'portrait', 'face', 'strikers_shadow'
    ]
    for term in toxic_terms:
        if term in title_lower:
            return -10000

    # =========================================================
    # 2. STRICT NAME MATCHING
    # =========================================================
    # Normalize the strings: remove "file:", remove extension, swap _ and - for spaces
    clean_title = title_lower.replace("file:", "").split(".")[0].replace("_", " ").replace("-", " ")
    clean_base = base_name.lower().replace("_", " ").replace("-", " ")
    
    # If the Persona's name isn't even in the file title, NUKE IT.
    if clean_base not in clean_title:
        return -10000
        
    score += 1000 # Passed the name check!

    # =========================================================
    # 3. TIER LIST SCORING (Game Priority)
    # =========================================================
    if 'p5x' in title_lower: score += 3000
    elif 'p5r' in title_lower or 'royal' in title_lower: score += 3000
    elif 'p3r' in title_lower or 'reload' in title_lower: score += 3000
    elif 'p4g' in title_lower or 'golden' in title_lower: score += 2800
    elif 'p5' in title_lower or 'persona_5' in title_lower: score += 2000
    elif 'p4' in title_lower or 'persona_4' in title_lower: score += 1800
    
    if 'smtv' in title_lower or 'smt5' in title_lower or 'vengeance' in title_lower: score += 1500
    elif 'smt4' in title_lower or 'smtiv' in title_lower or 'sj' in title_lower: score += 1000
    elif 'smt3' in title_lower or 'nocturne' in title_lower or 'smt' in title_lower: score += 500

    # 4. QUALITY KEYWORDS
    if 'render' in title_lower: score += 500
    if 'transparent' in title_lower: score += 500
    if 'art' in title_lower: score += 200
    if 'model' in title_lower: score -= 200

    # 5. DLC/Picaro matching
    if "picaro" in clean_base:
        if "picaro" in clean_title: score += 5000
        else: score -= 5000

    return score

def get_best_global_file(name):
    api_url = "https://megamitensei.fandom.com/api.php"
    search_queries = [f"{name} render", name]
    
    best_file = None
    highest_score = -9999
    
    for query in search_queries:
        params = {
            "action": "query",
            "list": "search",
            "srsearch": query,
            "srnamespace": 6,
            "srlimit": 50,
            "format": "json"
        }
        
        try:
            response = requests.get(api_url, params=params, headers=HEADERS, timeout=10)
            if response.status_code == 200:
                results = response.json().get("query", {}).get("search", [])
                
                for res in results:
                    title = res["title"]
                    score = score_universal_render(title, name)
                    
                    if score > highest_score:
                        highest_score = score
                        best_file = title
        except:
            pass
            
        if highest_score >= 2000:
            break

    if highest_score >= 0:
        return best_file, highest_score
    return None, highest_score

def get_direct_url(file_title):
    api_url = "https://megamitensei.fandom.com/api.php"
    params = {
        "action": "query",
        "titles": file_title,
        "prop": "imageinfo",
        "iiprop": "url", 
        "format": "json"
    }
    try:
        response = requests.get(api_url, params=params, headers=HEADERS, timeout=10)
        if response.status_code == 200:
            pages = response.json().get("query", {}).get("pages", {})
            for page_id, page_info in pages.items():
                if "imageinfo" in page_info:
                    # Do NOT strip the /revision/ tag! The CDN needs it to serve the real image.
                    return page_info["imageinfo"][0]["url"]
    except:
        pass
    return None

def download_image(url, output_dir, safe_name):
    if not url: return "FAILED"
    filepath = os.path.join(output_dir, f"{safe_name}.png")
    
    if os.path.exists(filepath):
        return "SKIP"

    # We need specific image headers to bypass the Fandom CDN
    image_headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
        'Accept': 'image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8',
        'Referer': 'https://megamitensei.fandom.com/'
    }

    try:
        response = requests.get(url, headers=image_headers, timeout=15)
        
        # Check if Fandom served us an HTML error page disguised as an image
        if "text/html" in response.headers.get("Content-Type", ""):
            return "FAILED (Cloudflare Block)"
            
        if response.status_code == 200:
            with open(filepath, 'wb') as f:
                f.write(response.content)
            
            # Extra safety check: if the image is less than 5KB, it's a dummy placeholder
            if os.path.getsize(filepath) < 5000:
                os.remove(filepath)
                return "FAILED (Fake Placeholder Caught & Deleted)"
                
            return "OK"
    except:
        pass
    return "ERROR"

def process_app_json(json_path, output_dir):
    if not os.path.exists(json_path):
        print(f"[!] Path not found, skipping: {json_path}")
        return

    with open(json_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
        
    if isinstance(data, dict):
        targets = list(data.keys())
    else:
        targets = [t.get('name') for t in data if t.get('name')]
        
    if not targets: return
    
    # We save images relative to where you run the script, in an /images folder
    os.makedirs(output_dir, exist_ok=True)
    print(f"\n=== PROCESSING: {os.path.basename(json_path)} ({len(targets)} targets) ===")
    
    for i, raw_name in enumerate(targets, 1):
        if any(skip in raw_name for skip in ["Hand", "Maya", "Dice", "Table", "Relic", "Beetle"]):
            continue
            
        print(f"[{i}/{len(targets)}] {raw_name}... ", end="", flush=True)
        safe_name = raw_name.replace("/", "_").replace(":", "").replace("?", "")
        
        search_name = raw_name.replace(" Picaro", "") if " Picaro" in raw_name else raw_name
        search_name = NAME_MAP.get(search_name, search_name)
        
        # Check if already downloaded to save API calls
        if os.path.exists(os.path.join(output_dir, f"{safe_name}.png")):
            print("SKIP (Already downloaded)")
            continue
        
        best_file, score = get_best_global_file(search_name)
        
        if best_file:
            img_url = get_direct_url(best_file)
            if img_url:
                res = download_image(img_url, output_dir, safe_name)
                print(f"{res} (Score: {score} | {best_file.replace('File:', '')})")
            else:
                print("URL EXTRACT FAILED")
        else:
            print(f"REJECTED (No clean transparent renders found in Wiki database)")
            
        time.sleep(0.4)

if __name__ == '__main__':
    print("==================================================")
    print(" Ultimate 100% App Completion Image Scraper v8.2  ")
    print(f" Reading from: {BASE_DIR}")
    print("==================================================")
    
    # 100% OF ALL REPOSITORY FILES
    JOBS = [
        # PERSONA 3 (All Versions)
        (os.path.join(BASE_DIR, 'persona3', 'personas.json'), 'images/personas/p3fes'),
        (os.path.join(BASE_DIR, 'persona3', 'portable_personas.json'), 'images/personas/p3p'),
        (os.path.join(BASE_DIR, 'persona3', 'reload_personas.json'), 'images/personas/p3r'),
        (os.path.join(BASE_DIR, 'enemies', 'p3fes_enemies.json'), 'images/enemies/p3fes'),
        (os.path.join(BASE_DIR, 'enemies', 'p3fes_bosses.json'), 'images/enemies/p3fes'),
        (os.path.join(BASE_DIR, 'enemies', 'p3fes_main_bosses.json'), 'images/enemies/p3fes'),
        (os.path.join(BASE_DIR, 'enemies', 'p3fes_mini_bosses.json'), 'images/enemies/p3fes'),
        (os.path.join(BASE_DIR, 'enemies', 'p3p_enemies.json'), 'images/enemies/p3p'),
        (os.path.join(BASE_DIR, 'enemies', 'p3p_main_bosses.json'), 'images/enemies/p3p'),
        (os.path.join(BASE_DIR, 'enemies', 'p3p_mini_bosses.json'), 'images/enemies/p3p'),
        (os.path.join(BASE_DIR, 'enemies', 'p3r_enemies.json'), 'images/enemies/p3r'),
        
        # PERSONA 4 (Vanilla & Golden)
        (os.path.join(BASE_DIR, 'persona4', 'personas.json'), 'images/personas/p4'),
        (os.path.join(BASE_DIR, 'persona4', 'golden_personas.json'), 'images/personas/p4g'),
        (os.path.join(BASE_DIR, 'enemies', 'p4_enemies.json'), 'images/enemies/p4'),
        (os.path.join(BASE_DIR, 'enemies', 'p4_bosses.json'), 'images/enemies/p4'),
        (os.path.join(BASE_DIR, 'enemies', 'p4g_enemies.json'), 'images/enemies/p4g'),
        (os.path.join(BASE_DIR, 'enemies', 'p4g_bosses.json'), 'images/enemies/p4g'),
        
        # PERSONA 5 (Vanilla & Royal)
        (os.path.join(BASE_DIR, 'persona5', 'personas.json'), 'images/personas/p5'),
        (os.path.join(BASE_DIR, 'persona5', 'royal_personas.json'), 'images/personas/p5r'),
        (os.path.join(BASE_DIR, 'enemies', 'p5_enemies.json'), 'images/enemies/p5'),
        (os.path.join(BASE_DIR, 'enemies', 'p5_bosses.json'), 'images/enemies/p5'),
        (os.path.join(BASE_DIR, 'enemies', 'p5r_enemies.json'), 'images/enemies/p5r'),
        (os.path.join(BASE_DIR, 'enemies', 'p5r_bosses.json'), 'images/enemies/p5r')
    ]
    
    print("\nPress ENTER to start the massive scrape across all JSON files...")
    input()
    
    for json_path, out_dir in JOBS:
        process_app_json(json_path, out_dir)
        
    print("\n[+] 100% Global Image Download Complete!")