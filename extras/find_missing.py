"""
Find which personas are missing and why
"""
import json
import os

def get_persona_names(filepath):
    """Get list of persona names from JSON file"""
    if not os.path.exists(filepath):
        return []
    
    with open(filepath, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    if isinstance(data, dict):
        return list(data.keys())
    return data

def get_downloaded_names(directory):
    """Get list of downloaded persona names"""
    if not os.path.exists(directory):
        return set()
    
    downloaded = set()
    for filename in os.listdir(directory):
        if filename.endswith(('.png', '.jpg', '.jpeg', '.webp')):
            # Remove extension
            name = os.path.splitext(filename)[0]
            downloaded.add(name)
    return downloaded

# Check P4G
print("=" * 70)
print("P4G MISSING PERSONAS")
print("=" * 70)

p4g_all = set(get_persona_names('../app/src/main/assets/data/persona4/golden_personas.json'))
p4g_downloaded = get_downloaded_names('images/personas/p4g')
p4g_missing = p4g_all - p4g_downloaded

print(f"Total: {len(p4g_all)}")
print(f"Downloaded: {len(p4g_downloaded)}")
print(f"Missing: {len(p4g_missing)}\n")

if p4g_missing:
    print("Missing personas:")
    for name in sorted(p4g_missing)[:20]:  # Show first 20
        print(f"  - {name}")
    if len(p4g_missing) > 20:
        print(f"  ... and {len(p4g_missing) - 20} more")

# Check P5R
print("\n" + "=" * 70)
print("P5R MISSING PERSONAS")
print("=" * 70)

p5r_all = set(get_persona_names('../app/src/main/assets/data/persona5/royal_personas.json'))
p5r_downloaded = get_downloaded_names('images/personas/p5r')
p5r_missing = p5r_all - p5r_downloaded

print(f"Total: {len(p5r_all)}")
print(f"Downloaded: {len(p5r_downloaded)}")
print(f"Missing: {len(p5r_missing)}\n")

if p5r_missing:
    print("Missing personas:")
    for name in sorted(p5r_missing)[:20]:
        print(f"  - {name}")
    if len(p5r_missing) > 20:
        print(f"  ... and {len(p5r_missing) - 20} more")

# Save full lists
with open('missing_p4g_personas.txt', 'w', encoding='utf-8') as f:
    for name in sorted(p4g_missing):
        f.write(f"{name}\n")

with open('missing_p5r_personas.txt', 'w', encoding='utf-8') as f:
    for name in sorted(p5r_missing):
        f.write(f"{name}\n")

print(f"\n\nFull lists saved to:")
print(f"  - missing_p4g_personas.txt ({len(p4g_missing)} personas)")
print(f"  - missing_p5r_personas.txt ({len(p5r_missing)} personas)")
