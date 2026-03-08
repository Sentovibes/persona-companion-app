#!/usr/bin/env python3
"""
Process P3 FES and P3P main boss data and merge with existing enemy data.
"""

import json
from pathlib import Path

# Main bosses with dates
MAIN_BOSSES_DATES = {
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

def process_p3_bosses():
    """Process P3 FES and P3P main boss data."""
    print("Processing P3 FES and P3P main boss data...")
    
    # Load boss data
    fes_bosses_file = Path("extras/p3fes_main_bosses.json")
    p3p_bosses_file = Path("extras/p3p_main_bosses.json")
    
    fes_bosses = json.loads(fes_bosses_file.read_text(encoding='utf-8'))
    p3p_bosses = json.loads(p3p_bosses_file.read_text(encoding='utf-8'))
    
    # Add boss flags and dates
    for boss in fes_bosses:
        boss["isBoss"] = True
        boss["isMiniBoss"] = False
        if boss["name"] in MAIN_BOSSES_DATES:
            boss["date"] = MAIN_BOSSES_DATES[boss["name"]]
    
    for boss in p3p_bosses:
        boss["isBoss"] = True
        boss["isMiniBoss"] = False
        if boss["name"] in MAIN_BOSSES_DATES:
            boss["date"] = MAIN_BOSSES_DATES[boss["name"]]
    
    # Load existing enemy data
    fes_enemies = json.loads(Path("app/src/main/assets/data/enemies/p3fes_enemies.json").read_text())
    p3p_enemies = json.loads(Path("app/src/main/assets/data/enemies/p3p_enemies.json").read_text())
    
    # Merge bosses with enemies
    fes_all = fes_enemies + fes_bosses
    p3p_all = p3p_enemies + p3p_bosses
    
    # Sort by level
    fes_all.sort(key=lambda x: x.get("level", 0))
    p3p_all.sort(key=lambda x: x.get("level", 0))
    
    # Write back
    Path("app/src/main/assets/data/enemies/p3fes_enemies.json").write_text(json.dumps(fes_all, indent=2))
    Path("app/src/main/assets/data/enemies/p3p_enemies.json").write_text(json.dumps(p3p_all, indent=2))
    
    print(f"\n✓ P3 FES: Added {len(fes_bosses)} main bosses")
    print(f"  Total: {len(fes_all)} enemies")
    print(f"\n✓ P3 Portable: Added {len(p3p_bosses)} main bosses")
    print(f"  Total: {len(p3p_all)} enemies")

if __name__ == "__main__":
    process_p3_bosses()
