import json

# Read the JSON file
with open('app/src/main/assets/data/classroom/p5_classroom_answers.json', 'r', encoding='utf-8') as f:
    data = json.load(f)

# Remove P5R Exclusive flags recursively
def remove_exclusive_flags(obj):
    if isinstance(obj, dict):
        # Remove the key if it exists
        if 'P5R Exclusive' in obj:
            del obj['P5R Exclusive']
        # Recurse into nested dicts
        for value in obj.values():
            remove_exclusive_flags(value)
    elif isinstance(obj, list):
        # Recurse into list items
        for item in obj:
            remove_exclusive_flags(item)

remove_exclusive_flags(data)

# Write back with proper formatting
with open('app/src/main/assets/data/classroom/p5_classroom_answers.json', 'w', encoding='utf-8') as f:
    json.dump(data, f, indent=2, ensure_ascii=False)

print("Removed all P5R Exclusive flags")
