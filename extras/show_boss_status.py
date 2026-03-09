#!/usr/bin/env python3
"""
Show boss coverage status (main bosses and mini-bosses)
"""

import json
from pathlib import Path

# Boss data files
boss_files = [
    ("P3 FES Main Bosses", "../app/src/main/assets/data/enemies/p3fes_main_bosses.json"),
    ("P3 FES Mini Bosses", "../app/src/main/assets/data/enemies/p3fes_mini_bosses.json"),
    ("P3P Main Bosses", "../app/src/main/assets/data/enemies/p3p_main_bosses.json"),
    ("P3P Mini Bosses", "../app/src/main/assets/data/enemies/p3p_mini_bosses.json"),
]

# Check which have images in app
app_images_dir = Path("../app/src/main/assets/images/enemies")
app_images = {img.stem for img in app_images_dir.glob("*.png")}

print("=" * 70)
print("BOSS IMAGE COVERAGE STATUS")
print("=" * 70)

total_bosses = 0
total_have = 0
all_missing_bosses = {}

for game_name, json_path in boss_files:
    try:
        with open(json_path, "r", encoding="utf-8") as f:
            data = json.load(f)
        
        # Handle both list and dict formats
        if isinstance(data, list):
            names = [boss.get("name", "") for boss in data if isinstance(boss, dict)]
        else:
            names = list(data.keys())
        
        total = len(names)
        missing = []
        
        for name in names:
            if not name:
                continue
            safe_name = name.replace("/", "_").replace(":", "").replace("?", "")
            if safe_name not in app_images:
                missing.append(name)
        
        have = total - len(missing)
        percentage = (have / total) * 100 if total > 0 else 0
        
        total_bosses += total
        total_have += have
        
        # Status indicator
        if len(missing) == 0:
            status = "✓ COMPLETE"
            print(f"{game_name:25} {have:2}/{total:2} ({percentage:5.1f}%) {status}")
        else:
            status = f"✗ {len(missing):2} missing"
            print(f"{game_name:25} {have:2}/{total:2} ({percentage:5.1f}%) {status}")
            all_missing_bosses[game_name] = sorted(missing)
    
    except FileNotFoundError:
        print(f"{game_name:25} - FILE NOT FOUND")

overall_percentage = (total_have / total_bosses) * 100 if total_bosses > 0 else 0

print("\n" + "=" * 70)
print(f"OVERALL BOSSES: {total_have}/{total_bosses} ({overall_percentage:.1f}%)")
print(f"Missing: {total_bosses - total_have} bosses")
print("=" * 70)

# Show missing bosses
if all_missing_bosses:
    print("\n" + "=" * 70)
    print("MISSING BOSSES BY CATEGORY")
    print("=" * 70)
    
    for game_name, missing_list in all_missing_bosses.items():
        print(f"\n{game_name} ({len(missing_list)} missing):")
        print("-" * 70)
        for i, name in enumerate(missing_list, 1):
            print(f"  {i:2}. {name}")

print("\n" + "=" * 70)
print("NOTES:")
print("=" * 70)
print("P4, P4G, P5, P5R don't have separate boss files.")
print("Their bosses are mixed with regular enemies in the main enemy files.")
print("Main bosses like Nyx, Izanami, Yaldabaoth are 3D models (no 2D art).")
