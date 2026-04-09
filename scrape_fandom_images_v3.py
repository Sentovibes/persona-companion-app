import json
import urllib.request
import urllib.parse
import os
from concurrent.futures import ThreadPoolExecutor, as_completed

BASE_DIR = r"c:\Users\omare\Music\Persona-Companion-App\persona-companion-app\web\data"
TARGET_FOLDERS = {
    "persona3": "p3",
    "persona4": "p4",
    "persona5": "p5"
}

def search_fandom_images(persona_name):
    name = persona_name.replace(" ", "_")
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
    if "p3r" in title_lower: score += 90
    if "p4g" in title_lower: score += 80
    if "p4" in title_lower: score += 60
    if "p3" in title_lower: score += 50
    
    if "portrait" in title_lower: score += 30
    if "render" in title_lower: score += 15
    if "model" in title_lower: score += 10
    
    name_parts = persona_name.lower().split()
    if any(part in title_lower for part in name_parts):
        score += 20
        
    if game_hint in title_lower:
        score += 30
    return score

def find_best_image_url(persona_name, game_hint):
    search_name = persona_name
    if persona_name == "Arsene": search_name = "Arsène"
    if persona_name == "Seiten Taisei A": search_name = "Seiten Taisei"
    if persona_name == "Loki A": search_name = "Loki"
    
    titles = search_fandom_images(search_name)
    if not titles and "Picaro" in persona_name:
        search_name = persona_name.replace(" Picaro", "")
        titles = search_fandom_images(search_name)
    if not titles:
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
    
    return get_image_url(scored[0][0])

def process_persona(name, data_, game_hint):
    if "image" in data_ and data_["image"]: return name, None
    url = find_best_image_url(name, game_hint)
    return name, url

def main():
    updated_total = 0
    with ThreadPoolExecutor(max_workers=20) as executor:
        for folder, game_hint in TARGET_FOLDERS.items():
            folder_path = os.path.join(BASE_DIR, folder)
            if not os.path.isdir(folder_path): continue
            for filename in os.listdir(folder_path):
                if not filename.endswith('.json'): continue
                if any(x in filename for x in ["social_links", "enemies", "classroom"]): continue
                
                filepath = os.path.join(folder_path, filename)
                try:
                    with open(filepath, 'r', encoding='utf-8') as f:
                        personas = json.load(f)
                except:
                    continue
                    
                print(f"Processing {filepath}... starting threads")
                futures = {executor.submit(process_persona, name, data_, game_hint): name for name, data_ in personas.items()}
                
                updated = 0
                for future in as_completed(futures):
                    name, url = future.result()
                    if url:
                        personas[name]["image"] = url
                        updated += 1
                        
                if updated > 0:
                    with open(filepath, 'w', encoding='utf-8') as f:
                        json.dump(personas, f, indent=2, ensure_ascii=False)
                    print(f"-> Updated {updated} entries in {filename}")
                    updated_total += updated

    print(f"Done. Total updated: {updated_total}")

if __name__ == '__main__':
    main()
