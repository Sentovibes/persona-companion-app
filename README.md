# Persona Companion

A native Android companion app for the Persona series — built as an open-source mini wiki for players.

> Built with Kotlin · Jetpack Compose · Material 3

---

## What's in the app

| Feature | Status |
|---|---|
| Persona browser (P3 / P4 / P5) | ✅ Available |
| Persona stats & skills | ✅ Available |
| Search by name or arcana | ✅ Available |
| Fusion Calculator | 🔒 Coming soon |
| Social Links / Confidants guide | 🔒 Coming soon |
| Classroom Answers | 🔒 Coming soon |
| Bosses database | 🔒 Coming soon |
| Enemies database | 🔒 Coming soon |

---

## Project structure

```
persona-companion-app/
├── app/
│   └── src/main/
│       ├── java/com/persona/companion/
│       │   ├── data/            # Repository + static series catalogue
│       │   ├── models/          # Kotlin data classes (Persona, Game, Series)
│       │   ├── navigation/      # NavGraph and route definitions
│       │   ├── ui/
│       │   │   ├── screens/     # One file per screen
│       │   │   ├── components/  # Reusable Composables
│       │   │   ├── theme/       # Colors, Typography, Theme
│       │   │   └── viewmodels/  # ViewModel per screen
│       │   └── utils/           # JsonLoader
│       └── assets/
│           └── data/
│               ├── persona3/    # Persona 3 JSON files
│               ├── persona4/    # Persona 4 JSON files
│               └── persona5/    # Persona 5 / Royal JSON files
```

---

## How to build

1. Clone the repo
2. Open the project folder in **Android Studio Hedgehog** or later
3. Let Gradle sync
4. Run on a device or emulator (minSdk 26)

---

## How to contribute

### Adding or fixing Persona data

All Persona data lives in JSON files under `app/src/main/assets/data/`.  
Each file is a simple JSON array — just edit the relevant file and open a PR.

**Persona JSON schema:**

```json
{
  "name": "Orpheus",
  "arcana": "Fool",
  "level": 1,
  "stats": {
    "strength": 2,
    "magic": 3,
    "endurance": 2,
    "agility": 3,
    "luck": 2
  },
  "skills": [
    { "name": "Bash", "level": 0 },
    { "name": "Agi",  "level": 0 }
  ],
  "description": "The initial Persona of the protagonist.",
  "weaknesses":  ["Pierce"],
  "resistances": [],
  "nullifies":   [],
  "repels":      [],
  "absorbs":     [],
  "special_fusion": false
}
```

- `level` in skills — set to `0` for innate skills, otherwise the level learned
- Affinity arrays accept element strings: `Fire`, `Ice`, `Elec`, `Wind`, `Psychic`, `Nuclear`, `Bless`, `Curse`, `Phys`, `Gun`, `Almighty`

### Adding a new game version

1. Add your JSON data file to the correct `assets/data/` subfolder
2. Add a `Game(...)` entry to `SeriesData.kt` pointing to the new file
3. Done — the rest of the app picks it up automatically

### Adding a new screen (Coming Soon feature)

1. Create a new `@Composable` file under `ui/screens/`
2. Add a route object to `navigation/NavGraph.kt`
3. Add a composable destination in `NavGraph`
4. Update `CategoryScreen.kt` to set `available = true` for that category

---

## Data sources

Persona data is sourced from community wikis:

- [Megami Tensei Wiki](https://megamitensei.fandom.com/)
- [Megaten Database](https://megaten.io/)

This is a fan project with no affiliation to Atlus or SEGA.

---

## License

MIT — free to use, fork, and contribute.
