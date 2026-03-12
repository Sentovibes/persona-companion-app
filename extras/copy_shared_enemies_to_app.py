#!/usr/bin/env python3
"""
Copy enemy images from shared to appropriate game folders in app assets
"""
import os
import json
import shutil

def load_enemy_names_by_game():
    """Load all enemy names from JSON files"""
    games = {
        'p3fes': 'app/src/main/assets/data/enemies/p3fes_enemies.json',
        'p3p': 'app/src/main/assets/data/enemies/p3p_enemies.json',
        'p3r': 'app/src/main/assets/data/enemies/p3r_enemies.json',
        'p4': 'app/src/main/assets/data/enemies/p4_enemies.json',
        'p4g': 'app/src/main/assets/data/enemies/p4g_enemies.json',
        'p5': 'app/src/main/assets/data/enemies/p5_enemies.json',
        'p5r': 'app/src/main/assets/data/enemies/p5r_enemies.json'
    }
    
    enemies_by_game = {}
    for game_id, path in games.items():
        if os.path.exists(path):
            with open(path, 'r', encoding='utf-8') as f:
                data = json.load(f)
                # Handle both array and object formats
                if isinstance(data, list):
                    enemies_by_game[game_id] = {e['name'] for e in data}
                else:
                    enemies_by_game[game_id] = set(data.keys())
    
    return enemies_by_game

def normalize_name(name):
    """Normalize enemy name for matching"""
    return name.replace('/', '_').replace(':', '').replace('?', '').replace("'", '')

def main():
    print("=== Copying shared enemy images to app ===\n")
    
    # Load enemy names by game
    enemies_by_game = load_enemy_names_by_game()
    
    # Get images from shared
    shared_folder = 'extras/images/shared/enemies'
    if not os.path.exists(shared_folder):
        print("ERROR: shared enemies folder not found!")
        return
    
    images = [f for f in os.listdir(shared_folder) if f.endswith('.png')]
    print(f"Found {len(images)} images in shared/enemies folder\n")
    
    copied = 0
    skipped = 0
    not_found = []
    
    for image_file in sorted(images):
        enemy_name = image_file.replace('.png', '')
        normalized = normalize_name(enemy_name)
        
        # Find which game(s) this enemy belongs to
        found_in_games = []
        for game_id, enemy_names in enemies_by_game.items():
            # Check both original and normalized names
            if enemy_name in enemy_names or any(normalize_name(e) == normalized for e in enemy_names):
                found_in_games.append(game_id)
        
        if found_in_games:
            # Copy to all matching games
            for game_id in found_in_games:
                dest_folder = f'app/src/main/assets/images/enemies/{game_id}'
                os.makedirs(dest_folder, exist_ok=True)
                
                src = os.path.join(shared_folder, image_file)
                dest = os.path.join(dest_folder, image_file)
                
                if not os.path.exists(dest):
                    shutil.copy2(src, dest)
                    copied += 1
                else:
                    skipped += 1
        else:
            not_found.append(enemy_name)
    
    print(f"\n=== SUMMARY ===")
    print(f"Copied: {copied}")
    print(f"Skipped (already exist): {skipped}")
    print(f"Not found in any game: {len(not_found)}")
    
    if not_found:
        print(f"\n=== Images not matching any enemy ({len(not_found)}) ===")
        for name in not_found[:30]:
            print(f"  - {name}")
        if len(not_found) > 30:
            print(f"  ... and {len(not_found) - 30} more")

if __name__ == '__main__':
    main()
