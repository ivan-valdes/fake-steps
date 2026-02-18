#!/bin/bash
set -e

BUILD_TYPE="${1:-debug}"

echo "=== FakeSteps APK Builder ==="
echo "Build type: $BUILD_TYPE"
echo ""

if [ "$BUILD_TYPE" = "release" ]; then
    GRADLE_TASK="assembleRelease"
    APK_PATH="app/build/outputs/apk/release/app-release-unsigned.apk"
else
    GRADLE_TASK="assembleDebug"
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
fi

echo "Building Docker image (first time may take a few minutes)..."
docker compose build

echo ""
echo "Running Gradle $GRADLE_TASK..."
docker compose run --rm builder ./gradlew $GRADLE_TASK

echo ""
if [ -f "$APK_PATH" ]; then
    echo "BUILD SUCCESSFUL"
    echo "APK location: $APK_PATH"
    echo "Size: $(du -h "$APK_PATH" | cut -f1)"
else
    echo "Build completed. Checking for APK..."
    find app/build/outputs/apk -name "*.apk" 2>/dev/null || echo "No APK found - build may have failed."
fi
