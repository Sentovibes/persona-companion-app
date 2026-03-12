import json
from pathlib import Path

total = 0
for f in Path('../app/src/main/assets/data/enemies').glob('*_enemies.json'):
    data = json.load(open(f, 'r', encoding='utf-8'))
    count = len(data) if isinstance(data, list) else len(data.keys())
    print(f'{f.stem}: {count}')
    total += count

print(f'\nTotal across all games: {total}')
print(f'Images already downloaded: 296')
print(f'Missing images: {total - 296}')
