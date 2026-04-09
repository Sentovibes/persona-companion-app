# Persona Image Scraper

Uses Playwright (headless Chromium) to bypass Cloudflare and scrape portrait images from megatenwiki.com.

## Setup

```bash
cd scraper
npm install
npm run install-browser
```

## Run

```bash
npm run scrape
```

Progress is saved to `done.json` and `failed.json` — safe to interrupt and resume.

## Dedup

```bash
# Check for exact duplicate images
node dedup.js

# Auto-delete dupes (keeps shortest/canonical filename)
node dedup.js --auto-delete
```

## How it works

1. Launches headless Chromium and visits the wiki homepage to get Cloudflare cookies
2. For each persona in `royal_personas.json` (P5R — 232 personas):
   - Queries the MediaWiki API for portrait/model images
   - Scores results to prefer the latest game (P5R > P5 > P4G > ...)
   - Downloads the best match
3. Skips already-downloaded personas on resume
