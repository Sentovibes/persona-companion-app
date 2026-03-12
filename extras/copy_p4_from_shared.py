import os
import shutil

# Source folder
source_folder = "images/shared/enemies"

# Missing enemies for P4 and P4G
p4_missing = [
    "Contrarian King",
    "Escapist Soldier",
    "Extreme Vessel",
    "Intolerant Officer",
    "Judgement Sword",
    "Lost Okina",
    "Momentary Child",
    # "Rainy Brother 3" - not in shared folder
]

p4g_missing = [
    "Contrarian King",
    "Escapist Soldier",
    "Extreme Vessel",
    "Intolerant Officer",
    "Judgement Sword",
    "Kusumi-no-Okami",  # Not in shared folder
    "Lost Okina",
    "Momentary Child"
]

def safe_filename(name):
    """Convert filename to safe format (lowercase, spaces to underscores)"""
    return name.lower().replace(" ", "_").replace("/", "_").replace(":", "").replace("?", "").replace("-", "_")

# Copy P4 enemies
print("Copying P4 enemies from shared folder...")
target_folder = "downloaded_enemies/p4"
os.makedirs(target_folder, exist_ok=True)

for enemy_name in p4_missing:
    source_file = os.path.join(source_folder, f"{enemy_name}.png")
    safe_name = safe_filename(enemy_name)
    target_file = os.path.join(target_folder, f"{safe_name}.png")
    
    if os.path.exists(source_file):
        shutil.copy2(source_file, target_file)
        print(f"  ✓ Copied {enemy_name}")
    else:
        print(f"  ✗ Not found: {enemy_name}")

# Copy P4G enemies
print("\nCopying P4G enemies from shared folder...")
target_folder = "downloaded_enemies/p4g"
os.makedirs(target_folder, exist_ok=True)

for enemy_name in p4g_missing:
    source_file = os.path.join(source_folder, f"{enemy_name}.png")
    safe_name = safe_filename(enemy_name)
    target_file = os.path.join(target_folder, f"{safe_name}.png")
    
    if os.path.exists(source_file):
        shutil.copy2(source_file, target_file)
        print(f"  ✓ Copied {enemy_name}")
    else:
        print(f"  ✗ Not found: {enemy_name}")

print("\nCopy complete!")
