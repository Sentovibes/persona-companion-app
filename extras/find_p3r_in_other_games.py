import os
from pathlib import Path

def safe_filename(name):
    """Convert name to safe filename format"""
    safe = name.lower().replace(" ", "_").replace("/", "_").replace(":", "").replace("?", "").replace("&", "").replace("-", "_")
    safe = safe.replace("___", "_")
    return safe

# P3R missing enemies
p3r_missing = [
    'Ancient Castle', 'Appropriating Noble', 'Arcanist Decapitator', 'Barbaric Beast Wheel',
    'Black Hand', 'Blazing Middle Sibling', 'Bloody Maria', 'Chaos Panzer',
    'Clairvoyant Relic', 'Comeback Castle', 'Controlling Partner', 'Cruel Greatsword',
    'Cultist of Death', 'Cultist of the Storm', 'Dancing Beast Wheel', 'Deadly Eldest Sibling',
    'Demented Knight', 'Dependent Partner', 'Despairing Tiara', 'Deviant Convict',
    'Disturbing Dice', 'Enslaved Cupid', 'Executioner\'s Crown', 'Executive Greatsword',
    'Feral Beast', 'Five Fingers of Blight', 'Fleetfooted Cavalry', 'Foot Soldier',
    'Genocidal Mercenary', 'Gold Hand', 'Grievous Table', 'Haughty Belle',
    'Haunted Castle', 'Heartless Relic', 'Heat Overseer', 'Hedonistic Sinner',
    'Heretic Magus', 'High Judge of Hell', 'Icebreaker Lion', 'Ill-Fated Maya',
    'Imposing Skyscraper', 'Invasive Serpent', 'Invigorated Gigas', 'Isolated Castle',
    'Jin', 'Jotun of Authority', 'Lascivious Lady', 'Lightning Eagle',
    'Loathsome Tiara', 'Luckless Cupid', 'Merciless Judge', 'Minotaur Nulla',
    'Morbid Book', 'Necromachinery', 'Obsessive Sand', 'Ochlocratic Sand',
    'Omnipotent Balance', 'Overseer of Creation', 'Pagoda of Disaster', 'Pink Hand',
    'Profligate Gigas', 'Purging Right Hand', 'Raging Turret', 'Rampaging Sand',
    'Resentful Surveillant', 'Scornful Dice', 'Serpent of Absurdity', 'Servant Tower',
    'Silver Hand', 'Skeptical Tiara', 'Sky Overseer', 'Slaughter Twins',
    'Spiritual Castle', 'Statue', 'Subservient Left Hand', 'Swift Axle',
    'Takaya', 'Tank-Form Shadow', 'Terminal Table', 'Terror Dice',
    'Tome of Atrophy', 'Tome of Persecution', 'Ultimate Gigas', 'Venomous Magus',
    'Voltaic Youngest Sibling', 'White Hand', 'Will O\' Wisp Raven'
]

# Other game folders to search
other_games = ['p3fes', 'p3p', 'p4', 'p4g', 'p5', 'p5r']

found_in_other_games = {}

for enemy_name in p3r_missing:
    safe_name = safe_filename(enemy_name)
    
    for game in other_games:
        game_folder = Path(f"downloaded_enemies/{game}")
        if game_folder.exists():
            # Check for the file
            for ext in ['.png', '.jpg', '.jpeg']:
                file_path = game_folder / f"{safe_name}{ext}"
                if file_path.exists():
                    if enemy_name not in found_in_other_games:
                        found_in_other_games[enemy_name] = []
                    found_in_other_games[enemy_name].append((game, str(file_path)))
                    break

print(f"P3R Missing Enemies Found in Other Games: {len(found_in_other_games)}")
print("="*70)

if found_in_other_games:
    for enemy_name, locations in sorted(found_in_other_games.items()):
        print(f"\n{enemy_name}:")
        for game, path in locations:
            print(f"  Found in {game.upper()}: {path}")
else:
    print("No P3R missing enemies found in other game folders.")

print("\n" + "="*70)
print(f"Total found: {len(found_in_other_games)}/{len(p3r_missing)}")
