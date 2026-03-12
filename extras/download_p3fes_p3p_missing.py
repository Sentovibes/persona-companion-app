import requests
import os

print("=" * 70)
print("DOWNLOADING P3FES & P3P MISSING ENEMIES")
print("=" * 70)

# Manual URLs for the remaining missing enemies
# These need to be found manually on the wiki
manual_downloads = {
    "p3fes": {
        # "affection_relic": "URL_HERE",
        # "conceited_maya": "URL_HERE",
        # "indolent_maya_a": "URL_HERE",
        # "judgement_sword": "URL_HERE",
        # "loss_giant": "URL_HERE",
        # "merciful_maya": "URL_HERE",
    },
    "p3p": {
        # "judgement_sword": "URL_HERE",
        # "merciful_maya": "URL_HERE",
    }
}

print("\nP3FES Missing (9):")
print("  1. Affection Relic")
print("  2. Chariot & Justice (already have chariot___justice.png)")
print("  3. Conceited Maya")
print("  4. Emperor & Empress (already have emperor___empress.png)")
print("  5. Fortune & Strength (already have fortune___strength.png)")
print("  6. Indolent Maya A")
print("  7. Judgement Sword")
print("  8. Loss Giant")
print("  9. Merciful Maya")

print("\nP3P Missing (5):")
print("  1. Chariot & Justice (already have chariot___justice.png)")
print("  2. Emperor & Empress (already have emperor___empress.png)")
print("  3. Fortune & Strength (already have fortune___strength.png)")
print("  4. Judgement Sword")
print("  5. Merciful Maya")

print("\n" + "=" * 70)
print("NOTES:")
print("=" * 70)
print("- Compound names (Chariot & Justice, etc.) were downloaded but")
print("  saved with '___' separator instead of '&'")
print("- Need to find wiki URLs for the remaining 6 unique enemies")
print("- Once URLs are found, add them to manual_downloads dict above")
print("\nSearch these on https://megatenwiki.com:")
print("  - Affection Relic")
print("  - Conceited Maya") 
print("  - Indolent Maya")
print("  - Judgement Sword")
print("  - Loss Giant")
print("  - Merciful Maya")
