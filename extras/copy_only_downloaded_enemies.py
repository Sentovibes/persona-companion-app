#!/usr/bin/env python3
"""
Copy ONLY downloaded enemies from extras to app assets
This replaces ALL old enemy images with the new downloaded ones
"""
import os
import shutil
from pathlib import Path

def copy_downloaded_enemies():
    """Copy only downloaded enemies to app assets"""
    
    # Source folders
    downloaded_folder = Path("extras/downloaded_enemies")
    were_missing_folder = Path("extras/were_missing_enemies")
    
    # Destination base
    dest_base = Path("app/src/main/assets/images/enemies")
    
    total_copied = 0
    
    # Copy from downloaded_enemies (organized by game folders)
    if downloaded_folder.exists():
        for game_folder in downloaded_folder.iterdir():
            if game_folder.is_dir():
                game_name = game_folder.name
                dest_game_folder = dest_base / game_name
                dest_game_folder.mkdir(parents=True, exist_ok=True)
                
                print(f"\nCopying {game_name} enemies...")
                for img_file in game_folder.glob("*.*"):
                    if img_file.suffix.lower() in ['.png', '.jpg', '.jpeg', '.webp']:
                        dest_file = dest_game_folder / img_file.name
                        shutil.copy2(img_file, dest_file)
                        total_copied += 1
                        print(f"  ✓ {img_file.name}")
    
    # Copy from were_missing_enemies (flat folder)
    if were_missing_folder.exists():
        print(f"\nCopying previously missing enemies...")
        for img_file in were_missing_folder.glob("*.*"):
            if img_file.suffix.lower() in ['.png', '.jpg', '.jpeg', '.webp']:
                # Try to determine game from filename or put in shared
                # For now, put in a 'shared' folder
                dest_shared = dest_base / "shared"
                dest_shared.mkdir(parents=True, exist_ok=True)
                dest_file = dest_shared / img_file.name
                shutil.copy2(img_file, dest_file)
                total_copied += 1
                print(f"  ✓ {img_file.name}")
    
    print(f"\n=== COMPLETE ===")
    print(f"Total enemy images copied: {total_copied}")
    
    # Show what we have now
    print(f"\n=== Current enemy images in app ===")
    for game_folder in dest_base.iterdir():
        if game_folder.is_dir():
            count = len(list(game_folder.glob("*.*")))
            print(f"  {game_folder.name}: {count} images")

if __name__ == '__main__':
    print("=== Copy ONLY Downloaded Enemies ===")
    print("This will replace ALL old enemy images with new downloaded ones")
    print("\nSource folders:")
    print("  - extras/downloaded_enemies/")
    print("  - extras/were_missing_enemies/")
    print("\nDestination:")
    print("  - app/src/main/assets/images/enemies/")
    print("\nOld enemy images have been removed!")
    print()
    
    copy_downloaded_enemies()
