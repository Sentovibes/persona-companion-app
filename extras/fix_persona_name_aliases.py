#!/usr/bin/env python3
"""
Fix persona name mismatches by copying images with correct names
"""
import os
import shutil

# Name aliases: JSON name -> Image file name
ALIASES = {
    'Niddhoggr': 'Nidhoggr',
    'Phoenix': 'Houou',
    'Yomotsu-Shikome': 'Yomotsu Shikome',
}

def main():
    print("=== Fixing persona name aliases ===\n")
    
    games = ['p3fes', 'p3p', 'p3r', 'p4', 'p4g', 'p5', 'p5r']
    
    for json_name, image_name in ALIASES.items():
        print(f"\nLooking for {image_name}.png to copy as {json_name}.png...")
        
        for game in games:
            src_path = f'app/src/main/assets/images/personas/{game}/{image_name}.png'
            dest_path = f'app/src/main/assets/images/personas/{game}/{json_name}.png'
            
            if os.path.exists(src_path) and not os.path.exists(dest_path):
                shutil.copy2(src_path, dest_path)
                print(f"  ✓ Copied to {game}/{json_name}.png")

if __name__ == '__main__':
    main()
