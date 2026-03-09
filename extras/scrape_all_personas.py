import requests
import json
import os
import time
import re

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
    title_lower = title.lower()
    score = 0
    
    # 1. ABSOLUTE BANS (Instant Rejection)
    if not title_lower.endswith('.png'):
        return -10000 
        
    toxic_terms = [
        'dx2', 'liberation', 'anime', 'manga', 'episode', 'screenshot', 
        'card', 'tcg', 'sprite', 'concept', 'bg', 'background', 'censored',
        'movie', 'stage', 'p4a', 'arena', 'd2', 'teppen', 'star_ocean', 
        'identity_v', 'collab', 'icon', 'portrait', 'face', 'strikers_shadow',
        'all_out_attack', 'aoa' # Banning the All-Out Attack screens that plagued you
    ]
    for term in toxic_terms:
        if term in title_lower:
            return -10000

    # =========================================================
    # 2. THE V9.0 ENFORCER: STRICT NAME MATCHING
    # =========================================================
    # Remove "file:" and ".png", then strip all special characters
    clean_title = re.sub(r'^file:', '', title_lower).split('.png')[0]
    title_solid = re.sub(r'[^a-z0-9]', '', clean_title)
    base_solid = re.sub(r'[^a-z0-9]', '', base_name.lower())
    
    # If the Persona's name isn't in the file title, DESTROY IT.
    if base_solid not in title_solid:
        return -10000
        
    # Prevent Sub-String Traps (Angel matching Archangel, Pixie matching High Pixie)
    if base_solid == "angel" and "archangel" in title_solid: return -10000
    if base_solid == "pixie" and "highpixie" in title_solid: return -10000
    if base_solid == "frost" and any(x in title_solid for x in ["jack", "king", "black"]): return -10000
    if base_solid == "maya" and "cowardly" in title_solid: return -10000
        
    score += 1000 # Passed the Enforcer check!

    # =========================================================
    # 3. TIER LIST SCORING (Fixed Elif logic to prevent stacking)
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
    if "picaro" in base_solid:
        if "picaro" in title_solid: score += 5000
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
                    return page_info["imageinfo"][0]["url"]
    except:
        pass
    return None

def download_image(url, output_dir, safe_name):
    if not url: return "FAILED"
    filepath = os.path.join(output_dir, f"{safe_name}.png")
    
    if os.path.exists(filepath):
        return "SKIP"

    image_headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
        'Accept': 'image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8',
        'Referer': 'https://megamitensei.fandom.com/'
    }

    try:
        response = requests.get(url, headers=image_headers, timeout=15)
        
        if "text/html" in response.headers.get("Content-Type", ""):
            return "FAILED (Cloudflare Block)"
            
        if response.status_code == 200:
            with open(filepath, 'wb') as f:
                f.write(response.content)
            
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
    
    os.makedirs(output_dir, exist_ok=True)
    print(f"\n=== PROCESSING: {os.path.basename(json_path)} ({len(targets)} targets) ===")
    
    for i, raw_name in enumerate(targets, 1):
        if any(skip in raw_name for skip in ["Hand", "Maya", "Dice", "Table", "Relic", "Beetle"]):
            continue
            
        print(f"[{i}/{len(targets)}] {raw_name}... ", end="", flush=True)
        safe_name = raw_name.replace("/", "_").replace(":", "").replace("?", "")
        
        search_name = raw_name.replace(" Picaro", "") if " Picaro" in raw_name else raw_name
        search_name = NAME_MAP.get(search_name, search_name)
        
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
    print(" [V9.0 ENFORCER] Ultimate Image Scraper           ")
    print(f" Reading from: {BASE_DIR}")
    print("==================================================")
    
    JOBS = [
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
        (os.path.join(BASE_DIR, 'persona4', 'personas.json'), 'images/personas/p4'),
        (os.path.join(BASE_DIR, 'persona4', 'golden_personas.json'), 'images/personas/p4g'),
        (os.path.join(BASE_DIR, 'enemies', 'p4_enemies.json'), 'images/enemies/p4'),
        (os.path.join(BASE_DIR, 'enemies', 'p4_bosses.json'), 'images/enemies/p4'),
        (os.path.join(BASE_DIR, 'enemies', 'p4g_enemies.json'), 'images/enemies/p4g'),
        (os.path.join(BASE_DIR, 'enemies', 'p4g_bosses.json'), 'images/enemies/p4g'),
        (os.path.join(BASE_DIR, 'persona5', 'personas.json'), 'images/personas/p5'),
        (os.path.join(BASE_DIR, 'persona5', 'royal_personas.json'), 'images/personas/p5r'),
        (os.path.join(BASE_DIR, 'enemies', 'p5_enemies.json'), 'images/enemies/p5'),
        (os.path.join(BASE_DIR, 'enemies', 'p5_bosses.json'), 'images/enemies/p5'),
        (os.path.join(BASE_DIR, 'enemies', 'p5r_enemies.json'), 'images/enemies/p5r'),
        (os.path.join(BASE_DIR, 'enemies', 'p5r_bosses.json'), 'images/enemies/p5r')
    ]
    
    print("\nMake sure you deleted the wrong images from your folders before starting!")
    print("Press ENTER to start...")
    input()
    
    for json_path, out_dir in JOBS:
        process_app_json(json_path, out_dir)