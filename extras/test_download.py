import requests

url = "https://megatenwiki.com/images/4/48/P3_Elizabeth_Artwork.png"

headers = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
}

print(f"Downloading: {url}")
response = requests.get(url, timeout=30, headers=headers)
print(f"Status: {response.status_code}")
print(f"Content length: {len(response.content)}")

if response.status_code == 200:
    with open("test_elizabeth.png", 'wb') as f:
        f.write(response.content)
    print("✓ Downloaded successfully!")
else:
    print(f"✗ Failed: {response.text[:200]}")
