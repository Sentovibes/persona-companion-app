# Image Scraping Guide

## Problem
Fandom Wiki blocks automated scraping (returns 403 Forbidden). The Python scripts cannot download images directly.

## Solutions

### Option 1: Manual Download with Browser Extension
1. Install "DownThemAll" or similar download manager extension
2. Visit persona pages on https://megamitensei.fandom.com/wiki/
3. Use extension to batch download images from infoboxes
4. Save to `extras/images/personas/` or `extras/images/enemies/`

### Option 2: Selenium Browser Automation
Use Selenium to control a real browser (bypasses 403):
```python
from selenium import webdriver
from selenium.webdriver.common.by import By
import time

driver = webdriver.Chrome()
driver.get('https://megamitensei.fandom.com/wiki/Alice')
time.sleep(2)
img = driver.find_element(By.CSS_SELECTOR, '.pi-image-thumbnail')
img_url = img.get_attribute('src')
# Download img_url
```

### Option 3: Use Existing Game Assets
Extract images directly from game files (requires game dumps):
- P3R: Extract from game files
- P4G: Extract from PC version
- P5R: Extract from game files

## Image Organization

Images should be organized as:
```
extras/images/
├── personas/
│   ├── p3r/
│   ├── p4g/
│   └── p5r/
└── enemies/
    ├── p3r/
    ├── p4g/
    └── p5r/
```

## Game Version Mapping
- All P3 games (P3, P3 FES, P3P) → use P3R images
- All P4 games (P4, P4G) → use P4G images
- All P5 games (P5, P5R, P5S) → use P5R images

## Next Steps
Once images are downloaded:
1. Place them in the correct folders under `extras/images/`
2. Copy to `app/src/main/assets/images/` 
3. Update app code to load images from assets
4. Images will display in cast view and detail screens
