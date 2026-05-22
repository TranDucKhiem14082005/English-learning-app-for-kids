#!/bin/bash
# Shell script to fix Android resources and build

cd "$(dirname "$0")"

echo ""
echo "====================================="
echo "Android Resource Fix and Build Script"
echo "====================================="
echo ""

echo "Step 1: Creating placeholder PNG files..."
echo ""

node fix_png_resources.js
if [ $? -ne 0 ]; then
    echo ""
    echo "ERROR: Failed to create PNG files"
    exit 1
fi

echo ""
echo "Step 2: Verifying PNG files were created..."
echo ""

for file in "app/src/main/res/mipmap-hdpi/color_q7.png" \
            "app/src/main/res/mipmap-hdpi/color_q12.png" \
            "app/src/main/res/mipmap-hdpi/color_q13.png"
do
    if [ -f "$file" ]; then
        size=$(stat -f%z "$file" 2>/dev/null || stat -c%s "$file" 2>/dev/null)
        echo "✓ $(basename $file) ($size bytes)"
    else
        echo "✗ $(basename $file) NOT found"
    fi
done

echo ""
echo "Step 3: Running Gradle build..."
echo ""

# Try to build release first
if [ -f "gradlew" ]; then
    ./gradlew assembleRelease
    if [ $? -ne 0 ]; then
        echo ""
        echo "Release build failed, trying regular build..."
        ./gradlew build
        if [ $? -ne 0 ]; then
            echo ""
            echo "Regular build failed, trying resource compile only..."
            ./gradlew compileReleaseResources
        fi
    fi
else
    echo "gradlew not found"
    exit 1
fi

echo ""
echo "====================================="
echo "Build process complete"
echo "====================================="
echo ""
