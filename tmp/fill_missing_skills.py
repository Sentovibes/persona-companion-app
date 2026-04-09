import shutil
import os

skills_dir = r"app\src\main\assets\data\skills"

# Map variants to base game skill files
copies = {
    "p3fes_skills.json": "p3_skills.json",  # copy from p3 (which doesn't exist, use p3r)
    "p3p_skills.json": "p3_skills.json",    # same
    "p4g_skills.json": "p4_skills.json",    # P4G uses P4 base skills
    "p5r_skills.json": "p5_skills.json",    # P5R uses P5 base skills
}

# First check if p3_skills.json exists, if not use p3r
if not os.path.exists(os.path.join(skills_dir, "p3_skills.json")):
    # Use p3r as the base for p3/p3fes/p3p
    copies["p3fes_skills.json"] = "p3r_skills.json"
    copies["p3p_skills.json"] = "p3r_skills.json"
    copies["p3_skills.json"] = "p3r_skills.json"

for dest_name, src_name in copies.items():
    src_path = os.path.join(skills_dir, src_name)
    dest_path = os.path.join(skills_dir, dest_name)
    if os.path.exists(src_path) and not os.path.exists(dest_path):
        shutil.copy2(src_path, dest_path)
        print(f"Copied {src_name} -> {dest_name}")
    elif os.path.exists(dest_path):
        print(f"Already exists: {dest_name}")
    else:
        print(f"Source missing: {src_name}")

# List final state
print("\nFinal skill files:")
for f in sorted(os.listdir(skills_dir)):
    size = os.path.getsize(os.path.join(skills_dir, f))
    print(f"  {f}: {size:,} bytes")
