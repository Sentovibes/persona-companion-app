#!/usr/bin/env python3
"""
Convert P3R enemy data from megaten-fusion-tool format to app format
"""
import json
from pathlib import Path
import shutil

def convert_enemies(input_path, output_path):
    """Convert enemies from old format to new format"""
    
    # Load old format
    with open(input_path, 'r', encoding='utf-8') as f:
        old_data = json.load(f)
    
    # Convert to new format
    enemies = []
    for name, enemy in old_data.items():
        # Parse stats array [hp, sp, str, mag, end, agi, luk]
        stats_array = enemy.get('stats', [0, 0, 0, 0, 0, 0, 0])
        
        converted = {
            "name": name,
            "arcana": enemy.get('race', 'Unknown'),
            "level": enemy.get('lvl', 1),
            "hp": stats_array[0] if len(stats_array) > 0 else 0,
            "sp": stats_array[1] if len(stats_array) > 1 else 0,
            "stats": {
                "strength": stats_array[2] if len(stats_array) > 2 else 0,
                "magic": stats_array[3] if len(stats_array) > 3 else 0,
                "endurance": stats_array[4] if len(stats_array) > 4 else 0,
                "agility": stats_array[5] if len(stats_array) > 5 else 0,
                "luck": stats_array[6] if len(stats_array) > 6 else 0
            },
            "resists": enemy.get('resists', '-'),
            "skills": enemy.get('skills', []),
            "area": enemy.get('area', 'Unknown'),
            "exp": enemy.get('exp', 0),
            "drops": {
                "gem": "-",
                "item": "-"
            }
        }
        
        # Parse drops from dodds field
        dodds = enemy.get('dodds', {})
        if dodds:
            items = list(dodds.keys())
            if len(items) > 0:
                converted["drops"]["item"] = items[0]
            if len(items) > 1:
                converted["drops"]["gem"] = items[1]
        
        # Determine if boss/mini-boss based on name or area
        name_lower = name.lower()
        area = enemy.get('area', '').lower()
        
        is_boss = any(x in name_lower for x in ['nyx', 'reaper', 'elizabeth', 'margaret', 'theodore', 'erebus'])
        is_mini_boss = any(x in area for x in ['mini', 'boss']) or 'P' in enemy.get('race', '')
        
        converted["isBoss"] = is_boss
        converted["isMiniBoss"] = is_mini_boss and not is_boss
        
        enemies.append(converted)
    
    # Sort by level
    enemies.sort(key=lambda x: x['level'])
    
    # Save converted data
    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(enemies, f, indent=2, ensure_ascii=False)
    
    return len(enemies)

def convert_p3r_enemies():
    """Convert P3R and Aigis enemies to app format"""
    
    # Convert P3R base enemies
    p3r_input = Path("app/src/main/assets/data/enemies/p3r_enemies.json")
    p3r_output = Path("app/src/main/assets/data/enemies/p3r_enemies_converted.json")
    
    # Backup old file
    p3r_backup = Path("app/src/main/assets/data/enemies/p3r_enemies_old.json")
    shutil.copy2(p3r_input, p3r_backup)
    print(f"Backed up P3R enemies to: {p3r_backup}")
    
    count = convert_enemies(p3r_input, p3r_output)
    print(f"Converted {count} P3R enemies")
    
    # Replace old file with converted
    shutil.copy2(p3r_output, p3r_input)
    print(f"Replaced {p3r_input} with converted data")
    
    # Convert P3R Aigis enemies
    aigis_input = Path("app/src/main/assets/data/enemies/p3r_episode_aigis/enemies.json")
    aigis_output = Path("app/src/main/assets/data/enemies/p3r_episode_aigis/enemies_converted.json")
    
    if aigis_input.exists():
        # Backup old file
        aigis_backup = Path("app/src/main/assets/data/enemies/p3r_episode_aigis/enemies_old.json")
        shutil.copy2(aigis_input, aigis_backup)
        print(f"\nBacked up Aigis enemies to: {aigis_backup}")
        
        count = convert_enemies(aigis_input, aigis_output)
        print(f"Converted {count} Aigis enemies")
        
        # Replace old file with converted
        shutil.copy2(aigis_output, aigis_input)
        print(f"Replaced {aigis_input} with converted data")
    else:
        print(f"\nAigis enemy file not found: {aigis_input}")

if __name__ == '__main__':
    convert_p3r_enemies()
