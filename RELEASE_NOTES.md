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
