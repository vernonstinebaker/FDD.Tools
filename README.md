# FDD Tools

Last Updated: 2025-08-12

A modern Feature-Driven Development (FDD) project management tool built with Java and JavaFX.

Legacy design/migration documents have been relocated to archive/legacy-docs for repository cleanliness.

## Overview

FDD Tools is a desktop application that helps teams manage Feature-Driven Development projects. It provides visualization and management capabilities for FDD hierarchies including Programs, Projects, Aspects, Subjects, Activities, and Features. The application features a JavaFX interface with zoom/pan canvas capabilities and a consistent user experience.

## Technology Stack

- **Java 21** (Latest LTS version)
- **JavaFX 21** - Modern UI framework with canvas capabilities
- **Maven** for build management
- **JAXB/Jakarta XML Bind** for XML processing
- **OpenCSV** for CSV file handling
- **SLF4J + Logback (complete)** – unified structured logging with MDC (projectPath, selectedNode, action)

## Key Features

### ✅ JavaFX Interface

- **Canvas**: Zoom (0.1x–5.0x) and pan capabilities with mouse/keyboard controls
- **Rendering**: SF Pro Text font with contrast handling  
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

### ✅ Recent Highlights (Aug 2025)

- Column-search Fit + auto-fit on resize
- Semantic base stylesheet; ThemeService loads system/light/dark/high-contrast
- Removed legacy `modern-style.css` (fully superseded by semantic + variant themes)
- Preferences: theme, language, audit/perf toggles, zoom persistence
- Drag & drop controller extraction (ordered move, indicators, tooltips)
- Async image export with spans & audit
- Save workflow hardening (Save vs Save As semantics, filename normalization)
- Recent Files & layout persistence + tests
- Logging: audit & perf appenders, Span API, nested MDC scopes

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

**Packaging / Native App Image (macOS):**

Create a native macOS .app bundle (and optional DMG) using a custom jlink runtime:

```bash
mvn -DskipTests -Pmacos-app-image verify
```

Results:

- App image: `target/dist/macos/app-image/FDD Tools.app`
- DMG installer: `target/dist/macos/FDD Tools-1.0.0.dmg` (use `-DskipDmg=true` to skip)

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
java -Dfddtools.app.name="FDD Tools" -jar target/FDDTools-1.0-SNAPSHOT.jar
```

Shell script `scripts/package-macos.sh` remains for manual experiments, but the Maven profile is the preferred path.

**Technical Implementation:**

- Uses `MenuBar.setUseSystemMenuBar(true)` for proper menu integration
- Multiple icon sizes (16x16, 32x32, 64x64, 128x128) for optimal dock display
- `MacOSIntegrationService` performs early property configuration and uses reflection for Taskbar icon setting (no compile-time AWT dependency in core UI classes)
- Window position/size persisted & restored via `WindowBounds` (pure JavaFX-friendly)

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

## Troubleshooting (packaging)

- Icon appears briefly then quits:
	- Run `.../Contents/MacOS/FDDTools` from a terminal to see console output.
	- Ensure you built with `-Pmacos-app-image verify` so the embedded runtime includes required modules.
- “Missing JavaFX application class ...” on launch:
	- Often indicates a missing base module (e.g., `java.naming` used by Logback). Rebuild with the macOS profile; it includes the right modules.
- Native access warning:
	- The packaged app already sets `--enable-native-access=javafx.graphics`. For plain `java -jar`, add this option if you want to suppress the warning.

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

- **JavaFX Canvas**: Zoom/pan with 0.1x-5.0x range, image export, context menus
- **Rendering**: SF Pro Text font, contrast handling
- **JavaFX Tree Interface**: Default tree with auto-expand, focus restoration, context menus, orange accent theme
- **Enhanced UX**: Edit dialogs, node selection restoration, semantic styling
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

## Documentation Archive

Historical, superseded long-form migration documents have been moved under `archive/` to keep the root clean:

- `archive/JAVAFX_MIGRATION_GUIDE.md`
- `archive/PHASE_7_JAVAFX_FOUNDATION_COMPLETE.md`

Verification: See `SWING_REMOVAL_VERIFICATION.md` for confirmation of full Swing removal.

## Development Roadmap

### Completed ✅

- [x] JavaFX Canvas with zoom/pan capabilities
- [x] Modern tree interface with auto-expand
- [x] High-quality text rendering and smart contrast
- [x] Edit dialog focus restoration
- [x] Cross-platform font optimization
- [x] Semantic UI styling and user experience
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
