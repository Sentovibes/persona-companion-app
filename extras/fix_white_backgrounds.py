#!/usr/bin/env python3
"""
Fix white backgrounds on persona images - make them transparent
"""

from PIL import Image
from pathlib import Path

images_dir = Path("../app/src/main/assets/images/personas")

# List of personas that were recently added (might have white backgrounds)
recent_personas = [
    "Ananta", "Anat", "Chimera", "Cybele", "Ganga", "Gurulu", "Houou",
    "Kamu Susano-o", "Kushi Mitama", "Legion", "Mercurius", "Nidhoggr",
    "Omoikane", "Sandman", "Shiisaa", "Shiva", "Take-Mikazuchi", "Tam Lin",
    "Virtue", "Yakshini", "Yurlungur",
    # P3 FES/P3P
    "Yomotsu Shikome", "Ghoul", "Empusa", "Nata Taishi", "Girimehkala",
    "Oukuninushi", "Kingu", "Laksmi", "Seiten Taisei",
    # P4/P4G
    "Ukobach", "Yomotsu-Shikome", "Senri", "Sylph", "Phoenix", "Yomotsu-Ikusa",
    "Gdon", "Taotie", "Pabilsag", "Niddhoggr", "Taowu", "Jinn", "Mahakala", "Ongyo-Ki",
    # P5/P5R
    "Regent", "Queen's Necklace", "Stone of Scone", "Koh-i-Noor", "Choronzon",
    "Orlov", "Emperor's Amulet", "Hope Diamond", "Bugs", "Tsukiyomi",
    "Tsukiyomi Picaro", "Cait Sith", "Orichalcum", "Fafnir"
]

fixed = []
errors = []

for persona_name in recent_personas:
    img_path = images_dir / f"{persona_name}.png"
    
    if not img_path.exists():
        continue
    
    try:
        img = Image.open(img_path)
        
        # Convert to RGBA if not already
        if img.mode != 'RGBA':
            img = img.convert('RGBA')
        
        # Get image data
        data = img.getdata()
        
        # Create new image data with white pixels made transparent
        new_data = []
        for item in data:
            # If pixel is white or near-white (RGB > 240), make it transparent
            if item[0] > 240 and item[1] > 240 and item[2] > 240:
                new_data.append((255, 255, 255, 0))  # Transparent
            else:
                new_data.append(item)
        
        # Update image
        img.putdata(new_data)
        
        # Save back
        img.save(img_path, 'PNG', optimize=True)
        fixed.append(persona_name)
        print(f"✓ Fixed {persona_name}")
        
    except Exception as e:
        errors.append((persona_name, str(e)))
        print(f"✗ Error fixing {persona_name}: {e}")

print("\n" + "=" * 60)
print(f"Fixed: {len(fixed)} images")
if errors:
    print(f"Errors: {len(errors)}")
    for name, error in errors:
        print(f"  - {name}: {error}")
