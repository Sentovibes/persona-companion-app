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

# Get all images from shared folder (recursively)
shared_folder = Path("images/shared")
all_shared_images = {}
for img_path in shared_folder.rglob("*.png"):
    # Store both with and without special characters for matching
    stem = img_path.stem
    all_shared_images[stem.lower()] = img_path
    # Also store safe version
    safe = safe_filename(stem)
    all_shared_images[safe] = img_path

target_folder = Path("downloaded_enemies/p3r")
target_folder.mkdir(parents=True, exist_ok=True)

copied = 0
skipped = 0
not_found = []

for enemy_name in sorted(unique_base_names):
    safe_name = safe_filename(enemy_name)
    target_path = target_folder / f"{safe_name}.png"
    
    # Skip if already exists
    if target_path.exists():
        skipped += 1
        continue
    
    # Try to find in shared folder
    source_path = None
    
    # Try exact match first
    if enemy_name in all_shared_images:
        source_path = all_shared_images[enemy_name]
    elif enemy_name.lower() in all_shared_images:
        source_path = all_shared_images[enemy_name.lower()]
    elif safe_name in all_shared_images:
        source_path = all_shared_images[safe_name]
    
    if source_path:
        shutil.copy2(source_path, target_path)
        print(f"✓ {enemy_name} (from {source_path.parent.name})")
        copied += 1
    else:
        not_found.append(enemy_name)

print("\n" + "="*70)
print(f"Copied: {copied}")
print(f"Skipped (already exist): {skipped}")
print(f"Not found: {len(not_found)}")

if not_found:
    print("\nStill missing:")
    for name in not_found[:20]:
        print(f"  - {name}")
    if len(not_found) > 20:
        print(f"  ... and {len(not_found) - 20} more")

print("="*70)
