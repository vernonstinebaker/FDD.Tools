#!/bin/bash
set -euo pipefail

# macOS native app packaging script for FDD Tools
# Requires: Java 21+, jpackage (included with JDK), Maven build done (shade jar present)
# Reads configuration from etc/macos-app-config.properties

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
CONFIG_FILE="$ROOT_DIR/etc/macos-app-config.properties"
DIST_DIR="$ROOT_DIR/dist/macos"
JAR="$ROOT_DIR/target/FDDTools-1.0-SNAPSHOT.jar"
ICNS="$ROOT_DIR/src/main/resources/FDDTools.icns"

if [ ! -f "$CONFIG_FILE" ]; then echo "Missing config file: $CONFIG_FILE"; exit 1; fi
if [ ! -f "$JAR" ]; then echo "Jar not built. Run: mvn -q -DskipTests package"; exit 1; fi
if [ ! -f "$ICNS" ]; then echo "Missing icns icon: $ICNS"; exit 1; fi

# Read simple key=value props and expose as env vars with dot -> underscore conversion
declare APP_NAME BUNDLE_ID VERSION_RAW VERSION VENDOR
while IFS='=' read -r rawKey rawVal; do
  [[ -z "${rawKey// }" ]] && continue
  [[ "$rawKey" =~ ^# ]] && continue
  key=$(echo "$rawKey" | sed 's/\r$//' | tr -d ' ' | tr '.' '_')
  val=$(echo "${rawVal%%$'\r'}")
  case "$key" in
    app_name) APP_NAME="$val" ;;
    app_bundleIdentifier) BUNDLE_ID="$val" ;;
    app_version) VERSION_RAW="$val" ;;
    app_vendor) VENDOR="$val" ;;
  esac
done < "$CONFIG_FILE"

# Defaults and sanitization
APP_NAME=${APP_NAME:-FDD Tools}
BUNDLE_ID=${BUNDLE_ID:-net.sourceforge.fddtools}
VERSION_RAW=${VERSION_RAW:-1.0.0}
VENDOR=${VENDOR:-FDD Tools Project}
# jpackage requires numeric x[.y[.z]]; strip common suffixes like -SNAPSHOT
VERSION=${VERSION_RAW/-SNAPSHOT/}
if [[ ! "$VERSION" =~ ^[0-9]+(\.[0-9]+){0,2}$ ]]; then VERSION=1.0.0; fi

mkdir -p "$DIST_DIR"

echo "Packaging ${APP_NAME} (${VERSION})..."

# Let jpackage generate a correct Info.plist (incl. CFBundleExecutable). Do NOT override it.
jpackage \
  --type app-image \
  --input "$ROOT_DIR/target" \
  --main-jar "$(basename "$JAR")" \
  --main-class net.sourceforge.fddtools.FDDApplicationFX \
  --name "$APP_NAME" \
  --icon "$ICNS" \
  --app-version "${VERSION}" \
  --vendor "${VENDOR}" \
  --dest "$DIST_DIR" \
  --mac-package-identifier "$BUNDLE_ID" \
  --mac-package-name "$APP_NAME"

echo "Resulting app image contents:"
find "$DIST_DIR" -maxdepth 2 -type f -print

APP_BUNDLE="${DIST_DIR}/${APP_NAME}.app"
if [ -d "$APP_BUNDLE" ]; then
  echo "Verifying CFBundleExecutable..."
  /usr/libexec/PlistBuddy -c 'Print :CFBundleExecutable' "$APP_BUNDLE/Contents/Info.plist" || true
  echo "Launcher perms:"; ls -l "$APP_BUNDLE/Contents/MacOS" || true
  # Normalize launcher name to remove spaces (Finder handles spaces, but some edge cases are brittle)
  LAUNCHER_NOSPACE=$(echo "$APP_NAME" | tr -d ' ')
  if [ "$LAUNCHER_NOSPACE" != "$APP_NAME" ]; then
    SRC_LAUNCHER="$APP_BUNDLE/Contents/MacOS/$APP_NAME"
    DST_LAUNCHER="$APP_BUNDLE/Contents/MacOS/$LAUNCHER_NOSPACE"
    if [ -f "$SRC_LAUNCHER" ]; then
      echo "Renaming launcher to remove spaces: '$APP_NAME' -> '$LAUNCHER_NOSPACE'"
      mv "$SRC_LAUNCHER" "$DST_LAUNCHER"
      chmod +x "$DST_LAUNCHER"
      /usr/libexec/PlistBuddy -c "Set :CFBundleExecutable $LAUNCHER_NOSPACE" "$APP_BUNDLE/Contents/Info.plist" || true
      # Rename the jpackage cfg to match the new launcher base name
      SRC_CFG="$APP_BUNDLE/Contents/app/$APP_NAME.cfg"
      DST_CFG="$APP_BUNDLE/Contents/app/$LAUNCHER_NOSPACE.cfg"
      if [ -f "$SRC_CFG" ]; then mv "$SRC_CFG" "$DST_CFG"; fi
      echo "Updated CFBundleExecutable and cfg filename to '$LAUNCHER_NOSPACE'"
    fi
  fi
fi

echo "Done. You can run: open '${DIST_DIR}/${APP_NAME}.app'"
