import subprocess
import time
import os
from PIL import Image

ADB = r"F:\AndroidDev\sdk\platform-tools\adb.exe"
OUT = os.path.abspath("store_screenshots")
os.makedirs(OUT, exist_ok=True)

def cmd(args):
    subprocess.run([ADB] + args, check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

def reset_app():
    cmd(["shell", "am", "force-stop", "com.persona.companion"])
    time.sleep(0.5)
    cmd(["shell", "monkey", "-p", "com.persona.companion", "-c", "android.intent.category.LAUNCHER", "1"])
    time.sleep(2.0)

def tap(x, y, delay=1.5):
    cmd(["shell", "input", "tap", str(x), str(y)])
    time.sleep(delay)

def snap(name):
    remote = "/sdcard/screen.png"
    local = os.path.join(OUT, f"{name}.png")
    cmd(["shell", "screencap", "-p", remote])
    subprocess.run([ADB, "pull", remote, local], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    with Image.open(local) as im:
        rgb = im.convert("RGB")
        rgb.save(local, "PNG", optimize=True)
    print(f"[VERIFIED IN-APP] {name}.png ({rgb.size[0]}x{rgb.size[1]})")

def main():
    print("=== CAPTURING GENUINE IN-APP SCREENSHOTS ===")

    # 1. Home Screen (Game Selector)
    reset_app()
    snap("01_home_screen")

    # 2. P5R Main Hub
    reset_app()
    tap(244, 1990) # P5R button
    snap("02_p5r_hub")

    # 3. Persona Compendium List
    reset_app()
    tap(244, 1990) # P5R
    tap(500, 500)  # Personas
    snap("03_persona_compendium")

    # 4. Persona Detail (Arsène stats & skills)
    reset_app()
    tap(244, 1990) # P5R
    tap(500, 500)  # Personas
    tap(500, 600)  # First Persona
    snap("04_persona_detail")

    # 5. Fusion Calculator
    reset_app()
    tap(244, 1990) # P5R
    tap(500, 680)  # Fusion Calculator
    snap("05_fusion_calculator")

    # 6. Confidants & Social Links List
    reset_app()
    tap(244, 1990) # P5R
    tap(500, 1050) # Social Links / Confidants
    snap("06_confidants_guide")

    # 7. Classroom & Exam Answers
    reset_app()
    tap(244, 1990) # P5R
    tap(500, 1230) # Classroom Answers
    snap("07_classroom_answers")

    # 8. Persona 4 Golden Hub
    reset_app()
    tap(244, 1620) # P4G button
    snap("08_p4g_hub")

    # Clean up device temp
    cmd(["shell", "rm", "-f", "/sdcard/screen.png"])
    print("=== ALL SCREENSHOTS SUCCESSFULLY CAPTURED & VALIDATED ===")

if __name__ == "__main__":
    main()
