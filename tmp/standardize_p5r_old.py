import json
import os

def standardize_item(item):
    return {
        "name": item.get("name", ""),
        "effect": item.get("effect", item.get("description", "")),
        "buy": item.get("price", ""),
        "sell": "",
        "location": item.get("location", "")
    }

def main():
    base_path = r"c:\Users\omare\Music\Persona-Companion-App\persona-companion-app"
    src_path = os.path.join(base_path, "app", "src", "main", "assets", "data", "items", "p5_items.json")
    out_path = os.path.join(base_path, "app", "src", "main", "assets", "data", "items", "p5r.json")
    
    if os.path.exists(src_path):
        with open(src_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        # P5_items.json has [ "items": [ ... ] ] structure
        items_list = data.get("items", [])
        standardized_items = [standardize_item(item) for item in items_list]
        
        # Keep the persona5RoyalItems top-level key for consistency with our master plan
        final_data = {
            "persona5RoyalItems": {
                "all": standardized_items
            }
        }
        
        with open(out_path, 'w', encoding='utf-8') as f:
            json.dump(final_data, f, indent=2)
        print(f"Standardized P5R using old DB: {out_path}")
    else:
        print(f"Error: {src_path} not found.")

if __name__ == "__main__":
    main()
