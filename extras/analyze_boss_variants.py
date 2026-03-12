import json
from pathlib import Path
from collections import defaultdict
import re

print("=" * 70)
print("ANALYZING BOSS VARIANTS (B, C, D suffixes)")
print("=" * 70)

all_enemies = []
boss_variants = defaultdict(list)  # base_name -> [variants]

# Load all enemy data
for f in sorted(Path('../app/src/main/assets/data/enemies').glob('*_enemies.json')):
    game_name = f.stem.replace('_enemies', '').upper()
    data = json.load(open(f, 'r', encoding='utf-8'))
    
    if isinstance(data, list):
        names = [enemy.get("name", "") for enemy in data if isinstance(enemy, dict) and enemy.get("name")]
    else:
        names = [name for name in data.keys() if name]
    
    for name in names:
        all_enemies.append((name, game_name))
        
        # Check if it ends with " B", " C", " D", etc.
        match = re.match(r'^(.+?)\s+([B-Z])$', name)
        if match:
            base_name = match.group(1)
            variant = match.group(2)
            boss_variants[base_name].append((name, game_name, variant))

print(f"\nTotal enemies: {len(all_enemies)}")
print(f"Boss variants found: {len(boss_variants)} base bosses with variants")

# Count total variants
total_variants = sum(len(variants) for variants in boss_variants.values())
print(f"Total variant entries: {total_variants}")

print("\n" + "=" * 70)
print("BOSSES WITH MULTIPLE FORMS")
print("=" * 70)

# Group by number of variants
by_count = defaultdict(list)
for base_name, variants in boss_variants.items():
    by_count[len(variants)].append((base_name, variants))

for count in sorted(by_count.keys(), reverse=True):
    bosses = by_count[count]
    print(f"\n{count} variants ({len(bosses)} bosses):")
    for base_name, variants in sorted(bosses)[:10]:  # Show first 10
        variant_list = ', '.join([f"{v[2]}" for v in sorted(variants, key=lambda x: x[2])])
        games = set(v[1] for v in variants)
        print(f"  {base_name}: {variant_list} (in {', '.join(sorted(games))})")
    if len(bosses) > 10:
        print(f"  ... and {len(bosses) - 10} more")

print("\n" + "=" * 70)
print("IMPACT ON IMAGE COUNT")
print("=" * 70)

# Calculate unique count without variants
unique_without_variants = set()
for name, game in all_enemies:
    # Remove variant suffix
    base_name = re.sub(r'\s+[B-Z]$', '', name)
    unique_without_variants.add(base_name)

print(f"Unique enemies (treating variants as same): {len(unique_without_variants)}")
print(f"Unique enemies (treating variants as different): {len(set(name for name, game in all_enemies))}")
print(f"Difference (variant duplicates): {len(set(name for name, game in all_enemies)) - len(unique_without_variants)}")

print("\n" + "=" * 70)
print("RECOMMENDATION")
print("=" * 70)
print(f"If we use ONE image for all variants (e.g., 'Desirous Maya' for B, C, D):")
print(f"  Need: {len(unique_without_variants)} unique images")
print(f"  Currently have: 296 images")
print(f"  Missing: {len(unique_without_variants) - 296} images")
