import os
import time
import json
import re
from pathlib import Path
import undetected_chromedriver as uc
from selenium.webdriver.common.by import By
import requests
import urllib.parse

OUTPUT_DIR = "../app/src/main/assets/images/personas_shared"
DONE_LOG   = "persona_done.json"
FAILED_LOG = "persona_failed.json"
BASE_URL   = "https://megatenwiki.com/api.php"

# ── Persona game prefixes only — reject everything else ──────────────────────
PERSONA_PREFIXES = ["P5X", "P5R", "P5S", "P5T", "P5", "P4G", "P4", "P3R", "P3P", "P3F", "P3",
                    "PQ2", "PQ1", "PQ", "P2IS", "P2EP", "P1"]

# Prefixes that mean it's an SMT/non-Persona image — hard reject
REJECT_PREFIXES = ["SMT", "DDS", "DS1", "DS2", "SH", "SJ", "RH", "KMT", "NINE",
                   "IMAGINE", "STRANGE", "NOCTURNE", "RAIDOU", "DEVIL", "SOUL",
                   "LAST", "DIGITAL", "PERSONA_1", "MIP_"]

# Image quality keywords (higher = better)
QUALITY_SCORE = {
    "Artwork": 100, "Art": 90, "Render": 80, "Portrait": 80,
    "Model": 70, "Sprite": 30, "Card": 20, "Graphic": 20,
}

# Game prefix score (higher = more recent/preferred)
PREFIX_SCORE = {
    "P5X": 100, "P5R": 90, "P5S": 85, "P5T": 83, "P5": 80,
    "P4G": 70, "P4": 65,
    "P3R": 60, "P3P": 55, "P3F": 52, "P3": 50,
    "PQ2": 40, "PQ1": 37, "PQ": 35, "P2IS": 20, "P2EP": 20, "P1": 10,
}

# Skip these regardless
SKIP_KW = ["Icon", "icon", "Slash", "Ailment", "Status", "Skill", "Bullet",
           "Cut-in", "Cutin", "Battle_UI", "HUD", "Thumbnail", "Favicon",
           "Logo", "Banner", "Cover", "Box", "Scan", "Wallpaper"]

# All persona data files
GAMES = [
    ("p5r", "../app/src/main/assets/data/persona5/royal_personas.json"),
    ("p5",  "../app/src/main/assets/data/persona5/personas.json"),
    ("p4g", "../app/src/main/assets/data/persona4/golden_personas.json"),
    ("p4",  "../app/src/main/assets/data/persona4/personas.json"),
    ("p3r", "../app/src/main/assets/data/persona3/reload_personas.json"),
    ("p3",  "../app/src/main/assets/data/persona3/personas.json"),
    ("p3p", "../app/src/main/assets/data/persona3/portable_personas.json"),
]


def to_filename(name):
    s = name.lower()
    for a, b in [("è","e"),("é","e"),("ā","a"),("ō","o"),("ū","u"),("î","i")]:
        s = s.replace(a, b)
    s = re.sub(r"\s+", "_", s)
    s = re.sub(r"[^a-z0-9_\-]", "", s)
    return s + ".png"


def is_persona_image(title):
    """Return True only if this looks like a Persona-series image."""
    t = title.replace("File:", "")
    # Hard reject SMT/non-persona prefixes
    for bad in REJECT_PREFIXES:
        if t.startswith(bad) or t.startswith(bad.lower()):
            return False
    # Must start with a known persona prefix OR contain persona name keywords
    for p in PERSONA_PREFIXES:
        if t.startswith(p + "_") or t.startswith(p + " "):
            return True
    return False


def score_image(title):
    t = title.replace("File:", "")
    if not is_persona_image(title):
        return -9999
    if any(k in t for k in SKIP_KW):
        return -500
    s = 0
    for prefix, ps in PREFIX_SCORE.items():
        if t.startswith(prefix + "_") or t.startswith(prefix + " "):
            s += ps
            break
    for kw, qs in QUALITY_SCORE.items():
        if kw in t:
            s += qs
            break
    if t.lower().endswith(".png"):
        s += 5
    return s


def fetch_json(driver, url, retries=2):
    for attempt in range(retries + 1):
        driver.get(url)
        time.sleep(2.5)
        src = driver.page_source
        if "Just a moment" in src or "Checking your browser" in src:
            print("    [CF] Challenge, waiting 12s...")
            time.sleep(12)
        try:
            pre = driver.find_element(By.TAG_NAME, "pre")
            return json.loads(pre.text)
        except Exception:
            if attempt < retries:
                time.sleep(3)
    return None


def search_files(driver, query, limit=10):
    """Search the wiki file namespace for images matching query."""
    enc = urllib.parse.quote(query, safe="")
    url = f"{BASE_URL}?action=query&list=search&srsearch={enc}&srnamespace=6&srlimit={limit}&format=json"
    data = fetch_json(driver, url)
    if not data:
        return []
    return [r["title"] for r in data.get("query", {}).get("search", [])]


def get_page_images(driver, page_name):
    """Get all images listed on a wiki page."""
    enc = urllib.parse.quote(page_name, safe="")
    url = f"{BASE_URL}?action=parse&page={enc}&prop=images&format=json"
    data = fetch_json(driver, url)
    if not data or "error" in data:
        return []
    return data.get("parse", {}).get("images", [])


def resolve_url(driver, file_title):
    """Get direct download URL for a File: title."""
    enc = urllib.parse.quote(file_title, safe=":")
    url = f"{BASE_URL}?action=query&titles={enc}&prop=imageinfo&iiprop=url&format=json"
    data = fetch_json(driver, url)
    if not data:
        return None
    pages = data.get("query", {}).get("pages", {})
    page = list(pages.values())[0] if pages else {}
    return (page.get("imageinfo") or [{}])[0].get("url")


def build_search_queries(name):
    """Generate search queries from most to least specific."""
    # Direct overrides for known tricky names
    OVERRIDES = {
        "Orpheus F":           ["Orpheus Telos", "P3R Orpheus Telos", "P3F Orpheus Telos"],
        "Orpheus F Picaro":    ["Orpheus F Picaro", "P5T Orpheus F Picaro"],
        "Seiten Taisei A":     ["Seiten Taisei", "P5 Seiten Taisei"],
        "Hitokoto-Nushi":      ["P4 Hitokoto-Nushi", "Hitokoto-Nushi"],
        "Koropokkuru":         ["Koropokkuru persona", "P3 Koropokkuru", "P4 Koropokkuru"],
        "Seiryuu":             ["Seiryu persona", "P3 Seiryu"],
        "Seiryu":              ["Seiryu persona", "P3 Seiryu"],
        "Yamatano-Orochi":     ["Yamata-no-Orochi", "P3 Yamata-no-Orochi"],
        "Yomotsu Shikome":     ["Yomotsu-Shikome", "P3 Yomotsu-Shikome"],
        "Niddhoggr":           ["Nidhoggr", "P5R Nidhoggr"],
        "Loki A":              ["P5R Loki", "Loki persona"],
        "Seiten Taisei":       ["P5 Seiten Taisei", "Seiten Taisei persona"],
        "Oukuninushi":         ["Okuninushi", "P5R Okuninushi"],
        "Oumitsunu":           ["Oumitsunu persona", "P3 Oumitsunu"],
        "Laksmi":              ["Lakshmi persona", "P3 Lakshmi"],
        "Yaksini":             ["Yakshini", "P5R Yakshini"],
        "Girimehkala":         ["Girimehkala persona", "P3 Girimehkala"],
        "Girimekhala":         ["Girimehkala persona", "P3 Girimehkala"],
    }
    if name in OVERRIDES:
        return OVERRIDES[name]
    queries = []
    for prefix in ["P5X", "P5R", "P5", "P4G", "P4", "P3R", "P3"]:
        queries.append(f"{prefix} {name}")
    queries.append(f"{name} persona")
    queries.append(name)
    return queries


def download_image(driver, img_url, dest_path):
    """Download image using browser session cookies."""
    cookies = {c["name"]: c["value"] for c in driver.get_cookies()}
    ua = driver.execute_script("return navigator.userAgent;")
    resp = requests.get(img_url, cookies=cookies, timeout=25,
                        headers={"User-Agent": ua, "Referer": "https://megatenwiki.com/"})
    if resp.status_code != 200:
        raise Exception(f"HTTP {resp.status_code}")
    magic = resp.content[:4].hex()
    if not (magic.startswith("89504e47") or magic.startswith("ffd8ff") or magic.startswith("47494638")):
        raise Exception(f"Not an image (magic {magic})")
    if len(resp.content) < 3000:
        raise Exception(f"Too small ({len(resp.content)}b)")
    with open(dest_path, "wb") as f:
        f.write(resp.content)
    return len(resp.content)


def scrape_persona(driver, name):
    """
    Strategy:
    1. Search file namespace with game-specific queries
    2. Fall back to page images list
    Pick best scoring Persona-only image.
    """
    candidates = []

    # Strategy 1: file search
    for query in build_search_queries(name):
        results = search_files(driver, query, limit=8)
        candidates.extend(results)
        persona_hits = [r for r in results if is_persona_image(r)]
        if len(persona_hits) >= 2:
            break  # enough good candidates
        time.sleep(0.5)

    # Strategy 2: page images (fallback)
    if not any(is_persona_image(c) for c in candidates):
        page_imgs = get_page_images(driver, name.replace(" ", "_"))
        candidates.extend([f"File:{i}" for i in page_imgs])

    # Dedupe and score
    seen = set()
    unique = []
    for c in candidates:
        if c not in seen:
            seen.add(c)
            unique.append(c)

    scored = sorted(unique, key=score_image, reverse=True)

    # Try top candidates
    for candidate in scored[:5]:
        s = score_image(candidate)
        if s < 0:
            continue  # skip non-persona images entirely

        print(f"    Try: {candidate}  (score {s})")
        img_url = resolve_url(driver, candidate)
        if not img_url:
            continue

        dest = os.path.join(OUTPUT_DIR, to_filename(name))
        try:
            size = download_image(driver, img_url, dest)
            size_kb = round(size / 1024)
            print(f"    OK  {name} -> {to_filename(name)} ({size_kb}KB)")
            return True, to_filename(name)
        except Exception as e:
            print(f"    DL fail: {e}")
            continue

    return False, "no persona image found"


def main():
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    done_log = set(json.load(open(DONE_LOG)) if os.path.exists(DONE_LOG) else [])
    failed   = dict(json.load(open(FAILED_LOG)) if os.path.exists(FAILED_LOG) else {})

    # Collect all unique persona names
    all_personas = {}
    for game_id, json_path in GAMES:
        try:
            data = json.load(open(json_path, encoding="utf-8-sig"))
            names = list(data.keys()) if isinstance(data, dict) else [e.get("name", "") for e in data]
            for name in names:
                if name and name not in all_personas:
                    all_personas[name] = game_id
        except FileNotFoundError:
            print(f"  Skipping {game_id} — not found")

    existing = {p.stem for p in Path(OUTPUT_DIR).glob("*.png")}

    def needs_download(name):
        stem = to_filename(name)[:-4]
        if stem not in existing:
            return True  # missing
        if name in failed:
            return True  # previously failed, retry
        if name not in done_log:
            return True  # not in done log
        return False

    remaining = [n for n in all_personas if needs_download(n)]
    # Retry failed first
    remaining.sort(key=lambda n: (0 if n in failed else 1, n))

    total = len(all_personas)
    print(f"\n{'='*70}")
    print(f"PERSONA IMAGE DOWNLOADER  (P5X -> P3, Persona-only)")
    print(f"{'='*70}")
    print(f"Total unique personas : {total}")
    print(f"On disk               : {len(existing)}")
    print(f"To download/retry     : {len(remaining)}  ({len(failed)} retrying failed)")
    print(f"Output                : {os.path.abspath(OUTPUT_DIR)}")
    print(f"{'='*70}\n")

    if not remaining:
        print("All done!")
        return

    print("Starting Chrome...")
    options = uc.ChromeOptions()
    driver = uc.Chrome(options=options, use_subprocess=True, version_main=145)

    ok_count = fail_count = 0

    try:
        for i, name in enumerate(remaining, 1):
            print(f"\n[{i}/{len(remaining)}] {name}  ({all_personas[name]})")
            ok, info = scrape_persona(driver, name)

            if ok:
                done_log.add(name)
                failed.pop(name, None)
                ok_count += 1
            else:
                failed[name] = info
                fail_count += 1
                print(f"    FAIL: {info}")

            if (ok_count + fail_count) % 5 == 0:
                json.dump(sorted(done_log), open(DONE_LOG, "w"), indent=2)
                json.dump(failed,           open(FAILED_LOG, "w"), indent=2)
                pct = round((total - len(remaining) + ok_count + fail_count) / total * 100)
                print(f"\n  --- {pct}% | +{ok_count} ok | {fail_count} failed ---\n")

            time.sleep(0.8)

    finally:
        json.dump(sorted(done_log), open(DONE_LOG, "w"), indent=2)
        json.dump(failed,           open(FAILED_LOG, "w"), indent=2)
        driver.quit()

    print(f"\n{'='*70}")
    print(f"DONE  |  {ok_count} downloaded  |  {fail_count} failed")
    print(f"Images: {os.path.abspath(OUTPUT_DIR)}")
    if failed:
        print(f"Failed: {os.path.abspath(FAILED_LOG)}")
    print(f"{'='*70}\n")


if __name__ == "__main__":
    main()
