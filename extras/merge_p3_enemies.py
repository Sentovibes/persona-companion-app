#!/usr/bin/env python3
"""
Merge P3 FES and P3P enemy categories into single files.
Combines regular enemies, mini-bosses, and main bosses.
"""

import json
from pathlib import Path

def merge_enemy_files(base_name):
    """Merge enemies, mini-bosses, and main bosses for a game version."""
    extras = Path("extras")
    
    # Load all three categories
    enemies = json.loads((extras / f"{base_name}_enemies.json").read_text())
    mini_bosses = json.loads((extras / f"{base_name}_mini_bosses.json").read_text())
    main_bosses = json.loads((extras / f"{base_name}_main_bosses.json").read_text())
    
    # Combine all enemies
    all_enemies = enemies + mini_bosses + main_bosses
    
    # Sort by level
    all_enemies.sort(key=lambda x: x['level'])
    
    # Write to assets
    output_path = Path("app/src/main/assets/data/enemies") / f"{base_name}_enemies.json"
    output_path.write_text(json.dumps(all_enemies, indent=2))
    
    print(f"✓ {base_name}: {len(enemies)} enemies + {len(mini_bosses)} mini-bosses + {len(main_bosses)} main bosses = {len(all_enemies)} total")

if __name__ == "__main__":
    print("Merging P3 enemy files...")
    merge_enemy_files("p3fes")
    merge_enemy_files("p3p")
    print("\nDone! All P3 enemies merged.")
