#!/usr/bin/env python3
"""
Populate canonical Palace & Mementos locations for all P5 / P5R shadows.
Replaces 'Unknown' with the authentic in-game Palace and Mementos area.
"""

import json
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent.parent

PALACE_LOCATIONS = {
    # Kamoshida's Palace
    "Pixie": "Kamoshida's Palace / Qimranut",
    "Jack-o'-Lantern": "Kamoshida's Palace / Qimranut",
    "Agathion": "Kamoshida's Palace / Aiyatsbus",
    "Mandrake": "Kamoshida's Palace / Qimranut",
    "Bicorn": "Kamoshida's Palace / Aiyatsbus",
    "Incubus": "Kamoshida's Palace / Aiyatsbus",
    "Cait Sith": "Kamoshida's Palace / Qimranut",
    "Kelpie": "Kamoshida's Palace / Aiyatsbus",
    "Silky": "Kamoshida's Palace / Aiyatsbus",
    "Succubus": "Kamoshida's Palace / Aiyatsbus",
    "Berith": "Kamoshida's Palace / Aiyatsbus",
    "Archangel": "Kamoshida's Palace / Aiyatsbus",
    "Eligor": "Kamoshida's Palace / Chemdah",

    # Madarame's Palace
    "Obariyon": "Madarame's Palace / Aiyatsbus",
    "Hua Po": "Madarame's Palace / Chemdah",
    "Koropokguru": "Madarame's Palace / Chemdah",
    "Koropokkuru": "Madarame's Palace / Chemdah",
    "Mokoi": "Madarame's Palace / Chemdah",
    "Angel": "Madarame's Palace / Aiyatsbus",
    "Regent": "Madarame's Palace / Chemdah",
    "Slime": "Madarame's Palace / Aiyatsbus",
    "Koppa Tengu": "Madarame's Palace / Chemdah",
    "Jack Frost": "Madarame's Palace / Chemdah",
    "Apsaras": "Madarame's Palace / Chemdah",
    "Kodama": "Madarame's Palace / Chemdah",
    "Onmoraki": "Madarame's Palace / Chemdah",
    "Ame-no-Uzume": "Madarame's Palace / Chemdah",
    "Ippon-Datara": "Madarame's Palace / Chemdah",
    "Inugami": "Madarame's Palace / Chemdah",
    "Makami": "Madarame's Palace / Chemdah",
    "Shiisaa": "Madarame's Palace / Chemdah",
    "Nekomata": "Madarame's Palace / Chemdah",
    "Orobas": "Madarame's Palace / Chemdah",
    "Sudama": "Madarame's Palace / Chemdah",
    "Shiki-Ouji": "Madarame's Palace / Chemdah",
    "Nue": "Madarame's Palace / Chemdah",

    # Kaneshiro's Palace
    "Queen's Necklace": "Kaneshiro's Palace / Kaitul",
    "High Pixie": "Kaneshiro's Palace / Kaitul",
    "Black Ooze": "Kaneshiro's Palace / Kaitul",
    "Leanan Sidhe": "Kaneshiro's Palace / Kaitul",
    "Oni": "Kaneshiro's Palace / Kaitul",
    "Yaksini": "Kaneshiro's Palace / Kaitul",
    "Orthrus": "Kaneshiro's Palace / Kaitul",
    "Fuu-Ki": "Kaneshiro's Palace / Kaitul",
    "Rakshasa": "Kaneshiro's Palace / Kaitul",
    "Naga": "Kaneshiro's Palace / Kaitul",
    "Sui-Ki": "Kaneshiro's Palace / Kaitul",
    "Kin-Ki": "Kaneshiro's Palace / Kaitul",
    "Take-Minakata": "Kaneshiro's Palace / Kaitul",

    # Futaba's Palace
    "Stone of Scone": "Futaba's Palace / Akzeriyyuth",
    "Sandman": "Futaba's Palace / Akzeriyyuth",
    "Anzu": "Futaba's Palace / Akzeriyyuth",
    "Isis": "Futaba's Palace / Akzeriyyuth",
    "Lamia": "Futaba's Palace / Akzeriyyuth",
    "Andras": "Futaba's Palace / Akzeriyyuth",
    "Pisaca": "Futaba's Palace / Akzeriyyuth",
    "Choronzon": "Futaba's Palace / Akzeriyyuth",
    "Kurama Tengu": "Futaba's Palace / Akzeriyyuth",
    "Mothman": "Futaba's Palace / Akzeriyyuth",
    "Anubis": "Futaba's Palace / Akzeriyyuth",
    "Thunderbird": "Futaba's Palace / Akzeriyyuth",
    "Arahabaki": "Futaba's Palace / Akzeriyyuth",
    "Thoth": "Futaba's Palace / Akzeriyyuth",

    # Okumura's Palace
    "Koh-i-Noor": "Okumura's Palace / Adyeshach",
    "Decarabia": "Okumura's Palace / Adyeshach",
    "Lilim": "Okumura's Palace / Adyeshach",
    "Kaiwan": "Okumura's Palace / Adyeshach",
    "Belphegor": "Okumura's Palace / Adyeshach",
    "Legion": "Okumura's Palace / Adyeshach",
    "Mithras": "Okumura's Palace / Adyeshach",
    "Unicorn": "Okumura's Palace / Adyeshach",
    "Kikuri-Hime": "Okumura's Palace / Adyeshach",
    "Power": "Okumura's Palace / Adyeshach",
    "Ose": "Okumura's Palace / Adyeshach",
    "Kushinada": "Okumura's Palace / Adyeshach",
    "Kumbhanda": "Okumura's Palace / Adyeshach",
    "Girimehkala": "Okumura's Palace / Adyeshach",
    "Melchizedek": "Okumura's Palace / Adyeshach",
    "Abyssal King of Avarice": "Okumura's Palace (Boss)",

    # Sae's Palace
    "Orlov": "Sae's Palace / Sheriruth",
    "Queen Mab": "Sae's Palace / Sheriruth",
    "Valkyrie": "Sae's Palace / Sheriruth",
    "Rangda": "Sae's Palace / Sheriruth",
    "Narcissus": "Sae's Palace / Sheriruth",
    "Dakini": "Sae's Palace / Sheriruth",
    "Sarasvati": "Sae's Palace / Sheriruth",
    "Ganesha": "Sae's Palace / Sheriruth",
    "Skadi": "Sae's Palace / Sheriruth",
    "Parvati": "Sae's Palace / Sheriruth",
    "Thor": "Sae's Palace / Sheriruth",
    "Yamata-no-Orochi": "Sae's Palace / Sheriruth",
    "Bugs": "Sae's Palace / Sheriruth",
    "Oberon": "Sae's Palace / Sheriruth",

    # Shido's Palace
    "Emperor's Amulet": "Shido's Palace / Sheriruth",
    "Jatayu": "Shido's Palace / Sheriruth",
    "Barong": "Shido's Palace / Sheriruth",
    "Norn": "Shido's Palace / Sheriruth",
    "Garuda": "Shido's Palace / Sheriruth",
    "Cerberus": "Shido's Palace / Sheriruth",
    "Titania": "Shido's Palace / Sheriruth",
    "Baphomet": "Shido's Palace / Sheriruth",
    "Lilith": "Shido's Palace / Sheriruth",
    "Moloch": "Shido's Palace / Sheriruth",
    "King Frost": "Shido's Palace / Sheriruth",
    "Forneus": "Shido's Palace / Sheriruth",
    "Kali": "Shido's Palace / Sheriruth",
    "Hanuman": "Shido's Palace / Sheriruth",

    # Qliphoth World / Depths of Mementos
    "Hope Diamond": "Depths of Mementos",
    "Crystal Skull": "Depths of Mementos / Da'at",
    "Orichalcum": "Qliphoth World / Da'at",
    "Mara": "Depths of Mementos & Qliphoth",
    "Chernobog": "Qliphoth World",
    "Dominion": "Qliphoth World",
    "Mot": "Qliphoth World",
    "Throne": "Qliphoth World",
    "Nebiros": "Qliphoth World",
    "Abaddon": "Qliphoth World",
    "Belial": "Qliphoth World",
    "Baal": "Qliphoth World",

    # Maruki's Palace (P5R)
    "Atavaka": "Maruki's Palace / Da'at",
    "Loa": "Maruki's Palace / Da'at",
    "Byakhee": "Maruki's Palace / Da'at",
    "Dionysus": "Maruki's Palace / Da'at",
    "Macabre": "Maruki's Palace / Da'at",
    "Chimera": "Maruki's Palace / Da'at",
    "Cu Chulainn": "Maruki's Palace / Da'at",
    "Scathach": "Maruki's Palace / Da'at",
    "Alilat": "Maruki's Palace / Da'at",
    "Hastur": "Maruki's Palace / Da'at",
    "Fafnir": "Maruki's Palace / Da'at",
}

SHADOW_NAME_OVERRIDES = {
    "Abyssal King of Avarice": "Okumura's Palace (Boss)",
    "Justine & Caroline": "Velvet Room (Secret Boss)",
    "Leafy Old Man": "Madarame's Palace / Chemdah",
}

def update_enemy_file(path: Path):
    if not path.exists():
        return
    with open(path, 'r', encoding='utf-8-sig') as f:
        data = json.load(f)

    updated = 0
    for enemy in data:
        curr_area = enemy.get("area", "").strip()
        if curr_area == "Unknown" or not curr_area or curr_area == "-":
            name = enemy.get("name", "")
            pname = enemy.get("persona_name", "")

            new_loc = None
            if name in SHADOW_NAME_OVERRIDES:
                new_loc = SHADOW_NAME_OVERRIDES[name]
            elif pname in PALACE_LOCATIONS:
                new_loc = PALACE_LOCATIONS[pname]
            elif name in PALACE_LOCATIONS:
                new_loc = PALACE_LOCATIONS[name]

            if new_loc:
                enemy["area"] = new_loc
                updated += 1

    with open(path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)

    print(f"Updated {updated} shadow locations in {path.relative_to(BASE_DIR)}")

def main():
    target_files = [
        BASE_DIR / "app" / "src" / "main" / "assets" / "data" / "enemies" / "p5_enemies.json",
        BASE_DIR / "app" / "src" / "main" / "assets" / "data" / "enemies" / "p5r_enemies.json",
        BASE_DIR / "docs" / "data" / "enemies" / "p5_enemies.json",
        BASE_DIR / "docs" / "data" / "enemies" / "p5r_enemies.json",
    ]
    for p in target_files:
        update_enemy_file(p)

if __name__ == "__main__":
    main()
