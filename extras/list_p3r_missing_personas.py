#!/usr/bin/env python3
"""
List missing P3R persona images
"""

import json
from pathlib import Path

# Load P3R personas
with open("../app/src/main/assets/data/persona3/reload_personas.json", "r", encoding="utf-8") as f:
    data = json.load(f)

# Check which have images
images_dir = Path("../app/src/main/assets/images/personas")
missing = []

for name in data.keys():
    safe_name = name.replace("/", "_").replace(":", "").replace("?", "")
    image_path = images_dir / f"{safe_name}.png"
    
    if not image_path.exists():
        missing.append(name)

print("=" * 60)
print("P3R MISSING PERSONAS")
print("=" * 60)
for i, name in enumerate(sorted(missing), 1):
    print(f"{i}. {name}")

print(f"\nTotal missing: {len(missing)}")
print("\nThese personas have 2D portraits in the game files!")
print("Extract from: P3R/Content/L10N/en/Xrd777/Battle/Persona/")
