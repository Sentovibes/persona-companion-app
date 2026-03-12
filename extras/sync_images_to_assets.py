#!/usr/bin/env python3
"""
Sync downloaded images to app assets folder.
This ensures debug builds have all the latest correct images.
Organizes images by game to avoid duplicates.
"""

import os
import shutil
from pathlib import Path

def sync_images():
    """
    Sync images from extras to app/src/main/assets/images
    Syncs both enemies and personas, organized by game
    Merges from both downloaded_enemies and were_missing_enemies
    """
    script_dir = Path(__file__).parent
    enemies_source = script_dir / "downloaded_enemies"
    enemies_missing_source = script_dir / "were_missing_enemies"
    personas_source = script_dir / "images" / "personas"
    target_dir = script_dir.parent / "app" / "src" / "main" / "assets" / "images"
    
    print(f"📁 Enemies source 1: {enemies_source}")
    print(f"📁 Enemies source 2: {enemies_missing_source}")
    print(f"📁 Personas source: {personas_source}")
    print(f"📁 Target: {target_dir}")
    print()
    
    # Clear old images first
    if target_dir.exists():
        print("🗑️  Clearing old images...")
        shutil.rmtree(target_dir)
        print("✅ Old images cleared")
    
    # Create target directories
    enemies_dir = target_dir / "enemies"
    personas_dir = target_dir / "personas"
    enemies_dir.mkdir(parents=True, exist_ok=True)
    personas_dir.mkdir(parents=True, exist_ok=True)
    
    stats = {
        "enemies": 0,
        "personas": 0,
        "enemies_by_game": {},
        "personas_by_game": {}
    }
    
    # Copy enemy images from both sources
    if enemies_source.exists():
        print("\n📦 Copying enemy images from downloaded_enemies...")
        for game_dir in sorted(enemies_source.iterdir()):
            if game_dir.is_dir():
                game_name = game_dir.name
                if game_name not in stats["enemies_by_game"]:
                    stats["enemies_by_game"][game_name] = 0
                
                # Create game subfolder in enemies
                game_target_dir = enemies_dir / game_name
                game_target_dir.mkdir(exist_ok=True)
                
                # Copy all images for this game
                for ext in ['*.png', '*.jpg', '*.webp']:
                    for image_file in game_dir.glob(ext):
                        target_file = game_target_dir / image_file.name
                        shutil.copy2(image_file, target_file)
                        
                        stats["enemies"] += 1
                        stats["enemies_by_game"][game_name] += 1
    
    # Copy additional enemy images from were_missing_enemies
    if enemies_missing_source.exists():
        print("\n📦 Copying additional enemy images from were_missing_enemies...")
        for game_dir in sorted(enemies_missing_source.iterdir()):
            if game_dir.is_dir():
                game_name = game_dir.name
                if game_name not in stats["enemies_by_game"]:
                    stats["enemies_by_game"][game_name] = 0
                
                # Create game subfolder in enemies
                game_target_dir = enemies_dir / game_name
                game_target_dir.mkdir(exist_ok=True)
                
                # Copy all images for this game (will overwrite if exists)
                added = 0
                for ext in ['*.png', '*.jpg', '*.webp']:
                    for image_file in game_dir.glob(ext):
                        target_file = game_target_dir / image_file.name
                        if not target_file.exists():
                            shutil.copy2(image_file, target_file)
                            stats["enemies"] += 1
                            stats["enemies_by_game"][game_name] += 1
                            added += 1
                        else:
                            # Overwrite with newer version
                            shutil.copy2(image_file, target_file)
                            added += 1
                
                if added > 0:
                    print(f"   Added {added} images for {game_name}")
    
    # Copy persona images
    if personas_source.exists():
        print("\n📦 Copying persona images...")
        for game_dir in sorted(personas_source.iterdir()):
            if game_dir.is_dir() and game_dir.name != "were-missing":
                game_name = game_dir.name
                if game_name not in stats["personas_by_game"]:
                    stats["personas_by_game"][game_name] = 0
                
                # Create game subfolder in personas
                game_target_dir = personas_dir / game_name
                game_target_dir.mkdir(exist_ok=True)
                
                # Copy all images for this game
                for ext in ['*.png', '*.jpg', '*.webp']:
                    for image_file in game_dir.glob(ext):
                        target_file = game_target_dir / image_file.name
                        shutil.copy2(image_file, target_file)
                        
                        stats["personas"] += 1
                        stats["personas_by_game"][game_name] += 1
    
    # Also copy from shared personas folder to all game folders
    shared_personas = script_dir / "images" / "shared" / "personas"
    if shared_personas.exists():
        print("\n📦 Copying shared persona images to all games...")
        shared_count = 0
        for ext in ['*.png', '*.jpg', '*.webp']:
            for image_file in shared_personas.glob(ext):
                # Copy to each game folder
                for game_name in ["p3fes", "p3p", "p3r", "p4", "p4g", "p5", "p5r"]:
                    game_target_dir = personas_dir / game_name
                    game_target_dir.mkdir(exist_ok=True)
                    target_file = game_target_dir / image_file.name
                    
                    # Only copy if doesn't exist (don't overwrite game-specific images)
                    if not target_file.exists():
                        shutil.copy2(image_file, target_file)
                        if game_name not in stats["personas_by_game"]:
                            stats["personas_by_game"][game_name] = 0
                        stats["personas"] += 1
                        stats["personas_by_game"][game_name] += 1
                        shared_count += 1
        
        print(f"   Added {shared_count} shared images across all games")
    
    print(f"\n✅ Sync complete!")
    print(f"   Enemy images: {stats['enemies']}")
    print(f"   Persona images: {stats['personas']}")
    print(f"   Total: {stats['enemies'] + stats['personas']}")
    
    # Show breakdown by game
    print("\n📊 Breakdown by game:")
    print("\n  Enemies:")
    for game, count in sorted(stats["enemies_by_game"].items()):
        print(f"    {game}: {count} images")
    
    print("\n  Personas:")
    for game, count in sorted(stats["personas_by_game"].items()):
        print(f"    {game}: {count} images")
    
    print("\n" + "="*60)
    print("✅ Debug build will now use these updated images!")
    print("📝 Images organized by game to avoid duplicates")
    print("   Structure: images/TYPE/GAME/name.png")
    print("="*60)

def main():
    print("="*60)
    print("Syncing All Images to Assets")
    print("="*60)
    print()
    
    sync_images()

if __name__ == "__main__":
    main()
