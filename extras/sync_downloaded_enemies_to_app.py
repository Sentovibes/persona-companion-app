#!/usr/bin/env python3
"""
Sync downloaded enemy images from extras to app assets
This will replace old images with the new downloaded ones
"""
import os
import shutil
from pathlib import Path

def sync_enemies():
    """Copy downloaded enemies to app assets"""
    source_base = Path("extras/downloaded_enemies")
    dest_base = Path("app/src/main/assets/images/enemies")
    
    if not source_base.exists():
        print(f"Source folder not found: {source_base}")
        return
    
    if not dest_base.exists():
        print(f"Destination folder not found: {dest_base}")
        return
    
    copied = 0
    skipped = 0
    errors = 0
    
    # Process each game folder
    for game_folder in source_base.iterdir():
        if not game_folder.is_dir():
            continue
        
        game_id = game_folder.name
        dest_folder = dest_base / game_id
        
        print(f"\nProcessing {game_id}...")
        
        if not dest_folder.exists():
            dest_folder.mkdir(parents=True)
            print(f"  Created folder: {dest_folder}")
        
        # Copy all images from this game
        for img_file in game_folder.glob("*.*"):
            if img_file.suffix.lower() in ['.png', '.jpg', '.jpeg', '.webp']:
                dest_file = dest_folder / img_file.name
                
                try:
                    shutil.copy2(img_file, dest_file)
                    copied += 1
                    if copied % 100 == 0:
                        print(f"  Copied {copied} files...")
                except Exception as e:
                    print(f"  Error copying {img_file.name}: {e}")
                    errors += 1
    
    # Also copy from were_missing_enemies
    missing_source = Path("extras/were_missing_enemies")
    if missing_source.exists():
        print(f"\nProcessing were_missing_enemies...")
        for img_file in missing_source.glob("*.*"):
            if img_file.suffix.lower() in ['.png', '.jpg', '.jpeg', '.webp']:
                