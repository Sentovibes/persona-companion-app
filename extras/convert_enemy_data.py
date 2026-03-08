"""
Convert enemy data from megaten-fusion-tool to our format
All enemies in one file - no boss categorization since source data doesn't distinguish
"""

import json
import os

def convert_enemy_data(source_file, output_file):
    """Convert enemy data file to our format"""
    print(f"Processing: {source_file}")
    
    if not os.path.exists(source_file):
        print(f"  ✗ File not found: {source_file}")
        return
    
    with open(source_file, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    enemies = []
    
    for name, enemy_data in data.items():
        enemy = {
            "name": name,
            "arcana": enemy_data.get("race", "Unknown"),
            "level": enemy_data.get("lvl", 1),
            "hp": enemy_data.get("stats", [0])[0] if "stats" in enemy_data else 0,
            "sp": enemy_data.get("stats", [0, 0])[1] if "stats" in enemy_data and len(enemy_data["stats"]) > 1 else 0,
            "stats": {
                "strength": enemy_data.get("stats", [0, 0, 0])[2] if "stats" in enemy_data and len(enemy_data["stats"]) > 2 else 0,
                "magic": enemy_data.get("stats", [0, 0, 0, 0])[3] if "stats" in enemy_data and len(enemy_data["stats"]) > 3 else 0,
                "endurance": enemy_data.get("stats", [0, 0, 0, 0, 0])[4] if "stats" in enemy_data and len(enemy_data["stats"]) > 4 else 0,
                "agility": enemy_data.get("stats", [0, 0, 0, 0, 0, 0])[5] if "stats" in enemy_data and len(enemy_data["stats"]) > 5 else 0,
                "luck": enemy_data.get("stats", [0, 0, 0, 0, 0, 0, 0])[6] if "stats" in enemy_data and len(enemy_data["stats"]) > 6 else 0
            },
            "resists": enemy_data.get("resists", "----------"),
            "skills": enemy_data.get("skills", []),
            "area": enemy_data.get("area", "Unknown"),
            "exp": enemy_data.get("exp", 0),
            "drops": {
                "gem": enemy_data.get("gem", "-"),
                "item": enemy_data.get("item", "-") if "item" in enemy_data else "-"
            }
        }
        
        enemies.append(enemy)
    
    # Save file
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(enemies, f, indent=2, ensure_ascii=False)
    
    print(f"  ✓ {len(enemies)} enemies")
    print(f"  → {output_file}")

def main():
    print("="*60)
    print("Enemy Data Converter")
    print("="*60)
    print()
    
    # Ensure extras folder exists
    os.makedirs("extras", exist_ok=True)
    
    # Define source files from megaten-fusion-tool
    base_path = "C:/Users/omare/Downloads/megaten-fusion-tool-master/megaten-fusion-tool-master/src/app"
    conversions = [
        (f"{base_path}/p3/data/van-enemy-data.json", "extras/p3fes_enemies.json"),
        (f"{base_path}/p3/data/ans-enemy-data.json", "extras/p3fes_answer_enemies.json"),
        (f"{base_path}/p4/data/enemy-data.json", "extras/p4_enemies.json"),
        (f"{base_path}/p4/data/golden-enemy-data.json", "extras/p4g_enemies.json"),
        (f"{base_path}/p5/data/enemy-data.json", "extras/p5_enemies.json"),
        (f"{base_path}/p5/data/roy-enemy-data.json", "extras/p5r_enemies.json"),
    ]
    
    for source, output in conversions:
        convert_enemy_data(source, output)
        print()
    
    print("="*60)
    print("Conversion complete!")
    print("="*60)

if __name__ == "__main__":
    main()
