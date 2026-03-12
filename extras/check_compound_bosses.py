import json
from pathlib import Path

print("Checking compound boss image handling...")
print("=" * 70)

# Compound bosses that should use first part's image
compound_bosses = {
    "Emperor & Empress": "emperor",
    "Chariot & Justice": "chariot", 
    "Fortune & Strength": "fortune",
    "Hanged Man": "hanged_man"  # Check if this exists
}

# Check P3R downloaded images
p3r_folder = Path("extras/downloaded_enemies/p3r")
downloaded = {img.stem for img in p3r_folder.glob("*.png")}

print("\nCompound Boss Image Mapping:")
for boss_name, expected_image in compound_bosses.items():
    # Convert boss name using ImageUtils logic
    safe_name = boss_name.lower().replace(" ", "_").replace("&", "").replace("___", "_").replace("__", "_")
    
    exists = expected_image in downloaded
    status = "✓" if exists else "✗"
    
    print(f"{status} {boss_name}")
    print(f"  Safe name: {safe_name}.png")
    print(f"  Expected image: {expected_image}.png")
    print(f"  Image exists: {exists}")
    print()

# Check the other missing bosses
print("\nOther Missing Bosses:")
missing_bosses = [
    ("Chidori Yoshino", "chidori"),
    ("Jin Shirato", "jin"),
    ("Takaya Sakaki", "takaya"),
    ("Erebus", "erebus"),
    ("Craven Venoms", "craven_venoms")
]

for boss_name, expected_image in missing_bosses:
    safe_name = boss_name.lower().replace(" ", "_")
    exists = expected_image in downloaded
    status = "✓" if exists else "✗"
    
    print(f"{status} {boss_name}")
    print(f"  Safe name: {safe_name}.png")
    print(f"  Expected image: {expected_image}.png")
    print(f"  Image exists: {exists}")
    print()

print("=" * 70)
print("\nAll downloaded P3R images:")
for img in sorted(downloaded):
    if any(keyword in img for keyword in ["emperor", "empress", "chariot", "justice", "fortune", "strength", "chidori", "jin", "takaya", "erebus", "craven"]):
        print(f"  - {img}.png")
