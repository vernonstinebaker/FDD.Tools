# FDD Tools

Last Updated: 2025-08-28

A modern Feature-Driven Development (FDD) project management tool built with Java and JavaFX.

Legacy design/migration documents have been relocated to archive/legacy-docs for repository cleanliness.

## Overview

FDD ## Quick smoke te## Project Structuret Structureuild: `mvn -DskipTests -Pmacos-app-image verify`
2. Launch via Finder (double‑click) or run `.../Contents/MacOS/FDDTools`.
3. Check:

- Window opens; menu bar shows About/Preferences/Quit
- Preferences dialog toggles save
- New Project and Save/Save As work
- Quit exits cleanly and writes shutdown log

## Troubleshooting (packaging)

- Icon appears briefly then quits:
  - Run `.../Contents/MacOS/FDDTools` from a terminal to see console output.
  - Ensure you built with `-Pmacos-app-image verify` so the embedded runtime includes required modules.
- "Missing JavaFX application class ..." on launch:
  - Often indicates a missing base module (e.g., `java.naming` used by Logback). Rebuild with the macOS profile; it includes the right modules.
- Native access warning:
  - The packaged app already sets `--enable-native-access=javafx.graphics`. For plain `java -jar`, add this option if you want to suppress the warning.nsive desktop application that helps teams manage Feature-Driven Development projects. It provides visualization and management capabilities for FDD hierarchies including Programs, Projects, Aspects, Subjects, Activities, and Features. The application features a professional JavaFX interface with advanced canvas zoom/pan capabilities, perfect fit-to-window functionality, and a modern, responsive user experience.

## Technology Stack

- **Java 21** (Latest LTS version)
- **JavaFX 22.0.1** - Modern UI framework with advanced canvas capabilities
- **Maven** for build management and cross-platform packaging
- **JAXB/Jakarta XML Bind** for XML processing
- **OpenCSV** for CSV file handling
- **SLF4J + Logback (complete)** – unified structured logging with MDC (projectPath, selectedNode, action)

## Key Features

### ✅ Advanced Canvas System

- **Professional Zoom System**: Zoom range 0.1x–5.0x with smooth scaling and consistent layout
- **Perfect Fit-to-Window**: Intelligent layout optimization that maximizes viewport usage with zero scrolling
- **Dynamic Window Responsiveness**: Fit mode automatically readjusts when window size changes
- **Cross-Platform Gesture Support**: Mac trackpad pinch-to-zoom integration with fallback for other platforms
- **Smart Scrolling**: Dynamic scroll policies - vertical-only at 100% zoom, 2D scrolling when zoomed
- **Keyboard Shortcuts**: Ctrl+Plus/Minus/0 for zoom controls, Space for accessibility
- **High-Quality Rendering**: Pixel-perfect text rendering with disabled image smoothing
- **Professional Action Bar**: Modern zoom controls with interactive percentage field and functional slider

### ✅ Modern User Interface

- **Interactive Zoom Controls**: Editable zoom percentage field and responsive slider with orange theme
- **Consistent Visual Design**: Orange theme throughout with unified hover effects (#fd7e14)
- **Clear Button Labels**: Save Image, Print, Fit buttons with proper icons and text
- **Responsive Layout**: Action bar adapts to window size changes
- **Cross-Platform Native Integration**: macOS system menu bar, dock icon, and native gesture support

### ✅ Advanced Tree & Persistence Management

- **JavaFX Tree View**: Default interface with auto-expand & orange accent theming
- **Smart Selection**: Automatic root node selection and focus restoration
- **Context Menus**: Right-click operations for all node types (styled)
- **Recent Files (MRU)**: Persistent list of recently opened projects
- **Layout Persistence**: SplitPane divider positions remembered across sessions
- **Search Functionality**: Find tree nodes with forward/backward navigation and highlighting
- **Navigation History**: Back/forward buttons for seamless project exploration
- **Click-to-Focus**: Click on canvas elements to focus corresponding tree nodes
- **Enhanced Tree Actions**: Action bar integrated within tree view for better UX
- **Centralized Project Service**: `ProjectService` manages current root, file path, and dirty state
- **Centralized Dialog Handling**: `DialogService` unifies error, confirmation, about & preferences dialogs
- **Background Task Overlay**: `BusyService` provides non-blocking async open/save with visual overlay (open/save now wrapped; further IO to follow)
- **Cross-Platform**: Consistent behavior across macOS, Windows, and Linux

### ✅ Enhanced User Experience

- **Focus Restoration**: Maintains node selection after edit operations
- **Busy Overlay**: Prevents interaction during long-running IO tasks
- **Seamless Dialogs**: JavaFX dialogs with proper modal behavior (legacy About / Preferences now routed through DialogService)
- **Styling**: High contrast design with semantic classes
- **Responsive Layout**: Declarative property bindings for menu enablement (selection, clipboard, undo/redo, save state)

### ✅ Data Management & Editing

- **Complete Milestone System**: Progress tracking with visual indicators
- **Work Package Management**: Feature assignment and organization (undoable)
- **Work Package Commands**: Add / Delete / Rename operations fully undoable
- **XML Project Files**: Reliable save/load with validation
- **Internationalization**: Multi-language support with runtime language switching (dynamic relabel; no restart required)
- **Reserved Initials Band**: Stable feature box layout regardless of owner text
- **Snapshot-Based Editing**: Generalized EditNodeCommand captures name, prefix, owner initials, milestone statuses, and work package assignment for robust undo/redo

### ✅ Undo / Redo

- Commands: add / delete / paste / edit / work package CRUD / ordered move (drag & drop)
- Rich snapshots (name/prefix/owner/milestones/work package)
- Status bar previews next undo/redo
- Extensible pattern for future operations

### ✅ Recent Highlights (Aug 2025) - Canvas Excellence & Enhanced Navigation

- **Perfect Fit-to-Window**: Advanced algorithm that calculates optimal layout and eliminates all scrolling
- **Mac Trackpad Integration**: Native pinch-to-zoom gesture support with smooth zoom factor handling
- **Smart Auto-Fit on Resize**: When in fit mode, automatically readjusts layout as window size changes
- **Dynamic Scroll Behavior**: Intelligent scroll policies based on zoom level and fit state
- **Modern Action Bar**: Professional zoom controls with Unicode symbols, interactive controls, and consistent theming
- **Orange Theme Consistency**: Unified hover effects and visual styling throughout the interface
- **Cross-Platform CI**: Enhanced Maven configuration with platform detection for reliable builds
- **Enhanced Layout Management**: Viewport-aware canvas sizing with dynamic element arrangement
- **Professional Zoom Experience**: Editable zoom field, interactive slider, and comprehensive keyboard shortcuts
- **Search & Navigation**: Tree search with forward/backward navigation, highlighting, and result persistence
- **Navigation History**: Back/forward buttons for intuitive project exploration and workflow
- **Click-to-Focus Integration**: Click canvas elements to automatically focus corresponding tree nodes
- **Improved Tree UI**: Action bar relocated within tree view for better component association
- **Enhanced CI/Testing**: Comprehensive headless testing infrastructure for reliable continuous integration

### ✅ Previous Highlights

- **Semantic Base Stylesheet**: ThemeService loads system/light/dark/high-contrast themes
- **Removed Legacy Styling**: Fully superseded by semantic + variant themes
- **Enhanced Preferences**: Theme, language, audit/perf toggles, zoom persistence with restore capability
- **Drag & Drop Excellence**: Controller extraction with ordered move, indicators, and tooltips
- **Async Image Export**: Background processing with spans & audit logging
- **Save Workflow Hardening**: Save vs Save As semantics with filename normalization
- **Recent Files & Layout Persistence**: MRU list and SplitPane divider positions with comprehensive tests
- **Advanced Logging**: Audit & perf appenders with Span API and nested MDC scopes

Full granular history: see `CHANGELOG.md`.

Detailed drag & drop behavior is documented in `CONTRIBUTING.md`.

Rules mirror creation constraints (e.g., Activities only under Subjects). Invalid attempts display a transient tooltip explaining the reason instead of failing silently.

Undo/redo fully preserves ordering: moving a node records both original and destination indices.

Implementation now delegated to `FDDTreeDragAndDropController`, keeping `FDDTreeViewFX` focused on selection, keyboard shortcuts, and context menus.

Keyboard equivalents (accessibility & power use):

- Alt+Up / Alt+Down: Move selected node earlier/later among siblings (if possible)
- Alt+Left: Move node to its grandparent (placed immediately before former parent)
- Alt+Right: Nest node under its immediate previous sibling (if hierarchy allows)

These shortcuts are non-destructive, fully undoable, and respect hierarchy rules.

### Accessibility (Planned Enhancements)

Current focus:

- High Contrast Mode toggle (enhanced cell selection + focus colors)
- Keyboard parity for all structural drag/drop operations (Alt+Arrow shortcuts)

Planned / backlog improvements:

- Role metadata via `aria-role` analogs once JavaFX exposes richer accessibility API hooks (map tree nodes to role="treeitem")
- Announce drag start, target change, and drop result through assistive technology (leveraging `AccessibleRole` and firing events)
- Provide textual description for insertion indicators (before/after) via temporary status bar message or accessibility notification region
- Focus ring refinement for high contrast mode (distinct color vs selection)
- Optional reduced-motion preference to disable drag auto-expand delay animation

Contributions welcome: see CONTRIBUTING for DnD architecture; accessibility PRs should include a short manual screen reader test note (VoiceOver / NVDA).

### Incremental Tree Updates

Tree structural operations now avoid full rebuilds:

- Each domain node maps to a `TreeItem` via an identity index (IdentityHashMap)
- `updateAfterMove` re-links the existing `TreeItem` under the new parent or reorders among siblings
- Fallback: if mapping is missing or arguments are null, a full `refresh()` occurs (selection & expansion states snapshot/restore)
- Benefits: preserves expansion state, reduces GC pressure, improves perceived responsiveness on large hierarchies

Status announcements (for future accessibility): structural moves call a `announceStatus` hook (wired externally to status bar) for screen reader narration once JavaFX exposes a richer API.

## Development Environment

### VS Code Dev Container (Recommended)

The easiest way to get started with development is using the pre-configured VS Code Dev Container:

1. **Install Prerequisites**: VS Code with the "Dev Containers" extension
2. **Open Project**: Open this project in VS Code
3. **Launch Container**: Choose "Reopen in Container" when prompted (or use Command Palette)
4. **Start Developing**: The container includes Java 21, Maven, and Xvfb for headless testing

The Dev Container provides:

- ✅ **Zero Setup**: No need to install Java or Maven locally  
- ✅ **Consistent Environment**: Same versions across all developer machines
- ✅ **Headless Testing**: Pre-configured for JavaFX testing without a display
- ✅ **VS Code Integration**: Optimized extensions and settings

### Alternative: Local Development

If you prefer local development:

```bash
# Standard Maven commands (requires Java 21 and Maven installed locally)
mvn clean test        # Run tests
mvn clean compile     # Build without tests
mvn clean package     # Create JAR file
```

### Docker Testing (No Local Java Required)

For testing without installing Java/Maven locally:

```bash
# Run tests in Docker container
docker run --rm \
  -v $(pwd):/workspace \
  -w /workspace \
  -e DISPLAY=:99 \
  -e JAVA_OPTS="-Djava.awt.headless=true" \
  mcr.microsoft.com/devcontainers/java:21-bullseye \
  bash -c "apt-get update -qq && apt-get install -y -qq xvfb maven && Xvfb :99 -screen 0 1024x768x24 > /dev/null 2>&1 & mvn clean test"
```

See [`.devcontainer/README.md`](.devcontainer/README.md) for detailed setup instructions.

## Building and Running

FDD Tools uses a clean, best-practices approach for deployment:

### Building the Application

```bash
mvn clean package
```

This creates a self-contained "fat JAR" at `target/FDDTools-${project.version}.jar` (e.g., `target/FDDTools-3.0.0-beta.jar`) that includes:

- All JavaFX dependencies
- All required libraries
- Proper macOS Desktop API integration
- Application icon and resources

### Running the Application

#### Direct JAR execution

```bash
java -jar target/FDDTools-3.0.0-beta.jar
```

#### Maven execution

```bash
mvn compile exec:java -Dexec.mainClass="net.sourceforge.fddtools.FDDApplicationFX"
```

#### Development execution

```bash
mvn javafx:run
```

#### macOS Integration

The application properly integrates with macOS using the Desktop API:

- (In progress) Ensure application name shows as "FDD Tools" in system menu (currently may display "java"; pending bundle metadata refinement)
- **System Menu Bar**: Application menus appear in the macOS system menu bar at the top of the screen (not in the window)
- **Custom Dock Icon**: Displays the FDD Tools icon in the dock (not the generic Java icon)
- Handles macOS About, Preferences, and Quit menu items
- Follows macOS UI guidelines

**Packaging / Native App Image (macOS):**

Create a native macOS .app bundle (and optional DMG) using a custom jlink runtime:

```bash
mvn -DskipTests -Pmacos-app-image verify
```

Results:

- App image: `target/dist/macos/app-image/FDD Tools.app`
- DMG installer: `target/dist/macos/FDD Tools-${project.version}.dmg` (e.g., `FDD Tools-3.0.0-beta.dmg`; use `-DskipDmg=true` to skip)

Notes:

- The embedded runtime includes: `javafx.base, javafx.graphics, javafx.controls, javafx.fxml, java.desktop, java.naming, java.prefs, java.logging, java.xml, jdk.unsupported`.
- The packaged app adds Java options: `--enable-native-access=javafx.graphics` and `--add-reads=javafx.graphics=ALL-UNNAMED`.
- jpackage input is minimal (only the main JAR) to keep `Contents/app` clean and avoid classpath confusion.

Open the app or run the launcher binary:

```bash
open "target/dist/macos/app-image/FDD Tools.app"
# or
"target/dist/macos/app-image/FDD Tools.app/Contents/MacOS/FDDTools"
```

Override application name or bundle id at build time:

```bash
mvn -DskipTests -Pmacos-app-image -Dfddtools.app.name="FDD Tools" -Dfddtools.bundle.id=net.sourceforge.fddtools verify
```

At runtime you can override the menu/dock name for plain jar runs:

```bash
java -Dfddtools.app.name="FDD Tools" -jar target/FDDTools-3.0.0-beta.jar
```

**Technical Implementation:**

- Uses `MenuBar.setUseSystemMenuBar(true)` for proper menu integration
- Multiple icon sizes (16x16, 32x32, 64x64, 128x128) for optimal dock display
- `MacOSIntegrationService` performs early property configuration and uses reflection for Taskbar icon setting (no compile-time AWT dependency in core UI classes)
- Window position/size persisted & restored via preferences service (pure JavaFX-friendly)

### Entry point

- The application entry point is `net.sourceforge.fddtools.FDDApplicationFX` (extends `javafx.application.Application`).
- `FDDMainWindowFX` is the primary UI container created in `FDDApplicationFX#start(...)` and is not an entry point.

### Development

For development, you can run directly through Maven:

```bash
mvn javafx:run
```

### Runtime Logs

Runtime logs (`logs/*.log`) are ignored by version control. Adjust retention/rotation in `logback.xml`. Do not commit generated logs.

### Architecture

- **Fat JAR**: Single executable JAR containing all dependencies
- **Desktop API**: Native macOS integration without external scripts
- **JavaFX 21**: Modern UI framework with high-DPI support
- **Maven**: Standard build system following best practices

No shell scripts, no complex bundling - single executable JAR.

## Quick smoke test (macOS bundle)

1. Build: `mvn -DskipTests -Pmacos-app-image verify`
2. Launch via Finder (double‑click) or run `.../Contents/MacOS/FDDTools`.
3. Check:

- Window opens; menu bar shows About/Preferences/Quit
- Preferences dialog toggles save
- New Project and Save/Save As work
- Quit exits cleanly and writes shutdown log

## Project Structure

```text
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── net/sourceforge/fddtools/
│   │   │       ├── Main.java              # Entry point (delegates to FDDApplicationFX)
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

- **JavaFX Canvas**: Zoom/pan with 0.1x-5.0x range, image export, context menus, click-to-focus integration
- **Rendering**: SF Pro Text font, contrast handling
- **JavaFX Tree Interface**: Default tree with auto-expand, focus restoration, context menus, orange accent theme
- **Search & Navigation**: Tree search with forward/backward navigation, highlighting, and navigation history
- **Enhanced UX**: Edit dialogs, node selection restoration, semantic styling, integrated action bars
- **Complete Data Management**: Milestone tracking, work packages, XML persistence, MRU & layout prefs
- **Cross-Platform Support**: Verified on macOS, Windows, and Linux with comprehensive CI testing
- **Undo / Redo Foundation**: Command stack with generalized multi-field edit support + Work Package CRUD
- **Central Services**: ProjectService, DialogService, BusyService integrated; menu enablement now binding-driven
- **Async IO (Phase 1)**: Open / Save migrated to background tasks with overlay

### ✅ Technical Excellence

- **Java 21 Compatibility**: Modern language features and performance
- **Pure JavaFX Implementation**: Swing code & dependencies removed
- **Maven Build System**: Reliable dependency management and build process
- **Comprehensive Test Suite**: 451 tests with headless CI infrastructure for reliable automation
- **Structured Logging**: SLF4J + Logback fully integrated (console + rolling file) with MDC keys (projectPath, selectedNode, action) across commands, selection, project lifecycle, and async tasks

### 🔄 In Progress / Near-Term

- Structured logging enhancements (optional markers, audit/perf enrichment)
- Event bus / lightweight model event dispatch
- Complete action panel binding conversion (residual buttons)
- (Done) Full UI string externalization & dynamic runtime relabel (language switching)
- Additional async wrapping (import/export; print planned)

### 🔄 Optional / Upcoming Enhancements

1. **Print Functionality**: Canvas printing (PrinterJob integration)
2. **Extended Export Options**: PDF / SVG / multi-resolution assets
3. **Extended Undo / Redo Coverage**: Drag/drop, preference changes, bulk operations
4. **Advanced Zoom Presets**: Predefined levels & fit heuristics
5. **Performance Optimization**: Incremental redraw / dirty regions for very large hierarchies (perf spans established groundwork)
6. **macOS Name Finalization**: Bundle / Info.plist packaging adjustments
7. **Universal Dialog Centering**: Apply to all dialogs (nearly complete)
8. **Live Theme Switching**: Apply light/dark without restart
9. **JSON Structured Logs** (optional parallel appender for external ingestion)

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
- **Action Bar**: Zoom / Fit / Reset / Export buttons now appear in a dedicated bar directly below the canvas (no separate right panel).

## Known Issues / Limitations

| Area | Issue | Workaround |
|------|-------|------------|
| Canvas Shrink Reflow | After expanding then narrowing the window, canvas may not immediately reduce column count (shrink path recalculation incomplete). | Temporary: tap Fit or nudge zoom (±) to force recompute; automated immediate shrink recalculation is in progress. |
| Horizontal Scroll | Horizontal scrollbar suppressed by current fit-to-width strategy; behavior under review. | Use zoom-out or Fit if cards overflow. |
| macOS App Name | Some systems may briefly show "java" before title correction. | Cosmetic; resolved with native packaging later. |
| Printing | Print not implemented. | Export image instead. |
| Dark Theme | Only light theme provided. | N/A yet. |

### Tree Operations

- **Navigation**: Click nodes to select, tree auto-expands on startup
- **Search**: Use search field to find nodes with forward/backward navigation and highlighting
- **History**: Back/forward buttons for intuitive navigation through your exploration path
- **Context Actions**: Right-click for Add, Edit, Delete operations
- **Focus Restoration**: Edit operations maintain your selection automatically
- **Canvas Integration**: Click canvas elements to focus corresponding tree nodes
- **Keyboard**: Use arrow keys and Enter for navigation

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project uses the Apache License 1.1. See individual file headers for details.

## Documentation Archive

Historical, superseded long-form migration documents have been moved under `archive/` to keep the root clean:

- `archive/JAVAFX_MIGRATION_GUIDE.md`
- `archive/PHASE_7_JAVAFX_FOUNDATION_COMPLETE.md`

Verification: See `SWING_REMOVAL_VERIFICATION.md` for confirmation of full Swing removal.

## Development Roadmap

### Completed ✅

- [x] JavaFX Canvas with zoom/pan capabilities
- [x] Modern tree interface with auto-expand and search functionality
- [x] Tree search with forward/backward navigation and highlighting
- [x] Navigation history with back/forward buttons
- [x] Click-to-focus integration between canvas and tree
- [x] Enhanced tree UI with integrated action bars
- [x] High-quality text rendering and smart contrast
- [x] Edit dialog focus restoration
- [x] Cross-platform font optimization
- [x] Semantic UI styling and user experience
- [x] Comprehensive CI infrastructure with headless testing
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
