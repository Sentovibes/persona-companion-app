import os
import shutil

# Start from a higher level to be safe
root_search = r"C:\Users\omare\Music\Persona-Companion-App\EXTRA DB"
dest = r"app\src\main\assets\data\skills"

if not os.path.exists(dest):
    os.makedirs(dest)

# Map of how we want to name them locally vs how they might be found
games = ["p3p", "p3r", "p4g", "p5r"] # These are the main ones with skill data usually

print(f"Searching in {root_search}...")

for root, dirs, files in os.walk(root_search):
    if "skill-data.json" in files:
        # Check which game this belongs to by looking at the path
        for game in games:
            if f"\\{game}\\" in root.lower() or f"/{game}/" in root.lower():
                src_path = os.path.join(root, "skill-data.json")
                dest_path = os.path.join(dest, f"{game}_skills.json")
                try:
                    shutil.copy(src_path, dest_path)
                    print(f"Copied {game} from {root}")
                except Exception as e:
                    print(f"Error copying {game}: {e}")
