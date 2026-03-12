import json
import shutil
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

print(f"Looking for {len(unique_base_names)} unique P3R enemies in shared folder...")
print("="*70)

# Source and target folders
shared_folder = Path("images/shared/enemies")
target_folder = Path("downloaded_enemies/p3r")
target_folder.mkdir(parents=True, exist_ok=True)

# Get all images from shared folder (recursively)
all_shared_images = {}
for img_path in shared_folder.rglob("*.png"):
    all_shared_images[img_path.stem.lower()] = img_path

copied_count = 0
not_found = []

for enemy_name in sorted(unique_base_names):
    safe_name = safe_filename(enemy_name)
    target_path = target_folder / f"{safe_name}.png"
    
    # Skip if already exists
    if target_path.exists():
        print(f"✓ {enemy_name} (already exists)")
        copied_count += 1
        continue
    
    # Try to find in shared folder
    if safe_name in all_shared_images:
        source_path = all_shared_images[safe_name]
        shutil.copy2(source_path, target_path)
        print(f"✓ {enemy_name} (copied from {source_path.parent.name})")
        copied_count += 1
    else:
        not_found.append(enemy_name)

print("\n" + "="*70)
print(f"Copied: {copied_count}/{len(unique_base_names)}")
print(f"Not found: {len(not_found)}")

if not_found:
    print("\nMissing enemies:")
    for name in not_found[:20]:
        print(f"  - {name}")
    if len(not_found) > 20:
        print(f"  ... and {len(not_found) - 20} more")
