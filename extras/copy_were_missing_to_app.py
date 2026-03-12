#!/usr/bin/env python3
"""
Copy images from were-missing to appropriate game folders in app assets
"""
import os
import json
import shutil

def load_persona_names_by_game():
    """Load all persona names from JSON files"""
    games = {
        'p3fes': 'app/src/main/assets/data/persona3/personas.json',
        'p3p': 'app/src/main/assets/data/persona3/portable_personas.json',
        'p3r': 'app/src/main/assets/data/persona3/reload_personas.json',
        'p4': 'app/src/main/assets/data/persona4/personas.json',
        'p4g': 'app/src/main/assets/data/persona4/golden_personas.json',
        'p5': 'app/src/main/assets/data/persona5/personas.json',
        'p5r': 'app/src/main/assets/data/persona5/royal_personas.json'
    }
    
    personas_by_game = {}
    for game_id, path in games.items():
        if os.path.exists(path):
            with open(path, 'r', encoding='utf-8') as f:
                data = json.load(f)
                # Handle both array and object formats
                if isinstance(data, list):
                    personas_by_game[game_id] = {p['name'] for p in data}
                else:
                    personas_by_game[game_id] = set(data.keys())
    
    return personas_by_game

def normalize_name(name):
    """Normalize persona name for matching"""
    return name.replace('/', '_').replace(':', '').replace('?', '').replace("'", '')

def main():
    print("=== Copying were-missing images to app ===\n")
    
    # Load persona names by game
    personas_by_game = load_persona_names_by_game()
    
    # Get images from were-missing
    were_missing_folder = 'extras/images/personas/were-missing'
    if not os.path.exists(were_missing_folder):
        print("ERROR: were-missing folder not found!")
        return
    
    images = [f for f in os.listdir(were_missing_folder) if f.endswith('.png')]
    print(f"Found {len(images)} images in were-missing folder\n")
    
    copied = 0
    skipped = 0
    not_found = []
    
    for image_file in sorted(images):
        persona_name = image_file.replace('.png', '')
        normalized = normalize_name(persona_name)
        
        # Find which game(s) this persona belongs to
        found_in_games = []
        for game_id, persona_names in personas_by_game.items():
            # Check both original and normalized names
            if persona_name in persona_names or any(normalize_name(p) == normalized for p in persona_names):
                found_in_games.append(game_id)
        
        if found_in_games:
            # Copy to all matching games
            for game_id in found_in_games:
                dest_folder = f'app/src/main/assets/images/personas/{game_id}'
                os.makedirs(dest_folder, exist_ok=True)
                
                src = os.path.join(were_missing_folder, image_file)
                dest = os.path.join(dest_folder, image_file)
                
                if not os.path.exists(dest):
                    shutil.copy2(src, dest)
                    print(f"✓ Copied {persona_name} to {game_id}")
                    copied += 1
                else:
                    skipped += 1
        else:
            not_found.append(persona_name)
    
    print(f"\n=== SUMMARY ===")
    print(f"Copied: {copied}")
    print(f"Skipped (already exist): {skipped}")
    print(f"Not found in any game: {len(not_found)}")
    
    if not_found:
        print(f"\n=== Images not matching any persona ({len(not_found)}) ===")
        print("(These might be enemies, not personas)")
        for name in not_found[:20]:
            print(f"  - {name}")
        if len(not_found) > 20:
            print(f"  ... and {len(not_found) - 20} more")

if __name__ == '__main__':
    main()
