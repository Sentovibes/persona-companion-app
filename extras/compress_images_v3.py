#!/usr/bin/env python3
"""
Aggressive PNG compression - convert to WebP for smaller size
"""

from PIL import Image
import os
from pathlib import Path

def compress_to_webp(input_path, output_path, max_size=256, quality=80):
    """
    Convert PNG to WebP with compression
    - Resize to max_size x max_size (maintaining aspect ratio)
    - Convert to WebP format (much smaller)
    - Quality 80 (good balance)
    """
    try:
        with Image.open(input_path) as img:
            # Resize if too large
            if img.width > max_size or img.height > max_size:
                img.thumbnail((max_size, max_size), Image.Resampling.LANCZOS)
            
            # Change extension to .webp
            output_webp = output_path.with_suffix('.webp')
            
            # Save as WebP
            img.save(output_webp, 'WEBP', quality=quality)
            
            # Delete original PNG
            input_path.unlink()
            
            return True, output_webp.stat().st_size
    except Exception as e:
        print(f"Error compressing {input_path}: {e}")
        return False, 0

def main():
    # Source and destination
    source_dir = Path("app/src/main/assets/images")
    
    if not source_dir.exists():
        print(f"Error: {source_dir} not found!")
        return
    
    # Get all PNG files
    png_files = list(source_dir.rglob("*.png"))
    total = len(png_files)
    
    print(f"Found {total} PNG files to convert to WebP")
    print("Converting to WebP (256x256 max, quality 80)...")
    print()
    
    success_count = 0
    total_before = 0
    total_after = 0
    
    for i, png_file in enumerate(png_files, 1):
        before_size = png_file.stat().st_size
        total_before += before_size
        
        # Convert to WebP
        success, after_size = compress_to_webp(png_file, png_file, max_size=256, quality=80)
        if success:
            total_after += after_size
            success_count += 1
            
            if i % 50 == 0 or i == total:
                print(f"[{i}/{total}] Processed... Current total: {total_after/1024/1024:.2f} MB")
        else:
            print(f"[{i}/{total}] {png_file.name}: FAILED")
    
    print()
    print("=" * 60)
    print(f"Conversion complete!")
    print(f"Success: {success_count}/{total}")
    print(f"Total before (PNG): {total_before/1024/1024:.2f} MB")
    print(f"Total after (WebP): {total_after/1024/1024:.2f} MB")
    print(f"Total reduction: {(1 - total_after/total_before)*100:.1f}%")
    print("=" * 60)
    print()
    print("NOTE: You need to update ImageUtils.kt to load .webp instead of .png")

if __name__ == "__main__":
    main()
