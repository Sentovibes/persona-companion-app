"""
Direct CDN image downloader
Uses direct wikia CDN URLs - no wiki scraping needed
"""
import requests
import json
import os
import time

def try_download_image(name, output_dir):
    """
    Try to download image directly from wikia CDN
    Pattern: https://static.wikia.nocookie.net/megamitensei/images/X/XX/Name.ext
    """
    # Clean name for filename (remove spaces, special chars)
    clean_name = name.replace(" ", "").replace("-", "").replace("'", "").replace("&", "")
    
    # Try different filename patterns
    patterns = [
        clean_name,  # SwiftAxle
        name.replace(" ", ""),  # Swift Axle -> SwiftAxle
        name.replace(" ", "_"),  # Swift Axle -> Swift_Axle
        name.replace(" ", "-"),  # Swift Axle -> Swift-Axle
    ]
    
    # Try different extensions
    extensions = ['.png', '.jpg', '.jpeg', '.webp']
    
    # We can't predict the hash path (d/de/), so we need to try common patterns
    # or get them from a list
    
    # For now, just try to download if we have the full URL
    # This is a placeholder - you'd need to manually map names to URLs
    # or use a different approach
    
    return None

def download_from_url_list():
    """
    Download images from a manually created URL list
    Create a file 'enemy_image_urls.txt' with format:
    Enemy Name|https://static.wikia.nocookie.net/megamitensei/images/...
    """
    url_file = 'enemy_image_urls.txt'
    if not os.path.exists(url_file):
        print(f"Create {url_file} with enemy names and URLs")
        print("Format: Enemy Name|URL")
        print("Example: Swift Axle|https://static.wikia.nocookie.net/megamitensei/images/d/de/SwiftAxle.jpg")
        return
    
    with open(url_file, 'r', encoding='utf-8') as f:
        for line in f:
            if '|' not in line:
                continue
            name, url = line.strip().split('|', 1)
            
            # Determine output directory and filename
            ext = os.path.splitext(url)[1] or '.png'
            safe_name = name.replace("/", "_").replace(":", "")
            filepath = f'images/enemies/{safe_name}{ext}'
            
            if os.path.exists(filepath):
                print(f"[SKIP] {name}")
                continue
            
            try:
                response = requests.get(url, timeout=15)
                if response.status_code == 200:
                    os.makedirs('images/enemies', exist_ok=True)
                    with open(filepath, 'wb') as f:
                        f.write(response.content)
                    print(f"[OK] {name}")
                else:
                    print(f"[FAIL] {name} - Status {response.status_code}")
            except Exception as e:
                print(f"[ERROR] {name} - {e}")
            
            time.sleep(0.5)

if __name__ == '__main__':
    print("Enemy Image Downloader")
    print("=" * 50)
    print("\nThis script downloads images from a URL list.")
    print("Create 'enemy_image_urls.txt' with format:")
    print("  Enemy Name|https://static.wikia.nocookie.net/...")
    print("\nYou can get URLs by:")
    print("  1. Manually browsing wiki pages")
    print("  2. Using browser dev tools to copy image URLs")
    print("  3. Using a browser extension")
    print("\n" + "=" * 50)
    
    download_from_url_list()
