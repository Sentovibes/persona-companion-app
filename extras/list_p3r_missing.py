#!/usr/bin/env python3
"""
List missing P3R enemy images
"""

import json
from pathlib import Path

# Load P3R enemies
with open("../app/src/main/assets/data/enemies/p3r_enemies.json", "r", encoding="utf-8") as f:
    enemies = json.load(f)

# Check which have images
images_dir = Path("../app/src/main/assets/images/enemies")
missing = []
bosses = []

for enemy in enemies:
    name = enemy["name"]
    safe_name = name.replace("/", "_").replace(":", "").replace("?", "")
    image_path = images_dir / f"{safe_name}.png"
    
    if not image_path.exists():
        # Check if it's a boss (has phases or is a known boss)
        is_boss = (
            "Nyx" in name or
            "Elizabeth" in name or
            "Erebus" in name or
            "Reaper" in name or
            "Strega" in name or
            enemy.get("phases") is not None or
            enemy.get("level", 0) > 80
        )
        
        if is_boss:
            bosses.append(name)
        else:
            missing.append(name)

print("=" * 60)
print("P3R MISSING BOSSES (HIGH PRIORITY)")
print("=" * 60)
for i, name in enumerate(sorted(bosses), 1):
    print(f"{i}. {name}")

print(f"\nTotal missing bosses: {len(bosses)}")
print(f"Total missing regular enemies: {len(missing)}")
print(f"Total missing: {len(bosses) + len(missing)}")
