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

print(f"Restoring P3R images from other game folders...")
print("="*70)

target_folder = Path("downloaded_enemies/p3r")
target_folder.mkdir(parents=True, exist_ok=True)

# Search in all other game folders
other_games = ['p3fes', 'p3p', 'p4', 'p4g', 'p5', 'p5r']
all_images = {}

for game in other_games:
    game_folder = Path(f"downloaded_enemies/{game}")
    if game_folder.exists():
        for img in game_folder.glob("*.png"):
            all_images[img.stem.lower()] = img

print(f"Found {len(all_images)} images in other game folders")

copied_count = 0

for enemy_name in sorted(unique_base_names):
    safe_name = safe_filename(enemy_name)
    target_path = target_folder / f"{safe_name}.png"
    
    if target_path.exists():
        copied_count += 1
        continue
    
    # Try to find in other games
    if safe_name in all_images:
        source_path = all_images[safe_name]
        shutil.copy2(source_path, target_path)
        print(f"✓ {enemy_name} (from {source_path.parent.name})")
        copied_count += 1

print("\n" + "="*70)
print(f"Restored: {copied_count}/{len(unique_base_names)}")
print("="*70)
