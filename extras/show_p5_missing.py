#!/usr/bin/env python3
"""
Show P5 and P5R missing personas with details
"""

import json
from pathlib import Path

# Load P5 data
with open("../app/src/main/assets/data/persona5/personas.json", "r", encoding="utf-8") as f:
    p5_data = json.load(f)

with open("../app/src/main/assets/data/persona5/royal_personas.json", "r", encoding="utf-8") as f:
    p5r_data = json.load(f)

# Check which have images
app_images_dir = Path("../app/src/main/assets/images/personas")
app_images = {img.stem for img in app_images_dir.glob("*.png")}

# P5 missing
p5_missing = []
for name in p5_data.keys():
    safe_name = name.replace("/", "_").replace(":", "").replace("?", "")
    if safe_name not in app_images:
        persona = p5_data[name]
        p5_missing.append({
            "name": name,
            "arcana": persona.get("arcana", "Unknown"),
            "level": persona.get("level", 0)
        })

# P5R missing
p5r_missing = []
for name in p5r_data.keys():
    safe_name = name.replace("/", "_").replace(":", "").replace("?", "")
    if safe_name not in app_images:
        persona = p5r_data[name]
        p5r_missing.append({
            "name": name,
            "arcana": persona.get("arcana", "Unknown"),
            "level": persona.get("level", 0)
        })

# Sort by level
p5_missing.sort(key=lambda x: x["level"])
p5r_missing.sort(key=lambda x: x["level"])

print("=" * 70)
print("PERSONA 5 MISSING PERSONAS")
print("=" * 70)
print(f"Total: {len(p5_missing)}/210 missing ({len(p5_missing)/210*100:.1f}%)")
print(f"Have: {210-len(p5_missing)}/210 ({(210-len(p5_missing))/210*100:.1f}%)")
print("=" * 70)

for i, p in enumerate(p5_missing, 1):
    print(f"{i:2}. {p['name']:<25} Lv {p['level']:2} - {p['arcana']}")

print("\n" + "=" * 70)
print("PERSONA 5 ROYAL MISSING PERSONAS")
print("=" * 70)
print(f"Total: {len(p5r_missing)}/232 missing ({len(p5r_missing)/232*100:.1f}%)")
print(f"Have: {232-len(p5r_missing)}/232 ({(232-len(p5r_missing))/232*100:.1f}%)")
print("=" * 70)

for i, p in enumerate(p5r_missing, 1):
    print(f"{i:2}. {p['name']:<25} Lv {p['level']:2} - {p['arcana']}")

# Check which are in both
p5_names = {p["name"] for p in p5_missing}
p5r_names = {p["name"] for p in p5r_missing}
in_both = p5_names & p5r_names

print("\n" + "=" * 70)
print("ANALYSIS")
print("=" * 70)
print(f"Missing in both P5 and P5R: {len(in_both)}")
print(f"P5 exclusive missing: {len(p5_names - p5r_names)}")
print(f"P5R exclusive missing: {len(p5r_names - p5_names)}")

if in_both:
    print("\nMissing in BOTH games:")
    for name in sorted(in_both):
        print(f"  - {name}")

if p5r_names - p5_names:
    print("\nP5R exclusive missing:")
    for name in sorted(p5r_names - p5_names):
        print(f"  - {name}")

print("\n" + "=" * 70)
print("NOTES:")
print("=" * 70)
print("Most missing are treasure demons (collectible gems).")
print("Add these to the were-missing folder to complete P5/P5R!")
