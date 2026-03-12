import json
import os

# Load P4 personas
with open('app/src/main/assets/data/persona4/personas.json', 'r', encoding='utf-8') as f:
    p4_personas = json.load(f)

# Load P4G personas
with open('app/src/main/assets/data/persona4/golden_personas.json', 'r', encoding='utf-8') as f:
    p4g_personas = json.load(f)

# Get all persona image files
image_dir = 'app/src/main/assets/images/personas_shared'
image_files = set()
if os.path.exists(image_dir):
    for file in os.listdir(image_dir):
        if file.endswith('.png'):
            # Remove .png extension and convert to lowercase
            image_files.add(file[:-4].lower())

print("=== P4 Persona Image Check ===")
print(f"Total P4 personas: {len(p4_personas)}")
print(f"Total P4G personas: {len(p4g_personas)}")
print(f"Total persona images: {len(image_files)}")
print()

# Check P4 personas
p4_missing = []
for persona_name in p4_personas.keys():
    # Convert to expected filename format
    safe_name = persona_name.lower().replace(' ', '_').replace('-', '_').replace('/', '_').replace(':', '').replace('?', '').replace("'", '').replace('&', '')
    
    if safe_name not in image_files:
        p4_missing.append(persona_name)

# Check P4G personas
p4g_missing = []
for persona_name in p4g_personas.keys():
    # Convert to expected filename format
    safe_name = persona_name.lower().replace(' ', '_').replace('-', '_').replace('/', '_').replace(':', '').replace('?', '').replace("'", '').replace('&', '')
    
    if safe_name not in image_files:
        p4g_missing.append(persona_name)

print(f"P4 personas missing images: {len(p4_missing)}")
if p4_missing:
    print("Missing P4 personas:")
    for name in sorted(p4_missing)[:20]:  # Show first 20
        safe_name = name.lower().replace(' ', '_').replace('-', '_').replace('/', '_').replace(':', '').replace('?', '').replace("'", '').replace('&', '')
        print(f"  - {name} (looking for: {safe_name}.png)")
    if len(p4_missing) > 20:
        print(f"  ... and {len(p4_missing) - 20} more")

print()
print(f"P4G personas missing images: {len(p4g_missing)}")
if p4g_missing:
    print("Missing P4G personas:")
    for name in sorted(p4g_missing)[:20]:  # Show first 20
        safe_name = name.lower().replace(' ', '_').replace('-', '_').replace('/', '_').replace(':', '').replace('?', '').replace("'", '').replace('&', '')
        print(f"  - {name} (looking for: {safe_name}.png)")
    if len(p4g_missing) > 20:
        print(f"  ... and {len(p4g_missing) - 20} more")

# Check if any images exist but with different casing
print("\n=== Sample of available images ===")
for img in sorted(list(image_files))[:30]:
    print(f"  {img}.png")
