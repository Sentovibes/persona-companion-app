import json
from pathlib import Path
from collections import Counter

print("=" * 70)
print("CHECKING FOR DUPLICATE ENEMIES WITHIN EACH GAME")
print("=" * 70)

total_duplicates = 0

for f in sorted(Path('../app/src/main/assets/data/enemies').glob('*_enemies.json')):
    game_name = f.stem.replace('_enemies', '').upper()
    data = json.load(open(f, 'r', encoding='utf-8'))
    
    if isinstance(data, list):
        names = [enemy.get("name", "") for enemy in data if isinstance(enemy, dict) and enemy.get("name")]
    else:
        names = [name for name in data.keys() if name]
    
    # Count occurrences
    name_counts = Counter(names)
    duplicates = {name: count for name, count in name_counts.items() if count > 1}
    
    if duplicates:
        print(f"\n{game_name}: {len(names)} total entries, {len(name_counts)} unique")
        print(f"  Duplicates: {len(duplicates)} enemies appear multiple times")
        print(f"  Total duplicate entries: {sum(count - 1 for count in duplicates.values())}")
        
        # Show top duplicates
        top_dupes = sorted(duplicates.items(), key=lambda x: x[1], reverse=True)[:10]
        for name, count in top_dupes:
            print(f"    {name}: appears {count} times")
        
        if len(duplicates) > 10:
            print(f"    ... and {len(duplicates) - 10} more duplicates")
        
        total_duplicates += sum(count - 1 for count in duplicates.values())
    else:
        print(f"\n{game_name}: {len(names)} entries, all unique ✓")

print("\n" + "=" * 70)
print("SUMMARY")
print("=" * 70)
print(f"Total duplicate entries across all games: {total_duplicates}")
print(f"These are the same enemy repeated in the same game's JSON file")
