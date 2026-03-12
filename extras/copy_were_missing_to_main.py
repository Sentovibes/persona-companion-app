import os
import shutil
from pathlib import Path

# Copy from were_missing_enemies to downloaded_enemies for all games
games = ['p3fes', 'p3p', 'p3r', 'p4', 'p4g', 'p5', 'p5r']

total_copied = 0

for game in games:
    source_folder = Path(f"were_missing_enemies/{game}")
    target_folder = Path(f"downloaded_enemies/{game}")
    
    if not source_folder.exists():
        continue
    
    # Create target folder if it doesn't exist
    target_folder.mkdir(parents=True, exist_ok=True)
    
    # Get all image files
    image_files = list(source_folder.glob("*.png")) + list(source_folder.glob("*.jpg")) + list(source_folder.glob("*.jpeg"))
    
    if image_files:
        print(f"\n{game.upper()}:")
        for source_file in image_files:
            target_file = target_folder / source_file.name
            
            # Skip if already exists
            if target_file.exists():
                print(f"  ⊘ Skipped {source_file.name} (already exists)")
            else:
                shutil.copy2(source_file, target_file)
                print(f"  ✓ Copied {source_file.name}")
                total_copied += 1

print("\n" + "="*70)
print(f"Total files copied: {total_copied}")
print("="*70)
