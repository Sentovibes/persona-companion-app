# Privacy Policy for Persona Companion

**Effective Date:** November 20, 2026  
**Applies to:** Persona Companion (Android App & Web App)  
**Developer:** Sentovibes  
**Hosted URL:** [https://sentovibes.github.io/persona-companion-app/privacy.html](https://sentovibes.github.io/persona-companion-app/privacy.html)

## 1. Scope
Persona Companion is an offline reference guide for Persona 3, Persona 4, and Persona 5. This policy covers the Android application and the companion Web App hosted at `sentovibes.github.io/persona-companion-app`. Both work offline without an account or login, and neither collects, stores, sells, or shares personal information.

## 2. Data We Do Not Collect
Persona Companion does not ask for or gather names, email addresses, phone numbers, account credentials, payment details, GPS location, advertising IDs, or cross-app browsing history. There is no user registration and no remote backend storing user profiles.

## 3. Android Permissions
All persona and enemy artwork ships inside the app package. The Android build requests only the permissions below:

| Permission | Purpose |
| --- | --- |
| `INTERNET` | Runs the optional local Wi-Fi Cast server (HTTP/WebSocket on your local network to view guides on a TV or second screen), checks the public GitHub Releases API when you tap "Check for Updates" in Settings, and opens external links you tap (GitHub or Ko-fi). No personal data is sent. |
| `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`, `POST_NOTIFICATIONS` | Keeps the local Wi-Fi Cast server active while the app is backgrounded and shows an ongoing notification so you can see and stop the active session. |
| `WAKE_LOCK` | Prevents the device from suspending the local Wi-Fi Cast server while a casting session is running. |

## 4. Local Storage & Backups
Saved favorites, compendium checklists, completed requests, Social Link progress, recent history, and settings stay on your device.

| Platform | How data is stored and removed |
| --- | --- |
| Android App | Stored locally in Android Room Database and SharedPreferences. You can export or restore a `persona_companion_backup.json` file through Android's system file picker. Standard Android system backup (`allowBackup`) may back up local data if enabled in your device settings. To erase everything, clear storage in Android Settings or uninstall the app. |
| Web App | Stored in your browser's `localStorage`, with static files cached offline via a Service Worker. Supports JSON backup export and import compatible with the Android app, plus an optional user-configured connection to a local Velvet Hook server on your own machine or LAN. Clearing site data in your browser removes all stored state. |

## 5. Third-Party Services & Analytics

| Platform | Ads, analytics, and external requests |
| --- | --- |
| Android App | No ads, no analytics, no crash-reporting SDKs, and no third-party trackers. |
| Web App | No ads. Uses Umami Cloud (`cloud.umami.is`) for cookieless aggregate page-view counts without personal data or cross-site tracking, and loads web fonts from Google Fonts (`fonts.googleapis.com`). |

## 6. Children's Privacy
Persona Companion does not knowingly collect personal information from children under 13. Because no personal data is collected or transmitted, the app complies with COPPA and Google Play Families Policy requirements.

## 7. Disclaimer, Changes & Contact
Persona Companion is a fan-made reference tool and is not affiliated with, endorsed by, or sponsored by ATLUS or SEGA. Persona, Persona 3, Persona 4, and Persona 5 are trademarks of ATLUS Co., Ltd. and SEGA Corporation.

If this policy changes in a future release, the updated version will be published here with a new effective date. For questions or privacy requests, open an issue on GitHub at [github.com/Sentovibes/persona-companion-app](https://github.com/Sentovibes/persona-companion-app).
