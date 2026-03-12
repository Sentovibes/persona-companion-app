#!/usr/bin/env python3
"""
Consolidate all persona and enemy images into single shared folders.
The app will look up images by name instead of by game folder.
"""
import os
import shutil
from pathlib import Path
from collections import defaultdict

def consolidate_images():
    """Consolidate all images into shared folders"""
    
    # Source folders
    personas_base = Path("app/src/main/assets/images/personas")
    enemies_base = Path("app/src/main/assets/images/enemies")
    
    # Destination folders
    personas_shared = Path("app/src/main/assets/images/personas_shared")
    enemies_shared = Path("app/src/main/assets/images/enemies_shared")
    
    # Create destination folders
    personas_shared.mkdir(parents=True, exist_ok=True)
    enemies_shared.mkdir(parents=True, exist_ok=True)
    
    # Track duplicates
    persona_files = {}
    enemy_files = {}
    
    print("=== Consolidating Persona Images ===")
    if personas_base.exists():
        for game_folder in personas_base.iterdir():
            if game_folder.is_dir():
                print(f"\nProcessing {game_folder.name}...")
                for img_file in game_folder.glob("*.png"):
                    filename = img_file.name
                    if filename not in persona_files:
                        # First time seeing this file - copy it
                        dest_file = personas_shared / filename
                        shutil.copy2(img_file, dest_file)
                        persona_files[filename] = game_folder.name
                        print(f"  ✓ {filename}")
                    else:
                        # Duplicate - skip
                        print(f"  ⊘ {filename} (already from {persona_files[filename]})")
    
    print("\n=== Consolidating Enemy Images ===")
    if enemies_base.exists():
        for game_folder in enemies_base.iterdir():
            if game_folder.is_dir():
                print(f"\nProcessing {game_folder.name}...")
                for img_file in game_folder.glob("*.png"):
                    filename = img_file.name
                    if filename not in enemy_files:
                        # First time seeing this file - copy it
                        dest_file = enemies_shared / filename
                        shutil.copy2(img_file, dest_file)
                        enemy_files[filename] = game_folder.name
                        print(f"  ✓ {filename}")
                    else:
                        # Duplicate - skip
                        print(f"  ⊘ {filename} (already from {enemy_files[filename]})")
    
    print("\n=== Summary ===")
    print(f"Unique persona images: {len(persona_files)}")
    print(f"Unique enemy images: {len(enemy_files)}")
    
    # Calculate space saved
    original_persona_count = sum(1 for _ in personas_base.rglob("*.png")) if personas_base.exists() else 0
    original_enemy_count = sum(1 for _ in enemies_base.rglob("*.png")) if enemies_base.exists() else 0
    
    persona_duplicates = original_persona_count - len(persona_files)
    enemy_duplicates = original_enemy_count - len(enemy_files)
    
    print(f"\nOriginal persona images: {original_persona_count}")
    print(f"Duplicates removed: {persona_duplicates}")
    print(f"\nOriginal enemy images: {original_enemy_count}")
    print(f"Duplicates removed: {enemy_duplicates}")
    print(f"\nTotal duplicates removed: {persona_duplicates + enemy_duplicates}")
    
    # Now remove old folders
    print("\n=== Removing old game-specific folders ===")
    if personas_base.exists():
        shutil.rmtree(personas_base)
        print("✓ Removed personas/")
    if enemies_base.exists():
        shutil.rmtree(enemies_base)
        print("✓ Removed enemies/")
    
    print("\n=== COMPLETE ===")
    print(f"All images consolidated into:")
    print(f"  - {personas_shared}")
    print(f"  - {enemies_shared}")

if __name__ == '__main__':
    consolidate_images()
