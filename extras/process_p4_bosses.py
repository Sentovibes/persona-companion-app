#!/usr/bin/env python3
"""
Process P4/P4G boss and mini-boss data and merge with existing enemy data.
"""

import json
from pathlib import Path

def convert_resists_array(resists_array):
    """Convert resistance array to string format."""
    resist_map = {
        "wk": "w",
        "rs": "r",
        "nu": "n",
        "dr": "d",
        "rp": "d",  # repel -> drain
        "ab": "d",  # absorb -> drain
        "-": "-"
    }
    return "".join(resist_map.get(r, "-") for r in resists_array)

def process_boss_parts(parts_data):
    """Process boss parts."""
    parts = []
    for part_name, part_data in parts_data.items():
        parts.append({
            "name": part_name,
            "hp": part_data.get("hp", 0),
            "sp": part_data.get("sp"),
            "resists": convert_resists_array(part_data.get("resists", [])),
            "skills": part_data.get("skills", [])
        })
    return parts

def process_boss_phases(phases_data):
    """Process boss phases."""
    phases = []
    for phase_name, phase_data in phases_data.items():
        phase = {
            "name": phase_name,
            "hp": phase_data.get("hp", 0),
            "sp": phase_data.get("sp", 999),
            "resists": convert_resists_array(phase_data.get("resists", [])),
            "skills": phase_data.get("skills", [])
        }
        phases.append(phase)
    return phases

def process_p4_bosses():
    """Process P4/P4G boss and mini-boss data."""
    print("Processing P4/P4G boss data...")
    
    # Load boss and mini-boss data
    boss_file = Path("persona4+golden-bosses-template.json")
    mini_boss_file = Path("persona4+golden-mini-bosses.json")
    
    boss_data = json.loads(boss_file.read_text(encoding='utf-8'))
    mini_boss_data = json.loads(mini_boss_file.read_text(encoding='utf-8'))
    
    # Load existing enemy data
    p4_enemies = json.loads(Path("app/src/main/assets/data/enemies/p4_enemies.json").read_text())
    p4g_enemies = json.loads(Path("app/src/main/assets/data/enemies/p4g_enemies.json").read_text())
    
    main_bosses = []
    mini_bosses = []
    
    # Process main bosses
    for boss_name, boss_info in boss_data.items():
        is_secret = "Secret Boss" in boss_info.get("version", "")
        
        boss = {
            "name": boss_name,
            "arcana": boss_info.get("arcana", "Unknown"),
            "level": boss_info.get("level", 0),
            "hp": boss_info.get("hp", 0),
            "sp": boss_info.get("sp", 999),
            "resists": convert_resists_array(boss_info.get("resists", [])) if "resists" in boss_info else "-------",
            "skills": boss_info.get("skills", []),
            "area": "Boss",
            "exp": 0,
            "version": boss_info.get("version", ""),
            "isBoss": not is_secret,
            "isMiniBoss": is_secret
        }
        
        # Handle phases
        if "phases" in boss_info:
            boss["phases"] = process_boss_phases(boss_info["phases"])
        
        # Handle parts
        if "parts" in boss_info:
            boss["parts"] = process_boss_parts(boss_info["parts"])
        
        if is_secret:
            mini_bosses.append(boss)
        else:
            main_bosses.append(boss)
    
    # Process mini-bosses
    for mini_name, mini_info in mini_boss_data.items():
        mini = {
            "name": mini_name,
            "arcana": mini_info.get("arcana", "Unknown"),
            "level": mini_info.get("level", 0),
            "hp": mini_info.get("hp", 0),
            "sp": mini_info.get("sp", 999),
            "resists": convert_resists_array(mini_info.get("resists", [])),
            "skills": mini_info.get("skills", []),
            "area": mini_info.get("location", "Unknown"),
            "exp": 0,
            "version": mini_info.get("version", ""),
            "isBoss": False,
            "isMiniBoss": True
        }
        mini_bosses.append(mini)
    
    # Separate by version
    p4_main = [b for b in main_bosses if "Golden Exclusive" not in b.get("version", "")]
    p4_mini = [b for b in mini_bosses if "Golden Exclusive" not in b.get("version", "")]
    
    p4g_main = main_bosses
    p4g_mini = mini_bosses
    
    # Combine and sort
    p4_all = p4_enemies + p4_mini + p4_main
    p4g_all = p4g_enemies + p4g_mini + p4g_main
    
    p4_all.sort(key=lambda x: x.get("level", 0))
    p4g_all.sort(key=lambda x: x.get("level", 0))
    
    # Write back
    Path("app/src/main/assets/data/enemies/p4_enemies.json").write_text(json.dumps(p4_all, indent=2))
    Path("app/src/main/assets/data/enemies/p4g_enemies.json").write_text(json.dumps(p4g_all, indent=2))
    
    print(f"\n✓ P4: Added {len(p4_main)} main bosses + {len(p4_mini)} mini-bosses")
    print(f"  Total: {len(p4_all)} enemies")
    print(f"\n✓ P4G: Added {len(p4g_main)} main bosses + {len(p4g_mini)} mini-bosses")
    print(f"  Total: {len(p4g_all)} enemies")
    
    print("\nBosses with phases:")
    for boss in main_bosses:
        if "phases" in boss:
            print(f"  - {boss['name']}: {len(boss['phases'])} phases")
    
    print("\nBosses with parts:")
    for boss in main_bosses:
        if "parts" in boss:
            print(f"  - {boss['name']}: {len(boss['parts'])} parts")

if __name__ == "__main__":
    process_p4_bosses()
