#!/usr/bin/env python3
"""
Remove all P3R bosses and mini-bosses, then add new ones with phases/parts
"""
import json
from pathlib import Path
import shutil

def convert_resists(resists_array):
    """Convert resist array to string format"""
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
    
    # Handle bosses with parts
    parts_list = None
    if 'parts' in boss and boss['parts']:
        parts_list = []
        for part_name, part_data in boss['parts'].items():
            parts_list.append({
                "name": part_name,
                "hp": part_data.get('hp', 0),
                "sp": part_data.get('sp', 999),
                "resists": convert_resists(part_data.get('resists', ['-'] * 9)),
                "skills": part_data.get('skills', [])
            })
        
        # Use first part for main stats
        first_part = list(boss['parts'].values())[0]
        hp = first_part.get('hp', 0)
        sp = first_part.get('sp', 999)
        resists = convert_resists(first_part.get('resists', ['-'] * 9))
        skills = first_part.get('skills', [])
    
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
        "isBoss": boss['type'] in ['Full Moon Boss', 'Story Boss', 'Super Boss', 'Final Boss', 'Secret Boss'],
        "isMiniBoss": boss['type'] in ['Floor Boss', 'Monad Boss', 'Abyss Boss']
    }
    
    # Add phases if present
    if phases_list:
        converted["phases"] = phases_list
    
    # Add parts if present
    if parts_list:
        converted["parts"] = parts_list
    
    return converted

def replace_bosses():
    """Remove all bosses/mini-bosses and add new ones"""
    
    # Load existing P3R enemies (use base version with only regular enemies)
    existing_path = Path("app/src/main/assets/data/enemies/p3r_enemies_base.json")
    if not existing_path.exists():
        existing_path = Path("app/src/main/assets/data/enemies/p3r_enemies.json")
    
    with open(existing_path, 'r', encoding='utf-8') as f:
        existing_enemies = json.load(f)
    
    # Keep only regular enemies (not bosses or mini-bosses)
    regular_enemies = []
    removed_count = 0
    for enemy in existing_enemies:
        if not enemy.get('isBoss', False) and not enemy.get('isMiniBoss', False):
            regular_enemies.append(enemy)
        else:
            removed_count += 1
            print(f"Removing: {enemy['name']}")
    
    print(f"\nRemoved {removed_count} bosses/mini-bosses")
    print(f"Kept {len(regular_enemies)} regular enemies")
    
    # Load new mini bosses
    mini_bosses_path = Path("extras/persona3reload-mini-bosses.json")
    with open(mini_bosses_path, 'r', encoding='utf-8') as f:
        mini_bosses_data = json.load(f)
    
    # Load new main bosses
    main_bosses_path = Path("extras/persona3reload-bosses.json")
    with open(main_bosses_path, 'r', encoding='utf-8') as f:
        main_bosses_data = json.load(f)
    
    # Convert and add all new bosses
    new_bosses = []
    
    print("\nAdding mini bosses:")
    for boss in mini_bosses_data['bosses']:
        converted = convert_boss(boss)
        new_bosses.append(converted)
        print(f"  + {converted['name']}")
    
    print("\nAdding main bosses:")
    for boss in main_bosses_data['bosses']:
        converted = convert_boss(boss)
        new_bosses.append(converted)
        phases_info = f" ({len(converted.get('phases', []))} phases)" if 'phases' in converted else ""
        parts_info = f" ({len(converted.get('parts', []))} parts)" if 'parts' in converted else ""
        print(f"  + {converted['name']}{phases_info}{parts_info}")
    
    # Combine regular enemies with new bosses
    all_enemies = regular_enemies + new_bosses
    all_enemies.sort(key=lambda x: x['level'])
    
    # Save
    output_path = Path("app/src/main/assets/data/enemies/p3r_enemies_new.json")
    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(all_enemies, f, indent=2, ensure_ascii=False)
    
    print(f"\n=== Summary ===")
    print(f"Total enemies: {len(all_enemies)}")
    print(f"  Regular: {len(regular_enemies)}")
    print(f"  Mini Bosses: {sum(1 for e in all_enemies if e.get('isMiniBoss', False))}")
    print(f"  Main Bosses: {sum(1 for e in all_enemies if e.get('isBoss', False))}")
    print(f"\nSaved to: {output_path}")
    
    # Backup and replace
    backup_path = Path("app/src/main/assets/data/enemies/p3r_enemies_backup.json")
    shutil.copy2(existing_path, backup_path)
    print(f"Backed up to: {backup_path}")
    
    shutil.copy2(output_path, existing_path)
    print(f"Replaced {existing_path}")

if __name__ == '__main__':
    replace_bosses()
