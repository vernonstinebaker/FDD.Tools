#!/bin/bash

# Start virtual display for headless testing
Xvfb :99 -screen 0 1024x768x24 > /dev/null 2>&1 &

# Find and set JAVA_HOME correctly
export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
export PATH=$JAVA_HOME/bin:$PATH

# Verify environment
echo "Java version:"
java --version
echo ""
echo "Maven version:"
mvn --version
echo ""
echo "JAVA_HOME: $JAVA_HOME"
echo "DISPLAY: $DISPLAY"
echo ""

#!/bin/bash

# Source environment
source /etc/environment

# Start virtual display for headless testing
Xvfb :99 -screen 0 1024x768x24 > /dev/null 2>&1 &

# Find and set JAVA_HOME correctly and export it
export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
export PATH=$JAVA_HOME/bin:$PATH

# Verify environment
echo "Java version:"
java --version
echo ""
echo "Maven version:"
mvn --version
echo ""
echo "JAVA_HOME: $JAVA_HOME"
echo "DISPLAY: $DISPLAY"
echo ""

# Execute the passed command with environment preserved
exec "$@"
