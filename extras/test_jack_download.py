"""
Test downloading Jack-o'-Lantern with the updated logic
"""
import sys
sys.path.insert(0, '.')

from download_all_images import try_download_from_cdn
import os

# Create test directory
os.makedirs('test_images', exist_ok=True)

# Test Jack-o'-Lantern
print("Testing Jack-o'-Lantern download...")
result = try_download_from_cdn("Jack-o'-Lantern", "test_images", "P5R")

if result:
    print("✓ Successfully downloaded!")
    print(f"Check test_images/ folder")
else:
    print("✗ Failed to download")
