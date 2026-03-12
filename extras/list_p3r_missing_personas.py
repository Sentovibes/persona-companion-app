#!/usr/bin/env python3
"""
List missing P3R persona images
"""

import json
from pathlib import Path

def safe_filename(name):
    """Convert name to safe filename format"""
    safe = name.lower().replace(" ", "_").replace("/", "_").replace(":", "").replace("?", "").replace("&", "").replace("-", "_").replace("'", "")
    safe = safe.replace("___", "_")
    return safe

# Load P3R personas
with open("../app/src/main/assets/data/persona3/reload_personas.json", "r", encoding="utf-8") as f:
    data = json.load(f)

# Check in extras/images/personas/p3r and shared/personas
p3r_images_dir = Path("images/personas/p3r")
shared_images_dir = Path("images/shared/personas")

# Get all available images
available_images = set()
if p3r_images_dir.exists():
    for img in p3r_images_dir.glob("*.*"):
        available_images.add(img.stem.lower())
if shared_images_dir.exists():
    for img in shared_images_dir.glob("*.*"):
        available_images.add(img.stem.lower())

missing = []
for name in data.keys():
    safe_name = safe_filename(name)
    if safe_name not in available_images:
        missing.append(name)

print("=" * 60)
print("P3R MISSING PERSONAS")
print("=" * 60)
for i, name in enumerate(sorted(missing), 1):
    print(f"{i}. {name}")

print(f"\nTotal missing: {len(missing)}")
print(f"Total personas: {len(data)}")
print(f"Available: {len(data) - len(missing)}")
if len(data) > 0:
    print(f"Success rate: {((len(data) - len(missing))/len(data)*100):.1f}%")
