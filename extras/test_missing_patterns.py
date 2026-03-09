"""
Test different filename patterns for missing personas
"""
import requests
import hashlib

def get_wikia_hash(filename):
    """Calculate Wikia's MD5 hash for a filename"""
    md5 = hashlib.md5(filename.encode('utf-8')).hexdigest()
    return md5[0], md5[0:2]

def test_patterns(name, game_suffix=""):
    """Test many filename patterns and show which ones work"""
    print(f"\nTesting: {name}")
    print("="*60)
    
    # Generate many possible patterns
    patterns = []
    
    # Basic transformations
    no_space = name.replace(" ", "")
    underscore = name.replace(" ", "_")
    no_apostrophe = name.replace("'", "")
    no_hyphen = name.replace("-", "")
    no_special = name.replace("'", "").replace("-", "")
    
    # Combine transformations
    patterns.extend([
        name,
        no_space,
        underscore,
        no_apostrophe,
        no_hyphen,
        no_special,
        no_apostrophe.replace(" ", ""),
        no_apostrophe.replace(" ", "_"),
        no_hyphen.replace(" ", ""),
        no_hyphen.replace(" ", "_"),
        no_special.replace(" ", ""),
        no_special.replace(" ", "_"),
        name.replace("-", " "),
        name.replace("-", "_"),
        no_apostrophe.replace("-", " "),
        no_apostrophe.replace("-", "_"),
    ])
    
    # Add game suffix versions
    if game_suffix:
        base_patterns = patterns.copy()
        for p in base_patterns:
            patterns.append(f"{p}_{game_suffix}")
            patterns.append(f"{p} {game_suffix}")
    
    # Remove duplicates
    patterns = list(dict.fromkeys(patterns))
    
    extensions = ['.png', '.jpg', '.jpeg', '.webp']
    
    found = []
    for pattern in patterns:
        for ext in extensions:
            filename = pattern + ext
            hash1, hash2 = get_wikia_hash(filename)
            url = f"https://static.wikia.nocookie.net/megamitensei/images/{hash1}/{hash2}/{filename}"
            
            try:
                response = requests.head(url, timeout=5)
                if response.status_code == 200:
                    found.append((pattern, ext, url))
                    print(f"✓ FOUND: {pattern}{ext}")
            except:
                pass
    
    if not found:
        print("✗ No matches found")
    
    return found

# Test some problematic personas
test_cases = [
    ("Jack-o'-Lantern", "P5R"),
    ("Izanagi-no-Okami", "P4G"),
    ("Hell Biker", "P5R"),
    ("King Frost", "P5R"),
    ("Pyro Jack", "P5R"),
    ("Cu Chulainn", "P5R"),
    ("Kusi Mitama", "P4G"),
    ("Nigi Mitama", "P4G"),
]

print("Testing filename patterns for missing personas...")
print("This will help us understand the wiki's naming convention")

for name, suffix in test_cases:
    test_patterns(name, suffix)
    
print("\n" + "="*60)
print("Testing complete!")
