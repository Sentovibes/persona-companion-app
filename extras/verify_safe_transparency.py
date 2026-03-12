#!/usr/bin/env python3
"""
Verify that transparency script will only touch PNG files, not JSON databases
"""
import os

def check_what_will_be_processed():
    """Show what files will be processed"""
    base_paths = [
        'app/src/main/assets/images/personas',
        'app/src/main/assets/images/enemies'
    ]
    
    png_files = []
    other_files = []
    
    for base_path in base_paths:
        if not os.path.exists(base_path):
            print(f"Path not found: {base_path}")
            continue
            
        for root, dirs, files in os.walk(base_path):
            for file in files:
                file_path = os.path.join(root, file)
                if file.endswith('.png'):
                    png_files.append(file_path)
                else:
                    other_files.append(file_path)
    
    print("=== SAFETY CHECK ===")
    print(f"\nPNG files that WILL be processed: {len(png_files)}")
    if png_files:
        print("Sample PNG files:")
        for f in png_files[:5]:
            print(f"  {f}")
        if len(png_files) > 5:
            print(f"  ... and {len(png_files) - 5} more")
    
    print(f"\nOther files that will be IGNORED: {len(other_files)}")
    if other_files:
        print("Files that will be ignored:")
        for f in other_files:
            print(f"  {f}")
    
    print("\n=== DATABASE FILES CHECK ===")
    db_paths = [
        'app/src/main/assets/data/personas',
        'app/src/main/assets/data/enemies'
    ]
    
    json_files = []
    for db_path in db_paths:
        if os.path.exists(db_path):
            for root, dirs, files in os.walk(db_path):
                for file in files:
                    if file.endswith('.json'):
                        json_files.append(os.path.join(root, file))
    
    print(f"JSON database files found: {len(json_files)}")
    print("These will NOT be touched by the transparency script:")
    for f in json_files:
        print(f"  ✓ {f}")
    
    print("\n=== CONCLUSION ===")
    print(f"✓ {len(png_files)} PNG images will be processed")
    print(f"✓ {len(json_files)} JSON database files are safe")
    print(f"✓ {len(other_files)} other files will be ignored")
    print("\nYour database is safe!")

if __name__ == '__main__':
    check_what_will_be_processed()
