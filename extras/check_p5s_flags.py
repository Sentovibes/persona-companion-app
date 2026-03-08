import json

with open('../app/src/main/assets/data/enemies/p5s_enemies.json', 'r', encoding='utf-8') as f:
    data = json.load(f)

print(f'Total: {len(data)}')
print(f'Bosses: {sum(1 for e in data if e.get("isBoss"))}')
print(f'Mini-bosses: {sum(1 for e in data if e.get("isMiniBoss"))}')
print(f'Regular: {sum(1 for e in data if not e.get("isBoss") and not e.get("isMiniBoss"))}')

print("\nBosses:")
for e in data:
    if e.get("isBoss"):
        print(f"  - {e['name']} (Lv {e['level']})")

print("\nMini-bosses (first 10):")
for i, e in enumerate([e for e in data if e.get("isMiniBoss")][:10]):
    print(f"  - {e['name']} (Lv {e['level']})")
