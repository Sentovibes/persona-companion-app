import os
import shutil

base = r"C:\Users\omare\Music\Persona-Companion-App\EXTRA DB\megaten-fusion-tool-master\src\app"
dest = r"C:\Users\omare\Music\Persona-Companion-App\persona-companion-app\app\src\main\assets\data\skills"

# Map of game folder names in the fusion tool to our app game IDs
game_map = {
    "p3": "p3fes",
    "p3p": "p3p", 
    "p3r": "p3r",
    "p4": "p4",
    "p4g": "p4g",
    "p5": "p5",
    "p5r": "p5r",
}

found = []
for root, dirs, files in os.walk(base):
    for f in files:
        if f == "skill-data.json":
            full_path = os.path.join(root, f)
            # Extract the game folder name from the path
            # Path like: .../src/app/p3r/data/skill-data.json
            parts = full_path.replace("\\", "/").split("/")
            # Find the game folder (the one right after 'app')
            try:
                app_idx = parts.index("app")
                game_folder = parts[app_idx + 1]
            except:
                continue
            
            if game_folder in game_map:
                dest_name = f"{game_map[game_folder]}_skills.json"
                dest_path = os.path.join(dest, dest_name)
                print(f"Copying {full_path} -> {dest_path}")
                shutil.copy2(full_path, dest_path)
                found.append(game_folder)

print(f"\nCopied {len(found)} skill files: {found}")
print(f"\nMissing games: {set(game_map.keys()) - set(found)}")
