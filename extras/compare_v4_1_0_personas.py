#!/usr/bin/env python3
"""
Compare persona images between v4.1.0 and current state
"""
import os
import json

# Load persona data from all games
def load_all_personas():
    games = {
        'p3fes': 'app/src/main/assets/data/persona3/fes_personas.json',
        'p3p': 'app/src/main/assets/data/persona3/portable_personas.json',
        'p3r': 'app/src/main/assets/data/persona3/reload_personas.json',
        'p4': 'app/src/main/assets/data/persona4/personas.json',
        'p4g': 'app/src/main/assets/data/persona4/golden_personas.json',
        'p5': 'app/src/main/assets/data/persona5/personas.json',
        'p5r': 'app/src/main/assets/data/persona5/royal_personas.json'
    }
    
    all_personas = {}
    for game_id, path in games.items():
        if os.path.exists(path):
            with open(path, 'r', encoding='utf-8') as f:
                personas = json.load(f)
                all_personas[game_id] = [p['name'] for p in personas]
    
    return all_personas

# Check what images exist in current app
def check_current_images():
    current = {}
    for game_id in ['p3fes', 'p3p', 'p3r', 'p4', 'p4g', 'p5', 'p5r']:
        folder = f'app/src/main/assets/images/personas/{game_id}'
        if os.path.exists(folder):
            images = [f.replace('.png', '') for f in os.listdir(folder) if f.endswith('.png')]
            current[game_id] = set(images)
        else:
            current[game_id] = set()
    return current

# Check what's in were-missing
def check_were_missing():
    folder = 'extras/images/personas/were-missing'
    if os.path.exists(folder):
        return set([f.replace('.png', '') for f in os.listdir(folder) if f.endswith('.png')])
    return set()

def main():
    print("=== Persona Image Status Comparison ===\n")
    
    all_personas = load_all_personas()
    current_images = check_current_images()
    were_missing = check_were_missing()
    
    print(f"Images in were-missing folder: {len(were_missing)}\n")
    
    total_needed = 0
    total_have = 0
    total_missing = 0
    
    for game_id, persona_names in all_personas.items():
        needed = len(persona_names)
        have = len(current_images[game_id])
        missing = needed - have
        
        total_needed += needed
        total_have += have
        total_missing += missing
        
        coverage = (have / needed * 100) if needed > 0 else 0
        
        print(f"{game_id.upper()}: {have}/{needed} ({coverage:.1f}%)")
        
        if missing > 0:
            # Find which personas are missing
            missing_names = []
            for name in persona_names:
                safe_name = name.replace('/', '_').replace(':', '').replace('?', '')
                if safe_name not in current_images[game_id]:
                    missing_names.append(name)
            
            print(f"  Missing {missing}: {', '.join(missing_names[:5])}")
            if len(missing_names) > 5:
                print(f"  ... and {len(missing_names) - 5} more")
    
    print(f"\n=== TOTALS ===")
    print(f"Total needed: {total_needed}")
    print(f"Total have: {total_have}")
    print(f"Total missing: {total_missing}")
    print(f"Overall coverage: {(total_have / total_needed * 100):.1f}%")
    
    print(f"\n=== COMPARISON WITH v4.1.0 ===")
    print(f"v4.1.0 had: 320 persona images (100% coverage)")
    print(f"Current has: {total_have} persona images")
    print(f"Difference: {total_have - 320} images")
    
    if total_have > 320:
        print("\nYou have MORE images now (likely due to game-specific organization)")
    elif total_have < 320:
        print(f"\nYou are MISSING {320 - total_have} images from v4.1.0!")

if __name__ == '__main__':
    main()
