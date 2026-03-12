import undetected_chromedriver as uc
from selenium.webdriver.common.by import By
import time
import requests
import os
import re

def safe_filename(name):
    """Convert name to safe filename format"""
    return name.lower().replace(" ", "_").replace("/", "_").replace(":", "").replace("?", "").replace("&", "").replace("-", "_").replace("'", "")

def search_wiki_for_image(driver, persona_name, game_prefix):
    """Search Megaten Wiki for persona image"""
    
    # Try different name variations
    name_variations = [persona_name]
    
    # For compound names with &, try both together and separately
    if "&" in persona_name:
        parts = [p.strip() for p in persona_name.split("&")]
        name_variations.extend(parts)
    
    for name_var in name_variations:
        print(f"  Trying: {name_var}")
        
        # Search for the page
        search_url = f"https://megatenwiki.com/wiki/{name_var.replace(' ', '_')}"
        
        try:
            driver.get(search_url)
            time.sleep(2)
            
            # Look for images with various keywords
            keywords = ["Model", "Render", "Portrait", "Shadow", "Graphic", "Artwork", "Sprite"]
            game_prefixes = [game_prefix, "P5", "P5R", "P3", "P3R", "P4", "P4G", "SMT"]
            
            # Find all image links
            images = driver.find_elements(By.CSS_SELECTOR, "a.image img")
            
            for img in images:
                src = img.get_attribute("src")
                if not src or "megatenwiki.com/images" not in src:
                    continue
                
                # Check if it matches our criteria
                for prefix in game_prefixes:
                    for keyword in keywords:
                        pattern = f"{prefix}.*{keyword}"
                        if re.search(pattern, src, re.IGNORECASE):
                            # Get full resolution URL
                            full_url = src
                            if "/thumb/" in full_url:
                                full_url = re.sub(r'/thumb(/.*)/\d+px-.*', r'\1', full_url)
                            
                            print(f"    ✓ Found: {full_url}")
                            return full_url
            
        except Exception as e:
            print(f"    Error searching {name_var}: {e}")
            continue
    
    return None

def download_image(url, output_path):
    """Download image from URL"""
    try:
        response = requests.get(url, timeout=10)
        if response.status_code == 200:
            with open(output_path, 'wb') as f:
                f.write(response.content)
            return True
    except Exception as e:
        print(f"    Error downloading: {e}")
    return False

# Remaining missing enemies
remaining_missing = [
    "Ame-no-Uzume",
    "Fuu-Ki",
    "Girimehkala",
    "Ippon-Datara",
    "Jack-o'-Lantern",
    "Justine & Caroline",
    "Kikuri-Hime",
    "Kin-Ki",
    "Koh-i-Noor",
    "Koropokguru",
    "Kushinada",
    "Orichalcum",
    "Queen Mab",
    "Shadow Magario",
    "Shadow Sae Niijima",
    "Shadow Takase",
    "Shiki-Ouji",
    "Sui-Ki",
    "Take-Minakata",
    "Yaksini",
    "Yamata-no-Orochi"
]

# Setup Chrome driver
print("Starting Chrome driver...")
options = uc.ChromeOptions()
options.add_argument('--headless=new')
driver = uc.Chrome(options=options, version_main=145)

try:
    for game_id in ["p5", "p5r"]:
        print(f"\n{'='*60}")
        print(f"Downloading {game_id.upper()} remaining enemies")
        print(f"{'='*60}")
        
        output_folder = f"downloaded_enemies/{game_id}"
        os.makedirs(output_folder, exist_ok=True)
        
        game_prefix = "P5R" if game_id == "p5r" else "P5"
        
        for persona_name in remaining_missing:
            safe_name = safe_filename(persona_name)
            output_path = os.path.join(output_folder, f"{safe_name}.png")
            
            # Skip if already exists
            if os.path.exists(output_path):
                print(f"\n{persona_name}: Already exists, skipping")
                continue
            
            print(f"\n{persona_name}:")
            
            # Search for image
            image_url = search_wiki_for_image(driver, persona_name, game_prefix)
            
            if image_url:
                # Download
                if download_image(image_url, output_path):
                    print(f"  ✓ Downloaded successfully")
                else:
                    print(f"  ✗ Download failed")
            else:
                print(f"  ✗ No image found")
            
            time.sleep(1)
    
finally:
    driver.quit()
    print("\n" + "="*60)
    print("Download complete!")
