<p align="center">
  <img src="https://media0.giphy.com/media/v1.Y2lkPTc5MGI3NjExbWlpYTNqYzZra2cwc3oxM2czMjMxaXoyajY1YXkxcG45OHRkdDluOSZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/raGZMvPBIKclfNP4t0/giphy.gif" width="800" alt="Persona Series Transition" />
</p>

# Persona Companion App v7.1.0

[![Status](https://img.shields.io/badge/Status-Active-brightgreen.svg)]()
[![Version](https://img.shields.io/badge/Version-7.1.0-blue.svg)]()
[![License](https://img.shields.io/badge/License-MIT-orange.svg)](LICENSE)
[![Support](https://img.shields.io/badge/Support-Ko--fi-red.svg)](https://ko-fi.com/sentovibes)

> The ultimate, all-in-one "Absolute Perfection" reference tool for the Persona series.

The Persona Companion App is a high-performance, native Android application built with Jetpack Compose and Material 3. It serves as a comprehensive reference toolkit for mainline Persona titles, providing detailed data on Personas, Enemies, Social Links, and Classroom solutions.

---

## Supported Titles

| Game | Features | Theme |
| :--- | :--- | :--- |
| Persona 3 Reload | Full Compendium, Episode Aigis, 101 Elizabeth Requests | Indigo Blue |
| Persona 5 Royal | Confidants, Itemization, DLC, 43 Quests | Rebellion Red |
| Persona 4 Golden | Quests, Social Links, Fusion, Shuffle Time | Investigation Yellow |
| Persona 3 FES/P | Both MC Routes (Elizabeth & Theodore), 80/99 Requests | Sea Blue |

---

<p align="center">
  <img src="https://media1.tenor.com/m/zsjPmz0e7QwAAAAC/persona-5-take-your-heart.gif" width="600" alt="Take Your Heart" />
</p>

## v7.1.0: Complete Guides, All Bosses & Arcana Rank Mastery Update
This landmark update expands combat, quest, and dungeon exploration across the franchise:
- **Comprehensive Boss Prep Guides (All 7 Games)**: Expert human-grade strategies from Game8 & TheGamer covering every story boss, dungeon boss, optional boss, palace ruler, Tartarus floor guardian, and superboss (The Reaper, Margaret, Lavenza, Okumura, Nyx Avatar, Elizabeth & Theodore).
- **All Side-Quests & Velvet Room Requests**: 101 Requests in P3R, 99 in P3FES, 80 in P3P (with dynamic Theodore / Elizabeth switching when FeMC mode is active), 88 in P4G, 69 in P4, 43 in P5R, and 36 in P5.
- **Shuffle Time, Arcana Ranks & Floor Personas**: Minor Arcana breakdown by Rank (1 to 10) with exact EXP multipliers (+20% to +400%), Money bonuses, HP/SP recovery, and full skill card drop tables. Filterable directory of all Personas obtainable by floor and Tartarus block.
- **Clean Typography & Performance**: Clean professional layout with emoji-free formatting, optimized data loaders, and direct desktop Android VM launcher support.

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

### Web Experience (NEW)
- **Universal Access**: Run the companion on any device with a modern browser.
- **Responsive Design**: Supports Adaptive Rail navigation (Tablet/Desktop) and Stacked screens (Phone).
- **Quest Tracking**: Mark requests as "Completed" directly in the web UI; state is persisted via `localStorage`.
- **Offline Ready**: Local-first data architecture for fast, reliable reference.

---

## Installation

### Android APK
1. Download the latest release from the GitHub Releases tab.
2. Install the APK on an Android device (Android 8.0+ required).
3. Optional: Enable HD Image Support via the Settings menu.

---

## Web Version (Live site)

The companion now features a fully-fledged web application! This is perfect for having the compendium open on a second monitor or tablet while gaming.

### Use Online
If hosted on GitHub Pages, you can access the live web version immediately without downloading anything:
 **[Persona Companion Web](https://sentovibes.github.io/persona-companion-app/)**

### Launching Locally
1. Clone the repository or download the source code.
2. Navigate to the `web/` directory.
3. Serve the directory using a simple local server (e.g., `npx http-server` or VS Code Live Server).
4. No heavy backend required—it's a fast, local-first Single Page Application!

---

## Building and Development

```bash
# Clone the repository
git clone https://github.com/Sentovibes/persona-companion-app.git

# Build the Android APK
./gradlew assembleDebug
```

---

## Roadmap: What's Next?
- [ ] **Forward Fusion Calculator**: Select any two Personas to see the result.
- [ ] **Custom Persona Build Planner**: Save your "Ultimate Builds" to your local roster.
- [ ] **Shadow Negotiation Guide**: Upbeat? Timid? Never fail a negotiation again.
- [ ] **Daily Schedule Helper**: Track missable events and exam dates in real-time.

---

## Support the Project
If this app helped you save a Social Link or find that one rare drop, consider buying the dev a coffee!

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/sentovibes)

---

## Legal
This is a fan-made project and is **not** affiliated with, endorsed by, or connected to **Atlus** or **SEGA**. Made with care for the Persona community.
