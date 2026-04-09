import os
import json
import csv

def tsv_to_json(tsv_path, category):
    items = []
    if not os.path.exists(tsv_path):
        return []
    
    with open(tsv_path, 'r', encoding='utf-8') as f:
        reader = csv.reader(f, delimiter='\t')
        for row in reader:
            if not row or len(row) < 2:
                continue
            name = row[0].strip()
            effect = row[1].strip()
            if not name or name.lower() == 'name':
                continue
            
            items.append({
                "name": name,
                "description": effect,
                "effect": effect,
                "price": 0,
                "sellPrice": 0,
                "category": category
            })
    return items

def process_game(game_code, data_dir, output_file):
    all_items = []
    
    # Generic item files in p345
    mappings = {
        "item-effects.tsv": "Consumables",
        "accessory-effects.tsv": "Accessories",
        "armor-effects.tsv": "Armor",
        "melee-effects.tsv": "Weapons",
        "ranged-effects.tsv": "Weapons",
        "003-item.tsv": "Consumables",
        "000-weapon.tsv": "Weapons",
        "001-armor.tsv": "Armor",
        "002-accessory.tsv": "Accessories",
        "weapon-effects.tsv": "Weapons",
        "boots-effects.tsv": "Armor"
    }
    
    for filename, cat in mappings.items():
        path = os.path.join(data_dir, filename)
        all_items.extend(tsv_to_json(path, cat))
    
    if all_items:
        os.makedirs(os.path.dirname(output_file), exist_ok=True)
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump({"items": all_items}, f, indent=4)
        print(f"Generated {output_file} with {len(all_items)} items")

# Paths
megaten_db = r"C:\Users\omare\Music\Persona-Companion-App\EXTRA DB\megaten-database-main\src\p345"
app_assets = r"C:\Users\omare\Music\Persona-Companion-App\persona-companion-app\app\src\main\assets\data\items"

# Persona 5
process_game("p5", os.path.join(megaten_db, "p5-data"), os.path.join(app_assets, "p5_items.json"))
process_game("p5r", os.path.join(megaten_db, "p5-data"), os.path.join(app_assets, "p5r_items.json"))

# Persona 4
process_game("p4", os.path.join(megaten_db, "p4-data"), os.path.join(app_assets, "p4_items.json"))
process_game("p4g", os.path.join(megaten_db, "p4-data"), os.path.join(app_assets, "p4g_items.json"))

# Persona 3
process_game("p3", os.path.join(megaten_db, "p3-data"), os.path.join(app_assets, "p3_items.json"))
process_game("p3fes", os.path.join(megaten_db, "p3-data"), os.path.join(app_assets, "p3fes_items.json"))
process_game("p3p", os.path.join(megaten_db, "p3-data"), os.path.join(app_assets, "p3p_items.json"))

print("Done!")
