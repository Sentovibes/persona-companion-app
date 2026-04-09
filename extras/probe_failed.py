import undetected_chromedriver as uc
from selenium.webdriver.common.by import By
import json, time, urllib.parse

tests = [
    ("Byakko",        "P3 Byakko"),
    ("Pyro Jack",     "Pyro Jack persona"),
    ("Seiten Taisei", "Seiten Taisei persona"),
    ("Satanael",      "P5R Satanael"),
    ("Helel",         "P3 Helel persona"),
    ("Ariadne Picaro","Ariadne Picaro"),
    ("Kohryu",        "Kohryu persona"),
    ("Dis",           "Dis persona"),
    ("Seiryu",        "Seiryu persona"),
    ("Houou",         "Houou persona"),
]

options = uc.ChromeOptions()
driver = uc.Chrome(options=options, use_subprocess=True, version_main=145)

for persona, query in tests:
    enc = urllib.parse.quote(query, safe="")
    url = f"https://megatenwiki.com/api.php?action=query&list=search&srsearch={enc}&srnamespace=6&srlimit=5&format=json"
    driver.get(url)
    time.sleep(3)
    try:
        pre = driver.find_element(By.TAG_NAME, "pre")
        data = json.loads(pre.text)
        results = data.get("query", {}).get("search", [])
        print(f"\n{persona} ({query}) -> {len(results)} results")
        for r in results:
            print(f"   {r['title']}")
    except Exception as e:
        print(f"{persona}: ERROR {e}")
    time.sleep(1)

driver.quit()
