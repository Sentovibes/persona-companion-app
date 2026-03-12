#!/usr/bin/env python3
"""
Check what enemies are actually missing images
"""
import os
import json

def normalize_name(name):
    """Normalize name for file matching"""
    return name.replace('/', '_').replace(':', '').replace('?', '')

def check_missing_per_game():
    """Check missing enemy images per game"""
    games = {
        'P3 FES': ('app/src/main/assets/data/enemies/p3fes_enemies.json', 'p3fes'),
        'P3P': ('app/src/main/assets/data/enemies/p3p_enemies.json', 'p3p'),
        'P3R': ('app/src/main/assets/data/enemies/p3r_enemies.json', 'p3r'),
        'P4': ('app/src/main/assets/data/enemies/p4_enemies.json', 'p4'),
        'P4G': ('app/src/main/assets/data/enemies/p4g_enemies.json', 'p4g'),
        'P5': ('app/src/main/assets/data/enemies/p5_enemies.json', 'p5'),
        'P5R': ('app/src/main/assets/data/enemies/p5r_enemies.json', 'p5r')
    }
    
    total_missing = 0
    all_missing = []
    
    for game_name, (json_path, folder_id) in games.items():
        if not os.path.exists(json_path):
            print(f"{game_name}: JSON not found")
            continue
            
        with open(json_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
            if isinstance(data, list):
                enemy_names = [e['name'] for e in data]
            else:
                enemy_names = list(data.keys())
        
        image_folder = f'app/src/main/assets/images/enemies/{folder_id}'
        if os.path.exists(image_folder):
            existing_images = set([f.replace('.png', '') for f in os.listdir(image_folder) if f.endswith('.png')])
        else:
            existing_images = set()
        
        missing = []
        for name in enemy_names:
            normalized = normalize_name(name)
            if normalized not in existing_images:
                missing.append(name)
        
        coverage = ((len(enemy_names) - len(missing)) / len(enemy_names) * 100) if enemy_names else 0
        print(f"{game_name}: {len(enemy_names) - len(missing)}/{len(enemy_names)} ({coverage:.1f}%)")
        
        if missing:
            print(f"  Missing ({len(missing)}): {', '.join(missing[:10])}")
            if len(missing) > 10:
                print(f"  ... and {len(missing) - 10} more")
            all_missing.extend([(game_name, m) for m in missing])
            total_missing += len(missing)
    
    return total_missing, all_missing

def main():
    print("=== ACTUAL MISSING ENEMIES ===\n")
    total_missing, all_missing = check_missing_per_game()
    
    print(f"\n=== SUMMARY ===")
    print(f"Total missing: {total_missing}")
    
    if total_missing == 0:
        print("\n✓ 100% COVERAGE! All enemies have images!")
    else:
        print(f"\n⚠ {total_missing} enemies are missing images")
        
        if total_missing <= 20:
            print("\n=== ALL MISSING ENEMIES ===")
            for game, name in all_missing:
                print(f"  {game}: {name}")

if __name__ == '__main__':
    main()
