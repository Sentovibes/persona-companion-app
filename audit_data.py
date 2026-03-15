"""
Data audit: check for missing fields, missing personas, missing data
across all 7 games vs megaten-fusion-tool source data.
"""
import json, os
from pathlib import Path

TOOL = Path("C:/Users/omare/Downloads/megaten-fusion-tool-master/megaten-fusion-tool-master/src/app")
OUR  = Path("web/data")

def load(path):
    try:
        with open(path, encoding='utf-8-sig') as f:
            return json.load(f)
    except:
        try:
            with open(path, encoding='utf-8') as f:
                return json.load(f)
        except Exception as e:
            return None

def section(title):
    print(f"\n{'='*60}")
    print(f"  {title}")
    print(f"{'='*60}")

def ok(msg):   print(f"  ✓ {msg}")
def warn(msg): print(f"  ⚠ {msg}")
def err(msg):  print(f"  ✗ {msg}")

issues = []

def flag(msg):
    issues.append(msg)
    err(msg)

# ── Helper: check persona fields ──────────────────────────────────────────────
REQUIRED_FIELDS = ['arcana', 'level', 'stats', 'skills']
OPTIONAL_FIELDS = ['resists', 'description', 'unlock', 'trait', 'item']

def audit_personas(game, our_data, tool_data, tool_extra=None):
    """Compare our persona data vs tool's for missing personas and fields."""
    tool_all = dict(tool_data)
    if tool_extra:
        tool_all.update(tool_extra)

    our_names  = set(our_data.keys())
    tool_names = set(tool_all.keys())

    # Personas in tool but not in ours (excluding party personas)
    missing = []
    for name, p in tool_all.items():
        if p.get('fusion') in ('party', 'accident'): continue
        if name not in our_names:
            missing.append(name)
    if missing:
        flag(f"[{game}] {len(missing)} personas in tool but missing from ours: {missing[:10]}{'...' if len(missing)>10 else ''}")
    else:
        ok(f"[{game}] All tool personas present ({len(our_names)} total)")

    # Check required fields on our personas
    missing_fields = {}
    for name, p in our_data.items():
        for field in REQUIRED_FIELDS:
            if field not in p or p[field] is None:
                missing_fields.setdefault(field, []).append(name)

    for field, names in missing_fields.items():
        flag(f"[{game}] {len(names)} personas missing '{field}': {names[:5]}{'...' if len(names)>5 else ''}")

    # Check optional fields coverage
    for field in OPTIONAL_FIELDS:
        count = sum(1 for p in our_data.values() if field in p and p[field])
        total = len(our_data)
        pct = count/total*100 if total else 0
        if field == 'resists' and pct < 50:
            warn(f"[{game}] '{field}' coverage: {count}/{total} ({pct:.0f}%)")
        elif field in ('description', 'unlock') and pct < 10:
            warn(f"[{game}] '{field}' coverage: {count}/{total} ({pct:.0f}%)")

    # Check for zero-level personas
    zero_lvl = [n for n, p in our_data.items() if not (p.get('level') or p.get('lvl'))]
    if zero_lvl:
        flag(f"[{game}] {len(zero_lvl)} personas with no level: {zero_lvl[:5]}")

    # Check for empty skills
    no_skills = [n for n, p in our_data.items() if not p.get('skills')]
    if no_skills:
        warn(f"[{game}] {len(no_skills)} personas with no skills: {no_skills[:5]}{'...' if len(no_skills)>5 else ''}")

# ── P3FES ─────────────────────────────────────────────────────────────────────
section("P3FES")
t = TOOL / 'p3/data'
our = load(OUR / 'persona3/personas.json')
van = load(t / 'van-demon-data.json')
fes = load(t / 'fes-demon-data.json')
if our and van and fes:
    audit_personas('P3FES', our, van, fes)
else:
    flag("[P3FES] Failed to load data files")

# ── P3P ───────────────────────────────────────────────────────────────────────
section("P3P")
our = load(OUR / 'persona3/portable_personas.json')
p3p = load(t / 'p3p-demon-data.json')
if our and van and fes and p3p:
    combined = dict(van); combined.update(fes); combined.update(p3p)
    audit_personas('P3P', our, combined)
else:
    flag("[P3P] Failed to load data files")

# ── P3R ───────────────────────────────────────────────────────────────────────
section("P3R")
t3r = TOOL / 'p3r/data'
our = load(OUR / 'persona3/reload_personas.json')
demons = load(t3r / 'demon-data.json')
if our and demons:
    audit_personas('P3R', our, demons)
else:
    flag("[P3R] Failed to load data files")

# ── P4 ────────────────────────────────────────────────────────────────────────
section("P4")
t4 = TOOL / 'p4/data'
our = load(OUR / 'persona4/personas.json')
demons = load(t4 / 'demon-data.json')
if our and demons:
    audit_personas('P4', our, demons)
else:
    flag("[P4] Failed to load data files")

# ── P4G ───────────────────────────────────────────────────────────────────────
section("P4G")
our = load(OUR / 'persona4/golden_personas.json')
golden = load(t4 / 'golden-demon-data.json')
if our and demons and golden:
    combined = dict(demons); combined.update(golden)
    audit_personas('P4G', our, combined)
else:
    flag("[P4G] Failed to load data files")

# ── P5 ────────────────────────────────────────────────────────────────────────
section("P5")
t5 = TOOL / 'p5/data'
our = load(OUR / 'persona5/personas.json')
demons = load(t5 / 'demon-data.json')
dlc    = load(t5 / 'dlc-data.json')
if our and demons and dlc:
    audit_personas('P5', our, demons, dlc)
else:
    flag("[P5] Failed to load data files")

# ── P5R ───────────────────────────────────────────────────────────────────────
section("P5R")
our    = load(OUR / 'persona5/royal_personas.json')
demons = load(t5 / 'roy-demon-data.json')
dlc    = load(t5 / 'roy-dlc-data.json')
if our and demons and dlc:
    audit_personas('P5R', our, demons, dlc)
else:
    flag("[P5R] Failed to load data files")

# ── Special fusions ───────────────────────────────────────────────────────────
section("Special Fusions")
specials = {
    'p3fes': (OUR/'special-fusions/p3-special.json',   TOOL/'p3/data/fes-special-recipes.json'),
    'p3p':   (OUR/'special-fusions/p3-special.json',   TOOL/'p3/data/fes-special-recipes.json'),
    'p3r':   (OUR/'special-fusions/p3r-special.json',  TOOL/'p3r/data/special-recipes.json'),
    'p4':    (OUR/'special-fusions/p4-special.json',   TOOL/'p4/data/special-recipes.json'),
    'p4g':   (OUR/'special-fusions/p4-special.json',   TOOL/'p4/data/special-recipes.json'),
    'p5':    (OUR/'special-fusions/p5-special.json',   TOOL/'p5/data/special-recipes.json'),
    'p5r':   (OUR/'special-fusions/p5r-special.json',  TOOL/'p5/data/roy-special-recipes.json'),
}
for game, (our_path, tool_path) in specials.items():
    our_s  = load(our_path)
    tool_s = load(tool_path)
    if our_s is None or tool_s is None:
        flag(f"[{game}] Failed to load special fusion files"); continue
    our_names  = set(our_s.keys())
    tool_names = set(tool_s.keys())
    missing = tool_names - our_names
    extra   = our_names - tool_names
    # filter element demons (empty list in tool)
    elem = {n for n,v in tool_s.items() if isinstance(v,list) and len(v)==0}
    missing -= elem
    if missing:
        flag(f"[{game}] Special fusions missing from ours: {sorted(missing)}")
    else:
        ok(f"[{game}] All special fusions present ({len(our_names)} entries)")
    if extra - elem:
        warn(f"[{game}] Extra special fusions in ours (not in tool): {sorted(extra - elem)}")

# ── Fusion charts ─────────────────────────────────────────────────────────────
section("Fusion Charts")
chart_checks = [
    ('p3fes', OUR/'fusion-charts/p3-fusion-chart.json',      TOOL/'p3/data/fes-fusion-chart.json'),
    ('p3p',   OUR/'fusion-charts/p3p-fusion-chart.json',     TOOL/'p3/data/fes-fusion-chart.json'),
    ('p3r',   OUR/'fusion-charts/p3r-fusion-chart.json',     TOOL/'p3r/data/fusion-chart.json'),
    ('p4',    OUR/'fusion-charts/p4-base-fusion-chart.json', TOOL/'p4/data/fusion-chart.json'),
    ('p4g',   OUR/'fusion-charts/p4-fusion-chart.json',      TOOL/'p4/data/golden-fusion-chart.json'),
    ('p5',    OUR/'fusion-charts/p5-base-fusion-chart.json', TOOL/'p5/data/fusion-chart.json'),
    ('p5r',   OUR/'fusion-charts/p5-fusion-chart.json',      TOOL/'p5/data/roy-fusion-chart.json'),
]
for game, our_path, tool_path in chart_checks:
    our_c  = load(our_path)
    tool_c = load(tool_path)
    if our_c is None or tool_c is None:
        flag(f"[{game}] Failed to load chart"); continue
    our_races  = our_c.get('races', [])
    tool_races = tool_c.get('races', [])
    if set(our_races) != set(tool_races):
        missing_r = set(tool_races) - set(our_races)
        extra_r   = set(our_races)  - set(tool_races)
        if missing_r: flag(f"[{game}] Chart missing races: {missing_r}")
        if extra_r:   warn(f"[{game}] Chart has extra races: {extra_r}")
    else:
        ok(f"[{game}] Chart races match ({len(our_races)} races)")

# ── Enemies ───────────────────────────────────────────────────────────────────
section("Enemies")
enemy_files = {
    'p3fes': OUR/'enemies/p3fes_enemies.json',
    'p3p':   OUR/'enemies/p3p_enemies.json',
    'p3r':   OUR/'enemies/p3r_enemies.json',
    'p4':    OUR/'enemies/p4_enemies.json',
    'p4g':   OUR/'enemies/p4g_enemies.json',
    'p5':    OUR/'enemies/p5_enemies.json',
    'p5r':   OUR/'enemies/p5r_enemies.json',
}
for game, path in enemy_files.items():
    data = load(path)
    if data is None:
        flag(f"[{game}] Enemy file missing or unreadable")
        continue
    items = list(data.values()) if isinstance(data, dict) else data
    total = len(items)
    no_hp  = sum(1 for e in items if not e.get('hp'))
    no_res = sum(1 for e in items if not e.get('resists'))
    ok(f"[{game}] {total} enemies loaded")
    if no_hp  > total * 0.5: warn(f"[{game}] {no_hp}/{total} enemies missing HP")
    if no_res > total * 0.5: warn(f"[{game}] {no_res}/{total} enemies missing resists")

# ── Social Links ──────────────────────────────────────────────────────────────
section("Social Links")
sl_files = {
    'p3fes': OUR/'social-links/p3fes_social_links.json',
    'p3p_m': OUR/'social-links/p3p_male_social_links.json',
    'p3p_f': OUR/'social-links/p3p_femc_social_links.json',
    'p3r':   OUR/'social-links/p3r_social_links.json',
    'p4':    OUR/'social-links/p4+p4g_social_links.json',
    'p5':    OUR/'social-links/p5+p5r_social_links.json',
}
for game, path in sl_files.items():
    data = load(path)
    if data is None:
        flag(f"[{game}] Social link file missing")
        continue
    ok(f"[{game}] {len(data)} social links/confidants")

# ── Classroom ─────────────────────────────────────────────────────────────────
section("Classroom")
for game, path in [
    ('p3', OUR/'classroom/p3_classroom_answers.json'),
    ('p4', OUR/'classroom/p4_classroom_answers.json'),
    ('p5', OUR/'classroom/p5_classroom_answers.json'),
]:
    data = load(path)
    if data is None:
        flag(f"[{game}] Classroom file missing")
    else:
        ok(f"[{game}] Classroom data present")

# ── Summary ───────────────────────────────────────────────────────────────────
section("SUMMARY")
if issues:
    print(f"\n  {len(issues)} issue(s) found:\n")
    for i, issue in enumerate(issues, 1):
        print(f"  {i}. {issue}")
else:
    print("\n  No issues found — all data looks complete!")
