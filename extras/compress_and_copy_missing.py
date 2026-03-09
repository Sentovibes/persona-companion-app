#!/usr/bin/env python3
"""
Compress and copy the were-missing personas to app assets
"""

from PIL import Image
from pathlib import Path
import shutil

source_dir = Path("images/personas/were-missing")
dest_dir = Path("../app/src/main/assets/images/personas")

# Ensure destination exists
dest_dir.mkdir(parents=True, exist_ok=True)

processed = []
errors = []

for img_path in sorted(source_dir.glob("*.png")):
    try:
        # Open and compress
        img = Image.open(img_path)
        
        # Convert to RGB if needed (remove alpha for compression)
        if img.mode in ('RGBA', 'LA', 'P'):
            background = Image.new('RGB', img.size, (255, 255, 255))
            if img.mode == 'P':
                img = img.convert('RGBA')
            background.paste(img, mask=img.split()[-1] if img.mode in ('RGBA', 'LA') else None)
            img = background
        
        # Resize to 256px max dimension
        img.thumbnail((256, 256), Image.Resampling.LANCZOS)
        
        # Save to destination
        dest_path = dest_dir / img_path.name.strip()  # Strip any extra spaces
        img.save(dest_path, 'PNG', optimize=True)
        
        processed.append(img_path.name)
        print(f"✓ {img_path.name} -> {dest_path.name}")
        
    except Exception as e:
        errors.append((img_path.name, str(e)))
        print(f"✗ {img_path.name}: {e}")

print("\n" + "=" * 60)
print(f"Processed: {len(processed)} images")
if errors:
    print(f"Errors: {len(errors)}")
    for name, error in errors:
        print(f"  - {name}: {error}")
