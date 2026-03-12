#!/usr/bin/env python3
"""
Script to prepare images for CDN hosting.
This creates a manifest.json file listing all images and their metadata.
"""

import os
import json
from pathlib import Path

def get_file_size(filepath):
    """Get file size in bytes"""
    return os.path.getsize(filepath)

def create_manifest(images_dir, output_file):
    """
    Create a manifest.json file listing all images.
    
    Format:
    [
        {
            "filename": "jack_frost.png",
            "path": "personas/p5/jack_frost.png",
            "category": "personas",
            "game": "p5",
            "size": 123456
        },
        ...
    ]
    """
    manifest = []
    
    # Walk through all subdirectories
    for root, dirs, files in os.walk(images_dir):
        for file in files:
            if file.endswith('.png') or file.endswith('.jpg') or file.endswith('.webp'):
                filepath = os.path.join(root, file)
                relative_path = os.path.relpath(filepath, images_dir)
                
                # Parse category and game from path
                parts = relative_path.split(os.sep)
                category = parts[0] if len(parts) > 0 else "unknown"
                game = parts[1] if len(parts) > 1 else "unknown"
                
                manifest.append({
                    "filename": file,
                    "path": relative_path.replace(os.sep, "/"),  # Use forward slashes for URLs
                    "category": category,
                    "game": game,
                    "size": get_file_size(filepath)
                })
    
    # Sort by path for consistency
    manifest.sort(key=lambda x: x["path"])
    
    # Write manifest
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(manifest, f, indent=2)
    
    # Print statistics
    total_size = sum(item["size"] for item in manifest)
    print(f"Created manifest with {len(manifest)} images")
    print(f"Total size: {total_size / (1024*1024):.2f} MB")
    print(f"Manifest saved to: {output_file}")
    
    # Print breakdown by category
    categories = {}
    for item in manifest:
        cat = item["category"]
        if cat not in categories:
            categories[cat] = {"count": 0, "size": 0}
        categories[cat]["count"] += 1
        categories[cat]["size"] += item["size"]
    
    print("\nBreakdown by category:")
    for cat, stats in sorted(categories.items()):
        print(f"  {cat}: {stats['count']} images, {stats['size'] / (1024*1024):.2f} MB")

def main():
    # Assuming script is in extras/ and images are in downloaded_enemies/
    script_dir = Path(__file__).parent
    
    # You can change this to point to wherever you want to prepare images from
    images_dir = script_dir / "downloaded_enemies"
    
    if not images_dir.exists():
        print(f"Error: Images directory not found: {images_dir}")
        print("Please update the images_dir path in the script")
        return
    
    output_file = script_dir / "manifest.json"
    
    print(f"Scanning images in: {images_dir}")
    create_manifest(images_dir, output_file)
    
    print("\n" + "="*60)
    print("Next steps:")
    print("1. Upload all images to your CDN/hosting service")
    print("2. Upload manifest.json to the same location")
    print("3. Update BASE_URL in ImageDownloadManager.kt with your CDN URL")
    print("4. Test the download feature in a release build")
    print("="*60)

if __name__ == "__main__":
    main()
