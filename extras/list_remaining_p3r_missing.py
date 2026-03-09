#!/usr/bin/env python3
"""
List remaining missing P3R persona images after copying from extras
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
print("P3R REMAINING MISSING PERSONAS (After copying from extras)")
print("=" * 60)
for i, name in enumerate(sorted(missing), 1):
    print(f"{i}. {name}")

total_p3r = len(data)
have = total_p3r - len(missing)
percentage = (have / total_p3r) * 100

print(f"\nP3R Coverage: {have}/{total_p3r} ({percentage:.1f}%)")
print(f"Still missing: {len(missing)} personas")
