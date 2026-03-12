import os
import shutil

source_folder = "images/shared/enemies"
target_folder = "downloaded_enemies/p3r"

p3r_from_shared = {
    "Chidori.png": "chidori.png",
    "Mage Soldier.png": "mage_soldier.png",
    "Ruthless Ice Raven.png": "ruthless_ice_raven.png"
}

os.makedirs(target_folder, exist_ok=True)

print("Copying P3R enemies from shared folder...")
for source_name, target_name in p3r_from_shared.items():
    source_file = os.path.join(source_folder, source_name)
    target_file = os.path.join(target_folder, target_name)
    
    if os.path.exists(source_file):
        shutil.copy2(source_file, target_file)
        print(f"  ✓ Copied {source_name}")
    else:
        print(f"  ✗ Not found: {source_name}")

print("Copy complete!")
