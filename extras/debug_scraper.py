#!/usr/bin/env python3
"""
Debug scraper - saves HTML to file for inspection
"""

from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from bs4 import BeautifulSoup
import time

def setup_driver():
    """Setup Chrome driver with options to avoid detection"""
    chrome_options = Options()
    
    # Run in visible mode to see what's happening
    # chrome_options.add_argument('--headless=new')  
    chrome_options.add_argument('--window-size=1920,1080')
    chrome_options.add_argument('--no-sandbox')
    chrome_options.add_argument('--disable-dev-shm-usage')
    chrome_options.add_argument('--disable-blink-features=AutomationControlled')
    chrome_options.add_experimental_option("excludeSwitches", ["enable-automation"])
    chrome_options.add_experimental_option('useAutomationExtension', False)
    
    chrome_options.add_argument("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
    
    driver = webdriver.Chrome(options=chrome_options)
    
    driver.execute_cdp_cmd("Page.addScriptToEvaluateOnNewDocument", {
        "source": """
        Object.defineProperty(navigator, 'webdriver', {
          get: () => undefined
        })
        """
    })
    return driver

if __name__ == "__main__":
    print("Setting up driver...")
    driver = setup_driver()
    
    try:
        url = "https://megamitensei.fandom.com/wiki/Kenji_Tomochika"
        print(f"Loading {url}...")
        driver.get(url)
        
        print("Waiting 5 seconds for page to load...")
        time.sleep(5)
        
        print("Saving page source...")
        with open('debug_page.html', 'w', encoding='utf-8') as f:
            f.write(driver.page_source)
        
        print("Parsing with BeautifulSoup...")
        soup = BeautifulSoup(driver.page_source, 'html.parser')
        
        # Check for Cloudflare
        if soup.title and "Just a moment" in soup.title.string:
            print("❌ BLOCKED by Cloudflare!")
        else:
            print("✓ Page loaded successfully")
            print(f"  Title: {soup.title.string if soup.title else 'No title'}")
        
        # Find all tables
        all_tables = soup.find_all('table')
        print(f"\n✓ Found {len(all_tables)} total tables")
        
        article_tables = soup.find_all('table', class_='article-table')
        print(f"✓ Found {len(article_tables)} tables with class='article-table'")
        
        wikitable = soup.find_all('table', class_='wikitable')
        print(f"✓ Found {len(wikitable)} tables with class='wikitable'")
        
        # Print first few table classes
        print("\nFirst 5 table classes:")
        for i, table in enumerate(all_tables[:5]):
            classes = table.get('class', [])
            print(f"  Table {i+1}: {classes}")
            
        print("\n✓ Saved HTML to debug_page.html")
        print("✓ Open it in a browser to inspect the structure")
        
    finally:
        driver.quit()
        print("\nDriver closed")
