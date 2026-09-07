# Product Requirements Document (PRD)

## 1. Executive Summary
- **Product Name**: Persona Companion App
- **Version**: 7.2.0
- **Target Audience**: Players of mainline Persona titles seeking an offline-first, comprehensive reference tool.
- **Platforms**: Android Application and Web Progressive Web App (PWA).
- **Core Value Proposition**: An all-in-one companion delivering 100% database completeness, high-resolution visual artwork, and combat/quest strategy across 7 mainline Persona games without requiring an internet connection.

---

## 2. Supported Titles & Scope
The application provides full support, localized terminology, and custom visual styling for seven titles:
1. **Persona 3 FES** (The Journey & The Answer, 99 Elizabeth Requests, 170+ Weapon/Armor stats)
2. **Persona 3 Portable** (Male & FeMC routes, Elizabeth & Theodore requests, 710 Shinshoudo trading items)
3. **Persona 3 Reload** (Full Compendium, Episode Aigis, 101 Elizabeth Requests, Tartarus exploration)
4. **Persona 4** (Original PS2 Compendium, Investigation Team Social Links, 69 Quests)
5. **Persona 4 Golden** (Expanded Compendium, Marie & Hollow Forest, 88 Quests, Shuffle Time rank multipliers)
6. **Persona 5** (Original Compendium, Confidants, 36 Mementos Requests, Untouchable Shop equipment)
7. **Persona 5 Royal** (Third Semester, 43 Requests, Will Seeds, Kichijoji Jazz Club, DLC skills & traits)

---

## 3. Key Functional Modules

### 3.1 Game Selection & Multi-Game Switching
- **Screen**: Home / Game Selection
- **Behavior**: Users select a Persona title to dynamically switch active color themes (Indigo Blue for P3R, Investigation Yellow for P4G, Rebellion Red for P5R, Sea Blue for P3FES/P3P).
- **Persistence**: Remembers the user's last selected title across sessions.

### 3.2 Persona Compendium & Detail View
- **List View**: Displays all Personas for the selected title with sorting and filtering by Arcana, Level, elemental affinities, and DLC status.
- **Search Engine**: Real-time fuzzy query filter matching Persona name, Arcana, and skills.
- **Detail View**: Displays base stats (St, Ma, En, Ag, Lu), elemental affinity grid (Physical, Gun, Fire, Ice, Electric, Wind, Psychic, Nuclear, Bless, Curse, Almighty), full learned skill list with unlock levels, and high-resolution visual render.

### 3.3 Fusion Calculator Engine
- **Forward Fusion**: Select any two or three Personas to compute the resulting fusion Persona.
- **Reverse Fusion**: From any Persona detail page, calculates all possible 2-way and special triangle recipes required to fuse that Persona, sorted by total summoning cost.
- **Special Fusion Directory**: Dedicated tab for multi-persona recipes (e.g., Alice, Black Frost, Yoshitsune, Satanael).

### 3.4 Enemy Bestiary & Boss Guides
- **Enemy Directory**: Searchable list of all 1,503 shadows and bosses categorized by encounter area (Palaces, Mementos, Tartarus blocks, TV World dungeons).
- **Elemental Affinities**: Complete weakness, resistance, nullification, and reflection charts to plan combat tactics.
- **Boss Prep Guides**: Step-by-step boss strategy guides covering HP pools, turn mechanics, attack rotations, and recommended party compositions for all story, optional, and superbosses (The Reaper, Margaret, Lavenza, Okumura, Nyx Avatar, Elizabeth & Theodore).

### 3.5 Social Link & Confidant Dialogue Assistant
- **Directory**: Full list of Social Links / Confidants for each protagonist route (including Male and FeMC in P3P).
- **Optimal Choices**: Displays the best conversational response for every dialogue prompt to maximize affinity points (with matched Arcana multipliers).
- **Availability & Perks**: Documents meeting locations, days of the week, weather restrictions, and rank-up ability unlocks.

### 3.6 Classroom & Exam Answers
- **Calendar Navigation**: Chronological list of all in-game teacher questions, surprise quizzes, midterms, and final exam questions.
- **Quick Lookup**: Real-time search filter allowing users to instantly retrieve answers by typing keywords.

### 3.7 Side-Quests & Request Tracker
- **Directory**: Comprehensive index of 436 Velvet Room requests and NPC side quests.
- **Interactive Checkbox**: Allows users to mark individual requests as "Completed".
- **State Persistence**: Saves completed quest states in `localStorage` (Web) and Room SQLite (Android).

### 3.8 Items & Skills Lore Directory
- **Items**: 8,736 entries detailing attack power, defense, evasion, stat bonuses, healing properties, and shop costs.
- **Skills**: 3,638 entries with HP/SP costs, target scope (Single / All), accuracy rates, and exact mechanical effects.

---

## 4. Non-Functional Requirements & Architecture

### 4.1 Web Companion (PWA)
- **Zero Backend Dependency**: Static Single-Page Application (HTML5, Vanilla JS ES Modules, CSS3).
- **Offline First**: Service Worker caching for instant load times without an active internet connection.
- **Responsive Layout**: Fluid breakpoints supporting Desktop/Tablet (Adaptive Navigation Rail) and Mobile (Stacked Bottom Navigation).
- **Local Storage**: Retains user preferences, bookmarks, and quest completion toggles.

### 4.2 Performance & Quality Benchmarks
- **Page Load Time**: Under 1.5 seconds on mobile networks; under 300ms on cached repeat visits.
- **Zero Console Errors**: Clean browser console with zero unhandled exceptions or failed network calls.
- **Search Latency**: Filter results update in under 50ms for lists containing up to 1,500 items.
