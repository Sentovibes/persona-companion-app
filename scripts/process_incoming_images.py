#!/usr/bin/env python3
"""
Persona Companion App — Incoming Images Processor & Filter
Scans `incoming_images/`, identifies target Persona or Enemy,
optimizes assets for both Android (PNG) and Web (WebP),
and reports remaining missing artwork.
"""

import os
import sys
import json
import re
import shutil
from pathlib import Path
from PIL import Image

BASE_DIR = Path(__file__).resolve().parent.parent
INBOX_DIR = BASE_DIR / "incoming_images"
PROCESSED_DIR = INBOX_DIR / "processed"

APP_PERSONAS_DIR = BASE_DIR / "app" / "src" / "main" / "assets" / "images" / "personas_shared"
APP_ENEMIES_DIR = BASE_DIR / "app" / "src" / "main" / "assets" / "images" / "enemies_shared"

WEB_PERSONAS_DIR = BASE_DIR / "docs" / "assets" / "images" / "personas"
WEB_ENEMIES_DIR = BASE_DIR / "docs" / "assets" / "images" / "enemies"

def sanitize_name(name: str) -> str:
    if not name:
        return ""
    s = name.lower()
    for src, dst in [('è', 'e'), ('é', 'e'), ('ā', 'a'), ('ō', 'o'), ('ū', 'u'), ('î', 'i'), ('\u2019', '')]:
        s = s.replace(src, dst)
    s = re.sub(r'\s+', '_', s)
    s = s.replace('/', '_')
    for ch in [':', '?', '\'', '&']:
        s = s.replace(ch, '')
    return s

def sanitize_enemy_name(name: str) -> str:
    if not name:
        return ""
    clean = name
    if re.search(r'\s[A-Z]$', clean):
        clean = clean[:-2].strip()
    if ' & ' in clean:
        clean = clean.split(' & ')[0]
    boss_map = {'chidori yoshino': 'chidori', 'jin shirato': 'jin', 'takaya sakaki': 'takaya'}
    if clean.lower() in boss_map:
        clean = boss_map[clean.lower()]
    return sanitize_name(clean)

def load_canonical_data():
    persona_map = {}  # safe_name -> canonical_name
    persona_files = [
        BASE_DIR / "app" / "src" / "main" / "assets" / "data" / "persona3" / "personas.json",
        BASE_DIR / "app" / "src" / "main" / "assets" / "data" / "persona3" / "portable_personas.json",
        BASE_DIR / "app" / "src" / "main" / "assets" / "data" / "persona3" / "reload_personas.json",
        BASE_DIR / "app" / "src" / "main" / "assets" / "data" / "persona4" / "personas.json",
        BASE_DIR / "app" / "src" / "main" / "assets" / "data" / "persona4" / "golden_personas.json",
        BASE_DIR / "app" / "src" / "main" / "assets" / "data" / "persona5" / "personas.json",
        BASE_DIR / "app" / "src" / "main" / "assets" / "data" / "persona5" / "royal_personas.json",
    ]
    for pf in persona_files:
        if pf.exists():
            with open(pf, 'r', encoding='utf-8-sig') as f:
                data = json.load(f)
                for name in data.keys():
                    safe = sanitize_name(name)
                    persona_map[safe] = name

    enemy_map = {}  # safe_name -> canonical_name
    enemy_files = [
        BASE_DIR / "app" / "src" / "main" / "assets" / "data" / "enemies" / "p3fes_enemies.json",
        BASE_DIR / "app" / "src" / "main" / "assets" / "data" / "enemies" / "p3p_enemies.json",
        BASE_DIR / "app" / "src" / "main" / "assets" / "data" / "enemies" / "p3r_enemies.json",
        BASE_DIR / "app" / "src" / "main" / "assets" / "data" / "enemies" / "p4_enemies.json",
        BASE_DIR / "app" / "src" / "main" / "assets" / "data" / "enemies" / "p4g_enemies.json",
        BASE_DIR / "app" / "src" / "main" / "assets" / "data" / "enemies" / "p5_enemies.json",
        BASE_DIR / "app" / "src" / "main" / "assets" / "data" / "enemies" / "p5r_enemies.json",
    ]
    for ef in enemy_files:
        if ef.exists():
            with open(ef, 'r', encoding='utf-8-sig') as f:
                data = json.load(f)
                arr = data if isinstance(data, list) else data.get("enemies", [])
                for item in arr:
                    ename = item.get("name") or item.get("enemy_name")
                    if ename:
                        safe = sanitize_enemy_name(ename)
                        enemy_map[safe] = ename

    return persona_map, enemy_map

def normalize_stem(stem: str) -> str:
    s = sanitize_name(stem)
    # Strip common noise suffixes/prefixes
    noise_patterns = [
        r'^(persona|shadow|boss|enemy)_',
        r'_(p3|p3fes|p3p|p3r|p4|p4g|p5|p5r|render|portrait|art|hd|cutin|sprite|full|thumb)$',
        r'_(png|webp|jpg|jpeg)$'
    ]
    changed = True
    while changed:
        before = s
        for pat in noise_patterns:
            s = re.sub(pat, '', s)
        changed = (s != before)
    return s

def match_target(stem: str, persona_map: dict, enemy_map: dict):
    norm = normalize_stem(stem)
    
    # 1. Exact match in personas
    if norm in persona_map:
        return "persona", norm, persona_map[norm]
    # 2. Exact match in enemies
    if norm in enemy_map:
        return "enemy", norm, enemy_map[norm]

    # 3. Fuzzy search in personas
    for safe, canonical in persona_map.items():
        if norm == safe or norm == safe.replace('-', '_') or norm.replace('-', '_') == safe:
            return "persona", safe, canonical

    # 4. Fuzzy search in enemies
    for safe, canonical in enemy_map.items():
        if norm == safe or norm == safe.replace('-', '_') or norm.replace('-', '_') == safe:
            return "enemy", safe, canonical

    return None, norm, None

def process_images():
    print("=" * 70)
    print("   PERSONA COMPANION APP -- INCOMING IMAGES PROCESSOR")
    print("=" * 70)

    if not INBOX_DIR.exists():
        INBOX_DIR.mkdir(parents=True, exist_ok=True)
    PROCESSED_DIR.mkdir(parents=True, exist_ok=True)

    persona_map, enemy_map = load_canonical_data()
    print(f"Loaded {len(persona_map)} canonical Personas and {len(enemy_map)} canonical Enemies.")

    valid_exts = {'.png', '.jpg', '.jpeg', '.webp'}
    items = [p for p in INBOX_DIR.iterdir() if p.is_file() and p.suffix.lower() in valid_exts]

    if not items:
        print(f"\nInbox is currently empty: {INBOX_DIR}")
        print("Drop .png, .jpg, or .webp files here and re-run this script.")
        print_missing_report(persona_map, enemy_map)
        return

    print(f"\nFound {len(items)} image(s) to process in {INBOX_DIR.name}/:\n")

    matched_count = 0
    unmatched = []

    for file_path in items:
        target_type, safe_name, canonical_name = match_target(file_path.stem, persona_map, enemy_map)

        if not target_type:
            print(f"[UNMATCHED] {file_path.name} -> normalized to '{safe_name}' (No matching Persona/Enemy found)")
            unmatched.append(file_path.name)
            continue

        try:
            with Image.open(file_path) as img:
                img = img.convert("RGBA")
                
                if target_type == "persona":
                    # 1. Android PNG (original high-res or max 1024)
                    app_out = APP_PERSONAS_DIR / f"{safe_name}.png"
                    img.save(app_out, format="PNG", optimize=True)
                    
                    # 2. Web WebP (max 320x320 for rapid mobile loading)
                    web_img = img.copy()
                    web_img.thumbnail((320, 320), Image.Resampling.LANCZOS)
                    web_out = WEB_PERSONAS_DIR / f"{safe_name}.webp"
                    web_img.save(web_out, format="WEBP", quality=85, method=6)
                    
                    print(f"[PERSONA] {file_path.name} -> {canonical_name} ({safe_name})")
                    print(f"          + App: {app_out.relative_to(BASE_DIR)}")
                    print(f"          + Web: {web_out.relative_to(BASE_DIR)}")
                
                else: # target_type == "enemy"
                    # 1. Android PNG
                    app_out = APP_ENEMIES_DIR / f"{safe_name}.png"
                    img.save(app_out, format="PNG", optimize=True)
                    
                    # 2. Web WebP (max 256x256)
                    web_img = img.copy()
                    web_img.thumbnail((256, 256), Image.Resampling.LANCZOS)
                    web_out = WEB_ENEMIES_DIR / f"{safe_name}.webp"
                    web_img.save(web_out, format="WEBP", quality=85, method=6)
                    
                    print(f"[ENEMY]   {file_path.name} -> {canonical_name} ({safe_name})")
                    print(f"          + App: {app_out.relative_to(BASE_DIR)}")
                    print(f"          + Web: {web_out.relative_to(BASE_DIR)}")

            # Move to processed archive
            dest = PROCESSED_DIR / file_path.name
            shutil.move(str(file_path), str(dest))
            matched_count += 1

        except Exception as e:
            print(f"[ERROR]   Failed to process {file_path.name}: {e}")

    print("\n" + "-" * 70)
    print(f"Processed & Integrated: {matched_count} images")
    if unmatched:
        print(f"Unmatched Files ({len(unmatched)}): {', '.join(unmatched)}")
    print("-" * 70)

    print_missing_report(persona_map, enemy_map)

def print_missing_report(persona_map, enemy_map):
    # Check current missing personas
    app_personas = {p.stem.lower() for p in APP_PERSONAS_DIR.glob("*.png")}
    web_personas = {p.stem.lower() for p in WEB_PERSONAS_DIR.glob("*.webp")}

    missing_personas = []
    for safe, canonical in persona_map.items():
        if safe not in app_personas or safe not in web_personas:
            missing_personas.append((canonical, safe))

    # Check current missing enemies
    app_enemies = {p.stem.lower() for p in APP_ENEMIES_DIR.glob("*.png")}
    web_enemies = {p.stem.lower() for p in WEB_ENEMIES_DIR.glob("*.webp")}

    # The 8 canonical missing enemies
    known_missing_enemy_names = [
        "Ill-fated Maya", "Tank-Form Shadow", "Craven Venoms",
        "Kunino-sagiri", "Ameno-sagiri", "Kusumi-no-Okami",
        "Abyssal King of Avarice", "Justine & Caroline"
    ]
    missing_enemies = []
    for name in known_missing_enemy_names:
        safe = sanitize_enemy_name(name)
        if safe not in app_enemies or safe not in web_enemies:
            missing_enemies.append((name, safe))

    print("\nCURRENT MISSING ARTWORK STATUS:")
    print(f"  Missing Personas ({len(missing_personas)}):")
    if missing_personas:
        for cname, safe in missing_personas:
            print(f"    - {cname} (drop as: '{safe}.png' or '{cname}.jpg')")
    else:
        print("    [100% COMPLETE] Zero missing personas!")

    print(f"\n  Missing Enemies ({len(missing_enemies)}):")
    if missing_enemies:
        for cname, safe in missing_enemies:
            print(f"    - {cname} (drop as: '{safe}.png' or '{cname}.jpg')")
    else:
        print("    [100% COMPLETE] Zero missing enemies!")
    print("=" * 70)

if __name__ == "__main__":
    process_images()
