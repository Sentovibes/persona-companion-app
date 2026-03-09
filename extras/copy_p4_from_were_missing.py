#!/usr/bin/env python3
"""
Copy P4/P4G personas from were-missing folder
"""

from PIL import Image
from pathlib import Path

# P4 missing personas - check for exact matches and aliases
p4_missing = {
    "Ukobach": "Ukobach.png",
    "Yomotsu-Shikome": "Yomotsu Shikome.png",  # Space vs dash
    "Senri": "Senri.png",
    "Sylph": "Sylph.png",
    "Phoenix": None,  # Need to check if it's Feng Huang or Houou
    "Yomotsu-Ikusa": "Yomotsu-Ikusa.png",
    "Gdon": "Gdon.png",
    "Taotie": "Taotie.png",
    "Pabilsag": "Pabilsag.png",
    "Niddhoggr": "Nidhoggr.png",  # Single 'd' vs double 'd'
    "Taowu": "Taowu.png",
    "Jinn": "Jinn.png",
    "Mahakala": "Mahakala.png",
    "Ongyo-Ki": "Ongyo-Ki.png",
}

source_dir = Path("images/personas/were-missing")
dest_dir = Path("../app/src/main/assets/images/personas")

# Check what's available
available = list(source_dir.glob("*.png"))
print("=" * 60)
print("CHECKING WERE-MISSING FOLDER FOR P4 PERSONAS")
print("=" * 60)

found = []
not_found = []

for persona_name, filename in p4_missing.items():
    if filename is None:
        # Check for Phoenix aliases
        if (source_dir / "Houou.png").exists():
            found.append((persona_name, "Houou.png", "Phoenix"))
            print(f"✓ {persona_name} -> Houou.png (alias)")
        elif (source_dir / "Feng Huang.png").exists():
            found.append((persona_name, "Feng Huang.png", "Phoenix"))
            print(f"✓ {persona_name} -> Feng Huang.png (alias)")
        else:
            not_found.append(persona_name)
            print(f"✗ {persona_name} - NOT FOUND")
    elif (source_dir / filename).exists():
        found.append((persona_name, filename, persona_name))
        print(f"✓ {persona_name} -> {filename}")
    else:
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
    
    for persona_name, source_filename, dest_name in found:
        source_path = source_dir / source_filename
        dest_path = dest_dir / f"{dest_name}.png"
        
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
            print(f"✓ Copied {dest_name}.png")
            
        except Exception as e:
            print(f"✗ Error copying {persona_name}: {e}")
    
    print(f"\nCopied {len(found)} personas")
