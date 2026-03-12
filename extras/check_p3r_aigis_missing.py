#!/usr/bin/env python3
"""
Check what P3R Episode Aigis personas and enemies are missing images
"""
import os
import json

def normalize_name(name):
    """Normalize name for file matching"""
    return name.replace('/', '_').replace(':', '').replace('?', '')

def check_aigis_personas():
    """Check P3R Episode Aigis personas"""
    json_path = 'app/src/main/assets/data/personas/p3r_episode_aigis/personas.json'
    if not os.path.exists(json_path):
        print("P3R Episode Aigis personas JSON not found")
        return []
    
    with open(json_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
        persona_names = list(data.keys())
    
    # Check in p3r persona images folder
    image_folder = 'app/src/main/assets/images/personas/p3r'
    if os.path.exists(image_folder):
        existing_images = set([f.replace('.png', '') for f in os.listdir(image_folder) if f.endswith('.png')])
    else:
        existing_images = set()
    
    missing = []
    for name in persona_names:
        normalized = normalize_name(name)
        if normalized not in existing_images:
            missing.append(name)
    
    print(f"P3R Episode Aigis Personas: {len(persona_names) - len(missing)}/{len(persona_names)}")
    if missing:
        print(f"  Missing: {', '.join(missing)}")
    
    return missing

def check_aigis_enemies():
    """Check P3R Episode Aigis enemies"""
    json_path = 'app/src/main/assets/data/enemies/p3r_episode_aigis/enemies.json'
    if not os.path.exists(json_path):
        print("P3R Episode Aigis enemies JSON not found")
        return []
    
    with open(json_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
        # Get unique persona names from enemies
        unique_personas = set()
        for enemy_data in data.values():
            if isinstance(enemy_data, dict):
                persona_name = enemy_data.get('race', enemy_data.get('name', ''))
                if persona_name:
                    unique_personas.add(persona_name)
    
    # Check in p3r enemy images folder
    image_folder = 'app/src/main/assets/images/enemies/p3r'
    if os.path.exists(image_folder):
        existing_images = set([f.replace('.png', '') for f in os.listdir(image_folder) if f.endswith('.png')])
    else:
        existing_images = set()
    
    missing = []
    for name in unique_personas:
        normalized = normalize_name(name)
        if normalized not in existing_images:
            missing.append(name)
    
    print(f"P3R Episode Aigis Enemies: {len(unique_personas) - len(missing)}/{len(unique_personas)}")
    if missing:
        print(f"  Missing: {', '.join(sorted(missing)[:20])}")
        if len(missing) > 20:
            print(f"  ... and {len(missing) - 20} more")
    
    return missing

def main():
    print("=== P3R Episode Aigis Missing Images ===\n")
    
    persona_missing = check_aigis_personas()
    enemy_missing = check_aigis_enemies()
    
    total_missing = len(persona_missing) + len(enemy_missing)
    
    print(f"\n=== SUMMARY ===")
    print(f"Total missing: {total_missing}")
    print(f"  Personas: {len(persona_missing)}")
    print(f"  Enemies: {len(enemy_missing)}")

if __name__ == '__main__':
    main()
