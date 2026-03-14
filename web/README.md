# Persona Companion Web

A lightweight web version of the Persona Companion app, accessible directly from your browser.

## Features

- Browse personas, enemies, and classroom answers
- Search and filter functionality
- Responsive design for mobile and desktop
- No installation required
- Works offline after first load

## Deployment to GitHub Pages

1. Push the `web` folder to your repository
2. Go to repository Settings > Pages
3. Set source to "Deploy from a branch"
4. Select branch: `main` and folder: `/web`
5. Save and wait for deployment

Your site will be available at: `https://sentovibes.github.io/persona-companion-app/`

## Local Development

Simply open `index.html` in your browser. No build process required.

## Data Sources

The web app reads JSON data directly from the Android app's assets folder. All data is loaded client-side.

## Limitations

- Images are NOT included in the web version (would be too large for GitHub Pages)
- Social Links feature is not yet implemented
- Fusion calculator is not available in web version

## Browser Support

Works on all modern browsers (Chrome, Firefox, Safari, Edge).
