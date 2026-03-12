import json
import os

# Check all persona data files
persona_files = {
    'P3R': 'app/src/main/assets/data/persona3/reload_personas.json',
    'P3FES': 'app/src/main/assets/data/persona3/fes_personas.json',
    'P3P': 'app/src/main/assets/data/persona3/portable_personas.json',
    'P4': 'app/src/main/assets/data/persona4/personas.json',
    'P4G': 'app/src/main/assets/data/persona4/golden_personas.json',
    'P5': 'app/src/main/assets/data/persona5/personas.json',
    'P5R': 'app/src/main/assets/data/persona5/royal_personas.json'
}

print("=== Persona Duplicate Check ===\n")

for game, filepath in persona_files.items():
    if not os.path.exists(filepath):
        print(f"{game}: File not found - {filepath}")
        continue
    
    with open(filepath, 'r', encoding='utf-8') as f:
        personas = json.load(f)
    
    # Count persona names
    if isinstance(personas, dict):
        persona_names = list(personas.keys())
    else:
        persona_names = [p['name'] for p in personas]
    
    # Check for duplicates
    name_counts = {}
    for name in persona_names:
        name_counts[name] = name_counts.get(name, 0) + 1
    
    duplicates = {name: count for name, count in name_counts.items() if count > 1}
    
    print(f"{game}: {len(persona_names)} personas")
    if duplicates:
        print(f"  ⚠️  Found {len(duplicates)} duplicates:")
        for name, count in sorted(duplicates.items()):
            print(f"    - {name}: appears {count} times")
    else:
        print(f"  ✓ No duplicates")
    print()
