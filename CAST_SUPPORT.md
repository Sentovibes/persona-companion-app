# Google Cast / Chromecast Support - v3.2.1

## Overview
The app now supports casting to Chromecast and other Google Cast devices. You can cast the app from your phone or tablet to your TV.

## Features

### Cast Button
- Appears in the top-right of the home screen (next to settings)
- Only shows on phones and tablets (not on Android TV)
- Standard Google Cast icon that users recognize
- Tap to see available Cast devices

### How It Works
1. **Phone/Tablet**: Acts as the controller
2. **TV/Chromecast**: Displays the full app UI
3. **Automatic Detection**: App detects when casting and switches to TV layout
4. **Seamless Experience**: Navigate on phone, see results on TV

### Supported Devices
- Chromecast (all generations)
- Chromecast Ultra
- Chromecast with Google TV
- Android TV with Cast built-in
- Smart TVs with Chromecast built-in

## Usage

### Connecting
1. Make sure your phone/tablet and Cast device are on the same WiFi network
2. Open the Persona Companion app
3. Tap the Cast button (top-right)
4. Select your Cast device from the list
5. App will appear on your TV

### Disconnecting
1. Tap the Cast button again
2. Tap "Disconnect" or "Stop casting"
3. App returns to phone/tablet mode

## Technical Details

### Implementation
- **Google Cast SDK**: v21.4.0
- **MediaRouter**: v1.7.0
- **Receiver App ID**: CC1AD845 (default media receiver)

### Files Added
- `CastOptionsProvider.kt` - Cast SDK configuration
- `CastButton.kt` - Cast button component
- `CastUtils.kt` - Cast detection utilities

### Device Detection
The app automatically detects when casting:
```kotlin
DeviceType.CAST -> {
    // Use TV layout with large images
    // Text scaled 1.3x for readability
    // Extra padding (48dp)
}
```

### Layouts When Casting
- **Phone/Tablet**: Shows controls and navigation
- **TV**: Shows full UI with:
  - Horizontal series selection
  - L-shaped enemy details
  - Large images
  - 1.3x text scale
  - Extra padding

## Testing

### Requirements
- Android phone/tablet with the app installed
- Chromecast or Cast-enabled TV
- Both devices on same WiFi network

### Test Steps
1. Install debug APK on phone: `persona-companion-v3.2.1-debug-debug.apk`
2. Open app
3. Look for Cast button (top-right, next to settings)
4. Tap Cast button
5. Select your Cast device
6. Verify app appears on TV
7. Navigate on phone, see results on TV
8. Check TV layout (horizontal series, large text)

### Troubleshooting

**Cast button doesn't appear**
- Check WiFi connection
- Make sure Cast device is powered on
- Restart app
- Check if Cast device is on same network

**Can't find Cast device**
- Ensure both devices on same WiFi
- Check firewall settings
- Restart Cast device
- Update Google Play Services

**App crashes when casting**
- Check debug logs in debug build
- Verify Cast SDK initialized properly
- Check AndroidManifest has CastOptionsProvider

**TV shows phone layout**
- Device detection might be failing
- Check DeviceUtils.getDeviceType()
- Verify CastUtils.isCasting() returns true

## Limitations

### Current Version
- Uses default Cast receiver (basic functionality)
- No custom Cast UI on receiver
- No media controls (not a media app)
- Cast state not persisted across app restarts

### Future Enhancements
- Custom Cast receiver with branded UI
- Better state synchronization
- Cast queue management
- Picture-in-picture support
- Voice commands via Cast
- Multi-room audio (if applicable)

## Custom Receiver (Future)

To create a custom Cast receiver:

1. **Register at Google Cast Console**
   - Go to https://cast.google.com/publish/
   - Create new application
   - Get your custom receiver app ID

2. **Update CastOptionsProvider**
   ```kotlin
   .setReceiverApplicationId("YOUR_APP_ID_HERE")
   ```

3. **Create Custom Receiver HTML**
   - Host on HTTPS server
   - Implement Cast receiver API
   - Add custom branding/UI

4. **Benefits**
   - Custom branding
   - Better UI control
   - Advanced features
   - Analytics

## APK Size Impact

### Before Cast Support (v3.2.0)
- Debug: 17.68 MB
- Release: 12.12 MB

### After Cast Support (v3.2.1)
- Debug: 20.82 MB (+3.14 MB)
- Release: 14.80 MB (+2.68 MB)

The increase is due to:
- Google Cast SDK (~2.5 MB)
- MediaRouter library (~0.2 MB)

## Permissions

No additional permissions required! Cast uses existing INTERNET permission.

## Privacy

- No data sent to Google except Cast discovery
- No analytics or tracking
- All data stays local
- Cast connection is direct (device to device)

## Known Issues

- None currently

## Version History
- **v3.2.1**: Initial Cast support
  - Cast button in UI
  - Automatic device detection
  - TV layout when casting
  - Default receiver integration
