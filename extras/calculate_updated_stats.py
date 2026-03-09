#!/usr/bin/env python3
"""
Calculate updated image coverage statistics after adding P3R personas
"""

import json
from pathlib import Path

# Get all available images
images_dir = Path("../app/src/main/assets/images/personas")
available_images = {img.stem for img in images_dir.glob("*.png")}

print(f"Total persona images available: {len(available_images)}\n")

# Check each game
games = [
    ("P3 FES", "../app/src/main/assets/data/persona3/personas.json"),
    ("P3 Portable", "../app/src/main/assets/data/persona3/portable_personas.json"),
    ("P3 Reload", "../app/src/main/assets/data/persona3/reload_personas.json"),
    ("P4", "../app/src/main/assets/data/persona4/personas.json"),
    ("P4 Golden", "../app/src/main/assets/data/persona4/golden_personas.json"),
    ("P5", "../app/src/main/assets/data/persona5/personas.json"),
    ("P5 Royal", "../app/src/main/assets/data/persona5/royal_personas.json"),
]

total_personas = 0
total_have = 0

print("=" * 60)
print("PERSONA IMAGE COVERAGE (UPDATED)")
print("=" * 60)

for game_name, json_path in games:
    with open(json_path, "r", encoding="utf-8") as f:
        data = json.load(f)
    
    total = len(data)
    have = sum(1 for name in data.keys() if name.replace("/", "_").replace(":", "").replace("?", "") in available_images)
    missing = total - have
    percentage = (have / total) * 100
    
    total_personas += total
    total_have += have
    
    print(f"{game_name}: {have}/{total} ({percentage:.1f}%) - Missing {missing}")

overall_percentage = (total_have / total_personas) * 100
print("\n" + "=" * 60)
print(f"TOTAL PERSONAS: {total_have}/{total_personas} ({overall_percentage:.1f}%)")
print(f"Missing: {total_personas - total_have}")
print("=" * 60)
