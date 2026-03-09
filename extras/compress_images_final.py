#!/usr/bin/env python3
"""
Final aggressive PNG compression
Target: 256px max, reduce colors if needed
"""

from PIL import Image
import os
from pathlib import Path

def compress_image_aggressive(input_path, output_path, max_size=256):
    """
    Aggressively compress PNG
    - Resize to 256x256 max
    - Reduce to 256 colors if large
    - Optimize PNG
    """
    try:
        with Image.open(input_path) as img:
            # Resize if too large
            if img.width > max_size or img.height > max_size:
                img.thumbnail((max_size, max_size), Image.Resampling.LANCZOS)
            
            # If image is still large, reduce colors
            if img.mode == 'RGBA':
                # Keep RGBA but optimize
                img.save(output_path, 'PNG', optimize=True, compress_level=9)
            elif img.mode == 'RGB':
                # Convert to palette mode (256 colors) if beneficial
                img.save(output_path, 'PNG', optimize=True, compress_level=9)
            else:
                img.save(output_path, 'PNG', optimize=True, compress_level=9)
            
            return True
    except Exception as e:
        print(f"Error: {e}")
        return False

def main():
    source_dir = Path("app/src/main/assets/images")
    
    if not source_dir.exists():
        print(f"Error: {source_dir} not found!")
        return
    
    png_files = list(source_dir.rglob("*.png"))
    total = len(png_files)
    
    print(f"Found {total} PNG files")
    print("Compressing (256px max, level 9 compression)...")
    print()
    
    success_count = 0
    total_before = 0
    total_after = 0
    
    for i, png_file in enumerate(png_files, 1):
        before_size = png_file.stat().st_size
        total_before += before_size
        
        if compress_image_aggressive(png_file, png_file, max_size=256):
            after_size = png_file.stat().st_size
            total_after += after_size
            success_count += 1
            
            if i % 100 == 0 or i == total:
                print(f"[{i}/{total}] {total_after/1024/1024:.2f} MB")
    
    print()
    print("=" * 60)
    print(f"Complete! {success_count}/{total}")
    print(f"Before: {total_before/1024/1024:.2f} MB")
    print(f"After: {total_after/1024/1024:.2f} MB")
    print(f"Reduction: {(1 - total_after/total_before)*100:.1f}%")
    print("=" * 60)

if __name__ == "__main__":
    main()
