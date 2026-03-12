import undetected_chromedriver as uc
from selenium.webdriver.common.by import By
import time
import json
import os
import base64

def safe_filename(name):
    """Convert name to safe filename format"""
    return name.lower().replace(" ", "_").replace("/", "_").replace(":", "").replace("?", "").replace("&", "").replace("-", "_").replace("'", "")

def search_wiki_for_image(driver, enemy_name):
    """Search Megaten Wiki API for P3R enemy image"""
    
    base_url = "https://megatenwiki.com/api.php"
    
    # Try different name variations
    name_variations = [
        enemy_name,
        enemy_name + " (Shadow)",
        enemy_name + " Shadow",
        enemy_name + " (P3R)",
    ]
    
    # For compound names, try splitting
    if "/" in enemy_name:
        parts = [p.strip() for p in enemy_name.split("/")]
        name_variations.extend(parts)
    
    if "&" in enemy_name:
        parts = [p.strip() for p in enemy_name.split("&")]
        name_variations.extend(parts)
    
    # Try removing common prefixes/suffixes that might differ
    if enemy_name.endswith(" Hand"):
        base = enemy_name.replace(" Hand", "")
        name_variations.append(base + " Hand")
    if enemy_name.endswith(" Castle"):
        base = enemy_name.replace(" Castle", "")
        name_variations.append(base + " Castle")
    if enemy_name.endswith(" Tower"):
        base = enemy_name.replace(" Tower", "")
        name_variations.append(base + " Tower")
    
    for name_var in name_variations:
        print(f"    Trying: {name_var}")
        
        clean_name = name_var.strip().replace(" ", "_")
        
        try:
            # STEP 1: Get images list from API
            api_url = f"{base_url}?action=parse&page={clean_name}&prop=images&format=json"
            driver.get(api_url)
            time.sleep(2)
            
            page_source = driver.page_source
            
            # Check for Cloudflare challenge
            if "Just a moment" in page_source or "Checking your browser" in page_source:
                print("      [⏳] Cloudflare challenge, waiting...")
                time.sleep(8)
                page_source = driver.page_source
            
            # Check for 403
            if "403" in page_source:
                continue
            
            # Extract JSON from <pre> tag
            try:
                pre_element = driver.find_element(By.TAG_NAME, "pre")
                json_text = pre_element.text
                data = json.loads(json_text)
            except:
                continue
            
            if "error" in data or "parse" not in data or "images" not in data["parse"]:
                continue
            
            images_list = data["parse"]["images"]
            
            # STEP 2: Filter for P3R images
            keywords = ["Model", "Render", "Portrait", "Shadow", "Graphic", "Artwork", "Sprite"]
            
            for img_name in images_list:
                # Check if image is P3R
                if "P3R" in img_name and any(keyword in img_name for keyword in keywords):
                    # STEP 3: Get image URL
                    api_url_2 = f"{base_url}?action=query&titles=File:{img_name}&prop=imageinfo&iiprop=url&format=json"
                    driver.get(api_url_2)
                    time.sleep(1.5)
                    
                    try:
                        pre_element = driver.find_element(By.TAG_NAME, "pre")
                        json_text = pre_element.text
                        data_2 = json.loads(json_text)
                        
                        pages = data_2.get("query", {}).get("pages", {})
                        for page_id, page_data in pages.items():
                            if "imageinfo" in page_data:
                                image_url = page_data["imageinfo"][0]["url"]
                                print(f"      ✓ Found: {image_url}")
                                return image_url
                    except:
                        continue
            
        except Exception as e:
            continue
    
    return None

def download_image_with_selenium(driver, url, output_path):
    """Download image using Selenium to bypass Cloudflare"""
    try:
        # Navigate to the image URL
        driver.get(url)
        time.sleep(2)
        
        # Get the image as base64
        script = """
        var img = document.querySelector('img');
        if (!img) return null;
        var canvas = document.createElement('canvas');
        canvas.width = img.naturalWidth;
        canvas.height = img.naturalHeight;
        var ctx = canvas.getContext('2d');
        ctx.drawImage(img, 0, 0);
        return canvas.toDataURL('image/png').split(',')[1];
        """
        
        base64_data = driver.execute_script(script)
        
        if base64_data:
            image_data = base64.b64decode(base64_data)
            with open(output_path, 'wb') as f:
                f.write(image_data)
            return True
        else:
            print(f"      Error: Could not extract image")
    except Exception as e:
        print(f"      Error: {e}")
    return False

# P3R missing enemies
p3r_missing = [
    'Ancient Castle', 'Appropriating Noble', 'Arcanist Decapitator', 'Barbaric Beast Wheel',
    'Black Hand', 'Blazing Middle Sibling', 'Bloody Maria', 'Chaos Panzer',
    'Clairvoyant Relic', 'Comeback Castle', 'Controlling Partner', 'Cruel Greatsword',
    'Cultist of Death', 'Cultist of the Storm', 'Dancing Beast Wheel', 'Deadly Eldest Sibling',
    'Demented Knight', 'Dependent Partner', 'Despairing Tiara', 'Deviant Convict',
    'Disturbing Dice', 'Enslaved Cupid', 'Executioner\'s Crown', 'Executive Greatsword',
    'Feral Beast', 'Five Fingers of Blight', 'Fleetfooted Cavalry', 'Foot Soldier',
    'Genocidal Mercenary', 'Gold Hand', 'Grievous Table', 'Haughty Belle',
    'Haunted Castle', 'Heartless Relic', 'Heat Overseer', 'Hedonistic Sinner',
    'Heretic Magus', 'High Judge of Hell', 'Icebreaker Lion', 'Imposing Skyscraper',
    'Invasive Serpent', 'Invigorated Gigas', 'Isolated Castle', 'Jin',
    'Jotun of Authority', 'Lascivious Lady', 'Lightning Eagle', 'Loathsome Tiara',
    'Luckless Cupid', 'Merciless Judge', 'Minotaur Nulla', 'Morbid Book',
    'Necromachinery', 'Obsessive Sand', 'Ochlocratic Sand', 'Omnipotent Balance',
    'Overseer of Creation', 'Pagoda of Disaster', 'Pink Hand', 'Profligate Gigas',
    'Purging Right Hand', 'Raging Turret', 'Rampaging Sand', 'Resentful Surveillant',
    'Scornful Dice', 'Serpent of Absurdity', 'Servant Tower', 'Silver Hand',
    'Skeptical Tiara', 'Sky Overseer', 'Slaughter Twins', 'Spiritual Castle',
    'Statue', 'Subservient Left Hand', 'Swift Axle', 'Takaya',
    'Tank-Form Shadow', 'Terminal Table', 'Terror Dice', 'Tome of Atrophy',
    'Tome of Persecution', 'Ultimate Gigas', 'Venomous Magus', 'Voltaic Youngest Sibling',
    'White Hand', 'Will O\' Wisp Raven'
]

# Setup output folder
output_folder = "were_missing_enemies/p3r"
os.makedirs(output_folder, exist_ok=True)

print(f"Downloading {len(p3r_missing)} missing P3R enemies to were_missing_enemies/p3r/")
print("="*70)

# Setup Chrome driver
print("\nStarting Chrome driver...")
options = uc.ChromeOptions()
driver = uc.Chrome(options=options, version_main=145)
print("Chrome driver started!")

# Test connection
print("Testing connection...")
driver.get("https://megatenwiki.com")
time.sleep(5)
print("Ready!\n")

downloaded_count = 0
failed_count = 0

try:
    for enemy_name in p3r_missing:
        safe_name = safe_filename(enemy_name)
        output_path = os.path.join(output_folder, f"{safe_name}.png")
        
        # Skip if already exists in were_missing
        if os.path.exists(output_path):
            print(f"\n{enemy_name}: Already in were_missing, skipping")
            continue
        
        # Skip if already exists in main downloaded_enemies folder
        main_path = os.path.join("downloaded_enemies/p3r", f"{safe_name}.png")
        if os.path.exists(main_path):
            print(f"\n{enemy_name}: Already in downloaded_enemies, skipping")
            continue
        
        print(f"\n{enemy_name}:")
        
        # Search for image
        image_url = search_wiki_for_image(driver, enemy_name)
        
        if image_url:
            # Download
            if download_image_with_selenium(driver, image_url, output_path):
                print(f"  ✓ Downloaded successfully")
                downloaded_count += 1
            else:
                print(f"  ✗ Download failed")
                failed_count += 1
        else:
            print(f"  ✗ No image found")
            failed_count += 1
        
        time.sleep(0.5)

finally:
    driver.quit()
    print("\n" + "="*70)
    print("Download complete!")
    print(f"Successfully downloaded: {downloaded_count}")
    print(f"Failed: {failed_count}")
    print("="*70)
