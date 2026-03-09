"""
Download only the missing personas using the updated patterns
"""
import sys
sys.path.insert(0, '.')

from download_all_images import try_download_from_cdn
import os
import time

def download_missing(game_id, missing_file, game_suffix):
    """Download missing personas for a specific game"""
    if not os.path.exists(missing_file):
        print(f"[SKIP] {missing_file} not found")
        return
    
    with open(missing_file, 'r', encoding='utf-8') as f:
        missing_personas = [line.strip() for line in f if line.strip()]
    
    output_dir = f'images/personas/{game_id}'
    os.makedirs(output_dir, exist_ok=True)
    
    print(f"\n{'='*70}")
    print(f"{game_id.upper()} - Downloading {len(missing_personas)} missing personas")
    print(f"{'='*70}")
    
    success_count = 0
    for i, name in enumerate(missing_personas, 1):
        print(f"[{i}/{len(missing_personas)}] {name}...", end=" ", flush=True)
        
        if try_download_from_cdn(name, output_dir, game_suffix):
            print("✓ OK")
            success_count += 1
        else:
            print("✗ NOT FOUND")
        
        time.sleep(0.1)
    
    print(f"\n{game_id.upper()} Results: {success_count}/{len(missing_personas)} downloaded")
    return success_count

if __name__ == "__main__":
    print("="*70)
    print("Downloading Missing Personas with Enhanced Patterns")
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
