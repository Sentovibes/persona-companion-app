import json
from pathlib import Path
import re

print("=" * 70)
print("ENEMY IMAGE DOWNLOAD SUMMARY")
print("=" * 70)

games = [
    ("p3fes", "app/src/main/assets/data/enemies/p3fes_enemies.json"),
    ("p3p", "app/src/main/assets/data/enemies/p3p_enemies.json"),
    ("p3r", "app/src/main/assets/data/enemies/p3r_enemies.json"),
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
    
    # Check downloaded folder
    download_folder = Path(f"extras/downloaded_enemies/{game_id}")
    downloaded_safe_names = set()
    if download_folder.exists():
        for img in download_folder.glob("*.*"):
            downloaded_safe_names.add(img.stem)
    
    # Convert unique base names to safe names for comparison
    needed_safe_names = set()
    name_to_safe_map = {}  # Track original name to safe name mapping
    for name in unique_base_names:
        clean_name = name
        
        # Special handling for compound boss names (use first part only)
        if " & " in clean_name:
            clean_name = clean_name.split(" & ")[0]
        
        # Special handling for boss names with full names (ONLY for P3R)
        if game_id == "p3r":
            boss_name_map = {
                "chidori yoshino": "chidori",
                "jin shirato": "jin",
                "takaya sakaki": "takaya"
            }
            
            lower_name = clean_name.lower()
            if lower_name in boss_name_map:
                clean_name = boss_name_map[lower_name]
        
        safe_name = clean_name.lower().replace(" ", "_").replace("/", "_").replace(":", "").replace("?", "").replace("&", "").replace("-", "_").replace("'", "")
        # Handle triple underscores from & conversion
        safe_name = safe_name.replace("___", "_")
        needed_safe_names.add(safe_name)
        name_to_safe_map[name] = safe_name
    
    # Find missing - check if either the split name OR compound name exists
    missing_safe = needed_safe_names - downloaded_safe_names
    missing = set()
    for name in unique_base_names:
        safe_name = name_to_safe_map[name]
        
        # For compound names, also check if the full compound name exists (with __ or ___)
        if " & " in name:
            compound_safe_double = name.lower().replace(" ", "_").replace("&", "_").replace("___", "__").replace("/", "_").replace(":", "").replace("?", "").replace("-", "_").replace("'", "")
            compound_safe_triple = name.lower().replace(" ", "_").replace("&", "__").replace("___", "___").replace("/", "_").replace(":", "").replace("?", "").replace("-", "_").replace("'", "")
            # If either the split name OR compound name exists, it's not missing
            if safe_name not in downloaded_safe_names and compound_safe_double not in downloaded_safe_names and compound_safe_triple not in downloaded_safe_names:
                missing.add(name)
        elif safe_name in missing_safe:
            missing.add(name)
    
    print(f"\n{game_id.upper()}:")
    print(f"  Total entries in JSON: {len(names)}")
    print(f"  Unique base enemies: {len(unique_base_names)}")
    print(f"  Downloaded: {len(downloaded_safe_names)}")
    print(f"  Missing: {len(missing)}")
    
    if missing and len(missing) <= 20:
        print(f"  Missing enemies:")
        for name in sorted(missing)[:20]:
            print(f"    - {name}")
    elif missing:
        print(f"  Missing enemies (first 20):")
        for name in sorted(missing)[:20]:
            print(f"    - {name}")
        print(f"    ... and {len(missing) - 20} more")
    
    total_needed += len(unique_base_names)
    total_downloaded += len(downloaded_safe_names)
    total_missing += len(missing)

print("\n" + "=" * 70)
print("OVERALL SUMMARY")
print("=" * 70)
print(f"Total unique enemies needed: {total_needed}")
print(f"Total downloaded: {total_downloaded}")
print(f"Total missing: {total_missing}")
coverage_rate = ((total_needed - total_missing) / total_needed * 100)
print(f"Coverage rate: {coverage_rate:.1f}%")
print(f"Missing rate: {(total_missing / total_needed * 100):.1f}%")
