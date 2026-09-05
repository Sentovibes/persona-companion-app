import subprocess
import time
import os
import sys
from PIL import Image

ADB = r"F:\AndroidDev\sdk\platform-tools\adb.exe"
OUT_DIR = os.path.abspath("store_screenshots")
os.makedirs(OUT_DIR, exist_ok=True)

def adb_cmd(args):
    return subprocess.run([ADB] + args, check=True, capture_output=True)

def tap(x, y):
    adb_cmd(["shell", "input", "tap", str(x), str(y)])
    time.sleep(1.2)

def back():
    adb_cmd(["shell", "input", "keyevent", "4"])
    time.sleep(0.8)

def capture_screen(filename):
    remote = "/sdcard/screen_tmp.png"
    local = os.path.join(OUT_DIR, filename)
    adb_cmd(["shell", "screencap", "-p", remote])
    adb_cmd(["pull", remote, local])
    # Open and re-save via Pillow to ensure 100% compliant 24-bit PNG header
    with Image.open(local) as im:
        rgb_im = im.convert("RGB")
        rgb_im.save(local, "PNG", optimize=True)
    
    # Verify file
    with Image.open(local) as im:
        im.verify()
    print(f"[OK] {filename} -> Verified PNG (1080x2400)")

def main():
    print("Starting screenshot capture sequence...")
    # 1. Reset to Home
    adb_cmd(["shell", "monkey", "-p", "com.persona.companion", "-c", "android.intent.category.LAUNCHER", "1"])
    time.sleep(2.0)
    capture_screen("01_home_screen.png")

    # 2. P5R Hub
    tap(244, 1990)
    capture_screen("02_p5r_hub.png")

    # 3. Persona Compendium
    tap(500, 500)
    capture_screen("03_persona_compendium.png")

    # 4. Persona Detail
    tap(500, 600)
    capture_screen("04_persona_detail.png")
    back()
    back()

    # 5. Fusion Calculator
    tap(500, 680)
    capture_screen("05_fusion_calculator.png")
    back()

    # 6. Confidants Guide
    tap(500, 1050)
    capture_screen("06_confidants_guide.png")
    back()

    # 7. Classroom Answers
    tap(500, 1230)
    capture_screen("07_classroom_answers.png")
    back()
    back()

    # 8. P4G Hub
    tap(244, 1620)
    capture_screen("08_p4g_hub.png")
    back()

    # Clean up device temp file
    adb_cmd(["shell", "rm", "-f", "/sdcard/screen_tmp.png"])
    print("\nAll screenshots captured and verified successfully!")

if __name__ == "__main__":
    main()
