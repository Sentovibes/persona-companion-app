import json
import os

def standardize_item(item, source_type):
    # Standard schema: name, effect, buy, sell, location
    if source_type == 'p3r':
        return {
            "name": item.get("name", ""),
            "effect": item.get("effect", item.get("desc", "")),
            "buy": item.get("buy", ""),
            "sell": item.get("sell", ""),
            "location": item.get("location", item.get("loc", ""))
        }
    elif source_type == 'p3p':
        return {
            "name": item.get("name", ""),
            "effect": item.get("effect", item.get("info", "")),
            "buy": item.get("buy", ""),
            "sell": item.get("sell", ""),
            "location": item.get("location", "")
        }
    elif source_type == 'p4g':
        return {
            "name": item.get("name", ""),
            "effect": item.get("effect", ""),
            "buy": item.get("buy", ""),
            "sell": item.get("sell", ""),
            "location": item.get("location", "")
        }
    elif source_type == 'p5':
        return {
            "name": item.get("name", ""),
            "effect": item.get("effect", item.get("description", "")),
            "buy": item.get("price", ""),
            "sell": "",
            "location": item.get("location", "")
        }
    return item

def process_file(src_path, out_path, source_type, top_key):
    if not os.path.exists(src_path):
        print(f"Warning: {src_path} not found.")
        return False
    
    with open(src_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    # Identify item list
    items_list = []
    if isinstance(data, list):
        items_list = data
    elif isinstance(data, dict):
        # Look for common list keys or recurse
        def find_list(d):
            if isinstance(d, list): return d
            if isinstance(d, dict):
                for k, v in d.items():
                    res = find_list(v)
                    if res: return res
            return None
        items_list = find_list(data) or []

    standardized = [standardize_item(i, source_type) for i in items_list if isinstance(i, dict) and 'name' in i]
    
    final_data = {top_key: {"all": standardized}}
    
    with open(out_path, 'w', encoding='utf-8') as f:
        json.dump(final_data, f, indent=2)
    print(f"Standardized {src_path} -> {out_path}")
    return True

def main():
    base_path = r"c:\Users\omare\Music\Persona-Companion-App\persona-companion-app"
    asset_dir = os.path.join(base_path, "app", "src", "main", "assets", "data", "items")
    
    # 1. P3P & FES
    process_file(os.path.join(base_path, "p3p items.json"), os.path.join(asset_dir, "p3p_items.json"), "p3p", "persona3PortableItems")
    process_file(os.path.join(base_path, "p3p items.json"), os.path.join(asset_dir, "p3fes_items.json"), "p3p", "persona3FESItems")
    
    # 2. P4G & P4 Base
    process_file(os.path.join(base_path, "p4g items.json"), os.path.join(asset_dir, "p4g_items.json"), "p4g", "persona4GoldenItems")
    process_file(os.path.join(base_path, "p4g items.json"), os.path.join(asset_dir, "p4_items.json"), "p4g", "persona4BaseItems")
    
    # 3. P5 Royal
    process_file(os.path.join(asset_dir, "p5_items.json"), os.path.join(asset_dir, "p5r_items.json"), "p5", "persona5RoyalItems")
    
    # 4. P3 Reload (Using our previously established master as src if p3r_items.json exists as a backup or raw)
    # Actually, let's look for p3rr.json as source for P3R
    process_file(os.path.join(asset_dir, "p3r_items.json"), os.path.join(asset_dir, "p3r_items.json"), "p3r", "persona3ReloadItems")

if __name__ == "__main__":
    main()
