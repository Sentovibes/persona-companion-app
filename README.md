<p align="center">
  <img src="https://media0.giphy.com/media/v1.Y2lkPTc5MGI3NjExbWlpYTNqYzZra2cwc3oxM2czMjMxaXoyajY1YXkxcG45OHRkdDluOSZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/raGZMvPBIKclfNP4t0/giphy.gif" width="800" alt="Persona Series Transition" />
</p>

# Persona Companion App v8.1

[![Status](https://img.shields.io/badge/Status-Active-brightgreen.svg)]()
[![Google Play](https://img.shields.io/badge/Google_Play-Persona_Companion-green?logo=google-play&logoColor=white)](https://play.google.com/store/apps/details?id=com.persona.companion)
[![Version](https://img.shields.io/badge/Version-8.1-blue.svg)]()
[![License](https://img.shields.io/badge/License-MIT-orange.svg)](LICENSE)
[![Support](https://img.shields.io/badge/Support-Ko--fi-red.svg)](https://ko-fi.com/sentovibes)

> The comprehensive reference companion and fusion calculator for the Persona series.

The Persona Companion App is a high-performance, native Android application built with Jetpack Compose and Material 3, alongside a companion Web PWA. It serves as an authoritative reference toolkit for mainline Persona titles, providing detailed data on Personas, Enemies, Social Links, Quests, Items, Skills, and Classroom solutions.

---

## Supported Titles

| Game | Features | Theme Accent |
| :--- | :--- | :--- |
| Persona 3 Reload | Full Compendium, Episode Aigis, 101 Elizabeth Requests, Monad Doors | Indigo Blue |
| Persona 5 Royal | Confidants, Itemization, DLC, 43 Quests, Palace Strategies | Rebellion Red |
| Persona 4 Golden | Quests, Social Links, Fusion, Shuffle Time, Classroom | Investigation Yellow |
| Persona 3 FES / Portable | Both MC Routes (Elizabeth & Theodore), 80/99 Requests | Sea Blue |

---

<p align="center">
  <img src="https://media1.tenor.com/m/zsjPmz0e7QwAAAAC/persona-5-take-your-heart.gif" width="600" alt="Take Your Heart" />
</p>

## v8.1: Built-in WebP Artworks, Pinned Confidants & Universal Backup

- **Built-in WebP Compendium**: Migrated all 962 Persona & Enemy visuals to a compact 14.1 MB WebP catalog bundled directly offline inside the app (zero external 1 GB download required).
- **Pinned & Maxed Confidants**: Pin active Social Links and Confidants to the top for instant hangout answers, and mark completed ones as Maxed across Android and Web PWA.
- **Universal JSON Backup & Restore**: Export and import favorites, completed quests, pinned/maxed confidants, and settings seamlessly between Android and the Web PWA.
- **Skill Inheritance Rules**: Inspect incompatible affinities and inherited skill slot formulas directly on Persona detail cards.
- **UI & Android 15 Hardening**: Refreshed tactile dark-mode card styling, non-destructive Room migrations, foreground Cast service compliance, and full Android 15 edge-to-edge system insets.

---

## v8.0: Global Search, No Spoilers Mode & 100% Franchise Bestiary

The v8.0 milestone introduces major utility, visual completion, and privacy tools:

- **Unified Global Search**: Instant cross-database search across Personas, Skills, Enemies, Bosses, Quests, Items, Social Links, and Guides from any screen with live debounced indexing.
- **No Spoilers Mode**: Social Link optimal dialogue answers and late-game boss encounters are concealed behind tap-to-reveal toggles.
- **100% Franchise Bestiary Visuals (874 / 874 Enemies)**: Full artwork coverage across all 874 enemies and 320 Personas in Persona 3 Reload, Persona 4 Golden, and Persona 5 Royal, including Episode Aigis superbosses, Monad passages, and authentic boss fallbacks.
- **Preferred Game Launchers**: Configure your default game per series or select "None" to choose freely on each session.
- **In-App What's New Dialog**: Interactive popup on first launch highlighting new features, accessible anytime from Settings.
- **Android 15 Certified**: 100% compliant with 16 KB native memory page sizes and low-latency navigation.

---

## Core Features

### Multi-Device Support
- Phone Mode: Touch-optimized, compact layouts for mobile use.
- Tablet Mode: Dual-pane navigation for large-screen efficiency.
- Android TV: D-pad optimized navigation with scaled text and visuals.
- Cast Mode: Direct data streaming to compatible TV displays.

### Comprehensive Databases
- Persona Compendium: Full base stats, skillsets, and cheapest fusion recipes sorted by Yen (¥) summoning costs.
- Enemy Bestiary: Elemental weaknesses, resistances, locations, and drop data.
- Social Link Guides: Best dialogue choices and rank-up schedules.
- Classroom Answers: Verified answers for all supported games and exam dates.

### Quality of Life
- Favorites and History: Quick-access pins for Personas and Shadows.
- OLED Dark Mode: True midnight black theme for night gaming sessions.
- Smart Filtering: Search and filter by Arcana, Level, DLC, or Element.
- In-App Update Checker: Automatic notifications when new data or app builds release.

### Web Experience
- **Universal Access**: Run the companion on any desktop or mobile browser.
- **Responsive Layout**: Supports adaptive rail navigation and compact views.
- **Quest Tracking**: Mark requests as completed directly in the web UI with local persistence.
- **Offline Ready**: Local-first data architecture for fast, reliable reference.

---

## Installation

### Google Play Store (Official)
The app is available on the Google Play Store:

[![Get it on Google Play](https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png)](https://play.google.com/store/apps/details?id=com.persona.companion)

Or open the store page directly: **[Persona Companion on Google Play](https://play.google.com/store/apps/details?id=com.persona.companion)**

### Android APK (Direct Download)
1. Download the latest release APK from the GitHub Releases tab.
2. Install the APK on an Android device (Android 8.0+ required) with the full WebP artwork catalog bundled offline.

---

## Web Version (Live Site)

The companion includes a fully featured web application, ideal for having the compendium open on a second monitor or tablet while playing.

### Use Online
Access the live web version immediately without installation:  
**[Persona Companion Web](https://sentovibes.github.io/persona-companion-app/)**

### Launching Locally
1. Clone the repository or download the source code.
2. Navigate to the `docs/` directory.
3. Serve the directory using a simple local HTTP server (for example, `npx http-server docs` or VS Code Live Server).
4. No heavy backend required: fast, local-first Single Page Application.

---

## Building and Development

```bash
# Clone the repository
git clone https://github.com/Sentovibes/persona-companion-app.git

# Build the Android debug APK
./gradlew assembleDebug
```

---

## Roadmap: What is Next?
- [x] **Universal JSON Backup & Restore**: Cross-platform transfer of favorites, quest checkmarks, and notes between Android and Web PWA.
- [ ] **Mayoido Antiques Weapon Fusion Directory**: Nihil weapon and heart item crafting tables for Persona 3 Reload and Episode Aigis.
- [x] **Skill Inheritance Rules Matrix**: Display incompatible elements and inheritance slot caps in the compendium.
- [ ] **Custom Persona Build Planner**: Teammate 8-skill build planner with Jazz Jin and bike skill recommendations.

---

## Support the Project
If this app helped you save a Social Link or find that one rare drop, consider supporting development:

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/sentovibes)

---

## Legal
This is a fan-made project and is **not** affiliated with, endorsed by, or connected to **Atlus** or **SEGA**. Made with care for the Persona community.
