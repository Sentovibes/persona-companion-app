#!/usr/bin/env python3
"""
Make all persona and enemy images have transparent backgrounds using AI
Uses rembg library with U2-Net model for smart background removal

Install: pip install "rembg[cpu]" pillow
Or for GPU: pip install "rembg[gpu]" pillow

IMPORTANT: This ONLY processes PNG files in app/src/main/assets/images/
It will NOT touch any JSON database files!
"""
import os
from PIL import Image
from rembg import remove

def make_transparent_ai(image_path):
    """
    Remove background using AI model
    Much smarter than threshold-based removal
    """
    try:
        with open(image_path, 'rb') as input_file:
            input_data = input_file.read()
        
        # AI-powered background removal
        output_data = remove(input_data)
        
        # Save the result
        with open(image_path, 'wb') as output_file:
            output_file.write(output_data)
        
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
                    print(f"  Processing {file}... ({processed + 1}/{total})")
                    if make_transparent_ai(image_path):
                        processed += 1
    
    print(f"\n=== COMPLETE ===")
    print(f"Total images: {total}")
    print(f"Successfully processed: {processed}")
    print(f"Failed: {total - processed}")

if __name__ == '__main__':
    print("=== AI-Powered Background Removal ===")
    print("This uses a neural network (U2-Net) to intelligently remove backgrounds")
    print("Much better than simple threshold-based removal!")
    print("\nRequirements: pip install \"rembg[cpu]\" pillow")
    print("Or for GPU: pip install \"rembg[gpu]\" pillow")
    print("\nSAFETY: Only processes PNG files in app/src/main/assets/images/")
    print("Your JSON database files are safe!")
    print("\nWARNING: This will take longer but produces better results\n")
    
    response = input("Continue? (y/n): ")
    if response.lower() == 'y':
        process_all_images()
    else:
        print("Cancelled")
