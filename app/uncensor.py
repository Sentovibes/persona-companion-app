import json, glob, re

def fix_censors(text):
    if not isinstance(text, str): return text
    # Heart character variants: ❤ (U+2764) and ❤️ (U+2764 + U+FE0F)
    hearts_pattern = re.compile(r'[❤\u2764\ufe0f]+')
    
    def replacer(match):
        h = match.group(0)
        # Using a conservative count
        count = len(h.replace('\ufe0f', ''))
        if count >= 7: return 'asshole'
        if count == 6: return 'asshole'
        if count == 5: return 'shitty'
        if count == 4: return 'shit'
        if count == 3: return 'ass'
        return '...'

    return hearts_pattern.sub(replacer, text)

def deep_fix(obj):
    if isinstance(obj, dict):
        return {k: deep_fix(v) for k, v in obj.items()}
    elif isinstance(obj, list):
        return [deep_fix(i) for i in obj]
    elif isinstance(obj, str):
        return fix_censors(obj)
    return obj

for f_path in glob.glob(r'c:\Users\omare\Music\Persona-Companion-App\persona-companion-app\app\src\main\assets\data\social-links\*.json'):
    with open(f_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    updated_data = deep_fix(data)
    
    with open(f_path, 'w', encoding='utf-8') as out:
        json.dump(updated_data, out, indent=2, ensure_ascii=False)

print('Success: All files uncensored.')
