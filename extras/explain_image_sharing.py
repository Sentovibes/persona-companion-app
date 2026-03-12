import json
from pathlib import Path

print("=" * 70)
print("WHY 1503 ENEMIES = 602 IMAGES")
print("=" * 70)

games = [
    ("p3fes", "app/src/main/assets/data/enemies/p3fes_enemies.json"),
    ("p3p", "app/src/main/assets/data/enemies/p3p_enemies.json"),
    ("p3r", "app/src/main/assets/data/enemies/p3r_enemies.json"),
    ("p4", "app/src/main/assets/data/enemies/p4_enemies.json"),
    ("p4g", "app/src/main/assets/data/enemies/p4g_enemies.json"),
    ("p5", "app/src/main/assets/data/enemies/p5_enemies.json"),
    ("p5r", "app/src/main/assets/data/enemies/p5r_enemies.json"),
]

# Track which enemies appear in which games
enemy_appearances = {}
total_entries = 0

for game_id, json_path in games:
    with open(json_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    if isinstance(data, list):
        enemies = data
    else:
        enemies = [{"name": name, **props} for name, props in data.items()]
    
    total_entries += len(enemies)
    
    for enemy in enemies:
        if not isinstance(enemy, dict):
            continue
        
        name = enemy.get('name', '')
        if not name:
            continue
        
        # Get base name (remove variants)
        base_name = name
        if name.endswith(' A') or name.endswith(' B') or name.endswith(' C') or name.endswith(' D'):
            base_name = name[:-2].strip()
        
        if base_name not in enemy_appearances:
            enemy_appearances[base_name] = []
        
        if game_id not in enemy_appearances[base_name]:
            enemy_appearances[base_name].append(game_id)

# Count unique base enemies
unique_enemies = len(enemy_appearances)

# Count enemies that appear in multiple games
shared_enemies = {name: games for name, games in enemy_appearances.items() if len(games) > 1}

print(f"\nTotal enemy entries across all games: {total_entries}")
print(f"Unique base enemy names: {unique_enemies}")
print(f"Enemies shared across multiple games: {len(shared_enemies)}")
print(f"Actual unique images needed: 602")

print("\n" + "=" * 70)
print("REASONS FOR THE DIFFERENCE:")
print("=" * 70)

print("\n1. SHARED ENEMIES ACROSS GAMES")
print(f"   {len(shared_enemies)} enemies appear in multiple games")
print("   Examples:")
for name, game_list in list(shared_enemies.items())[:10]:
    print(f"   - {name}: {', '.join(game_list)}")

print("\n2. ENEMY VARIANTS (A/B/C/D)")
print("   Variants use the same base image")
variants = [name for name in enemy_appearances.keys() if any(
    v in enemy_appearances for v in [f"{name} A", f"{name} B", f"{name} C", f"{name} D"]
)]
print(f"   ~{len(variants)} enemies have variants")
print("   Examples:")
print("   - Cowardly Maya A/B/C/D → all use cowardly_maya.png")
print("   - Magic Hand A/B → both use magic_hand.png")

print("\n3. COMPOUND BOSS NAMES")
print("   Multi-part bosses use first part's image")
print("   Examples:")
print("   - Emperor & Empress → uses emperor.png")
print("   - Chariot & Justice → uses chariot.png")
print("   - Fortune & Strength → uses fortune.png")

print("\n4. NYX AVATAR PHASES")
print("   Nyx Avatar has 14 phases but uses 1 image")
print("   - Nyx Avatar (Phase 1-14) → all use nyx_avatar.png")

print("\n" + "=" * 70)
print("BREAKDOWN BY GAME:")
print("=" * 70)

for game_id, json_path in games:
    with open(json_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    if isinstance(data, list):
        enemies = data
    else:
        enemies = [{"name": name, **props} for name, props in data.items()]
    
    # Count unique base names
    base_names = set()
    for enemy in enemies:
        if not isinstance(enemy, dict):
            continue
        name = enemy.get('name', '')
        if not name:
            continue
        base_name = name
        if name.endswith(' A') or name.endswith(' B') or name.endswith(' C') or name.endswith(' D'):
            base_name = name[:-2].strip()
        base_names.add(base_name)
    
    print(f"\n{game_id.upper()}:")
    print(f"  Total entries: {len(enemies)}")
    print(f"  Unique base names: {len(base_names)}")

print("\n" + "=" * 70)
print("SUMMARY")
print("=" * 70)
print(f"Total entries: {total_entries}")
print(f"Unique base enemies: {unique_enemies}")
print(f"Shared across games: {len(shared_enemies)}")
print(f"Images in shared folder: 602")
print(f"\nThe difference is due to:")
print(f"  - Enemies appearing in multiple games (reuse same image)")
print(f"  - Enemy variants A/B/C/D (reuse base image)")
print(f"  - Compound bosses (use first part's image)")
print(f"  - Multi-phase bosses (use single image)")
