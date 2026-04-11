<p align="center">
  <img src="https://media0.giphy.com/media/v1.Y2lkPTc5MGI3NjExbWlpYTNqYzZra2cwc3oxM2czMjMxaXoyajY1YXkxcG45OHRkdDluOSZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/raGZMvPBIKclfNP4t0/giphy.gif" width="800" alt="Persona Series Transition" />
</p>

# Persona Companion App v6.0.0

[![Status](https://img.shields.io/badge/Status-Active-brightgreen.svg)]()
[![Version](https://img.shields.io/badge/Version-6.0.0--blue.svg)]()
[![License](https://img.shields.io/badge/License-MIT-orange.svg)](LICENSE)
[![Support](https://img.shields.io/badge/Support-Ko--fi-red.svg)](https://ko-fi.com/sentovibes)

> The ultimate, all-in-one "Absolute Perfection" reference tool for the Persona series.

The Persona Companion App is a high-performance, native Android application built with Jetpack Compose and Material 3. It serves as a comprehensive reference toolkit for mainline Persona titles, providing detailed data on Personas, Enemies, Social Links, and Classroom solutions.

---

## Supported Titles

| Game | Features | Theme |
| :--- | :--- | :--- |
| Persona 3 Reload | Full Compendium, Episode Aigis | Indigo Blue |
| Persona 5 Royal | Confidants, Itemization, DLC | Rebellion Red |
| Persona 4 Golden | Quests, Social Links, Fusion | Investigation Yellow |
| Persona 3 FES/P | Both MC Routes, Requests | Sea Blue |

---

<p align="center">
  <img src="https://media1.tenor.com/m/zsjPmz0e7QwAAAAC/persona-5-take-your-heart.gif" width="600" alt="Take Your Heart" />
</p>

## New in v6.0.0: The Item Compendium Update
This update introduces a standardized universal format for all item databases.
- 5,000+ Items standardized across the series.
- Full mapping for P5R (1,873 items), P3R (1,229), P4G (1,196), and P3F/P (690).
- Fixed categorization for weapons, protectors, and consumables.

---

## Core Features

### Multi-Device Support
- Phone Mode: Touch-optimized, compact layouts for mobile use.
- Tablet Mode: Dual-pane navigation for large-screen efficiency.
- Android TV: D-pad optimized navigation with scaled text and visuals.
- Cast Mode: Direct data streaming to compatible TV devices via Chromecast.

### Comprehensive Databases
- Persona Compendium: Full stats, skillsets, and fusion recipes.
- Enemy Bestiary: Weaknesses, resistances, and standard/rare drop data.
- Social Link Guides: Optimized dialogue choices and rank-up requirements.
- Classroom Answers: Solutions for all supported titles and exam dates.

### Quality of Life
- Favorites and History: Save frequently used entries for instant access.
- OLED Dark Mode: Battery-optimized true black theme for night gaming.
- Smart Filtering: Advanced search by Arcana, Level, DLC, or Element.
- Automatic Updates: Built-in checker to stay current with the latest data.

---

## Installation

### Android APK
1. Download the latest release from the GitHub Releases tab.
2. Install the APK on an Android device (Android 8.0+ required).
3. Optional: Enable HD Image Support via the Settings menu.

### HD Image Setup
The app supports a high-quality 1.3GB image pack for full profile pictures and image viewing.
1. Download the pack directly from Settings > HD Images.
2. Alternatively, import a local zip file from the persona-companion-images repository.

---

## Building from Source

```bash
# Clone the project
git clone https://github.com/Sentovibes/persona-companion-app.git

# Build the APK
./gradlew assembleDebug
```

---

## Future Roadmap
- [ ] Forward Fusion Calculator: Select any two Personas to determine the result.
- [ ] Custom Build Planner: Save ultimate skill configurations to local storage.
- [ ] Shadow Negotiation Guide: Decision-making assistance for Persona 5 interactions.
- [ ] Daily Schedule Tracker: Calendar-based reminders for missable events.

---

## Support and Legal
This is a fan-made project and is not affiliated with, endorsed by, or connected to Atlus or SEGA.

If this project helped your playthrough, consider supporting the continuous development:

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/sentovibes)

## Building and Development

```bash
# Clone the repository
git clone https://github.com/Sentovibes/persona-companion-app.git

# Build the core app
./gradlew assembleDebug
```

### Data Pipeline
We use a custom Python pipeline (`tmp/convert_to_app_format.py`) to keep our master JSON records in the root synchronized with the Android assets.

---

## 🗺️ Roadmap: What's Next?
- [ ] **Forward Fusion Calculator**: Select any two Personas to see the result.
- [ ] **Custom Persona Build Planner**: Save your "Ultimate Builds" to your local roster.
- [ ] **Shadow Negotiation Guide**: Upbeat? Timid? Never fail a negotiation again.
- [ ] **Daily Schedule Helper**: Track missable events and exam dates in real-time.

---

## 💖 Support the Project
If this app helped you save a Social Link or find that one rare drop, consider buying the dev a coffee!

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/sentovibes)

---

## ⚖️ Legal
This is a fan-made project and is **not** affiliated with, endorsed by, or connected to **Atlus** or **SEGA**.

Made with care for the Persona community. 🃏
