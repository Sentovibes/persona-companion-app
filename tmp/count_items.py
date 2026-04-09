import json, os

assets = r"c:\Users\omare\Music\Persona-Companion-App\persona-companion-app\app\src\main\assets\data\items"
root = r"c:\Users\omare\Music\Persona-Companion-App\persona-companion-app"

def count_nested(data):
    total = 0
    if isinstance(data, list):
        for item in data:
            if isinstance(item, dict) and "name" in item:
                total += 1
            elif isinstance(item, (dict, list)):
                total += count_nested(item)
    elif isinstance(data, dict):
        for v in data.values():
            total += count_nested(v)
    return total

print("=== ASSET FILES (what app loads) ===")
for f in sorted(os.listdir(assets)):
    if f.endswith(".json"):
        with open(os.path.join(assets, f), "r", encoding="utf-8") as fh:
            try:
                data = json.load(fh)
                count = len(data.get("items", []))
                print(f"  {f}: {count} items")
            except:
                print(f"  {f}: PARSE ERROR")

print()
print("=== ROOT FILES (your master sources) ===")
for f in ["p5r.json", "p4g items.json", "p3p items.json"]:
    path = os.path.join(root, f)
    if os.path.exists(path):
        with open(path, "r", encoding="utf-8") as fh:
            data = json.load(fh)
        print(f"  {f}: {count_nested(data)} items")

# Check old p5_items.json
old = os.path.join(assets, "p5_items.json")
if os.path.exists(old):
    with open(old, "r", encoding="utf-8") as fh:
        data = json.load(fh)
    print(f"  p5_items.json (OLD DB): {len(data.get('items', []))} items")
