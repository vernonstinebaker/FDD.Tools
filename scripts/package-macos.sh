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

# shellcheck disable=SC1090
source <(grep -v '^#' "$CONFIG_FILE" | sed 's/\r$//' | awk -F= '{print "export " $1 "=" $2}')

APP_NAME="${app_name:-$app_name}" # expect 'app.name' -> app_name after source
BUNDLE_ID="${app_bundleIdentifier:-net.sourceforge.fddtools}" # 'app.bundleIdentifier'
VERSION="${app_version:-1.0-SNAPSHOT}"
VENDOR="${app_vendor:-FDD Tools Project}"

mkdir -p "$DIST_DIR"

INFO_PLIST="$DIST_DIR/Info.plist"
cat > "$INFO_PLIST" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleName</key><string>${APP_NAME}</string>
    <key>CFBundleDisplayName</key><string>${APP_NAME}</string>
    <key>CFBundleIdentifier</key><string>${BUNDLE_ID}</string>
    <key>CFBundleVersion</key><string>${VERSION}</string>
    <key>CFBundleShortVersionString</key><string>${VERSION}</string>
    <key>CFBundleIconFile</key><string>FDDTools</string>
    <key>LSMinimumSystemVersion</key><string>11.0</string>
    <key>NSHighResolutionCapable</key><true/>
</dict>
</plist>
EOF

echo "Packaging ${APP_NAME} (${VERSION})..."

jpackage \
  --type app-image \
  --input "$ROOT_DIR/target" \
  --main-jar "$(basename "$JAR")" \
  --name "$APP_NAME" \
  --icon "$ICNS" \
  --app-version "${VERSION}" \
  --vendor "${VENDOR}" \
  --dest "$DIST_DIR" \
  --java-options "-Dapple.awt.application.name=${APP_NAME}" \
  --java-options "-Dapple.awt.application.appearance=system" \
  --resource-dir "$DIST_DIR" \
  --mac-package-identifier "$BUNDLE_ID" \
  --mac-package-name "$APP_NAME"

echo "Resulting app image contents:"\nfind "$DIST_DIR" -maxdepth 2 -type f -print

echo "Done. You can run: open '${DIST_DIR}/${APP_NAME}.app'"
