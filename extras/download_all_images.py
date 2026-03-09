"""
Download ALL images (personas + enemies) from Wikia CDN
Uses MD5 hash to construct direct CDN URLs
"""
import requests
import json
import os
import hashlib
import time

# Map of persona names in our JSON to their wiki names
NAME_MAP = {
    "Jack-o'-Lantern": "Pyro Jack",
    "Bugs": "Bugbear",
    "Kushinada": "Kushinada-Hime",
}

def get_wikia_hash(filename):
    """Calculate Wikia's MD5 hash for a filename"""
    md5 = hashlib.md5(filename.encode('utf-8')).hexdigest()
    return md5[0], md5[0:2]

def try_download_from_cdn(name, output_dir, game_suffix=""):
    """Try to download image directly from CDN with many filename variations"""
    # Apply name mapping (e.g., Jack-o'-Lantern -> Pyro Jack)
    wiki_name = NAME_MAP.get(name, name)
    
    filename_patterns = []
    
    # Game-specific patterns with variants (P5R, P5X, P5, etc.)
    if game_suffix:
        game_variants = []
        if game_suffix == "P5R":
            game_variants = ["P5R", "P5X", "P5S", "P5"]
        elif game_suffix == "P4G":
            game_variants = ["P4G", "P4"]
        elif game_suffix == "P3R":
            game_variants = ["P3R", "P3P", "P3FES", "P3"]
        
        for variant in game_variants:
            # Try with underscores
            filename_patterns.append(f"{variant}_{wiki_name.replace(' ', '')}")
            filename_patterns.append(f"{variant}_{wiki_name.replace(' ', '_')}")
            filename_patterns.append(f"{variant} {wiki_name}")
    
    # Standard patterns with various transformations
    clean_name = wiki_name.replace("'", "").replace("-", "")
    clean_name_underscore = wiki_name.replace("'", "").replace("-", "_")
    clean_name_space = wiki_name.replace("'", "").replace("-", " ")
    
    filename_patterns.extend([
        # Special wiki patterns
        f"{wiki_name} (Uncensored)",
        f"{wiki_name.replace(' ', '_')} (Uncensored)",
        f"{wiki_name.replace(' ', '')} (Uncensored)",
        
        # No spaces
        wiki_name.replace(" ", ""),
        clean_name.replace(" ", ""),
        wiki_name.replace(" ", "").replace("'", ""),
        wiki_name.replace(" ", "").replace("-", ""),
        
        # Underscores
        wiki_name.replace(" ", "_"),
        clean_name.replace(" ", "_"),
        clean_name_underscore.replace(" ", "_"),
        wiki_name.replace(" ", "_").replace("'", ""),
        wiki_name.replace(" ", "_").replace("-", "_"),
        
        # Spaces preserved
        wiki_name,
        clean_name_space,
        wiki_name.replace("'", ""),
        wiki_name.replace("-", ""),
        
        # Special cases for hyphens
        wiki_name.replace("-", " "),
        wiki_name.replace("-", "_"),
    ])
    
    # SMT patterns (fallback for personas that only have SMT art)
    smt_variants = ["SMTV", "SMTIV", "SMTIII", "SMTII", "SMT", "SMT5", "SMT4", "SMT3", "SMT2"]
    for smt in smt_variants:
        filename_patterns.extend([
            f"{wiki_name.replace(' ', '_')} ({smt}_Art)",
            f"{wiki_name.replace(' ', '_')} ({smt} Art)",
            f"{wiki_name.replace(' ', '')} ({smt}_Art)",
            f"{wiki_name.replace(' ', '')} ({smt} Art)",
            f"{smt}_{wiki_name.replace(' ', '_')}",
            f"{smt}_{wiki_name.replace(' ', '')}",
        ])
    
    # Remove duplicates while preserving order
    seen = set()
    unique_patterns = []
    for pattern in filename_patterns:
        if pattern not in seen:
            seen.add(pattern)
            unique_patterns.append(pattern)
    
    extensions = ['.png', '.jpg', '.jpeg', '.webp']
    
    for filename_base in filename_patterns:
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
                    
                    return True
            except:
                pass
    
    return False

def download_personas():
    """Download persona images"""
    games = {
        'p3r': ('../app/src/main/assets/data/persona3reload/reload_personas.json', 'P3R'),
        'p4g': ('../app/src/main/assets/data/persona4/golden_personas.json', 'P4G'),
        'p5r': ('../app/src/main/assets/data/persona5/royal_personas.json', 'P5R'),
    }
    
    total_success = 0
    total_count = 0
    
    for game, (filepath, suffix) in games.items():
        if not os.path.exists(filepath):
            print(f"[SKIP] {filepath} not found")
            continue
            
        with open(filepath, 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        # Handle dict format (persona names as keys)
        if isinstance(data, dict):
            personas = list(data.keys())
        else:
            personas = data
        
        output_dir = f'images/personas/{game}'
        os.makedirs(output_dir, exist_ok=True)
        
        print(f"\n=== {game.upper()} Personas ({len(personas)} total) ===")
        
        success_count = 0
        for i, name in enumerate(personas, 1):
            if not name:
                continue
            
            print(f"[{i}/{len(personas)}] {name}...", end=" ", flush=True)
            
            if try_download_from_cdn(name, output_dir, suffix):
                print("OK")
                success_count += 1
            else:
                print("NOT FOUND")
            
            time.sleep(0.1)  # Small delay
        
        print(f"Downloaded: {success_count}/{len(personas)}")
        total_success += success_count
        total_count += len(personas)
    
    return total_success, total_count

def download_enemies(all_enemies=False):
    """Download enemy images"""
    games = {
        'p3r': ('../app/src/main/assets/data/enemies/p3r_enemies.json', 'P3R'),
        'p4g': ('../app/src/main/assets/data/enemies/p4g_enemies.json', 'P4G'),
        'p5r': ('../app/src/main/assets/data/enemies/p5r_enemies.json', 'P5R'),
    }
    
    total_success = 0
    total_count = 0
    
    for game, (filepath, suffix) in games.items():
        if not os.path.exists(filepath):
            continue
            
        with open(filepath, 'r', encoding='utf-8') as f:
            enemies = json.load(f)
        
        if all_enemies:
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
            
            if try_download_from_cdn(name, output_dir, suffix):
                print("OK")
                success_count += 1
            else:
                print("NOT FOUND")
            
            time.sleep(0.1)
        
        print(f"Downloaded: {success_count}/{len(enemies_to_download)}")
        total_success += success_count
        total_count += len(enemies_to_download)
    
    return total_success, total_count

if __name__ == '__main__':
    print("=" * 60)
    print("  COMPLETE IMAGE DOWNLOADER")
    print("  Downloads Personas + Enemies from Wikia CDN")
    print("=" * 60)
    
    print("\nWhat to download?")
    print("  1. Personas only")
    print("  2. Bosses only")
    print("  3. All enemies")
    print("  4. Personas + Bosses")
    print("  5. EVERYTHING (Personas + All Enemies)")
    
    choice = input("\nChoice (1-5): ")
    
    print("\nStarting download...\n")
    start_time = time.time()
    
    total_success = 0
    total_count = 0
    
    if choice in ['1', '4', '5']:
        print("\n" + "=" * 60)
        print("DOWNLOADING PERSONAS")
        print("=" * 60)
        s, c = download_personas()
        total_success += s
        total_count += c
    
    if choice in ['2', '4']:
        print("\n" + "=" * 60)
        print("DOWNLOADING BOSSES")
        print("=" * 60)
        s, c = download_enemies(all_enemies=False)
        total_success += s
        total_count += c
    
    if choice in ['3', '5']:
        print("\n" + "=" * 60)
        print("DOWNLOADING ALL ENEMIES")
        print("=" * 60)
        s, c = download_enemies(all_enemies=True)
        total_success += s
        total_count += c
    
    elapsed = time.time() - start_time
    
    print("\n" + "=" * 60)
    print("DOWNLOAD COMPLETE")
    print("=" * 60)
    print(f"Total downloaded: {total_success}/{total_count}")
    print(f"Success rate: {total_success/total_count*100:.1f}%")
    print(f"Time elapsed: {elapsed:.1f} seconds")
    print(f"\nImages saved to: images/personas/ and images/enemies/")
