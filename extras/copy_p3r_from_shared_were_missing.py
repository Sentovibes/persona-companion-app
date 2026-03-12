import shutil
from pathlib import Path

def safe_filename(name):
    """Convert name to safe filename format"""
    safe = name.lower().replace(" ", "_").replace("/", "_").replace(":", "").replace("?", "").replace("&", "").replace("-", "_").replace("'", "")
    safe = safe.replace("___", "_")
    return safe

# P3R enemies found in shared/were missing folder
p3r_mappings = {
    "Appropriating Noble.png": "appropriating_noble.png",
    "Arcanist Decapitator.png": "arcanist_decapitator.png",
    "Barbaric Beast Wheel.png": "barbaric_beast_wheel.png",
    "Black Hand.png": "black_hand.png",
    "Blazing Middle Sibling.png": "blazing_middle_sibling.png",
    "Bloody Maria.png": "bloody_maria.png",
    "Bold Checkmate.png": "bold_checkmate.png",
    "Chaos Panzer.png": "chaos_panzer.png",
    "Clairvoyant Relic.png": "clairvoyant_relic.png",
    "Comeback Castle.png": "comeback_castle.png",
    "Controlling Partner.png": "controlling_partner.png",
    "Cruel Greatsword.png": "cruel_greatsword.png",
    "Cultist of Death.png": "cultist_of_death.png",
    "Cultist of the Storm.png": "cultist_of_the_storm.png",
    "Dancing Beast Wheel.png": "dancing_beast_wheel.png",
    "Deadly Eldest Sibling.png": "deadly_eldest_sibling.png",
    "Demented Knight.png": "demented_knight.png",
    "Dependent Partner.png": "dependent_partner.png",
    "Despairing Tiara.png": "despairing_tiara.png",
    "Deviant Convict.png": "deviant_convict.png",
    "Disturbing Dice.png": "disturbing_dice.png",
    "Dutiful Checkmate.png": "dutiful_checkmate.png",
    "Enslaved Cupid.png": "enslaved_cupid.png",
    "Ethereal Hand.png": "ethereal_hand.png",
    "Executioner's Crown.png": "executioners_crown.png",
    "Executive Greatsword.png": "executive_greatsword.png",
    "Feral Beast.png": "feral_beast.png",
    "Final Checkmate.png": "final_checkmate.png",
    "Five Fingers of Blight.png": "five_fingers_of_blight.png",
    "Fleetfooted Cavalry.png": "fleetfooted_cavalry.png",
    "Judgement Sword.png": "judgment_sword.png",  # Note: Judgement vs Judgment spelling
}

source_folder = Path("images/shared/enemies/were missing")
target_folder = Path("downloaded_enemies/p3r")
target_folder.mkdir(parents=True, exist_ok=True)

print("Copying P3R enemies from shared/were missing folder...")
print("="*70)

copied = 0
skipped = 0

for source_name, target_name in p3r_mappings.items():
    source_path = source_folder / source_name
    target_path = target_folder / target_name
    
    if not source_path.exists():
        print(f"✗ Source not found: {source_name}")
        continue
    
    if target_path.exists():
        print(f"⊘ Skipped {target_name} (already exists)")
        skipped += 1
        continue
    
    shutil.copy2(source_path, target_path)
    print(f"✓ Copied {source_name} → {target_name}")
    copied += 1

print("\n" + "="*70)
print(f"Copied: {copied}")
print(f"Skipped: {skipped}")
print("="*70)
