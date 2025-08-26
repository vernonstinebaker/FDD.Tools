# FDD Tools - Dev Container Setup

This directory contains the VS Code Dev Container configuration for the FDD Tools project.

## What is a Dev Container?

A Development Container (or Dev Container for short) allows you to use a container as a full-featured development environment. This approach provides:

- **Consistent Environment**: Same Java version, Maven, and system dependencies across all developer machines
- **Isolated Dependencies**: No need to install Java 21 or Maven locally
- **Headless Testing**: Pre-configured Xvfb for JavaFX testing without a display
- **Zero Setup**: Works out of the box with VS Code

## Usage

### Option 1: VS Code Dev Containers Extension (Recommended)

1. Install the "Dev Containers" extension in VS Code
2. Open this project in VS Code
3. When prompted (or via Command Palette: "Dev Containers: Reopen in Container"), choose to reopen in container
4. VS Code will build the container and open the project inside it
5. Run tests with: `mvn clean test`

### Option 2: Manual Docker Usage

If you prefer not to use the Dev Containers extension:

```bash
# Build the development environment
docker build -f .devcontainer/Dockerfile -t fdd-tools-dev .

# Run interactively
docker run -it --rm -v $(pwd):/workspace -w /workspace fdd-tools-dev bash

# Run tests
docker run --rm -v $(pwd):/workspace -w /workspace fdd-tools-dev mvn clean test
```

## Features

- **Java 21**: Latest LTS version with proper JAVA_HOME configuration
- **Maven**: Latest version for dependency management and testing
- **Xvfb**: Virtual display server for headless JavaFX testing
- **VS Code Extensions**: Pre-configured Java development extensions
- **Maven Cache**: Persistent Maven repository cache for faster builds

## Benefits Over Local CI Scripts

- **No Root Pollution**: All configuration contained in `.devcontainer/`
- **VS Code Integration**: Native support, no custom scripts needed
- **Industry Standard**: Dev Containers are widely adopted
- **Better Performance**: More efficient than docker-compose for development
- **Automatic Setup**: VS Code handles everything automatically

## Testing JavaFX Components

The container is pre-configured with Xvfb running on display `:99`, so JavaFX tests that require a display will work correctly:

```bash
# Tests run automatically with virtual display
mvn test

# Or run specific test
mvn test -Dtest=FDDApplicationFXTest
```

## Customization

You can modify `devcontainer.json` to:
- Add more VS Code extensions
- Install additional system packages
- Configure environment variables
- Mount additional volumes
- Forward ports for web applications

## Comparison with Previous Approach

| Aspect | Custom Scripts | Dev Container |
|--------|---------------|---------------|
| Root Directory | Cluttered with .sh files | Clean - only .devcontainer/ |
| VS Code Integration | Manual setup | Native support |
| Maintenance | Custom scripts to maintain | Industry standard |
| Learning Curve | Project-specific | Transferable skill |
| Performance | Multiple containers | Single optimized container |
| Documentation | Custom README | Standard approach |
