@echo off
echo ========================================
echo Building Persona Themed UI Test APK
echo ========================================
echo.

cd /d "%~dp0"

echo Cleaning previous builds...
call gradlew.bat clean

echo.
echo Building Test UI APK...
call gradlew.bat :testui:assembleDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo BUILD SUCCESSFUL!
    echo ========================================
    echo.
    echo APK Location:
    echo testui\build\outputs\apk\debug\testui-debug.apk
    echo.
    echo Transfer this file to your phone and install it.
    echo.
    start "" "%cd%\testui\build\outputs\apk\debug"
) else (
    echo.
    echo ========================================
    echo BUILD FAILED!
    echo ========================================
    echo Check the error messages above.
    echo.
)

pause
