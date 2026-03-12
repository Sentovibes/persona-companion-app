@echo off
echo Building Persona Themed UI Test APK...
echo.

cd /d "%~dp0"

if not exist "gradlew.bat" (
    echo ERROR: gradlew.bat not found!
    echo You need to create a Gradle wrapper first.
    echo.
    echo Run this in the project directory:
    echo gradle wrapper
    pause
    exit /b 1
)

echo Running Gradle build...
call gradlew.bat assembleDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo BUILD SUCCESSFUL!
    echo ========================================
    echo.
    echo APK Location:
    echo build\outputs\apk\debug\app-debug.apk
    echo.
    echo Transfer this file to your phone and install it.
    echo.
) else (
    echo.
    echo ========================================
    echo BUILD FAILED!
    echo ========================================
    echo.
    echo Make sure you have:
    echo 1. Java JDK 17 installed
    echo 2. ANDROID_HOME environment variable set
    echo 3. Android SDK installed
    echo.
)

pause
