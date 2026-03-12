import os
import shutil

# Source folder
source_folder = "images/shared/enemies"

# Missing enemies that are in shared folder
p5_from_shared = [
    "Shadow Akitsu",
    "Shadow Asakura",
    "Shadow Fukurai",
    "Shadow Honjo",
    "Shadow Isshiki",
    "Shadow Jochi",
    "Shadow Kiritani",
    "Shadow Kishi",
    "Shadow Makigami",
    "Shadow Mogami",
    "Shadow Naguri",
    "Shadow Nakanohara",
    "Shadow Nejima",
    "Shadow Oda",
    "Shadow Odo",
    "Shadow Oyamada",
    "Shadow Sakoda",
    "Shadow Shimizu",
    "Shadow Takanashi",
    "Shadow Togo",
    "Shadow Tsuboi",
    "Shadow Tsuda",
    "Shadow Uchimura",
    "Shadow Wakasa"
]

def safe_filename(name):
    """Convert filename to safe format (lowercase, spaces to underscores)"""
    return name.lower().replace(" ", "_").replace("/", "_").replace(":", "").replace("?", "").replace("&", "").replace("-", "_")

# Copy to both P5 and P5R
for game in ["p5", "p5r"]:
    print(f"\nCopying {game.upper()} enemies from shared folder...")
    target_folder = f"downloaded_enemies/{game}"
    os.makedirs(target_folder, exist_ok=True)
    
    for enemy_name in p5_from_shared:
        source_file = os.path.join(source_folder, f"{enemy_name}.png")
        safe_name = safe_filename(enemy_name)
        target_file = os.path.join(target_folder, f"{safe_name}.png")
        
        if os.path.exists(source_file):
            shutil.copy2(source_file, target_file)
            print(f"  ✓ Copied {enemy_name}")
        else:
            print(f"  ✗ Not found: {enemy_name}")

print("\nCopy complete!")
