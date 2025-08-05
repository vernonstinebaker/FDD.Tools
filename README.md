# FDD Tools

A Feature-Driven Development (FDD) project management tool built with Java.

## Overview

FDD Tools is a desktop application that helps teams manage Feature-Driven Development projects. It provides visualization and management capabilities for FDD hierarchies including Programs, Projects, Aspects, Subjects, Activities, and Features.

## Technology Stack

- **Java 21** (Latest LTS version)
- **Swing** UI framework (legacy, consider migrating to JavaFX)
- **Maven** for build management
- **JAXB/Jakarta XML Bind** for XML processing
- **OpenCSV** for CSV file handling

## Building the Project

### Prerequisites

- Java 21 or higher
- Maven 3.8 or higher

### Build Commands

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package as JAR
mvn package

# Run the application
java -jar target/FDDTools-1.0-SNAPSHOT.jar
```

## Project Structure

```text
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── net/sourceforge/fddtools/
│   │   │       ├── Main.java              # Application entry point
│   │   │       ├── ui/                    # UI components
│   │   │       ├── model/                 # Data models
│   │   │       ├── persistence/           # File I/O
│   │   │       └── util/                  # Utility classes
│   │   └── resources/
│   │       └── messages*.properties      # Internationalization
│   └── test/
│       └── java/                         # Unit tests
└── pom.xml                               # Maven configuration
```

## Current State

### ✅ What Works

- Application compiles and runs with Java 21
- All original functionality is preserved
- Maven build system is properly configured
- XML processing with Jakarta XML Bind
- CSV import/export functionality
- Internationalization support

### ⚠️ Technical Debt

- **UI Framework**: Still using Swing (consider JavaFX migration)
- **Legacy Dependencies**: SwingX (2013) and JDesktop Beansbinding (2008) are outdated
- **macOS Integration**: Using deprecated Apple EAWT APIs
- **Testing**: No unit tests present
- **Documentation**: Limited JavaDoc coverage
- **Logging**: Using java.util.logging instead of modern frameworks

### 🔄 Recommended Improvements

1. **Migrate UI to JavaFX** for modern look and feel
2. **Add comprehensive unit tests** with JUnit 5
3. **Replace legacy dependencies** with modern alternatives
4. **Implement proper logging** with SLF4J/Logback
5. **Add CI/CD pipeline** configuration
6. **Improve error handling** and validation
7. **Add data persistence layer** (JPA/Hibernate)
8. **Update to Apache License 2.0**

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project uses the Apache License 1.1. See individual file headers for details.

## Future Roadmap

- [ ] JavaFX migration for modern UI
- [ ] Add REST API for web integration
- [ ] Cloud storage support
- [ ] Real-time collaboration features
- [ ] Enhanced reporting and analytics
- [ ] Docker containerization
- [ ] Microservices architecture
