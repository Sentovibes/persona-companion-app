#!/usr/bin/env python3
"""
Copy P5/P5R personas from were-missing folder
"""

from PIL import Image
from pathlib import Path

# P5/P5R missing personas
p5_missing = [
    "Regent",
    "Queen's Necklace",
    "Stone of Scone",
    "Koh-i-Noor",
    "Choronzon",
    "Orlov",
    "Emperor's Amulet",
    "Hope Diamond",
    "Bugs",
    "Tsukiyomi",
    "Tsukiyomi Picaro",
    # P5R exclusive
    "Cait Sith",
    "Orichalcum",
    "Fafnir",
]

source_dir = Path("images/personas/were-missing")
dest_dir = Path("../app/src/main/assets/images/personas")

# Check what's available
print("=" * 60)
print("CHECKING WERE-MISSING FOLDER FOR P5/P5R PERSONAS")
print("=" * 60)

found = []
not_found = []

for persona_name in p5_missing:
    # Check for exact match
    filename = f"{persona_name}.png"
    source_path = source_dir / filename
    
    if source_path.exists():
        found.append((persona_name, filename))
        print(f"✓ {persona_name} -> {filename}")
    else:
        # Check for Orlov special case
        if persona_name == "Orlov":
            orlov_alt = source_dir / "300px-P5_Orlov_Model.png"
            if orlov_alt.exists():
                found.append((persona_name, "300px-P5_Orlov_Model.png"))
                print(f"✓ {persona_name} -> 300px-P5_Orlov_Model.png")
                continue
        
        not_found.append(persona_name)
        print(f"✗ {persona_name} - NOT FOUND")

print(f"\nFound: {len(found)}/14")
print(f"Not found: {len(not_found)}/14")

if not_found:
    print("\nStill missing:")
    for name in not_found:
        print(f"  - {name}")

# Copy and compress found personas
if found:
    print("\n" + "=" * 60)
    print("COPYING AND COMPRESSING")
    print("=" * 60)
    
    for persona_name, source_filename in found:
        source_path = source_dir / source_filename
        dest_path = dest_dir / f"{persona_name}.png"
        
        try:
            img = Image.open(source_path)
            
            # Convert to RGB if needed
            if img.mode in ('RGBA', 'LA', 'P'):
                background = Image.new('RGB', img.size, (255, 255, 255))
                if img.mode == 'P':
                    img = img.convert('RGBA')
                background.paste(img, mask=img.split()[-1] if img.mode in ('RGBA', 'LA') else None)
                img = background
            
            # Resize to 256px max dimension
            img.thumbnail((256, 256), Image.Resampling.LANCZOS)
            
            # Save
            img.save(dest_path, 'PNG', optimize=True)
            print(f"✓ Copied {persona_name}.png")
            
        except Exception as e:
            print(f"✗ Error copying {persona_name}: {e}")
    
    print(f"\nCopied {len(found)} personas")
