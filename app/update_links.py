import json

def update_p4g():
    path = r'c:\Users\omare\Music\Persona-Companion-App\persona-companion-app\app\src\main\assets\data\social-links\p4+p4g_social_links.json'
    with open(path, 'r', encoding='utf-8') as f:
        data = json.load(f)

    p4g_awakenings = {
        'Magician': {'Persona': 'Takehaya Susano-o', 'Name': 'Youthful Wind', 'Description': 'Restores HP/Cures ailments for party and increases Hit/Evasion rate.', 'Requirement': 'Talk to Yosuke in January (Rank 10 required)'},
        'Chariot': {'Persona': 'Haraedo-no-Okami', 'Name': 'Dragon Hustle', 'Description': 'Increases Attack, Defense, and Hit/Evasion for the whole party.', 'Requirement': 'Talk to Chie in January (Rank 10 required)'},
        'Priestess': {'Persona': 'Sume-Omikami', 'Name': 'Burning Petals', 'Description': 'Severe Fire damage to all enemies.', 'Requirement': 'Talk to Yukiko in January (Rank 10 required)'},
        'Emperor': {'Persona': 'Takeji Zaiten', 'Name': "The Man's Way", 'Description': 'Chance to Down and Dizzy all enemies.', 'Requirement': 'Talk to Kanji in January (Rank 10 required)'},
        'Lovers': {'Persona': 'Kouzeon', 'Name': 'Complete Analysis', 'Description': 'Reveals all enemy weaknesses and stats at the start of battle.', 'Requirement': 'Talk to Rise in January (Rank 10 required)'},
        'Fortune': {'Persona': 'Yamato Sumikami', 'Name': 'Shield of Justice', 'Description': 'Protects the party from all damage for one turn.', 'Requirement': 'Talk to Naoto in January (Rank 10 required)'},
        'Star': {'Persona': 'Kamui-Moshiri', 'Name': 'Kamui Miracle', 'Description': 'Random effect on all allies and enemies.', 'Requirement': 'Talk to Teddie in January (Rank 10 required)'}
    }

    for arcana, awakening in p4g_awakenings.items():
        if arcana in data:
            data[arcana]['ThirdAwakening'] = awakening
            # Also ensure Ultimate Persona (Rank 10) is set for the "Golden Trap" warning
            ultimates = {
                'Magician': 'Susano-o', 'Chariot': 'Suzuka Gongen', 'Priestess': 'Amaterasu',
                'Emperor': 'Take-Mikazuchi', 'Lovers': 'Kanzeon', 'Fortune': 'Yamato-Takeru', 'Star': 'Kamui'
            }
            if arcana in ultimates:
                data[arcana]['UltimatePersona'] = ultimates[arcana]

    with open(path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)

def update_p5r():
    path = r'c:\Users\omare\Music\Persona-Companion-App\persona-companion-app\app\src\main\assets\data\social-links\p5+p5r_social_links.json'
    with open(path, 'r', encoding='utf-8') as f:
        data = json.load(f)

    p5r_awakenings = {
        'Chariot': {'Persona': 'William', 'Requirement': 'Standard hang-out in Third Semester'},
        'Lovers': {'Persona': 'Celestine', 'Requirement': 'Standard hang-out in Third Semester'},
        'Magician': {'Persona': 'Diego', 'Requirement': 'Standard hang-out in Third Semester'},
        'Emperor': {'Persona': 'Gorokichi', 'Requirement': 'Standard hang-out in Third Semester'},
        'Priestess': {'Persona': 'Agnes', 'Requirement': 'Standard hang-out in Third Semester'},
        'Hermit': {'Persona': 'Al Azif', 'Requirement': 'Standard hang-out in Third Semester'},
        'Empress': {'Persona': 'Lucy', 'Requirement': 'Standard hang-out in Third Semester'},
        'Justice': {'Persona': 'Hereward', 'Requirement': 'Standard hang-out in Third Semester'},
        'Faith': {'Persona': 'Ella', 'Requirement': 'Standard hang-out in Third Semester'}
    }

    for arcana, awakening in p5r_awakenings.items():
        if arcana in data:
            # We already have UltimatePersona set for P5R in the previous script
            data[arcana]['ThirdAwakening'] = awakening

    with open(path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)

update_p4g()
update_p5r()
print('Success: P4G and P5R Third Awakenings Injected.')
