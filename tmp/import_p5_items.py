import json
import os
import csv

def parse_p5_tsv(root_dir, game_id):
    items = []
    
    # Files to process
    files_map = {
        "item-effects.tsv": "Consumable",
        "melee-effects.tsv": "Weapon",
        "gun-effects.tsv": "Weapon",
        "armor-effects.tsv": "Armor",
        "accessory-effects.tsv": "Accessory",
        "skillcard-effects.tsv": "Skill Card"
    }
    
    data_dir = os.path.join(root_dir, "EXTRA DB", "megaten-database-main", "src", "p345", f"{game_id}-data")
    
    if not os.path.exists(data_dir):
        print(f"Directory {data_dir} not found.")
        return []

    for file_name, category in files_map.items():
        file_path = os.path.join(data_dir, file_name)
        if os.path.exists(file_path):
            print(f"  Processing {file_name}...")
            with open(file_path, 'r', encoding='utf-8') as f:
                reader = csv.reader(f, delimiter='\t')
                for row in reader:
                    if not row or len(row) < 2:
                        continue
                    
                    name = row[0].strip()
                    if not name or name.lower() in ["blank", "reserve", "(reserve)", "-"]:
                        continue
                    
                    # Filter out hex-coded dummy items (e.g. 0x120)
                    if name.startswith("0x") and all(c in "0123456789abcdefABCDEFx" for c in name):
                        continue
                        
                    effect = row[1].strip()
                    
                    item = {
                        "name": name,
                        "category": category,
                        "description": effect,
                        "effect": effect,
                        "price": "",
                        "location": "",
                        "attack": None,
                        "accuracy": None
                    }
                    
                    # For weapons/armor, sometimes there are more columns
                    if len(row) >= 4 and category == "Weapon":
                        item["attack"] = row[2].strip()
                        item["accuracy"] = row[3].strip()
                    
                    items.append(item)
        else:
            print(f"  File {file_name} not found.")
            
    return items

root_dir = r"c:\Users\omare\Music\Persona-Companion-App"
output_dir = os.path.join(root_dir, "persona-companion-app", "app", "src", "main", "assets", "data", "items")

# Process P5 and P5R
for game in ["p5"]:
    print(f"Parsing {game} items...")
    items = parse_p5_tsv(root_dir, game)
    if items:
        # Save to p5_items.json and p5r_items.json
        for out_file in ["p5_items.json", "p5r_items.json"]:
            path = os.path.join(output_dir, out_file)
            with open(path, 'w', encoding='utf-8') as f:
                json.dump({"items": items}, f, indent=4)
            print(f"  -> Saved {len(items)} items to {out_file}")

print("P5 Import complete.")
