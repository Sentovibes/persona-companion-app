#!/usr/bin/env python3
"""
Extract P3R enemy data from megaten-fusion-tool.
Includes main game and Episode Aigis enemies.
"""

import json
from pathlib import Path

# Source paths
MEGATEN_ROOT = Path(r"C:\Users\omare\Downloads\megaten-fusion-tool-master\megaten-fusion-tool-master")

def convert_resists(resists_obj):
    """Convert resistance object to string format."""
    if isinstance(resists_obj, str):
        return resists_obj
    
    resist_map = {
        "wk": "w",
        "rs": "r",
        "nu": "n",
        "dr": "d",
        "rp": "d",
        "ab": "d",
        "-": "-"
    }
    
    # P3R element order
    elements = ["phys", "fire", "ice", "elec", "wind", "light", "dark"]
    result = ""
    
    for elem in elements:
        val = resists_obj.get(elem, "-")
        result += resist_map.get(val, "-")
    
    return result

def convert_enemy(name, data, is_subboss=False):
    """Convert megaten-fusion-tool enemy format to our format."""
    # Handle stats - P3R uses array format [hp, sp, st, ma, en, ag, lu]
    stats_data = data.get("stats", [])
    if isinstance(stats_data, list) and len(stats_data) >= 7:
        hp = stats_data[0]
        sp = stats_data[1]
        strength = stats_data[2]
        magic = stats_data[3]
        endurance = stats_data[4]
        agility = stats_data[5]
        luck = stats_data[6]
    else:
        hp = data.get("hp", 0)
        sp = data.get("mp", 0)
        strength = magic = endurance = agility = luck = 0
    
    return {
        "name": name,
        "arcana": data.get("race", "Unknown"),
        "level": data.get("lvl", 0),
        "hp": hp,
        "sp": sp,
        "stats": {
            "strength": strength,
            "magic": magic,
            "endurance": endurance,
            "agility": agility,
            "luck": luck
        },
        "resists": convert_resists(data.get("resists", {})),
        "skills": data.get("skills", []),
        "area": data.get("area", "Unknown"),
        "exp": data.get("exp", 0),
        "drops": {
            "gem": "-",
            "item": data.get("drop", "-")
        },
        "isBoss": False,
        "isMiniBoss": is_subboss
    }

def extract_p3r_enemies():
    """Extract P3R enemy data."""
    print("Extracting P3R enemy data from megaten-fusion-tool...")
    
    # Load main game enemies
    main_enemy_file = MEGATEN_ROOT / "src/app/p3r/data/enemy-data.json"
    main_enemies = []
    if main_enemy_file.exists():
        enemy_data = json.loads(main_enemy_file.read_text(encoding='utf-8'))
        for name, data in enemy_data.items():
            main_enemies.append(convert_enemy(name, data))
        print(f"  Loaded {len(main_enemies)} main game enemies")
    
    # Load Episode Aigis enemies
    aeg_enemy_file = MEGATEN_ROOT / "src/app/p3r/data/aeg-enemy-data.json"
    aeg_enemies = []
    if aeg_enemy_file.exists():
        enemy_data = json.loads(aeg_enemy_file.read_text(encoding='utf-8'))
        for name, data in enemy_data.items():
            aeg_enemies.append(convert_enemy(name, data))
        print(f"  Loaded {len(aeg_enemies)} Episode Aigis enemies")
    
    # Combine all enemies (remove duplicates by name)
    all_enemies_dict = {}
    for enemy in main_enemies + aeg_enemies:
        all_enemies_dict[enemy["name"]] = enemy
    
    all_enemies = list(all_enemies_dict.values())
    all_enemies.sort(key=lambda x: x["level"])
    
    # Write to assets
    output_path = Path("app/src/main/assets/data/enemies/p3r_enemies.json")
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(json.dumps(all_enemies, indent=2))
    
    print(f"\n  ✓ Total: {len(all_enemies)} unique enemies")
    print(f"  Written to {output_path}")

if __name__ == "__main__":
    extract_p3r_enemies()
    print("\n✓ P3R enemy data extracted!")
    print("Waiting for mini-boss and main boss data...")
