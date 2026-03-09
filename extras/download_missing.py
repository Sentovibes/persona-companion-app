"""
Download only the missing personas with enhanced filename patterns
"""
import requests
import json
import os
import hashlib
import time

def get_wikia_hash(filename):
    """Calculate Wikia's MD5 hash for a filename"""
    md5 = hashlib.md5(filename.encode('utf-8')).hexdigest()
    return md5[0], md5[0:2]

def try_download_from_cdn(name, output_dir, game_suffix=""):
    """Try to download image directly from CDN with many filename variations"""
    filename_patterns = []
    
    # Game-specific patterns first
    if game_suffix:
        clean_name = name.replace("'", "").replace("-", "")
        clean_name_underscore = name.replace("'", "").replace("-", "_")
        
        filename_patterns.extend([
            f"{name.replace(' ', '')}_{game_suffix}",
            f"{name.replace(' ', '_')}_{game_suffix}",
            f"{clean_name.replace(' ', '')}_{game_suffix}",
            f"{clean_name.replace(' ', '_')}_{game_suffix}",
            f"{clean_name_underscore.replace(' ', '')}_{game_suffix}",
            f"{clean_name_underscore.replace(' ', '_')}_{game_suffix}",
        ])
    
    # Standard patterns with many variations
    clean_name = name.replace("'", "").replace("-", "")
    clean_name_underscore = name.replace("'", "").replace("-", "_")
    clean_name_space = name.replace("'", "").replace("-", " ")
    
    filename_patterns.extend([
        # No spaces
        name.replace(" ", ""),
        clean_name.replace(" ", ""),
        name.replace(" ", "").replace("'", ""),
        name.replace(" ", "").replace("-", ""),
        
        # Underscores
        name.replace(" ", "_"),
        clean_name.replace(" ", "_"),
        clean_name_underscore.replace(" ", "_"),
        name.replace(" ", "_").replace("'", ""),
        name.replace(" ", "_").replace("-", "_"),
        
        # Spaces preserved
        name,
        clean_name_space,
        name.replace("'", ""),
        name.replace("-", ""),
        
        # Special cases for hyphens
        name.replace("-", " "),
        name.replace("-", "_"),
    ])
    
    # Remove duplicates while preserving order
    seen = set()
    unique_patterns = []
    for pattern in filename_patterns:
        if pattern not in seen:
            seen.add(pattern)
            unique_patterns.append(pattern)
    
    extensions = ['.png', '.jpg', '.jpeg', '.webp']
    
    for filename_base in unique_patterns:
        for ext in extensions:
            filename = filename_base + ext
            hash1, hash2 = get_wikia_hash(filename)
            
            url = f"https://static.wikia.nocookie.net/megamitensei/images/{hash1}/{hash2}/{filename}"
            
            try:
                response = requests.get(url, timeout=10)
                if response.status_code == 200:
                    safe_name = name.replace("/", "_").replace(":", "").replace("?", "")
                    filepath = os.path.join(output_dir, f"{safe_name}{ext}")
                    
                    with open(filepath, 'wb') as f:
                        f.write(response.content)
                    
                    print(f"  ✓ Found with pattern: {filename_base}{ext}")
                    return True
            except:
                pass
    
    return False

def download_missing_personas():
    """Download only missing personas"""
    games = {
        'p4g': ('missing_p4g_personas.txt', 'P4G'),
        'p5r': ('missing_p5r_personas.txt', 'P5R'),
    }
    
    for game, (missing_file, suffix) in games.items():
        if not os.path.exists(missing_file):
            print(f"[SKIP] {missing_file} not found")
            continue
        
        with open(missing_file, 'r', encoding='utf-8') as f:
            missing_personas = [line.strip() for line in f if line.strip()]
        
        output_dir = f'images/personas/{game}'
        os.makedirs(output_dir, exist_ok=True)
        
        print(f"\n{'='*70}")
        print(f"{game.upper()} - Downloading {len(missing_personas)} missing personas")
        print(f"{'='*70}")
        
        success_count = 0
        for i, name in enumerate(missing_personas, 1):
            print(f"[{i}/{len(missing_personas)}] {name}...", end=" ", flush=True)
            
            if try_download_from_cdn(name, output_dir, suffix):
                success_count += 1
            else:
                print("  ✗ NOT FOUND")
            
            time.sleep(0.1)  # Small delay to be nice to the server
        
        print(f"\n{game.upper()} Results: {success_count}/{len(missing_personas)} downloaded")

if __name__ == "__main__":
    download_missing_personas()
