#!/bin/bash
# FDD Tools Test Runner
# Provides convenient commands for running different test categories

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

show_help() {
    cat << EOF
FDD Tools Test Runner

Usage: $0 [OPTION]

Options:
    all             Run all tests (default Maven behavior)
    core            Run core/business logic tests only (CI-safe, excludes UI tests)
    ui              Run UI tests only (requires display/window manager)
    ci              Run CI-safe tests (same as 'core')
    quick           Run quick smoke tests
    coverage        Run tests with coverage report
    help            Show this help message

Examples:
    $0 core         # Run all tests except problematic UI tests
    $0 ui           # Run only UI tests (for local development)
    $0 coverage     # Run tests and generate Jacoco coverage report
    $0 ci           # Same as 'core' - for CI environments

Notes:
    - 'core' and 'ci' profiles exclude FDDTreeViewStylingTest and FDDTreeViewHoverInteractionTest
      which are known to fail in headless environments
    - 'ui' profile runs only UI tests and requires a display (X11/Wayland/macOS)
    - All profiles use headless-compatible test utilities where possible
EOF
}

case "${1:-all}" in
    all)
        echo "Running all tests..."
        mvn test
        ;;
    core|ci)
        echo "Running core tests (CI-safe, excluding problematic UI tests)..."
        mvn test -Pci-safe
        ;;
    ui)
        echo "Running UI tests only..."
        mvn test -Pui-tests
        ;;
    quick)
        echo "Running quick smoke tests..."
        mvn test -Dtest="**/MainTest,**/ProjectServiceTest,**/PreferencesServiceTest"
        ;;
    coverage)
        echo "Running tests with coverage report..."
        mvn clean test jacoco:report -Pci-safe
        echo "Coverage report generated in: target/site/jacoco/index.html"
        ;;
    help)
        show_help
        ;;
    *)
        echo "Unknown option: $1"
        echo "Run '$0 help' for usage information."
        exit 1
        ;;
esac
