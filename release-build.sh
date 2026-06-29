#!/usr/bin/env bash
# Local release-build helper for AndLinux.
# Usage: ./release-build.sh
#
# Requires a JDK 17 install and (optionally) a local.properties pointing at your
# Android SDK. If local.properties is missing, the script tries to fall back to
# the ANDROID_HOME / ANDROID_SDK_ROOT environment variables.
set -euo pipefail

cd "$(dirname "$0")"

# Make sure gradlew is executable.
chmod +x gradlew

# Best-effort local.properties generation from the environment.
if [[ ! -f local.properties ]]; then
    SDK_DIR="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
    if [[ -n "$SDK_DIR" ]]; then
        echo "sdk.dir=$SDK_DIR" > local.properties
        echo "Generated local.properties from ANDROID_HOME=$SDK_DIR"
    else
        echo "WARNING: no local.properties and no ANDROID_HOME set; build may fail." >&2
    fi
fi

./gradlew --no-daemon clean
./gradlew --no-daemon assembleFdroidRelease -x lintVitalFdroidRelease

echo
echo "Build finished. APKs:"
ls -1 app/build/outputs/apk/Fdroid/release/*.apk 2>/dev/null || true
