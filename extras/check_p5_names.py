import json

data = json.load(open('../app/src/main/assets/data/enemies/p5_enemies.json', 'r', encoding='utf-8'))

check = ['Ame-no-Uzume', 'Fuu-Ki', 'Ippon-Datara']

for persona in check:
    enemies = [x for x in data if x.get('persona_name') == persona]
    if enemies:
        e = enemies[0]
        print(f"{persona}:")
        print(f"  enemy name: {e.get('name')}")
        print(f"  persona_name: {e.get('persona_name')}")
