"""Fix P5/P5R data: add element demons to special files, add missing DLC personas."""
import json
from pathlib import Path

TOOL = Path('C:/Users/omare/Downloads/megaten-fusion-tool-master/megaten-fusion-tool-master/src/app')
OUR_WEB = Path('web/data')

def load(path):
    with open(path, encoding='utf-8') as f:
        return json.load(f)

def load_bom(path):
    try:
        return load(path)
    except:
        with open(path, encoding='utf-8-sig') as f:
            return json.load(f)

def save(path, data):
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)

# ── P5 ──
t = TOOL / 'p5/data'
element_chart = load(t/'element-chart.json')
p5_elems = element_chart.get('elems', [])

our_p5_special = load_bom(OUR_WEB/'special-fusions/p5-special.json')
for name in p5_elems:
    if name not in our_p5_special:
        our_p5_special[name] = []
        print(f'P5: Added element demon {name} to special')
save(OUR_WEB/'special-fusions/p5-special.json', our_p5_special)

# ── P5R ──
roy_element_chart = load(t/'roy-element-chart.json')
p5r_elems = roy_element_chart.get('elems', [])

our_p5r_special = load_bom(OUR_WEB/'special-fusions/p5r-special.json')
for name in p5r_elems:
    if name not in our_p5r_special:
        our_p5r_special[name] = []
        print(f'P5R: Added element demon {name} to special')
save(OUR_WEB/'special-fusions/p5r-special.json', our_p5r_special)

print('Done.')
