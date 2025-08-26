#!/bin/bash

# GitHub Actions Simulation Script
# This script simulates the exact GitHub Actions environment

set -e

echo "🚀 GitHub Actions Simulation - FDD Tools"
echo "========================================"

# Check if we're in the right directory
if [ ! -f "pom.xml" ]; then
    echo "❌ Error: Please run this script from the FDD.Tools project root directory"
    exit 1
fi

# Build the GitHub Actions-like environment
echo "🔨 Building GitHub Actions simulation environment..."
docker build -f .github/test/Dockerfile.github-actions -t fdd-tools-ci .

# Create a temporary directory for the workspace
WORKSPACE_DIR="/tmp/fdd-tools-workspace-$$"
mkdir -p "$WORKSPACE_DIR"
cp -r . "$WORKSPACE_DIR/"

echo "🧪 Running in GitHub Actions-like environment..."

# Run the container with the same setup as GitHub Actions
docker run --rm -it \
    --name fdd-tools-ci-test \
    -v "$WORKSPACE_DIR:/home/runner/work/fdd-tools" \
    -w "/home/runner/work/fdd-tools" \
    -e CI=true \
    -e GITHUB_ACTIONS=true \
    -e DISPLAY=:99 \
    fdd-tools-ci bash -c "
        echo '📋 Environment Information:'
        echo 'Java Version:'
        java --version
        echo ''
        echo 'Maven Version:'
        mvn --version
        echo ''
        echo 'Environment Variables:'
        env | grep -E '(JAVA_|MAVEN_|DISPLAY|CI|GITHUB)' | sort
        echo ''
        echo '🧪 Running Tests:'
        echo '=================='
        mvn clean test -B
    "

# Cleanup
echo "🧹 Cleaning up temporary workspace..."
rm -rf "$WORKSPACE_DIR"

echo "✅ GitHub Actions simulation complete!"
