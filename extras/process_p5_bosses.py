#!/usr/bin/env python3
"""
Process P5/P5R boss data and merge with existing enemy data.
Handles multi-phase bosses and bosses with multiple parts.
"""

import json
from pathlib import Path

# Mini-bosses to exclude from main bosses
MINI_BOSSES = ["Justine & Caroline", "Lavenza"]

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
    """Process boss parts (like Kamoshida's Trophy, Madarame's eyes, etc.)."""
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
    """Process boss phases (like Kaneshiro's Piggytron -> Bael)."""
    phases = []
    for phase_name, phase_data in phases_data.items():
        phase = {
            "name": phase_name,
            "hp": phase_data.get("hp", 0),
            "sp": phase_data.get("sp", 999),
            "resists": convert_resists_array(phase_data.get("resists", [])),
            "skills": phase_data.get("skills", [])
        }
        
        # Check if this phase has parts (like Yaldabaoth)
        if "parts" in phase_data:
            phase["parts"] = process_boss_parts(phase_data["parts"])
        
        phases.append(phase)
    return phases

def process_p5_bosses():
    """Process P5/P5R boss data."""
    print("Processing P5/P5R boss data...")
    
    # Load boss data
    boss_file = Path("extras/persona5+royal-bosses.json")
    boss_data = json.loads(boss_file.read_text(encoding='utf-8'))
    
    # Load existing enemy data
    p5_enemies = json.loads(Path("app/src/main/assets/data/enemies/p5_enemies.json").read_text())
    p5r_enemies = json.loads(Path("app/src/main/assets/data/enemies/p5r_enemies.json").read_text())
    
    main_bosses = []
    mini_bosses = []
    
    for boss_name, boss_info in boss_data.items():
        # Check if mini-boss
        is_mini = boss_name in MINI_BOSSES or "Secret Boss" in boss_info.get("version", "")
        
        # Base boss structure
        boss = {
            "name": boss_name,
            "arcana": boss_info.get("arcana", "Unknown"),
            "level": boss_info.get("level", 0),
            "hp": boss_info.get("hp", 0),
            "sp": boss_info.get("sp", 999),
            "resists": convert_resists_array(boss_info.get("resists", [])) if "resists" in boss_info else "----------",
            "skills": boss_info.get("skills", []),
            "area": "Boss",
            "exp": 0,
            "version": boss_info.get("version", ""),
            "isBoss": not is_mini,
            "isMiniBoss": is_mini
        }
        
        # Handle phases
        if "phases" in boss_info:
            boss["phases"] = process_boss_phases(boss_info["phases"])
        
        # Handle parts (for bosses without phases but with parts)
        if "parts" in boss_info:
            boss["parts"] = process_boss_parts(boss_info["parts"])
        
        if is_mini:
            mini_bosses.append(boss)
        else:
            main_bosses.append(boss)
    
    # Merge with existing enemies
    # P5 gets vanilla bosses
    p5_bosses = [b for b in main_bosses if "Royal Exclusive" not in b.get("version", "")]
    p5_mini = [b for b in mini_bosses if "Royal Exclusive" not in b.get("version", "")]
    
    # P5R gets all bosses
    p5r_bosses = main_bosses
    p5r_mini = mini_bosses
    
    # Combine and sort
    p5_all = p5_enemies + p5_mini + p5_bosses
    p5r_all = p5r_enemies + p5r_mini + p5r_bosses
    
    p5_all.sort(key=lambda x: x.get("level", 0))
    p5r_all.sort(key=lambda x: x.get("level", 0))
    
    # Write back
    Path("app/src/main/assets/data/enemies/p5_enemies.json").write_text(json.dumps(p5_all, indent=2))
    Path("app/src/main/assets/data/enemies/p5r_enemies.json").write_text(json.dumps(p5r_all, indent=2))
    
    print(f"\n✓ P5: Added {len(p5_bosses)} main bosses + {len(p5_mini)} mini-bosses")
    print(f"  Total: {len(p5_all)} enemies")
    print(f"\n✓ P5R: Added {len(p5r_bosses)} main bosses + {len(p5r_mini)} mini-bosses")
    print(f"  Total: {len(p5r_all)} enemies")
    
    print("\nBosses with phases:")
    for boss in main_bosses:
        if "phases" in boss:
            print(f"  - {boss['name']}: {len(boss['phases'])} phases")
    
    print("\nBosses with parts:")
    for boss in main_bosses:
        if "parts" in boss:
            print(f"  - {boss['name']}: {len(boss['parts'])} parts")

if __name__ == "__main__":
    process_p5_bosses()
