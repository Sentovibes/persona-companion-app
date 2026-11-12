# Persona Companion — HarmonyOS Watch (`.hap`)

Native HarmonyOS ArkTS wearable module targeting Huawei Watch devices (`"deviceTypes": ["wearable"]`, API 12+).

## Included Features
- P5R, P5, P4G, P4, P3R, P3P, P3FES game switcher
- Classroom & Exam Answers
- Enemy / Shadow Weaknesses & Boss lookup with search filter
- Confidant / Social Link optimal hangout choices with Pin-to-Top
- All game JSON datasets bundled under `entry/src/main/resources/rawfile/data/` (~1.96 MB total, well inside Huawei Watch's 20 MB ceiling)

## Running on Your Friend's Huawei Watch
1. Download and install **Huawei DevEco Studio** (`developer.huawei.com/consumer/en/deveco-studio/`).
2. Open the `harmonyos-watch/` folder in DevEco Studio.
3. Pair/connect the Huawei Watch on the same Wi-Fi network via DevEco Studio (**Tools -> IP Connection** or Huawei Health/Developer options wireless debugging on the watch).
4. Open **File -> Project Structure -> Signing Configs**, sign in with your free **Huawei ID**, check **Automatically generate signature**, and click **OK**.
5. Click **Run** to install directly onto the watch, or **Build -> Build Hap(s)/APP(s) -> Build APP(s)** to export a signed `.app` / `.hap` for Huawei AppGallery Watch.
