import json
import os

# Map special files to their corresponding persona data files
special_to_data = {
    "extras/persona3fes+portable-special.json": ["extras/p3fes_persona_data.json", "extras/p3p_persona_data.json"],
    "extras/persona3reload-special.json": ["extras/p3r_persona_data.json"],
    "extras/persona4+golden-special.json": ["extras/p4_persona_data.json", "extras/p4g_persona_data.json"],
    "extras/persona5-special.json": ["extras/p5_persona_data.json"],
    "extras/persona5royal-special.json": ["extras/p5r_persona_data.json"]
}

def remove_special_recipes():
    for special_file, data_files in special_to_data.items():
        if not os.path.exists(special_file):
            print(f"Warning: {special_file} not found, skipping...")
            continue
        
        # Read special fusion data to get list of special personas
        with open(special_file, 'r', encoding='utf-8') as f:
            special_data = json.load(f)
        
        special_personas = list(special_data.keys())
        print(f"\nProcessing {special_file}...")
        print(f"Found {len(special_personas)} special fusion personas")
        
        # Remove recipes from each corresponding data file
        for data_file in data_files:
            if not os.path.exists(data_file):
                print(f"  Warning: {data_file} not found, skipping...")
                continue
            
            # Read persona data
            with open(data_file, 'r', encoding='utf-8') as f:
                persona_data = json.load(f)
            
            removed_count = 0
            
            # Remove recipes from special fusion personas
            for persona_name in special_personas:
                if persona_name in persona_data:
                    if "recipes" in persona_data[persona_name]:
                        del persona_data[persona_name]["recipes"]
                        removed_count += 1
            
            # Save updated persona data
            with open(data_file, 'w', encoding='utf-8') as f:
                json.dump(persona_data, f, indent=2, ensure_ascii=False)
            
            print(f"  Updated {data_file}: removed recipes from {removed_count} personas")

if __name__ == "__main__":
    remove_special_recipes()
    print("\nRecipe removal complete!")
