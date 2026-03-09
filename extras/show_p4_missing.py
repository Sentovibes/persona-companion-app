#!/usr/bin/env python3
"""
Show P4 and P4G missing personas with details
"""

import json
from pathlib import Path

# Load P4 data
with open("../app/src/main/assets/data/persona4/personas.json", "r", encoding="utf-8") as f:
    p4_data = json.load(f)

with open("../app/src/main/assets/data/persona4/golden_personas.json", "r", encoding="utf-8") as f:
    p4g_data = json.load(f)

# Check which have images
app_images_dir = Path("../app/src/main/assets/images/personas")
app_images = {img.stem for img in app_images_dir.glob("*.png")}

# P4 missing
p4_missing = []
for name in p4_data.keys():
    safe_name = name.replace("/", "_").replace(":", "").replace("?", "")
    if safe_name not in app_images:
        persona = p4_data[name]
        p4_missing.append({
            "name": name,
            "arcana": persona.get("arcana", "Unknown"),
            "level": persona.get("level", 0)
        })

# P4G missing
p4g_missing = []
for name in p4g_data.keys():
    safe_name = name.replace("/", "_").replace(":", "").replace("?", "")
    if safe_name not in app_images:
        persona = p4g_data[name]
        p4g_missing.append({
            "name": name,
            "arcana": persona.get("arcana", "Unknown"),
            "level": persona.get("level", 0)
        })

# Sort by level
p4_missing.sort(key=lambda x: x["level"])
p4g_missing.sort(key=lambda x: x["level"])

print("=" * 70)
print("PERSONA 4 MISSING PERSONAS")
print("=" * 70)
print(f"Total: {len(p4_missing)}/187 missing ({len(p4_missing)/187*100:.1f}%)")
print(f"Have: {187-len(p4_missing)}/187 ({(187-len(p4_missing))/187*100:.1f}%)")
print("=" * 70)

for i, p in enumerate(p4_missing, 1):
    print(f"{i:2}. {p['name']:<25} Lv {p['level']:2} - {p['arcana']}")

print("\n" + "=" * 70)
print("PERSONA 4 GOLDEN MISSING PERSONAS")
print("=" * 70)
print(f"Total: {len(p4g_missing)}/205 missing ({len(p4g_missing)/205*100:.1f}%)")
print(f"Have: {205-len(p4g_missing)}/205 ({(205-len(p4g_missing))/205*100:.1f}%)")
print("=" * 70)

for i, p in enumerate(p4g_missing, 1):
    print(f"{i:2}. {p['name']:<25} Lv {p['level']:2} - {p['arcana']}")

# Check which are in both
p4_names = {p["name"] for p in p4_missing}
p4g_names = {p["name"] for p in p4g_missing}
in_both = p4_names & p4g_names

print("\n" + "=" * 70)
print("ANALYSIS")
print("=" * 70)
print(f"Missing in both P4 and P4G: {len(in_both)}")
print(f"P4 exclusive missing: {len(p4_names - p4g_names)}")
print(f"P4G exclusive missing: {len(p4g_names - p4_names)}")

if in_both:
    print("\nMissing in BOTH games:")
    for name in sorted(in_both):
        print(f"  - {name}")

print("\n" + "=" * 70)
print("NOTES:")
print("=" * 70)
print("These personas need to be downloaded from wiki/Google.")
print("Most are rare fusion-only or DLC personas.")
