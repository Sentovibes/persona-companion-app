#!/usr/bin/env python3
"""
Check how many enemies have images vs how many are missing
"""
import json
from pathlib import Path

def check_coverage():
    # Load all enemy data files
    enemy_files = {
        'P3 FES': 'app/src/main/assets/data/enemies/p3fes_enemies.json',
        'P3 Portable': 'app/src/main/assets/data/enemies/p3p_enemies.json',
        'P3 Reload': 'app/src/main/assets/data/enemies/p3r_enemies.json',
        'P3R Episode Aigis': 'app/src/main/assets/data/enemies/p3r_episode_aigis/enemies.json',
        'P4': 'app/src/main/assets/data/enemies/p4_enemies.json',
        'P4 Golden': 'app/src/main/assets/data/enemies/p4g_enemies.json',
        'P5': 'app/src/main/assets/data/enemies/p5_enemies.json',
        'P5 Royal': 'app/src/main/assets/data/enemies/p5r_enemies.json',
    }
    
    # Get all available images
    images_path = Path('app/src/main/assets/images/enemies_shared')
    available_images = set()
    if images_path.exists():
        for img in images_path.glob('*.png'):
            # Remove extension and convert to lowercase with underscores
            name = img.stem.lower().replace(' ', '_').replace('-', '_')
            available_images.add(name)
    
    print(f"Total images available: {len(available_images)}\n")
    
    # Check each game
    total_enemies = 0
    total_with_images = 0
    total_missing = 0
    
    all_missing = []
    
    for game_name, file_path in enemy_files.items():
        path = Path(file_path)
        if not path.exists():
            print(f"{game_name}: File not found")
            continue
        
        with open(path, 'r', encoding='utf-8') as f:
            enemies = json.load(f)
        
        missing_in_game = []
        for enemy in enemies:
            total_enemies += 1
            # Use persona_name for P5/P5R, otherwise use name
            name_to_check = enemy.get('persona_name', enemy['name'])
            # Convert enemy name to image filename format
            img_name = name_to_check.lower().replace(' ', '_').replace('-', '_')
            # Remove special characters
            img_name = img_name.replace('/', '_').replace(':', '_').replace('?', '').replace("'", '').replace('&', '_')
            
            if img_name in available_images:
                total_with_images += 1
            else:
                total_missing += 1
                missing_in_game.append(enemy['name'])
        
        print(f"{game_name}:")
        print(f"  Total: {len(enemies)}")
        print(f"  With images: {len(enemies) - len(missing_in_game)}")
        print(f"  Missing: {len(missing_in_game)}")
        
        if missing_in_game:
            all_missing.extend([(game_name, name) for name in missing_in_game])
            if len(missing_in_game) <= 10:
                for name in missing_in_game:
                    print(f"    - {name}")
            else:
                print(f"    (Too many to list)")
        print()
    
    print("=" * 60)
    print(f"TOTAL SUMMARY:")
    print(f"  Total enemies across all games: {total_enemies}")
    print(f"  Enemies with images: {total_with_images}")
    print(f"  Enemies missing images: {total_missing}")
    print(f"  Coverage: {(total_with_images/total_enemies*100):.1f}%")
    
    if all_missing:
        print(f"\n{len(all_missing)} missing images:")
        for game, name in all_missing[:20]:  # Show first 20
            print(f"  {game}: {name}")
        if len(all_missing) > 20:
            print(f"  ... and {len(all_missing) - 20} more")

if __name__ == '__main__':
    check_coverage()
