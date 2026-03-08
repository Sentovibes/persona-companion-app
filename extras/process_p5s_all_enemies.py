import json

# P5S demon data (regular enemies/personas that can be encountered)
p5s_demons = """PASTE_DEMON_DATA_HERE"""

# Load existing boss/miniboss data
with open('p5s_enemies.json', 'r', encoding='utf-8') as f:
    existing_enemies = json.load(f)

# Parse the demon data
demons = json.loads(p5s_demons)

def convert_resists(resist_str):
    """Convert resist string format"""
    # s=strong/resist, n=null, w=weak, d=drain/absorb
    mapping = {'s': 'r', 'n': 'n', 'w': 'w', 'd': 'd', '-': '-'}
    return ''.join(mapping.get(c, '-') for c in resist_str)

# Process regular enemies (non-boss demons)
regular_enemies = []
for name, data in demons.items():
    # Skip treasure demons
    if data.get('race') == 'Treasure':
        continue
    
    # Skip if already in boss/miniboss list
    if any(e['name'] == name for e in existing_enemies):
        continue
    
    enemy = {
        "name": name,
        "arcana": data.get('race', 'Unknown'),
        "level": data.get('lvl', 1),
        "hp": 0,  # Not in demon data
        "sp": 0,  # Not in demon data
        "exp": 0,
        "resists": convert_resists(data.get('resists', '----------')),
        "skills": list(data.get('skills', {}).keys()),
        "area": data.get('location', 'Unknown'),
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

# Save
with open('p5s_enemies_complete.json', 'w', encoding='utf-8') as f:
    json.dump(all_enemies, f, indent=2, ensure_ascii=False)

print(f"✓ Processed {len(all_enemies)} total P5S enemies")
print(f"  - Main Bosses: {sum(1 for e in all_enemies if e['isBoss'])}")
print(f"  - Mini Bosses: {sum(1 for e in all_enemies if e['isMiniBoss'])}")
print(f"  - Regular Enemies: {sum(1 for e in all_enemies if not e['isBoss'] and not e['isMiniBoss'])}")
