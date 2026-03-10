#!/usr/bin/env python3
"""
Prepare HD image packs for external hosting
Uses original high-quality images from were-missing folder
"""

import zipfile
from pathlib import Path
import shutil

# Source: were-missing folder has the original HD images
source_dir = Path("images/personas/were-missing")
output_dir = Path("image-packs")
output_dir.mkdir(exist_ok=True)

# Get all persona images
persona_images = list(source_dir.glob("*.png"))

print(f"Found {len(persona_images)} HD persona images")

# Create persona image pack
persona_pack = output_dir / "persona-images-hd-v1.0.zip"
with zipfile.ZipFile(persona_pack, 'w', zipfile.ZIP_DEFLATED) as zipf:
    for img in persona_images:
        # Add to zip with just the filename (no path)
        zipf.write(img, f"personas/{img.name}")
        print(f"Added {img.name}")

pack_size = persona_pack.stat().st_size / (1024 * 1024)
print(f"\n✓ Created {persona_pack.name} ({pack_size:.2f} MB)")

# Create manifest file
manifest = output_dir / "manifest.json"
import json

manifest_data = {
    "version": "1.0",
    "packs": [
        {
            "id": "personas-hd",
            "name": "Persona Images (HD)",
            "description": "High-quality persona portraits",
            "version": "1.0",
            "size_mb": round(pack_size, 2),
            "count": len(persona_images),
            "url": "https://github.com/Sentovibes/persona-companion-app/releases/download/images-v1.0/persona-images-hd-v1.0.zip",
            "filename": "persona-images-hd-v1.0.zip"
        }
    ]
}

with open(manifest, 'w') as f:
    json.dump(manifest_data, f, indent=2)

print(f"✓ Created manifest.json")
print(f"\nUpload {persona_pack.name} to GitHub Releases")
print(f"Tag: images-v1.0")
