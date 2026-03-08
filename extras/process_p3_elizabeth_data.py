#!/usr/bin/env python3
"""
Process P3 enemy data from elizabeth-master/shadows.json
Creates separate files for P3 FES and P3 Portable with proper categorization.
Flattens the nested structure into the format expected by the app.
"""

import json
from pathlib import Path

# Elizabeth data path
ELIZABETH_PATH = Path(r"C:\Users\omare\Downloads\elizabeth-master\elizabeth-master\shadows.json")

# Monad Depths enemies (FES only, not in Portable)
MONAD_ENEMIES = [
    "Chaos Cyclops", "Divine Mother", "Eternal Sand", "Grand Magus", 
    "Hallowed Turret", "Jotun of Blood", "Jotun of Evil", "Jotun of Grief", "Jotun of Power"
]

# Mini-bosses (floor bosses) - enemies with "B" suffix or specific patterns
MINI_BOSS_PATTERNS = [" B", "Relic", "Musha"]

# Main bosses with dates
MAIN_BOSSES = {
    "Arcana Priestess": "4/20",
    "Arcana Empress": "5/9", 
    "Arcana Emperor": "6/8",
    "Arcana Hierophant": "7/7",
    "Arcana Lovers": "8/6",
    "Arcana Chariot": "9/5",
    "Arcana Justice": "10/4",
    "Arcana Hermit": "11/3",
    "Arcana Fortune": "12/2",
    "Arcana Strength": "12/30",
    "Nyx Avatar": "1/31",
    "The Reaper": "Always"
}

# Additional mini-boss names that don't match patterns
ADDITIONAL_MINI_BOSSES = [
    "Rampage Drive", "Fierce Cyclops", "Brilliant Cyclops", 
    "Chaos Cyclops", "Jotun of Power", "Jotun of Evil",
    "Jotun of Blood", "Jotun of Grief"
]

# P3 element order: Slash, Strike, Pierce, Fire, Ice, Elec, Wind, Light, Dark, Almighty
P3_ELEMENTS = ["Slash", "Strike", "Pierce", "Fire", "Ice", "Elec", "Wind", "Light", "Dark", "Almi"]

def convert_resistances(resist_obj):
    """Convert elizabeth resistance format to string format."""
    # Map resistance types to characters
    resist_map = {
        "Weak": "w",
        "Strong": "r",  # Strong = Resist
        "Null": "n",
        "Drain": "d",
        "Repel": "d"  # Treat repel as drain
    }
    
    # Build resistance string for P3 elements
    result = []
    for elem in P3_ELEMENTS:
        found = "-"  # Default to neutral
        for resist_type, elements in resist_obj.items():
            if elem in elements or (elem == "Almi" and "Almighty" in elements):
                found = resist_map.get(resist_type, "-")
                break
        result.append(found)
    
    return "".join(result)

def flatten_enemy(shadow_data):
    """Flatten elizabeth nested structure to app format."""
    name = shadow_data.get("name", "Unknown")
    
    # Use "The Journey" variant as default (most common)
    info_list = shadow_data.get("info", [])
    journey_info = None
    for info in info_list:
        if info.get("variant") == "The Journey":
            journey_info = info
            break
    
    # Fallback to first variant if no Journey found
    if not journey_info and info_list:
        journey_info = info_list[0]
    
    if not journey_info:
        print(f"Warning: No info found for {name}")
        return None
    
    # Extract data
    resistances = journey_info.get("resistances", {})
    
    # Create flattened enemy
    enemy = {
        "name": name,
        "arcana": "Shadow",  # Elizabeth data doesn't have arcana
        "level": 0,  # Elizabeth data doesn't have level
        "hp": 0,  # Elizabeth data doesn't have stats
        "sp": 0,
        "resists": convert_resistances(resistances),
        "skills": [],  # Elizabeth data doesn't have skills
        "area": "Tartarus",  # Default area
        "exp": 0,
        "isBoss": False,
        "isMiniBoss": False
    }
    
    return enemy

def is_mini_boss(enemy):
    """Check if enemy is a mini-boss."""
    name = enemy.get("name", "")
    # Check for patterns
    for pattern in MINI_BOSS_PATTERNS:
        if pattern in name:
            return True
    # Check additional mini-bosses
    if name in ADDITIONAL_MINI_BOSSES:
        return True
    return False

def is_main_boss(enemy):
    """Check if enemy is a main boss."""
    return enemy.get("name", "") in MAIN_BOSSES

def is_monad_enemy(enemy):
    """Check if enemy is from Monad Depths."""
    name = enemy.get("name", "")
    return name in MONAD_ENEMIES

def process_elizabeth_data():
    """Process elizabeth shadows.json data."""
    print("Loading elizabeth data...")
    raw_data = json.loads(ELIZABETH_PATH.read_text(encoding='utf-8'))
    
    # Flatten all enemies
    flattened = []
    for shadow in raw_data:
        enemy = flatten_enemy(shadow)
        if enemy:
            flattened.append(enemy)
    
    print(f"Flattened {len(flattened)} enemies from elizabeth data")
    
    # Separate into categories
    p3fes_enemies = []
    p3fes_mini_bosses = []
    p3fes_main_bosses = []
    
    p3p_enemies = []
    p3p_mini_bosses = []
    p3p_main_bosses = []
    
    for enemy in flattened:
        name = enemy.get("name", "")
        
        # Add date and flags for main bosses
        if is_main_boss(enemy):
            enemy["date"] = MAIN_BOSSES[name]
            enemy["isBoss"] = True
            enemy["isMiniBoss"] = False
            p3fes_main_bosses.append(enemy)
            p3p_main_bosses.append(enemy)
        elif is_mini_boss(enemy):
            enemy["isBoss"] = False
            enemy["isMiniBoss"] = True
            # Check if Monad (FES only)
            if is_monad_enemy(enemy):
                p3fes_mini_bosses.append(enemy)
            else:
                p3fes_mini_bosses.append(enemy)
                p3p_mini_bosses.append(enemy)
        else:
            # Regular enemy
            enemy["isBoss"] = False
            enemy["isMiniBoss"] = False
            if is_monad_enemy(enemy):
                p3fes_enemies.append(enemy)
            else:
                p3fes_enemies.append(enemy)
                p3p_enemies.append(enemy)
    
    # Combine all for each version
    p3fes_all = p3fes_enemies + p3fes_mini_bosses + p3fes_main_bosses
    p3p_all = p3p_enemies + p3p_mini_bosses + p3p_main_bosses
    
    # Write to assets
    assets_path = Path("app/src/main/assets/data/enemies")
    assets_path.mkdir(parents=True, exist_ok=True)
    
    (assets_path / "p3fes_enemies.json").write_text(json.dumps(p3fes_all, indent=2))
    (assets_path / "p3p_enemies.json").write_text(json.dumps(p3p_all, indent=2))
    
    print(f"\n✓ P3 FES: {len(p3fes_enemies)} enemies + {len(p3fes_mini_bosses)} mini-bosses + {len(p3fes_main_bosses)} main bosses = {len(p3fes_all)} total")
    print(f"✓ P3 Portable: {len(p3p_enemies)} enemies + {len(p3p_mini_bosses)} mini-bosses + {len(p3p_main_bosses)} main bosses = {len(p3p_all)} total")
    print(f"\nFiles written to {assets_path}")

if __name__ == "__main__":
    process_elizabeth_data()
