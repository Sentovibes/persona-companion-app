# v3.1.0 Implementation TODO

## ✅ Completed
- Debug build configuration with separate APK
- Debug logger system
- Debug error handler (catches crashes)
- Debug overlay UI with logs, errors, and stats tabs
- Floating debug button (debug builds only)
- Version updated to 3.1.0
- UserPreferences for favorites and history
- FilterOptions models (PersonaFilters, EnemyFilters, enums)
- FilterSheet UI components (PersonaFilterSheet, EnemyFilterSheet)
- FilterUtils for filtering and sorting logic
- PersonaListViewModel updated with filter support

## 🚧 In Progress - Need to Complete

### ViewModels
- [x] Update EnemyListViewModel with filter support
- [x] Add favorites toggle to EnemyListViewModel
- [ ] Add recent history tracking to both ViewModels

### UI Screens
- [x] Update PersonaListScreen to show filter button and use filters
- [x] Update EnemyListScreen to show filter button and use filters
- [x] Add favorite button to PersonaDetailScreen
- [x] Add favorite button to EnemyDetailScreen
- [ ] Add recent history tracking when viewing details
- [ ] Update PersonaDetailScreen to track views
- [ ] Update EnemyDetailScreen to track views

### Dark Mode (v3.3)
- [ ] Add dark mode toggle to SettingsScreen
- [ ] Update PersonaCompanionTheme to support dark mode
- [ ] Read dark mode preference and apply

### Compact View (v3.3)
- [ ] Add compact view toggle to SettingsScreen
- [ ] Create compact list item layouts
- [ ] Update list screens to use compact/detailed view based on preference

### Comparison Mode (v3.3)
- [ ] Create ComparisonScreen for personas
- [ ] Create ComparisonScreen for enemies
- [ ] Add "Compare" button to detail screens
- [ ] Implement side-by-side comparison UI

### Export/Share (v3.3)
- [ ] Add share button to PersonaDetailScreen
- [ ] Add share button to EnemyDetailScreen
- [ ] Format persona/enemy data as shareable text
- [ ] Implement Android share intent

### Quick Stats Icons (v3.3)
- [ ] Create resistance summary icon component
- [ ] Add to list items for quick reference
- [ ] Color-code by resistance type

### Swipe Gestures (v3.3)
- [ ] Implement swipe-to-favorite on list items
- [ ] Add swipe indicators
- [ ] Haptic feedback

### Recently Viewed
- [ ] Create RecentlyViewedScreen
- [ ] Add navigation to recently viewed
- [ ] Display recent personas and enemies

### Favorites Screen
- [ ] Create FavoritesScreen
- [ ] Add navigation to favorites
- [ ] Display favorite personas and enemies
- [ ] Allow removal from favorites

## Testing Needed
- [ ] Test debug build - verify error catching works
- [ ] Test debug overlay - verify logs appear
- [ ] Test filtering on personas
- [ ] Test filtering on enemies
- [ ] Test sorting options
- [ ] Test favorites persistence
- [ ] Test dark mode
- [ ] Test compact view
- [ ] Build both debug and release APKs

## Known Issues
- None yet

## Notes
- Debug build has `.debug` suffix in package name
- Debug features only enabled when BuildConfig.ENABLE_DEBUG_FEATURES is true
- Favorites stored in SharedPreferences as Set<String>
- Recent history stored as JSON in SharedPreferences (max 50 items)
