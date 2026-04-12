# Release Notes - v6.0.0

## Massive Release - Standardized Database & Quest Tracker

### New Features

**Comprehensive Item Database**
- Standardized ~10,000 items across all games (9,904 unique entries)
- Full coverage for P3P, P3FES, P3R, P4, P4G, P5, and P5R
- Every consumable, weapon, accessory, and key item included
- Verified data accuracy against megaten-fusion-tool and game internals
- No more missing items for P3 Reload DLC!

**Master Skills Database**
- Integrated comprehensive skill data for 4,098 unique skills
- High-fidelity elemental icons (Phys, Fire, Ice, Elec, Wind, Light, Dark, Almighty, etc.)
- Detailed effect descriptions and multi-game scaling behavior
- Categorized by type: Physical, Magic, Support, Passive, and Recovery

**Quest & Request Tracker**
- Dedicated tracking system for 546 City Quests and Velvet Room Requests
- **Local Persistence**: Mark quests as completed and stay tracked across app restarts
- **Reverse Linking**: Items required for requests are now linked directly to acquisition methods
- Organized by game series for easy navigation (P3R Bureau, P4G Quests, P5R Requests)

### Technical Improvements

**Optimized Database Performance**
- Migrated to Database Version 9 with optimized indices for massive lists
- Smooth, lag-free scrolling even through 10,000+ item entries
- Improved startup time with lazy loading for complex data paths

**APK Size & Assets**
- Reduced APK size to 13.4MB (Highly compressed JSON and vector assets)
- Standardized data paths across all games (Unified hyphen-based filesystem)
- Removed 12MB of temporary/redundant files from the build path

---

**Full Changelog**: https://github.com/Sentovibes/persona-companion-app/compare/v5.2.0...v6.0.0

---
