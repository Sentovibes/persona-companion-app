#!/usr/bin/env python3
"""
Make all persona and enemy images have transparent backgrounds
Removes white/near-white backgrounds
"""
import os
from PIL import Image

def make_transparent(image_path, threshold=240):
    """
    Make white backgrounds transparent
    threshold: RGB value above which pixels are considered white (0-255)
    """
    try:
        img = Image.open(image_path).convert("RGBA")
        datas = img.getdata()
        
        new_data = []
        for item in datas:
            # If pixel is mostly white (all RGB values above threshold)
            if item[0] > threshold and item[1] > threshold and item[2] > threshold:
                # Make it transparent
                new_data.append((255, 255, 255, 0))
            else:
                new_data.append(item)
        
        img.putdata(new_data)
        img.save(image_path, "PNG")
        return True
    except Exception as e:
        print(f"Error processing {image_path}: {e}")
        return False

def process_all_images():
    """Process all persona and enemy images"""
    base_paths = [
        'app/src/main/assets/images/personas',
        'app/src/main/assets/images/enemies'
    ]
    
    total = 0
    processed = 0
    
    for base_path in base_paths:
        if not os.path.exists(base_path):
            continue
            
        print(f"\nProcessing {base_path}...")
        
        for root, dirs, files in os.walk(base_path):
            for file in files:
                if file.endswith('.png'):
                    total += 1
                    image_path = os.path.join(root, file)
                    if make_transparent(image_path):
                        processed += 1
                        if processed % 100 == 0:
                            print(f"  Processed {processed}/{total}...")
    
    print(f"\n=== COMPLETE ===")
    print(f"Total images: {total}")
    print(f"Successfully processed: {processed}")
    print(f"Failed: {total - processed}")

if __name__ == '__main__':
    print("=== Making All Images Transparent ===")
    print("This will remove white backgrounds from all persona and enemy images")
    print("Threshold: RGB > 240 = transparent\n")
    
    response = input("Continue? (y/n): ")
    if response.lower() == 'y':
        process_all_images()
    else:
        print("Cancelled")
