"""
Check which personas/enemies are missing images across all games
"""
import json
import os

BASE_DIR = r"C:\Users\omare\Music\Persona-Companion-App\persona-companion-app\app\src\main\assets\data"

def check_missing(json_path, image_dir, game_name):
    """Check missing images for a specific game"""
    if not os.path.exists(json_path):
        return None
    
    with open(json_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    if isinstance(data, dict):
        names = list(data.keys())
    else:
        names = [item.get('name') for item in data if item.get('name')]
    
    missing = []
    for name in names:
        safe_name = name.replace("/", "_").replace(":", "").replace("?", "")
        image_path = os.path.join(image_dir, f"{safe_name}.png")
        if not os.path.exists(image_path):
            missing.append(name)
    
    total = len(names)
    downloaded = total - len(missing)
    percentage = (downloaded / total * 100) if total > 0 else 0
    
    return {
        'game': game_name,
        'total': total,
        'downloaded': downloaded,
        'missing': len(missing),
        'percentage': percentage,
        'missing_list': missing
    }

if __name__ == "__main__":
    print("="*70)
    print("MISSING IMAGES REPORT")
    print("="*70)
    
    games = [
        # Personas
        (os.path.join(BASE_DIR, 'persona3', 'personas.json'), 'images/personas/p3fes', 'P3 FES Personas'),
        (os.path.join(BASE_DIR, 'persona3', 'portable_personas.json'), 'images/personas/p3p', 'P3P Personas'),
        (os.path.join(BASE_DIR, 'persona3', 'reload_personas.json'), 'images/personas/p3r', 'P3R Personas'),
        (os.path.join(BASE_DIR, 'persona4', 'personas.json'), 'images/personas/p4', 'P4 Personas'),
        (os.path.join(BASE_DIR, 'persona4', 'golden_personas.json'), 'images/personas/p4g', 'P4G Personas'),
        (os.path.join(BASE_DIR, 'persona5', 'personas.json'), 'images/personas/p5', 'P5 Personas'),
        (os.path.join(BASE_DIR, 'persona5', 'royal_personas.json'), 'images/personas/p5r', 'P5R Personas'),
        
        # Enemies
        (os.path.join(BASE_DIR, 'enemies', 'p3fes_enemies.json'), 'images/enemies/p3fes', 'P3 FES Enemies'),
        (os.path.join(BASE_DIR, 'enemies', 'p3p_enemies.json'), 'images/enemies/p3p', 'P3P Enemies'),
        (os.path.join(BASE_DIR, 'enemies', 'p3r_enemies.json'), 'images/enemies/p3r', 'P3R Enemies'),
        (os.path.join(BASE_DIR, 'enemies', 'p4_enemies.json'), 'images/enemies/p4', 'P4 Enemies'),
        (os.path.join(BASE_DIR, 'enemies', 'p4g_enemies.json'), 'images/enemies/p4g', 'P4G Enemies'),
        (os.path.join(BASE_DIR, 'enemies', 'p5_enemies.json'), 'images/enemies/p5', 'P5 Enemies'),
        (os.path.join(BASE_DIR, 'enemies', 'p5r_enemies.json'), 'images/enemies/p5r', 'P5R Enemies'),
    ]
    
    all_results = []
    
    for json_path, image_dir, game_name in games:
        result = check_missing(json_path, image_dir, game_name)
        if result:
            all_results.append(result)
            print(f"\n{result['game']}: {result['downloaded']}/{result['total']} ({result['percentage']:.1f}%)")
            if result['missing'] > 0 and result['missing'] <= 10:
                print(f"  Missing: {', '.join(result['missing_list'])}")
            elif result['missing'] > 10:
                print(f"  Missing {result['missing']} images (too many to list)")
    
    print(f"\n{'='*70}")
    print("SUMMARY")
    print(f"{'='*70}")
    
    persona_results = [r for r in all_results if 'Personas' in r['game']]
    enemy_results = [r for r in all_results if 'Enemies' in r['game']]
    
    total_personas = sum(r['total'] for r in persona_results)
    downloaded_personas = sum(r['downloaded'] for r in persona_results)
    
    total_enemies = sum(r['total'] for r in enemy_results)
    downloaded_enemies = sum(r['downloaded'] for r in enemy_results)
    
    print(f"\nPersonas: {downloaded_personas}/{total_personas} ({downloaded_personas/total_personas*100:.1f}%)")
    print(f"Enemies: {downloaded_enemies}/{total_enemies} ({downloaded_enemies/total_enemies*100:.1f}%)")
    print(f"TOTAL: {downloaded_personas + downloaded_enemies}/{total_personas + total_enemies} ({(downloaded_personas + downloaded_enemies)/(total_personas + total_enemies)*100:.1f}%)")
