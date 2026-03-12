import json
from pathlib import Path
import re

def safe_filename(name):
    """Convert name to safe filename format"""
    safe = name.lower().replace(" ", "_").replace("/", "_").replace(":", "").replace("?", "").replace("&", "").replace("-", "_").replace("'", "")
    safe = safe.replace("___", "_")
    return safe

games = [
    ("p3fes", "../app/src/main/assets/data/enemies/p3fes_enemies.json"),
    ("p3p", "../app/src/main/assets/data/enemies/p3p_enemies.json"),
    ("p3r", "../app/src/main/assets/data/enemies/p3r_enemies.json"),
    ("p4", "../app/src/main/assets/data/enemies/p4_enemies.json"),
    ("p4g", "../app/src/main/assets/data/enemies/p4g_enemies.json"),
    ("p5", "../app/src/main/assets/data/enemies/p5_enemies.json"),
    ("p5r", "../app/src/main/assets/data/enemies/p5r_enemies.json"),
]

print("="*70)
print("ALL MISSING ENEMIES BY GAME")
print("="*70)

total_missing = 0

for game_id, json_path in games:
    # Load enemy data
    with open(json_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    if isinstance(data, list):
        names = []
        for enemy in data:
            if isinstance(enemy, dict):
                # For P5/P5R, use persona_name if available
                if game_id in ['p5', 'p5r'] and 'persona_name' in enemy:
                    names.append(enemy.get('persona_name', ''))
                else:
                    names.append(enemy.get('name', ''))
    else:
        names = [name for name in data.keys() if name]
    
    # Get unique base names (remove A-Z variants)
    unique_base_names = set()
    for name in names:
        base_name = re.sub(r'\s+[A-Z]$', '', name)
        unique_base_names.add(base_name)
    
    # Check downloaded folder
    download_folder = Path(f"downloaded_enemies/{game_id}")
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
    
    if missing:
        print(f"\n{game_id.upper()}: {len(missing)} missing")
        print("-" * 70)
        for name in sorted(missing):
            print(f"  - {name}")
        total_missing += len(missing)
    else:
        print(f"\n{game_id.upper()}: ✓ Complete (0 missing)")

print("\n" + "="*70)
print(f"TOTAL MISSING ACROSS ALL GAMES: {total_missing}")
print("="*70)
