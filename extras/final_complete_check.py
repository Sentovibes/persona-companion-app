#!/usr/bin/env python3
"""
Final complete check of all missing images across all games
"""
import os
import json

def normalize_name(name):
    return name.replace('/', '_').replace(':', '').replace('?', '')

def check_personas():
    """Check all persona games"""
    games = {
        'P3 FES': ('app/src/main/assets/data/persona3/personas.json', 'p3fes'),
        'P3P': ('app/src/main/assets/data/persona3/portable_personas.json', 'p3p'),
        'P3R': ('app/src/main/assets/data/persona3/reload_personas.json', 'p3r'),
        'P3R Aigis': ('app/src/main/assets/data/personas/p3r_episode_aigis/personas.json', 'p3r'),
        'P4': ('app/src/main/assets/data/persona4/personas.json', 'p4'),
        'P4G': ('app/src/main/assets/data/persona4/golden_personas.json', 'p4g'),
        'P5': ('app/src/main/assets/data/persona5/personas.json', 'p5'),
        'P5R': ('app/src/main/assets/data/persona5/royal_personas.json', 'p5r')
    }
    
    all_missing = []
    
    for game_name, (json_path, folder_id) in games.items():
        if not os.path.exists(json_path):
            continue
            
        with open(json_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
            persona_names = list(data.keys()) if isinstance(data, dict) else [p['name'] for p in data]
        
        image_folder = f'app/src/main/assets/images/personas/{folder_id}'
        if os.path.exists(image_folder):
            existing_images = set([f.replace('.png', '') for f in os.listdir(image_folder) if f.endswith('.png')])
        else:
            existing_images = set()
        
        missing = []
        for name in persona_names:
            normalized = normalize_name(name)
            if normalized not in existing_images:
                missing.append(name)
        
        if missing:
            for m in missing:
                all_missing.append((game_name, m))
    
    return all_missing

def check_enemies():
    """Check all enemy games"""
    games = {
        'P3 FES': ('app/src/main/assets/data/enemies/p3fes_enemies.json', 'p3fes'),
        'P3P': ('app/src/main/assets/data/enemies/p3p_enemies.json', 'p3p'),
        'P3R': ('app/src/main/assets/data/enemies/p3r_enemies.json', 'p3r'),
        'P3R Aigis': ('app/src/main/assets/data/enemies/p3r_episode_aigis/enemies.json', 'p3r'),
        'P4': ('app/src/main/assets/data/enemies/p4_enemies.json', 'p4'),
        'P4G': ('app/src/main/assets/data/enemies/p4g_enemies.json', 'p4g'),
        'P5': ('app/src/main/assets/data/enemies/p5_enemies.json', 'p5'),
        'P5R': ('app/src/main/assets/data/enemies/p5r_enemies.json', 'p5r')
    }
    
    all_missing = []
    
    for game_name, (json_path, folder_id) in games.items():
        if not os.path.exists(json_path):
            continue
            
        with open(json_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        # Get unique enemy/persona names
        unique_names = set()
        if isinstance(data, list):
            for enemy in data:
                if isinstance(enemy, dict):
                    name = enemy.get('persona_name', enemy.get('name', ''))
                    if name:
                        unique_names.add(name)
        else:
            for enemy_data in data.values():
                if isinstance(enemy_data, dict):
                    name = enemy_data.get('race', enemy_data.get('persona_name', enemy_data.get('name', '')))
                    if name:
                        unique_names.add(name)
        
        # Check in enemy images folder
        image_folder = f'app/src/main/assets/images/enemies/{folder_id}'
        if os.path.exists(image_folder):
            existing_images = set([f.replace('.png', '') for f in os.listdir(image_folder) if f.endswith('.png')])
        else:
            existing_images = set()
        
        # Also check persona images for P5/P5R
        if folder_id in ['p5', 'p5r']:
            persona_folder = f'app/src/main/assets/images/personas/{folder_id}'
            if os.path.exists(persona_folder):
                persona_images = set([f.replace('.png', '') for f in os.listdir(persona_folder) if f.endswith('.png')])
                existing_images.update(persona_images)
        
        missing = []
        for name in unique_names:
            normalized = normalize_name(name)
            if normalized not in existing_images:
                missing.append(name)
        
        if missing:
            for m in missing:
                all_missing.append((game_name, m))
    
    return all_missing

def main():
    print("=== FINAL COMPLETE MISSING IMAGES CHECK ===\n")
    
    print("PERSONAS:")
    persona_missing = check_personas()
    if persona_missing:
        for game, name in persona_missing:
            print(f"  {game}: {name}")
    else:
        print("  ✓ All personas have images!")
    
    print(f"\nENEMIES:")
    enemy_missing = check_enemies()
    if enemy_missing:
        # Group by game
        by_game = {}
        for game, name in enemy_missing:
            if game not in by_game:
                by_game[game] = []
            by_game[game].append(name)
        
        for game, names in sorted(by_game.items()):
            print(f"  {game}: {len(names)} missing")
            print(f"    {', '.join(sorted(names)[:5])}")
            if len(names) > 5:
                print(f"    ... and {len(names) - 5} more")
    else:
        print("  ✓ All enemies have images!")
    
    print(f"\n=== FINAL SUMMARY ===")
    print(f"Missing personas: {len(persona_missing)}")
    print(f"Missing enemies: {len(enemy_missing)}")
    print(f"Total missing: {len(persona_missing) + len(enemy_missing)}")

if __name__ == '__main__':
    main()
