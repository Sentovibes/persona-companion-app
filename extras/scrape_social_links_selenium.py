#!/usr/bin/env python3
"""
Scrapes Social Link/Confidant data from Megaten Wiki using Selenium to bypass 403 errors.
Requires: pip install selenium beautifulsoup4
And: ChromeDriver in PATH
"""

from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from bs4 import BeautifulSoup
import json
import re
import time

def setup_driver():
    """Setup Chrome driver with options to avoid detection"""
    chrome_options = Options()
    
    # Use the new headless mode which is much harder for Cloudflare to detect
    chrome_options.add_argument('--headless=new')  
    chrome_options.add_argument('--window-size=1920,1080')
    chrome_options.add_argument('--no-sandbox')
    chrome_options.add_argument('--disable-dev-shm-usage')
    chrome_options.add_argument('--disable-blink-features=AutomationControlled')
    chrome_options.add_experimental_option("excludeSwitches", ["enable-automation"])
    chrome_options.add_experimental_option('useAutomationExtension', False)
    
    # Add a realistic User-Agent
    chrome_options.add_argument("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
    
    driver = webdriver.Chrome(options=chrome_options)
    
    # Inject script to completely hide webdriver flag on EVERY page load
    driver.execute_cdp_cmd("Page.addScriptToEvaluateOnNewDocument", {
        "source": """
        Object.defineProperty(navigator, 'webdriver', {
          get: () => undefined
        })
        """
    })
    return driver

def parse_html_table(table):
    """Parses an HTML table into a 2D list, resolving rowspans and colspans safely."""
    rows = table.find_all('tr')
    grid = []

    for row_idx, row in enumerate(rows):
        # Ensure current row exists in our grid
        while len(grid) <= row_idx:
            grid.append([])
            
        col_idx = 0
        for cell in row.find_all(['th', 'td']):
            # Skip filled cells from previous rowspans
            while col_idx < len(grid[row_idx]) and grid[row_idx][col_idx] is not None:
                col_idx += 1
                
            rowspan = int(cell.get('rowspan', 1))
            colspan = int(cell.get('colspan', 1))
            text = cell.get_text(separator=" ", strip=True)
            
            # Safely fill the grid for the spanned cells without throwing IndexErrors
            for r in range(rowspan):
                while len(grid) <= row_idx + r:
                    grid.append([])
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

def scrape_social_links_selenium(driver, url, arcana_name):
    """Scrapes a specific character's page using Selenium and builds the JSON."""
    print(f"  Scraping {arcana_name}...", end=" ", flush=True)
    
    try:
        driver.get(url)
        # Give JS and Cloudflare time to execute
        time.sleep(4) 
        
        soup = BeautifulSoup(driver.page_source, 'html.parser')
        
        # Detect if Cloudflare fully blocked us
        if soup.title and "Just a moment" in soup.title.string:
            print("[!] Blocked by Cloudflare challenge!")
            return {}
        
        # Fandom tables almost always use 'article-table'. Fallback to all tables if none found.
        tables = soup.find_all('table', class_='article-table')
        if not tables:
            tables = soup.find_all('table')
            
        print(f"[Found {len(tables)} tables]", end=" ", flush=True)
        
        arcana_data = {}
        current_rank = 1
        any_counter = 1
        
        for table in tables:
            grid = parse_html_table(table)
            if not grid or len(grid) < 2:
                continue
                
            headers_row = [str(h).lower() if h else "" for h in grid[0]]
            
            # Broadened search terms to catch various Fandom wiki table formats
            has_choice = any(k in h for h in headers_row for k in ['choice', 'response', 'answer', 'prompt', 'option'])
            has_points = any(k in h for h in headers_row for k in ['point', 'pts', 'score'])
            
            if not has_choice or not has_points:
                continue
                
            choice_idx, point_idx = -1, -1
            for i, header in enumerate(headers_row):
                if any(k in header for k in ['choice', 'response', 'answer', 'prompt', 'option']):
                    choice_idx = i
                # If there are multiple points columns, we want the first one we find
                elif any(k in header for k in ['point', 'pts', 'score']) and point_idx == -1:
                    point_idx = i
                    
            if choice_idx == -1 or point_idx == -1:
                continue

            rank_key = f"Rank {current_rank}"
            rank_data = {}
            
            for row in grid[1:]:
                if len(row) > choice_idx and len(row) > point_idx:
                    choice_text = str(row[choice_idx]) if row[choice_idx] else ""
                    points = extract_points(row[point_idx])
                    
                    if choice_text.lower() == 'any' or choice_text == '-':
                        choice_text = f"Any_{any_counter}"
                        any_counter += 1
                    
                    if choice_text and choice_text.strip() != "":
                        rank_data[choice_text] = points
            
            if rank_data:
                arcana_data[rank_key] = {"Next Rank": 0, **rank_data}
                current_rank += 1
                any_counter = 1 

        print(f"OK ({len(arcana_data)} ranks)")
        return arcana_data
        
    except Exception as e:
        print(f"FAIL ({e})")
        return {}

if __name__ == "__main__":
    
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

    print("=" * 60)
    print("Social Links Scraper with Selenium")
    print("=" * 60)
    print("\nRequirements:")
    print("  1. pip install selenium beautifulsoup4")
    print("  2. ChromeDriver in PATH")
    print("\n" + "=" * 60)
    
    input("\nPress Enter to start (or Ctrl+C to cancel)...")
    
    print("\nSetting up Chrome driver...")
    driver = setup_driver()
    
    try:
        print("Starting scrape job...\n")

        for filename, targets in games.items():
            print(f"=== Generating {filename} ===")
            database = {}
            
            for target in targets:
                data = scrape_social_links_selenium(driver, target["url"], target["arcana"])
                if data:
                    database[target["arcana"]] = data
                    
            with open(filename, 'w', encoding='utf-8') as f:
                json.dump(database, f, indent=2, ensure_ascii=False)
                
            print(f"Saved -> {filename}\n")
            
        print("\n" + "=" * 60)
        print("ALL DONE! Check your folder for the generated JSON files.")
        print("=" * 60)
        
    finally:
        driver.quit()
        
        print("\nDriver closed")