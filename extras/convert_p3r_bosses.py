#!/usr/bin/env python3
"""
Convert P3R boss data from new format to app format and merge with existing enemies
"""
import json
from pathlib import Path

def convert_resists(resists_array):
    """Convert resist array to string format"""
    # Map: wk=weak, rs=resist, nu=null, rp=repel, ab=absorb, -=normal
    mapping = {
        'wk': 'w',
        'rs': 'r',
        'nu': 'n',
        'rp': 'p',
        'ab': 'a',
        '-': '-'
    }
    return ''.join(mapping.get(r, '-') for r in resists_array)

def convert_boss(boss):
    """Convert a boss from new format to app format"""
    location_str = f"{boss['location']['block']} {boss['location']['floor']}"
    if boss['location']['area'] != "Tartarus":
        location_str = f"{boss['location']['area']} - {location_str}"
    
    # Handle bosses with phases
    phases_list = None
    if 'phases' in boss and boss['phases']:
        # Convert phases dict to list
        phases_list = []
        for phase_name, phase_data in boss['phases'].items():
            phases_list.append({
                "name": phase_name,
                "hp": phase_data.get('hp', 0),
                "sp": phase_data.get('sp', 999),
                "resists": convert_resists(phase_data.get('resists', ['-'] * 9)),
                "skills": phase_data.get('skills', [])
            })
        
        # Use first phase for main stats
        first_phase = list(boss['phases'].values())[0]
        hp = first_phase.get('hp', 0)
        sp = first_phase.get('sp', 999)
        resists = convert_resists(first_phase.get('resists', ['-'] * 9))
        skills = first_phase.get('skills', [])
    else:
        hp = boss.get('hp', 0)
        sp = boss.get('sp', 999)
        resists = convert_resists(boss.get('resists', ['-'] * 9))
        skills = boss.get('skills', [])
    
    converted = {
        "name": boss['name'],
        "arcana": boss['arcana'],
        "level": boss['level'],
        "hp": hp,
        "sp": sp,
        "stats": {
            "strength": 0,
            "magic": 0,
            "endurance": 0,
            "agility": 0,
            "luck": 0
        },
        "resists": resists,
        "skills": skills,
        "area": location_str,
        "exp": 0,
        "drops": {
            "gem": "-",
            "item": "-"
        },
        "isBoss": boss['type'] in ['Full Moon Boss', 'Story Boss', 'Super Boss'],
        "isMiniBoss": boss['type'] in ['Floor Boss', 'Monad Boss', 'Abyss Boss']
    }
    
    # Add phases if present
    if phases_list:
        converted["phases"] = phases_list
    
    return converted

def merge_bosses():
    """Merge new boss data with existing P3R enemies"""
    
    # Load existing P3R enemies
    existing_path = Path("app/src/main/assets/data/enemies/p3r_enemies.json")
    with open(existing_path, 'r', encoding='utf-8') as f:
        existing_enemies = json.load(f)
    
    # Load new mini bosses
    mini_bosses_path = Path("extras/persona3reload-mini-bosses.json")
    with open(mini_bosses_path, 'r', encoding='utf-8') as f:
        mini_bosses_data = json.load(f)
    
    # Load new main bosses
    main_bosses_path = Path("extras/persona3reload-bosses.json")
    with open(main_bosses_path, 'r', encoding='utf-8') as f:
        main_bosses_data = json.load(f)
    
    # Create a dict of existing enemies by name for easy lookup
    existing_dict = {enemy['name']: enemy for enemy in existing_enemies}
    
    # Convert and add mini bosses
    for boss in mini_bosses_data['bosses']:
        converted = convert_boss(boss)
        # Replace if exists, otherwise add
        existing_dict[converted['name']] = converted
    
    # Convert and add main bosses
    for boss in main_bosses_data['bosses']:
        # Skip bosses with parts for now (need special handling)
        if 'parts' in boss:
            print(f"Skipping {boss['name']} (has parts - needs special handling)")
            continue
        
        converted = convert_boss(boss)
        existing_dict[converted['name']] = converted
    
    # Convert back to list and sort by level
    merged_enemies = list(existing_dict.values())
    merged_enemies.sort(key=lambda x: x['level'])
    
    # Save merged data
    output_path = Path("app/src/main/assets/data/enemies/p3r_enemies_merged.json")
    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(merged_enemies, f, indent=2, ensure_ascii=False)
    
    print(f"Merged {len(merged_enemies)} total enemies")
    print(f"Saved to: {output_path}")
    
    # Backup and replace
    import shutil
    backup_path = Path("app/src/main/assets/data/enemies/p3r_enemies_before_bosses.json")
    shutil.copy2(existing_path, backup_path)
    print(f"Backed up to: {backup_path}")
    
    shutil.copy2(output_path, existing_path)
    print(f"Replaced {existing_path}")
    
    # Count boss types
    bosses = sum(1 for e in merged_enemies if e['isBoss'])
    mini_bosses = sum(1 for e in merged_enemies if e['isMiniBoss'])
    regular = len(merged_enemies) - bosses - mini_bosses
    
    print(f"\nBreakdown:")
    print(f"  Main Bosses: {bosses}")
    print(f"  Mini Bosses: {mini_bosses}")
    print(f"  Regular Enemies: {regular}")

if __name__ == '__main__':
    merge_bosses()
