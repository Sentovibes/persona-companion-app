import json
import os
import re

def categorize_item(item):
    name = item.get("name", "").lower()
    desc = item.get("description", "").lower()
    effect = item.get("effect", "").lower()
    attack = item.get("attack")
    accuracy = item.get("accuracy")
    
    # Priority 1: Weapons (have attack stats)
    if attack and str(attack).strip() and str(attack).lower() != "null" and str(attack).lower() != "none" and str(attack) != "0":
        return "Weapon"
    
    # Special case for some weapons that might have attack "0" or "Varies"
    weapon_keywords = ["sword", "katana", "fist", "knuckles", "gloves", "rifle", "cannon", "shotgun", "launcher", "bow", "axe", "hammer", "mace", "staff", "blade", "dagger"]
    if any(re.search(r'\b' + re.escape(k) + r'\b', name) for k in weapon_keywords):
        if "card" not in name:
            return "Weapon"

    # Priority 2: Armor
    armor_keywords = ["mail", "vest", "uniform", "robe", "suit", "armor", "cloth", "guard", "shirt", "boots", "shoes", "sandals", "leggings"]
    if any(re.search(r'\b' + re.escape(k) + r'\b', name) for k in armor_keywords):
        if "card" not in name:
            return "Armor"
    
    # Priority 3: Skill Cards
    if "skill card" in name or item.get("category") == "Skill Card":
        # Double check it's not a weapon mislabeled
        if attack and str(attack).strip() and str(attack) != "null":
            return "Weapon"
        return "Skill Card"

    # Priority 4: Consumables (Restoration/Battle items)
    if any(k in desc for k in ["restores", "cures", "dispels", "neutralizes", "revives", "casts", "deals", "damage to"]):
        return "Consumable"
    if any(k in effect for k in ["restores", "cures", "dispels", "neutralizes", "revives", "casts", "deals", "damage to"]):
        return "Consumable"
    if any(k in name for k in ["sutra", "gem", "medicine", "soma", "water", "bead", "soul", "egg", "juice", "soda", "bread", "cake", "cookie"]):
        return "Consumable"

    # Priority 5: Accessories
    if any(k in name for k in ["ring", "badge", "necklace", "charm", "earring", "bracelet", "anklet", "talisman", "orb", "band", "belt"]):
        return "Accessory"
    if "accessory" in desc or "accessory" in effect:
        return "Accessory"

    return "Consumable" # Default fallback for P3 items which are mostly consumables if not categorized

def fix_file(path):
    if not os.path.exists(path):
        print(f"File not found: {path}")
        return
    
    print(f"Fixing {path}...")
    with open(path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    items = data.get("items", [])
    for item in items:
        item["category"] = categorize_item(item)
    
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2)
    print(f"Fixed {len(items)} items in {path}")

base_path = r"app/src/main/assets/data/items"
fix_file(os.path.join(base_path, "p3_items.json"))
fix_file(os.path.join(base_path, "p3p_items.json"))
fix_file(os.path.join(base_path, "p3fes_items.json"))
fix_file(os.path.join(base_path, "p3r_items.json"))
