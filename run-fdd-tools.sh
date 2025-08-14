#!/bin/bash

# Build and run FDD Tools JavaFX application with proper macOS integration
echo "Building FDD Tools..."
mvn compile

if [ $? -eq 0 ]; then
    echo "Starting FDD Tools with macOS integration..."
    java --enable-native-access=ALL-UNNAMED \
         -cp "target/classes:$(mvn -q dependency:build-classpath -Dmdep.outputFile=/dev/stdout)" \
         net.sourceforge.fddtools.FDDApplicationFX
else
    echo "Build failed. Please check the compilation errors."
    exit 1
fi
