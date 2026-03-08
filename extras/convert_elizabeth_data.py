"""
Convert elizabeth shadows.json to our enemy format
Properly categorizes P3 enemies, mini-bosses, and main bosses
"""

import json

# P3 Main Bosses (Full Moon Operations + Final Bosses) with dates
P3_MAIN_BOSSES = {
    'Arcana Priestess': '4/20',
    'Arcana Empress': '5/9',
    'Arcana Emperor': '5/9',
    'Arcana Hierophant': '6/8',
    'Arcana Lovers': '7/7',
    'Arcana Chariot': '8/6',
    'Arcana Justice': '8/6',
    'Arcana Hermit': '9/5',
    'Arcana Fortune': '10/4',
    'Arcana Strength': '10/4',
    'Arcana Hanged Man': '11/3',
    'Jin Shirato': '1/31',
    'Takaya Sakaki': '1/31',
    'Nyx Avatar': '1/31',
    'Nyx': '1/31',
    'Erebus': '3/5 (The Answer)'
}

# P3 Mini-Bosses (Floor Bosses, Reaper, Elizabeth, etc.)
P3_MINI_BOSSES = [
    'Reaper', 'Elizabeth', 'Theodore', 'Margaret',
    'Rampage Drive', 'Fierce Cyclops', 'Fanatic Tower', 'Reckoning Dice',
    'Judgement Sword', 'Intrepid Knight', 'Phantom King', 'Royal Dancer',
    'Arcane Turret', 'Natural Dancer', 'Jotun of Grief', 'Jotun of Power',
    'Jotun of Evil', 'Jotun of Blood', 'Sleeping Table', 'Conceited Maya',
    'Tenjin Musha', 'Amorous Snake', 'Acheron Seeker', 'Carnal Snake',
    'Golden Beetle', 'Wealth Hand', 'Golden Hand'
]

def is_main_boss(name):
    """Check if enemy is a main story boss"""
    return name in P3_MAIN_BOSSES

def get_boss_date(name):
    """Get the date for a main boss"""
    return P3_MAIN_BOSSES.get(name, 'Unknown')

def is_mini_boss(name):
    """Check if enemy is a mini-boss"""
    return any(boss.lower() in name.lower() for boss in P3_MINI_BOSSES)

def parse_resistances(resist_data):
    """Convert elizabeth resistance format to our format"""
    resist_str = "----------"  # Phys, Fire, Ice, Elec, Wind, Light, Dark, Almi, Pierce, Strike
    
    resist_map = {
        'Weak': 'w',
        'Strong': 's',
        'Null': 'n',
        'Repel': 'r',
        'Drain': 'd',
        'Neutral': '-'
    }
    
    # Map element names to positions
    element_positions = {
        'Slash': 0,
        'Fire': 1,
        'Ice': 2,
        'Elec': 3,
        'Wind': 4,
        'Light': 5,
        'Dark': 6,
        'Almi': 7,
        'Pierce': 8,
        'Strike': 9
    }
    
    resist_list = list(resist_str)
    
    for resist_type, elements in resist_data.items():
        if resist_type in resist_map:
            char = resist_map[resist_type]
            for element in elements:
                if element in element_positions:
                    resist_list[element_positions[element]] = char
    
    return ''.join(resist_list)

def convert_elizabeth_to_enemies(elizabeth_file):
    """Convert elizabeth shadows.json to categorized enemies"""
    print(f"Converting: {elizabeth_file}")
    
    with open(elizabeth_file, 'r', encoding='utf-8') as f:
        shadows = json.load(f)
    
    enemies = []
    mini_bosses = []
    main_bosses = []
    
    for shadow in shadows:
        name = shadow.get('name', 'Unknown')
        info_list = shadow.get('info', [])
        
        # Use the first P3 variant we find
        for info in info_list:
            game = info.get('game', '')
            if 'Persona 3' in game:
                resistances = info.get('resistances', {})
                resist_str = parse_resistances(resistances)
                
                enemy = {
                    "name": name,
                    "arcana": "Shadow",
                    "level": 1,
                    "hp": 100,
                    "sp": 50,
                    "stats": {
                        "strength": 10,
                        "magic": 10,
                        "endurance": 10,
                        "agility": 10,
                        "luck": 10
                    },
                    "resists": resist_str,
                    "skills": [],
                    "area": get_boss_date(name) if is_main_boss(name) else "Unknown",
                    "exp": 0,
                    "drops": {
                        "gem": "-",
                        "item": "-"
                    }
                }
                
                # Categorize
                if is_main_boss(name):
                    main_bosses.append(enemy)
                elif is_mini_boss(name):
                    mini_bosses.append(enemy)
                else:
                    enemies.append(enemy)
                
                break  # Only use first P3 variant
    
    print(f"  Elizabeth data: {len(enemies)} enemies, {len(mini_bosses)} mini-bosses, {len(main_bosses)} main bosses")
    return enemies, mini_bosses, main_bosses

def merge_with_existing(elizabeth_enemies, elizabeth_mini, elizabeth_main):
    """Merge elizabeth data with existing P3 data"""
    # Load existing P3 enemies
    try:
        with open('extras/p3fes_enemies.json', 'r', encoding='utf-8') as f:
            existing = json.load(f)
        print(f"  Found {len(existing)} existing P3 enemies")
        
        # Separate existing by name matching
        existing_enemies = []
        existing_mini = []
        existing_main = []
        
        for e in existing:
            if is_main_boss(e['name']):
                existing_main.append(e)
            elif is_mini_boss(e['name']):
                existing_mini.append(e)
            else:
                existing_enemies.append(e)
        
        print(f"  Existing: {len(existing_enemies)} enemies, {len(existing_mini)} mini-bosses, {len(existing_main)} main bosses")
        
        # Merge - prefer existing data, add elizabeth data for new entries
        def merge_lists(existing_list, elizabeth_list):
            existing_names = {e['name'] for e in existing_list}
            new_items = [e for e in elizabeth_list if e['name'] not in existing_names]
            return existing_list + new_items
        
        merged_enemies = merge_lists(existing_enemies, elizabeth_enemies)
        merged_mini = merge_lists(existing_mini, elizabeth_mini)
        merged_main = merge_lists(existing_main, elizabeth_main)
        
        print(f"  Merged: {len(merged_enemies)} enemies, {len(merged_mini)} mini-bosses, {len(merged_main)} main bosses")
        
        return merged_enemies, merged_mini, merged_main
        
    except FileNotFoundError:
        print("  No existing file found, using elizabeth data only")
        return elizabeth_enemies, elizabeth_mini, elizabeth_main

def main():
    print("="*60)
    print("P3 Enemy Categorizer")
    print("="*60)
    print()
    
    elizabeth_file = "C:/Users/omare/Downloads/elizabeth-master/elizabeth-master/shadows.json"
    
    # Convert elizabeth data
    elizabeth_enemies, elizabeth_mini, elizabeth_main = convert_elizabeth_to_enemies(elizabeth_file)
    
    # Merge with existing
    enemies, mini_bosses, main_bosses = merge_with_existing(elizabeth_enemies, elizabeth_mini, elizabeth_main)
    
    # Save categorized files
    with open('extras/p3_enemies.json', 'w', encoding='utf-8') as f:
        json.dump(enemies, f, indent=2, ensure_ascii=False)
    print(f"\n  Saved: extras/p3_enemies.json ({len(enemies)} enemies)")
    
    with open('extras/p3_mini_bosses.json', 'w', encoding='utf-8') as f:
        json.dump(mini_bosses, f, indent=2, ensure_ascii=False)
    print(f"  Saved: extras/p3_mini_bosses.json ({len(mini_bosses)} mini-bosses)")
    
    with open('extras/p3_main_bosses.json', 'w', encoding='utf-8') as f:
        json.dump(main_bosses, f, indent=2, ensure_ascii=False)
    print(f"  Saved: extras/p3_main_bosses.json ({len(main_bosses)} main bosses)")
    
    print()
    print("="*60)
    print("Categorization complete!")
    print("="*60)

if __name__ == "__main__":
    main()
