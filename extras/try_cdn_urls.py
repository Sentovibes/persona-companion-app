"""
Try to download images directly from CDN by guessing URL patterns
The hash in the URL appears to be based on MD5 of the filename
"""
import requests
import json
import os
import hashlib

def get_wikia_hash(filename):
    """
    Calculate Wikia's hash for a filename
    Wikia uses MD5 hash of the filename
    """
    md5 = hashlib.md5(filename.encode('utf-8')).hexdigest()
    # First level is first char, second level is first two chars
    return md5[0], md5[0:2]

def try_download_from_cdn(name, output_dir):
    """
    Try to download image directly from CDN
    """
    # Try different filename patterns
    filename_patterns = [
        name.replace(" ", ""),  # "Swift Axle" -> "SwiftAxle"
        name.replace(" ", "_"),  # "Swift Axle" -> "Swift_Axle"
        name,  # Original with spaces
    ]
    
    extensions = ['.png', '.jpg', '.jpeg', '.webp']
    
    for filename_base in filename_patterns:
        for ext in extensions:
            filename = filename_base + ext
            hash1, hash2 = get_wikia_hash(filename)
            
            url = f"https://static.wikia.nocookie.net/megamitensei/images/{hash1}/{hash2}/{filename}"
            
            try:
                response = requests.get(url, timeout=10)
                if response.status_code == 200:
                    # Success! Save the image
                    safe_name = name.replace("/", "_").replace(":", "")
                    filepath = os.path.join(output_dir, f"{safe_name}{ext}")
                    
                    with open(filepath, 'wb') as f:
                        f.write(response.content)
                    
                    return True, url
            except:
                pass
    
    return False, None

def download_enemies_from_cdn(download_all=False):
    """Download enemy images directly from CDN"""
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
        
        # Filter based on user choice
        if download_all:
            enemies_to_download = enemies
            print(f"\n=== {game.upper()} ALL Enemies ({len(enemies)} total) ===")
        else:
            enemies_to_download = [e for e in enemies if e.get('isBoss') or e.get('isMiniBoss')]
            print(f"\n=== {game.upper()} Bosses ({len(enemies_to_download)} total) ===")
        
        output_dir = f'images/enemies/{game}'
        os.makedirs(output_dir, exist_ok=True)
        
        success_count = 0
        for i, enemy in enumerate(enemies_to_download, 1):
            name = enemy.get('name')
            if not name:
                continue
            
            print(f"[{i}/{len(enemies_to_download)}] {name}...", end=" ", flush=True)
            
            success, url = try_download_from_cdn(name, output_dir)
            if success:
                print(f"OK")
                success_count += 1
            else:
                print("NOT FOUND")
        
        print(f"\nDownloaded: {success_count}/{len(enemies_to_download)}")

if __name__ == '__main__':
    print("CDN Direct Download")
    print("=" * 50)
    print("\nTrying to download images directly from Wikia CDN...")
    print("This uses MD5 hash to guess the URL pattern.\n")
    
    print("Options:")
    print("  1. Download bosses only (faster)")
    print("  2. Download ALL enemies (slower, ~500+ images)")
    
    choice = input("\nChoice (1/2): ")
    
    download_all = (choice == '2')
    download_enemies_from_cdn(download_all)
    print("\n[DONE] Check images/enemies/ folder")
