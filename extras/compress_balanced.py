#!/usr/bin/env python3
"""
Balanced compression - good quality, reasonable size
256px max, optimize PNG
"""

from PIL import Image
from pathlib import Path

def compress_image(input_path, output_path, max_size=256):
    """
    Resize to 256px max and optimize
    Keeps quality high, just reduces dimensions
    """
    try:
        with Image.open(input_path) as img:
            # Resize if needed
            if img.width > max_size or img.height > max_size:
                img.thumbnail((max_size, max_size), Image.Resampling.LANCZOS)
            
            # Save with optimization (keeps quality)
            img.save(output_path, 'PNG', optimize=True)
            return True
    except Exception as e:
        print(f"Error: {e}")
        return False

def main():
    source_dir = Path("app/src/main/assets/images")
    
    if not source_dir.exists():
        print("Error: images folder not found!")
        return
    
    png_files = list(source_dir.rglob("*.png"))
    total = len(png_files)
    
    print(f"Compressing {total} images to 256px max...")
    print("This will look good on phone screens (256px is plenty for profile pics)")
    print()
    
    total_before = 0
    total_after = 0
    
    for i, png_file in enumerate(png_files, 1):
        before_size = png_file.stat().st_size
        total_before += before_size
        
        if compress_image(png_file, png_file, max_size=256):
            after_size = png_file.stat().st_size
            total_after += after_size
            
            if i % 100 == 0 or i == total:
                print(f"[{i}/{total}] {total_after/1024/1024:.1f} MB")
    
    print()
    print("=" * 50)
    print(f"Before: {total_before/1024/1024:.1f} MB")
    print(f"After: {total_after/1024/1024:.1f} MB")
    print(f"Saved: {(total_before - total_after)/1024/1024:.1f} MB ({(1 - total_after/total_before)*100:.0f}%)")
    print("=" * 50)

if __name__ == "__main__":
    main()
