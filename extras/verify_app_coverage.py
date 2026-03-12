import json
from pathlib import Path
import re

print("=" * 70)
print("VERIFYING APP IMAGE COVERAGE")
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

# Get all images in shared folder
shared_folder = Path("app/src/main/assets/images/enemies_shared")
available_images = {img.stem for img in shared_folder.glob("*.png")}

print(f"\nTotal images in enemies_shared: {len(available_images)}")
print()

total_enemies = 0
total_with_images = 0
total_missing = 0

for game_id, json_path in games:
    # Load enemy data
    with open(json_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    if isinstance(data, list):
        enemies = data
    else:
        enemies = [{"name": name, **props} for name, props in data.items()]
    
    missing_images = []
    
    for enemy in enemies:
        if not isinstance(enemy, dict):
            continue
            
        name = enemy.get('name', '')
        if not name:
            continue
        
        # Apply ImageUtils logic
        clean_name = name
        
        # Remove variant suffixes
        if re.match(r'.*\s[A-Z]$', clean_name):
            clean_name = clean_name[:-2].strip()
        
        # Handle compound names (use first part only)
        if " & " in clean_name:
            clean_name = clean_name.split(" & ")[0]
        
        # Handle boss names with full names (P3R only)
        if game_id == "p3r":
            boss_map = {
                "chidori yoshino": "chidori",
                "jin shirato": "jin",
                "takaya sakaki": "takaya"
            }
            lower_name = clean_name.lower()
            if lower_name in boss_map:
                clean_name = boss_map[lower_name]
        
        # For P5/P5R, use persona_name if available
        if game_id in ['p5', 'p5r'] and 'persona_name' in enemy and enemy['persona_name']:
            clean_name = enemy['persona_name']
        
        # Convert to safe name
        safe_name = clean_name.lower().replace(" ", "_").replace("-", "_").replace("/", "_").replace(":", "").replace("?", "").replace("'", "").replace("'", "").replace("&", "")
        
        # Check if image exists
        if safe_name not in available_images:
            missing_images.append(f"{name} → {safe_name}.png")
        else:
            total_with_images += 1
        
        total_enemies += 1
    
    print(f"{game_id.upper()}:")
    print(f"  Total enemies: {len(enemies)}")
    print(f"  Missing images: {len(missing_images)}")
    
    if missing_images and len(missing_images) <= 10:
        for missing in missing_images[:10]:
            print(f"    - {missing}")
    elif missing_images:
        for missing in missing_images[:10]:
            print(f"    - {missing}")
        print(f"    ... and {len(missing_images) - 10} more")
    
    total_missing += len(missing_images)
    print()

print("=" * 70)
print("OVERALL APP COVERAGE")
print("=" * 70)
print(f"Total enemies across all games: {total_enemies}")
print(f"Enemies with images: {total_with_images}")
print(f"Enemies missing images: {total_missing}")
print(f"Coverage rate: {(total_with_images / total_enemies * 100):.1f}%")
