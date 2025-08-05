#!/bin/bash

# Build script for creating a native macOS application bundle

# First, ensure the JAR is built
echo "Building JAR file..."
mvn clean package

# Check if build was successful
if [ ! -f "target/FDDTools-1.0-SNAPSHOT.jar" ]; then
    echo "Error: JAR file not found. Build failed."
    exit 1
fi

# Create the macOS app bundle using jpackage
echo "Creating macOS application bundle..."
jpackage \
    --type app-image \
    --name "FDD Tools" \
    --app-version "2.0.0" \
    --vendor "FDD Tools Team" \
    --input target \
    --main-jar FDDTools-1.0-SNAPSHOT.jar \
    --main-class net.sourceforge.fddtools.Main \
    --java-options "-Xmx512m" \
    --java-options "-Dapple.awt.application.name=FDD Tools" \
    --mac-package-name "FDD Tools" \
    --mac-bundle-identifier "net.sourceforge.fddtools" \
    --dest dist

# Check if jpackage was successful
if [ -d "dist/FDD Tools.app" ]; then
    echo "Success! Application bundle created at: dist/FDD Tools.app"
    echo ""
    echo "To create a DMG installer, run:"
    echo "jpackage --type dmg --app-image 'dist/FDD Tools.app' --name 'FDD Tools' --dest dist"
else
    echo "Error: Failed to create application bundle"
    exit 1
fi