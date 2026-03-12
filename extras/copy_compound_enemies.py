import os
import shutil

# Compound enemies that should use individual images
compound_mappings = {
    "chariot_&_justice": "chariot",  # Use Chariot image
    "emperor_&_empress": "emperor",  # Use Emperor image
    "fortune_&_strength": "fortune"  # Use Fortune image
}

games = ["p3fes", "p3p"]

for game in games:
    game_folder = f"downloaded_enemies/{game}"
    
    if not os.path.exists(game_folder):
        print(f"Folder not found: {game_folder}")
        continue
    
    for compound_name, individual_name in compound_mappings.items():
        # Find the individual image
        individual_files = [f for f in os.listdir(game_folder) if f.startswith(individual_name + ".")]
        
        if individual_files:
            source_file = individual_files[0]
            source_path = os.path.join(game_folder, source_file)
            
            # Get the extension
            ext = os.path.splitext(source_file)[1]
            
            # Create compound filename
            compound_file = compound_name + ext
            target_path = os.path.join(game_folder, compound_file)
            
            # Copy the file
            shutil.copy2(source_path, target_path)
            print(f"{game}: Copied {source_file} -> {compound_file}")
        else:
            print(f"{game}: WARNING - Individual image not found for {individual_name}")

print("\nCompound enemy images created!")
