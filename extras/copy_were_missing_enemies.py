#!/usr/bin/env python3
"""
Copy enemy images from were-missing to app assets
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
                if isinstance(data, list):
                    enemies_by_game[game_id] = {e['name'] for e in data}
                else:
                    enemies_by_game[game_id] = set(data.keys())
    
    return enemies_by_game

def normalize_name(name):
    """Normalize name for matching"""
    return name.replace('/', '_').replace(':', '').replace('?', '').replace("'", '')

# List of known enemy images in were-missing
ENEMY_IMAGES = [
    'Fleetfooted Cavalry', 'Foot Soldier', 'Genocidal Mercenary', 'Gold Hand',
    'Grievous Table', 'Haunted Castle', 'Heartless Relic', 'Heat Overseer',
    'Hedonistic Sinner', 'Heretic Magus', 'High Judge of Hell', 'Icebreaker Lion',
    'Imposing Skyscraper', 'Invaluable Hand', 'Invasive Serpent', 'Invigorated Gigas',
    'Isolated Castle', 'Jin', 'Jotun of Authority', 'Lascivious Lady',
    'Lightning Eagle', 'Loathsome Tiara', 'Luckless Cupid', 'Mage Soldier',
    'Merciless Judge', 'Mercurius', 'Minotaur Nulla', 'Miracle Hand',
    'Morbid Book', 'Necromachinery', 'Obsessive Sand', 'Ochlocratic Sand',
    'Omnipotent Balance', 'Overseer of Creation', 'Pagoda of Disaster', 'Pink Hand',
    'Precious Hand', 'Principled Checkmate', 'Profligate Gigas', 'Purging Right Hand',
    'Raging Turret', 'Rampaging Sand', 'Resentful Surveillant', 'Reticent Checkmate',
    'Ruthless Ice Raven', 'Scornful Dice', 'Serpent of Absurdity', 'Servant Tower',
    'Shadow of the Void', 'Silver Hand', 'Skeptical Tiara', 'Sky Overseer',
    'Slaughter Twins', 'Spiritual Castle', 'Subservient Left Hand', 'Swift Axle',
    'Takaya', 'Tank-Form Shadow', 'Terminal Table', 'The Reaper',
    'Tome of Atrophy', 'Tome of Persecution', 'Venomous Magus', 'Voltaic Youngest Sibling',
    'White Hand', 'Will O\' Wisp Raven'
]

def main():
    print("=== Copying were-missing enemy images to app ===\n")
    
    enemies_by_game = load_enemy_names_by_game()
    were_missing_folder = 'extras/images/personas/were-missing'
    
    copied = 0
    skipped = 0
    not_found = []
    
    for enemy_name in ENEMY_IMAGES:
        image_file = f"{enemy_name}.png"
        src = os.path.join(were_missing_folder, image_file)
        
        if not os.path.exists(src):
            print(f"⚠ Image not found: {enemy_name}")
            continue
        
        normalized = normalize_name(enemy_name)
        found_in_games = []
        
        for game_id, enemy_names in enemies_by_game.items():
            if enemy_name in enemy_names or any(normalize_name(e) == normalized for e in enemy_names):
                found_in_games.append(game_id)
        
        if found_in_games:
            for game_id in found_in_games:
                dest_folder = f'app/src/main/assets/images/enemies/{game_id}'
                os.makedirs(dest_folder, exist_ok=True)
                dest = os.path.join(dest_folder, image_file)
                
                if not os.path.exists(dest):
                    shutil.copy2(src, dest)
                    print(f"✓ Copied {enemy_name} to {game_id}")
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
        print(f"\n=== Not found in enemy data ({len(not_found)}) ===")
        for name in not_found[:20]:
            print(f"  - {name}")

if __name__ == '__main__':
    main()
