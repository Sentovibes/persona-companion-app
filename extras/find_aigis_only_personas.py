import json

# Load base P3R personas
with open('app/src/main/assets/data/persona3/reload_personas.json', 'r', encoding='utf-8') as f:
    base_personas = set(json.load(f).keys())

# Load Episode Aigis personas
with open('app/src/main/assets/data/personas/p3r_episode_aigis/personas.json', 'r', encoding='utf-8') as f:
    aigis_personas = set(json.load(f).keys())

# Find duplicates and Aigis-only
duplicates = base_personas & aigis_personas
aigis_only = aigis_personas - base_personas

print(f"=== Episode Aigis Persona Analysis ===\n")
print(f"Base P3R personas: {len(base_personas)}")
print(f"Episode Aigis personas: {len(aigis_personas)}")
print(f"Duplicates: {len(duplicates)}")
print(f"Aigis-only personas: {len(aigis_only)}")
print()

if aigis_only:
    print("Aigis-exclusive personas:")
    for persona in sorted(aigis_only):
        print(f"  - {persona}")
else:
    print("No Aigis-exclusive personas found - all are duplicates!")

print()
print(f"Duplicate personas (appearing in both files): {len(duplicates)}")
if len(duplicates) <= 20:
    for persona in sorted(duplicates):
        print(f"  - {persona}")
else:
    print(f"  (Too many to list - {len(duplicates)} duplicates)")
