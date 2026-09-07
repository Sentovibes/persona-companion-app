# Release Notes — v7.2.0

## Persona Companion App (Android & Web)

This major release achieves **100.0% database completion** across all 7 supported games (Persona 3 FES, Persona 3 Portable, Persona 3 Reload, Persona 4, Persona 4 Golden, Persona 5, and Persona 5 Royal), brings 100% artwork coverage to every persona and enemy in the franchise, resolves Palace and Mementos shadow locations, and launches Image Pack v2.0.

---

### Key Highlights

#### 1. 100.0% Franchise Artwork Coverage (Web & Android)
- **Personas (320 / 320 — 100.0%)**:
  - Integrated high-resolution artwork for all remaining Personas: Abaddon, Ananta, Anat, and Koropokkuru.
  - Every single Persona across Persona 3, 4, and 5 now features high-resolution artwork in the Android app (`personas_shared/`) and optimized transparent WebP avatars in the Web companion app (`docs/assets/images/personas/`).
- **Enemies & Bosses (1,503 / 1,503 entries — 100.0%)**:
  - Integrated artwork for all remaining shadows and bosses: Ill-fated Maya, Kunino-sagiri, Ameno-sagiri, Kusumi-no-Okami, Abyssal King of Avarice, Tank-Form Shadow, Craven Venoms, and Justine & Caroline.
  - 100% of enemies now render authentic sprites and models on both platforms.

#### 2. 100.0% Item & Skill Description Coverage
- **Items (8,736 / 8,736 items with descriptions — 100.0%)**:
  - Populated authentic in-game effect text, stat bonuses, unlock conditions, and drop sources for 4,481 previously blank or missing items.
  - **Persona 3 FES**: Cleaned 52 corrupted table header entries and fixed 170 items where attack power values had shifted into description fields.
  - **Persona 3 Portable**: Expanded from incomplete 293-item baseline to all 710 canonical items with Shinshoudo trading lore.
  - **Persona 5 / Royal**: Replaced all 195 `Item_002` through `Item_11F` placeholders with authentic Untouchable shop stats and added all 24 Will Seed locations.
- **Skills (3,638 / 3,638 skills with descriptions — 100.0%)**:
  - Added full in-game effect text to all 3,638 skills across all 7 games (previously 0% text descriptions).
  - Restored 142 missing skills across the series (P3 FES fusion spells, P3P additions, P5R traits and unique DLC skills like `Myriad Truths` and `Neo Cadenza`).
  - Audited and eliminated 244 hex-offset memory strings and 42 `Skill_XXX` internal placeholders.

#### 3. Canonical Palace & Mementos Locations for P5 Shadows
- **Eliminated "Location: Unknown"**: Populated authentic Palace and Mementos sections (e.g. *Kamoshida's Palace / Qimranut*, *Madarame's Palace / Chemdah*, *Kaneshiro's Palace / Kaitul*, *Maruki's Palace / Da'at*) for all 129 Persona 5 and 5 Royal shadows that previously defaulted to Unknown.

#### 4. Image Pack v2.0 Release
- **Images Pack**: `images.zip` v2.0 (1,026 MB, 930 verified image files) packaged and ready for CDN deployment at `https://github.com/Sentovibes/persona-companion-images/releases/download/v2.0/images.zip`.
- **Integrity**: Excluded legacy backups and unused test files to optimize package footprint.

---

### Release Artifacts

| Platform | Target | File | Version Code | Version Name |
|---|---|---|---|---|
| **Android Phone** | Release APK | `persona-companion-v7.2.0.apk` | `41` | `7.2.0` |
| **Android Phone** | Release AAB (Play Store) | `app-release.aab` | `41` | `7.2.0` |
| **Image Pack** | Cloud Download Pack | `images.zip` | `v2.0` | `2.0` |

---

### Verification
- **Unit & Integration Tests**: 10 / 10 automated headless Puppeteer integration tests PASSED.
- **Production Asset Validation Suite**: 14 / 14 JSON files PASSED with 0 placeholders and 0 schema errors.
- **Android Gradle Build**: Built and signed with release keystore.
