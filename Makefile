# FDD Tools - Development Tasks
# Use this Makefile for common development tasks

.PHONY: help test test-local clean build dev-container

# Default target
help:
	@echo "FDD Tools Development Commands:"
	@echo ""
	@echo "  make test          - Run tests (requires Java 21 and Maven installed)"
	@echo "  make test-local    - Run tests in Docker container (no local Java needed)"
	@echo "  make build         - Build the project"
	@echo "  make clean         - Clean build artifacts"
	@echo "  make dev-container - Open project in VS Code Dev Container"
	@echo ""
	@echo "Recommended: Use 'make dev-container' for the best development experience"

# Run tests locally (requires Java 21 and Maven)
test:
	mvn clean test

# Run tests in Docker container (no local dependencies needed)
test-local:
	@echo "🐳 Running tests in Docker container..."
	@docker run --rm \
		-v $(PWD):/workspace \
		-w /workspace \
		-e DISPLAY=:99 \
		-e JAVA_OPTS="-Djava.awt.headless=true" \
		--entrypoint="" \
		mcr.microsoft.com/devcontainers/java:21-bullseye \
		bash -c "apt-get update -qq && apt-get install -y -qq xvfb maven && Xvfb :99 -screen 0 1024x768x24 > /dev/null 2>&1 & mvn clean test"

# Build the project
build:
	mvn clean compile

# Clean build artifacts
clean:
	mvn clean

# Open in VS Code Dev Container (requires VS Code with Dev Containers extension)
dev-container:
	@echo "🚀 Opening project in VS Code Dev Container..."
	@echo "Make sure you have VS Code with the 'Dev Containers' extension installed"
	@code . || echo "VS Code not found in PATH. Please open the project manually in VS Code and choose 'Reopen in Container'"
