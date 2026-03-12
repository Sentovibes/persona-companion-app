import json

# Load P3R enemies
with open('app/src/main/assets/data/enemies/p3r_enemies.json', 'r', encoding='utf-8') as f:
    p3r_enemies = json.load(f)

print(f"=== P3R Enemy Duplicate Check ===")
print(f"Total P3R enemies: {len(p3r_enemies)}")
print()

# Check for duplicates by name
name_counts = {}
for enemy in p3r_enemies:
    name = enemy['name']
    name_counts[name] = name_counts.get(name, 0) + 1

# Find duplicates
duplicates = {name: count for name, count in name_counts.items() if count > 1}

if duplicates:
    print(f"Found {len(duplicates)} duplicate enemy names:")
    print()
    for name, count in sorted(duplicates.items()):
        print(f"  {name}: appears {count} times")
        # Show details of each duplicate
        matching_enemies = [e for e in p3r_enemies if e['name'] == name]
        for i, enemy in enumerate(matching_enemies, 1):
            print(f"    #{i}: Level {enemy['level']}, HP {enemy['hp']}, Area: {enemy['area']}")
            print(f"        Boss: {enemy.get('isBoss', False)}, Mini-Boss: {enemy.get('isMiniBoss', False)}")
            print(f"        Episode Aigis: {enemy.get('episodeAigis', False)}")
        print()
else:
    print("No duplicate enemy names found!")

# Check for near-duplicates (same name with variant suffix like A, B, C)
print("\n=== Enemy Variants ===")
base_names = {}
for enemy in p3r_enemies:
    name = enemy['name']
    # Check if it ends with a space and single letter
    if len(name) > 2 and name[-2] == ' ' and name[-1].isalpha():
        base_name = name[:-2]
        if base_name not in base_names:
            base_names[base_name] = []
        base_names[base_name].append(name)

if base_names:
    print(f"Found {len(base_names)} enemies with variants:")
    for base, variants in sorted(base_names.items()):
        if len(variants) > 1:
            print(f"  {base}: {', '.join(sorted(variants))}")
else:
    print("No enemy variants found")
