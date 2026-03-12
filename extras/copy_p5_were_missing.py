import os
import shutil

# Source folder
source_folder = "images/shared/enemies/were missing"

# P5/P5R missing enemies with their file mappings
p5_missing = {
    "Ame-no-Uzume.png": "ame_no_uzume.png",
    "Fuu-Ki.png": "fuu_ki.png",
    "Girimehkala.png": "girimehkala.png",
    "Ippon-Datara.png": "ippon_datara.png",
    "Jack-o'-Lantern.png": "jack_o_lantern.png",
    "Justine & Caroline.png": "justine__caroline.png",
    "Kikuri-Hime.png": "kikuri_hime.png",
    "Kin-Ki.png": "kin_ki.png",
    "Koh-i-Noor.png": "koh_i_noor.png",
    "Koropokguru.png": "koropokguru.png",
    "Kushinada.png": "kushinada.png",
    "Orichalcum.png": "orichalcum.png",
    "Queen Mab.png": "queen_mab.png",
    "Shadow Magario=Succubus.png": "shadow_magario.png",
    "Shadow Sae Niijima.png": "shadow_sae_niijima.png",
    "Titania=Shadow Takase.png": "shadow_takase.png",
    "Shiki-Ouji.png": "shiki_ouji.png",
    "Sui-Ki.png": "sui_ki.png",
    "Take-Minakata.png": "take_minakata.png",
    "Yaksini.png": "yaksini.png",
    "Yamata-no-Orochi.png": "yamata_no_orochi.png"
}

# Copy to both P5 and P5R
for game in ["p5", "p5r"]:
    print(f"\nCopying {game.upper()} enemies from were missing folder...")
    target_folder = f"downloaded_enemies/{game}"
    os.makedirs(target_folder, exist_ok=True)
    
    copied = 0
    for source_name, target_name in p5_missing.items():
        source_file = os.path.join(source_folder, source_name)
        target_file = os.path.join(target_folder, target_name)
        
        if os.path.exists(source_file):
            shutil.copy2(source_file, target_file)
            print(f"  ✓ Copied {source_name} -> {target_name}")
            copied += 1
        else:
            print(f"  ✗ Not found: {source_name}")
    
    print(f"  Total copied: {copied}/{len(p5_missing)}")

print("\nCopy complete!")
