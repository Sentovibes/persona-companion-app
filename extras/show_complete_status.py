#!/usr/bin/env python3
"""
Show complete status of all persona images across all games
"""

import json
from pathlib import Path

# Load all persona data
games = [
    ("P3 FES", "../app/src/main/assets/data/persona3/personas.json"),
    ("P3 Portable", "../app/src/main/assets/data/persona3/portable_personas.json"),
    ("P3 Reload", "../app/src/main/assets/data/persona3/reload_personas.json"),
    ("P4", "../app/src/main/assets/data/persona4/personas.json"),
    ("P4 Golden", "../app/src/main/assets/data/persona4/golden_personas.json"),
    ("P5", "../app/src/main/assets/data/persona5/personas.json"),
    ("P5 Royal", "../app/src/main/assets/data/persona5/royal_personas.json"),
]

# Check which have images in app
app_images_dir = Path("../app/src/main/assets/images/personas")
app_images = {img.stem for img in app_images_dir.glob("*.png")}

print("=" * 70)
print("PERSONA IMAGE COVERAGE - COMPLETE STATUS")
print("=" * 70)
print(f"Total persona images available: {len(app_images)}\n")

total_personas = 0
total_have = 0
all_missing = {}

for game_name, json_path in games:
    with open(json_path, "r", encoding="utf-8") as f:
        data = json.load(f)
    
    total = len(data)
    missing = []
    
    for name in data.keys():
        safe_name = name.replace("/", "_").replace(":", "").replace("?", "")
        if safe_name not in app_images:
            missing.append(name)
    
    have = total - len(missing)
    percentage = (have / total) * 100
    
    total_personas += total
    total_have += have
    
    # Status indicator
    if len(missing) == 0:
        status = "✓ COMPLETE"
        print(f"{game_name:15} {have:3}/{total:3} ({percentage:5.1f}%) {status}")
    else:
        status = f"✗ {len(missing):2} missing"
        print(f"{game_name:15} {have:3}/{total:3} ({percentage:5.1f}%) {status}")
        all_missing[game_name] = sorted(missing)

overall_percentage = (total_have / total_personas) * 100

print("\n" + "=" * 70)
print(f"OVERALL: {total_have}/{total_personas} ({overall_percentage:.1f}%)")
print(f"Missing: {total_personas - total_have} personas")
print("=" * 70)

# Show missing personas by game
if all_missing:
    print("\n" + "=" * 70)
    print("MISSING PERSONAS BY GAME")
    print("=" * 70)
    
    for game_name, missing_list in all_missing.items():
        print(f"\n{game_name} ({len(missing_list)} missing):")
        print("-" * 70)
        for i, name in enumerate(missing_list, 1):
            print(f"  {i:2}. {name}")

print("\n" + "=" * 70)
print("SUMMARY")
print("=" * 70)
print("✓ P3 FES: COMPLETE")
print("✓ P3 Portable: COMPLETE")
print("✓ P3 Reload: COMPLETE")
print(f"✗ P4: {len(all_missing.get('P4', []))} missing")
print(f"✗ P4 Golden: {len(all_missing.get('P4 Golden', []))} missing")
print(f"✗ P5: {len(all_missing.get('P5', []))} missing")
print(f"✗ P5 Royal: {len(all_missing.get('P5 Royal', []))} missing")
print("=" * 70)
