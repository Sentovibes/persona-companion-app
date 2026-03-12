#!/usr/bin/env python3
"""
Test if Social Link data is on walkthrough pages instead of character pages
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
        # Try different potential URLs for Social Link guides
        test_urls = [
            "https://megamitensei.fandom.com/wiki/Social_Link",
            "https://megamitensei.fandom.com/wiki/Persona_3/Social_Links",
            "https://megamitensei.fandom.com/wiki/Persona_3_Social_Links",
            "https://megamitensei.fandom.com/wiki/Social_Link/Persona_3"
        ]
        
        for url in test_urls:
            print(f"\nTrying: {url}")
            try:
                driver.get(url)
                time.sleep(3)
                
                soup = BeautifulSoup(driver.page_source, 'html.parser')
                
                if soup.title and "Just a moment" in soup.title.string:
                    print("  ❌ Cloudflare blocked")
                    continue
                    
                if "not found" in soup.title.string.lower() or "404" in soup.title.string:
                    print("  ❌ Page not found")
                    continue
                
                print(f"  ✓ Page exists: {soup.title.string}")
                
                # Look for tables with rank/choice data
                all_tables = soup.find_all('table')
                print(f"  ✓ Found {len(all_tables)} tables")
                
                rank_tables = 0
                for table in all_tables:
                    text = table.get_text()
                    if 'Rank' in text and ('Choice' in text or 'Response' in text or 'Points' in text):
                        rank_tables += 1
                        
                if rank_tables > 0:
                    print(f"  ✅ FOUND {rank_tables} tables with Social Link data!")
                    print(f"  This might be the right page!")
                    
            except Exception as e:
                print(f"  ❌ Error: {e}")
                
    finally:
        driver.quit()
        print("\nDriver closed")
