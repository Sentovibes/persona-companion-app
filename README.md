# Persona Companion App v6.0.0

[![Status](https://img.shields.io/badge/Status-Active-brightgreen.svg)]()
[![Version](https://img.shields.io/badge/Version-6.0.0--WIP-blue.svg)]()
[![License](https://img.shields.io/badge/License-MIT-orange.svg)](LICENSE)
[![Support](https://img.shields.io/badge/Support-Ko--fi-red.svg)](https://ko-fi.com/sentovibes)

> The ultimate, all-in-one "Absolute Perfection" reference tool for the Persona series.

The Persona Companion App is a high-performance, native Android application built with Jetpack Compose and Material 3. It is designed to be a comprehensive reference toolkit for players, providing detailed data across the entire mainline series.

---

## New in v6.0.0: The Item Compendium Update
Universal standardization of item databases across the entire series.
- 5,000+ Items processed into a universal format.
- P5 Royal: 1,873 items.
- P3 Reload: 1,229 items.
- P4 Golden: 1,196 items.
- P3 FES/Portable: 690 items.

---

## Supported Titles

| Game | Features | Theme |
| :--- | :--- | :--- |
| Persona 3 Reload | Full Compendium, Episode Aigis | Indigo Blue |
| Persona 5 Royal | Confidants, Itemization, DLC | Rebellion Red |
| Persona 4 Golden | Quests, Social Links, Fusion | Investigation Yellow |
| Persona 3 FES/P | Both MC Routes, Requests | Sea Blue |

---

## Key Features

### Comprehensive Databases
- Persona Compendium: Stats, full skillsets, and advanced fusion recipes.
- Enemy Bestiary: Weaknesses, resistances, and drop items.
- Social Link Guides: Optimized dialogue choices and rank requirements.
- Classroom Answers: Solutions for all supported titles.

### UX and UI
- Multi-Device: Optimized for Phones, Tablets, Android TV, and ChromeCast.
- Smart Filters: Filter by Arcana, Level, DLC status, or Element.
- HD Image Support: Optional 1.3GB HD asset pack for full immersion.
- OLED Dark Mode: Optimized themes for battery efficiency on OLED screens.

---

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
