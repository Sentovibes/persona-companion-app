import json
import os

# Mapping of special files to their corresponding offline data files
special_to_offline = {
    "persona3fes+portable-special.json": ["p3fes_offline_data.json", "p3p_offline_data.json"],
    "persona3reload-special.json": ["p3r_offline_data.json", "p3raeg_offline_data.json"],
    "persona4+golden-special.json": ["p4_offline_data.json", "p4g_offline_data.json"],
    "persona5-special.json": ["p5_offline_data.json"],
    "persona5royal-special.json": ["p5r_offline_data.json"]
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
