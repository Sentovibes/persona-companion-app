import shutil
from pathlib import Path

# Manual mappings for the remaining enemies
mappings = {
    # P4G
    ("299px-P4G_Cursed_Woman's_Pot_Model.png", "downloaded_enemies/p4g/cursed_womans_pot.png"),
    ("Heaven's Giant.png", "downloaded_enemies/p4g/heavens_giant.png"),
    # P5
    ("300px-P5_Emperor's_Amulet_Model.png", "downloaded_enemies/p5/emperors_amulet.png"),
    ("Queen's Necklace.png", "downloaded_enemies/p5/queens_necklace.png"),
    # P5R (same as P5)
    ("300px-P5_Emperor's_Amulet_Model.png", "downloaded_enemies/p5r/emperors_amulet.png"),
    ("Queen's Necklace.png", "downloaded_enemies/p5r/queens_necklace.png"),
}

source_folder = Path("images/shared/enemies/were missing")

print("Copying remaining P4G, P5, P5R enemies...")
print("="*70)

copied = 0
skipped = 0
not_found = 0

for source_name, target_path_str in mappings:
    source_path = source_folder / source_name
    target_path = Path(target_path_str)
    
    if not source_path.exists():
        print(f"✗ Source not found: {source_name}")
        not_found += 1
        continue
    
    if target_path.exists():
        print(f"⊘ Skipped {target_path.name} (already exists)")
        skipped += 1
        continue
    
    target_path.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(source_path, target_path)
    print(f"✓ Copied {source_name} → {target_path.name}")
    copied += 1

print("\n" + "="*70)
print(f"Copied: {copied}")
print(f"Skipped: {skipped}")
print(f"Not found: {not_found}")
print("="*70)
