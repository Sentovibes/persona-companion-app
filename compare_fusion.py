"""
Full fusion recipe comparison: Our app vs megaten-fusion-tool
Covers: p3fes, p3p, p3r, p4, p4g, p5, p5r
"""
import json, os, sys
from pathlib import Path

TOOL = Path("C:/Users/omare/Downloads/megaten-fusion-tool-master/megaten-fusion-tool-master/src/app")
OUR_WEB = Path("web/data")

def load(path, enc='utf-8'):
    with open(path, encoding=enc) as f:
        return json.load(f)

def load_bom(path):
    try:
        return load(path, 'utf-8-sig')
    except:
        return load(path, 'utf-8')

# ── Core algorithm (mirrors per-fusion-chart.ts + smt/per-nonelem-fissions.ts) ──

def build_fission_table(races, full_table, triangular=False):
    """Upper triangle for square charts, full table for triangular charts."""
    ft = {}
    for i, raceA in enumerate(races):
        row = full_table[i] if i < len(full_table) else []
        if triangular:
            # triangular: row i has i+1 columns, cOffset=0, raceB=races[c]
            for c, raceR in enumerate(row):
                if not raceR or raceR == '-': continue
                raceB = races[c]
                if i == c: continue  # diagonal
                if raceR not in ft: ft[raceR] = {}
                if raceA not in ft[raceR]: ft[raceR][raceA] = []
                if raceB not in ft[raceR][raceA]: ft[raceR][raceA].append(raceB)
        else:
            # square: upper triangle only (j >= i, skip diagonal)
            for j in range(i, len(races)):
                if j == i: continue
                raceR = row[j] if j < len(row) else None
                if not raceR or raceR == '-': continue
                raceB = races[j]
                if raceR not in ft: ft[raceR] = {}
                if raceA not in ft[raceR]: ft[raceR][raceA] = []
                if raceB not in ft[raceR][raceA]: ft[raceR][raceA].append(raceB)
    return ft

def calc_recipes(personas, special_names, chart, special_data, triangular=False):
    """Returns {name: recipe_count} for all non-special personas."""
    NON_FUSABLE = {'party', 'accident', 'special'}
    # Element demons: in special_data with empty list
    elem_names = {n for n, v in special_data.items() if isinstance(v, list) and len(v) == 0}
    by_arcana = {}
    for name, p in personas.items():
        if p.get('fusion') in NON_FUSABLE: continue
        a = p.get('arcana') or p.get('race') or ''
        if a not in by_arcana: by_arcana[a] = []
        lvl = p.get('level') or p.get('lvl') or 0
        by_arcana[a].append({'name': name, 'level': lvl})
    for a in by_arcana:
        by_arcana[a].sort(key=lambda x: x['level'])

    races = chart['races']
    full_table = chart['table']
    ft = build_fission_table(races, full_table, triangular)

    def result_lvls(arcana):
        return sorted([p['level'] for p in by_arcana.get(arcana, []) if p['name'] not in special_names])
    def ing_lvls(arcana):
        return sorted([p['level'] for p in by_arcana.get(arcana, []) if p.get('fusion') != 'party' and p['name'] not in elem_names])
    def rev(arcana, lvl):
        return next((p['name'] for p in by_arcana.get(arcana, []) if p['level'] == lvl), None)

    counts = {}
    for name, p in personas.items():
        if p.get('fusion') in NON_FUSABLE: continue
        if name in special_names:
            val = special_data.get(name, [])
            # Our format: [[ing1, ing2, ...]] — each ingredient is a separate recipe
            counts[name] = len(val[0]) if val and isinstance(val[0], list) else len(val)
            continue

        arcana = p.get('arcana') or p.get('race') or ''
        lvl = p.get('level') or p.get('lvl') or 0
        rlvls = result_lvls(arcana)
        if lvl not in rlvls:
            counts[name] = 0
            continue
        idx = rlvls.index(lvl)
        recipes = set()

        # same-arcana
        same_min = 2 * (lvl - 1)
        same_max = 2 * (rlvls[idx+1] - 1) if idx+1 < len(rlvls) else 200
        same_next = 2 * (rlvls[idx+2] - 1) if idx+2 < len(rlvls) else 200
        ilvls = [l for l in ing_lvls(arcana) if l != lvl]
        ing_m = same_max // 2 + 1
        for l2 in ilvls:
            if ing_m < l2 and ing_m + l2 < same_next:
                n1, n2 = rev(arcana, ing_m), rev(arcana, l2)
                if n1 and n2: recipes.add((min(n1,n2), max(n1,n2)))
        for i in range(len(ilvls)):
            for j in range(i+1, len(ilvls)):
                s = ilvls[i] + ilvls[j]
                if same_min <= s < same_max:
                    n1, n2 = rev(arcana, ilvls[i]), rev(arcana, ilvls[j])
                    if n1 and n2: recipes.add((min(n1,n2), max(n1,n2)))

        # cross-arcana
        prev_lvl = rlvls[idx-1] if idx > 0 else None
        next_lvl = rlvls[idx+1] if idx+1 < len(rlvls) else None
        cross_min = 2*prev_lvl - 1 if prev_lvl else 0
        cross_max = 2*lvl - 1 if next_lvl else 200
        for raceA, raceBs in ft.get(arcana, {}).items():
            for lvlA in ing_lvls(raceA):
                for raceB in raceBs:
                    if raceA == raceB: continue
                    for lvlB in ing_lvls(raceB):
                        if lvlB > cross_min - lvlA and lvlB <= cross_max - lvlA and (raceA != raceB or lvlA < lvlB):
                            nA, nB = rev(raceA, lvlA), rev(raceB, lvlB)
                            if nA and nB: recipes.add((min(nA,nB), max(nA,nB)))

        counts[name] = len(recipes)
    return counts

# ── Tool-side simulation ──

def tool_calc(tool_personas, special_names, chart, special_data, triangular=False):
    """Same algorithm but using tool's persona data (lvl field)."""
    NON_FUSABLE = {'party', 'accident', 'special'}
    # Element demons: in special_data with empty list
    elem_names = {n for n, v in special_data.items() if isinstance(v, list) and len(v) == 0}
    by_arcana = {}
    for name, p in tool_personas.items():
        if p.get('fusion') in NON_FUSABLE: continue
        a = p.get('race') or ''
        if a not in by_arcana: by_arcana[a] = []
        lvl = p.get('lvl') or p.get('level') or 0
        by_arcana[a].append({'name': name, 'level': lvl})
    for a in by_arcana:
        by_arcana[a].sort(key=lambda x: x['level'])

    races = chart['races']
    full_table = chart['table']
    ft = build_fission_table(races, full_table, triangular)

    def result_lvls(arcana):
        return sorted([p['level'] for p in by_arcana.get(arcana, []) if p['name'] not in special_names])
    def ing_lvls(arcana):
        return sorted([p['level'] for p in by_arcana.get(arcana, []) if p['name'] not in elem_names])
    def rev(arcana, lvl):
        return next((p['name'] for p in by_arcana.get(arcana, []) if p['level'] == lvl), None)

    counts = {}
    for name, p in tool_personas.items():
        if p.get('fusion') in NON_FUSABLE: continue
        if name in special_names:
            val = special_data.get(name, [])
            # Tool format: [ing1, ing2, ...] — flat list, each ingredient is a separate recipe
            counts[name] = len(val[0]) if val and isinstance(val[0], list) else len(val)
            continue
        arcana = p.get('race') or ''
        lvl = p.get('lvl') or p.get('level') or 0
        rlvls = result_lvls(arcana)
        if lvl not in rlvls:
            counts[name] = 0
            continue
        idx = rlvls.index(lvl)
        recipes = set()

        same_min = 2*(lvl-1)
        same_max = 2*(rlvls[idx+1]-1) if idx+1 < len(rlvls) else 200
        same_next = 2*(rlvls[idx+2]-1) if idx+2 < len(rlvls) else 200
        ilvls = [l for l in ing_lvls(arcana) if l != lvl]
        ing_m = same_max//2 + 1
        for l2 in ilvls:
            if ing_m < l2 and ing_m + l2 < same_next:
                n1, n2 = rev(arcana, ing_m), rev(arcana, l2)
                if n1 and n2: recipes.add((min(n1,n2), max(n1,n2)))
        for i in range(len(ilvls)):
            for j in range(i+1, len(ilvls)):
                s = ilvls[i]+ilvls[j]
                if same_min <= s < same_max:
                    n1, n2 = rev(arcana, ilvls[i]), rev(arcana, ilvls[j])
                    if n1 and n2: recipes.add((min(n1,n2), max(n1,n2)))

        prev_lvl = rlvls[idx-1] if idx > 0 else None
        next_lvl = rlvls[idx+1] if idx+1 < len(rlvls) else None
        cross_min = 2*prev_lvl-1 if prev_lvl else 0
        cross_max = 2*lvl-1 if next_lvl else 200
        for raceA, raceBs in ft.get(arcana, {}).items():
            for lvlA in ing_lvls(raceA):
                for raceB in raceBs:
                    if raceA == raceB: continue
                    for lvlB in ing_lvls(raceB):
                        if lvlB > cross_min-lvlA and lvlB <= cross_max-lvlA and (raceA != raceB or lvlA < lvlB):
                            nA, nB = rev(raceA, lvlA), rev(raceB, lvlB)
                            if nA and nB: recipes.add((min(nA,nB), max(nA,nB)))
        counts[name] = len(recipes)
    return counts

def compare(game, our_counts, tool_counts):
    all_names = set(our_counts) | set(tool_counts)
    mismatches = []
    for name in sorted(all_names):
        ours = our_counts.get(name)
        theirs = tool_counts.get(name)
        if ours != theirs:
            mismatches.append((name, ours, theirs))
    total = len(all_names)
    matched = total - len(mismatches)
    print(f"\n{'='*60}")
    print(f"  {game}: {matched}/{total} personas match")
    print(f"{'='*60}")
    if mismatches:
        print(f"  {'Persona':<30} {'Ours':>6} {'Tool':>6}")
        print(f"  {'-'*44}")
        for name, ours, theirs in mismatches[:30]:
            o = str(ours) if ours is not None else 'N/A'
            t = str(theirs) if theirs is not None else 'N/A'
            print(f"  {name:<30} {o:>6} {t:>6}")
        if len(mismatches) > 30:
            print(f"  ... and {len(mismatches)-30} more")
    else:
        print("  All match!")
    return len(mismatches)

# ══════════════════════════════════════════════════════════════
# P3FES
# ══════════════════════════════════════════════════════════════
def run_p3fes():
    print("\n>>> P3FES")
    t = TOOL / 'p3/data'
    van = load(t/'van-demon-data.json')
    fes = load(t/'fes-demon-data.json')
    fes_party = load(t/'fes-party-data.json')
    van_special = load(t/'van-special-recipes.json')
    fes_special = load(t/'fes-special-recipes.json')
    pair_special = load(t/'pair-special-recipes.json')
    chart = load(t/'fes-fusion-chart.json')

    for k,v in pair_special.items(): van_special[k] = v
    for k,v in van_special.items():
        if k not in fes_special: fes_special[k] = v
    special_names = set(fes_special.keys())

    tool_personas = {}
    tool_personas.update(van)
    tool_personas.update(fes)
    tool_personas.update(fes_party)
    for name in fes_party: tool_personas[name]['fusion'] = 'party'

    our_personas = load_bom(OUR_WEB/'persona3/personas.json')
    our_special = load_bom(OUR_WEB/'special-fusions/p3-special.json')
    our_chart = load_bom(OUR_WEB/'fusion-charts/p3-fusion-chart.json')
    our_special_names = set(our_special.keys())

    our_counts = calc_recipes(our_personas, our_special_names, our_chart, our_special, triangular=False)
    tool_counts = tool_calc(tool_personas, special_names, chart, fes_special, triangular=False)
    return compare('P3FES', our_counts, tool_counts)

# ══════════════════════════════════════════════════════════════
# P3P
# ══════════════════════════════════════════════════════════════
def run_p3p():
    print("\n>>> P3P")
    t = TOOL / 'p3/data'
    van = load(t/'van-demon-data.json')
    fes = load(t/'fes-demon-data.json')
    p3p_data = load(t/'p3p-demon-data.json')
    p3p_party = load(t/'p3p-party-data.json')
    fes_party = load(t/'fes-party-data.json')
    van_special = load(t/'van-special-recipes.json')
    fes_special = load(t/'fes-special-recipes.json')
    pair_special = load(t/'pair-special-recipes.json')
    chart = load(t/'fes-fusion-chart.json')

    for k,v in pair_special.items(): van_special[k] = v
    for k,v in van_special.items():
        if k not in fes_special: fes_special[k] = v
    special_names = set(fes_special.keys())

    tool_personas = {}
    tool_personas.update(van)
    tool_personas.update(fes)
    tool_personas.update(p3p_data)
    tool_personas.update(fes_party)
    tool_personas.update(p3p_party)
    for name in fes_party: tool_personas[name]['fusion'] = 'party'
    for name in p3p_party: tool_personas[name]['fusion'] = 'party'

    our_personas = load_bom(OUR_WEB/'persona3/portable_personas.json')
    our_special = load_bom(OUR_WEB/'special-fusions/p3-special.json')
    our_chart = load_bom(OUR_WEB/'fusion-charts/p3p-fusion-chart.json')
    our_special_names = set(our_special.keys())

    our_counts = calc_recipes(our_personas, our_special_names, our_chart, our_special, triangular=False)
    tool_counts = tool_calc(tool_personas, special_names, chart, fes_special, triangular=False)
    return compare('P3P', our_counts, tool_counts)

# ══════════════════════════════════════════════════════════════
# P3R
# ══════════════════════════════════════════════════════════════
def run_p3r():
    print("\n>>> P3R")
    t = TOOL / 'p3r/data'
    demons = load(t/'demon-data.json')
    party = load(t/'party-data.json')
    special = load(t/'special-recipes.json')
    chart = load(t/'fusion-chart.json')

    special_names = set(special.keys())
    tool_personas = {}
    tool_personas.update(demons)
    tool_personas.update(party)
    for name in party: tool_personas[name]['fusion'] = 'party'

    our_personas = load_bom(OUR_WEB/'persona3/reload_personas.json')
    our_special = load_bom(OUR_WEB/'special-fusions/p3r-special.json')
    our_chart = load_bom(OUR_WEB/'fusion-charts/p3r-fusion-chart.json')
    our_special_names = set(our_special.keys())

    # P3R uses triangular chart
    our_counts = calc_recipes(our_personas, our_special_names, our_chart, our_special, triangular=True)
    tool_counts = tool_calc(tool_personas, special_names, chart, special, triangular=True)
    return compare('P3R', our_counts, tool_counts)

# ══════════════════════════════════════════════════════════════
# P4
# ══════════════════════════════════════════════════════════════
def run_p4():
    print("\n>>> P4")
    t = TOOL / 'p4/data'
    demons = load(t/'demon-data.json')
    party = load(t/'party-data.json')
    special = load(t/'special-recipes.json')
    chart = load(t/'fusion-chart.json')

    special_names = set(special.keys())
    tool_personas = {}
    tool_personas.update(demons)
    tool_personas.update(party)
    for name in party: tool_personas[name]['fusion'] = 'party'

    our_personas = load_bom(OUR_WEB/'persona4/personas.json')
    our_special = load_bom(OUR_WEB/'special-fusions/p4-special.json')
    our_chart = load_bom(OUR_WEB/'fusion-charts/p4-base-fusion-chart.json')
    our_special_names = set(our_special.keys())

    our_counts = calc_recipes(our_personas, our_special_names, our_chart, our_special, triangular=False)
    tool_counts = tool_calc(tool_personas, special_names, chart, special, triangular=False)
    return compare('P4', our_counts, tool_counts)

# ══════════════════════════════════════════════════════════════
# P4G
# ══════════════════════════════════════════════════════════════
def run_p4g():
    print("\n>>> P4G")
    t = TOOL / 'p4/data'
    demons = load(t/'demon-data.json')
    golden = load(t/'golden-demon-data.json')
    party = load(t/'party-data.json')
    golden_party = load(t/'golden-party-data.json')
    special = load(t/'special-recipes.json')
    chart = load(t/'golden-fusion-chart.json')

    special_names = set(special.keys())
    tool_personas = {}
    tool_personas.update(demons)
    tool_personas.update(golden)
    tool_personas.update(party)
    tool_personas.update(golden_party)
    for name in party: tool_personas[name]['fusion'] = 'party'
    for name in golden_party: tool_personas[name]['fusion'] = 'party'

    our_personas = load_bom(OUR_WEB/'persona4/golden_personas.json')
    our_special = load_bom(OUR_WEB/'special-fusions/p4-special.json')
    our_chart = load_bom(OUR_WEB/'fusion-charts/p4-fusion-chart.json')
    our_special_names = set(our_special.keys())

    our_counts = calc_recipes(our_personas, our_special_names, our_chart, our_special, triangular=False)
    tool_counts = tool_calc(tool_personas, special_names, chart, special, triangular=False)
    return compare('P4G', our_counts, tool_counts)

# ══════════════════════════════════════════════════════════════
# P5
# ══════════════════════════════════════════════════════════════
def run_p5():
    print("\n>>> P5")
    t = TOOL / 'p5/data'
    demons = load(t/'demon-data.json')
    dlc = load(t/'dlc-data.json')
    party = load(t/'party-data.json')
    special = load(t/'special-recipes.json')
    chart = load(t/'fusion-chart.json')

    special_names = set(special.keys())
    tool_personas = {}
    tool_personas.update(demons)
    tool_personas.update(dlc)
    tool_personas.update(party)
    for name in party: tool_personas[name]['fusion'] = 'party'

    our_personas = load_bom(OUR_WEB/'persona5/personas.json')
    our_special = load_bom(OUR_WEB/'special-fusions/p5-special.json')
    our_chart = load_bom(OUR_WEB/'fusion-charts/p5-base-fusion-chart.json')
    our_special_names = set(our_special.keys())

    # P5 uses triangular chart
    our_counts = calc_recipes(our_personas, our_special_names, our_chart, our_special, triangular=True)
    tool_counts = tool_calc(tool_personas, special_names, chart, special, triangular=True)
    return compare('P5', our_counts, tool_counts)

# ══════════════════════════════════════════════════════════════
# P5R
# ══════════════════════════════════════════════════════════════
def run_p5r():
    print("\n>>> P5R")
    t = TOOL / 'p5/data'
    demons = load(t/'roy-demon-data.json')
    dlc = load(t/'roy-dlc-data.json')
    party = load(t/'roy-party-data.json')
    special = load(t/'roy-special-recipes.json')
    chart = load(t/'roy-fusion-chart.json')

    special_names = set(special.keys())
    tool_personas = {}
    tool_personas.update(demons)
    tool_personas.update(dlc)
    tool_personas.update(party)
    for name in party: tool_personas[name]['fusion'] = 'party'

    our_personas = load_bom(OUR_WEB/'persona5/royal_personas.json')
    our_special = load_bom(OUR_WEB/'special-fusions/p5r-special.json')
    our_chart = load_bom(OUR_WEB/'fusion-charts/p5-fusion-chart.json')
    our_special_names = set(our_special.keys())

    our_counts = calc_recipes(our_personas, our_special_names, our_chart, our_special, triangular=True)
    tool_counts = tool_calc(tool_personas, special_names, chart, special, triangular=True)
    return compare('P5R', our_counts, tool_counts)

# ── Main ──
if __name__ == '__main__':
    total_mismatches = 0
    total_mismatches += run_p3fes()
    total_mismatches += run_p3p()
    total_mismatches += run_p3r()
    total_mismatches += run_p4()
    total_mismatches += run_p4g()
    total_mismatches += run_p5()
    total_mismatches += run_p5r()
    print(f"\n{'='*60}")
    print(f"  TOTAL MISMATCHES ACROSS ALL GAMES: {total_mismatches}")
    print(f"{'='*60}")
