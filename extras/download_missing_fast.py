"""
Fast download of missing personas - optimized patterns only
"""
import requests
import hashlib
import os
import time
from concurrent.futures import ThreadPoolExecutor, as_completed

# Map of persona names in our JSON to their wiki names
NAME_MAP = {
    "Jack-o'-Lantern": "Pyro Jack",
    "Bugs": "Bugbear",
    "Kushinada": "Kushinada-Hime",
    "Yatagarasu": "Yatagarasu",
    "Ho-Oh": "Ho-Ou",
    "Macabre": "Macabre",
    "Hitokoto-Nushi": "Hitokotonushi",
    "Niddhoggr": "Nidhogg",
    "Take-Minakata": "Takeminakata",
    "Yomotsu-Ikusa": "Yomotsu-ikusa",
}

def get_wikia_hash(filename):
    """Calculate Wikia's MD5 hash for a filename"""
    md5 = hashlib.md5(filename.encode('utf-8')).hexdigest()
    return md5[0], md5[0:2]

def try_download_fast(name, output_dir, game_suffix):
    """Try to download with optimized pattern list"""
    wiki_name = NAME_MAP.get(name, name)
    safe_name = name.replace("/", "_").replace(":", "").replace("?", "")
    
    # Check if already exists
    for ext in ['.png', '.jpg', '.jpeg', '.webp']:
        if os.path.exists(os.path.join(output_dir, f"{safe_name}{ext}")):
            return "SKIP"
    
    # Optimized pattern list (most common patterns only)
    patterns = []
    
    # Try ALL game variants (personas appear in multiple games but wiki may only have one version)
    # Priority: P5X uncensored > P5X > P4G > P3R > SMT
    patterns.extend([
        # P5X (best quality, most recent)
        f"P5X_{wiki_name.replace(' ', '_')}_uncensored",
        f"P5X_{wiki_name.replace(' ', '')}",
        f"P5X_{wiki_name.replace(' ', '_')}",
        # P5R/P5S/P5
        f"P5R_{wiki_name.replace(' ', '_')}",
        f"P5S_{wiki_name.replace(' ', '_')}",
        f"P5_{wiki_name.replace(' ', '_')}",
        # P4G/P4
        f"P4G_{wiki_name.replace(' ', '_')}",
        f"P4_{wiki_name.replace(' ', '_')}",
        # P3R/P3P/P3
        f"P3R_{wiki_name.replace(' ', '_')}",
        f"P3P_{wiki_name.replace(' ', '_')}",
        f"P3_{wiki_name.replace(' ', '_')}",
    ])
    
    # Common patterns (without game prefix)
    patterns.extend([
        f"{wiki_name.replace(' ', '_')}_(Uncensored)",
        f"{wiki_name.replace(' ', '_')}_uncensored",
        wiki_name.replace(" ", "_"),
        wiki_name.replace(" ", ""),
    ])
    
    # SMT fallback (try these last)
    smt_variants = ["SMTV", "SMTII", "SMTIII", "SMTIV", "SMT"]
    for smt in smt_variants:
        patterns.extend([
            f"{wiki_name.replace(' ', '_')}_{smt}",
            f"{wiki_name.replace(' ', '_')}_{smt}_Art",
            f"{wiki_name.replace(' ', '_')}_({smt}_Art)",
            f"{wiki_name.replace(' ', '_')} ({smt} Art)",
        ])
    
    extensions = ['.png', '.jpg', '.jpeg', '.webp']
    
    # Try each pattern
    for pattern in patterns:
        for ext in extensions:
            filename = pattern + ext
            hash1, hash2 = get_wikia_hash(filename)
            url = f"https://static.wikia.nocookie.net/megamitensei/images/{hash1}/{hash2}/{filename}"
            
            try:
                response = requests.get(url, timeout=2)  # Reduced from 5 to 2 seconds
                if response.status_code == 200:
                    filepath = os.path.join(output_dir, f"{safe_name}{ext}")
                    with open(filepath, 'wb') as f:
                        f.write(response.content)
                    return "OK"
            except:
                pass
    
    return "FAIL"

def download_one_persona(args):
    """Download a single persona (for parallel execution)"""
    name, output_dir, game_suffix, index, total = args
    result = try_download_fast(name, output_dir, game_suffix)
    return (name, result, index, total)

def download_missing(game_id, missing_file, game_suffix):
    """Download missing personas with parallel requests"""
    if not os.path.exists(missing_file):
        print(f"[SKIP] {missing_file} not found")
        return 0
    
    with open(missing_file, 'r', encoding='utf-8') as f:
        missing_personas = [line.strip() for line in f if line.strip()]
    
    output_dir = f'images/personas/{game_id}'
    os.makedirs(output_dir, exist_ok=True)
    
    print(f"\n{'='*70}")
    print(f"{game_id.upper()} - Downloading {len(missing_personas)} missing personas")
    print(f"{'='*70}")
    
    # Prepare arguments for parallel execution
    args_list = [
        (name, output_dir, game_suffix, i+1, len(missing_personas))
        for i, name in enumerate(missing_personas)
    ]
    
    success_count = 0
    
    # Use ThreadPoolExecutor for parallel downloads (10 threads)
    with ThreadPoolExecutor(max_workers=10) as executor:
        futures = {executor.submit(download_one_persona, args): args for args in args_list}
        
        for future in as_completed(futures):
            name, result, index, total = future.result()
            
            if result == "OK":
                print(f"[{index}/{total}] {name}... ✓ OK")
                success_count += 1
            elif result == "SKIP":
                print(f"[{index}/{total}] {name}... SKIP")
            else:
                print(f"[{index}/{total}] {name}... ✗ NOT FOUND")
    
    print(f"\n{game_id.upper()} Results: {success_count}/{len(missing_personas)} downloaded")
    return success_count

if __name__ == "__main__":
    print("="*70)
    print("Fast Download - Missing Personas Only")
    print("="*70)
    
    total_success = 0
    
    # Download P4G missing
    total_success += download_missing('p4g', 'missing_p4g_personas.txt', 'P4G')
    
    # Download P5R missing
    total_success += download_missing('p5r', 'missing_p5r_personas.txt', 'P5R')
    
    print(f"\n{'='*70}")
    print(f"TOTAL: {total_success} new personas downloaded!")
    print(f"{'='*70}")
    print("\nRun check_progress.py to see updated stats")
