#!/usr/bin/env python3
"""
Check if any P4/P4G/P5/P5R missing personas exist in extras folder
"""

import json
from pathlib import Path

# Load persona data
games = [
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
all_to_copy = []

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
            found_in_extras.append((name, safe_name, extras_found[safe_name]))
            all_to_copy.append((safe_name, extras_found[safe_name][0]))
            print(f"✓ {name} - FOUND in {', '.join(extras_found[safe_name])}")
        else:
            not_found.append(name)
            print(f"✗ {name}")
    
    print(f"\nFound in extras: {len(found_in_extras)}/{len(missing)}")
    if not_found:
        print(f"Not found: {len(not_found)}")

# Summary
if all_to_copy:
    print("\n" + "=" * 60)
    print("SUMMARY - PERSONAS TO COPY FROM EXTRAS")
    print("=" * 60)
    # Remove duplicates
    unique_to_copy = list(set(all_to_copy))
    for safe_name, location in sorted(unique_to_copy):
        print(f"  {safe_name}.png from {location}/")
    
    print(f"\nTotal to copy: {len(unique_to_copy)}")
else:
    print("\n" + "=" * 60)
    print("✓ NO PERSONAS FOUND IN EXTRAS")
    print("=" * 60)
    print("All missing P4/P4G/P5/P5R personas need to be downloaded.")
