import json

# Load boss data
with open('p5s-bosses.json', 'r', encoding='utf-8') as f:
    bosses = json.load(f)

# Load mini-boss data
with open('p5s-minibosses.json', 'r', encoding='utf-8') as f:
    minibosses = json.load(f)

# P5S uses same elements as P5/P5R: Physical, Gun, Fire, Ice, Elec, Wind, Psy, Nuke, Bless, Curse
elements = ["Physical", "Gun", "Fire", "Ice", "Elec", "Wind", "Psy", "Nuke", "Bless", "Curse"]

def convert_resists_to_string(resists_array):
    """Convert resistance array to string format"""
    mapping = {
        '-': '-',
        'wk': 'w',
        'rs': 'r',
        'nu': 'n',
        'dr': 'd',
        'rp': 'p',
        'ab': 'a'
    }
    return ''.join(mapping.get(r, '-') for r in resists_array)

def process_enemy(name, data, is_boss, is_mini_boss):
    """Convert enemy data to app format"""
    enemy = {
        "name": name,
        "arcana": data.get("arcana", "Unknown"),
        "level": data.get("level", 1),
        "hp": data.get("hp", 0),
        "sp": data.get("sp", 0),
        "exp": 0,  # P5S doesn't have exp in the data
        "resists": convert_resists_to_string(data.get("resists", ["-"] * 10)),
        "skills": data.get("skills", []),
        "area": data.get("location", "Unknown"),
        "version": data.get("version", ""),
        "isBoss": is_boss,
        "isMiniBoss": is_mini_boss,
        "drops": None
    }
    
    # Add stats if available
    if "stats" in data:
        enemy["stats"] = {
            "strength": data["stats"].get("strength", 0),
            "magic": data["stats"].get("magic", 0),
            "endurance": data["stats"].get("endurance", 0),
            "agility": data["stats"].get("agility", 0),
            "luck": data["stats"].get("luck", 0)
        }
    
    # Handle multi-phase bosses
    if "phases" in data:
        phases = []
        for phase_name, phase_data in data["phases"].items():
            phase = {
                "name": phase_name,
                "hp": phase_data.get("hp", 0),
                "sp": phase_data.get("sp", 0),
                "resists": convert_resists_to_string(phase_data.get("resists", ["-"] * 10)),
                "skills": phase_data.get("skills", [])
            }
            
            # Handle parts within phases
            if "parts" in phase_data:
                parts = []
                for part_name, part_data in phase_data["parts"].items():
                    part = {
                        "name": part_name,
                        "hp": part_data.get("hp", 0),
                        "resists": convert_resists_to_string(part_data.get("resists", ["-"] * 10)),
                        "skills": part_data.get("skills", [])
                    }
                    if "sp" in part_data:
                        part["sp"] = part_data["sp"]
                    parts.append(part)
                phase["parts"] = parts
            
            phases.append(phase)
        enemy["phases"] = phases
    
    return enemy

# Process all enemies
all_enemies = []

# Main bosses (6 total)
for name, data in bosses.items():
    all_enemies.append(process_enemy(name, data, is_boss=True, is_mini_boss=False))

# Mini-bosses (25 total)
for name, data in minibosses.items():
    all_enemies.append(process_enemy(name, data, is_boss=False, is_mini_boss=True))

# Sort by level
all_enemies.sort(key=lambda x: x["level"])

# Save to output file
with open('p5s_enemies.json', 'w', encoding='utf-8') as f:
    json.dump(all_enemies, f, indent=2, ensure_ascii=False)

print(f"✓ Processed {len(all_enemies)} P5S enemies")
print(f"  - Main Bosses: {sum(1 for e in all_enemies if e['isBoss'])}")
print(f"  - Mini Bosses: {sum(1 for e in all_enemies if e['isMiniBoss'])}")
