import json
from pathlib import Path
import re

def safe_filename(name):
    """Convert name to safe filename format"""
    safe = name.lower().replace(" ", "_").replace("/", "_").replace(":", "").replace("?", "").replace("&", "").replace("-", "_").replace("'", "")
    safe = safe.replace("___", "_")
    return safe

# Load P3R enemy data
with open('../app/src/main/assets/data/enemies/p3r_enemies.json', 'r', encoding='utf-8') as f:
    data = json.load(f)

names = [name for name in data.keys() if name]

# Get unique base names (remove A-Z variants)
unique_base_names = set()
for name in names:
    base_name = re.sub(r'\s+[A-Z]$', '', name)
    unique_base_names.add(base_name)

# Get valid safe filenames
valid_filenames = set()
for name in unique_base_names:
    valid_filenames.add(safe_filename(name))

print(f"Valid P3R enemies: {len(valid_filenames)}")

# Check downloaded folder
p3r_folder = Path("downloaded_enemies/p3r")
all_files = list(p3r_folder.glob("*.png"))

print(f"Total files in folder: {len(all_files)}")

# Find files that don't match any P3R enemy
to_delete = []
for img_file in all_files:
    if img_file.stem not in valid_filenames:
        to_delete.append(img_file)

print(f"Files to delete (not P3R enemies): {len(to_delete)}")

if to_delete:
    print("\nDeleting extra files...")
    for img_file in to_delete:
        img_file.unlink()
        print(f"  Deleted: {img_file.name}")

print(f"\nCleaned! Remaining files: {len(list(p3r_folder.glob('*.png')))}")
