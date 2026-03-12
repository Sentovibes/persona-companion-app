#!/usr/bin/env python3
"""
Check what personas are actually missing images
"""
import os
import json

def normalize_name(name):
    """Normalize name for file matching"""
    return name.replace('/', '_').replace(':', '').replace('?', '')

def check_missing_per_game():
    """Check missing images per game"""
    games = {
        'P3 FES': ('app/src/main/assets/data/persona3/personas.json', 'p3fes'),
        'P3P': ('app/src/main/assets/data/persona3/portable_personas.json', 'p3p'),
        'P3R': ('app/src/main/assets/data/persona3/reload_personas.json', 'p3r'),
        'P4': ('app/src/main/assets/data/persona4/personas.json', 'p4'),
        'P4G': ('app/src/main/assets/data/persona4/golden_personas.json', 'p4g'),
        'P5': ('app/src/main/assets/data/persona5/personas.json', 'p5'),
        'P5R': ('app/src/main/assets/data/persona5/royal_personas.json', 'p5r')
    }
    
    total_missing = 0
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
        
        coverage = ((len(persona_names) - len(missing)) / len(persona_names) * 100) if persona_names else 0
        print(f"{game_name}: {len(persona_names) - len(missing)}/{len(persona_names)} ({coverage:.1f}%)")
        
        if missing:
            print(f"  Missing: {', '.join(missing[:5])}")
            if len(missing) > 5:
                print(f"  ... and {len(missing) - 5} more")
            all_missing.extend([(game_name, m) for m in missing])
            total_missing += len(missing)
    
    return total_missing, all_missing

def main():
    print("=== ACTUAL MISSING PERSONAS ===\n")
    total_missing, all_missing = check_missing_per_game()
    
    print(f"\n=== SUMMARY ===")
    print(f"Total missing: {total_missing}")
    
    if total_missing == 0:
        print("\n✓ 100% COVERAGE! All personas have images!")
    else:
        print(f"\n⚠ {total_missing} personas are missing images")

if __name__ == '__main__':
    main()
