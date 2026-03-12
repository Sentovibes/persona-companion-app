import json
from pathlib import Path
import re

def safe_filename(name):
    """Convert name to safe filename format"""
    safe = name.lower().replace(" ", "_").replace("/", "_").replace(":", "").replace("?", "").replace("&", "").replace("-", "_").replace("'", "")
    safe = safe.replace("___", "_")
    return safe

# Load P3R Episode Aigis enemy data
with open('../app/src/main/assets/data/enemies/p3r_episode_aigis/enemies.json', 'r', encoding='utf-8') as f:
    data = json.load(f)

names = [name for name in data.keys() if name]

# Get unique base names (remove A-Z variants)
unique_base_names = set()
for name in names:
    base_name = re.sub(r'\s+[A-Z]$', '', name)
    unique_base_names.add(base_name)

# Check downloaded folder (use same P3R folder since they're the same game)
download_folder = Path("downloaded_enemies/p3r")
downloaded_safe_names = set()
if download_folder.exists():
    for img in download_folder.glob("*.*"):
        downloaded_safe_names.add(img.stem)

# Find missing
missing = []
for name in unique_base_names:
    safe_name = safe_filename(name)
    if safe_name not in downloaded_safe_names:
        missing.append(name)

print(f"P3R EPISODE AIGIS MISSING ENEMIES: {len(missing)} total")
print("="*70)
for i, name in enumerate(sorted(missing), 1):
    print(f"{i:2}. {name}")

print("\n" + "="*70)
print(f"Total missing: {len(missing)}")
print(f"Total unique: {len(unique_base_names)}")
print(f"Downloaded: {len(unique_base_names) - len(missing)}")
print(f"Success rate: {((len(unique_base_names) - len(missing))/len(unique_base_names)*100):.1f}%")
