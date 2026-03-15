import json
from pathlib import Path

TOOL = Path('C:/Users/omare/Downloads/megaten-fusion-tool-master/megaten-fusion-tool-master/src/app')
t = TOOL / 'p5/data'
demons = json.load(open(t/'demon-data.json'))
dlc = json.load(open(t/'dlc-data.json'))
party = json.load(open(t/'party-data.json'))
special = json.load(open(t/'special-recipes.json'))
chart = json.load(open(t/'fusion-chart.json'))
element_chart = json.load(open(t/'element-chart.json'))

all_tool = {}
all_tool.update(demons)
all_tool.update(dlc)
all_tool.update(party)
for name in party: all_tool[name]['fusion'] = 'party'
special_names = set(special.keys())
elem_names = {n for n, v in special.items() if isinstance(v, list) and len(v) == 0}

print('Elem names:', elem_names)

# Build ingredient pool
ings = {}
results = {}
for name, p in all_tool.items():
    if p.get('fusion') in ('party', 'special', 'accident'): continue
    if name in elem_names: continue
    r = p.get('race', '')
    if r not in ings: ings[r] = []
    ings[r].append(p['lvl'])
    if name not in special_names:
        if r not in results: results[r] = []
        results[r].append(p['lvl'])
for r in ings: ings[r].sort()
for r in results: results[r].sort()

# Build fission table (triangular for P5)
races = chart['races']
table = chart['table']
ft = {}
for i, raceA in enumerate(races):
    row = table[i] if i < len(table) else []
    # triangular: row i has i+1 columns
    for c, raceR in enumerate(row):
        if not raceR or raceR == '-': continue
        raceB = races[c]
        if i == c: continue
        if raceR not in ft: ft[raceR] = {}
        if raceA not in ft[raceR]: ft[raceR][raceA] = []
        if raceB not in ft[raceR][raceA]: ft[raceR][raceA].append(raceB)

def rev_tool(race, lvl):
    return next((n for n, p in all_tool.items() if p.get('race') == race and p.get('lvl') == lvl
                 and p.get('fusion') not in ('party', 'special', 'accident') and n not in elem_names), None)

# Clotho: Fortune arcana
target = 'Clotho'
p_clotho = all_tool[target]
arcana = p_clotho['race']
lvl = p_clotho['lvl']
print(f'Clotho: arcana={arcana}, lvl={lvl}')
print(f'Fortune results: {sorted(results.get(arcana, []))}')
print(f'Fortune ings: {sorted(ings.get(arcana, []))}')

rlvls = sorted(results.get(arcana, []))
if lvl not in rlvls:
    print('Clotho not in result lvls!')
else:
    idx = rlvls.index(lvl)
    prev_lvl = rlvls[idx-1] if idx > 0 else None
    next_lvl = rlvls[idx+1] if idx+1 < len(rlvls) else None
    cross_min = 2*prev_lvl - 1 if prev_lvl else 0
    cross_max = 2*lvl - 1 if next_lvl else 200
    print(f'cross_min={cross_min}, cross_max={cross_max}')
    print(f'Clotho fissions: {ft.get(arcana, {})}')

    recipes = set()
    for raceA, raceBs in ft.get(arcana, {}).items():
        for lvlA in ings.get(raceA, []):
            for raceB in raceBs:
                if raceA == raceB: continue
                for lvlB in ings.get(raceB, []):
                    if lvlB > cross_min - lvlA and lvlB <= cross_max - lvlA and (raceA != raceB or lvlA < lvlB):
                        nA, nB = rev_tool(raceA, lvlA), rev_tool(raceB, lvlB)
                        if nA and nB: recipes.add((min(nA, nB), max(nA, nB)))
    print(f'Tool Clotho recipes: {len(recipes)}')

# Now check our data
our = json.load(open('web/data/persona5/personas.json', encoding='utf-8-sig'))
our_special = json.load(open('web/data/special-fusions/p5-special.json', encoding='utf-8-sig'))
our_chart = json.load(open('web/data/fusion-charts/p5-fusion-chart.json', encoding='utf-8-sig'))
our_special_names = set(our_special.keys())
our_elem_names = {n for n, v in our_special.items() if isinstance(v, list) and len(v) == 0}

our_ings = {}
our_results = {}
for name, p in our.items():
    if p.get('fusion') in ('party', 'special', 'accident'): continue
    if name in our_elem_names: continue
    r = p.get('arcana') or p.get('race') or ''
    if r not in our_ings: our_ings[r] = []
    our_ings[r].append(p.get('level') or p.get('lvl') or 0)
    if name not in our_special_names:
        if r not in our_results: our_results[r] = []
        our_results[r].append(p.get('level') or p.get('lvl') or 0)
for r in our_ings: our_ings[r].sort()
for r in our_results: our_results[r].sort()

print(f'\nOur Fortune ings: {sorted(our_ings.get(arcana, []))}')
print(f'Our Fortune results: {sorted(our_results.get(arcana, []))}')

# Compare ingredient pools
all_races = set(ings) | set(our_ings)
for race in sorted(all_races):
    t_set = sorted(ings.get(race, []))
    o_set = sorted(our_ings.get(race, []))
    if t_set != o_set:
        print(f'DIFF {race}: tool={t_set} ours={o_set}')
