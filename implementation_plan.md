# Implementation Plan - Rebuilding Item Database (V3)

The user has provided specific `.txt` files for P3 (FES, Portable, Reload), P4 (Original, Golden), and P5. 
The objective is to rebuild the item database with "EVERYTHING" from the source files (Price, Location, Stats, etc.) and update the UI to show an expandable card for each item.

## Proposed Changes

### [RESEARCH] Extensive Data Mapping
I will update the `Item` model and the import script to capture:
- **General Fields**: Name, Info (Effect), Price, Location.
- **Equipment Fields**: Attack, Accuracy, Stats (to show in the expanded view).
- **Categories**: Weapon, Armor, Accessory, Skill Card, Item.

### [MODIFY] [Item.kt](file:///c:/Users/omare/Music/Persona-Companion-App/persona-companion-app/app/src/main/java/com/persona/companion/models/Item.kt)
Add fields to support the full Wiki data:
```kotlin
data class Item(
    val name: String,
    val description: String, // Maps to 'Info' in the txt
    val effect: String = "",  // Maps to specifically identified effects
    val price: String = "",   // Changed to String to handle formats like "850 / 680"
    val sellPrice: String = "",
    val location: String = "",
    val attack: String? = null,
    val accuracy: String? = null,
    val category: String = "General"
)
```

### [MODIFY] [ItemListScreen.kt](file:///c:/Users/omare/Music/Persona-Companion-App/persona-companion-app/app/src/main/java/com/persona/companion/ui/screens/ItemListScreen.kt)
Update `ItemCard` to be expandable:
- Default view shows Name, Icon (based on category), and a summary of Effect.
- Expanded view (on tap) shows **Price**, **Sell Price**, **Location**, and **Stats** (for weapons/armor).

### [NEW] [import_wiki_items.py](file:///c:/Users/omare/Music/Persona-Companion-App/persona-companion-app/tmp/import_wiki_items.py)
A specialized Python script to parse the Wikipedia-style dumps:
- **Heuristics**: Detect table headers and data rows.
- **Cleaning**: Remove wiki artifacts like `[[` or `|`.
- **Merging**: Handle items spanning multiple lines.

### [NEW] Data Assets
- `data/items/p3r_items.json` [Persona 3 Reload]
- `data/items/p3p_items.json`
- `data/items/p3fes_items.json`
- `data/items/p4_items.json`
- `data/items/p4g_items.json`

## User Review Required

> [!IMPORTANT]
> The "expanded" view will show **EVERYTHING** found in the text files, including long location strings (e.g., "Locked Chest - Steamy Bathhouse B3F"). I will use a clean, vertically stacked layout in the expanded card.

> [!TIP]
> I will add a "Persona 3 Reload" entry to the P3 series menu to support the new `p3r_items.json`.

## Open Questions
*None at this time based on user feedback (P3R confirmed).*

## Verification Plan

### Automated Tests
- Run `python import_wiki_items.py` and verify all 6 JSON files are generated.
- Lint check `ItemListScreen.kt` for UI state management.

### Manual Verification
- Deploy to emulator and tap on a "Peach Seed" or a "Bastard Sword".
- Verify that the card expands to show "Location: Shiroku Store" and "Price: ¥14,000".
- Verify that Persona 3 Reload now has a full item list.
