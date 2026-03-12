#!/usr/bin/env python3
"""
Check for shared enemies across games and copy them to missing game folders
"""
import os
import shutil
from pathlib import Path
import re

def get_safe_name(name):
    """Convert enemy name to safe filename"""
    safe = name.lower().replace(" ", "_").replace("/", "_").replace(":", "").replace("?", "").replace("&", "").replace("-", "_").replace("'", "")
    safe = safe.replace("___", "_")
    return safe

def check_shared_enemies():
    """Check for enemies that exist in one game but missing in another"""
    
    # Get all enemy images by game
    app_enemies_path = Path("app/src/main/assets/images/enemies")
    downloaded_enemies_path = Path("extras/downloaded_enemies")
    
    game_images = {}
    
    # Scan app assets
    print("=== Scanning app assets ===")
    for game_folder in app_enemies_path.iterdir():
        if game_folder.is_dir():
            game_name = game_folder.name
            images = set()
            for img in game_folder.glob("*.png"):
                images.add(img.stem.lower())
            game_images[game_name] = images
            print(f"{game_name}: {len(images)} images")
    
    # Scan downloaded enemies
    print("\n=== Scanning downloaded enemies ===")
    downloaded_images = {}
    for game_folder in downloaded_enemies_path.iterdir():
        if game_folder.is_dir():
            game_name = game_folder.name
            images = {}
            for img in game_folder.glob("*.*"):
                if img.suffix.lower() in ['.png', '.jpg', '.jpeg', '.webp']:
                    images[img.stem.lower()] = img
            downloaded_images[game_name] = images
            print(f"{game_name}: {len(images)} images")
    
    # Find shared enemies (enemies that appear in multiple games)
    all_enemy_names = set()
    for images in downloaded_images.values():
        all_enemy_names.update(images.keys())
    
    # Check which enemies are shared
    shared_enemies = {}
    for enemy_name in all_enemy_names:
        games_with_enemy = []
        for game_name, images in downloaded_images.items():
            if enemy_name in images:
                games_with_enemy.append(game_name)
        if len(games_with_enemy) > 1:
            shared_enemies[enemy_name] = games_with_enemy
    
    print(f"\n=== Found {len(shared_enemies)} shared enemies ===")
    
    # Now check app assets for missing shared enemies
    print("\n=== Checking for missing shared enemies in app ===")
    copied = 0
    
    for enemy_name, source_games in shared_enemies.items():
        # Check each game folder in app
        for game_name in game_images.keys():
            if game_name not in downloaded_images:
                continue
                
            # If this enemy should be in this game but isn't
            if enemy_name not in game_images[game_name]:
                # Try to find it in another game's downloaded folder
                source_file = None
                for source_game in source_games:
                    if source_game in downloaded_images and enemy_name in downloaded_images[source_game]:
                        source_file = downloaded_images[source_game][enemy_name]
                        break
                
                if source_file:
                    dest_file = app_enemies_path / game_name / source_file.name
                    if not dest_file.exists():
                        shutil.copy2(source_file, dest_file)
                        print(f"  ✓ Copied {enemy_name} from {source_game} to {game_name}")
                        copied += 1
    
    print(f"\n=== Copied {copied} shared enemy images ===")
    
    # Show final counts
    print("\n=== Final counts in app ===")
    for game_folder in app_enemies_path.iterdir():
        if game_folder.is_dir():
            count = len(list(game_folder.glob("*.png")))
            print(f"  {game_folder.name}: {count} images")

if __name__ == '__main__':
    check_shared_enemies()
