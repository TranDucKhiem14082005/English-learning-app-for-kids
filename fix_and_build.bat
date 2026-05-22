@echo off
REM Batch script to fix Android resources and run build
REM This script creates placeholder PNG files and runs the Gradle build

setlocal enabledelayedexpansion

cd /d "%~dp0"

echo.
echo =====================================
echo Android Resource Fix and Build Script
echo =====================================
echo.

REM Step 1: Create PNG files using Node.js
echo Step 1: Creating placeholder PNG files...
echo.

node fix_png_resources.js
if errorlevel 1 (
    echo.
    echo ERROR: Failed to create PNG files
    exit /b 1
)

echo.
echo Step 2: Verifying PNG files were created...
echo.

if exist "app\src\main\res\mipmap-hdpi\color_q7.png" (
    echo ✓ color_q7.png exists
) else (
    echo ✗ color_q7.png NOT found
)

if exist "app\src\main\res\mipmap-hdpi\color_q12.png" (
    echo ✓ color_q12.png exists
) else (
    echo ✗ color_q12.png NOT found
)

if exist "app\src\main\res\mipmap-hdpi\color_q13.png" (
    echo ✓ color_q13.png exists
) else (
    echo ✗ color_q13.png NOT found
)

echo.
echo Step 3: Running Gradle build...
echo.

REM Try to build release first
call gradlew.bat assembleRelease
if errorlevel 1 (
    echo.
    echo Release build failed, trying regular build...
    call gradlew.bat build
    if errorlevel 1 (
        echo.
        echo Regular build failed, trying resource compile only...
        call gradlew.bat compileReleaseResources
    )
)

echo.
echo =====================================
echo Build process complete
echo =====================================
echo.

endlocal
