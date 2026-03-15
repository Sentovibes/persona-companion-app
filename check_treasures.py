import json
from pathlib import Path
TOOL = Path('C:/Users/omare/Downloads/megaten-fusion-tool-master/megaten-fusion-tool-master/src/app')
t = TOOL / 'p5/data'
demons = json.load(open(t/'demon-data.json'))
roy = json.load(open(t/'roy-demon-data.json'))
element_chart = json.load(open(t/'element-chart.json'))
roy_element_chart = json.load(open(t/'roy-element-chart.json'))

print('P5 element chart elems:', element_chart.get('elems', []))
print('P5R element chart elems:', roy_element_chart.get('elems', []))

treasures = ['Crystal Skull', "Emperor's Amulet", 'Hope Diamond', 'Koh-i-Noor', 'Orichalcum', 'Orlov', "Queen's Necklace", 'Regent', 'Stone of Scone']
for name in treasures:
    p = demons.get(name) or roy.get(name)
    if p:
        print(name, '->', p.get('race'), p.get('lvl'), p.get('fusion'))
    else:
        print(name, '-> NOT IN DEMONS')
