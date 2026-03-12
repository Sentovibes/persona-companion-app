import os

image_dir = 'app/src/main/assets/images/personas_shared'
renamed = 0

for filename in os.listdir(image_dir):
    if not filename.endswith('.png'):
        continue
    
    # Check if filename has any uppercase letters
    if filename != filename.lower():
        old_path = os.path.join(image_dir, filename)
        new_path = os.path.join(image_dir, filename.lower())
        
        try:
            os.rename(old_path, new_path)
            print(f"  {filename} -> {filename.lower()}")
            renamed += 1
        except Exception as e:
            print(f"  ERROR: {filename} - {e}")

print(f"\nRenamed {renamed} files to lowercase")
