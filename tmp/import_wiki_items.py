import json
import re
import os

def parse_wiki_txt(file_path):
    current_category = "General"
    item_map = {}
    last_item = None
    headers = None
    
    with open(file_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    
    for i, line_raw in enumerate(lines):
        line = line_raw.strip()
        if not line:
            continue
            
        # Detect Categories
        new_cat = None
        if any(x in line for x in ["Expendables", "Expendable", "Items", "Consumables"]):
            new_cat = "Consumable"
        elif any(x in line for x in ["Weapons", "Melee", "Ranged"]):
            new_cat = "Weapon"
        elif any(x in line for x in ["Armor", "Body Armor", "Leg Armor", "Clothes"]):
            new_cat = "Armor"
        elif any(x in line for x in ["Accessories", "Accessories"]):
            new_cat = "Accessory"
        elif any(x in line for x in ["Skill Cards", "Incense Cards"]):
            new_cat = "Skill Card"
        elif any(x in line for x in ["Quest Items", "Materials", "Key Items", "Valuables"]):
            new_cat = "Other"
            
        if new_cat:
            current_category = new_cat
            headers = None
            continue

        # Look for headers
        # P3FES: Item Info Price ($) Location
        is_header = any(x in line for x in ["Item", "Name"]) and any(x in line for x in ["Description", "Info", "Obtained", "Location", "Usage"])
        if is_header:
            parts = line.split('\t')
            headers = [h.strip().lower() for h in parts]
            continue
            
        if headers:
            parts = line_raw.strip('\n').split('\t')
            
            # Continuation check: line starts with tab or is just one or two parts that look like info
            # Or if it's explicitly a sub-header line like "Buy Sell" in FES
            if line.lower() in ["buy", "sell", "buy\tsell", "buy sell"]:
                continue

            if (line_raw.startswith('\t') or len(parts) < 2) and last_item:
                extra = line.strip()
                if extra:
                    # Don't add if it looks like a header
                    if extra.lower() in ["item", "info", "price", "location", "buy", "sell", "range", "attack", "accuracy"]:
                        continue
                        
                    if last_item["location"]:
                        if extra not in last_item["location"]:
                            last_item["location"] += " | " + extra
                    else:
                        last_item["location"] = extra
                continue

            if len(parts) >= 2:
                name = parts[0].strip()
                # Sanitization: Strip corrupted characters and common artifacts
                name = re.sub(r'[\u220e\u2588\u25a0]', '', name) # Remove blocks like ■ and █
                name = re.sub(r'\s{2,}', ' ', name).strip() # Normalize whitespace
                
                # Filter out garbage
                if not name or name.lower() in ["buy", "sell", "in battle", "outside", "usage", "item", "name", "price", "location", "info"]:
                    continue
                
                # Heuristic for Persona 4/3: If name looks like a price (contains commas or only digits) 
                # and we have a last_item, treat it as continuation if the previous line was short.
                is_price_like = re.match(r'^[\d,¥\-/ ]+$', name) is not None
                if is_price_like and last_item:
                    # Merge these parts into price/location/etc.
                    # We skip defining a new item and just let the header-loop below update last_item
                    item = last_item
                else:
                    if name.startswith("(") and name.endswith(")"): # Skip (Reserved) items
                        continue
                    
                    # Refine categorization (e.g. books shouldn't be weapons)
                    final_category = current_category
                    lower_name = name.lower()
                    desc_lower = parts[1].lower() if len(parts) > 1 else ""
                    if any(x in lower_name for x in ["book", "guide", "magazine", "reading"]):
                        final_category = "Other"
                    elif any(x in desc_lower for x in ["read ", "reading ", " study "]):
                        final_category = "Other"

                    # De-duplicate: use existing item or create new
                    if name in item_map:
                        item = item_map[name]
                    else:
                        item = {
                            "name": name,
                            "category": final_category,
                            "description": "",
                            "effect": "",
                            "price": "",
                            "sellPrice": "",
                            "location": "",
                            "attack": None,
                            "accuracy": None
                        }
                        item_map[name] = item
                
                last_item = item
                
                # Dynamic mapping based on headers
                for h_idx, h_name in enumerate(headers):
                    if h_idx >= len(parts): break
                    val = parts[h_idx].strip()
                    if not val: continue
                    
                    # Also sanitize the values (description/location)
                    val = re.sub(r'[\u220e\u2588\u25a0]', '', val)
                    val = re.sub(r'\s{2,}', ' ', val).strip()

                    if ("item" in h_name or "name" in h_name) and not is_price_like:
                        item["name"] = val
                    elif "description" in h_name or "info" in h_name or "effect" in h_name:
                        if not item["description"] or len(val) > len(item["description"]):
                            item["description"] = val
                    elif "obtained" in h_name or "location" in h_name:
                        if item["location"] and val not in item["location"]:
                            item["location"] += " | " + val
                        else:
                            item["location"] = val
                    elif "price" in h_name or "buy" in h_name:
                        item["price"] = val
                    elif "sell" in h_name:
                        item["sellPrice"] = val
                    elif "attack" in h_name:
                        item["attack"] = val
                    elif "accuracy" in h_name:
                        item["accuracy"] = val
                
                # P3R specific: Handle multi-column usage if header present
                if headers and "usage" in headers and len(parts) >= 4:
                    d_idx = 3 # desc
                    l_idx = 4 # loc
                    if len(parts) > d_idx: 
                        desc = parts[d_idx].strip()
                        if desc and len(desc) > len(item["description"] or ""):
                            item["description"] = desc
                    if len(parts) > l_idx:
                        loc = parts[l_idx].strip()
                        if loc and loc not in (item["location"] or ""):
                            item["location"] = (item["location"] + " | " + loc) if item["location"] else loc

                # P3FES specific: Handle shifted price/location across lines
                # If name is 'Medicine' and next line starts with '3,980' or similar
                # This is hard to do here without looking ahead, but the multi-line logic already catches some.

    # Pre-save de-duplication: items with same name, category, and effect/location
    unique_items = []
    seen_signatures = set()
    for item in item_map.values():
        signature = (item["name"], item["category"], item["description"], item["location"])
        if signature not in seen_signatures:
            unique_items.append(item)
            seen_signatures.add(signature)

    return unique_items

# Run import
source_files = {
    "p3p_items.json": "p3p items.txt",
    "p3fes_items.json": "p3fes items.txt",
    "p3r_items.json": "p3r items.txt",
    "p4_items.json": "persona 4 items (not golden).txt",
    "p4g_items.json": "persona 4 golden items(not base).txt"
}

root_dir = r"c:\Users\omare\Music\Persona-Companion-App\persona-companion-app"
output_dir = os.path.join(root_dir, "app", "src", "main", "assets", "data", "items")

os.makedirs(output_dir, exist_ok=True)

for json_name, txt_name in source_files.items():
    txt_path = os.path.join(root_dir, txt_name)
    if os.path.exists(txt_path):
        print(f"Parsing {txt_name}...")
        parsed_items = parse_wiki_txt(txt_path)
        output_path = os.path.join(output_dir, json_name)
        with open(output_path, 'w', encoding='utf-8') as jf:
            json.dump({"items": parsed_items}, jf, indent=4)
        print(f"  -> Saved {len(parsed_items)} items to {json_name}")
    else:
        print(f"Warning: {txt_name} not found.")

print("Import complete.")
