"""
Backup all downloaded images and clean directories for fresh scrape
"""
import os
import shutil
from datetime import datetime

def backup_images():
    """Backup all images to a timestamped folder"""
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    backup_dir = f"images_backup_{timestamp}"
    
    if os.path.exists("images"):
        print(f"Backing up images to {backup_dir}...")
        shutil.copytree("images", backup_dir)
        print(f"✓ Backup complete: {backup_dir}")
        return backup_dir
    else:
        print("No images directory found")
        return None

def clean_images():
    """Delete all images"""
    if os.path.exists("images"):
        print("\nDeleting all images...")
        shutil.rmtree("images")
        print("✓ All images deleted")
    else:
        print("No images directory to delete")

if __name__ == "__main__":
    print("="*70)
    print("Backup and Clean Images")
    print("="*70)
    
    # Backup first
    backup_path = backup_images()
    
    # Then clean
    clean_images()
    
    print(f"\n{'='*70}")
    if backup_path:
        print(f"Backup saved to: {backup_path}")
    print("Ready for fresh scrape!")
    print(f"{'='*70}")
