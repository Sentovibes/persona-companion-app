import json
import shutil

print("=" * 70)
print("ADDING EPISODE AIGIS FLAGS TO P3R ENEMIES")
print("=" * 70)

# Load Aigis enemy names
with open('extras/aeg-enemy-data.json', 'r', encoding='utf-8') as f:
    aigis_data = json.load(f)

aigis_enemy_names = set(aigis_data.keys())
print(f"\nTotal Episode Aigis enemies: {len(aigis_enemy_names)}")

# Load P3R enemies
p3r_path = 'app/src/main/assets/data/enemies/p3r_enemies.json'
backup_path = p3r_path.replace('.json', '_before_aigis_flags.json')

# Backup
shutil.copy(p3r_path, backup_path)
print(f"Backed up to: {backup_path}")

with open(p3r_path, 'r', encoding='utf-8') as f:
    p3r_enemies = json.load(f)

print(f"Total P3R enemies: {len(p3r_enemies)}")

# Add episodeAigis flag
flagged_count = 0
for enemy in p3r_enemies:
    if enemy['name'] in aigis_enemy_names:
        enemy['episodeAigis'] = True
        flagged_count += 1

print(f"Added episodeAigis flag to {flagged_count} enemies")

# Save
with open(p3r_path, 'w', encoding='utf-8') as f:
    json.dump(p3r_enemies, f, indent=2, ensure_ascii=False)

print(f"✓ Saved updated P3R enemies")

# Verify
with open(p3r_path, 'r', encoding='utf-8') as f:
    verify = json.load(f)

aigis_flagged = [e for e in verify if e.get('episodeAigis')]
print(f"\nVerification: {len(aigis_flagged)} enemies now have episodeAigis flag")

print("\nSample flagged enemies:")
for e in aigis_flagged[:10]:
    print(f"  - {e['name']}")

print("\n" + "=" * 70)
print("COMPLETE - Episode Aigis toggle should now work!")
print("=" * 70)
