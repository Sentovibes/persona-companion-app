#!/usr/bin/env python3
"""
Show complete enemy/boss coverage status across all games
"""

import json
from pathlib import Path

# Load all enemy data
games = [
    ("P3 FES", "../app/src/main/assets/data/enemies/p3fes_enemies.json"),
    ("P3 Portable", "../app/src/main/assets/data/enemies/p3p_enemies.json"),
    ("P3 Reload", "../app/src/main/assets/data/enemies/p3r_enemies.json"),
    ("P4", "../app/src/main/assets/data/enemies/p4_enemies.json"),
    ("P4 Golden", "../app/src/main/assets/data/enemies/p4g_enemies.json"),
    ("P5", "../app/src/main/assets/data/enemies/p5_enemies.json"),
    ("P5 Royal", "../app/src/main/assets/data/enemies/p5r_enemies.json"),
]

# Check which have images in app
app_images_dir = Path("../app/src/main/assets/images/enemies")
app_images = {img.stem for img in app_images_dir.glob("*.png")}

print("=" * 70)
print("ENEMY IMAGE COVERAGE - COMPLETE STATUS")
print("=" * 70)
print(f"Total enemy images available: {len(app_images)}\n")

total_enemies = 0
total_have = 0
all_missing = {}

for game_name, json_path in games:
    try:
        with open(json_path, "r", encoding="utf-8") as f:
            data = json.load(f)
        
        total = len(data)
        missing = []
        
        # Handle both list and dict formats
        if isinstance(data, list):
            names = [enemy.get("name", "") for enemy in data if isinstance(enemy, dict)]
        else:
            names = list(data.keys())
        
        for name in names:
            if not name:
                continue
            safe_name = name.replace("/", "_").replace(":", "").replace("?", "")
            if safe_name not in app_images:
                missing.append(name)
        
        have = total - len(missing)
        percentage = (have / total) * 100 if total > 0 else 0
        
        total_enemies += total
        total_have += have
        
        # Status indicator
        if len(missing) == 0:
            status = "✓ COMPLETE"
            print(f"{game_name:15} {have:3}/{total:3} ({percentage:5.1f}%) {status}")
        else:
            status = f"✗ {len(missing):3} missing"
            print(f"{game_name:15} {have:3}/{total:3} ({percentage:5.1f}%) {status}")
            all_missing[game_name] = sorted(missing)
    
    except FileNotFoundError:
        print(f"{game_name:15} - FILE NOT FOUND")

overall_percentage = (total_have / total_enemies) * 100 if total_enemies > 0 else 0

print("\n" + "=" * 70)
print(f"OVERALL: {total_have}/{total_enemies} ({overall_percentage:.1f}%)")
print(f"Missing: {total_enemies - total_have} enemies")
print("=" * 70)

# Categorize enemies by type (if possible)
print("\n" + "=" * 70)
print("MISSING ENEMIES BY GAME (showing first 20 per game)")
print("=" * 70)

for game_name, missing_list in all_missing.items():
    print(f"\n{game_name} ({len(missing_list)} missing):")
    print("-" * 70)
    
    # Show first 20
    for i, name in enumerate(missing_list[:20], 1):
        print(f"  {i:2}. {name}")
    
    if len(missing_list) > 20:
        print(f"  ... and {len(missing_list) - 20} more")

print("\n" + "=" * 70)
print("NOTES:")
print("=" * 70)
print("Many enemies are 3D models with no official 2D art.")
print("Focus should be on main bosses and mini-bosses with official art.")
print("Generic enemies (Shadows, Tartarus enemies) often lack 2D portraits.")
