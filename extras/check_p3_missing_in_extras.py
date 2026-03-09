#!/usr/bin/env python3
"""
Check which P3 FES and P3P personas are missing and if they exist in extras
"""

import json
from pathlib import Path

# Load persona data
with open("../app/src/main/assets/data/persona3/personas.json", "r", encoding="utf-8") as f:
    p3fes_data = json.load(f)

with open("../app/src/main/assets/data/persona3/portable_personas.json", "r", encoding="utf-8") as f:
    p3p_data = json.load(f)

# Check which have images in app
app_images_dir = Path("../app/src/main/assets/images/personas")
app_images = {img.stem for img in app_images_dir.glob("*.png")}

# Check P3 FES missing
p3fes_missing = []
for name in p3fes_data.keys():
    safe_name = name.replace("/", "_").replace(":", "").replace("?", "")
    if safe_name not in app_images:
        p3fes_missing.append(name)

# Check P3P missing
p3p_missing = []
for name in p3p_data.keys():
    safe_name = name.replace("/", "_").replace(":", "").replace("?", "")
    if safe_name not in app_images:
        p3p_missing.append(name)

# Check extras folder
extras_dir = Path("images/personas")
extras_found = {}

for game_folder in extras_dir.glob("*"):
    if game_folder.is_dir() and game_folder.name != "were-missing":
        for img in game_folder.glob("*.png"):
            if img.stem not in extras_found:
                extras_found[img.stem] = []
            extras_found[img.stem].append(game_folder.name)

print("=" * 60)
print("P3 FES MISSING PERSONAS")
print("=" * 60)
p3fes_found_in_extras = []
p3fes_not_found = []

for name in sorted(p3fes_missing):
    safe_name = name.replace("/", "_").replace(":", "").replace("?", "")
    if safe_name in extras_found:
        p3fes_found_in_extras.append((name, extras_found[safe_name]))
        print(f"✓ {name} - FOUND in {', '.join(extras_found[safe_name])}")
    else:
        p3fes_not_found.append(name)
        print(f"✗ {name} - NOT FOUND")

print(f"\nP3 FES: {len(p3fes_missing)} missing total")
print(f"  - {len(p3fes_found_in_extras)} found in extras")
print(f"  - {len(p3fes_not_found)} not found anywhere")

print("\n" + "=" * 60)
print("P3 PORTABLE MISSING PERSONAS")
print("=" * 60)
p3p_found_in_extras = []
p3p_not_found = []

for name in sorted(p3p_missing):
    safe_name = name.replace("/", "_").replace(":", "").replace("?", "")
    if safe_name in extras_found:
        p3p_found_in_extras.append((name, extras_found[safe_name]))
        print(f"✓ {name} - FOUND in {', '.join(extras_found[safe_name])}")
    else:
        p3p_not_found.append(name)
        print(f"✗ {name} - NOT FOUND")

print(f"\nP3P: {len(p3p_missing)} missing total")
print(f"  - {len(p3p_found_in_extras)} found in extras")
print(f"  - {len(p3p_not_found)} not found anywhere")

# Summary
print("\n" + "=" * 60)
print("SUMMARY - CAN COPY FROM EXTRAS")
print("=" * 60)
all_to_copy = set()
for name, locations in p3fes_found_in_extras + p3p_found_in_extras:
    safe_name = name.replace("/", "_").replace(":", "").replace("?", "")
    all_to_copy.add((safe_name, locations[0]))

for safe_name, location in sorted(all_to_copy):
    print(f"  {safe_name}.png from {location}/")

print(f"\nTotal to copy: {len(all_to_copy)}")
