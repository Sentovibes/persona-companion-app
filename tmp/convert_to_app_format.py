"""
Convert the user's clean root JSON files into the flat format the Android app expects.

App expects: { "items": [ { "name": "...", "description": "...", "effect": "...", "price": "...", "category": "..." } ] }

Root files have nested categories with fields: name, info, buy, sell, loc/location
"""
import json
import os

BASE = r"c:\Users\omare\Music\Persona-Companion-App\persona-companion-app"
ASSETS = os.path.join(BASE, "app", "src", "main", "assets", "data", "items")

def flatten_json(data, category_path=""):
    """Recursively walk nested JSON and extract all item objects."""
    items = []
    if isinstance(data, list):
        for item in data:
            if isinstance(item, dict) and ("name" in item or "n" in item):
                items.append((item, category_path))
            elif isinstance(item, (dict, list)):
                items.extend(flatten_json(item, category_path))
    elif isinstance(data, dict):
        for key, value in data.items():
            new_path = key if not category_path else f"{category_path} > {key}"
            if isinstance(value, (dict, list)):
                items.extend(flatten_json(value, new_path))
    return items

def convert_item(item, category):
    """Convert a root-format item to the app's expected format with UI mapping."""
    # Prioritize internal 'category' field if present (for flat list JSONs)
    internal_cat = item.get("category", "")
    effective_cat = internal_cat if internal_cat else category
    
    # Handle massive variety of field names across games
    name = item.get("name_item", item.get("n", item.get("name", "")))
    info = item.get("description", item.get("d", item.get("info", item.get("desc", ""))))
    
    # Price handling (P5R: price, P4G: buy/sell, P3FES: price_(¥)_buy)
    buy = item.get("price_(¥)_buy", item.get("price", item.get("buy", "")))
    sell = item.get("price_(¥)_sell", item.get("sell", ""))
    
    loc = item.get("location", item.get("loc", ""))
    
    # Map to app fields
    price_str = ""
    if buy and buy != "-":
        price_str = str(buy).replace("\n", " / ")
    if sell and sell != "-":
        price_str += f" | Sell: {sell}" if price_str else f"Sell: {sell}"
    
    # Map category strings to App UI Categories:
    # ["Consumable", "Weapon", "Armor", "Accessory", "Skill Card", "Other"]
    cat_lower = str(effective_cat).lower()
    
    if any(k in cat_lower for k in ["consumable", "expendable", "recovery", "hp", "sp", "item", "food", "medicine"]):
        final_cat = "Consumable"
    elif any(k in cat_lower for k in ["weapon", "melee", "ranged", "blade", "sword", "gun", "bow", "spear", "axe", "club", "fist"]):
        final_cat = "Weapon"
    elif any(k in cat_lower for k in ["armor", "protector", "clothe", "suit", "guard"]):
        final_cat = "Armor"
    elif any(k in cat_lower for k in ["accessory", "accessories", "jewelry", "ring", "relic", "badge", "charm"]):
        final_cat = "Accessory"
    elif any(k in cat_lower for k in ["skill card", "cards"]):
        final_cat = "Skill Card"
    else:
        final_cat = "Other"
    
    return {
        "name": name,
        "description": info,
        "effect": info,
        "price": price_str,
        "location": str(loc) if loc else "",
        "category": final_cat
    }

def process_file(src_path, out_path, label):
    """Read a root JSON, flatten it, convert to app format, write to assets."""
    if not os.path.exists(src_path):
        print(f"  SKIP: {src_path} not found")
        return
    
    with open(src_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    raw_items = flatten_json(data)
    converted = [convert_item(item, cat) for item, cat in raw_items]
    
    output = {"items": converted}
    
    with open(out_path, 'w', encoding='utf-8') as f:
        json.dump(output, f, indent=2, ensure_ascii=False)
    
    print(f"  {label}: {len(converted)} items -> {out_path}")

def main():
    print("=== Converting clean root JSONs to app format ===\n")
    
    # P5 Royal
    process_file(
        os.path.join(BASE, "p5r.json"),
        os.path.join(ASSETS, "p5r_items.json"),
        "P5R"
    )
    
    # P4 Golden
    process_file(
        os.path.join(BASE, "p4g items.json"),
        os.path.join(ASSETS, "p4g_items.json"),
        "P4G"
    )
    
    # P4 Base = P4G
    process_file(
        os.path.join(BASE, "p4g items.json"),
        os.path.join(ASSETS, "p4_items.json"),
        "P4 (from P4G)"
    )
    
    # P3 Portable
    process_file(
        os.path.join(BASE, "p3p items.json"),
        os.path.join(ASSETS, "p3p_items.json"),
        "P3P"
    )
    
    # P3 FES = P3P
    process_file(
        os.path.join(BASE, "p3p items.json"),
        os.path.join(ASSETS, "p3fes_items.json"),
        "P3FES (from P3P)"
    )
    
    # P3R - from p3rr.json (user fixed it to valid JSON)
    process_file(
        os.path.join(BASE, "p3rr.json"),
        os.path.join(ASSETS, "p3r_items.json"),
        "P3R"
    )
    
    print("\n=== Done! All assets converted to app format. ===")

if __name__ == "__main__":
    main()
