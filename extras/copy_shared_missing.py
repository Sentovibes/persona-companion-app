import os
import shutil

# Source folder
source_folder = "images/shared/enemies/were missing"

# Target folders
target_folders = ["downloaded_enemies/p3fes", "downloaded_enemies/p3p"]

# Files to copy
files = [
    "Affection Relic.png",
    "Conceited Maya.png",
    "Indolent Maya.png",
    "Judgement Sword.png",
    "Loss Giant.png",
    "Merciful Maya.png"
]

def safe_filename(name):
    """Convert filename to safe format (lowercase, spaces to underscores)"""
    return name.lower().replace(" ", "_")

# Copy files to both target folders
for target_folder in target_folders:
    os.makedirs(target_folder, exist_ok=True)
    
    for file in files:
        source_path = os.path.join(source_folder, file)
        safe_name = safe_filename(file)
        target_path = os.path.join(target_folder, safe_name)
        
        if os.path.exists(source_path):
            shutil.copy2(source_path, target_path)
            print(f"Copied {file} -> {target_folder}/{safe_name}")
        else:
            print(f"WARNING: Source file not found: {source_path}")

print("\nCopy complete!")
