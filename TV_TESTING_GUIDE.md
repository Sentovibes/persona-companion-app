# Android TV Testing Guide

## Quick Start

### Create Android TV Emulator

1. **Open Android Studio**
2. **Tools → Device Manager**
3. **Click "Create Device"**
4. **Select "TV" category**
5. **Choose a TV profile:**
   - Android TV (1080p) - recommended for testing
   - Android TV (4K) - for high-res testing
6. **Select System Image:**
   - API 34 (Android 14) or latest
   - x86_64 architecture
7. **Click "Finish"**

### Run the App

1. **Select TV emulator** from device dropdown
2. **Click Run** (green play button)
3. **Wait for emulator to boot** (first time takes 2-3 minutes)
4. **App will install and launch automatically**

## Navigation

### Using Mouse (Easiest)
- Click anywhere to navigate
- Works like a touchscreen

### Using Keyboard (TV Simulation)
- **Arrow Keys**: Navigate up/down/left/right
- **Enter**: Select/click
- **Backspace**: Go back
- **Escape**: Home button

### Using Gamepad (Optional)
- Connect Xbox/PlayStation controller
- D-pad for navigation
- A/X button to select
- B/Circle to go back

## What to Test

### Home Screen
✅ Series cards displayed horizontally (not vertically)
✅ Larger text (1.3x scale)
✅ Larger card heights (160dp vs 90dp on phone)
✅ Extra padding (48dp)
✅ Settings icon in top-right

### Game Selection
✅ Games displayed horizontally
✅ Readable from distance
✅ D-pad navigation works

### Enemy Detail Screen
✅ Stats on left side
✅ Large image placeholder on right
✅ Text is readable from distance
✅ All stats visible
✅ Skills list readable
✅ Resistances clear

### Navigation Flow
✅ Can navigate with arrow keys
✅ Back button works
✅ Focus indicators visible
✅ No touch required

## Expected Behavior

### Layout Differences

**Phone (for comparison)**
```
┌─────────────────┐
│ P3              │
│ P4              │  ← Vertical
│ P5              │
└─────────────────┘
```

**TV (what you should see)**
```
┌─────────────────────────────┐
│ P3    P4    P5              │  ← Horizontal
└─────────────────────────────┘
```

### Detail Screen

**Phone**
- Stats only, no image
- Vertical scroll

**TV**
- Stats left (50% width)
- Image right (50% width)
- Larger text
- More padding

## Common Issues

### Emulator Won't Start
- **Solution**: Increase RAM in AVD settings (4GB minimum)
- **Solution**: Enable hardware acceleration in BIOS

### App Looks Like Phone Version
- **Check**: Device type in Settings screen
- **Check**: Screen width should be >840dp
- **Fix**: Recreate emulator with TV profile

### Navigation Doesn't Work
- **Check**: Click on emulator window first
- **Check**: Use arrow keys, not WASD
- **Try**: Mouse click instead

### Text Too Small
- **Check**: Text scale should be 1.3x on TV
- **Check**: Device detected as TV (not tablet)

## Performance Testing

### Load Times
- **Phone**: ~2-3 seconds (no images)
- **Tablet**: ~3-4 seconds (medium images)
- **TV**: ~4-5 seconds (high images)

### Memory Usage
- **Phone**: ~80MB (no images)
- **Tablet**: ~120MB (medium images)
- **TV**: ~150MB (high images)

### Scrolling
- Should be smooth on all devices
- No lag when navigating

## Screenshots

Take screenshots to compare:
1. Home screen (horizontal series)
2. Enemy detail (L-shaped layout)
3. Text size comparison
4. Padding differences

## Reporting Issues

If you find issues, note:
- Device type detected
- Screen resolution
- Layout used (phone/tablet/TV)
- Expected vs actual behavior
- Screenshots if possible

## Advanced Testing

### Test Different Resolutions
- 720p (1280x720)
- 1080p (1920x1080)
- 4K (3840x2160)

### Test Orientation Changes
- Portrait (should handle gracefully)
- Landscape (primary TV orientation)

### Test with Real TV
- Install APK on Android TV device
- Test with actual remote
- Check readability from couch distance (10 feet)

## Emulator Shortcuts

- **Ctrl + M**: Menu button
- **Ctrl + H**: Home button
- **Ctrl + B**: Back button
- **Ctrl + P**: Power button
- **Ctrl + K**: Keyboard toggle

## Next Steps

After testing:
1. Verify all layouts work correctly
2. Check text readability
3. Test navigation flow
4. Report any issues
5. Suggest improvements

## Future Enhancements to Test

When implemented:
- [ ] Actual enemy images (not placeholders)
- [ ] Custom focus indicators
- [ ] Voice search
- [ ] Game controller button mapping
- [ ] Cast support
