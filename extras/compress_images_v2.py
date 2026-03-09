#!/usr/bin/env python3
"""
Compress PNG images properly - keep transparency, just resize and optimize
"""

from PIL import Image
import os
from pathlib import Path

def compress_image(input_path, output_path, max_size=300):
    """
    Compress and resize image while keeping transparency
    - Resize to max_size x max_size (maintaining aspect ratio)
    - Keep original format (RGBA if transparent)
    - Optimize PNG compression
    """
    try:
        with Image.open(input_path) as img:
            # Resize if too large
            if img.width > max_size or img.height > max_size:
                img.thumbnail((max_size, max_size), Image.Resampling.LANCZOS)
            
            # Save with optimization (keep original mode)
            img.save(output_path, 'PNG', optimize=True)
            
            return True
    except Exception as e:
        print(f"Error compressing {input_path}: {e}")
        return False

def main():
    # Source and destination
    source_dir = Path("app/src/main/assets/images")
    
    if not source_dir.exists():
        print(f"Error: {source_dir} not found!")
        return
    
    # Get all PNG files
    png_files = list(source_dir.rglob("*.png"))
    total = len(png_files)
    
    print(f"Found {total} PNG files to compress")
    print("Compressing images (300x300 max, optimized, keeping transparency)...")
    print()
    
    success_count = 0
    total_before = 0
    total_after = 0
    
    for i, png_file in enumerate(png_files, 1):
        before_size = png_file.stat().st_size
        total_before += before_size
        
        # Compress in place
        if compress_image(png_file, png_file, max_size=400):
            after_size = png_file.stat().st_size
            total_after += after_size
            
            reduction = (1 - after_size / before_size) * 100
            success_count += 1
            
            if i % 50 == 0 or i == total:
                print(f"[{i}/{total}] Processed... Current total: {total_after/1024/1024:.2f} MB")
        else:
            print(f"[{i}/{total}] {png_file.name}: FAILED")
    
    print()
    print("=" * 60)
    print(f"Compression complete!")
    print(f"Success: {success_count}/{total}")
    print(f"Total before: {total_before/1024/1024:.2f} MB")
    print(f"Total after: {total_after/1024/1024:.2f} MB")
    print(f"Total reduction: {(1 - total_after/total_before)*100:.1f}%")
    print("=" * 60)

if __name__ == "__main__":
    main()
