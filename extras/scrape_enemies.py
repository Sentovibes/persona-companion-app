import requests
from bs4 import BeautifulSoup
import os
import time

def scrape_fandom_image(url, save_folder, enemy_name):
    # Create folder if it doesn't exist
    if not os.path.exists(save_folder):
        os.makedirs(save_folder)

    # User-Agent prevents the Wiki from blocking us
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
    }

    try:
        response = requests.get(url, headers=headers, timeout=10)
        soup = BeautifulSoup(response.text, 'html.parser')
        
        # Fandom puts main character/enemy images in an element called 'pi-image'
        image_box = soup.find('figure', class_='pi-image')
        if not image_box:
            print(f"[!] No image box found for {enemy_name}")
            return
            
        img_tag = image_box.find('img')
        if not img_tag:
            print(f"[!] No image tag found for {enemy_name}")
            return
            
        # Sometimes Fandom lazy-loads images, so we check data-src first
        img_url = img_tag.get('data-src') or img_tag.get('src')
        
        # 🎯 THE PRO TRICK: Fandom compresses images via the URL. 
        # We split the URL at '/revision/' to get the original, uncompressed PNG/JPG.
        if '/revision/' in img_url:
            img_url = img_url.split('/revision/')[0]
            
        # Download the actual image
        img_data = requests.get(img_url, headers=headers).content
        
        # Clean the name so it saves nicely (e.g., "Jack Frost" -> "jack_frost")
        safe_name = enemy_name.lower().replace(" ", "_").replace("/", "_")
        file_ext = ".png" if ".png" in img_url.lower() else ".jpg"
        save_path = os.path.join(save_folder, f"{safe_name}{file_ext}")
        
        with open(save_path, 'wb') as f:
            f.write(img_data)
            
        print(f"✅ Downloaded: {safe_name}{file_ext}")
        
        # Be nice to their servers so you don't get IP banned
        time.sleep(1) 
        
    except Exception as e:
        print(f"❌ Error downloading {enemy_name}: {e}")

# Example Usage:
# scrape_fandom_image("https://megamitensei.fandom.com/wiki/Jack_Frost", "wiki_rips", "Jack Frost")