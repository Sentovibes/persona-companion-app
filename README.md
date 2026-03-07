# Persona Companion

A native Android companion app for the Persona series — your pocket compendium for Personas across P3, P4, and P5.

> Built with Kotlin · Jetpack Compose · Material 3

---

## Features

- **Complete Persona Database** - Browse all Personas from P3 FES, P3 Portable, P3 Reload, P4, P4 Golden, P5, and P5 Royal
- **Detailed Stats & Skills** - View base stats, skill lists, elemental affinities, and more
- **Smart Search** - Search by Persona name or Arcana
- **Flexible Sorting** - Sort by Arcana (grouped), Level, or Name
- **Content Filters** - Toggle DLC and Episode Aigis personas on/off in settings
- **Beautiful UI** - Material 3 design with series-specific color themes
- **Offline First** - All data stored locally, no internet required

---

## Supported Games

| Game | Personas | Status |
|---|---|---|
| Persona 3 FES | 96 | ✅ Complete |
| Persona 3 Portable | 194 | ✅ Complete |
| Persona 3 Reload | 194 | ✅ Complete |
| Persona 4 | 187 | ✅ Complete |
| Persona 4 Golden | 205 | ✅ Complete |
| Persona 5 | 192 | ✅ Complete |
| Persona 5 Royal | 225 | ✅ Complete |

---

## Screenshots

[Coming soon]

---

## Download

Get the latest APK from the [Releases](../../releases) page.

**Requirements:**
- Android 8.0 (API 26) or higher
- ~10 MB storage space

---

## Project Structure

```
persona-companion-app/
├── app/
│   └── src/main/
│       ├── java/com/persona/companion/
│       │   ├── data/            # Repository + SeriesData catalogue
│       │   ├── models/          # Persona, Game, PersonaSeries models
│       │   ├── navigation/      # Navigation graph
│       │   ├── ui/
│       │   │   ├── screens/     # UI screens (Home, List, Detail, etc.)
│       │   │   ├── theme/       # Material 3 theming
│       │   │   └── viewmodels/  # State management
│       │   └── utils/           # JSON loader utility
│       └── assets/data/         # Persona JSON files
│           ├── persona3/
│           ├── persona4/
│           └── persona5/
```

---

## Building from Source

### Prerequisites
- JDK 17 or higher
- Android SDK (API 34)
- Gradle 8.2+

### Steps

1. Clone the repository:
```bash
git clone https://github.com/yourusername/persona-companion-app.git
cd persona-companion-app
```

2. Build the APK:
```bash
# Windows
gradlew.bat assembleDebug

# Linux/Mac
./gradlew assembleDebug
```

3. Find the APK at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## Contributing

### Adding or Updating Persona Data

All Persona data is stored in JSON files under `app/src/main/assets/data/`.

**Map Format** (P3 FES, P3P, P4, P4G, P5, P5R):
```json
{
  "Orpheus": {
    "race": "Fool",
    "lvl": 1,
    "stats": [3, 2, 2, 3, 2],
    "skills": {
      "Bash": 0.1,
      "Agi": 0.2,
      "Tarukaja": 3
    },
    "resists": "----w-s---",
    "inherits": "fire"
  }
}
```

**Array Format** (P3 Reload):
```json
[
  {
    "name": "Orpheus",
    "arcana": "Fool",
    "level": 1,
    "strength": 3,
    "magic": 2,
    "endurance": 2,
    "agility": 3,
    "luck": 2,
    "weak": ["Electric", "Dark"],
    "resists": ["Fire"]
  }
]
```

### Adding a New Game

1. Add the JSON file to `app/src/main/assets/data/<series>/`
2. Update `SeriesData.kt` to add a new `Game` entry
3. The app will automatically pick it up!

---

## Roadmap

- [ ] Fusion calculator
- [ ] Social Links / Confidants guide
- [ ] Classroom answers database
- [ ] Boss strategies
- [ ] Enemy compendium
- [ ] Dark mode toggle
- [ ] Export/import favorites

---

## Data Sources

Persona data compiled from:
- [Megami Tensei Wiki](https://megamitensei.fandom.com/)
- [Aqiu's Persona Fusion Calculator](https://aqiu384.github.io/megaten-fusion-tool/)
- Community contributions

This is a fan project with no affiliation to Atlus or SEGA.

---

## License

MIT License - see [LICENSE](LICENSE) for details.

---

## Changelog

### v1.5.0 (2026-03-07)
- Added Settings screen
- Added DLC persona filtering (toggle on/off)
- Added Episode Aigis filtering for P3 games
- Added 18 DLC personas to P5 Royal (225 total)
- Improved persona filtering system
- Settings accessible from home screen

### v1.0.0 (2026-03-07)
- Initial release
- Complete Persona database for 7 games
- Search and sort functionality
- Material 3 UI with series-specific themes
- Offline-first architecture
