"""
Test downloading Andras with (Uncensored) pattern
"""
import sys
sys.path.insert(0, '.')

from download_all_images import try_download_from_cdn
import os

# Create test directory
os.makedirs('test_images', exist_ok=True)

# Test Andras
print("Testing Andras download...")
result = try_download_from_cdn("Andras", "test_images", "P5R")

if result:
    print("✓ Successfully downloaded!")
    print(f"Check test_images/ folder")
else:
    print("✗ Failed to download")
