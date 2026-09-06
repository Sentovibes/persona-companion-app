# Release Notes - v7.1.5

## Wear OS Companion Release (Watch Only)
Initial standalone Wear OS companion release for Wear OS 4 and 5 smartwatches:
- Instant wrist lookup for Persona 3, Persona 4, and Persona 5 series games.
- Classroom & Exam cheat sheet with quick date navigation.
- Enemy weakness calculator with Normal, Mini-Boss, and Boss category tabs.
- Social Link and Confidant optimal dialogue choices.
- Hardware-optimized rotary touch bezel and digital crown scrolling support.
- Pure black high-contrast OLED theme for minimal battery consumption.

## Android Phone Release
- Synchronized version bump to v7.1.5 (versionCode 37) with native crash debug symbols.

---

# Release Notes - v7.1.4

## Fusion Calculator Precision Fix (Android & Web)
- **Resolved Odd-Sum Fusion Discrepancy**: Corrected cross-arcana boundary calculation on both Web (`web/app.js` and `docs/app.js`) and Android (`FusionCalculator.kt`). Previously, odd-level sums (e.g. Pixie Lv.2 + Sandman Lv.5 = 7) were mistakenly mapped to higher-tier personas (such as Archangel) instead of their canonical lower-tier persona (such as Angel) due to off-by-one boundary math on web and floating-point rounding without truncation in Android forward fusion.
- **Prevented Self-Ingredient Suggestions**: Excluded target personas from being suggested as ingredients to fuse themselves in edge-case chart combinations (e.g. Moon + Empress = Moon).
- **Comprehensive Compendium Verification**: Validated 100% consistency across all 117,629 recipe combinations in all 7 supported games (P3FES, P3P, P3R, P4, P4G, P5, P5R).

---

# Release Notes - v7.1.3

## Bug Fix Release - 16 KB Alignment & Fusion Upgrades

### 16 KB Page-Size Memory Alignment & Android 15 Compatibility
- **Eliminated 16 KB Device Crash Warning**: Completely resolved Google Play Console warning regarding `libtensorflowlite_jni.so` and `libimage_processing_util_jni.so`.
- **DEX-Only Pure Architecture**: Removed unused legacy TFLite and CameraX dependencies, stripping all unaligned `.so` native libraries. The app bundle is now 100% architecture-independent and fully certified for Android 15 devices with 16 KB memory page sizes.
- **Smaller Footprint**: Significantly reduced the final APK/AAB bundle size.

### Fusion Calculator: Cost Sorting, Ingredient Filter & Izanagi Saturation Fix
- **Cheapest Option by Default**: Fusion recipes (both standard 2-way and 3-way triangle fusions) are now sorted by **Total Summoning Cost (Cheapest First)** by default, matching aqiu384 and compendium mechanics.
- **Cheapest Option Badge**: The most economical recipe is prominently highlighted with a distinct `★ CHEAPEST OPTION` badge and clear Yen (`¥`) cost summary.
- **Interactive Sort Options**: Added real-time sort chips: *Cheapest First*, *Highest Cost*, *Lowest Level*, and *Highest Level*.
- **Live Ingredient Search & Filtering**: Added an ingredient filter search bar to quickly find recipes containing specific Personas in inventory or filter out unwanted ingredients.
- **Izanagi Saturation Resolved**: Fixed the triangle fusion recipe generation where Izanagi (Level 1) previously saturated the first dozens of recipes due to raw array iteration order. Both Android and Web/PWA now use pruned, cost-sorted evaluation to display diverse, cost-effective recipe options.

### Margaret Fusion Guide Correction (P4 & P4G)
- **Rank 3 Request Correction**: Corrected Margaret's Empress Rank 3 requirement from the erroneous *Gdon with Beast Weaver* to the canonical *Gdon with Rampage*.
- **Fusion Walkthrough Verified**: Updated walkthrough instructions to fuse Ares (Chariot) with Shiisaa (Hierophant), inheriting Rampage.

---

**Full Changelog**: https://github.com/Sentovibes/persona-companion-app/compare/v7.1.1...v7.1.3

---

# Release Notes - v7.1.0

## Major Content & Combat Mastery Release

### Comprehensive Boss Prep Guides (All 7 Games)
- Complete, Game8/TheGamer human-grade strategies for all story bosses, dungeon bosses, palace rulers, Tartarus floor guardians, full moon operations, optional bosses, and superbosses:
  - **Persona 4 Golden & P4**: The Reaper (21-chest rattle spawn trick, Makarakarn trigger prevention, Debilitate + Hassou Tobi setup), Margaret (50-turn enrage limit, 8-element cycle, banned Omnipotent Orb rule), Marie / Kusumi-no-Okami (Hollow Forest SP drain and Breaker item mechanics), Ameno-sagiri, Adachi, Contrarian King, and all dungeon guardians.
  - **Persona 5 Royal & P5**: Lavenza (4 strict phase checks: Elemental, Technical, Critical, and DPS race), Okumura (5-wave simultaneous kill & Baton Pass tactics), Caroline & Justine, Jose, Yaldabaoth, Maruki / Azathoth / Adam Kadmon, and all Palace Rulers.
  - **Persona 3 Reload, P3FES & P3P**: Nyx Avatar (14 phase shifts), Elizabeth & Theodore (8-turn element rotations, 9999 Megidolaon trigger avoidance, Enduring Soul survival), Vision Quest Margaret, and all Tartarus Block guardians.

### Velvet Room Requests & Side-Quests
- Added all 101 Elizabeth Requests for **Persona 3 Reload** (from #1 to #101 with complete requirements, item locations, and Omnipotent Orb rewards).
- Added all 99 Requests for **Persona 3 FES**.
- Added all 80 Requests for **Persona 3 Portable** with full dynamic protagonist switching:
  - Toggling **FeMC Mode** in Settings seamlessly switches the guide to **Theodore's Requests** with customized dates, dialogue, and Monad 10F Theodore fight requirements.
- Full side-quest databases for P4G (88 quests), P4 (69 quests), P5R (43 quests), and P5 (36 quests).

### Shuffle Time, Arcana Ranks & Floor Personas
- **Minor Arcana by Rank (Ranks 1 to 10)**:
  - Interactive selector displaying exact bonus EXP multipliers (+20% up to +400% Quadruple EXP at Rank 10/King).
  - Exact Money / Yen earnings and HP/SP recovery percentages.
  - Full skill card catalog per card rank (from early tier elements like Agi/Bufu up to endgame skills like Victory Cry, Severe 4th-tier Dynes, and Morning Star).
- **Personas by Floor / Tartarus Block**:
  - Filterable directory showing all obtainable Personas, Arcana types, levels, and floor appearances across all Tartarus blocks and TV World dungeons.
- **Major Arcana & Burst**:
  - All 22 Major Arcana tarot cards with effect breakdowns and Arcana Burst perks.
- Clean typography and professional styling with emojis removed across all guides.

### Emulator & Tooling Improvements
- Added one-click desktop VM launcher (`run_vm.bat`) with Direct3D11 ANGLE rendering (`-gpu angle_indirect`) and clean snapshot handling (`-no-snapshot-load`) for RTX 50-series GPUs.

---

# Release Notes - v7.0.0

## The Negotiation & Master Build Solver Update
- Shadow Negotiation Guide with 4 personality types (Upbeat, Timid, Gloomy, Irritable), Sun Confidant perks, and live database lookup.
- Persona Skill Inheritance Route Finder with automated fusion tree solver.
- Character names prominently displayed on Social Link cards.
- Day-by-Day Calendar walkthroughs.

---

# Release Notes - v5.1.0

## Bug Fix Release

### Fusion Calculator
- Fixed fusion accuracy across all 7 games — P3FES, P3P, P3R, P4, P4G, P5, P5R all verified against megaten-fusion-tool reference data
- Fixed asymmetric fission table bug causing some recipes to be missed
- Fixed same-arcana fusion logic
- Fixed triangular chart handling for P3R, P5, P5R
- Fixed element demon exclusion from ingredient and result pools
- Added missing DLC personas for P5 (18) and P5R (25)
- Added element demon entries to P5 and P5R special fusion data
- Fixed P5 base fusion chart (was incorrectly using P5R chart)
- P4G: 7 Hanged-arcana personas show more recipes than reference tool — confirmed as a data bug in the reference tool, our data is correct

### Persona & Enemy Lists
- Fixed name sort — clicking Name sort now works on first tap (was requiring two taps)
- Fixed level sort — same double-tap issue resolved
- Fixed persona image in list rows using wrong game ID (was hardcoded to P5)

### Persona & Enemy Detail Screen
- Fixed images not showing in detail screen after downloading image pack
- Detail screen now uses the same Coil image loader as the list (consistent behaviour)
- Fixed image loading running on wrong thread (could silently fail)

### Always Dark Theme
- Removed light mode — app always uses dark theme regardless of system setting

---

**Full Changelog**: https://github.com/Sentovibes/persona-companion-app/compare/v5.0.0...v5.1.0

---

# Release Notes - v5.0.0

## Major Release - Social Links, Confidants & Classroom Answers

### New Features

**Social Links & Confidants**
- Complete social link guides for all Persona games
- Full dialogue choices with point values for optimal progression
- P3P protagonist selection (Male MC or FeMC routes)
- Phone call choices marked with phone icon
- Requirements, locations, and availability information
- Share social links with friends

**Classroom Answers**
- Complete classroom answer database for P3, P4, and P5
- Correct answers for all exam questions
- Organized by date for easy reference
- No more failing exams!

**HD Image System**
- Optional 1.3GB HD image pack with 1500+ persona and enemy images
- Download directly from app or import local ZIP file
- Images automatically hidden when not downloaded (no placeholders)
- Profile pictures in phone mode, full-size images in tablet/TV mode
- Fixed image loading across all screens

**Enhanced Sharing**
- Share personas and enemies with full stats
- Share social links with dialogue choices and point values
- Share boss phases and multi-part bosses
- Share classroom answers

### Bug Fixes
- Fixed P5/P5R classroom answers (removed incorrect exclusive flags)
- Fixed social link data parsing for all games
- Improved ZIP extraction with proper path handling for Windows-created archives
- Fixed state persistence for downloaded images
- Fixed image visibility logic

### UI Improvements
- Better error handling and user feedback
- Cleaner settings screen with organized sections
- Improved image import flow with progress tracking

### Technical Improvements
- Migrated to cloud-based image delivery system
- Optimized APK size (13MB without images)
- Improved performance and memory usage
- Better error logging and diagnostics
- Synchronous SharedPreferences commits for reliability

---

**Full Changelog**: https://github.com/Sentovibes/persona-companion-app/compare/v4.0.0...v5.0.0
