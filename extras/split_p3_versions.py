"""
Split P3 enemy data into FES and Portable versions
Portable doesn't have Monad Depths
"""

import json

def split_p3_data():
    print("="*60)
    print("P3 FES/Portable Splitter")
    print("="*60)
    print()
    
    # Load categorized P3 data
    with open('extras/p3_enemies.json', 'r', encoding='utf-8') as f:
        enemies = json.load(f)
    
    with open('extras/p3_mini_bosses.json', 'r', encoding='utf-8') as f:
        mini_bosses = json.load(f)
    
    with open('extras/p3_main_bosses.json', 'r', encoding='utf-8') as f:
        main_bosses = json.load(f)
    
    print(f"Loaded: {len(enemies)} enemies, {len(mini_bosses)} mini-bosses, {len(main_bosses)} main bosses")
    
    # Filter out Monad Depths enemies for Portable
    def is_monad(enemy):
        area = enemy.get('area', '')
        return 'monad' in area.lower()
    
    # FES has everything
    fes_enemies = enemies
    fes_mini = mini_bosses
    fes_main = main_bosses
    
    # Portable excludes Monad
    portable_enemies = [e for e in enemies if not is_monad(e)]
    portable_mini = [e for e in mini_bosses if not is_monad(e)]
    portable_main = main_bosses  # Main bosses don't have Monad
    
    monad_enemies_removed = len(enemies) - len(portable_enemies)
    monad_mini_removed = len(mini_bosses) - len(portable_mini)
    
    print(f"\nP3 FES: {len(fes_enemies)} enemies, {len(fes_mini)} mini-bosses, {len(fes_main)} main bosses")
    print(f"P3 Portable: {len(portable_enemies)} enemies, {len(portable_mini)} mini-bosses, {len(portable_main)} main bosses")
    print(f"Removed {monad_enemies_removed} Monad enemies, {monad_mini_removed} Monad mini-bosses from Portable")
    
    # Save FES files
    with open('extras/p3fes_enemies.json', 'w', encoding='utf-8') as f:
        json.dump(fes_enemies, f, indent=2, ensure_ascii=False)
    
    with open('extras/p3fes_mini_bosses.json', 'w', encoding='utf-8') as f:
        json.dump(fes_mini, f, indent=2, ensure_ascii=False)
    
    with open('extras/p3fes_main_bosses.json', 'w', encoding='utf-8') as f:
        json.dump(fes_main, f, indent=2, ensure_ascii=False)
    
    print(f"\nSaved P3 FES files")
    
    # Save Portable files
    with open('extras/p3p_enemies.json', 'w', encoding='utf-8') as f:
        json.dump(portable_enemies, f, indent=2, ensure_ascii=False)
    
    with open('extras/p3p_mini_bosses.json', 'w', encoding='utf-8') as f:
        json.dump(portable_mini, f, indent=2, ensure_ascii=False)
    
    with open('extras/p3p_main_bosses.json', 'w', encoding='utf-8') as f:
        json.dump(portable_main, f, indent=2, ensure_ascii=False)
    
    print(f"Saved P3 Portable files")
    
    print()
    print("="*60)
    print("Split complete!")
    print("="*60)

if __name__ == "__main__":
    split_p3_data()
