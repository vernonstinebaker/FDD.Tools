# Dockerfile for local CI testing
# This mimics the GitHub Actions Ubuntu environment
FROM ubuntu:22.04

# Install basic dependencies
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    git \
    unzip \
    xvfb \
    && rm -rf /var/lib/apt/lists/*

# Install Java 21 and Maven in one step to avoid JAVA_HOME issues
RUN apt-get update && apt-get install -y \
    openjdk-21-jdk \
    maven \
    && rm -rf /var/lib/apt/lists/*

# Set environment variables after installation (auto-detect architecture)
RUN echo "export JAVA_HOME=\$(dirname \$(dirname \$(readlink -f \$(which java))))" >> /etc/environment
RUN echo "export PATH=\$JAVA_HOME/bin:\$PATH" >> /etc/environment

# Set up headless display (important for JavaFX testing)
ENV DISPLAY=:99
ENV JAVA_OPTS="-Djava.awt.headless=true"

# Create working directory
WORKDIR /app

# Copy the project
COPY . .

# Copy and set up entrypoint script
COPY .github/test/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Use entrypoint to set up environment
ENTRYPOINT ["/entrypoint.sh"]

# Default command - run tests
CMD ["mvn", "clean", "test"]
