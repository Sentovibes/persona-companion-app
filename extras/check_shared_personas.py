#!/usr/bin/env python3
"""
Check what persona images are in extras/images/shared but not in app
"""
import os

def get_images_in_folder(folder):
    """Get all PNG files in a folder"""
    if not os.path.exists(folder):
        return set()
    return set([f.replace('.png', '') for f in os.listdir(folder) if f.endswith('.png')])

def get_images_in_app():
    """Get all persona images currently in app assets"""
    all_images = set()
    for game in ['p3fes', 'p3p', 'p3r', 'p4', 'p4g', 'p5', 'p5r']:
        folder = f'app/src/main/assets/images/personas/{game}'
        if os.path.exists(folder):
            images = get_images_in_folder(folder)
            all_images.update(images)
    return all_images

def main():
    print("=== Checking Shared Personas vs App Assets ===\n")
    
    # Get images from shared folder
    shared = get_images_in_folder('extras/images/shared/personas')
    print(f"Images in extras/shared/personas: {len(shared)}")
    
    # Get images currently in app
    in_app = get_images_in_app()
    print(f"Unique persona images in app: {len(in_app)}")
    
    # Find what's in shared but not in app
    missing_from_app = shared - in_app
    
    print(f"\n=== Images in shared but NOT in app: {len(missing_from_app)} ===")
    if missing_from_app:
        for name in sorted(missing_from_app)[:30]:
            print(f"  - {name}")
        if len(missing_from_app) > 30:
            print(f"  ... and {len(missing_from_app) - 30} more")
    else:
        print("  None! All shared images are in the app.")
    
    # Find what's in app but not in shared
    not_in_shared = in_app - shared
    print(f"\n=== Images in app but NOT in shared: {len(not_in_shared)} ===")
    print("(This is normal - some images came from were-missing)")
    
    # Summary
    print(f"\n=== SUMMARY ===")
    print(f"Shared personas: {len(shared)}")
    print(f"App personas: {len(in_app)}")
    print(f"Missing from app: {len(missing_from_app)}")
    
    if len(missing_from_app) > 0:
        print(f"\nYou need to copy {len(missing_from_app)} images from shared to app!")
    else:
        print("\nAll shared images are already in the app!")

if __name__ == '__main__':
    main()
