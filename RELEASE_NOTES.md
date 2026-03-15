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
