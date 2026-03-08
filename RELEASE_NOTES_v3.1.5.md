# Persona Companion v3.1.5 Release Notes

## New Features

### Filtering System
- Added filter button to Persona and Enemy list screens
- Filter personas by:
  - Level range (min/max)
  - Skill type (Physical, Magic, Healing, Support, Passive, All)
  - Game exclusive content
  - DLC content
  - Favorites only
- Filter enemies by:
  - Level range (min/max)
  - Resistance type (weak to, resists, nullifies, drains specific elements)
  - Game exclusive content
  - Favorites only
- Multiple sort options for both personas and enemies

### Favorites System
- Heart icon in detail screens to favorite/unfavorite personas and enemies
- Favorites persist across app sessions
- Filter to show only favorited items

### Persona 5 Strikers Enemy Compendium
- Added complete P5S enemy database (86 enemies total)
  - 6 Main Bosses (including multi-phase bosses)
  - 26 Mini Bosses (Lock Keepers, Target Shadows, Powerful Shadows, Dire Shadows, Super Boss)
  - 54 Regular Enemies (all recruitable personas/shadows)
- Multi-phase boss support:
  - Akira Konoe (2 phases: Zephyrus Mech → True Form)
  - False God Demiurge (2 phases with parts in final phase)
- Complete stats, skills, resistances, and locations for all enemies

## Bug Fixes
- Fixed P5S enemy data (was incorrectly using P5 vanilla data)
- Improved filter performance with game-specific element handling

## Technical Details
- Version: 3.1.5 (Build 8)
- P5S enemies use correct element order: Physical, Gun, Fire, Ice, Elec, Wind, Psy, Nuke, Bless, Curse
- Filter system integrated with existing search functionality
- Debug build includes error logging and debug overlay

## Total Enemy Count by Game
- P3 FES: 368 (299 enemies, 55 mini-bosses, 14 main bosses)
- P3 Portable: 176 (140 enemies, 23 mini-bosses, 13 main bosses)
- P3 Reload: 496 (420 enemies, 62 mini-bosses, 14 main bosses)
- P4: 271 (243 enemies, 17 mini-bosses, 11 main bosses)
- P4 Golden: 297 (265 enemies, 19 mini-bosses, 13 main bosses)
- P5: 149 (112 enemies, 29 mini-bosses, 8 main bosses)
- P5 Royal: 168 (129 enemies, 30 mini-bosses, 9 main bosses)
- **P5 Strikers: 86 (54 regular enemies, 26 mini-bosses, 6 main bosses)**

**Grand Total: 2,011 enemies across all games**

## Known Issues
- None

## Coming in Future Updates
- Recent history tracking
- Dark mode toggle
- Compact view option
- Comparison mode (side-by-side)
- Export/share builds
- Quick stats icons
- Swipe gestures
