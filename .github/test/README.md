# Local CI Testing Environment

This directory contains tools to test your CI pipeline locally before pushing to GitHub, which can save significant debugging time.

## Prerequisites

You'll need one of these container solutions:

### Option 1: OrbStack (Recommended for macOS)
- Download from: https://orbstack.dev/
- Faster and more efficient than Docker Desktop on macOS
- Better integration with macOS filesystem

### Option 2: Docker Desktop
- Download from: https://www.docker.com/products/docker-desktop/

### Option 3: Apple's Container Project (Experimental)
- Part of macOS development tools
- More complex setup, use OrbStack or Docker Desktop instead

## Quick Start

### Test Your Current Changes
```bash
# From the project root directory
./ci-test.sh test
```

### Interactive Development
```bash
# Get a shell in the Linux CI environment
./ci-test.sh interactive

# Inside the container, you can run:
mvn test
mvn clean test
mvn test -Dtest=SpecificTestClass
```

### GitHub Actions Simulation
```bash
# Simulate the exact GitHub Actions environment
./github-actions-sim.sh
```

## Available Commands

### ci-test.sh
- `./ci-test.sh build` - Build the CI environment
- `./ci-test.sh test` - Run full test suite in Linux
- `./ci-test.sh interactive` - Interactive shell in CI environment  
- `./ci-test.sh clean` - Clean up containers and volumes
- `./ci-test.sh logs` - Show container logs

### github-actions-sim.sh
- `./github-actions-sim.sh` - One-shot GitHub Actions simulation

## What This Tests

✅ **Headless JavaFX Environment** - Tests your HeadlessTestUtil fixes  
✅ **Linux Compatibility** - Ensures your code works on Ubuntu 22.04  
✅ **Maven Dependencies** - Verifies all dependencies resolve correctly  
✅ **Environment Variables** - Tests with CI=true, GITHUB_ACTIONS=true  
✅ **Display Issues** - Catches "Unable to open DISPLAY" errors  

## Debugging Workflow

1. **Make changes to your code**
2. **Test locally first**: `./ci-test.sh test`
3. **Fix any issues** in the local environment
4. **Test with GitHub Actions simulation**: `./github-actions-sim.sh`
5. **Push to GitHub** with confidence

## Common Issues & Solutions

### Issue: "Unable to open DISPLAY"
```bash
# Test in interactive mode:
./ci-test.sh interactive

# Inside container:
echo $DISPLAY  # Should show :99
mvn test -Dtest=DialogServiceComprehensiveTest
```

### Issue: JavaFX Platform.startup() failures
```bash
# Check if your HeadlessTestUtil is working:
./ci-test.sh interactive

# Inside container:
mvn test -Dtest=FDDApplicationFXTest -X  # Verbose output
```

### Issue: Maven dependencies
```bash
# Check dependency resolution:
./ci-test.sh interactive

# Inside container:
mvn dependency:tree
mvn clean compile
```

## Performance Tips

### Speed Up Builds
The Docker containers cache Maven dependencies between runs. First run will be slower, subsequent runs much faster.

### Live Development
Use interactive mode for iterative development:
```bash
./ci-test.sh interactive
# Container stays running, make changes on macOS
# Run tests inside container immediately
```

### Cleanup
Clean up regularly to free disk space:
```bash
./ci-test.sh clean
docker system prune  # Remove unused Docker data
```

## Environment Details

### Standard CI Environment
- **OS**: Ubuntu 22.04 LTS
- **Java**: OpenJDK 21
- **Maven**: Latest stable
- **Display**: Xvfb :99 (headless)

### GitHub Actions Simulation
- **User**: runner (like GitHub Actions)
- **Working Directory**: /home/runner/work/fdd-tools
- **Environment**: CI=true, GITHUB_ACTIONS=true
- **Memory**: 2GB Maven heap

## Files in This Directory

- `Dockerfile` - Basic CI testing environment
- `Dockerfile.github-actions` - GitHub Actions simulation
- `docker-compose.yml` - Multi-service setup with volume caching
- `../ci-test.sh` - Main testing script
- `../github-actions-sim.sh` - GitHub Actions simulation script

## Troubleshooting

### Container Won't Start
```bash
# Check Docker/OrbStack is running
docker info

# Rebuild environment
./ci-test.sh clean
./ci-test.sh build
```

### Tests Pass Locally But Fail in CI
```bash
# Use the GitHub Actions simulation
./github-actions-sim.sh

# Check for environment differences
./ci-test.sh interactive
env | grep -E '(JAVA_|MAVEN_|DISPLAY|CI)'
```

### Slow Performance
- Use OrbStack instead of Docker Desktop on macOS
- Ensure sufficient RAM allocated to Docker/OrbStack
- Clean up unused containers regularly

This setup should catch 95% of CI issues before you push to GitHub! 🎯
