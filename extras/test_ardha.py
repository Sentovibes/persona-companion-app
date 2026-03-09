"""
Test downloading Ardha with SMT patterns
"""
import sys
sys.path.insert(0, '.')

from download_all_images import try_download_from_cdn
import os

# Create test directory
os.makedirs('test_images', exist_ok=True)

# Test Ardha
print("Testing Ardha download...")
result = try_download_from_cdn("Ardha", "test_images", "P5R")

if result:
    print("✓ Successfully downloaded!")
    print(f"Check test_images/ folder")
else:
    print("✗ Failed to download")
