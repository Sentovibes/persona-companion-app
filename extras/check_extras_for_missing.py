#!/usr/bin/env python3
"""
Check if missing P3R personas exist in extras/images
"""

from pathlib import Path

missing = [
    "Ananta", "Anat", "Chimera", "Cybele", "Ganga", "Gurulu", "Houou",
    "Izanagi", "Kaguya", "Kamu Susano-o", "Kushi Mitama", "Legion",
    "Magatsu-Izanagi", "Mercurius", "Messiah", "Mithras", "Nidhoggr",
    "Omoikane", "Orpheus", "Sandman", "Satanael", "Shiisaa", "Shiva",
    "Take-Mikazuchi", "Tam Lin", "Thanatos", "Titania", "Virtue",
    "Yakshini", "Yurlungur"
]

extras_dir = Path("images/personas")
found = []
not_found = []

for name in missing:
    safe_name = name.replace("/", "_").replace(":", "").replace("?", "")
    
    # Check all game folders
    found_in = []
    for game_folder in extras_dir.glob("*"):
        if game_folder.is_dir():
            image_path = game_folder / f"{safe_name}.png"
            if image_path.exists():
                found_in.append(game_folder.name)
    
    if found_in:
        found.append((name, found_in))
    else:
        not_found.append(name)

print("=" * 60)
print("FOUND IN EXTRAS (need to copy to shared)")
print("=" * 60)
for name, locations in found:
    print(f"✓ {name} - in {', '.join(locations)}")

print(f"\n{len(found)} found in extras!")

print("\n" + "=" * 60)
print("NOT FOUND (need to download)")
print("=" * 60)
for name in not_found:
    print(f"✗ {name}")

print(f"\n{len(not_found)} need to be downloaded")
