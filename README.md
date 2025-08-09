# FDD Tools

A modern Feature-Driven Development (FDD) project management tool built with Java and JavaFX.

## Overview

FDD Tools is a desktop application that helps teams manage Feature-Driven Development projects. It provides advanced visualization and management capabilities for FDD hierarchies including Programs, Projects, Aspects, Subjects, Activities, and Features. The application features a modern JavaFX interface with professional zoom/pan canvas capabilities and seamless user experience.

## Technology Stack

- **Java 21** (Latest LTS version)
- **JavaFX 21** - Modern UI framework with canvas capabilities
- **Maven** for build management
- **JAXB/Jakarta XML Bind** for XML processing
- **OpenCSV** for CSV file handling
- **SLF4J + Logback (in progress migration)** – replacing java.util.logging for structured logging

## Key Features

### ✅ Modern JavaFX Interface

- **Professional Canvas**: Zoom (0.1x–5.0x) and pan capabilities with mouse/keyboard controls
- **High-Quality Rendering**: SF Pro Text font with smart contrast detection  
- **Interactive Controls**: Zoom panel, context menus, and keyboard shortcuts
- **Image Export**: Save canvas as PNG/JPEG with file chooser dialog

### ✅ Advanced Tree & Persistence Management

- **JavaFX Tree View**: Default interface with auto-expand & orange accent theming
- **Smart Selection**: Automatic root node selection and focus restoration
- **Context Menus**: Right-click operations for all node types (styled)
- **Recent Files (MRU)**: Persistent list of recently opened projects
- **Layout Persistence**: SplitPane divider positions remembered across sessions
- **Centralized Project Service**: `ProjectService` manages current root, file path, and dirty state
- **Centralized Dialog Handling**: `DialogService` unifies error, confirmation, about & preferences dialogs
- **Background Task Overlay**: `BusyService` provides non-blocking async open/save with visual overlay (open/save now wrapped; further IO to follow)
- **Cross-Platform**: Consistent behavior across macOS, Windows, and Linux

### ✅ Enhanced User Experience

- **Focus Restoration**: Maintains node selection after edit operations
- **Busy Overlay**: Prevents interaction during long-running IO tasks
- **Seamless Dialogs**: JavaFX dialogs with proper modal behavior (legacy About / Preferences now routed through DialogService)
- **Professional Styling**: High contrast design with modern appearance
- **Responsive Layout**: Declarative property bindings for menu enablement (selection, clipboard, undo/redo, save state)

### ✅ Data Management & Editing

- **Complete Milestone System**: Progress tracking with visual indicators
- **Work Package Management**: Feature assignment and organization (undoable)
- **Work Package Commands**: Add / Delete / Rename operations fully undoable
- **XML Project Files**: Reliable save/load with validation
- **Internationalization**: Multi-language support
- **Reserved Initials Band**: Stable feature box layout regardless of owner text
- **Snapshot-Based Editing**: Generalized EditNodeCommand captures name, prefix, owner initials, milestone statuses, and work package assignment for robust undo/redo

### ✅ Undo / Redo Foundation

- **Command Stack**: Reversible operations (Add Child, Delete Node, Paste Node, Generalized Edit, Work Package CRUD)
- **Rich Snapshots**: Multi-field snapshots (name/prefix/owner/milestones/work package)
- **Live Status Binding**: Status bar labels auto-bind to next undo/redo descriptions
- **Extensible Pattern**: Ready for future structural operations (drag/drop, bulk edits, preference changes)

### ✅ New & Recently Added (Aug 2025)

- **Property Binding Migration**: Menus & actions now declaratively bound (Undo/Redo, Save, clipboard, selection). Remaining: some panel buttons.
- **Dialog Centralization**: About & Preferences now served by `DialogService` (legacy methods removed/redirected).
- **Background IO Foundation**: `BusyService` + async Task wrapping of open/save to prevent UI stalls.
- **Work Package Command Tests**: Added regression coverage for add/rename/delete + undo/redo.
- **Project Service Tests**: Validates property transitions (hasProject / hasPath) and dirty clearing.
- **Busy Service Tests**: Verifies async callbacks (success & failure) on FX thread.

## Building and Running

FDD Tools uses a clean, best-practices approach for deployment:

### Building the Application

```bash
mvn clean package
```

This creates a self-contained "fat JAR" at `target/FDDTools-1.0-SNAPSHOT.jar` that includes:

- All JavaFX dependencies
- All required libraries
- Proper macOS Desktop API integration
- Application icon and resources

### Running the Application

#### Direct execution

```bash
java -jar target/FDDTools-1.0-SNAPSHOT.jar
```

#### macOS Integration

The application properly integrates with macOS using the Desktop API:

- (In progress) Ensure application name shows as "FDD Tools" in system menu (currently may display "java"; pending bundle metadata refinement)
- **System Menu Bar**: Application menus appear in the macOS system menu bar at the top of the screen (not in the window)
- **Custom Dock Icon**: Displays the FDD Tools icon in the dock (not the generic Java icon)
- Handles macOS About, Preferences, and Quit menu items
- Follows macOS UI guidelines

**Technical Implementation:**

- Uses `MenuBar.setUseSystemMenuBar(true)` for proper menu integration
- Multiple icon sizes (16x16, 32x32, 64x64, 128x128) for optimal dock display
- AWT Taskbar API for reliable dock icon setting
- Early property configuration for consistent macOS behavior

### Development

For development, you can run directly through Maven:

```bash
mvn javafx:run
```

### Architecture

- **Fat JAR**: Single executable JAR containing all dependencies
- **Desktop API**: Native macOS integration without external scripts
- **JavaFX 21**: Modern UI framework with high-DPI support
- **Maven**: Standard build system following best practices

No shell scripts, no complex bundling - just a clean, professional executable JAR.

## Project Structure

```text
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── net/sourceforge/fddtools/
│   │   │       ├── Main.java              # Application entry point
│   │   │       ├── ui/                    # UI components (JavaFX only)
│   │   │       │   ├── fx/                # JavaFX components
│   │   │       │   │   ├── FDDCanvasFX.java      # Modern canvas
│   │   │       │   │   ├── FDDTreeViewFX.java    # JavaFX tree
│   │   │       │   │   ├── FDDElementDialogFX.java # Edit dialogs
│   │   │       │   │   └── FDDGraphicFX.java     # Element rendering
│   │   │       │   └── bridge/            # (Removed legacy bridge)
│   │   │       ├── model/                 # Data models
│   │   │       ├── persistence/           # File I/O
│   │   │       └── util/                  # Utility classes
│   │   └── resources/
│   │       └── messages*.properties      # Internationalization
│   └── test/
│       └── java/                         # Unit tests
└── pom.xml                               # Maven configuration
```

## Current Status

### ✅ Fully Implemented Features

- **Modern JavaFX Canvas**: Professional zoom/pan with 0.1x-5.0x range, image export, context menus
- **High-Quality Rendering**: SF Pro Text font, smart contrast detection, pixel-perfect text
- **JavaFX Tree Interface**: Default tree with auto-expand, focus restoration, context menus, orange accent theme
- **Enhanced UX**: Seamless edit dialogs, node selection restoration, professional styling
- **Complete Data Management**: Milestone tracking, work packages, XML persistence, MRU & layout prefs
- **Cross-Platform Support**: Verified on macOS, Windows, and Linux
- **Undo / Redo Foundation**: Command stack with generalized multi-field edit support + Work Package CRUD
- **Central Services**: ProjectService, DialogService, BusyService integrated; menu enablement now binding-driven
- **Async IO (Phase 1)**: Open / Save migrated to background tasks with overlay

### ✅ Technical Excellence

- **Java 21 Compatibility**: Modern language features and performance
- **Pure JavaFX Implementation**: Swing code & dependencies removed
- **Maven Build System**: Reliable dependency management and build process
- **Growing Test Suite**: Commands, preferences, layout, recent files, project service, busy service
- **Foundation for Structured Logging**: SLF4J + Logback dependency present; migration underway

### 🔄 In Progress / Near-Term

- Structured logging migration (replace java.util.logging with SLF4J + Logback config)
- Event bus / lightweight model event dispatch
- Complete action panel binding conversion
- Externalize remaining hard-coded UI strings
- Additional async wrapping (import/export, future print)

### 🔄 Optional / Upcoming Enhancements

1. **Print Functionality**: Canvas printing (PrinterJob integration)
2. **Extended Export Options**: PDF / SVG / multi-resolution assets
3. **Extended Undo / Redo Coverage**: Drag/drop, preference changes, bulk operations
4. **Advanced Zoom Presets**: Predefined levels & fit heuristics
5. **Performance Optimization**: Incremental redraw / dirty regions for very large hierarchies
6. **macOS Name Finalization**: Bundle / Info.plist packaging adjustments
7. **Universal Dialog Centering**: Apply to all dialogs (nearly complete)

## Usage

### Getting Started

1. **Launch Application**: Run via Maven or JAR file
2. **Create New Project**: File → New to start a new FDD project
3. **Add Elements**: Use context menus or action buttons to add Programs, Projects, etc.
4. **Edit Properties**: Double-click or right-click → Edit to modify elements
5. **Visual Management**: Use the canvas for visual project overview with zoom/pan
6. **Save Project**: File → Save to persist your work as XML

### Canvas Controls

- **Zoom**: Ctrl+Scroll wheel, Ctrl +/-, or zoom panel buttons
- **Pan**: Mouse drag or use scroll bars
- **Fit to Window**: Use Fit to Window button or context menu
- **Export**: Right-click → Save as Image or use canvas controls
- **Context Menu**: Right-click for zoom, export, and view options

### Tree Operations

- **Navigation**: Click nodes to select, tree auto-expands on startup
- **Context Actions**: Right-click for Add, Edit, Delete operations
- **Focus Restoration**: Edit operations maintain your selection automatically
- **Keyboard**: Use arrow keys and Enter for navigation

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project uses the Apache License 1.1. See individual file headers for details.

## Development Roadmap

### Completed ✅

- [x] JavaFX Canvas with zoom/pan capabilities
- [x] Modern tree interface with auto-expand
- [x] High-quality text rendering and smart contrast
- [x] Edit dialog focus restoration
- [x] Cross-platform font optimization
- [x] Professional UI styling and user experience
- [x] Undo / redo foundation (command stack + generalized snapshot editing)

### Future Opportunities 🔮

- [ ] Print functionality (canvas pages & preview)
- [ ] PDF / SVG export capabilities  
- [ ] Extended undo / redo coverage (drag & drop, work package panel edits, preferences, bulk operations)
- [ ] Advanced zoom presets and fit strategies
- [ ] Non-blocking notification toasts
- [ ] Cloud storage integration
- [ ] Real-time collaboration features
- [ ] REST API for web integration
- [ ] Enhanced reporting and analytics
- [ ] Dark / high-contrast theme variants
