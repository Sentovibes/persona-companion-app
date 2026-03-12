import requests
import os
from pathlib import Path

print("=" * 70)
print("DOWNLOADING MISSING ENEMIES FROM DIRECT URLS")
print("=" * 70)

# Direct URLs you provided
direct_downloads = {
    "bestial_wheel": "https://megatenwiki.com/images/b/ba/P3R_Bestial_Wheel_Model.png",
    "emperor": "https://megatenwiki.com/images/9/91/P3R_Emperor_Shadow_Model.png",
    "empress": "https://megatenwiki.com/images/9/9f/P3R_Empress_Shadow_Model.png",
    "elizabeth": "https://megatenwiki.com/images/4/4f/P3R_Elizabeth_Artwork.png",
    "nyx_avatar": "https://megatenwiki.com/images/d/de/P3R_Nyx_Avatar_Model.png",
    "chidori_yoshino": "https://megatenwiki.com/images/e/e5/P3R_Chidori_Yoshino_Artwork.png",
}

output_folder = "downloaded_enemies/p3r"
os.makedirs(output_folder, exist_ok=True)

headers = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
}

for safe_name, url in direct_downloads.items():
    output_path = os.path.join(output_folder, f"{safe_name}.png")
    
    # Skip if already exists
    if os.path.exists(output_path):
        print(f"⏭️  {safe_name}.png already exists, skipping")
        continue
    
    try:
        print(f"Downloading {safe_name}...")
        response = requests.get(url, headers=headers, timeout=15)
        
        if response.status_code == 200:
            with open(output_path, 'wb') as f:
                f.write(response.content)
            print(f"✅ Saved: {safe_name}.png")
        else:
            print(f"❌ Failed: {safe_name} (Status: {response.status_code})")
    except Exception as e:
        print(f"❌ Error downloading {safe_name}: {e}")

print("\n" + "=" * 70)
print("DOWNLOAD COMPLETE")
print("=" * 70)
