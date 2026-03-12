#!/usr/bin/env python3
"""
Check which downloaded enemy images are not yet in the app
"""
import os

def get_images_in_folder(folder):
    """Get all PNG files in a folder"""
    if not os.path.exists(folder):
        return set()
    return set([f.replace('.png', '') for f in os.listdir(folder) if f.endswith('.png')])

def get_images_in_app_enemies():
    """Get all enemy images currently in app assets"""
    all_images = set()
    for game in ['p3fes', 'p3p', 'p3r', 'p4', 'p4g', 'p5', 'p5r']:
        folder = f'app/src/main/assets/images/enemies/{game}'
        if os.path.exists(folder):
            images = get_images_in_folder(folder)
            all_images.update(images)
    return all_images

def main():
    print("=== Checking Downloaded Enemies vs App ===\n")
    
    # Get images currently in app
    in_app = get_images_in_app_enemies()
    print(f"Unique enemy images in app: {len(in_app)}")
    
    # Check downloaded_enemies
    games = ['p3fes', 'p3p', 'p3r', 'p4', 'p4g', 'p5', 'p5r']
    total_downloaded = 0
    total_missing = 0
    
    for game in games:
        folder = f'extras/downloaded_enemies/{game}'
        if os.path.exists(folder):
            downloaded = get_images_in_folder(folder)
            missing = downloaded - in_app
            total_downloaded += len(downloaded)
            total_missing += len(missing)
            
            if len(downloaded) > 0:
                print(f"{game}: {len(downloaded)} downloaded, {len(missing)} not in app")
    
    # Check were_missing_enemies
    were_missing_folder = 'extras/were_missing_enemies'
    if os.path.exists(were_missing_folder):
        # Check all subfolders
        were_missing_total = 0
        were_missing_not_in_app = 0
        
        for root, dirs, files in os.walk(were_missing_folder):
            for file in files:
                if file.endswith('.png'):
                    were_missing_total += 1
                    name = file.replace('.png', '')
                    if name not in in_app:
                        were_missing_not_in_app += 1
        
        print(f"\nwere_missing_enemies: {were_missing_total} total, {were_missing_not_in_app} not in app")
        total_downloaded += were_missing_total
        total_missing += were_missing_not_in_app
    
    print(f"\n=== SUMMARY ===")
    print(f"Total downloaded: {total_downloaded}")
    print(f"Not yet in app: {total_missing}")
    print(f"Already in app: {total_downloaded - total_missing}")

if __name__ == '__main__':
    main()
