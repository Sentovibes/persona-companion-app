import re
import json
import os

def parse_items_txt(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    
    items = []
    current_item = None
    
    # Simple heuristic: lines starting with an item name followed by info
    # Items usually start at a new line and have a specific format in these wiki dumps
    for i, line in enumerate(lines):
        line = line.strip()
        if not line: continue
        
        # Check for headers or noisy lines
        if line.startswith("Item\tInfo") or line.startswith("Buy\tSell") or line.startswith("Contents"):
            continue
            
        # If the line contains a tab, it might be an item definition: Name\tEffect\t...
        parts = line.split('\t')
        if len(parts) >= 2:
            name = parts[0].strip()
            effect = parts[1].strip()
            
            # Simple validation: item names are usually title case and not too long
            if len(name) < 40 and name[0].isupper():
                price = 0
                if len(parts) >= 3:
                    # Try to extract price
                    price_match = re.search(r'(\d[,.\d]*)', parts[2])
                    if price_match:
                        price_str = price_match.group(1).replace(',', '').replace('.', '')
                        try: price = int(price_str)
                        except: pass
                
                items.append({
                    "name": name,
                    "effect": effect,
                    "description": effect, # Duplicate for now
                    "price": price,
                    "category": "General" # Default
                })
    
    return items

def convert_all():
    dest_dir = r"app\src\main\assets\data\items"
    if not os.path.exists(dest_dir):
        os.makedirs(dest_dir)
        
    for file in os.listdir("."):
        if file.endswith("items.txt"):
            game_id = file.split(" ")[0].lower()
            print(f"Parsing {file} for game {game_id}...")
            items = parse_items_txt(file)
            if items:
                with open(os.path.join(dest_dir, f"{game_id}_items.json"), "w", encoding="utf-8") as f:
                    json.dump({"items": items}, f, indent=4)
                print(f"  Generated {len(items)} items.")

if __name__ == "__main__":
    convert_all()
