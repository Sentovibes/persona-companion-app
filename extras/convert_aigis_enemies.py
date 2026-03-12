#!/usr/bin/env python3
"""
Convert P3R Episode Aigis enemy data to app format
"""
import json
from pathlib import Path
import shutil

def convert_aigis_enemies():
    """Convert Aigis enemies from old format to new format"""
    
    # Load old format
    input_path = Path("app/src/main/assets/data/enemies/p3r_episode_aigis/enemies.json")
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
            },
            "isBoss": False,
            "isMiniBoss": False
        }
        
        # Parse drops
        dodds = enemy.get('dodds', {})
        if dodds:
            items = list(dodds.keys())
            if len(items) > 0:
                converted["drops"]["item"] = items[0]
            if len(items) > 1:
                converted["drops"]["gem"] = items[1]
        
        enemies.append(converted)
    
    # Sort by level
    enemies.sort(key=lambda x: x['level'])
    
    # Save
    output_path = Path("app/src/main/assets/data/enemies/p3r_episode_aigis/enemies_converted.json")
    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(enemies, f, indent=2, ensure_ascii=False)
    
    print(f"Converted {len(enemies)} Aigis enemies")
    print(f"Saved to: {output_path}")
    
    # Backup and replace
    backup_path = Path("app/src/main/assets/data/enemies/p3r_episode_aigis/enemies_old.json")
    shutil.copy2(input_path, backup_path)
    print(f"Backed up to: {backup_path}")
    
    shutil.copy2(output_path, input_path)
    print(f"Replaced {input_path}")

if __name__ == '__main__':
    convert_aigis_enemies()
