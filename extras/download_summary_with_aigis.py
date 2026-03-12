import json
from pathlib import Path
import re

print("=" * 70)
print("ENEMY IMAGE DOWNLOAD SUMMARY (Including Episode Aigis)")
print("=" * 70)

games = [
    ("p3fes", "app/src/main/assets/data/enemies/p3fes_enemies.json"),
    ("p3p", "app/src/main/assets/data/enemies/p3p_enemies.json"),
    ("p3r", "app/src/main/assets/data/enemies/p3r_enemies.json"),
    ("p3r_aigis", "app/src/main/assets/data/enemies/p3r_episode_aigis/enemies.json"),
    ("p4", "app/src/main/assets/data/enemies/p4_enemies.json"),
    ("p4g", "app/src/main/assets/data/enemies/p4g_enemies.json"),
    ("p5", "app/src/main/assets/data/enemies/p5_enemies.json"),
    ("p5r", "app/src/main/assets/data/enemies/p5r_enemies.json"),
]

total_needed = 0
total_downloaded = 0
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
    
    # Get unique base names (remove A/B/C/D variants)
    unique_base_names = set()
    for name in names:
        base_name = re.sub(r'\s+[A-Z]$', '', name)
        unique_base_names.add(base_name)
    
    # Check downloaded folder (Episode Aigis uses same p3r folder)
    folder_name = "p3r" if game_id == "p3r_aigis" else game_id
    download_folder = Path(f"extras/downloaded_enemies/{folder_name}")
    downloaded_safe_names = set()
    if download_folder.exists():
        for img in download_folder.glob("*.*"):
            downloaded_safe_names.add(img.stem)
    
    # Convert unique base names to safe names for comparison
    needed_safe_names = set()
    for name in unique_base_names:
        safe_name = name.lower().replace(" ", "_").replace("/", "_").replace(":", "").replace("?", "").replace("&", "").replace("-", "_").replace("'", "")
        # Handle triple underscores from & conversion
        safe_name = safe_name.replace("___", "_")
        needed_safe_names.add(safe_name)
    
    # Find missing
    missing_safe = needed_safe_names - downloaded_safe_names
    missing = set()
    for name in unique_base_names:
        safe_name = name.lower().replace(" ", "_").replace("/", "_").replace(":", "").replace("?", "").replace("&", "").replace("-", "_").replace("'", "")
        safe_name = safe_name.replace("___", "_")
        if safe_name in missing_safe:
            missing.add(name)
    
    display_name = "P3R Episode Aigis" if game_id == "p3r_aigis" else game_id.upper()
    print(f"\n{display_name}:")
    print(f"  Total entries in JSON: {len(names)}")
    print(f"  Unique base enemies: {len(unique_base_names)}")
    print(f"  Downloaded: {len(unique_base_names) - len(missing)}")
    print(f"  Missing: {len(missing)}")
    
    if missing and len(missing) <= 10:
        print(f"  Missing enemies:")
        for name in sorted(missing):
            print(f"    - {name}")
    elif missing:
        print(f"  Missing enemies (first 10):")
        for name in sorted(missing)[:10]:
            print(f"    - {name}")
        print(f"    ... and {len(missing) - 10} more")
    
    total_needed += len(unique_base_names)
    total_downloaded += (len(unique_base_names) - len(missing))
    total_missing += len(missing)

print("\n" + "=" * 70)
print("OVERALL SUMMARY")
print("=" * 70)
print(f"Total unique enemies needed: {total_needed}")
print(f"Total downloaded: {total_downloaded}")
print(f"Total missing: {total_missing}")
print(f"Success rate: {(total_downloaded/total_needed*100):.1f}%")
