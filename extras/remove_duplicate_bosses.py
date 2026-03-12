import json
from pathlib import Path
import shutil

print("=" * 70)
print("REMOVING DUPLICATE BOSS ENTRIES")
print("=" * 70)

games = [
    ("p3fes", "app/src/main/assets/data/enemies/p3fes_enemies.json"),
    ("p4", "app/src/main/assets/data/enemies/p4_enemies.json"),
    ("p4g", "app/src/main/assets/data/enemies/p4g_enemies.json"),
]

# Known intentional duplicates to keep
intentional_dupes = {
    "Change Relic",
    "Death Castle",
    "Clairvoyant Relic"
}

for game_id, json_path in games:
    print(f"\n{game_id.upper()}:")
    
    # Backup original
    backup_path = json_path.replace('.json', '_before_dedup.json')
    shutil.copy(json_path, backup_path)
    print(f"  Backed up to: {backup_path}")
    
    with open(json_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    if isinstance(data, list):
        enemies = data
    else:
        enemies = [{"name": name, **props} for name, props in data.items()]
    
    # Find duplicates
    names = [e.get('name', '') for e in enemies]
    dupes = [n for n in set(names) if names.count(n) > 1 and n not in intentional_dupes]
    
    if not dupes:
        print(f"  No duplicates to remove")
        continue
    
    print(f"  Found {len(dupes)} duplicate names")
    
    # For each duplicate, keep only the boss version (or the first if neither is boss)
    seen = {}
    to_remove = []
    
    for i, enemy in enumerate(enemies):
        name = enemy.get('name', '')
        
        if name not in dupes:
            continue
        
        is_boss = enemy.get('isBoss', False) or enemy.get('isMiniBoss', False)
        
        if name not in seen:
            # First occurrence
            seen[name] = {'index': i, 'is_boss': is_boss}
        else:
            # Duplicate found
            prev_index = seen[name]['index']
            prev_is_boss = seen[name]['is_boss']
            
            if is_boss and not prev_is_boss:
                # Current is boss, previous is not - remove previous, keep current
                to_remove.append(prev_index)
                seen[name] = {'index': i, 'is_boss': is_boss}
                print(f"    {name}: Keeping boss version (index {i}), removing regular (index {prev_index})")
            elif not is_boss and prev_is_boss:
                # Previous is boss, current is not - remove current
                to_remove.append(i)
                print(f"    {name}: Keeping boss version (index {prev_index}), removing regular (index {i})")
            else:
                # Both are boss or both are regular - keep first, remove current
                to_remove.append(i)
                print(f"    {name}: Keeping first occurrence (index {prev_index}), removing duplicate (index {i})")
    
    # Remove duplicates (in reverse order to maintain indices)
    for index in sorted(to_remove, reverse=True):
        del enemies[index]
    
    print(f"  Removed {len(to_remove)} duplicate entries")
    print(f"  New total: {len(enemies)} enemies")
    
    # Save cleaned data
    with open(json_path, 'w', encoding='utf-8') as f:
        json.dump(enemies, f, indent=2, ensure_ascii=False)
    
    print(f"  ✓ Saved cleaned data")

print("\n" + "=" * 70)
print("COMPLETE - Run check_all_duplicates.py to verify")
print("=" * 70)
