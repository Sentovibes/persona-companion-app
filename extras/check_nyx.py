import json

with open('app/src/main/assets/data/enemies/p3r_enemies.json', 'r', encoding='utf-8') as f:
    data = json.load(f)

nyx = [e for e in data if e['name'] == 'Nyx Avatar'][0]
print(f'Nyx Avatar:')
print(f'  isBoss: {nyx.get("isBoss")}')
print(f'  Has phases: {"phases" in nyx}')
print(f'  Phase count: {len(nyx.get("phases", []))}')
if 'phases' in nyx:
    print(f'  Phase names:')
    for p in nyx['phases']:
        print(f'    - {p["name"]}')
