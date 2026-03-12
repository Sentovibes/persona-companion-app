import json
from pathlib import Path
import re

def safe_filename(name):
    """Convert name to safe filename format"""
    safe = name.lower().replace(" ", "_").replace("/", "_").replace(":", "").replace("?", "").replace("&", "").replace("-", "_")
    safe = safe.replace("___", "_")
    return safe

games = [
    ("p4", "../app/src/main/assets/data/enemies/p4_enemies.json"),
    ("p4g", "../app/src/main/assets/data/enemies/p4g_enemies.json"),
]

for game_id, json_path in games:
    print(f"\n{'='*60}")
    print(f"{game_id.upper()} MISSING ENEMIES")
    print(f"{'='*60}")
    
    # Load enemy data
    with open(json_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    names = [enemy.get('name', '') for enemy in data if isinstance(enemy, dict)]
    
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
    
    print(f"\nTotal unique enemies: {len(unique_base_names)}")
    print(f"Downloaded: {len(downloaded_safe_names)}")
    print(f"Missing: {len(missing)}")
    print(f"\nMissing enemies:")
    for name in sorted(missing):
        print(f"  - {name}")
