import json
from pathlib import Path
import re
import os

print("=" * 70)
print("CREATING WERE_MISSING FOLDER STRUCTURE FOR ENEMIES")
print("=" * 70)

games = [
    ("p3fes", "../app/src/main/assets/data/enemies/p3fes_enemies.json"),
    ("p3p", "../app/src/main/assets/data/enemies/p3p_enemies.json"),
    ("p3r", "../app/src/main/assets/data/enemies/p3r_enemies.json"),
    ("p4", "../app/src/main/assets/data/enemies/p4_enemies.json"),
    ("p4g", "../app/src/main/assets/data/enemies/p4g_enemies.json"),
    ("p5", "../app/src/main/assets/data/enemies/p5_enemies.json"),
    ("p5r", "../app/src/main/assets/data/enemies/p5r_enemies.json"),
]

base_folder = "were_missing_enemies"
os.makedirs(base_folder, exist_ok=True)

total_missing = 0

for game_id, json_path in games:
    print(f"\n{game_id.upper()}:")
    
    # Load enemy data
    with open(json_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    # Get unique base names
    unique_enemies = set()
    if isinstance(data, list):
        for enemy in data:
            if isinstance(enemy, dict):
                # For P5/P5R, use persona_name if available
                if game_id in ['p5', 'p5r'] and 'persona_name' in enemy:
                    name = enemy.get('persona_name', '')
                else:
                    name = enemy.get('name', '')
                if name:
                    base_name = re.sub(r'\s+[A-Z]$', '', name)
                    unique_enemies.add(base_name)
    else:
        for name in data.keys():
            base_name = re.sub(r'\s+[A-Z]$', '', name)
            unique_enemies.add(base_name)
    
    # Check what's downloaded
    download_folder = Path(f"downloaded_enemies/{game_id}")
    downloaded = set()
    if download_folder.exists():
        for img in download_folder.glob("*.*"):
            downloaded.add(img.stem)
    
    # Find missing
    missing = []
    for enemy in unique_enemies:
        safe_name = enemy.lower().replace(" ", "_").replace("/", "_").replace(":", "").replace("?", "")
        if safe_name not in downloaded:
            missing.append(enemy)
    
    print(f"  Total: {len(unique_enemies)}, Downloaded: {len(downloaded)}, Missing: {len(missing)}")
    
    if missing:
        # Create game folder in were_missing
        game_folder = os.path.join(base_folder, game_id)
        os.makedirs(game_folder, exist_ok=True)
        
        # Save missing list
        missing_file = os.path.join(game_folder, "missing_list.txt")
        with open(missing_file, 'w', encoding='utf-8') as f:
            f.write(f"MISSING {game_id.upper()} ENEMIES ({len(missing)} total)\n")
            f.write("=" * 70 + "\n\n")
            for i, enemy in enumerate(sorted(missing), 1):
                safe_name = enemy.lower().replace(" ", "_").replace("/", "_").replace(":", "").replace("?", "")
                f.write(f"{i:3}. {enemy} -> {safe_name}.png\n")
        
        print(f"  Created: {missing_file}")
        total_missing += len(missing)

print("\n" + "=" * 70)
print("SUMMARY")
print("=" * 70)
print(f"Total missing across all games: {total_missing}")
print(f"Folder structure created at: {base_folder}/")
print("\nYou can now manually download missing images and place them in:")
print("  were_missing_enemies/<game>/")
