import json
from pathlib import Path

print("=" * 70)
print("CHECKING FOR DUPLICATE ENEMIES")
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

# Known intentional duplicates
intentional_dupes = {
    "Change Relic",
    "Death Castle",
    "Clairvoyant Relic"
}

total_dupes = 0

for game_id, json_path in games:
    with open(json_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    if isinstance(data, list):
        enemies = data
    else:
        enemies = [{"name": name, **props} for name, props in data.items()]
    
    names = [e.get('name', '') for e in enemies if isinstance(e, dict)]
    dupes = [n for n in set(names) if names.count(n) > 1]
    
    # Filter out intentional duplicates
    unexpected_dupes = [d for d in dupes if d not in intentional_dupes]
    
    print(f"\n{game_id.upper()}:")
    print(f"  Total enemies: {len(names)}")
    print(f"  Duplicates: {len(dupes)}")
    
    if unexpected_dupes:
        print(f"  ⚠ UNEXPECTED DUPLICATES:")
        for name in sorted(unexpected_dupes):
            count = names.count(name)
            # Check if any are bosses
            boss_entries = [e for e in enemies if e.get('name') == name]
            is_boss = any(e.get('isBoss') or e.get('isMiniBoss') for e in boss_entries)
            boss_flag = " [BOSS]" if is_boss else ""
            print(f"    - {name}: {count} times{boss_flag}")
        total_dupes += len(unexpected_dupes)
    elif dupes:
        print(f"  ✓ Only intentional duplicates:")
        for name in sorted(dupes):
            print(f"    - {name}: {names.count(name)} times")
    else:
        print(f"  ✓ No duplicates")
    
    # Check for persona_name field in P5/P5R
    if game_id in ['p5', 'p5r']:
        with_persona_name = sum(1 for e in enemies if e.get('persona_name'))
        print(f"  Enemies with persona_name: {with_persona_name}/{len(enemies)}")

print("\n" + "=" * 70)
if total_dupes > 0:
    print(f"⚠ FOUND {total_dupes} UNEXPECTED DUPLICATES ACROSS ALL GAMES")
else:
    print("✓ NO UNEXPECTED DUPLICATES - ALL CLEAN!")
print("=" * 70)
