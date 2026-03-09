"""
Test Barong download manually
"""
import sys
sys.path.insert(0, '.')

from download_missing_fast import try_download_fast
import os

os.makedirs('test_images', exist_ok=True)

print("Testing Barong...")
result = try_download_fast("Barong", "test_images", "P4G")
print(f"Result: {result}")

# Also test with P5R suffix
print("\nTesting Barong with P5R...")
result = try_download_fast("Barong", "test_images", "P5R")
print(f"Result: {result}")
