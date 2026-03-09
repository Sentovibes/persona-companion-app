# Persona Companion App - Version 4.0.0 Release Notes

## 🎨 Major Feature: Image Support (BETA)

Version 4.0.0 introduces image support for personas and enemies! This is a **BETA feature** with known limitations.

### What's New

#### Profile Images
- Persona and enemy images now display throughout the app
- Adaptive display based on device:
  - **Phone:** Small circular profile picture (64dp)
  - **Tablet:** Full-size rectangular image on right side of screen (80dp thumbnail in lists)
  - **TV:** Large rectangular image filling right panel (120dp thumbnail in lists)
- Tap any image to view full-size
- Works offline - all images stored in app

#### Cast Support for Images
- Images now broadcast to TV browsers via Cast
- Base64 encoding for seamless transmission
- Displays in right panel of 2x2 grid layout

#### Image Coverage

**Personas:**
- P3 FES: 169/169 (100.0%) - **✓ COMPLETE!**
- P3 Portable: 173/173 (100.0%) - **✓ COMPLETE!**
- P3 Reload: 194/194 (100.0%) - **✓ COMPLETE!**
- P4: 187/187 (100.0%) - **✓ COMPLETE!**
- P4 Golden: 205/205 (100.0%) - **✓ COMPLETE!**
- P5: 199/210 (94.8%) - **11 missing**
- P5 Royal: 218/232 (94.0%) - **14 missing**

**Total Personas: 1345/1370 (98.2%) - 25 missing**

**Enemies:**
- P3 FES: 230/368 (62.5%) - **138 missing**
- P3 Portable: 112/176 (63.6%) - **64 missing**
- P3 Reload: 193/496 (38.9%) - **303 missing**
- P4: 200/271 (73.8%) - **71 missing**
- P4 Golden: 220/297 (74.1%) - **77 missing**
- P5: 29/149 (19.5%) - **120 missing**
- P5 Royal: 30/168 (17.9%) - **138 missing**

**Total Enemies: 1014/1925 (52.7%) - 911 missing**

**GRAND TOTAL: 2359/3295 images (71.6%) - 936 missing**

### ⚠️ Known Issues (BETA)

1. **Some images still missing** - 98.2% persona coverage (all P3 & P4 games complete!), 52.7% enemy coverage
2. **Some images may be incorrect** - Automated scraping may have grabbed wrong images
3. **Quality varies** - Some images are from older games (SMT series) when Persona versions aren't available
4. **No P5 Strikers images** - P5S enemies not yet scraped
5. **Fallback icon** - Missing images show a generic person icon

### How It Works

- Images load from app assets (no internet required)
- Phone: Circular profile picture next to name
- Tablet/TV: Full rectangular image filling right side of screen
- Tap to expand to full screen
- Missing images show placeholder icon
- Works on all devices (phone, tablet, TV, cast)

### Performance Notes

- Images are 256px (compressed for quality vs size balance)
- Loaded on-demand (lazy loading)
- App size: 44MB release / 50MB debug
- No performance impact on devices without images

## Technical Details

### New Components
- `ImageUtils.kt` - Image loading utilities
- `ProfileImage.kt` - Reusable profile picture component
- 542 unique PNG images (256x256px) in assets
- Base64 encoding for Cast transmission

### Updated Screens
- `EnemyDetailScreen.kt` - Adaptive layout with images
- `PersonaDetailScreen.kt` - Adaptive layout with images
- `CastServer.kt` - Image broadcasting support
- Cast HTML receiver - Image display panel

### Adaptive Layouts
- Phone: Vertical scrolling with small circular images
- Tablet: Two-column (60/40 split) with full image on right
- TV: Two-column (33/67 split) with large image on right
- Cast: 2x2 grid with image panel

## Future Improvements (v4.1+)

### Planned for v4.1
- Download remaining personas (25 missing):
  - P5/P5R treasure demons (Regent, Orlov, Hope Diamond, etc.)
  - P5/P5R DLC personas (Tsukiyomi, Cait Sith, etc.)
- Target: 99%+ persona coverage

### Planned for v4.2
- Image feedback system:
  - "Report Wrong Image" button
  - "Submit Missing Image" feature
  - GitHub issue integration
- Verify and correct wrong images

### Future Enhancements
- Add thumbnail images to list views
- Compress images further for better performance
- Add P5 Strikers enemy images
- Manual quality check of all images
- Community contribution system

## Version Info

- **Version Code:** 11
- **Version Name:** 4.0.0
- **Release Date:** March 9, 2026
- **Feature Status:** BETA
- **APK Size:** 50 MB (estimated)
- **Image Count:** 593 unique images (306 personas + 287 enemies)
- **Image Coverage:** 71.6% overall (98.2% personas, 52.7% enemies)

## Feedback

This is a beta feature. If you notice:
- Wrong images for personas/enemies
- Missing images that should exist
- Performance issues
- Display bugs
- Image quality problems

Please report them via GitHub Issues so we can improve the image database in future updates!

## Installation

Download the APK from the releases page:
- `persona-companion-v4.0.0.apk` (44.15 MB - Release build)

**Installation Instructions:**
1. Download the APK file
2. Enable "Install from Unknown Sources" in your Android settings
3. Open the APK file to install
4. Grant any requested permissions

**Note:** This app is not available on Google Play Store. It's distributed via GitHub for sideloading.

---

**Note:** Images were scraped from the Megami Tensei Wiki using automated tools. Some images may be incorrect or from non-Persona games. We're working to improve accuracy in future updates. This is a BETA release - expect missing and potentially incorrect images.
