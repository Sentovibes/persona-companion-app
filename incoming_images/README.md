# Incoming Images Inbox

Drop any persona or enemy artwork into this folder.

### Supported formats
- `.png`, `.jpg`, `.jpeg`, `.webp`

### How to process
Run the automated image filter script:
```powershell
python scripts/process_incoming_images.py
```

### What the script does automatically
1. **Identifies Target**: Fuzzy-matches filenames against the official compendium and enemy databases (e.g. `abaddon_p5r.png` -> `Abaddon`, `ameno_sagiri.jpg` -> `Ameno-sagiri`).
2. **Generates App Assets**: Converts and saves the canonical PNG into `app/src/main/assets/images/personas_shared/` or `enemies_shared/`.
3. **Generates Web Assets**: Optimizes and generates transparent WebP into `docs/assets/images/personas/` or `enemies/`.
4. **Reports Status**: Prints how many previously missing images were filled and which ones remain.
