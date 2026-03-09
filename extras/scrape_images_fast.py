import requests
import json
import os
import time
from urllib.parse import quote
from bs4 import BeautifulSoup

HEADERS = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
}

# Fixes for names that differ between your JSON and the Wiki
NAME_MAP = {
    "Jack-o'-Lantern": "Pyro Jack", # Fandom uses Pyro Jack for the main URL
    "Bugs": "Bugbear",
    "Kushinada": "Kushinada-Hime",
    "Yatagarasu": "Yatagarasu",
    "Ho-Oh": "Ho-Ou",
    "Macabre": "Macabre",
}

def clean_fandom_url(url):
    """Strip resizing parameters for max resolution."""
    if '/revision/' in url:
        return url.split('/revision/')[0]
    return url

def get_file_extension(url):
    ext = os.path.splitext(url.split('?')[0])[1].lower()
    if ext not in ['.png', '.jpg', '.jpeg']:
        return '.png'
    return ext

def score_image(url, game, modifiers):
    """
    Scores an image URL based on how closely it matches the game we are scraping for.
    This ensures we prioritize modern Persona art over old SMT art.
    """
    url_lower = url.lower()
    score = 0
    
    # 1. Match the specific game (more flexible now)
    if game in ['p5', 'p5r', 'p5s']:
        # Accept any P5 variant (P5R, P5, P5X, P5S)
        if 'p5r' in url_lower or 'royal' in url_lower: score += 100
        elif 'p5x' in url_lower: score += 90  # P5X is also good modern art
        elif 'p5s' in url_lower or 'strikers' in url_lower: score += 85
        elif 'p5' in url_lower or 'persona_5' in url_lower: score += 80
        # Don't heavily penalize other Persona games, just prefer P5
        elif 'p4' in url_lower or 'p3' in url_lower: score += 20
        # Only penalize SMT games
        elif 'smt' in url_lower or 'smtv' in url_lower: score -= 50
    
    elif game in ['p4', 'p4g']:
        # Accept any P4 variant
        if 'p4g' in url_lower or 'golden' in url_lower: score += 100
        elif 'p4' in url_lower or 'persona_4' in url_lower: score += 80
        # Don't heavily penalize other Persona games
        elif 'p5' in url_lower or 'p3' in url_lower: score += 20
        # Only penalize SMT games
        elif 'smt' in url_lower or 'smtv' in url_lower: score -= 50
        
    elif game in ['p3', 'p3r', 'p3fes', 'p3p']:
        # Accept any P3 variant
        if 'p3r' in url_lower or 'reload' in url_lower: score += 100
        elif 'p3p' in url_lower or 'portable' in url_lower: score += 90
        elif 'p3fes' in url_lower or 'fes' in url_lower: score += 85
        elif 'p3' in url_lower or 'persona_3' in url_lower: score += 80
        # Don't heavily penalize other Persona games
        elif 'p5' in url_lower or 'p4' in url_lower: score += 20
        # Only penalize SMT games
        elif 'smt' in url_lower or 'smtv' in url_lower: score -= 50

    # 2. Match Modifiers (CRITICAL for DLC/Picaro Personas)
    if modifiers:
        for mod in modifiers:
            if mod in url_lower:
                score += 500  # Massive boost if it has 'picaro' or 'female'
            else:
                score -= 200  # Penalize if a Picaro search grabs a non-Picaro image

    # 3. Preference for high quality renders over icons
    if 'render' in url_lower: score += 30
    if 'portrait' in url_lower or 'icon' in url_lower: score -= 40
    if 'sprite' in url_lower: score -= 50

    return score

def find_best_image(base_name, game, modifiers):
    """Scrapes the Wiki Gallery and returns the highest scoring image."""
    search_name = base_name.replace(" ", "_")
    
    # Check the Gallery page first (where the good renders are), then fallback to main pages
    urls_to_check = [
        f"https://megamitensei.fandom.com/wiki/{quote(search_name)}/Gallery",
        f"https://megamitensei.fandom.com/wiki/{quote(search_name)}_(Persona)/Gallery",
        f"https://megamitensei.fandom.com/wiki/{quote(search_name)}",
        f"https://megamitensei.fandom.com/wiki/{quote(search_name)}_(Demon)"
    ]
    
    best_url = None
    highest_score = -9999

    for wiki_url in urls_to_check:
        try:
            response = requests.get(wiki_url, headers=HEADERS, timeout=10)
            if response.status_code == 200:
                soup = BeautifulSoup(response.text, 'html.parser')
                
                # Find every image link on the page
                for a_tag in soup.find_all('a', class_='image'):
                    img_url = a_tag.get('href')
                    if not img_url:
                        continue
                    
                    img_url = clean_fandom_url(img_url)
                    score = score_image(img_url, game, modifiers)
                    
                    if score > highest_score:
                        highest_score = score
                        best_url = img_url
                        
                # If we found any valid image on this page, return the highest scoring one
                # (Removed the strict limit so older demons like Andras can fall back to SMT art)
                if best_url:
                    return best_url
        except Exception as e:
            pass
            
    return best_url

def download_image(url, output_dir, safe_name):
    if not url: return "MISSING"
    
    ext = get_file_extension(url)
    filepath = os.path.join(output_dir, f"{safe_name}{ext}")
    
    if os.path.exists(filepath):
        return "SKIP"
    
    try:
        response = requests.get(url, headers=HEADERS, timeout=15)
        if response.status_code == 200:
            with open(filepath, 'wb') as f:
                f.write(response.content)
            return "OK"
    except:
        pass
    return "FAIL"

def scrape_smart(game_id, json_path):
    """Processes a single JSON file."""
    if not os.path.exists(json_path):
        print(f"[!] File not found: {json_path}")
        return

    with open(json_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
        
    personas = list(data.keys()) if isinstance(data, dict) else [p.get('name') for p in data if p.get('name')]
    
    output_dir = f'images/personas/{game_id}'
    os.makedirs(output_dir, exist_ok=True)
    print(f"\n=== Smart Scraping {game_id.upper()} Personas ({len(personas)} total) ===")

    for i, raw_name in enumerate(personas, 1):
        print(f"[{i}/{len(personas)}] {raw_name}... ", end="", flush=True)
        
        # Strip illegal characters for the file system
        safe_name = raw_name.replace("/", "_").replace(":", "").replace("?", "")
        
        # Parse out DLC modifiers (so "Izanagi Picaro" searches the "Izanagi" page)
        base_name = raw_name
        modifiers = []
        if " Picaro" in raw_name:
            base_name = raw_name.replace(" Picaro", "")
            modifiers.append("picaro")
        if " F" in raw_name and raw_name == "Orpheus F":
            base_name = "Orpheus"
            modifiers.append("female")
            
        # Apply manual name maps (e.g. Jack-o'-Lantern -> Pyro Jack)
        base_name = NAME_MAP.get(base_name, base_name)
        
        best_url = find_best_image(base_name, game_id, modifiers)
        
        result = download_image(best_url, output_dir, safe_name)
        
        # Add visual feedback
        if result == "OK":
            print(f"OK (Downloaded highest scoring art)")
        else:
            print(result)
            
        time.sleep(0.5)

if __name__ == '__main__':
    print("==============================================")
    print(" Smart Persona Gallery Scraper (v5.1)         ")
    print(" (Hunts specific P5R/P4G renders & Picaros)   ")
    print("==============================================")
    
    # Check P4G
    scrape_smart('p4g', '../app/src/main/assets/data/persona4/golden_personas.json')
    
    # Check P5R
    scrape_smart('p5r', '../app/src/main/assets/data/persona5/royal_personas.json')
    
    print("\n[DONE] Run your checker script again!")