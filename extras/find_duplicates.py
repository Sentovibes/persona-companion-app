"""
Find and remove duplicate images based on file hash
"""
import os
import hashlib
from collections import defaultdict

def get_file_hash(filepath):
    """Calculate MD5 hash of file"""
    hash_md5 = hashlib.md5()
    with open(filepath, "rb") as f:
        for chunk in iter(lambda: f.read(4096), b""):
            hash_md5.update(chunk)
    return hash_md5.hexdigest()

def find_duplicates(directory):
    """Find duplicate images in directory"""
    hashes = defaultdict(list)
    
    print(f"\nScanning {directory}...")
    
    for filename in os.listdir(directory):
        if filename.endswith('.png'):
            filepath = os.path.join(directory, filename)
            file_hash = get_file_hash(filepath)
            hashes[file_hash].append(filename)
    
    # Find duplicates
    duplicates = {h: files for h, files in hashes.items() if len(files) > 1}
    
    if duplicates:
        print(f"\n✗ Found {len(duplicates)} sets of duplicate images:")
        for file_hash, files in duplicates.items():
            print(f"\n  Duplicates (keeping first):")
            for i, filename in enumerate(files):
                if i == 0:
                    print(f"    ✓ KEEP: {filename}")
                else:
                    print(f"    ✗ DELETE: {filename}")
                    # Delete duplicate
                    os.remove(os.path.join(directory, filename))
        
        return len(duplicates)
    else:
        print(f"✓ No duplicates found")
        return 0

if __name__ == "__main__":
    print("="*70)
    print("FINDING AND REMOVING DUPLICATE IMAGES")
    print("="*70)
    
    total_removed = 0
    
    # Check personas
    persona_dir = 'images/shared/personas'
    if os.path.exists(persona_dir):
        removed = find_duplicates(persona_dir)
        total_removed += removed
    
    # Check enemies
    enemy_dir = 'images/shared/enemies'
    if os.path.exists(enemy_dir):
        removed = find_duplicates(enemy_dir)
        total_removed += removed
    
    print(f"\n{'='*70}")
    if total_removed > 0:
        print(f"Removed {total_removed} duplicate image sets")
    else:
        print("No duplicates found - all images are unique!")
    print(f"{'='*70}")
