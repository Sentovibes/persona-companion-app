#!/usr/bin/env python3
"""
Check status of all images (enemies and personas) across all games.
Compares JSON data files with actual images in assets.
"""

import json
from pathlib import Path

def safe_filename(name):
    """Convert name to safe filename format"""
    return name.lower().replace(" ", "_").replace("'", "").replace("-", "_").replace(":", "").replace("?", "").replace("/", "_")

def load_json(filepath):
    """Load JSON file"""
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            return json.load(f)
    except Exception as e:
        print(f"Error loading {filepath}: {e}")
        return []

def check_game_images(game_id, json_file, images_dir, is_enemy=True):
    """Check images for a specific game"""
    try:
        data = load_json(json_file)
        
        if not data:
            return {"total": 0, "found": 0, "missing": [], "percentage": 0}
        
        # Handle different JSON structures
        if isinstance(data, dict):
            if 'shadows' in data:
                items = data['shadows']
            elif 'demons' in data:
                items = data['demons']
            elif 'personas' in data:
                items = data['personas']
            else:
                # For persona data, the dict keys are persona names
                items = [{"name": key} for key in data.keys()]
        else:
            items = data
        
        total = len(items)
        found = 0
        missing = []
        
        for item in items:
            # Handle both dict and string items
            if isinstance(item, str):
                name = item
            else:
                # For P5/P5R enemies, use persona_name if available (actual demon name)
                # Otherwise use name (descriptive name)
                name = item.get('persona_name', item.get('name', ''))
            
            if not name:
                continue
            
            # Strip A-Z variants
            if name and len(name) >= 2 and name[-2:] in [' A', ' B', ' C', ' D', ' E', ' F', ' G', ' H']:
                name = name[:-2]
            
            safe_name = safe_filename(name)
            image_path = images_dir / f"{safe_name}.png"
            
            if image_path.exists():
                found += 1
            else:
                missing.append(name)
        
        return {
            "total": total,
            "found": found,
            "missing": missing,
            "percentage": (found / total * 100) if total > 0 else 0
        }
    except Exception as e:
        print(f"Error checking {game_id}: {e}")
        import traceback
        traceback.print_exc()
        return {"total": 0, "found": 0, "missing": [], "percentage": 0}

def main():
    script_dir = Path(__file__).parent
    app_dir = script_dir.parent / "app" / "src" / "main" / "assets"
    data_dir = app_dir / "data"
    images_dir = app_dir / "images"
    
    print("="*70)
    print("IMAGE STATUS REPORT")
    print("="*70)
    
    # Define games and their data files (using actual app data paths)
    games = {
        "P3FES": {
            "enemies": (data_dir / "enemies" / "p3fes_enemies.json", images_dir / "enemies" / "p3fes"),
            "personas": (data_dir / "persona3" / "fes_personas.json", images_dir / "personas" / "p3fes")
        },
        "P3P": {
            "enemies": (data_dir / "enemies" / "p3p_enemies.json", images_dir / "enemies" / "p3p"),
            "personas": (data_dir / "persona3" / "portable_personas.json", images_dir / "personas" / "p3p")
        },
        "P3R": {
            "enemies": (data_dir / "enemies" / "p3r_enemies.json", images_dir / "enemies" / "p3r"),
            "personas": (data_dir / "persona3" / "reload_personas.json", images_dir / "personas" / "p3r")
        },
        "P4": {
            "enemies": (data_dir / "enemies" / "p4_enemies.json", images_dir / "enemies" / "p4"),
            "personas": (data_dir / "persona4" / "p4_personas.json", images_dir / "personas" / "p4")
        },
        "P4G": {
            "enemies": (data_dir / "enemies" / "p4g_enemies.json", images_dir / "enemies" / "p4g"),
            "personas": (data_dir / "persona4" / "golden_personas.json", images_dir / "personas" / "p4g")
        },
        "P5": {
            "enemies": (data_dir / "enemies" / "p5_enemies.json", images_dir / "enemies" / "p5"),
            "personas": (data_dir / "persona5" / "p5_personas.json", images_dir / "personas" / "p5")
        },
        "P5R": {
            "enemies": (data_dir / "enemies" / "p5r_enemies.json", images_dir / "enemies" / "p5r"),
            "personas": (data_dir / "persona5" / "royal_personas.json", images_dir / "personas" / "p5r")
        }
    }
    
    total_stats = {
        "enemies": {"total": 0, "found": 0, "missing": 0},
        "personas": {"total": 0, "found": 0, "missing": 0}
    }
    
    for game_name, game_data in sorted(games.items()):
        print(f"\n{'='*70}")
        print(f"{game_name}")
        print(f"{'='*70}")
        
        for category in ["enemies", "personas"]:
            json_path, images_dir = game_data[category]
            
            if not json_path.exists():
                print(f"  {category.upper()}: JSON file not found ({json_path})")
                result = {"total": 0, "found": 0, "missing": [], "percentage": 0}
            elif not images_dir.exists():
                print(f"  {category.upper()}: Images directory not found ({images_dir})")
                result = {"total": 0, "found": 0, "missing": [], "percentage": 0}
            else:
                result = check_game_images(game_name, json_path, images_dir, is_enemy=(category=="enemies"))
            
            total_stats[category]["total"] += result["total"]
            total_stats[category]["found"] += result["found"]
            total_stats[category]["missing"] += len(result["missing"])
            
            status = "✅" if result["percentage"] == 100 else "⚠️" if result["percentage"] >= 95 else "❌"
            print(f"  {status} {category.upper()}: {result['found']}/{result['total']} ({result['percentage']:.1f}%)")
            
            if result["missing"] and len(result["missing"]) <= 10:
                print(f"     Missing: {', '.join(result['missing'])}")
            elif result["missing"]:
                print(f"     Missing {len(result['missing'])} images (showing first 5):")
                print(f"     {', '.join(result['missing'][:5])}")
    
    # Overall summary
    print(f"\n{'='*70}")
    print("OVERALL SUMMARY")
    print(f"{'='*70}")
    
    for category in ["enemies", "personas"]:
        stats = total_stats[category]
        percentage = (stats["found"] / stats["total"] * 100) if stats["total"] > 0 else 0
        status = "✅" if percentage == 100 else "⚠️" if percentage >= 95 else "❌"
        
        print(f"{status} {category.upper()}: {stats['found']}/{stats['total']} ({percentage:.1f}%)")
        print(f"   Missing: {stats['missing']} images")
    
    grand_total = total_stats["enemies"]["total"] + total_stats["personas"]["total"]
    grand_found = total_stats["enemies"]["found"] + total_stats["personas"]["found"]
    grand_missing = total_stats["enemies"]["missing"] + total_stats["personas"]["missing"]
    grand_percentage = (grand_found / grand_total * 100) if grand_total > 0 else 0
    
    print(f"\n{'='*70}")
    print(f"GRAND TOTAL: {grand_found}/{grand_total} ({grand_percentage:.1f}%)")
    print(f"Missing: {grand_missing} images")
    print(f"{'='*70}")

if __name__ == "__main__":
    main()
