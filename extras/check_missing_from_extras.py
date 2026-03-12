#!/usr/bin/env python3
"""
Check what persona images are in extras but not in app assets
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
    print("=== Checking Extras vs App Assets ===\n")
    
    # Get images from were-missing folder
    were_missing = get_images_in_folder('extras/images/personas/were-missing')
    print(f"Images in extras/were-missing: {len(were_missing)}")
    
    # Get images currently in app
    in_app = get_images_in_app()
    print(f"Unique persona images in app: {len(in_app)}")
    
    # Find what's in extras but not in app
    missing_from_app = were_missing - in_app
    
    print(f"\n=== Images in extras but NOT in app: {len(missing_from_app)} ===")
    if missing_from_app:
        for name in sorted(missing_from_app):
            print(f"  - {name}")
    else:
        print("  None! All extras images are in the app.")
    
    # Find what's in app but not in extras
    not_in_extras = in_app - were_missing
    print(f"\n=== Images in app but NOT in extras: {len(not_in_extras)} ===")
    print("(This is normal - most images came from other sources)")
    
    # Summary
    print(f"\n=== SUMMARY ===")
    print(f"Total unique persona names in app: {len(in_app)}")
    print(f"Images in extras/were-missing: {len(were_missing)}")
    print(f"Missing from app: {len(missing_from_app)}")
    
    if len(missing_from_app) > 0:
        print(f"\nYou need to copy {len(missing_from_app)} images from extras to app!")
    else:
        print("\nAll extras images are already in the app!")

if __name__ == '__main__':
    main()
