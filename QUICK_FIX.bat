@REM One-liner to fix PNGs and build - just copy and paste into Command Prompt
@REM cd d:\TinyEnglishApp && node fix_png_resources.js && gradlew.bat assembleRelease

@echo off
REM Alternative inline method without using separate scripts:
REM This demonstrates the core PNG creation in batch (though complex)

REM The simplest approach:
REM 1. Make sure you're in d:\TinyEnglishApp
REM 2. Run: node fix_png_resources.js
REM 3. Run: gradlew.bat assembleRelease

echo To fix the Android build, execute these commands:
echo.
echo cd d:\TinyEnglishApp
echo node fix_png_resources.js
echo gradlew.bat assembleRelease
echo.
echo Or run the all-in-one script:
echo.
echo fix_and_build.bat
echo.
