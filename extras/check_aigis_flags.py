import json

# Check P3R enemies for episodeAigis flag
with open('app/src/main/assets/data/enemies/p3r_enemies.json', 'r', encoding='utf-8') as f:
    enemies = json.load(f)

aigis_enemies = [e for e in enemies if e.get('episodeAigis')]

print(f"Total P3R enemies: {len(enemies)}")
print(f"Enemies with episodeAigis flag: {len(aigis_enemies)}")

if aigis_enemies:
    print("\nEpisode Aigis enemies:")
    for e in aigis_enemies[:20]:
        print(f"  - {e['name']}: episodeAigis={e.get('episodeAigis')}")
else:
    print("\n⚠ NO ENEMIES HAVE episodeAigis FLAG!")
    print("This is why the toggle doesn't work - the flag is missing!")
