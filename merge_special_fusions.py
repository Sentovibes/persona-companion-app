import json
import os

# Mapping of special files to their corresponding offline data files
special_to_offline = {
    "extras/persona3fes+portable-special.json": ["extras/p3fes_persona_data.json", "extras/p3p_persona_data.json"],
    "extras/persona3reload-special.json": ["extras/p3r_persona_data.json"],
    "extras/persona4+golden-special.json": ["extras/p4_persona_data.json", "extras/p4g_persona_data.json"],
    "extras/persona5-special.json": ["extras/p5_persona_data.json"],
    "extras/persona5royal-special.json": ["extras/p5r_persona_data.json"]
}

def merge_special_fusions():
    for special_file, offline_files in special_to_offline.items():
        if not os.path.exists(special_file):
            print(f"Warning: {special_file} not found, skipping...")
            continue
        
        # Read special fusion data
        with open(special_file, 'r', encoding='utf-8') as f:
            special_data = json.load(f)
        
        print(f"\nProcessing {special_file}...")
        print(f"Found {len(special_data)} personas with special fusions")
        
        # Update each corresponding offline data file
        for offline_file in offline_files:
            if not os.path.exists(offline_file):
                print(f"  Warning: {offline_file} not found, skipping...")
                continue
            
            # Read offline data
            with open(offline_file, 'r', encoding='utf-8') as f:
                offline_data = json.load(f)
            
            updated_count = 0
            
            # Merge special recipes
            for persona_name, special_recipes in special_data.items():
                if persona_name in offline_data:
                    # Overwrite the recipes array
                    offline_data[persona_name]["recipes"] = special_recipes
                    updated_count += 1
            
            # Save updated offline data
            with open(offline_file, 'w', encoding='utf-8') as f:
                json.dump(offline_data, f, indent=2, ensure_ascii=False)
            
            print(f"  Updated {offline_file}: {updated_count} personas modified")

if __name__ == "__main__":
    merge_special_fusions()
    print("\nMerge complete!")
