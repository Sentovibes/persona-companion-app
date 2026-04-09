import undetected_chromedriver as uc
from selenium.webdriver.common.by import By
import json, time, urllib.parse

# Check hitokoto-nushi (bad image) + 4 failed ones
tests = [
    "Hitokoto-Nushi",
    "Hitokoto-nushi",
    "Koropokkuru",
    "Orpheus F",
    "Orpheus Telos",
    "Seiten Taisei A",
    "Seiten_Taisei",
]

BASE = "https://megatenwiki.com/api.php"

options = uc.ChromeOptions()
driver = uc.Chrome(options=options, use_subprocess=True, version_main=145)

def search(query):
    enc = urllib.parse.quote(query, safe="")
    url = f"{BASE}?action=query&list=search&srsearch={enc}&srnamespace=6&srlimit=6&format=json"
    driver.get(url)
    time.sleep(3)
    src = driver.page_source
    if "Just a moment" in src:
        print("  [CF] waiting...")
        time.sleep(12)
    try:
        pre = driver.find_element(By.TAG_NAME, "pre")
        data = json.loads(pre.text)
        return [r["title"] for r in data.get("query", {}).get("search", [])]
    except:
        return []

for name in tests:
    results = search(name)
    persona = [r for r in results if any(p in r for p in ["P5","P4","P3","PQ"])]
    print(f"\n{name}:")
    if persona:
        for r in persona[:4]:
            print(f"  PERSONA: {r}")
    elif results:
        for r in results[:3]:
            print(f"  OTHER:   {r}")
    else:
        print("  NO RESULTS")
    time.sleep(1)

driver.quit()
