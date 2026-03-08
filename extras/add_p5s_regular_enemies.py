import json

# Instructions: Paste the P5S demon JSON data between the triple quotes below
P5S_DEMON_DATA = """
PASTE_YOUR_JSON_HERE
"""

# Load existing boss/miniboss data
with open('p5s_enemies.json', 'r', encoding='utf-8') as f:
    existing_enemies = json.load(f)

# Parse demon data
try:
    demons = json.loads(P5S_DEMON_DATA)
except:
    print("ERROR: Please paste the demon JSON data in the script")
    exit(1)

def convert_resists(resist_str):
    """Convert P5S resist format to app format"""
    # s=resist, n=null, w=weak, d=drain, -=normal
    mapping = {'s': 'r', 'n': 'n', 'w': 'w', 'd': 'd', '-': '-'}
    return ''.join(mapping.get(c, '-') for c in resist_str)

# Process regular enemies
regular_enemies = []
skipped_treasure = 0
skipped_existing = 0

for name, data in demons.items():
    # Skip treasure demons
    if data.get('race') == 'Treasure':
        skipped_treasure += 1
        continue
    
    # Skip if already in boss/miniboss list
    if any(e['name'] == name for e in existing_enemies):
        skipped_existing += 1
        continue
    
    enemy = {
        "name": name,
        "arcana": data.get('race', 'Unknown'),
        "level": data.get('lvl', 1),
        "hp": 0,  # Not available in demon data
        "sp": 0,  # Not available in demon data
        "exp": 0,
        "resists": convert_resists(data.get('resists', '----------')),
        "skills": list(data.get('skills', {}).keys()),
        "area": data.get('location', 'Various'),
        "version": "",
        "isBoss": False,
        "isMiniBoss": False,
        "drops": None
    }
    
    # Add stats if available
    if 'stats' in data and len(data['stats']) >= 5:
        enemy["stats"] = {
            "strength": data['stats'][0],
            "magic": data['stats'][1],
            "endurance": data['stats'][2],
            "agility": data['stats'][3],
            "luck": data['stats'][4]
        }
    
    regular_enemies.append(enemy)

# Combine all enemies
all_enemies = existing_enemies + regular_enemies

# Sort by level
all_enemies.sort(key=lambda x: x['level'])

# Save to new file
with open('p5s_enemies_complete.json', 'w', encoding='utf-8') as f:
    json.dump(all_enemies, f, indent=2, ensure_ascii=False)

print(f"✓ Processed {len(all_enemies)} total P5S enemies")
print(f"  - Main Bosses: {sum(1 for e in all_enemies if e['isBoss'])}")
print(f"  - Mini Bosses: {sum(1 for e in all_enemies if e['isMiniBoss'])}")
print(f"  - Regular Enemies: {len(regular_enemies)}")
print(f"\nSkipped:")
print(f"  - Treasure Demons: {skipped_treasure}")
print(f"  - Already in boss list: {skipped_existing}")
