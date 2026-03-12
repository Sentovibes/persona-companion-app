#!/usr/bin/env python3
"""
Save the Social Link main page to inspect its structure
"""

from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from bs4 import BeautifulSoup
import time

def setup_driver():
    chrome_options = Options()
    chrome_options.add_argument('--window-size=1920,1080')
    chrome_options.add_argument('--no-sandbox')
    chrome_options.add_argument('--disable-dev-shm-usage')
    chrome_options.add_argument('--disable-blink-features=AutomationControlled')
    chrome_options.add_experimental_option("excludeSwitches", ["enable-automation"])
    chrome_options.add_experimental_option('useAutomationExtension', False)
    chrome_options.add_argument("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
    
    driver = webdriver.Chrome(options=chrome_options)
    driver.execute_cdp_cmd("Page.addScriptToEvaluateOnNewDocument", {
        "source": "Object.defineProperty(navigator, 'webdriver', {get: () => undefined})"
    })
    return driver

if __name__ == "__main__":
    driver = setup_driver()
    
    try:
        url = "https://megamitensei.fandom.com/wiki/Social_Link"
        print(f"Loading {url}...")
        driver.get(url)
        
        print("Waiting 5 seconds...")
        time.sleep(5)
        
        print("Saving page source...")
        with open('../social_link_main_page.html', 'w', encoding='utf-8') as f:
            f.write(driver.page_source)
        
        soup = BeautifulSoup(driver.page_source, 'html.parser')
        
        print(f"✓ Page title: {soup.title.string if soup.title else 'No title'}")
        
        all_tables = soup.find_all('table')
        print(f"✓ Found {len(all_tables)} total tables")
        
        # Find tables with rank data
        rank_tables = []
        for i, table in enumerate(all_tables):
            text = table.get_text()
            if 'Rank' in text and ('Choice' in text or 'Response' in text or 'Points' in text):
                rank_tables.append((i, table))
                
        print(f"✓ Found {len(rank_tables)} tables with Social Link rank data")
        
        # Inspect the first rank table
        if rank_tables:
            idx, table = rank_tables[0]
            print(f"\n=== First Rank Table (Table #{idx+1}) ===")
            print(f"Classes: {table.get('class', [])}")
            rows = table.find_all('tr')
            print(f"Rows: {len(rows)}")
            if rows:
                first_row = rows[0]
                headers = [cell.get_text(strip=True) for cell in first_row.find_all(['th', 'td'])]
                print(f"Headers: {headers}")
                
        print("\n✓ Saved to social_link_main_page.html")
        
    finally:
        driver.quit()
        print("\nDriver closed")
