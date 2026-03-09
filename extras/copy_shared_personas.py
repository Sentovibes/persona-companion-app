"""
Copy shared persona images between games to avoid re-downloading
Many personas appear in multiple games
"""
import json
import os
import shutil

def get_persona_names(filepath):
    """Get list of persona names from JSON file"""
    if not os.path.exists(filepath):
        return set()
    
    with open(filepath, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    if isinstance(data, dict):
        return set(data.keys())
    else:
        return set(data)

def copy_shared_images(source_game, target_game, source_dir, target_dir, source_personas, target_personas):
    """Copy images for personas that exist in both games"""
    shared = source_personas & target_personas
    
    if not shared:
        print(f"No shared personas between {source_game} and {target_game}")
        return 0
    
    os.makedirs(target_dir, exist_ok=True)
    
    copied = 0
    for persona_name in shared:
        # Find the image file in source directory
        found = False
        for ext in ['.png', '.jpg', '.jpeg', '.webp']:
            source_file = os.path.join(source_dir, f"{persona_name}{ext}")
            if os.path.exists(source_file):
                target_file = os.path.join(target_dir, f"{persona_name}{ext}")
                
                # Only copy if target doesn't exist
                if not os.path.exists(target_file):
                    shutil.copy2(source_file, target_file)
                    copied += 1
                    print(f"  Copied: {persona_name}")
                
                found = True
                break
        
        if not found:
            print(f"  Missing: {persona_name} (not in source)")
    
    return copied

def main():
    print("=" * 60)
    print("  COPY SHARED PERSONA IMAGES")
    print("=" * 60)
    
    # Load persona lists
    p4g_personas = get_persona_names('../app/src/main/assets/data/persona4/golden_personas.json')
    p5r_personas = get_persona_names('../app/src/main/assets/data/persona5/royal_personas.json')
    p3r_personas = get_persona_names('../app/src/main/assets/data/persona3reload/reload_personas.json')
    
    print(f"\nP4G Personas: {len(p4g_personas)}")
    print(f"P5R Personas: {len(p5r_personas)}")
    print(f"P3R Personas: {len(p3r_personas)}")
    
    # Find overlaps
    p4g_p5r_shared = p4g_personas & p5r_personas
    p4g_p3r_shared = p4g_personas & p3r_personas
    p5r_p3r_shared = p5r_personas & p3r_personas
    
    print(f"\nShared between P4G and P5R: {len(p4g_p5r_shared)}")
    print(f"Shared between P4G and P3R: {len(p4g_p3r_shared)}")
    print(f"Shared between P5R and P3R: {len(p5r_p3r_shared)}")
    
    total_copied = 0
    
    # Copy P4G -> P5R
    if p4g_p5r_shared:
        print(f"\n--- Copying P4G -> P5R ---")
        copied = copy_shared_images(
            'P4G', 'P5R',
            'images/personas/p4g',
            'images/personas/p5r',
            p4g_personas,
            p5r_personas
        )
        total_copied += copied
        print(f"Copied {copied} images")
    
    # Copy P4G -> P3R
    if p4g_p3r_shared:
        print(f"\n--- Copying P4G -> P3R ---")
        copied = copy_shared_images(
            'P4G', 'P3R',
            'images/personas/p4g',
            'images/personas/p3r',
            p4g_personas,
            p3r_personas
        )
        total_copied += copied
        print(f"Copied {copied} images")
    
    # Copy P5R -> P3R (if P5R has images)
    if p5r_p3r_shared and os.path.exists('images/personas/p5r'):
        print(f"\n--- Copying P5R -> P3R ---")
        copied = copy_shared_images(
            'P5R', 'P3R',
            'images/personas/p5r',
            'images/personas/p3r',
            p5r_personas,
            p3r_personas
        )
        total_copied += copied
        print(f"Copied {copied} images")
    
    print(f"\n{'=' * 60}")
    print(f"Total images copied: {total_copied}")
    print(f"This saved downloading {total_copied} images!")
    print(f"{'=' * 60}")

if __name__ == '__main__':
    main()
