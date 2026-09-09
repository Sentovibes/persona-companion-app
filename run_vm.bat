@echo off
setlocal
cd /d "%~dp0"
set ANDROID_AVD_HOME=F:\AndroidDev\avd
set ANDROID_USER_HOME=F:\AndroidDev
set ANDROID_SDK_ROOT=F:\AndroidDev\sdk
set ANDROID_HOME=F:\AndroidDev\sdk
set PATH=F:\AndroidDev\sdk\platform-tools;F:\AndroidDev\sdk\emulator;%PATH%

echo ============================================================
echo   Starting Persona Companion Android Emulator (F:\AndroidDev)
echo ============================================================

taskkill /F /IM qemu-system-x86_64.exe >nul 2>&1
taskkill /F /IM emulator.exe >nul 2>&1

echo Launching emulator window...
start "" "F:\AndroidDev\sdk\emulator\emulator.exe" -avd persona_test -gpu host -no-snapshot-load -crash-report-mode disabled -no-metrics -adb-path "F:\AndroidDev\sdk\platform-tools\adb.exe"

echo Waiting for emulator to boot...
"F:\AndroidDev\sdk\platform-tools\adb.exe" wait-for-device
:waitboot
for /f "tokens=*" %%i in ('"F:\AndroidDev\sdk\platform-tools\adb.exe" shell getprop sys.boot_completed 2^>nul') do set BOOT=%%i
if not "%BOOT%"=="1" (
    ping 127.0.0.1 -n 3 >nul
    goto waitboot
)

echo Emulator ready! Installing latest APK...
if exist "app\build\outputs\apk\release\persona-companion-v7.1.3.apk" (
    "F:\AndroidDev\sdk\platform-tools\adb.exe" install -r app\build\outputs\apk\release\persona-companion-v7.1.3.apk
    "F:\AndroidDev\sdk\platform-tools\adb.exe" shell monkey -p com.persona.companion -c android.intent.category.LAUNCHER 1
) else if exist "app\build\outputs\apk\release\persona-companion-v7.1.1.apk" (
    "F:\AndroidDev\sdk\platform-tools\adb.exe" install -r app\build\outputs\apk\release\persona-companion-v7.1.1.apk
    "F:\AndroidDev\sdk\platform-tools\adb.exe" shell monkey -p com.persona.companion -c android.intent.category.LAUNCHER 1
) else (
    "F:\AndroidDev\sdk\platform-tools\adb.exe" install -r app\build\outputs\apk\debug\persona-companion-v7.1.0-debug-debug.apk
    "F:\AndroidDev\sdk\platform-tools\adb.exe" shell monkey -p com.persona.companion.debug -c android.intent.category.LAUNCHER 1
)
echo ============================================================
echo Done! App is running on the emulator.
echo ============================================================

:keepalive
ping 127.0.0.1 -n 10 >nul
goto keepalive
