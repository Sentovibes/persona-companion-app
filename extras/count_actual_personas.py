#!/usr/bin/env python3
"""
Count actual personas from JSON data files (the source of truth)
"""
import os
import json

def count_personas_in_json():
    """Count personas from JSON files"""
    games = {
        'P3 FES': 'app/src/main/assets/data/persona3/personas.json',
        'P3P': 'app/src/main/assets/data/persona3/portable_personas.json',
        'P3R': 'app/src/main/assets/data/persona3/reload_personas.json',
        'P4': 'app/src/main/assets/data/persona4/personas.json',
        'P4G': 'app/src/main/assets/data/persona4/golden_personas.json',
        'P5': 'app/src/main/assets/data/persona5/personas.json',
        'P5R': 'app/src/main/assets/data/persona5/royal_personas.json'
    }
    
    total = 0
    for game_name, path in games.items():
        if os.path.exists(path):
            with open(path, 'r', encoding='utf-8') as f:
                data = json.load(f)
                count = len(data) if isinstance(data, dict) else len(data)
                print(f"{game_name}: {count} personas")
                total += count
    
    print(f"\nTotal across all games: {total}")
    return total

def count_unique_personas():
    """Count unique persona names across all games"""
    games = {
        'p3fes': 'app/src/main/assets/data/persona3/personas.json',
        'p3p': 'app/src/main/assets/data/persona3/portable_personas.json',
        'p3r': 'app/src/main/assets/data/persona3/reload_personas.json',
        'p4': 'app/src/main/assets/data/persona4/personas.json',
        'p4g': 'app/src/main/assets/data/persona4/golden_personas.json',
        'p5': 'app/src/main/assets/data/persona5/personas.json',
        'p5r': 'app/src/main/assets/data/persona5/royal_personas.json'
    }
    
    all_names = set()
    for game_id, path in games.items():
        if os.path.exists(path):
            with open(path, 'r', encoding='utf-8') as f:
                data = json.load(f)
                if isinstance(data, dict):
                    all_names.update(data.keys())
                else:
                    all_names.update([p['name'] for p in data])
    
    print(f"\nUnique persona names across all games: {len(all_names)}")
    return all_names

def count_images_per_game():
    """Count images per game folder"""
    print("\n=== Images per game ===")
    for game in ['p3fes', 'p3p', 'p3r', 'p4', 'p4g', 'p5', 'p5r']:
        folder = f'app/src/main/assets/images/personas/{game}'
        if os.path.exists(folder):
            count = len([f for f in os.listdir(folder) if f.endswith('.png')])
            print(f"{game}: {count} images")

def main():
    print("=== PERSONA COUNT VERIFICATION ===\n")
    total = count_personas_in_json()
    unique = count_unique_personas()
    count_images_per_game()
    
    print(f"\n=== SUMMARY ===")
    print(f"Total personas (sum of all games): {total}")
    print(f"Unique personas (deduplicated): {len(unique)}")

if __name__ == '__main__':
    main()
