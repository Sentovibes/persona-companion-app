# Deployment Guide

## GitHub Pages Setup

The web version is deployed to GitHub Pages automatically.

### Initial Setup (Already Done)

1. Web files are in the `/web` folder
2. `.nojekyll` file prevents Jekyll processing
3. All JSON data copied from Android assets

### Accessing the Web App

Live URL: https://sentovibes.github.io/persona-companion-app/web/

### Updating the Web Version

When you update data in the Android app:

```bash
# Copy updated JSON files to web
xcopy "app\src\main\assets\data\*.json" "web\data\" /S /I /Y

# Commit and push
git add web/data/
git commit -m "Update web data"
git push origin main
```

GitHub Pages will automatically rebuild and deploy within 1-2 minutes.

## Android APK Release

### Building Release APK

```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

### Creating a GitHub Release

1. Go to https://github.com/Sentovibes/persona-companion-app/releases
2. Click "Draft a new release"
3. Create a new tag (e.g., `v5.0.1`)
4. Upload the APK file
5. Add release notes
6. Publish release

## Image Pack

The 1.3GB images.zip is hosted separately at:
https://github.com/Sentovibes/persona-companion-images/releases

Users can download it from the app's Settings or import it manually.

## Web Version Limitations

- No images (would exceed GitHub Pages size limits)
- No fusion calculator (requires complex logic)
- Social Links feature not yet implemented

The web version is meant as a quick reference tool, while the Android app provides the full experience.
