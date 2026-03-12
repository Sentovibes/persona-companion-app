import json
from pathlib import Path
from collections import defaultdict

print("=" * 70)
print("CLEANING DUPLICATE MINI-BOSS ENTRIES")
print("=" * 70)

def enemy_signature(enemy):
    """Create a unique signature for an enemy to detect exact duplicates"""
    return (
        enemy.get('name', ''),
        enemy.get('level', 0),
        enemy.get('hp', 0),
        enemy.get('sp', 0),
        enemy.get('area', ''),
        enemy.get('arcana', ''),
        str(enemy.get('skills', [])),
        str(enemy.get('resists', '')),
        enemy.get('isMiniBoss', False),
        enemy.get('isBoss', False)
    )

for json_file in sorted(Path('../app/src/main/assets/data/enemies').glob('*_enemies.json')):
    game_name = json_file.stem.replace('_enemies', '').upper()
    
    with open(json_file, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    if not isinstance(data, list):
        print(f"\n{game_name}: Skipping (not a list format)")
        continue
    
    original_count = len(data)
    
    # Track seen enemies by their signature
    seen_signatures = set()
    seen_names = defaultdict(list)
    cleaned_data = []
    duplicates_removed = []
    
    for enemy in data:
        sig = enemy_signature(enemy)
        name = enemy.get('name', '')
        
        # Track all entries with this name
        seen_names[name].append(enemy)
        
        # Check if this exact enemy already exists
        if sig in seen_signatures:
            duplicates_removed.append(enemy)
            print(f"\n{game_name}: Removing exact duplicate:")
            print(f"  Name: {name}")
            print(f"  Level: {enemy.get('level')}, HP: {enemy.get('hp')}, Area: {enemy.get('area')}")
            print(f"  isMiniBoss: {enemy.get('isMiniBoss')}, isBoss: {enemy.get('isBoss')}")
        else:
            seen_signatures.add(sig)
            cleaned_data.append(enemy)
    
    # Report on enemies with same name but different stats (keep these)
    for name, entries in seen_names.items():
        if len(entries) > 1:
            unique_sigs = set(enemy_signature(e) for e in entries)
            if len(unique_sigs) == len(entries):
                # All entries are unique (different stats/areas)
                mini_boss_count = sum(1 for e in entries if e.get('isMiniBoss'))
                regular_count = len(entries) - mini_boss_count
                if mini_boss_count > 0 and regular_count > 0:
                    print(f"\n{game_name}: Keeping {name} ({mini_boss_count} mini-boss + {regular_count} regular)")
    
    new_count = len(cleaned_data)
    removed_count = original_count - new_count
    
    if removed_count > 0:
        # Backup original file
        backup_file = json_file.with_suffix('.json.backup')
        with open(backup_file, 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
        print(f"\n{game_name}: Backed up to {backup_file.name}")
        
        # Write cleaned data
        with open(json_file, 'w', encoding='utf-8') as f:
            json.dump(cleaned_data, f, indent=2, ensure_ascii=False)
        
        print(f"{game_name}: Removed {removed_count} exact duplicates ({original_count} -> {new_count})")
    else:
        print(f"\n{game_name}: No exact duplicates found ({original_count} entries)")

print("\n" + "=" * 70)
print("CLEANUP COMPLETE")
print("=" * 70)
print("\nOriginal files backed up with .backup extension")
print("Run the batch downloader now!")
