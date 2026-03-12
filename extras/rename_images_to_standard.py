import os
from pathlib import Path
import shutil

print("=" * 70)
print("RENAMING IMAGES TO STANDARD FORMAT")
print("=" * 70)

shared_folder = Path("app/src/main/assets/images/enemies_shared")

# Mapping of current names to standard names
renames = {
    # Compound boss names - keep only the split versions
    "emperor__empress.png": None,  # Delete, use emperor.png
    "emperor___empress.png": None,  # Delete, use emperor.png
    "chariot__justice.png": None,  # Delete, use chariot.png
    "chariot___justice.png": None,  # Delete, use chariot.png
    "fortune__strength.png": None,  # Delete, use fortune.png
    "fortune___strength.png": None,  # Delete, use fortune.png
    
    # Boss names with full names - rename to short versions (P3R style)
    "chidori_yoshino.png": "chidori.png",
    "jin_shirato.png": "jin.png",
    "takaya_sakaki.png": "takaya.png",
    
    # Hyphenated names - convert to underscores
    "ill-fated_maya.png": "ill_fated_maya.png",
    "tank-form_shadow.png": "tank_form_shadow.png",
    "kunino-sagiri.png": "kunino_sagiri.png",
    "ameno-sagiri.png": "ameno_sagiri.png",
    "kusumi-no-okami.png": "kusumi_no_okami.png",
    "jack-o-lantern.png": "jack_o_lantern.png",
}

# Find all files that need renaming
files_to_rename = []
files_to_delete = []

for old_name, new_name in renames.items():
    old_path = shared_folder / old_name
    if old_path.exists():
        if new_name is None:
            files_to_delete.append(old_name)
        else:
            new_path = shared_folder / new_name
            if not new_path.exists():
                files_to_rename.append((old_name, new_name))
            else:
                print(f"⚠ {new_name} already exists, will delete {old_name}")
                files_to_delete.append(old_name)

print(f"\nFound {len(files_to_rename)} files to rename")
print(f"Found {len(files_to_delete)} files to delete")

if files_to_rename:
    print("\nFiles to rename:")
    for old, new in files_to_rename:
        print(f"  {old} → {new}")

if files_to_delete:
    print("\nFiles to delete (duplicates/compound names):")
    for name in files_to_delete:
        print(f"  {name}")

print("\nProceeding with renames...")

# Perform renames
renamed_count = 0
for old_name, new_name in files_to_rename:
    old_path = shared_folder / old_name
    new_path = shared_folder / new_name
    try:
        shutil.move(str(old_path), str(new_path))
        print(f"✓ Renamed: {old_name} → {new_name}")
        renamed_count += 1
    except Exception as e:
        print(f"✗ Failed to rename {old_name}: {e}")

# Delete duplicates
deleted_count = 0
for name in files_to_delete:
    path = shared_folder / name
    try:
        path.unlink()
        print(f"✓ Deleted: {name}")
        deleted_count += 1
    except Exception as e:
        print(f"✗ Failed to delete {name}: {e}")

print("\n" + "=" * 70)
print(f"COMPLETE: Renamed {renamed_count} files, deleted {deleted_count} files")
print("=" * 70)
