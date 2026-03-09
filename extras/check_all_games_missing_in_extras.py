#!/usr/bin/env python3
"""
Check which personas are missing for ALL games and if they exist in extras
"""

import json
from pathlib import Path

# Load all persona data
games = [
    ("P3 FES", "../app/src/main/assets/data/persona3/personas.json"),
    ("P3P", "../app/src/main/assets/data/persona3/portable_personas.json"),
    ("P3R", "../app/src/main/assets/data/persona3/reload_personas.json"),
    ("P4", "../app/src/main/assets/data/persona4/personas.json"),
    ("P4G", "../app/src/main/assets/data/persona4/golden_personas.json"),
    ("P5", "../app/src/main/assets/data/persona5/personas.json"),
    ("P5R", "../app/src/main/assets/data/persona5/royal_personas.json"),
]

# Check which have images in app
app_images_dir = Path("../app/src/main/assets/images/personas")
app_images = {img.stem for img in app_images_dir.glob("*.png")}

# Check extras folder
extras_dir = Path("images/personas")
extras_found = {}

for game_folder in extras_dir.glob("*"):
    if game_folder.is_dir() and game_folder.name != "were-missing":
        for img in game_folder.glob("*.png"):
            if img.stem not in extras_found:
                extras_found[img.stem] = []
            extras_found[img.stem].append(game_folder.name)

# Check each game
all_to_copy = set()

for game_name, json_path in games:
    with open(json_path, "r", encoding="utf-8") as f:
        data = json.load(f)
    
    missing = []
    for name in data.keys():
        safe_name = name.replace("/", "_").replace(":", "").replace("?", "")
        if safe_name not in app_images:
            missing.append(name)
    
    if not missing:
        print(f"{game_name}: ✓ COMPLETE (0 missing)")
        continue
    
    print(f"\n{'=' * 60}")
    print(f"{game_name} - {len(missing)} MISSING")
    print('=' * 60)
    
    found_in_extras = []
    not_found = []
    
    for name in sorted(missing):
        safe_name = name.replace("/", "_").replace(":", "").replace("?", "")
        if safe_name in extras_found:
            found_in_extras.append((name, extras_found[safe_name]))
            all_to_copy.add((safe_name, extras_found[safe_name][0]))
            print(f"✓ {name} - in {', '.join(extras_found[safe_name])}")
        else:
            not_found.append(name)
            print(f"✗ {name}")
    
    print(f"\nFound in extras: {len(found_in_extras)}/{len(missing)}")

# Summary
print("\n" + "=" * 60)
print("SUMMARY - PERSONAS TO COPY FROM EXTRAS")
print("=" * 60)
for safe_name, location in sorted(all_to_copy):
    print(f"  {safe_name}.png from {location}/")

print(f"\nTotal to copy: {len(all_to_copy)}")
