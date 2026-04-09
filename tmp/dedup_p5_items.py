import json
import os

def dedup_json(file_path):
    if not os.path.exists(file_path):
        print(f"File {file_path} not found.")
        return
    
    with open(file_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    items = data.get("items", [])
    unique_items = []
    seen_signatures = set()
    
    for item in items:
        # Create a signature based on all relevant fields
        signature = (
            item.get("name", "").strip(),
            item.get("category", "").strip(),
            item.get("description", "").strip(),
            item.get("effect", "").strip(),
            item.get("price", "").strip(),
            item.get("location", "").strip()
        )
        
        if signature not in seen_signatures:
            unique_items.append(item)
            seen_signatures.add(signature)
    
    data["items"] = unique_items
    
    with open(file_path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=4, ensure_ascii=False)
    
    print(f"De-duplicated {file_path}: {len(items)} -> {len(unique_items)}")

root = r"c:\Users\omare\Music\Persona-Companion-App\persona-companion-app\app\src\main\assets\data\items"
dedup_json(os.path.join(root, "p5_items.json"))
dedup_json(os.path.join(root, "p5r_items.json"))
