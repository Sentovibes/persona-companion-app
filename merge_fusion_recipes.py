import json
import os

# Map persona_data files to their corresponding offline_data files
data_mapping = {
    "p3fes_persona_data.json": "p3fes_offline_data.json",
    "p3p_persona_data.json": "p3p_offline_data.json",
    "p3r_persona_data.json": "p3r_offline_data.json",
    "p4_persona_data.json": "p4_offline_data.json",
    "p4g_persona_data.json": "p4g_offline_data.json",
    "p5_persona_data.json": "p5_offline_data.json",
    "p5r_persona_data.json": "p5r_offline_data.json"
}

def merge_recipes():
    for persona_file, offline_file in data_mapping.items():
        persona_path = os.path.join("extras", persona_file)
        offline_path = os.path.join("extras", offline_file)
        
        if not os.path.exists(persona_path) or not os.path.exists(offline_path):
            print(f"Skipping {persona_file} - files not found")
            continue
        
        # Load both files
        with open(persona_path, 'r', encoding='utf-8') as f:
            persona_data = json.load(f)
        
        with open(offline_path, 'r', encoding='utf-8') as f:
            offline_data = json.load(f)
        
        # Copy recipes from offline_data to persona_data
        for name in persona_data.keys():
            if name in offline_data and "recipes" in offline_data[name]:
                persona_data[name]["recipes"] = offline_data[name]["recipes"]
        
        # Save the merged data
        with open(persona_path, 'w', encoding='utf-8') as f:
            json.dump(persona_data, f, indent=2, ensure_ascii=False)
        
        print(f"Merged recipes into {persona_file}")

if __name__ == "__main__":
    merge_recipes()
    print("\nRecipe merge complete!")
