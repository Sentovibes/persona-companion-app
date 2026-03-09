"""
Image scraper using Selenium to bypass Fandom's anti-bot protection
Requires: pip install selenium
And: Download ChromeDriver from https://chromedriver.chromium.org/
"""
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.chrome.options import Options
import json
import os
import time
import requests

def setup_driver():
    """Setup Chrome driver with options to avoid detection"""
    chrome_options = Options()
    chrome_options.add_argument('--headless')  # Run in background
    chrome_options.add_argument('--no-sandbox')
    chrome_options.add_argument('--disable-dev-shm-usage')
    chrome_options.add_argument('--disable-blink-features=AutomationControlled')
    chrome_options.add_experimental_option("excludeSwitches", ["enable-automation"])
    chrome_options.add_experimental_option('useAutomationExtension', False)
    
    driver = webdriver.Chrome(options=chrome_options)
    driver.execute_script("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})")
    return driver

def get_image_url_selenium(driver, name):
    """Get image URL using Selenium"""
    search_name = name.replace(" ", "_")
    urls_to_try = [
        f"https://megamitensei.fandom.com/wiki/{search_name}",
        f"https://megamitensei.fandom.com/wiki/{search_name}_(Persona)",
        f"https://megamitensei.fandom.com/wiki/{search_name}_(Demon)",
        f"https://megamitensei.fandom.com/wiki/{search_name}_(Shadow)",
    ]
    
    for url in urls_to_try:
        try:
            driver.get(url)
            time.sleep(2)  # Wait for page load
            
            # Try to find infobox image
            try:
                img_element = driver.find_element(By.CSS_SELECTOR, '.portable-infobox img')
                img_url = img_element.get_attribute('src')
                
                if img_url and 'static.wikia.nocookie.net' in img_url:
                    # Clean URL
                    img_url = img_url.split('?')[0].split('#')[0]
                    if '/revision/' in img_url:
                        img_url = img_url.split('/revision/')[0]
                    return img_url
            except:
                pass
                
        except Exception as e:
            print(f"    Error: {e}")
            
    return None

def download_image(url, output_dir, name):
    """Download image from URL"""
    ext = os.path.splitext(url)[1] or '.png'
    safe_name = name.replace("/", "_").replace(":", "").replace("?", "")
    filepath = os.path.join(output_dir, f"{safe_name}{ext}")
    
    if os.path.exists(filepath):
        return True
    
    try:
        response = requests.get(url, timeout=15)
        if response.status_code == 200:
            with open(filepath, 'wb') as f:
                f.write(response.content)
            return True
    except:
        pass
    return False

def scrape_enemies_selenium():
    """Scrape enemy images using Selenium"""
    print("Setting up Chrome driver...")
    driver = setup_driver()
    
    try:
        games = {
            'p3r': '../app/src/main/assets/data/enemies/p3r_enemies.json',
            'p4g': '../app/src/main/assets/data/enemies/p4g_enemies.json',
            'p5r': '../app/src/main/assets/data/enemies/p5r_enemies.json',
        }
        
        for game, filepath in games.items():
            if not os.path.exists(filepath):
                continue
                
            with open(filepath, 'r', encoding='utf-8') as f:
                enemies = json.load(f)
            
            # Filter to bosses only
            bosses = [e for e in enemies if e.get('isBoss') or e.get('isMiniBoss')]
            
            output_dir = f'images/enemies/{game}'
            os.makedirs(output_dir, exist_ok=True)
            
            print(f"\n=== {game.upper()} Bosses ({len(bosses)} total) ===")
            
            for i, boss in enumerate(bosses, 1):
                name = boss.get('name')
                if not name:
                    continue
                
                print(f"[{i}/{len(bosses)}] {name}...", end=" ", flush=True)
                
                img_url = get_image_url_selenium(driver, name)
                if img_url:
                    if download_image(img_url, output_dir, name):
                        print("OK")
                    else:
                        print("FAIL")
                else:
                    print("NO PAGE")
                
                time.sleep(1)  # Be nice to the server
                
    finally:
        driver.quit()
        print("\nDriver closed")

if __name__ == '__main__':
    print("Selenium Image Scraper")
    print("=" * 50)
    print("\nRequirements:")
    print("  1. pip install selenium")
    print("  2. Download ChromeDriver")
    print("  3. Add ChromeDriver to PATH")
    print("\n" + "=" * 50)
    
    input("\nPress Enter to start (or Ctrl+C to cancel)...")
    
    scrape_enemies_selenium()
    print("\n[DONE] Check images/enemies/ folder")
