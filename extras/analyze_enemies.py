import json
from pathlib import Path
from collections import defaultdict

games_data = {}
all_enemies = defaultdict(list)  # enemy_name -> [games that have it]

print("=" * 70)
print("ENEMY ANALYSIS ACROSS ALL GAMES")
print("=" * 70)

# Load all enemy data
for f in sorted(Path('../app/src/main/assets/data/enemies').glob('*_enemies.json')):
    game_name = f.stem.replace('_enemies', '').upper()
    data = json.load(open(f, 'r', encoding='utf-8'))
    
    if isinstance(data, list):
        names = [enemy.get("name", "") for enemy in data if isinstance(enemy, dict) and enemy.get("name")]
    else:
        names = [name for name in data.keys() if name]
    
    games_data[game_name] = names
    
    # Track which games have each enemy
    for name in names:
        all_enemies[name].append(game_name)
    
    print(f"\n{game_name}: {len(names)} enemies")

print("\n" + "=" * 70)
print(f"TOTAL ENEMIES (with duplicates): {sum(len(names) for names in games_data.values())}")
print(f"UNIQUE ENEMY NAMES: {len(all_enemies)}")
print("=" * 70)

# Find duplicates
duplicates = {name: games for name, games in all_enemies.items() if len(games) > 1}

print(f"\nDUPLICATE ENEMIES (appear in multiple games): {len(duplicates)}")
print("\nShowing duplicates that appear in 3+ games:")
for name, games in sorted(duplicates.items(), key=lambda x: len(x[1]), reverse=True):
    if len(games) >= 3:
        print(f"  {name}: {', '.join(games)} ({len(games)} games)")

print("\n" + "=" * 70)
print("IMAGES NEEDED")
print("=" * 70)
print(f"If we need ONE image per unique enemy: {len(all_enemies)} images")
print(f"If we need images for ALL entries: {sum(len(names) for names in games_data.values())} images")

# Check existing images
app_images_dir = Path("../app/src/main/assets/images/enemies")
if app_images_dir.exists():
    app_images = {img.stem for img in app_images_dir.glob("*.*")}
    print(f"\nCurrently have: {len(app_images)} images")
    print(f"Missing (unique): {len(all_enemies) - len(app_images)} images")
