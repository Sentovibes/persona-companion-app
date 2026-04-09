"""Fix a single persona image — searches wiki, shows candidates, downloads best."""
import undetected_chromedriver as uc
from selenium.webdriver.common.by import By
import json, time, urllib.parse, os, requests
from pathlib import Path

TARGET_NAME = "Thanatos"
OUTPUT_DIR  = "../app/src/main/assets/images/personas_shared"
BASE        = "https://megatenwiki.com/api.php"

PERSONA_PREFIXES = ["P5X","P5R","P5S","P5T","P5","P4G","P4","P3R","P3P","P3F","P3","PQ2","PQ1","PQ"]
REJECT = ["SMT","DDS","DS1","DS2","SH","SJ","RH","KMT","NINE","IMAGINE","STRANGE","NOCTURNE","RAIDOU"]

def is_persona(title):
    t = title.replace("File:","")
    for bad in REJECT:
        if t.startswith(bad):
            return False
    for p in PERSONA_PREFIXES:
        if t.startswith(p+"_") or t.startswith(p+" "):
            return True
    return False

def fetch(driver, url):
    driver.get(url)
    time.sleep(3)
    if "Just a moment" in driver.page_source:
        print("  [CF] waiting..."); time.sleep(12)
    try:
        return json.loads(driver.find_element(By.TAG_NAME,"pre").text)
    except:
        return None

options = uc.ChromeOptions()
driver = uc.Chrome(options=options, use_subprocess=True, version_main=145)

try:
    all_results = []
    queries = ["P5R Thanatos", "P3R Thanatos", "P3 Thanatos", "Thanatos persona"]
    for q in queries:
        enc = urllib.parse.quote(q, safe="")
        data = fetch(driver, f"{BASE}?action=query&list=search&srsearch={enc}&srnamespace=6&srlimit=8&format=json")
        if data:
            hits = [r["title"] for r in data.get("query",{}).get("search",[])]
            all_results.extend(hits)
        time.sleep(1)

    # Dedupe and show all
    seen = set()
    unique = [x for x in all_results if not (x in seen or seen.add(x))]
    persona_hits = [x for x in unique if is_persona(x)]

    print(f"\nAll persona images found for {TARGET_NAME}:")
    for i, r in enumerate(persona_hits):
        print(f"  [{i}] {r}")

    if not persona_hits:
        print("No persona images found!")
        driver.quit()
        exit()

    # Pick best — prefer P5R/P3R Model or Artwork
    def score(t):
        s = 0
        for i, p in enumerate(["P5X","P5R","P5","P4G","P4","P3R","P3P","P3F","P3"]):
            if t.startswith("File:"+p+" ") or t.startswith("File:"+p+"_"):
                s += (9-i)*20; break
        for kw, v in [("Artwork",100),("Art",90),("Render",80),("Model",70),("Sprite",30),("Graphic",20)]:
            if kw in t: s += v; break
        return s

    best = max(persona_hits, key=score)
    print(f"\nBest: {best} (score {score(best)})")

    # Resolve URL
    enc = urllib.parse.quote(best, safe=":")
    data2 = fetch(driver, f"{BASE}?action=query&titles={enc}&prop=imageinfo&iiprop=url&format=json")
    pages = data2.get("query",{}).get("pages",{}) if data2 else {}
    img_url = (list(pages.values())[0].get("imageinfo") or [{}])[0].get("url") if pages else None

    if not img_url:
        print("Could not resolve URL"); driver.quit(); exit()

    print(f"URL: {img_url}")

    # Download
    cookies = {c["name"]:c["value"] for c in driver.get_cookies()}
    ua = driver.execute_script("return navigator.userAgent;")
    resp = requests.get(img_url, cookies=cookies, timeout=25,
                        headers={"User-Agent":ua,"Referer":"https://megatenwiki.com/"})

    dest = Path(OUTPUT_DIR) / "thanatos.png"
    with open(dest,"wb") as f:
        f.write(resp.content)
    print(f"\nSaved: {dest} ({resp.content.__len__()//1024}KB)")

finally:
    driver.quit()
