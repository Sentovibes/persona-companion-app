#!/usr/bin/env python3
"""
Compress and copy the new P3 FES personas from were-missing
"""

from PIL import Image
from pathlib import Path

# The 9 new P3 FES personas
new_personas = [
    "Yomotsu Shikome.png",
    "Ghoul.png",
    "Empusa.png",
    "Nata Taishi.png",
    "Girimehkala.png",
    "Oukuninushi.png",
    "Kingu.png",
    "Laksmi.png",
    "Seiten Taisei.png"
]

source_dir = Path("images/personas/were-missing")
dest_dir = Path("../app/src/main/assets/images/personas")

processed = []
errors = []

for filename in new_personas:
    img_path = source_dir / filename
    
    if not img_path.exists():
        errors.append((filename, "File not found"))
        print(f"✗ {filename} - NOT FOUND")
        continue
    
    try:
        # Open and compress
        img = Image.open(img_path)
        
        # Convert to RGB if needed
        if img.mode in ('RGBA', 'LA', 'P'):
            background = Image.new('RGB', img.size, (255, 255, 255))
            if img.mode == 'P':
                img = img.convert('RGBA')
            background.paste(img, mask=img.split()[-1] if img.mode in ('RGBA', 'LA') else None)
            img = background
        
        # Resize to 256px max dimension
        img.thumbnail((256, 256), Image.Resampling.LANCZOS)
        
        # Save to destination
        dest_path = dest_dir / filename
        img.save(dest_path, 'PNG', optimize=True)
        
        processed.append(filename)
        print(f"✓ {filename}")
        
    except Exception as e:
        errors.append((filename, str(e)))
        print(f"✗ {filename}: {e}")

print("\n" + "=" * 60)
print(f"Processed: {len(processed)}/{len(new_personas)} images")
if errors:
    print(f"Errors: {len(errors)}")
    for name, error in errors:
        print(f"  - {name}: {error}")
