import json
from pathlib import Path
TOOL = Path('C:/Users/omare/Downloads/megaten-fusion-tool-master/megaten-fusion-tool-master/src/app')
t = TOOL / 'p5/data'
special = json.load(open(t/'special-recipes.json'))
roy_special = json.load(open(t/'roy-special-recipes.json'))
element_chart = json.load(open(t/'element-chart.json'))
roy_element_chart = json.load(open(t/'roy-element-chart.json'))

treasures = element_chart.get('elems', []) + [e for e in roy_element_chart.get('elems', []) if e not in element_chart.get('elems', [])]
print('All treasure/element demons:', treasures)
for name in treasures:
    p5 = special.get(name, 'NOT IN SPECIAL')
    p5r = roy_special.get(name, 'NOT IN SPECIAL')
    print(f'{name}: P5={p5}, P5R={p5r}')

print()
# Check our special files
our_p5_special = json.load(open('web/data/special-fusions/p5-special.json', encoding='utf-8-sig'))
our_p5r_special = json.load(open('web/data/special-fusions/p5r-special.json', encoding='utf-8-sig'))
for name in treasures:
    p5 = our_p5_special.get(name, 'NOT IN SPECIAL')
    p5r = our_p5r_special.get(name, 'NOT IN SPECIAL')
    print(f'{name}: Our P5={p5}, Our P5R={p5r}')
