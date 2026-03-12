#!/usr/bin/env python3
"""
Replace app enemy images with ONLY downloaded transparent images
"""
import shutil
from pathlib import Path

def replace_enemy_images():
    # Source folders (downloaded transparent images)
    downloaded_source = Path("extras/downloaded_enemies")
    were_missing_source = Path("extras/were_missing_enemies")
    
    # Destination folder (app assets)
    dest = Path("app/src/main/assets/images/enemies_shared")
    
    # Backup current images
    backup = Path("app/src/main/assets/images/enemies_shared_OLD_BACKUP")
    if dest.exists() and not backup.exists():
        print(f"Backing up current images to: {backup}")
        shutil.copytree(dest, backup)
    
    # Clear destination
    if dest.exists():
        shutil.rmtree(dest)
    dest.mkdir(parents=True, exist_ok=True)
    
    # Copy downloaded images from all game subfolders
    copied_count = 0
    if downloaded_source.exists():
        for game_folder in downloaded_source.iterdir():
            if game_folder.is_dir():
                print(f"Processing {game_folder.name}...")
                for img in game_folder.glob("*.png"):
                    dest_file = dest / img.name
                    if not dest_file.exists():  # Don't overwrite if already copied from another game
                        shutil.copy2(img, dest_file)
                        copied_count += 1
                    # else:
                    #     print(f"  Skipping {img.name} (already exists)")
        print(f"Copied {copied_count} unique images from downloaded_enemies")
    
    # Copy were_missing images from all game subfolders
    were_missing_count = 0
    if were_missing_source.exists():
        for game_folder in were_missing_source.iterdir():
            if game_folder.is_dir():
                for img in game_folder.glob("*.png"):
                    dest_file = dest / img.name
                    if not dest_file.exists():  # Don't overwrite
                        shutil.copy2(img, dest_file)
                        were_missing_count += 1
        print(f"Copied {were_missing_count} images from were_missing_enemies")
    
    # Final count
    final_count = len(list(dest.glob("*.png")))
    print(f"\n=== Summary ===")
    print(f"Total enemy images in app: {final_count}")
    print(f"  From downloaded: {copied_count}")
    print(f"  From were_missing: {were_missing_count}")
    print(f"\nOld images backed up to: {backup}")
    print(f"New images location: {dest}")

if __name__ == '__main__':
    replace_enemy_images()
