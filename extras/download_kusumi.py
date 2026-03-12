import undetected_chromedriver as uc
from selenium.webdriver.common.by import By
import time
import requests
import re

def download_image(url, output_path):
    """Download image from URL"""
    try:
        response = requests.get(url, timeout=10)
        if response.status_code == 200:
            with open(output_path, 'wb') as f:
                f.write(response.content)
            return True
    except Exception as e:
        print(f"Error downloading: {e}")
    return False

# Setup Chrome driver
print("Starting Chrome driver...")
options = uc.ChromeOptions()
options.add_argument('--headless=new')
driver = uc.Chrome(options=options, version_main=145)

try:
    # Try different variations
    variations = [
        "Kusumi-no-Okami",
        "Kusumi_no_Okami",
        "Ameno-sagiri",  # This is the actual boss name
    ]
    
    for name in variations:
        print(f"\nTrying: {name}")
        search_url = f"https://megatenwiki.com/wiki/{name.replace(' ', '_')}"
        
        try:
            driver.get(search_url)
            time.sleep(2)
            
            # Find all image links
            images = driver.find_elements(By.CSS_SELECTOR, "a.image img")
            
            for img in images:
                src = img.get_attribute("src")
                if not src or "megatenwiki.com/images" not in src:
                    continue
                
                print(f"  Found image: {src}")
                
                # Check if it's a P4/P4G image
                if re.search(r'P4.*(?:Model|Render|Portrait|Shadow|Graphic|Artwork|Sprite)', src, re.IGNORECASE):
                    # Get full resolution URL
                    full_url = src
                    if "/thumb/" in full_url:
                        full_url = re.sub(r'/thumb(/.*)/\d+px-.*', r'\1', full_url)
                    
                    print(f"  ✓ Downloading: {full_url}")
                    
                    if download_image(full_url, "downloaded_enemies/p4g/kusumi_no_okami.png"):
                        print("  ✓ Downloaded successfully!")
                        driver.quit()
                        exit(0)
        
        except Exception as e:
            print(f"  Error: {e}")
    
    print("\n✗ No suitable image found")
    
finally:
    driver.quit()
