import json
import urllib.request
import urllib.parse
import os
import re
import time

def search_fandom_images(persona_name):
    name = persona_name.replace(" ", "_").replace('"', '')
    url = f"https://megamitensei.fandom.com/api.php?action=query&prop=images&titles={urllib.parse.quote(name)}&gimlimit=100&format=json"
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
    try:
        with urllib.request.urlopen(req) as response:
            data = json.loads(response.read().decode())
    except Exception:
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
    except Exception:
        return None
    pages = data.get("query", {}).get("pages", {})
    for page_id, page_info in pages.items():
        imageinfo = page_info.get("imageinfo", [])
        if imageinfo:
            return imageinfo[0].get("url")
    return None

def score_image_title(title, persona_name, game_hint):
    title_lower = title.lower()
    score = 0
    
    if "p5r" in title_lower: score += 100
    if "p5" in title_lower and "p5s" not in title_lower and "p5x" not in title_lower: score += 80
    
    if "portrait" in title_lower: score += 30
    if "render" in title_lower: score += 15
    if "model" in title_lower: score += 10
    if "battle" in title_lower: score += 5
    
    name_parts = persona_name.lower().split()
    if any(part in title_lower for part in name_parts):
        score += 20
        
    if game_hint in title_lower:
        score += 30
        
    return score

def find_best_image_url(persona_name, game_hint):
    search_name = persona_name
    titles = search_fandom_images(search_name)
    
    if not titles:
        # Search API directly as fallback
        url = f"https://megamitensei.fandom.com/api.php?action=query&list=search&srsearch={urllib.parse.quote(search_name)}&srnamespace=6&format=json"
        req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
        try:
            with urllib.request.urlopen(req) as response:
                data = json.loads(response.read().decode())
                titles = [t["title"] for t in data.get("query", {}).get("search", [])]
        except Exception:
            pass

    if not titles:
        return None
            
    valid_titles = [t for t in titles if t.lower().endswith(('.png', '.jpg', '.webp'))]
    if not valid_titles: return None
    
    scored = [(t, score_image_title(t, persona_name, game_hint)) for t in valid_titles]
    scored.sort(key=lambda x: x[1], reverse=True)
    
    best_title = scored[0][0]
    return get_image_url(best_title)

def main():
    files = [
        r'c:\Users\omare\Music\Persona-Companion-App\persona-companion-app\app\src\main\assets\data\enemies\p5_enemies.json',
        r'c:\Users\omare\Music\Persona-Companion-App\persona-companion-app\app\src\main\assets\data\enemies\p5r_enemies.json'
    ]
    
    total_updated = 0
    for path in files:
        if not os.path.exists(path):
            continue
            
        with open(path, 'r', encoding='utf-8') as f:
            data = json.load(f)
            
        updated = 0
        for item in data:
            if item.get('isBoss') or item.get('isMiniBoss'):
                if not item.get('image'):
                    name = item['name']
                    print(f"Fetching image for {name}...")
                    url = find_best_image_url(name, 'p5')
                    
                    if not url and "Shadow" not in name:
                        url = find_best_image_url("Shadow " + name, 'p5')
                        
                    # specific overrides
                    if not url and name == "Cognitive Wakaba Isshiki":
                        url = find_best_image_url("Wakaba Isshiki", 'p5')
                    if not url and name == "Holy Grail":
                        url = find_best_image_url("Holy Grail Boss", 'p5')
                    
                    if url:
                        print(f"  -> Found: {url}")
                        item['image'] = url
                        updated += 1
                    else:
                        print(f"  -> [!] Not found")
                    time.sleep(0.1)
                    
        if updated > 0:
            with open(path, 'w', encoding='utf-8') as f:
                json.dump(data, f, indent=2, ensure_ascii=False)
            print(f"Updated {updated} bosses in {os.path.basename(path)}")
            total_updated += updated

if __name__ == '__main__':
    main()
