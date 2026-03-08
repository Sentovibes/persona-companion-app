#!/usr/bin/env python3
"""
Update P3R boss data with new complete boss information.
Replaces existing P3R bosses with proper stats, skills, and multi-part/phase support.
"""

import json
from pathlib import Path

def convert_resists_array(resists_array):
    """Convert resistance array to string format."""
    resist_map = {
        "wk": "w",
        "rs": "r",
        "nu": "n",
        "ab": "d",  # absorb -> drain
        "rp": "d",  # repel -> drain
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
            "sp": part_data.get("sp", 999),
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

def update_p3r_bosses():
    """Update P3R boss data."""
    print("Updating P3R boss data...")
    
    # Load new boss data
    boss_file = Path("extras/persona3reload-bosses.json")
    boss_data = json.loads(boss_file.read_text(encoding='utf-8'))
    
    # Load existing P3R enemy data (regular enemies + mini-bosses)
    p3r_enemies = json.loads(Path("app/src/main/assets/data/enemies/p3r_enemies.json").read_text())
    
    # Remove old main bosses (keep only regular enemies and mini-bosses)
    p3r_enemies = [e for e in p3r_enemies if not e.get("isBoss", False)]
    
    main_bosses = []
    
    for boss_name, boss_info in boss_data.items():
        # Base boss structure
        boss = {
            "name": boss_name,
            "arcana": boss_info.get("arcana", "Unknown"),
            "level": boss_info.get("level", 0),
            "hp": boss_info.get("hp", 0),
            "sp": boss_info.get("sp", 999),
            "resists": convert_resists_array(boss_info.get("resists", [])) if "resists" in boss_info else "---------",
            "skills": boss_info.get("skills", []),
            "area": "Boss",
            "exp": 0,
            "version": boss_info.get("version", ""),
            "isBoss": True,
            "isMiniBoss": False
        }
        
        # Handle phases (like Nyx Avatar)
        if "phases" in boss_info:
            boss["phases"] = process_boss_phases(boss_info["phases"])
        
        # Handle parts (like Emperor & Empress)
        if "parts" in boss_info:
            boss["parts"] = process_boss_parts(boss_info["parts"])
        
        main_bosses.append(boss)
    
    # Combine: regular enemies + mini-bosses + main bosses
    p3r_all = p3r_enemies + main_bosses
    
    # Sort by level
    p3r_all.sort(key=lambda x: x.get("level", 0))
    
    # Write back
    Path("app/src/main/assets/data/enemies/p3r_enemies.json").write_text(json.dumps(p3r_all, indent=2))
    
    # Count categories
    regular = [e for e in p3r_all if not e["isBoss"] and not e["isMiniBoss"]]
    mini = [e for e in p3r_all if e["isMiniBoss"]]
    main = [e for e in p3r_all if e["isBoss"]]
    
    print(f"\n✓ P3R: {len(regular)} enemies + {len(mini)} mini-bosses + {len(main)} main bosses = {len(p3r_all)} total")
    
    print("\nBosses with phases:")
    for boss in main_bosses:
        if "phases" in boss:
            print(f"  - {boss['name']}: {len(boss['phases'])} phases")
    
    print("\nBosses with parts:")
    for boss in main_bosses:
        if "parts" in boss:
            print(f"  - {boss['name']}: {len(boss['parts'])} parts")

if __name__ == "__main__":
    update_p3r_bosses()
