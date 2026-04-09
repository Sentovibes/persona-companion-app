import json
import os

# --- PATHS ---
DATA_DIR = r"c:\Users\omare\Music\Persona-Companion-App\persona-companion-app\app\src\main\assets\data\social-links"
P4G_FILE = os.path.join(DATA_DIR, "p4+p4g_social_links.json")
P5R_FILE = os.path.join(DATA_DIR, "p5+p5r_social_links.json")
P3R_FILE = os.path.join(DATA_DIR, "p3r_social_links.json")

# --- DATA MAPPINGS ---

P4G_EXTRAS = {
    "Fool": {"Ultimate": "Izanagi-no-Okami"},
    "Magician": {"Ultimate": "Mada", "Item": "Junes Receipt"},
    "Priestess": {"Ultimate": "Scathach", "Item": "Gatekeeper's Seal"},
    "Empress": {"Ultimate": "Isis", "Item": "Proof of Power"},
    "Emperor": {"Ultimate": "Odin", "Item": "Bike Key"},
    "Hierophant": {"Ultimate": "Kohryu", "Item": "Lighter"},
    "Lovers": {"Ultimate": "Ishtar", "Item": "Tape of Girl's Voice"},
    "Chariot": {"Ultimate": "Futsunushi", "Item": "Sato-Gaitame"},
    "Justice": {"Ultimate": "Sraosha", "Item": "House Key"},
    "Hermit": {"Ultimate": "Ongyo-Ki", "Item": "Sturdy Sheath"},
    "Fortune": {"Ultimate": "Norn", "Item": "Velvet Card"},
    "Strength": {"Ultimate": "Zaou-Gongen", "Item": "Sports Tape"},
    "Hanged Man": {"Ultimate": "Attis", "Item": "Class Ring"},
    "Death": {"Ultimate": "Mahakala", "Item": "Envelope"},
    "Temperance": {"Ultimate": "Vishnu", "Item": "Boutique Receipt"},
    "Devil": {"Ultimate": "Beelzebub", "Item": "Charred Screw"},
    "Tower": {"Ultimate": "Shiva", "Item": "Winning Ball"},
    "Star": {"Ultimate": "Helel", "Item": "Cup of Wishes"},
    "Moon": {"Ultimate": "Sandalphon", "Item": "Cool Coffee Cup"},
    "Sun": {"Ultimate": "Asura-Ou", "Item": "Hand-knit Rabbit"},
    "Jester": {"Ultimate": "Magatsu-Izanagi", "Item": "Flowering Button"},
    "Aeon": {"Ultimate": "Kaguya", "Item": "Comb"},
    "Judgement": {"Ultimate": "Lucifer"}
}

P5R_ITEMS = {
    "Magician": "Mona's Scarf",
    "Priestess": "Bushi Calculator",
    "Empress": "Dyed Handkerchief",
    "Emperor": "Desire and Hope",
    "Hierophant": "Coffee Recipe",
    "Lovers": "Fashion Magazine",
    "Chariot": "Ripped Badge",
    "Justice": "Duel Glove",
    "Hermit": "Promise List",
    "Fortune": "Fortune Tarot Card",
    "Strength": "Cell Key",
    "Hanged Man": "Model Gun",
    "Death": "Dog Tag",
    "Temperance": "Unlimited Service",
    "Devil": "Interview Notes",
    "Tower": "Gun Controller",
    "Star": "Koma Piece",
    "Moon": "Student ID",
    "Sun": "Fountain Pen",
    "Faith": "Ring",
    "Councillor": "Vouchers"
}

P3R_ITEMS = {
    "Magician": "Handmade Choker",
    "Priestess": "Lacquered Box",
    "Empress": "Myriad Eggs",
    "Emperor": "Cuirass",
    "Hierophant": "Book Cover",
    "Lovers": "Gourd",
    "Chariot": "Sports Tape",
    "Justice": "Case Key",
    "Hermit": "Scripture",
    "Fortune": "Calculator",
    "Strength": "Trainers",
    "Hanged Man": "Juju",
    "Temperance": "Red Fabric",
    "Devil": "Business Card",
    "Tower": "Prayer Beads",
    "Star": "Blue Watch",
    "Moon": "Gourmet License",
    "Sun": "Signed Postcard",
    "Aeon": "Prototype"
}

def fix_p4g():
    print(f"Repairing P4G: {P4G_FILE}")
    with open(P4G_FILE, 'r', encoding='utf-8') as f:
        data = json.load(f)

    for arcana, arcana_data in data.items():
        # Inject Ultimate Persona field at top level
        if arcana in P4G_EXTRAS:
            arcana_data["UltimatePersona"] = P4G_EXTRAS[arcana]["Ultimate"]
            
            # Find Rank 10
            rank10_keys = [k for k in arcana_data.keys() if "Rank 10" in k]
            if rank10_keys:
                r10_key = rank10_keys[0]
                r10_node = arcana_data[r10_key]
                
                # Check for "Rank Up Progression" and "Benefit"
                if "Rank Up Progression" in arcana_data:
                    # Some files use "Rank Up Progression", others are nested flatly
                    # P4G seems to use Rank Up Progression for Magician etc.
                    pass 

                # Standardize Rank 10 Node
                item_name = P4G_EXTRAS[arcana].get("Item")
                if item_name:
                    if "Benefit" not in r10_node: r10_node["Benefit"] = {}
                    r10_node["Benefit"]["Name"] = item_name
                    # Fix description to remove "Joker" if it was there and mention unlock
                    desc = f"Unlocks the fusion of {P4G_EXTRAS[arcana]['Ultimate']}, the ultimate Persona of the {arcana} Arcana."
                    r10_node["Benefit"]["Description"] = desc

            # Also check nested "Rank Up Progression" -> "Rank 10"
            if "Rank Up Progression" in arcana_data:
                prog = arcana_data["Rank Up Progression"]
                if "Rank 10" in prog:
                    r10_node = prog["Rank 10"]
                    item_name = P4G_EXTRAS[arcana].get("Item")
                    if item_name:
                        if "Benefit" not in r10_node: r10_node["Benefit"] = {}
                        r10_node["Benefit"]["Name"] = item_name
                        desc = f"Unlocks the fusion of {P4G_EXTRAS[arcana]['Ultimate']}, the ultimate Persona of the {arcana} Arcana."
                        r10_node["Benefit"]["Description"] = desc

    # Replace "Joker" globally in the whole file
    json_str = json.dumps(data, indent=2, ensure_ascii=False)
    json_str = json_str.replace("Joker's", "the protagonist's")
    json_str = json_str.replace("Joker ", "the protagonist ")
    json_str = json_str.replace("Joker", "protagonist")

    with open(P4G_FILE, 'w', encoding='utf-8') as f:
        f.write(json_str)

def fix_p5r():
    print(f"Repairing P5R: {P5R_FILE}")
    with open(P5R_FILE, 'r', encoding='utf-8') as f:
        data = json.load(f)

    for arcana, arcana_data in data.items():
        # Add Key Items to Rank 10
        item_name = P5R_ITEMS.get(arcana)
        if item_name:
            # Find Rank 10 node
            for key in arcana_data.copy():
                if "Rank 10" in key:
                    r10_node = arcana_data[key]
                    if "Benefit" not in r10_node: r10_node["Benefit"] = {}
                    # Prepend/Set Item Name
                    r10_node["Benefit"]["Name"] = f"{item_name} / {r10_node['Benefit'].get('Name', '')}".strip(" / ")
                    
            if "Rank Up Progression" in arcana_data:
                p = arcana_data["Rank Up Progression"]
                for key in p:
                    if "Rank 10" in key:
                        r10_node = p[key]
                        if "Benefit" not in r10_node: r10_node["Benefit"] = {}
                        r10_node["Benefit"]["Name"] = f"{item_name} / {r10_node['Benefit'].get('Name', '')}".strip(" / ")

        # Resolve Double Awakening issue
        if "ThirdAwakening" in arcana_data:
            # Check Rank MAX or Rank 11-style nodes for redundant benefits
            for key in list(arcana_data.keys()) + (list(arcana_data.get("Rank Up Progression", {}).keys()) if "Rank Up Progression" in arcana_data else []):
                node = None
                if key in arcana_data: node = arcana_data[key]
                elif "Rank Up Progression" in arcana_data and key in arcana_data["Rank Up Progression"]:
                    node = arcana_data["Rank Up Progression"][key]
                
                if node and isinstance(node, dict) and "Benefit" in node:
                    b_name = str(node["Benefit"].get("Name", ""))
                    if "Third Awakening" in b_name or "Second Awakening" in b_name:
                        # If top-level UltimatePersona/ThirdAwakening exists, remove this Benefit Name/Desc if it's identical
                        # Actually the user says "ThirdAwakening appears twice".
                        # Let's remove the "Third Awakening" BENEFIT text to allow the ThirdAwakening object to handle it.
                        if "Third Awakening" in b_name:
                            print(f"Removing redundant Third Awakening benefit from {arcana} -> {key}")
                            del node["Benefit"]

    with open(P5R_FILE, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)

def fix_p3r():
    print(f"Repairing P3R: {P3R_FILE}")
    with open(P3R_FILE, 'r', encoding='utf-8') as f:
        data = json.load(f)

    for arcana, arcana_data in data.items():
        item_name = P3R_ITEMS.get(arcana)
        if item_name:
            # Find Rank 10
            for key in list(arcana_data.keys()):
                if "Rank 10" in key:
                    r10_node = arcana_data[key]
                    if "Benefit" not in r10_node: r10_node["Benefit"] = {}
                    r10_node["Benefit"]["Name"] = item_name
                    r10_node["Benefit"]["Description"] = f"Unlocks the fusion of {arcana_data.get('UltimatePersona', 'the Ultimate Persona')}, the ultimate Persona of the {arcana} Arcana."

            if "Rank Up Progression" in arcana_data:
                p = arcana_data["Rank Up Progression"]
                for key in p:
                    if "Rank 10" in key:
                        r10_node = p[key]
                        if "Benefit" not in r10_node: r10_node["Benefit"] = {}
                        r10_node["Benefit"]["Name"] = item_name
                        r10_node["Benefit"]["Description"] = f"Unlocks the fusion of {arcana_data.get('UltimatePersona', 'the Ultimate Persona')}, the ultimate Persona of the {arcana} Arcana."

    with open(P3R_FILE, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)

if __name__ == "__main__":
    fix_p4g()
    fix_p5r()
    fix_p3r()
    print("Done!")
