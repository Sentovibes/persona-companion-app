"""
Merge all images into a single shared folder and create mapping JSON
This allows all games to use the same image files
"""
import os
import shutil
import json

def merge_images():
    """Merge all persona and enemy images into shared folders"""
    
    # Source folders
    persona_sources = [
        'images/personas/p3fes',
        'images/personas/p3p',
        'images/personas/p3r',
        'images/personas/p4',
        'images/personas/p4g',
        'images/personas/p5',
        'images/personas/p5r'
    ]
    
    enemy_sources = [
        'images/enemies/p3fes',
        'images/enemies/p3p',
        'images/enemies/p3r',
        'images/enemies/p4',
        'images/enemies/p4g',
        'images/enemies/p5',
        'images/enemies/p5r'
    ]
    
    # Destination folders
    persona_dest = 'images/shared/personas'
    enemy_dest = 'images/shared/enemies'
    
    os.makedirs(persona_dest, exist_ok=True)
    os.makedirs(enemy_dest, exist_ok=True)
    
    print("="*70)
    print("MERGING IMAGES INTO SHARED FOLDERS")
    print("="*70)
    
    # Merge personas
    print("\nMerging personas...")
    persona_count = 0
    for source in persona_sources:
        if os.path.exists(source):
            for filename in os.listdir(source):
                if filename.endswith('.png'):
                    src_path = os.path.join(source, filename)
                    dest_path = os.path.join(persona_dest, filename)
                    
                    # Only copy if doesn't exist (first one wins)
                    if not os.path.exists(dest_path):
                        shutil.copy2(src_path, dest_path)
                        persona_count += 1
    
    print(f"✓ Merged {persona_count} unique persona images")
    
    # Merge enemies
    print("\nMerging enemies...")
    enemy_count = 0
    for source in enemy_sources:
        if os.path.exists(source):
            for filename in os.listdir(source):
                if filename.endswith('.png'):
                    src_path = os.path.join(source, filename)
                    dest_path = os.path.join(enemy_dest, filename)
                    
                    if not os.path.exists(dest_path):
                        shutil.copy2(src_path, dest_path)
                        enemy_count += 1
    
    print(f"✓ Merged {enemy_count} unique enemy images")
    
    print(f"\n{'='*70}")
    print(f"TOTAL: {persona_count + enemy_count} unique images in shared folders")
    print(f"{'='*70}")
    print(f"\nShared folders created:")
    print(f"  - {persona_dest}")
    print(f"  - {enemy_dest}")

if __name__ == "__main__":
    merge_images()
