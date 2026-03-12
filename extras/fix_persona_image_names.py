import os
import re

# Directory containing persona images
image_dir = 'app/src/main/assets/images/personas_shared'

if not os.path.exists(image_dir):
    print(f"Directory not found: {image_dir}")
    exit(1)

renamed_count = 0
files = os.listdir(image_dir)

print(f"Found {len(files)} files in {image_dir}")
print("\nRenaming files with spaces to underscores...")

for filename in files:
    if not filename.endswith('.png'):
        continue
    
    # Check if filename has spaces
    if ' ' in filename:
        # Create new filename with underscores
        new_filename = filename.replace(' ', '_')
        
        old_path = os.path.join(image_dir, filename)
        new_path = os.path.join(image_dir, new_filename)
        
        # Rename the file
        os.rename(old_path, new_path)
        print(f"  {filename} -> {new_filename}")
        renamed_count += 1

print(f"\nRenamed {renamed_count} files")
