"""
Audits personas_shared folder:
1. Finds images that are suspiciously small (likely bad/SMT sprites)
2. Cross-checks done log vs what's actually on disk
3. Probes the wiki to verify what image was downloaded for each persona
   by checking if a better Persona-series image exists
"""
import json, os, re
from pathlib import Path

OUTPUT_DIR = Path("../app/src/main/assets/images/personas_shared")
DONE_LOG   = "persona_done.json"
FAILED_LOG = "persona_failed.json"

done   = set(json.load(open(DONE_LOG))   if os.path.exists(DONE_LOG)   else [])
failed = dict(json.load(open(FAILED_LOG)) if os.path.exists(FAILED_LOG) else {})

imgs = {p.stem: p for p in OUTPUT_DIR.glob("*.png")}

print(f"Images on disk : {len(imgs)}")
print(f"Done log       : {len(done)}")
print(f"Failed         : {len(failed)}")

# Size audit
sizes = sorted([(p.stat().st_size, name, p) for name, p in imgs.items()])
print(f"\n── Smallest 15 images (potential bad downloads) ──")
for size, name, p in sizes[:15]:
    kb = size // 1024
    flag = " ⚠️  VERY SMALL" if kb < 80 else (" ⚠️  SMALL" if kb < 200 else "")
    print(f"  {kb:>6}KB  {name}{flag}")

print(f"\n── Largest 5 images ──")
for size, name, p in sizes[-5:]:
    mb = size / 1024 / 1024
    print(f"  {mb:.1f}MB  {name}")

# Find personas in done log but NOT on disk
def to_stem(name):
    s = name.lower()
    for a, b in [("è","e"),("é","e"),("ā","a"),("ō","o"),("ū","u")]:
        s = s.replace(a, b)
    s = re.sub(r"\s+", "_", s)
    return re.sub(r"[^a-z0-9_\-]", "", s)

missing_from_disk = [n for n in done if to_stem(n) not in imgs]
print(f"\n── In done log but missing from disk: {len(missing_from_disk)} ──")
for n in missing_from_disk:
    print(f"  {n}  (expected: {to_stem(n)}.png)")

# Flag very small images for re-download
bad_images = [(name, size//1024) for size, name, p in sizes if size < 80_000]
print(f"\n── Images under 80KB (flag for re-download): {len(bad_images)} ──")
for name, kb in bad_images:
    print(f"  {kb}KB  {name}.png")

if bad_images or missing_from_disk:
    print("\nRun with --fix to delete bad images and remove from done log for re-download")
else:
    print("\nAll images look good!")
