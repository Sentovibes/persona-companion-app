import json
import urllib.request
import urllib.parse
import os
import re
import time

P5_DATA_DIR = r"c:\Users\omare\Music\Persona-Companion-App\persona-companion-app\web\data\persona5"
FILES = ["personas.json", "royal_personas.json"]

# Helper to normalize persona names for wiki URLs
def normalize_name(name):
    return name.replace(" ", "_")

def search_fandom_images(persona_name):
    # Some common aliases or translations
    name = normalize_name(persona_name)
    url = f"https://megamitensei.fandom.com/api.php?action=query&prop=images&titles={urllib.parse.quote(name)}&gimlimit=100&format=json"
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
    try:
        with urllib.request.urlopen(req) as response:
            data = json.loads(response.read().decode())
    except Exception as e:
        print(f"Error fetching {name}: {e}")
        return None
    
    pages = data.get("query", {}).get("pages", {})
    titles = []
    for page_id, page_info in pages.items():
        if page_id == "-1":
            continue
        images = page_info.get("images", [])
        for img in images:
            titles.append(img["title"])
    return titles

def get_image_url(file_title):
    url = f"https://megamitensei.fandom.com/api.php?action=query&prop=imageinfo&iiprop=url&titles={urllib.parse.quote(file_title)}&format=json"
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
    try:
        with urllib.request.urlopen(req) as response:
            data = json.loads(response.read().decode())
    except Exception as e:
        return None
    pages = data.get("query", {}).get("pages", {})
    for page_id, page_info in pages.items():
        imageinfo = page_info.get("imageinfo", [])
        if imageinfo:
            return imageinfo[0].get("url")
    return None

def score_image_title(title, persona_name):
    title_lower = title.lower()
    score = 0
    # Prefer portraits and models from latest games
    if "p5r" in title_lower: score += 100
    if "p5" in title_lower and "p5s" not in title_lower and "p5x" not in title_lower: score += 80
    if "p3r" in title_lower: score += 70
    if "p4g" in title_lower or "p4" in title_lower: score += 50
    if "p3" in title_lower: score += 40
    
    if "portrait" in title_lower: score += 20
    if "render" in title_lower: score += 15
    if "model" in title_lower: score += 10
    
    # Needs to likely contain the name
    name_parts = persona_name.lower().split()
    if any(part in title_lower for part in name_parts):
        score += 5
        
    return score

def find_best_image_url(persona_name):
    # Handle known redirects/different names
    search_name = persona_name
    if persona_name == "Arsene": search_name = "Arsène"
    if persona_name == "Seiten Taisei A": search_name = "Seiten Taisei"
    if persona_name == "Loki A": search_name = "Loki"
    
    titles = search_fandom_images(search_name)
    if not titles:
        # try without picaro
        if "Picaro" in persona_name:
            search_name = persona_name.replace(" Picaro", "")
            titles = search_fandom_images(search_name)
        if not titles:
            return None
            
    # Filter to likely image files
    valid_titles = [t for t in titles if t.lower().endswith(('.png', '.jpg', '.webp'))]
    
    if not valid_titles: return None
    
    # Score titles
    scored = [(t, score_image_title(t, persona_name)) for t in valid_titles]
    scored.sort(key=lambda x: x[1], reverse=True)
    
    best_title = scored[0][0]
    return get_image_url(best_title)

def main():
    for filename in FILES:
        filepath = os.path.join(P5_DATA_DIR, filename)
        with open(filepath, 'r', encoding='utf-8') as f:
            personas = json.load(f)
            
        updated = 0
        for name, data_ in personas.items():
            # if we already have a great image, skip?
            # "rescrape all persona images" -> user wants them all re-scraped
            print(f"Fetching for {name}...")
            url = find_best_image_url(name)
            if url:
                data_["image"] = url
                updated += 1
                time.sleep(0.2) # be nice to the API
            else:
                print(f"Could not find image for {name}")
                
        with open(filepath, 'w', encoding='utf-8') as f:
            json.dump(personas, f, indent=2, ensure_ascii=False)
            
        print(f"Updated {updated} entries in {filename}")

if __name__ == '__main__':
    main()
