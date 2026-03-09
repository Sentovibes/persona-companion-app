#!/usr/bin/env python3
"""
Copy the found personas from extras to app assets
"""

from pathlib import Path
import shutil

to_copy = [
    ("Ariadne.png", "p5"),
    ("Asterius.png", "p5"),
    ("Athena.png", "p5r"),
    ("Izanagi-no-Okami.png", "p4"),
    ("Kushinada.png", "p5"),
    ("Orpheus F.png", "p5r"),
]

source_base = Path("images/personas")
dest_dir = Path("../app/src/main/assets/images/personas")

for filename, folder in to_copy:
    source = source_base / folder / filename
    dest = dest_dir / filename
    
    if source.exists():
        shutil.copy2(source, dest)
        print(f"✓ Copied {filename} from {folder}/")
    else:
        print(f"✗ NOT FOUND: {source}")

print(f"\nCopied {len(to_copy)} personas")
