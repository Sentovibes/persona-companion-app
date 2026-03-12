# Persona Companion App

A comprehensive companion and reference app for the mainline Persona series. Version 5.0 marks the final major feature release, providing complete databases for personas, enemies, social links, confidants, and classroom answers.

## Supported Games

- **Persona 3 FES** - Full compendium, enemies, social links, and classroom answers
- **Persona 3 Portable** - Male MC & FeMC social links, personas, enemies, and classroom answers
- **Persona 3 Reload** - Complete compendium, including Episode Aigis DLC
- **Persona 4 Golden** - Full compendium, enemies, social links, and classroom answers
- **Persona 5 / Persona 5 Royal** - Complete compendium, enemies, confidants, and classroom answers

## Features

### Multi-Device Support

- **Phone Mode** - Touch-optimized interface with compact layouts
- **Tablet Mode** - Dual-pane layouts to take advantage of larger screens
- **Android TV** - D-pad navigation with scaled text and images
- **Cast Mode** - Stream data directly to your TV via Chromecast

### Browse & Search

- Full persona compendium with stats, skills, and fusion recipes
- Enemy database detailing weaknesses, resistances, and drop items
- Social Links and Confidants with complete dialogue choices and point values
- Classroom answers for all supported titles with correct solutions
- Advanced filtering capabilities (game, arcana, level, DLC, Episode Aigis)

### HD Images (Optional)

The app is fully functional offline without images, but supports high-quality assets if you want them. Image spaces automatically hide if the pack isn't installed.

- Download the 1.3GB image pack directly from Settings
- Alternatively, import a local images.zip file
- Includes persona/enemy profile pictures and a full-size image viewer on mobile

### Quality of Life

- **Favorites & History** - Save frequently used personas/enemies and access recently viewed items
- **Smart Filtering** - Toggle DLC personas, Episode Aigis content, and P3P protagonist routes
- **Share Functionality** - Share personas, enemies, social links, and confidants with full details including boss phases
- **OLED Dark Theme** - Built-in dark mode optimized for battery saving on OLED screens
- **Automatic Updates** - Built-in update checker to stay current with new releases

## Installation

1. Download the latest APK from the [Releases](https://github.com/Sentovibes/persona-companion-app/releases) tab
2. Install on your Android device (requires Android 8.0+)
3. Optionally download HD images from Settings

## HD Images Setup

If you want the visual assets, you have two options:

### Option 1: In-App Download

1. Open **Settings > HD Images**
2. Tap **Download** (Note: this is a 1.3GB file)
3. Wait for the extraction to complete

### Option 2: Import Local File

1. Download `images.zip` from [persona-companion-images](https://github.com/Sentovibes/persona-companion-images/releases)
2. Open **Settings > HD Images** in the app
3. Tap **Import** and select the downloaded ZIP file

## Building from Source

```bash
# Clone the repository
git clone https://github.com/Sentovibes/persona-companion-app.git
cd persona-companion-app

# Build debug APK (includes images)
./gradlew assembleDebug

# Build release APK (no images)
./gradlew assembleRelease
```


## Credits

- Data sourced from the [Megaten Fusion Tool](https://github.com/aqiu384/megaten-fusion-tool)
- Images compiled from various Persona wikis and community sources
- Built natively with Jetpack Compose and Material 3

## Support

If you found this app useful during your playthroughs, consider supporting the project:

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/sentovibes)

## License & Legal

This is a fan-made project and is not affiliated with, endorsed by, or connected to Atlus or SEGA.

Released under the MIT License. See the [LICENSE](LICENSE) file for details.

## Bugs & Feedback

If you run into crashes or data inaccuracies, please [open an issue](https://github.com/Sentovibes/persona-companion-app/issues).

---

Made with care for Persona fans
