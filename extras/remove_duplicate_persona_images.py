#!/usr/bin/env python3
"""
Remove duplicate persona images from personas_shared folder.
Keeps only one copy of each image.
"""

import os
from pathlib import Path
from collections import defaultdict

# Path to personas_shared folder
PERSONAS_SHARED = Path(__file__).parent.parent / "app" / "src" / "main" / "assets" / "images" / "personas_shared"

def find_duplicates():
    """Find duplicate image files (case-insensitive)."""
    files_by_lowercase = defaultdict(list)
    
    for file in PERSONAS_SHARED.glob("*.png"):
        lowercase_name = file.name.lower()
        files_by_lowercase[lowercase_name].append(file)
    
    duplicates = {name: files for name, files in files_by_lowercase.items() if len(files) > 1}
    return duplicates

def remove_duplicates():
    """Remove duplicate files, keeping only one copy."""
    duplicates = find_duplicates()
    
    if not duplicates:
        print("No duplicates found!")
        return
    
    print(f"Found {len(duplicates)} sets of duplicate files")
    removed_count = 0
    
    for lowercase_name, files in duplicates.items():
        # Sort files to keep consistent behavior
        files = sorted(files, key=lambda f: f.name)
        keep = files[0]
        remove = files[1:]
        
        print(f"\n{lowercase_name}:")
        print(f"  Keeping: {keep.name}")
        
        for file in remove:
            print(f"  Removing: {file.name}")
            try:
                file.unlink()
                removed_count += 1
            except Exception as e:
                print(f"    Error: {e}")
    
    print(f"\nRemoved {removed_count} duplicate files")

if __name__ == "__main__":
    remove_duplicates()
