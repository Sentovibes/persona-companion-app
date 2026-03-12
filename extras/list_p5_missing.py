import json
from pathlib import Path

images_path = Path('app/src/main/assets/images/enemies_shared')
available = {img.stem.lower().replace(' ', '_').replace('-', '_') for img in images_path.glob('*.png')}

with open('app/src/main/assets/data/enemies/p5_enemies.json', 'r', encoding='utf-8') as f:
    p5_enemies = json.load(f)

missing = []
for enemy in p5_enemies:
    img_name = enemy['name'].lower().replace(' ', '_').replace('-', '_').replace('/', '_').replace(':', '_').replace('?', '').replace("'", '').replace('&', '_')
    if img_name not in available:
        missing.append(enemy['name'])

print(f'P5 missing {len(missing)} images:')
for name in missing[:30]:
    print(f'  {name}')
