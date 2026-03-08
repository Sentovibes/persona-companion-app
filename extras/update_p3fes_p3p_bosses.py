#!/usr/bin/env python3
"""
Update P3 FES and P3P boss data with complete boss information.
Replaces placeholder bosses with proper stats, skills, and multi-part/phase support.
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

def update_p3_bosses():
    """Update P3 FES and P3P boss data."""
    print("Updating P3 FES and P3P boss data...")
    
    # Load new boss data
    boss_file = Path("extras/persona3fes-portable-bosses.json")
    boss_data = json.loads(boss_file.read_text(encoding='utf-8'))
    
    # Load existing enemy data (regular enemies + mini-bosses)
    p3fes_enemies = json.loads(Path("app/src/main/assets/data/enemies/p3fes_enemies.json").read_text())
    p3p_enemies = json.loads(Path("app/src/main/assets/data/enemies/p3p_enemies.json").read_text())
    
    # Remove old main bosses (keep only regular enemies and mini-bosses)
    p3fes_enemies = [e for e in p3fes_enemies if not e.get("isBoss", False)]
    p3p_enemies = [e for e in p3p_enemies if not e.get("isBoss", False)]
    
    fes_bosses = []
    p3p_bosses = []
    
    for boss_name, boss_info in boss_data.items():
        version = boss_info.get("version", "")
        
        # Skip Portable-exclusive bosses for FES
        if "Portable Exclusive" in version:
            continue
        
        # Skip FES-exclusive bosses for Portable
        is_fes_exclusive = "FES Exclusive" in version
        
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
            "version": version,
            "isBoss": True,
            "isMiniBoss": False
        }
        
        # Handle phases (like Nyx Avatar)
        if "phases" in boss_info:
            boss["phases"] = process_boss_phases(boss_info["phases"])
        
        # Handle parts (like Emperor & Empress)
        if "parts" in boss_info:
            boss["parts"] = process_boss_parts(boss_info["parts"])
        
        # Add to FES
        fes_bosses.append(boss)
        
        # Add to P3P if not FES-exclusive
        if not is_fes_exclusive:
            p3p_bosses.append(boss.copy())
    
    # Combine: regular enemies + mini-bosses + main bosses
    p3fes_all = p3fes_enemies + fes_bosses
    p3p_all = p3p_enemies + p3p_bosses
    
    # Sort by level
    p3fes_all.sort(key=lambda x: x.get("level", 0))
    p3p_all.sort(key=lambda x: x.get("level", 0))
    
    # Write back
    Path("app/src/main/assets/data/enemies/p3fes_enemies.json").write_text(json.dumps(p3fes_all, indent=2))
    Path("app/src/main/assets/data/enemies/p3p_enemies.json").write_text(json.dumps(p3p_all, indent=2))
    
    # Count categories
    fes_regular = [e for e in p3fes_all if not e["isBoss"] and not e["isMiniBoss"]]
    fes_mini = [e for e in p3fes_all if e["isMiniBoss"]]
    fes_main = [e for e in p3fes_all if e["isBoss"]]
    
    p3p_regular = [e for e in p3p_all if not e["isBoss"] and not e["isMiniBoss"]]
    p3p_mini = [e for e in p3p_all if e["isMiniBoss"]]
    p3p_main = [e for e in p3p_all if e["isBoss"]]
    
    print(f"\n✓ P3 FES: {len(fes_regular)} enemies + {len(fes_mini)} mini-bosses + {len(fes_main)} main bosses = {len(p3fes_all)} total")
    print(f"✓ P3 Portable: {len(p3p_regular)} enemies + {len(p3p_mini)} mini-bosses + {len(p3p_main)} main bosses = {len(p3p_all)} total")
    
    print("\nBosses with phases:")
    for boss in fes_bosses:
        if "phases" in boss:
            print(f"  - {boss['name']}: {len(boss['phases'])} phases")
    
    print("\nBosses with parts:")
    for boss in fes_bosses:
        if "parts" in boss:
            print(f"  - {boss['name']}: {len(boss['parts'])} parts")

if __name__ == "__main__":
    update_p3_bosses()
