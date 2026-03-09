"""
Check download progress
"""
import json
import os

def count_images(directory):
    """Count images in a directory"""
    if not os.path.exists(directory):
        return 0
    return len([f for f in os.listdir(directory) if f.endswith(('.png', '.jpg', '.jpeg', '.webp'))])

def get_expected_count(filepath):
    """Get expected count from JSON file"""
    if not os.path.exists(filepath):
        return 0
    
    with open(filepath, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    if isinstance(data, dict):
        return len(data)
    return len(data)

def get_boss_count(filepath):
    """Get boss count from enemies JSON"""
    if not os.path.exists(filepath):
        return 0
    
    with open(filepath, 'r', encoding='utf-8') as f:
        enemies = json.load(f)
    
    return len([e for e in enemies if e.get('isBoss') or e.get('isMiniBoss')])

print("=" * 70)
print("  IMAGE DOWNLOAD PROGRESS")
print("=" * 70)

# Personas
print("\nPERSONAS:")
print("-" * 70)

p4g_expected = get_expected_count('../app/src/main/assets/data/persona4/golden_personas.json')
p4g_downloaded = count_images('images/personas/p4g')
print(f"P4G:  {p4g_downloaded:3d}/{p4g_expected:3d} ({p4g_downloaded/p4g_expected*100:.1f}%)")

p5r_expected = get_expected_count('../app/src/main/assets/data/persona5/royal_personas.json')
p5r_downloaded = count_images('images/personas/p5r')
print(f"P5R:  {p5r_downloaded:3d}/{p5r_expected:3d} ({p5r_downloaded/p5r_expected*100:.1f}%)")

p3r_expected = get_expected_count('../app/src/main/assets/data/persona3reload/reload_personas.json')
p3r_downloaded = count_images('images/personas/p3r')
if p3r_expected > 0:
    print(f"P3R:  {p3r_downloaded:3d}/{p3r_expected:3d} ({p3r_downloaded/p3r_expected*100:.1f}%)")

persona_total = p4g_downloaded + p5r_downloaded + p3r_downloaded
persona_expected = p4g_expected + p5r_expected + p3r_expected
print(f"\nTotal Personas: {persona_total}/{persona_expected} ({persona_total/persona_expected*100:.1f}%)")

# Bosses
print("\nBOSSES:")
print("-" * 70)

p3r_boss_expected = get_boss_count('../app/src/main/assets/data/enemies/p3r_enemies.json')
p3r_boss_downloaded = count_images('images/enemies/p3r')
print(f"P3R:  {p3r_boss_downloaded:3d}/{p3r_boss_expected:3d} ({p3r_boss_downloaded/p3r_boss_expected*100 if p3r_boss_expected > 0 else 0:.1f}%)")

p4g_boss_expected = get_boss_count('../app/src/main/assets/data/enemies/p4g_enemies.json')
p4g_boss_downloaded = count_images('images/enemies/p4g')
print(f"P4G:  {p4g_boss_downloaded:3d}/{p4g_boss_expected:3d} ({p4g_boss_downloaded/p4g_boss_expected*100 if p4g_boss_expected > 0 else 0:.1f}%)")

p5r_boss_expected = get_boss_count('../app/src/main/assets/data/enemies/p5r_enemies.json')
p5r_boss_downloaded = count_images('images/enemies/p5r')
print(f"P5R:  {p5r_boss_downloaded:3d}/{p5r_boss_expected:3d} ({p5r_boss_downloaded/p5r_boss_expected*100 if p5r_boss_expected > 0 else 0:.1f}%)")

boss_total = p3r_boss_downloaded + p4g_boss_downloaded + p5r_boss_downloaded
boss_expected = p3r_boss_expected + p4g_boss_expected + p5r_boss_expected
print(f"\nTotal Bosses: {boss_total}/{boss_expected} ({boss_total/boss_expected*100 if boss_expected > 0 else 0:.1f}%)")

# Grand total
print("\n" + "=" * 70)
grand_total = persona_total + boss_total
grand_expected = persona_expected + boss_expected
print(f"GRAND TOTAL: {grand_total}/{grand_expected} images ({grand_total/grand_expected*100:.1f}%)")
print("=" * 70)
