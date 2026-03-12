#!/usr/bin/env python3
"""
Calculate updated image coverage statistics after syncing images
"""

import json
from pathlib import Path

def safe_filename(name):
    """Convert name to safe filename format"""
    safe = name.lower().replace(" ", "_").replace("/", "_").replace(":", "").replace("?", "").replace("&", "").replace("-", "_").replace("'", "")
    safe = safe.replace("___", "_")
    return safe

# Check each game
games = [
    ("P3 FES", "../app/src/main/assets/data/persona3/personas.json", "p3fes"),
    ("P3 Portable", "../app/src/main/assets/data/persona3/portable_personas.json", "p3p"),
    ("P3 Reload", "../app/src/main/assets/data/persona3/reload_personas.json", "p3r"),
    ("P4", "../app/src/main/assets/data/persona4/personas.json", "p4"),
    ("P4 Golden", "../app/src/main/assets/data/persona4/golden_personas.json", "p4g"),
    ("P5", "../app/src/main/assets/data/persona5/personas.json", "p5"),
    ("P5 Royal", "../app/src/main/assets/data/persona5/royal_personas.json", "p5r"),
]

total_personas = 0
total_have = 0

print("=" * 60)
print("PERSONA IMAGE COVERAGE (UPDATED)")
print("=" * 60)

for game_name, json_path, folder_name in games:
    with open(json_path, "r", encoding="utf-8") as f:
        data = json.load(f)
    
    # Get available images for this game
    images_dir = Path(f"../app/src/main/assets/images/personas/{folder_name}")
    available_images = set()
    if images_dir.exists():
        for img in images_dir.glob("*.*"):
            available_images.add(img.stem.lower())
    
    total = len(data)
    have = sum(1 for name in data.keys() if safe_filename(name) in available_images)
    missing = total - have
    percentage = (have / total) * 100 if total > 0 else 0
    
    total_personas += total
    total_have += have
    
    print(f"{game_name}: {have}/{total} ({percentage:.1f}%) - Missing {missing}")

overall_percentage = (total_have / total_personas) * 100 if total_personas > 0 else 0
print("\n" + "=" * 60)
print(f"TOTAL PERSONAS: {total_have}/{total_personas} ({overall_percentage:.1f}%)")
print(f"Missing: {total_personas - total_have}")
print("=" * 60)

# Also show total images synced
print("\n" + "=" * 60)
print("IMAGES IN ASSETS")
print("=" * 60)
personas_base = Path("../app/src/main/assets/images/personas")
enemies_base = Path("../app/src/main/assets/images/enemies")

persona_count = 0
enemy_count = 0

if personas_base.exists():
    persona_count = sum(1 for _ in personas_base.rglob("*.*") if _.is_file())
if enemies_base.exists():
    enemy_count = sum(1 for _ in enemies_base.rglob("*.*") if _.is_file())

print(f"Persona images: {persona_count}")
print(f"Enemy images: {enemy_count}")
print(f"Total images: {persona_count + enemy_count}")
print("=" * 60)
