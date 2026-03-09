# Cast Support Documentation

## Overview
Custom cast system that works without Google Play Services. Displays enemy/persona data on TV browsers.

## Features
- ✅ HTTP server with WebSocket support (port 8080)
- ✅ QR code for easy connection
- ✅ 2x2 grid layout optimized for TV
- ✅ Stable connection with server-side ping
- ✅ WiFi lock to maintain connection
- ✅ Works while app is in foreground

## Layout Structure

### 2x2 Grid Display
```
┌─────────────────┬─────────────────┬──────────┐
│  Basic Info     │  Stats          │  Image   │
│  - Level        │  - Strength     │  Panel   │
│  - HP/SP        │  - Magic        │  (Ready  │
│  - Arcana       │  - Endurance    │   for    │
│  - Area         │  - Agility      │   future)│
│  - EXP          │  - Luck         │          │
│  - Drops        │                 │          │
├─────────────────┼─────────────────┤          │
│  Resistances    │  Skills         │          │
│  (Each on own   │  (Each on own   │          │
│   line)         │   line)         │          │
└─────────────────┴─────────────────┴──────────┘
```

## Usage

### Starting Cast
1. Open app on phone
2. Tap Cast button in top bar
3. Click "Start Casting"
4. On TV browser, navigate to displayed URL
5. Or scan QR code with another phone

### Connection Details
- Server runs on port 8080
- URL format: `http://192.168.1.X:8080`
- WebSocket keeps connection alive with 3-second ping
- WiFi lock prevents disconnection

### Behavior
- Server runs while app is in foreground
- Stops when app is closed or screen times out
- Automatically broadcasts when viewing enemy/persona details

## Image Support

### Current Status
- Image panel ready in layout
- Images not yet implemented
- Fandom Wiki blocks automated scraping

### Future Implementation
Images should be organized as:
```
app/src/main/assets/images/
├── personas/
│   ├── p3r/
│   ├── p4g/
│   └── p5r/
└── enemies/
    ├── p3r/
    ├── p4g/
    └── p5r/
```

### Image Mapping
- All P3 games (P3, P3 FES, P3P) → P3R images
- All P4 games (P4, P4G) → P4G images
- All P5 games (P5, P5R, P5S) → P5R images

### Getting Images
See `extras/IMAGE_SCRAPING_GUIDE.md` for options:
1. Manual download from wiki
2. Browser automation with Selenium
3. Extract from game files

## Technical Details

### Files
- `CastServer.kt` - HTTP/WebSocket server
- `CastManager.kt` - Server lifecycle management
- `CastButton.kt` - UI for starting/stopping cast
- `QRCodeGenerator.kt` - QR code generation
- `EnemyDetailScreen.kt` - Broadcasts enemy data

### Color Scheme
- Background: #0F0F0F
- Surface: #1A1A1A
- Cards: #222222
- Text: #EEEEEE
- Accent: #1A6FCC (P3 Blue)

### Dependencies
```gradle
implementation 'org.nanohttpd:nanohttpd:2.3.1'
implementation 'org.nanohttpd:nanohttpd-websocket:2.3.1'
implementation 'com.google.zxing:core:3.5.2'
```

## Version History
- v3.3.0 - Initial cast system implementation
- 2x2 grid layout
- Stable WebSocket connection
- WiFi lock support
