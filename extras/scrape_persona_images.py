import requests
import json
import os
import time
from urllib.parse import quote
from bs4 import BeautifulSoup

HEADERS = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
}

def clean_fandom_url(url):
    """Strips Fandom's resizing and scaling parameters to get the raw high-res image."""
    if '/revision/' in url:
        return url.split('/revision/')[0]
    return url

def get_file_extension(url):
    """Safely extracts the file extension from the raw URL."""
    ext = os.path.splitext(url)[1].lower()
    if ext not in ['.png', '.jpg', '.jpeg', '.webp', '.gif']:
        return '.png'
    return ext

def get_wiki_image_url(name):
    """
    Scrapes the Megami Tensei Wiki.
    Uses the powerful 'og:image' meta tag to pull images even when there is no infobox.
    """
    search_name = name.replace(" ", "_")
    
    wiki_urls = [
        f"https://megamitensei.fandom.com/wiki/{quote(search_name)}",
        f"https://megamitensei.fandom.com/wiki/{quote(search_name)}_(Persona)",
        f"https://megamitensei.fandom.com/wiki/{quote(search_name)}_(Demon)",
        f"https://megamitensei.fandom.com/wiki/{quote(search_name)}_(Shadow)"
    ]
    
    # Special Fallbacks for generic enemies that share a single wiki page
    if "Hand" in name and name not in ["Killing Hand", "God's Hand"]:
        wiki_urls.append("https://megamitensei.fandom.com/wiki/Hand_(Shadow)")
    if "Maya" in name:
        wiki_urls.append("https://megamitensei.fandom.com/wiki/Maya_(Shadow)")
    if "Dice" in name:
        wiki_urls.append("https://megamitensei.fandom.com/wiki/Dice_(Shadow)")
    if "Relic" in name:
        wiki_urls.append("https://megamitensei.fandom.com/wiki/Relic_(Shadow)")
    if "Table" in name:
        wiki_urls.append("https://megamitensei.fandom.com/wiki/Table_(Shadow)")
        
    for url in wiki_urls:
        try:
            response = requests.get(url, headers=HEADERS, timeout=10)
            if response.status_code == 200:
                soup = BeautifulSoup(response.text, 'html.parser')
                
                # 1. The Ultimate Fandom Trick: Open Graph Image
                og_image = soup.find('meta', property='og:image')
                if og_image and og_image.has_attr('content'):
                    img_url = og_image['content']
                    # Ignore it if Fandom returns the generic site logo placeholder
                    if "site-logo" not in img_url and "wiki-domain-image" not in img_url:
                        return clean_fandom_url(img_url)
                
                # 2. Fallback to Infobox parsing (Just in case)
                infobox = soup.find('aside', class_='portable-infobox')
                if infobox:
                    img_tag = infobox.find('img', class_='pi-image-thumbnail')
                    if img_tag:
                        parent_a = img_tag.find_parent('a')
                        if parent_a and parent_a.has_attr('href'):
                            return clean_fandom_url(parent_a['href'])
                        elif img_tag.has_attr('src'):
                            return clean_fandom_url(img_tag['src'])
        except:
            pass
            
    return None

def download_image(url, output_dir, name):
    """Downloads the image, renaming it safely and skipping if it already exists."""
    ext = get_file_extension(url)
    safe_name = name.replace("/", "_").replace(":", "").replace("?", "")
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

def scrape_personas():
    """Reads JSON files and downloads Persona images."""
    games = {
        'p3r': '../app/src/main/assets/data/persona3reload/reload_personas.json',
        'p4g': '../app/src/main/assets/data/persona4/golden_personas.json',
        'p5r': '../app/src/main/assets/data/persona5/royal_personas.json',
        'p5s': '../app/src/main/assets/data/persona5strikers/strikers_personas.json'
    }
    
    for game, filepath in games.items():
        if not os.path.exists(filepath):
            continue
            
        with open(filepath, 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        # Handle both list and dict JSON formats
        if isinstance(data, dict):
            personas = list(data.keys()) 
        else:
            personas = [p.get('name') for p in data if p.get('name')]
            
        output_dir = f'images/personas/{game}'
        os.makedirs(output_dir, exist_ok=True)
        
        print(f"\n=== {game.upper()} Personas ({len(personas)} total) ===")
        
        for i, name in enumerate(personas, 1):
            print(f"[{i}/{len(personas)}] {name}... ", end="", flush=True)
            
            img_url = get_wiki_image_url(name)
            if img_url:
                result = download_image(img_url, output_dir, name)
                print(result)
            else:
                print("NO PAGE")
            time.sleep(0.5)

def scrape_enemies():
    """Reads JSON files and downloads Enemy & Boss images."""
    files_to_check = [
        ('../app/src/main/assets/data/enemies/p3r_main_bosses.json', 'p3r'),
        ('../app/src/main/assets/data/enemies/p3r_mini_bosses.json', 'p3r'),
        ('../app/src/main/assets/data/enemies/p3r_enemies.json', 'p3r'),
        ('../app/src/main/assets/data/enemies/p4g_bosses.json', 'p4g'),
        ('../app/src/main/assets/data/enemies/p4g_enemies.json', 'p4g'),
        ('../app/src/main/assets/data/enemies/p5r_bosses.json', 'p5r'),
        ('../app/src/main/assets/data/enemies/p5r_enemies.json', 'p5r'),
        ('../app/src/main/assets/data/enemies/p5s_enemies.json', 'p5s'),
        ('../app/src/main/assets/data/enemies/p3fes_enemies.json', 'p3fes')
    ]
    
    for filepath, game in files_to_check:
        if not os.path.exists(filepath):
            continue
            
        with open(filepath, 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        # Safely extract names whether the JSON is an Array or Dictionary
        entities = []
        if isinstance(data, dict):
            entities = list(data.keys())
        elif isinstance(data, list):
            entities = [e.get('name') for e in data if e.get('name')]
            
        if not entities:
            continue
            
        output_dir = f'images/enemies/{game}'
        os.makedirs(output_dir, exist_ok=True)
        
        print(f"\n=== {game.upper()} Targets from {os.path.basename(filepath)} ({len(entities)} total) ===")
        
        for i, name in enumerate(entities, 1):
            print(f"[{i}/{len(entities)}] {name}... ", end="", flush=True)
            
            img_url = get_wiki_image_url(name)
            if img_url:
                result = download_image(img_url, output_dir, name)
                print(result)
            else:
                print("NO PAGE")
            time.sleep(0.5)

if __name__ == '__main__':
    print("==============================================")
    print(" Ultimate Persona Image Scraper (v3.0)        ")
    print("==============================================")
    
    # Run from the extras folder, ensure images directory exists
    os.makedirs('images', exist_ok=True)
    
    choice = input("\n1. Personas\n2. Enemies & Bosses\n3. Both\nChoice (1/2/3): ")
    
    if choice in ['1', '3']:
        scrape_personas()
    if choice in ['2', '3']:
        scrape_enemies()
    
    print("\n[DONE] Check the images/ folder.")