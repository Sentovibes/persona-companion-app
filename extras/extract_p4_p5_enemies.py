#!/usr/bin/env python3
"""
Extract P4/P4G/P5/P5R enemy data from megaten-fusion-tool.
Includes regular enemies and sub-bosses ONLY (no main bosses).
"""

import json
from pathlib import Path

# Source paths
MEGATEN_ROOT = Path(r"C:\Users\omare\Downloads\megaten-fusion-tool-master\megaten-fusion-tool-master")

# Game configurations
GAMES = {
    "p4": {
        "enemy_file": MEGATEN_ROOT / "src/app/p4/data/enemy-data.json",
        "subboss_file": None  # P4 doesn't have separate subboss file
    },
    "p4g": {
        "enemy_file": MEGATEN_ROOT / "src/app/p4/data/golden-enemy-data.json",
        "subboss_file": None  # P4G doesn't have separate subboss file
    },
    "p5": {
        "enemy_file": MEGATEN_ROOT / "src/app/p5/data/enemy-data.json",
        "subboss_file": MEGATEN_ROOT / "src/app/p5/data/subboss-data.json"
    },
    "p5r": {
        "enemy_file": MEGATEN_ROOT / "src/app/p5/data/roy-enemy-data.json",
        "subboss_file": MEGATEN_ROOT / "src/app/p5/data/subboss-data.json"  # P5R uses same subboss as P5
    }
}

def convert_resists(resists_obj):
    """Convert resistance object to string format."""
    if isinstance(resists_obj, str):
        return resists_obj
    
    # Map resistance values to characters
    resist_map = {
        "wk": "w",   # weak
        "rs": "r",   # resist
        "nu": "n",   # null
        "dr": "d",   # drain
        "rp": "d",   # repel (treat as drain)
        "ab": "d",   # absorb (treat as drain)
        "-": "-"     # normal
    }
    
    # Element order: phys, gun, fire, ice, elec, wind, psy, nuke, bless, curse
    elements = ["phys", "gun", "fire", "ice", "elec", "wind", "psy", "nuke", "bless", "curse"]
    result = ""
    
    for elem in elements:
        val = resists_obj.get(elem, "-")
        result += resist_map.get(val, "-")
    
    return result

def convert_enemy(name, data, is_subboss=False):
    """Convert megaten-fusion-tool enemy format to our format."""
    # Handle stats - can be array [hp, sp, st, ma, en, ag, lu] or object
    stats_data = data.get("stats", [])
    if isinstance(stats_data, list) and len(stats_data) >= 7:
        # Array format: [hp, sp, st, ma, en, ag, lu]
        hp = stats_data[0]
        sp = stats_data[1]
        strength = stats_data[2]
        magic = stats_data[3]
        endurance = stats_data[4]
        agility = stats_data[5]
        luck = stats_data[6]
    elif isinstance(stats_data, dict):
        # Object format
        hp = data.get("hp", 0)
        sp = data.get("mp", 0)
        strength = stats_data.get("st", 0)
        magic = stats_data.get("ma", 0)
        endurance = stats_data.get("en", 0)
        agility = stats_data.get("ag", 0)
        luck = stats_data.get("lu", 0)
    else:
        # Fallback
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
            "item": data.get("material", data.get("drop", "-"))
        },
        "isBoss": False,
        "isMiniBoss": is_subboss
    }

def process_game(game_id):
    """Process enemy data for a specific game."""
    config = GAMES[game_id]
    
    print(f"\nProcessing {game_id.upper()}...")
    
    # Load enemies
    enemies = []
    if config["enemy_file"].exists():
        enemy_data = json.loads(config["enemy_file"].read_text(encoding='utf-8'))
        for name, data in enemy_data.items():
            enemies.append(convert_enemy(name, data))
        print(f"  Loaded {len(enemies)} enemies")
    
    # Load sub-bosses
    subbosses = []
    if config["subboss_file"] and config["subboss_file"].exists():
        subboss_data = json.loads(config["subboss_file"].read_text(encoding='utf-8'))
        for name, data in subboss_data.items():
            subbosses.append(convert_enemy(name, data, is_subboss=True))
        print(f"  Loaded {len(subbosses)} sub-bosses")
    
    # Combine and sort by level
    all_enemies = enemies + subbosses
    all_enemies.sort(key=lambda x: x["level"])
    
    # Write to assets
    output_path = Path("app/src/main/assets/data/enemies") / f"{game_id}_enemies.json"
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(json.dumps(all_enemies, indent=2))
    
    print(f"  ✓ Total: {len(all_enemies)} ({len(enemies)} enemies + {len(subbosses)} sub-bosses)")
    print(f"  Written to {output_path}")

if __name__ == "__main__":
    print("Extracting P4/P4G/P5/P5R enemy data from megaten-fusion-tool...")
    
    for game_id in ["p4", "p4g", "p5", "p5r"]:
        process_game(game_id)
    
    print("\n✓ All done! Enemy data extracted for P4, P4G, P5, and P5R.")
    print("Main bosses will be added later when you provide the JSON.")
