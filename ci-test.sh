#!/bin/bash

# Local CI Testing Script
# This script helps you test your CI environment locally before pushing to GitHub

set -e

echo "🐳 FDD Tools - Local CI Testing Environment"
echo "==========================================="

# Check if we're in the right directory
if [ ! -f "pom.xml" ]; then
    echo "❌ Error: Please run this script from the FDD.Tools project root directory"
    exit 1
fi

# Function to check if Docker/OrbStack is running
check_docker() {
    if ! docker info >/dev/null 2>&1; then
        echo "❌ Error: Docker/OrbStack is not running"
        echo "Please start Docker Desktop or OrbStack and try again"
        exit 1
    fi
}

# Function to build the test environment
build_env() {
    echo "🔨 Building CI test environment..."
    cd .github/test
    docker-compose build ci-test
    cd ../..
    echo "✅ Environment built successfully"
}

# Function to run tests
run_tests() {
    echo "🧪 Running tests in Linux environment..."
    cd .github/test
    docker-compose run --rm test-runner
    cd ../..
}

# Function to start interactive environment
interactive() {
    echo "🖥️  Starting interactive environment..."
    echo "You can now run commands like: mvn test, mvn clean compile, etc."
    cd .github/test
    docker-compose up -d ci-test
    docker-compose exec ci-test bash
    cd ../..
}

# Function to cleanup
cleanup() {
    echo "🧹 Cleaning up..."
    cd .github/test
    docker-compose down
    docker-compose rm -f
    cd ../..
    echo "✅ Cleanup complete"
}

# Function to show logs
logs() {
    cd .github/test
    docker-compose logs ci-test
    cd ../..
}

# Main menu
case "${1:-help}" in
    "build")
        check_docker
        build_env
        ;;
    "test")
        check_docker
        build_env
        run_tests
        ;;
    "interactive"|"shell")
        check_docker
        build_env
        interactive
        ;;
    "clean"|"cleanup")
        cleanup
        ;;
    "logs")
        logs
        ;;
    "help"|*)
        echo ""
        echo "Usage: $0 [command]"
        echo ""
        echo "Commands:"
        echo "  build      - Build the CI test environment"
        echo "  test       - Run the full test suite in Linux environment"
        echo "  interactive - Start interactive shell in CI environment"
        echo "  shell      - Alias for interactive"
        echo "  logs       - Show container logs"
        echo "  clean      - Clean up containers and volumes"
        echo "  help       - Show this help message"
        echo ""
        echo "Examples:"
        echo "  $0 test                    # Run all tests"
        echo "  $0 interactive             # Get a shell to run custom commands"
        echo "  $0 build && $0 test        # Build then test"
        echo ""
        ;;
esac
