import json
import os
import re

def clean_text(text):
    if not text: return ""
    text = re.sub(r'\[\d+\]', '', text) 
    text = re.sub(r'\s+', ' ', text).strip()
    if text == "-": return ""
    return text

def parse_p5r(content):
    items = []
    lines = content.split('\n')
    current_category = "General"
    for line in lines:
        line = line.strip()
        if not line or "---" in line: continue
        
        if any(cat in line for cat in ["Consumable", "Skill Card", "Treasure", "Material", "Equipment"]):
            current_category = line
            continue
            
        parts = re.split(r'\t+', line)
        if len(parts) >= 3:
            try:
                name = parts[2]
                effect = parts[3] if len(parts) > 3 else ""
                info = parts[4] if len(parts) > 4 else ""
                price = parts[0] if parts[0].isdigit() else None
                
                if name.lower() in ["item", "name", "buy", "price"]: continue
                
                items.append({
                    "name": clean_text(name),
                    "description": clean_text(info),
                    "effect": clean_text(effect),
                    "price": price,
                    "category": current_category,
                    "gameId": "p5r"
                })
            except: continue
    return items

def parse_wiki_style(content, game_id):
    items = []
    lines = content.split('\n')
    current_category = "General"
    
    section_map = {
        "Expendable": "Consumable",
        "Weapons": "Weapon",
        "Armor": "Armor",
        "Accessories": "Accessory",
        "Skill Cards": "Skill Card",
        "Materials": "Material",
        "Itemization": "Itemization"
    }
    
    for line in lines:
        line = line.strip()
        if not line: continue
        
        # Detect category (including === headers)
        header_match = re.search(r'===\s*([^=]+)\s*===', line)
        if header_match:
            cat_name = header_match.group(1).strip()
            if "Consumable" in cat_name: current_category = "Consumable"
            elif "Weapon" in cat_name: current_category = "Weapon"
            elif "Armor" in cat_name: current_category = "Armor"
            elif "Accessory" in cat_name: current_category = "Accessory"
            elif "Skill Card" in cat_name: current_category = "Skill Card"
            elif "Material" in cat_name: current_category = "Material"
            continue
            
        for key, val in section_map.items():
            if key in line and len(line) < 30:
                current_category = val
                break
        
        # Skip junk
        if any(x in line for x in ["Sign In to Save", "Edit", "NakamaSprite", "Note:", "Buy	Sell"]): continue
        
        parts = re.split(r'\t+', line)
        
        if len(parts) >= 2:
            name = parts[0].strip()
            # Skip numbering like "1.1", "4.1.2", etc.
            if re.match(r'^\d+(\.\d+)*$', name): continue
            
            if name.replace(',', '').isdigit() or name.lower() in ["item", "name", "buy", "price"] or len(name) > 50: continue
            
            desc = parts[1]
            price = parts[2] if len(parts) > 2 and parts[2].replace(',', '').isdigit() else None
            sell = parts[3] if len(parts) > 3 and parts[3].replace(',', '').isdigit() else None
            
            items.append({
                "name": clean_text(name),
                "description": clean_text(desc),
                "price": clean_text(price),
                "sellPrice": clean_text(sell),
                "category": current_category,
                "gameId": game_id
            })
            
    return items

def parse_p3p_json(content):
    try:
        data = json.loads(content)
        items = []
        
        cat_map = {
            "Expendables": "Consumable",
            "HP": "Consumable",
            "SP": "Consumable",
            "HP & SP": "Consumable",
            "Revival": "Consumable",
            "Status": "Consumable",
            "Battle": "Consumable",
            "Food": "Consumable",
            "Incense Cards": "Consumable",
            "1h Sword": "Weapon",
            "2h Sword": "Weapon",
            "Spear": "Weapon",
            "Bow": "Weapon",
            "Knife": "Weapon",
            "Fist": "Weapon",
            "Gun": "Weapon",
            "Heavy": "Weapon",
            "Armor (Body)": "Armor",
            "Armor (Feet)": "Armor",
            "Accessories": "Accessory"
        }
        
        for entry in data:
            raw_cat = entry.get("category", "General")
            raw_sub = entry.get("subcategory", "")
            
            category = cat_map.get(raw_cat, cat_map.get(raw_sub, "Other"))
            
            name = entry.get("name_item", entry.get("name_weapon", ""))
            if not name: continue
            
            price_buy = entry.get("price_(¥)_buy", "")
            if isinstance(price_buy, str) and "\n" in price_buy:
                price_buy = price_buy.split("\n")[0]
            
            items.append({
                "name": clean_text(name),
                "description": clean_text(entry.get("info", "")),
                "effect": clean_text(entry.get("effect", entry.get("info", ""))),
                "price": clean_text(str(price_buy)),
                "sellPrice": clean_text(str(entry.get("price_(¥)_sell", ""))),
                "location": clean_text(entry.get("location", "")),
                "attack": clean_text(str(entry.get("attack", ""))) if "attack" in entry else None,
                "accuracy": clean_text(str(entry.get("accuracy", ""))) if "accuracy" in entry else None,
                "category": category,
                "gameId": "p3p"
            })
        return items
    except Exception as e:
        print(f"Error parsing P3P JSON: {e}")
        return []

def main():
    files = {
        "p3p": "p3p items.json",
        "p3fes": "p3fes items.txt"
    }
    
    base_path = r"c:\Users\omare\Music\Persona-Companion-App\persona-companion-app"
    output_dir = os.path.join(base_path, "app", "src", "main", "assets", "data", "items")
    os.makedirs(output_dir, exist_ok=True)
    
    for game_id, filename in files.items():
        file_path = os.path.join(base_path, filename)
        if not os.path.exists(file_path): 
            print(f"Skipping {game_id}: {file_path} not found")
            continue
            
        print(f"Processing {game_id}...")
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
            
        if filename.endswith(".json"):
            items = parse_p3p_json(content)
        else:
            items = parse_wiki_style(content, game_id)
            
        seen = set()
        unique_items = []
        for item in items:
            key = (item["name"].lower(), item["category"])
            if key not in seen:
                unique_items.append(item)
                seen.add(key)
        
        output_file = os.path.join(output_dir, f"{game_id}_items.json")
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump({"items": unique_items}, f, indent=2)
            
        print(f"Generated {output_file} with {len(unique_items)} items")

if __name__ == "__main__":
    main()

