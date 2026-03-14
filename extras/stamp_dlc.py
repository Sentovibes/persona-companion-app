"""
Stamps isDlc: true onto DLC personas in the app's persona JSON files.
Source of truth: megaten-fusion-tool dlc-data.json files.
"""
import json

MEGATEN = 'C:/Users/omare/Downloads/megaten-fusion-tool-master/megaten-fusion-tool-master/src/app'

# Load DLC name sets
with open(f'{MEGATEN}/p5/data/dlc-data.json') as f:
    p5_dlc = set(json.load(f).keys())
with open(f'{MEGATEN}/p5/data/roy-dlc-data.json') as f:
    p5r_dlc = set(json.load(f).keys())

# P3 and P4 have no DLC personas per megaten-tool
targets = [
    ('app/src/main/assets/data/persona5/personas.json',      p5_dlc),
    ('app/src/main/assets/data/persona5/royal_personas.json', p5r_dlc),
]

for path, dlc_names in targets:
    with open(path, encoding='utf-8') as f:
        data = json.load(f)

    stamped = 0
    for name, persona in data.items():
        if name in dlc_names:
            persona['isDlc'] = True
            stamped += 1
        else:
            # Remove stale flag if present
            persona.pop('isDlc', None)

    with open(path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)

    print(f'{path}: stamped {stamped} DLC personas')

print('\nDone! DLC personas now have isDlc: true in the JSON.')
print('Note: Only Arsene (P5R) will show 0 recipes when DLC is off - that is correct,')
print('      since all its recipes require a DLC ingredient.')
