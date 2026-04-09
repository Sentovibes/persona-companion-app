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
        
        # Detect category
        for key, val in section_map.items():
            if key in line and len(line) < 30:
                current_category = val
                break
        
        # Skip junk
        if any(x in line for x in ["Sign In to Save", "Edit", "NakamaSprite", "Note:", "Buy	Sell"]): continue
        
        parts = re.split(r'\t+', line)
        
        # Equipment Table Heuristic (mostly P4 Armor/Weapons)
        # Typically: Name, User/Type, Def, Eva, Stats, Desc, Price...
        if len(parts) >= 5:
            name = parts[0]
            # Validation: Name shouldn't be a number or a header
            if name.replace(',', '').isdigit() or name.lower() in ["item", "name", "buy", "price"]: continue
            
            user = parts[1]
            if user in ["Males", "Females", "Unisex", "Protagonist", "Yosuke", "Chie", "Yukiko", "Kanji", "Teddie", "Naoto"]:
                # equipment format
                stats = parts[4] if len(parts) > 4 else ""
                desc = parts[5] if len(parts) > 5 else ""
                price = parts[6] if len(parts) > 6 and parts[6].replace(',', '').isdigit() else None
                loc = parts[-1] if len(parts) > 7 else ""
            else:
                # simple table format
                desc = parts[1]
                loc = parts[2] if len(parts) > 2 else ""
                price = parts[3] if len(parts) > 3 and parts[3].replace(',', '').isdigit() else None
                stats = ""

            is_expansion = any(x in line for x in ["Episode Aigis", "The Answer", "Metis"])
            
            items.append({
                "name": clean_text(name),
                "description": clean_text(desc),
                "stats": clean_text(stats),
                "price": price,
                "location": clean_text(loc),
                "category": current_category,
                "gameId": game_id,
                "episodeAigis": is_expansion
            })
            
        elif len(parts) >= 2 or " - " in line:
            if " - " in line and len(parts) < 2:
                p = line.split(" - ", 1)
            else:
                p = parts
            
            name = p[0].strip()
            if name.replace(',', '').isdigit() or name.lower() in ["item", "name", "buy", "price"] or len(name) > 50: continue
            
            desc = p[1].strip() if len(p) > 1 else ""
            is_expansion = any(x in line for x in ["Episode Aigis", "The Answer", "Metis"])
            
            items.append({
                "name": clean_text(name),
                "description": clean_text(desc),
                "category": current_category,
                "gameId": game_id,
                "episodeAigis": is_expansion
            })
            
    return items

def main():
    files = {
        "p5r": "p5r itens.txt",
        "p3r": "p3r items.txt",
        "p3p": "p3p items.txt",
        "p3fes": "p3fes items.txt",
        "p4": "persona 4 items (not golden).txt",
        "p4g": "persona 4 golden items(not base).txt"
    }
    
    base_path = r"c:\Users\omare\Music\Persona-Companion-App\persona-companion-app"
    output_dir = os.path.join(base_path, "app", "src", "main", "assets", "data", "items")
    os.makedirs(output_dir, exist_ok=True)
    
    for game_id, filename in files.items():
        file_path = os.path.join(base_path, filename)
        if not os.path.exists(file_path): continue
            
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
            
        if game_id == "p5r":
            items = parse_p5r(content)
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
