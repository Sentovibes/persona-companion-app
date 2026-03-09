#!/usr/bin/env python3
"""
Show P3 FES missing personas with details
"""

import json
from pathlib import Path

# Load P3 FES data
with open("../app/src/main/assets/data/persona3/personas.json", "r", encoding="utf-8") as f:
    p3fes_data = json.load(f)

# Check which have images
app_images_dir = Path("../app/src/main/assets/images/personas")
app_images = {img.stem for img in app_images_dir.glob("*.png")}

missing = []
for name in p3fes_data.keys():
    safe_name = name.replace("/", "_").replace(":", "").replace("?", "")
    if safe_name not in app_images:
        persona = p3fes_data[name]
        missing.append({
            "name": name,
            "arcana": persona.get("arcana", "Unknown"),
            "level": persona.get("level", 0)
        })

# Sort by level
missing.sort(key=lambda x: x["level"])

print("=" * 60)
print("P3 FES MISSING PERSONAS")
print("=" * 60)
print(f"Total: {len(missing)}/169 missing ({len(missing)/169*100:.1f}%)")
print(f"Have: {169-len(missing)}/169 ({(169-len(missing))/169*100:.1f}%)")
print("=" * 60)

for i, p in enumerate(missing, 1):
    print(f"{i:2}. {p['name']:<25} Lv {p['level']:2} - {p['arcana']}")

print("\n" + "=" * 60)
print("NOTES:")
print("=" * 60)
print("These are P3 FES exclusive or rare personas.")
print("Most are also in P3P (same missing list).")
print("They need to be downloaded from wiki/Google.")
