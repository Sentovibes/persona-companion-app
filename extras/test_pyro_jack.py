"""
Test downloading Pyro Jack (Jack-o'-Lantern) using the CDN URL
"""
import requests
import hashlib

def get_wikia_hash(filename):
    """Calculate Wikia's MD5 hash for a filename"""
    md5 = hashlib.md5(filename.encode('utf-8')).hexdigest()
    return md5[0], md5[0:2]

# The actual filename from the wiki
filename = "P5X_Pyro_Jack.png"
hash1, hash2 = get_wikia_hash(filename)

url = f"https://static.wikia.nocookie.net/megamitensei/images/{hash1}/{hash2}/{filename}"

print(f"Testing: {filename}")
print(f"Hash: {hash1}/{hash2}")
print(f"URL: {url}")
print()

try:
    response = requests.get(url, timeout=10)
    print(f"Status: {response.status_code}")
    
    if response.status_code == 200:
        with open('test_pyro_jack.png', 'wb') as f:
            f.write(response.content)
        print("✓ Downloaded successfully!")
    else:
        print("✗ Failed to download")
except Exception as e:
    print(f"✗ Error: {e}")
