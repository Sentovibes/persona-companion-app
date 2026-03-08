#!/usr/bin/env python3
"""
Count enemies, mini-bosses, and main bosses for each game.
"""

import json
from pathlib import Path

def count_enemy_types(game_id):
    """Count enemy types for a specific game."""
    enemy_file = Path(f"app/src/main/assets/data/enemies/{game_id}_enemies.json")
    
    if not enemy_file.exists():
        return None
    
    enemies = json.loads(enemy_file.read_text(encoding='utf-8'))
    
    regular = sum(1 for e in enemies if not e.get("isMiniBoss", False) and not e.get("isBoss", False))
    mini_bosses = sum(1 for e in enemies if e.get("isMiniBoss", False))
    main_bosses = sum(1 for e in enemies if e.get("isBoss", False))
    
    return {
        "total": len(enemies),
        "regular": regular,
        "mini_bosses": mini_bosses,
        "main_bosses": main_bosses
    }

if __name__ == "__main__":
    games = {
        "P3 FES": "p3fes",
        "P3 Portable": "p3p",
        "P3 Reload": "p3r",
        "P4": "p4",
        "P4 Golden": "p4g",
        "P5": "p5",
        "P5 Royal": "p5r"
    }
    
    print("=" * 70)
    print("ENEMY COUNT BY GAME")
    print("=" * 70)
    print()
    
    for game_name, game_id in games.items():
        counts = count_enemy_types(game_id)
        if counts:
            print(f"{game_name}:")
            print(f"  Regular Enemies:  {counts['regular']:3d}")
            print(f"  Mini Bosses:      {counts['mini_bosses']:3d}")
            print(f"  Main Bosses:      {counts['main_bosses']:3d}")
            print(f"  {'─' * 30}")
            print(f"  TOTAL:            {counts['total']:3d}")
            print()
        else:
            print(f"{game_name}: No data found")
            print()
    
    print("=" * 70)
