import json
from pathlib import Path
import re

print("=" * 70)
print("MISSING P3R ENEMIES")
print("=" * 70)

# Load P3R enemy data
with open("../app/src/main/assets/data/enemies/p3r_enemies.json", 'r', encoding='utf-8') as f:
    data = json.load(f)

# Get unique base names
unique_enemies = set()
if isinstance(data, list):
    for enemy in data:
        if isinstance(enemy, dict) and enemy.get('name'):
            base_name = re.sub(r'\s+[B-Z]$', '', enemy.get('name', ''))
            unique_enemies.add(base_name)
else:
    # Dictionary format - keys are enemy names
    for name in data.keys():
        base_name = re.sub(r'\s+[A-Z]$', '', name)  # Strip A-Z suffix
        unique_enemies.add(base_name)

print(f"Total unique P3R enemies: {len(unique_enemies)}")

# Check what's downloaded
download_folder = Path("downloaded_enemies/p3r")
downloaded = set()
if download_folder.exists():
    for img in download_folder.glob("*.*"):
        downloaded.add(img.stem)

print(f"Downloaded: {len(downloaded)}")
print(f"Missing: {len(unique_enemies) - len(downloaded)}")

# Find missing
missing = []
for enemy in unique_enemies:
    safe_name = enemy.lower().replace(" ", "_").replace("/", "_").replace(":", "").replace("?", "")
    if safe_name not in downloaded:
        missing.append(enemy)

print("\n" + "=" * 70)
print(f"MISSING P3R ENEMIES ({len(missing)} total)")
print("=" * 70)

# Sort alphabetically
for i, enemy in enumerate(sorted(missing), 1):
    print(f"{i:3}. {enemy}")

# Save to file for easy reference
with open("missing_p3r_enemies.txt", 'w', encoding='utf-8') as f:
    f.write("MISSING P3R ENEMIES\n")
    f.write("=" * 70 + "\n\n")
    for i, enemy in enumerate(sorted(missing), 1):
        f.write(f"{i:3}. {enemy}\n")

print("\n" + "=" * 70)
print("List saved to: missing_p3r_enemies.txt")
print("=" * 70)
