import requests
from bs4 import BeautifulSoup
import json
import re
import time

def parse_html_table(table):
    """
    Parses an HTML table into a 2D list, resolving rowspans and colspans.
    This is critical for Megaten wiki tables where Prompts span multiple rows.
    """
    rows = table.find_all('tr')
    
    # Find max columns to initialize grid
    max_cols = 0
    for row in rows:
        max_cols = max(max_cols, len(row.find_all(['th', 'td'])))
        
    grid = []
    for _ in rows:
        grid.append([])

    for row_idx, row in enumerate(rows):
        col_idx = 0
        for cell in row.find_all(['th', 'td']):
            # Skip filled cells from previous rowspans
            while col_idx < len(grid[row_idx]) and grid[row_idx][col_idx] is not None:
                col_idx += 1
                
            rowspan = int(cell.get('rowspan', 1))
            colspan = int(cell.get('colspan', 1))
            text = cell.get_text(separator=" ", strip=True)
            
            # Fill the grid for the spanned cells
            for r in range(rowspan):
                while len(grid[row_idx + r]) <= col_idx + colspan - 1:
                    grid[row_idx + r].append(None)
                for c in range(colspan):
                    grid[row_idx + r][col_idx + c] = text
            col_idx += colspan
            
    return grid

def extract_points(point_str):
    """Extracts integer points from strings like '♪3', '+3', or '3'."""
    match = re.search(r'\d+', str(point_str))
    if match:
        return int(match.group())
    return 0

def scrape_social_links(url, arcana_name):
    """Scrapes a specific character's page and builds the JSON."""
    print(f"  Scraping {arcana_name}...")
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
    }
    
    try:
        response = requests.get(url, headers=headers, timeout=10)
        if response.status_code != 200:
            print(f"    [!] Failed to fetch {url} (Status: {response.status_code})")
            return {}
    except Exception as e:
        print(f"    [!] Error fetching {url}: {e}")
        return {}

    soup = BeautifulSoup(response.text, 'html.parser')
    tables = soup.find_all('table', class_='wiki-table')
    
    arcana_data = {}
    current_rank = 1
    any_counter = 1
    
    for table in tables:
        grid = parse_html_table(table)
        if not grid or len(grid) < 2:
            continue
            
        # Check if this table looks like a Social Link/Confidant table
        headers_row = [str(h).lower() if h else "" for h in grid[0]]
        if not any('choice' in h or 'response' in h for h in headers_row) or not any('point' in h for h in headers_row):
            continue
            
        # Find column indices
        choice_idx = -1
        point_idx = -1
        for i, header in enumerate(headers_row):
            if 'choice' in header or 'response' in header:
                choice_idx = i
            elif 'point' in header:
                point_idx = i
                
        if choice_idx == -1 or point_idx == -1:
            continue

        rank_key = f"Rank {current_rank}"
        rank_data = {}
        
        for row in grid[1:]:
            if len(row) > choice_idx and len(row) > point_idx:
                choice_text = str(row[choice_idx]) if row[choice_idx] else ""
                points = extract_points(row[point_idx])
                
                # Format "Any" choices so they don't overwrite each other in JSON
                if choice_text.lower() == 'any' or choice_text == '-':
                    choice_text = f"Any_{any_counter}"
                    any_counter += 1
                
                # Ignore truly empty choices
                if choice_text and choice_text.strip() != "":
                    rank_data[choice_text] = points
        
        # Add next rank placeholder if we found choices
        if rank_data:
            arcana_data[rank_key] = {"Next Rank": 0, **rank_data}
            current_rank += 1
            any_counter = 1 # Reset Any counter for next rank

    time.sleep(0.5) # Be polite to the wiki server
    return arcana_data

if __name__ == "__main__":
    
    # --- MASSIVE PRE-POPULATED TARGET LISTS ---
    games = {
        "p3_scraped.json": [
            {"arcana": "Magician", "url": "https://megamitensei.fandom.com/wiki/Kenji_Tomochika"},
            {"arcana": "Priestess", "url": "https://megamitensei.fandom.com/wiki/Fuuka_Yamagishi"},
            {"arcana": "Empress", "url": "https://megamitensei.fandom.com/wiki/Mitsuru_Kirijo"},
            {"arcana": "Emperor", "url": "https://megamitensei.fandom.com/wiki/Hidetoshi_Odagiri"},
            {"arcana": "Hierophant", "url": "https://megamitensei.fandom.com/wiki/Bunkichi_and_Mitsuko"},
            {"arcana": "Lovers", "url": "https://megamitensei.fandom.com/wiki/Yukari_Takeba"},
            {"arcana": "Chariot", "url": "https://megamitensei.fandom.com/wiki/Kazushi_Miyamoto"},
            {"arcana": "Justice", "url": "https://megamitensei.fandom.com/wiki/Chihiro_Fushimi"},
            {"arcana": "Hermit", "url": "https://megamitensei.fandom.com/wiki/Isako_Toriumi"},
            {"arcana": "Fortune", "url": "https://megamitensei.fandom.com/wiki/Keisuke_Hiraga"},
            {"arcana": "Strength", "url": "https://megamitensei.fandom.com/wiki/Yuko_Nishiwaki"},
            {"arcana": "Hanged Man", "url": "https://megamitensei.fandom.com/wiki/Maiko_Oohashi"},
            {"arcana": "Temperance", "url": "https://megamitensei.fandom.com/wiki/Bebe"},
            {"arcana": "Devil", "url": "https://megamitensei.fandom.com/wiki/President_Tanaka"},
            {"arcana": "Tower", "url": "https://megamitensei.fandom.com/wiki/Mutatsu"},
            {"arcana": "Star", "url": "https://megamitensei.fandom.com/wiki/Mamoru_Hayase"},
            {"arcana": "Moon", "url": "https://megamitensei.fandom.com/wiki/Nozomi_Suemitsu"},
            {"arcana": "Sun", "url": "https://megamitensei.fandom.com/wiki/Akinari_Kamiki"},
            {"arcana": "Aeon", "url": "https://megamitensei.fandom.com/wiki/Aigis"}
        ],
        "p3p_femc_scraped.json": [
            {"arcana": "Magician", "url": "https://megamitensei.fandom.com/wiki/Junpei_Iori"},
            {"arcana": "Chariot", "url": "https://megamitensei.fandom.com/wiki/Rio_Iwasaki"},
            {"arcana": "Justice", "url": "https://megamitensei.fandom.com/wiki/Ken_Amada"},
            {"arcana": "Hermit", "url": "https://megamitensei.fandom.com/wiki/Saori_Hasegawa"},
            {"arcana": "Fortune", "url": "https://megamitensei.fandom.com/wiki/Ryoji_Mochizuki"},
            {"arcana": "Strength", "url": "https://megamitensei.fandom.com/wiki/Koromaru"},
            {"arcana": "Star", "url": "https://megamitensei.fandom.com/wiki/Akihiko_Sanada"},
            {"arcana": "Moon", "url": "https://megamitensei.fandom.com/wiki/Shinjiro_Aragaki"}
        ],
        "p4_scraped.json": [
            {"arcana": "Magician", "url": "https://megamitensei.fandom.com/wiki/Yosuke_Hanamura"},
            {"arcana": "Priestess", "url": "https://megamitensei.fandom.com/wiki/Yukiko_Amagi"},
            {"arcana": "Emperor", "url": "https://megamitensei.fandom.com/wiki/Kanji_Tatsumi"},
            {"arcana": "Hierophant", "url": "https://megamitensei.fandom.com/wiki/Ryotaro_Dojima"},
            {"arcana": "Lovers", "url": "https://megamitensei.fandom.com/wiki/Rise_Kujikawa"},
            {"arcana": "Chariot", "url": "https://megamitensei.fandom.com/wiki/Chie_Satonaka"},
            {"arcana": "Justice", "url": "https://megamitensei.fandom.com/wiki/Nanako_Dojima"},
            {"arcana": "Hermit", "url": "https://megamitensei.fandom.com/wiki/Fox"},
            {"arcana": "Fortune", "url": "https://megamitensei.fandom.com/wiki/Naoto_Shirogane"},
            {"arcana": "Strength", "url": "https://megamitensei.fandom.com/wiki/Kou_Ichijo"},
            {"arcana": "Strength_Alt", "url": "https://megamitensei.fandom.com/wiki/Daisuke_Nagase"},
            {"arcana": "Hanged Man", "url": "https://megamitensei.fandom.com/wiki/Naoki_Konishi"},
            {"arcana": "Temperance", "url": "https://megamitensei.fandom.com/wiki/Eri_Minami"},
            {"arcana": "Devil", "url": "https://megamitensei.fandom.com/wiki/Sayoko_Uehara"},
            {"arcana": "Tower", "url": "https://megamitensei.fandom.com/wiki/Shu_Nakajima"},
            {"arcana": "Moon", "url": "https://megamitensei.fandom.com/wiki/Ai_Ebihara"},
            {"arcana": "Sun", "url": "https://megamitensei.fandom.com/wiki/Yumi_Ozawa"},
            {"arcana": "Sun_Alt", "url": "https://megamitensei.fandom.com/wiki/Ayane_Matsunaga"},
            {"arcana": "Jester", "url": "https://megamitensei.fandom.com/wiki/Tohru_Adachi"},
            {"arcana": "Aeon", "url": "https://megamitensei.fandom.com/wiki/Marie"}
        ],
        "p5_scraped.json": [
            {"arcana": "Priestess", "url": "https://megamitensei.fandom.com/wiki/Makoto_Niijima"},
            {"arcana": "Empress", "url": "https://megamitensei.fandom.com/wiki/Haru_Okumura"},
            {"arcana": "Emperor", "url": "https://megamitensei.fandom.com/wiki/Yusuke_Kitagawa"},
            {"arcana": "Hierophant", "url": "https://megamitensei.fandom.com/wiki/Sojiro_Sakura"},
            {"arcana": "Lovers", "url": "https://megamitensei.fandom.com/wiki/Ann_Takamaki"},
            {"arcana": "Chariot", "url": "https://megamitensei.fandom.com/wiki/Ryuji_Sakamoto"},
            {"arcana": "Justice", "url": "https://megamitensei.fandom.com/wiki/Goro_Akechi"},
            {"arcana": "Hermit", "url": "https://megamitensei.fandom.com/wiki/Futaba_Sakura"},
            {"arcana": "Fortune", "url": "https://megamitensei.fandom.com/wiki/Chihaya_Mifune"},
            {"arcana": "Hanged Man", "url": "https://megamitensei.fandom.com/wiki/Munehisa_Iwai"},
            {"arcana": "Death", "url": "https://megamitensei.fandom.com/wiki/Tae_Takemi"},
            {"arcana": "Temperance", "url": "https://megamitensei.fandom.com/wiki/Sadayo_Kawakami"},
            {"arcana": "Devil", "url": "https://megamitensei.fandom.com/wiki/Ichiko_Ohya"},
            {"arcana": "Tower", "url": "https://megamitensei.fandom.com/wiki/Shinya_Oda"},
            {"arcana": "Star", "url": "https://megamitensei.fandom.com/wiki/Hifumi_Togo"},
            {"arcana": "Moon", "url": "https://megamitensei.fandom.com/wiki/Yuuki_Mishima"},
            {"arcana": "Sun", "url": "https://megamitensei.fandom.com/wiki/Toranosuke_Yoshida"},
            {"arcana": "Faith", "url": "https://megamitensei.fandom.com/wiki/Kasumi_Yoshizawa"},
            {"arcana": "Councillor", "url": "https://megamitensei.fandom.com/wiki/Takuto_Maruki"}
        ]
    }

    print("Starting massive scrape job... Grab a coffee, this will take a minute!\n")

    for filename, targets in games.items():
        print(f"=== Generating {filename} ===")
        database = {}
        
        for target in targets:
            data = scrape_social_links(target["url"], target["arcana"])
            if data:
                database[target["arcana"]] = data
                
        with open(filename, 'w', encoding='utf-8') as f:
            json.dump(database, f, indent=2, ensure_ascii=False)
            
        print(f"Saved -> {filename}\n")
        
    print("ALL DONE! Check your folder for the generated JSON files.")