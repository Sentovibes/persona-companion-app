import os
import shutil

src = r"C:\Users\omare\Music\Persona-Companion-App\EXTRA DB\megaten-fusion-tool-master\megaten-fusion-tool-master\src\app"
dest = r"app\src\main\assets\data\skills"

if not os.path.exists(dest):
    os.makedirs(dest)

games = ["p3fes", "p3p", "p3r", "p4g", "p5", "p5r"]

for game in games:
    path = os.path.join(src, game, "data", "skill-data.json")
    if os.path.exists(path):
        target = os.path.join(dest, f"{game}_skills.json")
        try:
            shutil.copy(path, target)
            print(f"Copied {game}")
        except Exception as e:
            print(f"Failed to copy {game}: {e}")
    else:
        print(f"Missing {game}: {path}")
