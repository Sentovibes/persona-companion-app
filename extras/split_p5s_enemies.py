import json

# Load complete P5S enemy data
with open('p5s_enemies_complete.json', 'r', encoding='utf-8') as f:
    all_enemies = json.load(f)

# Split into categories
regular_enemies = [e for e in all_enemies if not e['isBoss'] and not e['isMiniBoss']]
mini_bosses = [e for e in all_enemies if e['isMiniBoss']]
main_bosses = [e for e in all_enemies if e['isBoss']]

# Save separate files
with open('p5s_enemies.json', 'w', encoding='utf-8') as f:
    json.dump(regular_enemies, f, indent=2, ensure_ascii=False)

with open('p5s_minibosses.json', 'w', encoding='utf-8') as f:
    json.dump(mini_bosses, f, indent=2, ensure_ascii=False)

with open('p5s_bosses.json', 'w', encoding='utf-8') as f:
    json.dump(main_bosses, f, indent=2, ensure_ascii=False)

print(f"✓ Split P5S enemies into separate files:")
print(f"  - p5s_enemies.json: {len(regular_enemies)} regular enemies")
print(f"  - p5s_minibosses.json: {len(mini_bosses)} mini-bosses")
print(f"  - p5s_bosses.json: {len(main_bosses)} main bosses")
