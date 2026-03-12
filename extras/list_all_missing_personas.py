#!/usr/bin/env python3
"""
List missing persona images for all games
"""

import json
from pathlib import Path

def safe_filename(name):
    """Convert name to safe filename format"""
    safe = name.lower().replace(" ", "_").replace("/", "_").replace(":", "").replace("?", "").replace("&", "").replace("-", "_").replace("'", "")
    safe = safe.replace("___", "_")
    return safe

games = [
    ("P3FES", "../app/src/main/assets/data/persona3/personas.json", "p3fes"),
    ("P3P", "../app/src/main/assets/data/persona3/portable_personas.json", "p3p"),
    ("P3R", "../app/src/main/assets/data/persona3/reload_personas.json", "p3r"),
    ("P3R Episode Aigis", "../app/src/main/assets/data/personas/p3r_episode_aigis/personas.json", "p3r"),
    ("P4", "../app/src/main/assets/data/persona4/personas.json", "p4"),
    ("P4G", "../app/src/main/assets/data/persona4/golden_personas.json", "p4g"),
    ("P5", "../app/src/main/assets/data/persona5/personas.json", "p5"),
    ("P5R", "../app/src/main/assets/data/persona5/royal_personas.json", "p5r"),
]

print("=" * 70)
print("ALL MISSING PERSONAS BY GAME")
print("=" * 70)

total_personas = 0
total_available = 0
total_missing = 0

for game_name, json_path, folder_name in games:
    # Load persona data
    try:
        with open(json_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
    except FileNotFoundError:
        print(f"\n{game_name}: JSON file not found")
        continue
    
    persona_names = list(data.keys())
    
    # Check in app/src/main/assets/images/personas/{game} (where they should be after sync)
    game_images_dir = Path(f"../app/src/main/assets/images/personas/{folder_name}")
    
    # Get all available images
    available_images = set()
    if game_images_dir.exists():
        for img in game_images_dir.glob("*.*"):
            available_images.add(img.stem.lower())
    
    # Find missing
    missing = []
    for name in persona_names:
        safe_name = safe_filename(name)
        if safe_name not in available_images:
            missing.append(name)
    
    count = len(persona_names)
    avail = count - len(missing)
    
    print(f"\n{game_name}:")
    print(f"  Total personas: {count}")
    print(f"  Available: {avail}")
    print(f"  Missing: {len(missing)}")
    if count > 0:
        print(f"  Success rate: {(avail/count*100):.1f}%")
    
    if missing and len(missing) <= 10:
        print(f"  Missing personas:")
        for name in sorted(missing):
            print(f"    - {name}")
    elif missing:
        print(f"  Missing personas (first 10):")
        for name in sorted(missing)[:10]:
            print(f"    - {name}")
        print(f"    ... and {len(missing) - 10} more")
    
    total_personas += count
    total_available += avail
    total_missing += len(missing)

print("\n" + "=" * 70)
print("OVERALL SUMMARY")
print("=" * 70)
print(f"Total personas: {total_personas}")
print(f"Total available: {total_available}")
print(f"Total missing: {total_missing}")
if total_personas > 0:
    print(f"Success rate: {(total_available/total_personas*100):.1f}%")
