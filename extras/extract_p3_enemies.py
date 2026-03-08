#!/usr/bin/env python3
"""
Extract P3 FES and P3 Portable enemy data from megaten-fusion-tool.
Uses van-enemy-data.json for vanilla/FES enemies and ans-enemy-data.json for The Answer.
"""

import json
from pathlib import Path

# Source paths
MEGATEN_P3 = Path(r"C:\Users\omare\Downloads\megaten-fusion-tool-master\megaten-fusion-tool-master\src\app\p3\data")

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

# Mini-boss patterns
MINI_BOSS_PATTERNS = ["Relic", "Musha", "Cyclops", "Jotun", "Turret", "Drive"]

def is_mini_boss(name):
    """Check if enemy is a mini-boss."""
    for pattern in MINI_BOSS_PATTERNS:
        if pattern in name:
            return True
    return False

def is_main_boss(name):
    """Check if enemy is a main boss."""
    return name in MAIN_BOSSES

def is_monad_enemy(area):
    """Check if enemy is from Monad Depths (FES only)."""
    return "Monad" in area

def normalize_resists(resists_str):
    """Normalize resistance string to lowercase standard format."""
    # Map: w=weak, r=resist, n=null, d=drain, s=strong(resist), -=normal
    # T and S (uppercase) seem to be special cases - treat as resist
    normalized = ""
    for char in resists_str:
        if char in ['T', 'S']:
            normalized += 'r'  # Treat uppercase as resist
        else:
            normalized += char.lower()
    return normalized

def convert_enemy(name, data):
    """Convert megaten-fusion-tool enemy format to our format."""
    # Stats format: [hp, sp, st, ma, en, ag, lu]
    stats_data = data.get("stats", [0, 0, 0, 0, 0, 0, 0])
    
    enemy = {
        "name": name,
        "arcana": data.get("race", "Shadow"),
        "level": data.get("lvl", 0),
        "hp": stats_data[0] if len(stats_data) > 0 else 0,
        "sp": stats_data[1] if len(stats_data) > 1 else 0,
        "stats": {
            "strength": stats_data[2] if len(stats_data) > 2 else 0,
            "magic": stats_data[3] if len(stats_data) > 3 else 0,
            "endurance": stats_data[4] if len(stats_data) > 4 else 0,
            "agility": stats_data[5] if len(stats_data) > 5 else 0,
            "luck": stats_data[6] if len(stats_data) > 6 else 0
        },
        "resists": normalize_resists(data.get("resists", "----------")),
        "skills": data.get("skills", []),
        "area": data.get("area", "Tartarus"),
        "exp": data.get("exp", 0),
        "drops": {
            "gem": data.get("gem", "-"),
            "item": data.get("item", "-")
        },
        "isBoss": False,
        "isMiniBoss": False
    }
    
    # Set boss flags
    if is_main_boss(name):
        enemy["isBoss"] = True
        enemy["date"] = MAIN_BOSSES[name]
    elif is_mini_boss(name):
        enemy["isMiniBoss"] = True
    
    return enemy

def process_p3_enemies():
    """Process P3 FES and P3P enemy data."""
    print("Processing P3 FES and P3P enemy data...")
    
    # Load vanilla/FES enemies
    van_file = MEGATEN_P3 / "van-enemy-data.json"
    van_data = json.loads(van_file.read_text(encoding='utf-8'))
    
    # Load The Answer enemies
    ans_file = MEGATEN_P3 / "ans-enemy-data.json"
    ans_data = json.loads(ans_file.read_text(encoding='utf-8'))
    
    # Convert all enemies
    fes_enemies = []
    p3p_enemies = []
    
    # Process vanilla enemies (both FES and P3P have these)
    for name, data in van_data.items():
        enemy = convert_enemy(name, data)
        area = enemy.get("area", "")
        
        # FES gets all enemies
        fes_enemies.append(enemy)
        
        # P3P gets all except Monad enemies
        if not is_monad_enemy(area):
            p3p_enemies.append(enemy)
    
    # Process The Answer enemies (FES only)
    for name, data in ans_data.items():
        enemy = convert_enemy(name, data)
        fes_enemies.append(enemy)
    
    # Sort by level
    fes_enemies.sort(key=lambda x: x["level"])
    p3p_enemies.sort(key=lambda x: x["level"])
    
    # Count categories
    fes_regular = [e for e in fes_enemies if not e["isBoss"] and not e["isMiniBoss"]]
    fes_mini = [e for e in fes_enemies if e["isMiniBoss"]]
    fes_main = [e for e in fes_enemies if e["isBoss"]]
    
    p3p_regular = [e for e in p3p_enemies if not e["isBoss"] and not e["isMiniBoss"]]
    p3p_mini = [e for e in p3p_enemies if e["isMiniBoss"]]
    p3p_main = [e for e in p3p_enemies if e["isBoss"]]
    
    # Write to assets
    assets_path = Path("app/src/main/assets/data/enemies")
    assets_path.mkdir(parents=True, exist_ok=True)
    
    (assets_path / "p3fes_enemies.json").write_text(json.dumps(fes_enemies, indent=2))
    (assets_path / "p3p_enemies.json").write_text(json.dumps(p3p_enemies, indent=2))
    
    print(f"\n✓ P3 FES: {len(fes_regular)} enemies + {len(fes_mini)} mini-bosses + {len(fes_main)} main bosses = {len(fes_enemies)} total")
    print(f"✓ P3 Portable: {len(p3p_regular)} enemies + {len(p3p_mini)} mini-bosses + {len(p3p_main)} main bosses = {len(p3p_enemies)} total")
    print(f"\nFiles written to {assets_path}")

if __name__ == "__main__":
    process_p3_enemies()
