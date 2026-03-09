"""
Test specific personas we know the URLs for
"""
import requests
import hashlib

def get_wikia_hash(filename):
    md5 = hashlib.md5(filename.encode('utf-8')).hexdigest()
    return md5[0], md5[0:2]

def test_url(filename):
    hash1, hash2 = get_wikia_hash(filename)
    url = f"https://static.wikia.nocookie.net/megamitensei/images/{hash1}/{hash2}/{filename}"
    
    try:
        response = requests.head(url, timeout=2)
        if response.status_code == 200:
            print(f"✓ {filename}")
            return True
        else:
            print(f"✗ {filename} - Status {response.status_code}")
            return False
    except Exception as e:
        print(f"✗ {filename} - Error: {e}")
        return False

print("Testing known URLs:")
print("="*60)

# Test the ones we know
test_url("P5X_Hell_Biker_uncensored.png")
test_url("P5X_Barong.png")
test_url("Andras_(Uncensored).png")
test_url("Ardha_(SMTII_Art).png")
test_url("P5X_Pyro_Jack.png")

print("\nTesting what our script would generate for 'Hell Biker':")
print("="*60)

name = "Hell Biker"
wiki_name = name.replace(" ", "_")

patterns = [
    f"P5X_{wiki_name}",
    f"P5X_{wiki_name}_uncensored",
    f"P5R_{wiki_name}",
]

for pattern in patterns:
    for ext in ['.png', '.jpg']:
        test_url(pattern + ext)
