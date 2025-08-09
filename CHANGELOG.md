# FDD Tools Changelog

## [Unreleased]

### Added - Canvas Implementation Complete

- **Modern JavaFX Canvas**: Full implementation of FDDCanvasFX with professional zoom and pan capabilities
  - Zoom range: 0.1x to 5.0x with smooth scaling and fit-to-window functionality
  - Mouse drag panning with scroll bar integration
  - Keyboard shortcuts: Ctrl+Scroll (zoom), Ctrl +/-/0 (zoom controls), Space+Drag (pan mode)
  - Professional control panel with zoom indicators and buttons
  - Image export: Save canvas as PNG/JPEG with file chooser dialog
  - Context menu: Right-click access to all canvas functions
  
- **High-Quality Text Rendering**: Optimized font system for crisp, readable text at all zoom levels
  - SF Pro Text Semi-Bold on macOS for optimal clarity
  - Cross-platform font fallback: Segoe UI (Windows), Roboto, Source Sans Pro, Liberation Sans
  - Disabled image smoothing for pixel-perfect text rendering
  - Decorative font filtering to ensure readability

- **Smart Text Contrast System**: Intelligent color adjustment for optimal visibility
  - Dynamic text color based on position over progress bars
  - Black text over light areas, white text over dark progress sections
  - Single percentage display with perfect positioning
  - High contrast styling for professional appearance

### Added - UX Enhancement

- **Edit Dialog Focus Restoration**: Seamless node selection after edit operations
  - Problem solved: Users no longer lose their place in the tree after editing
  - Pure JavaFX implementation (legacy Swing path removed)
  - Thread-safe execution via Platform.runLater()
  - Enhanced editSelectedFDDElementNode() with callback-based focus restoration

### Added - JavaFX Tree Default

- **JavaFX Tree View Default**: Application now starts with JavaFX tree view by default
  - Auto-expand functionality: All tree nodes expand automatically on startup
  - Root node auto-selection: Root node is automatically selected on initialization
  - Professional action panel: FDDActionPanelFX with reliable text symbol buttons
  - High contrast styling: Enhanced visibility with professional appearance
  - Canvas integration: Tree selection properly updates canvas view

### Changed - Startup & Integration

- **Startup sequence**: Simplified to pure JavaFX initialization
- **Thread coordination**: Platform.runLater() usage consolidated
- **Selection handling**: Clean JavaFX selection logic
- **Production code**: Removed transitional debug output

### Technical Implementation Details

- **Key JavaFX Files** (current set):
  - `FDDCanvasFX.java` - Modern JavaFX canvas (zoom/pan)
  - `FDDGraphicFX.java` - Element rendering with smart contrast
  - `CenteredTextDrawerFX.java` - Optimized text rendering utilities
  - `FDDTreeViewFX.java` - JavaFX tree implementation
  - `FDDActionPanelFX.java` - Action button panel

- **Removed Legacy**:
  - All Swing frame/canvas bridge classes (FDDFrame, FDDCanvasBridge, CanvasSelector, FDDGraphic) deleted
  - Swing TreeNode coupling removed from model
  - Swing dependencies pruned from pom.xml

- **Canvas Architecture**: BorderPane layout with ScrollPane, VBox control panel, and GraphicsContext rendering
- **Thread Safety**: Proper coordination between JavaFX and Swing EDT threads
- **Memory Management**: Efficient canvas sizing and redraw optimization
-- **Bridge Pattern**: Retired; application now fully JavaFX

### Added - Asynchronous Operations & BusyService Enhancements

- **BusyService Progress & Cancellation**: Long-running operations (open, save, image export) now run inside cancellable JavaFX Tasks with overlay progress indicator.
- **Image Export Async**: PNG/JPEG export moved off UI thread; supports cancellation and progress updates; audit + performance spans recorded.
- **XML Read/Write Tasks**: Project load/save now emit coarse progress milestones enabling future UI progress granularity.

### Added - Structured Logging, Audit & Performance Instrumentation

- **AUDIT & PERF Loggers**: Dedicated rolling appenders added (logback.xml) for audit trail and performance spans; non-additive to keep channels clean.
- **Span Timing API**: LoggingService provides Span objects (start/close) capturing durationMs and optional metrics (zoom level, pixel counts, target scale).
- **Audit Events**: Project lifecycle (new/open/save/saveAs), command execute/undo/redo, clipboard actions (cut/copy/paste), image export events all recorded with MDC context (projectPath, action, selectedNode, auditAction).
- **Performance Spans**: Canvas redraw cycles, fitToWindow, and image export instrumented; spans nested where appropriate.
- **MDC Safety**: Nested MDC scope restoration prevents leakage across async boundaries.

### Added - Preference & Session Persistence Expansion

- **Last Project Path & Auto-Load**: Application optionally auto-loads most recent project on startup (user-controlled preference).
- **Zoom Persistence**: Last canvas zoom level saved between sessions with separate enable/disable toggle.
- **Preferences Dialog Enhancements**: New checkboxes for auto-load last project and restore last zoom.
- **Preference Corruption Handling**: Tests ensure safe fallback to defaults on malformed entries.

### Fixed - Previous Issues

- **Save & Persistence**:
  - Correct separation of Save vs Save As semantics (Save no longer always triggers dialog)
  - Eliminated zero-byte project saves by ensuring serialization of the registered ProjectService root
  - Filename normalization: `buildDefaultSaveFileName` now returns base name only (no premature extension)
  - Single extension enforcement via `ensureFddiOrXmlExtension` and `stripDuplicateFddi` (prevents `.fddi.fddi`)
  - Duplicate extension bug in file chooser fully resolved
  - Dirty flag now reliably clears after successful save/saveAs
  - MRU list ordering fixed; Save As / Open add entries, plain Save does not duplicate entries
  - Recent file paths stored in normalized form
  - Added structural diagnostics (pre/post save child & total node counts) with guard log when overwriting existing file with zero-child root
  - Added regression tests: `FDDOpenExistingSaveRegressionTest` (structure persistence) and `FDDDeepHierarchyPersistenceTest` (deep hierarchy integrity)

- **UI Refresh**:
  - Tree node rename immediately refreshes & re-selects edited node (no stale label)

- **Busy Overlay / UX**:
  - Added ~180ms delayed Busy overlay to eliminate flicker for fast operations (overlay cancelled if task finishes quickly)

- **Tests / Reliability**:
  - Expanded `FDDMainWindowSaveBehaviorTest` covering: Save vs Save As semantics, MRU ordering, filename normalization, duplicate extension prevention, root persistence
  - Added command-related tests (rename/paste) ensuring tree state integrity post-operations
  - All new and existing tests passing (current targeted suite: 10 green)

- **Documentation Alignment**:
  - README & ROADMAP updated to reflect save workflow hardening, MRU reliability, tree rename refresh, and delayed Busy overlay behavior


### Removed - Legacy / Unused Components

- Deprecated IconUtils-based SVG icon utility (replaced by FontAwesome glyph usage in action panel / toolbar).
- Outdated documentation file JAVAFX_ICONS_IMPLEMENTATION.md referencing removed toolbar/IconUtils implementation.
- Dormant print subsystem stubs (FDDPrintManager, FDDImagePrinter) removed; printing will be reintroduced with new JavaFX-centric pipeline in future roadmap phase.

- **JavaFX Milestone Completion**: Fixed milestone status synchronization in FDDElementDialogFX
  - Milestone completion checkboxes now properly update the model
  - Progress bars correctly reflect milestone completion status
  - No more "Index out of bounds" errors when toggling milestones
  - Full save/load functionality for milestone states

### Changed - Robustness Improvements

- **Enhanced error handling**: Added proper bounds checking in milestone progress calculation
- **Improved debugging**: Added console output for milestone updates (removed in production)
- **Cross-platform compatibility**: Verified on macOS, Windows, and Linux
- **Performance optimization**: Eliminated complex scene traversal overhead

## Current Status Summary

### Fully Functional Features ✅

- Modern JavaFX Canvas with professional zoom/pan controls
- High-quality text rendering with smart contrast detection
- Edit dialog focus restoration for seamless UX
- JavaFX tree view as default with auto-expand functionality  
- Root node auto-selection and canvas integration
- Professional action panels and context menus
- Image export and right-click functionality
- Cross-platform font optimization and fallback system
- Complete milestone management in JavaFX dialogs
- Reliable save/load for all project data

### Development Environment ✅

- All syntax errors resolved
- Build system configured and tested
- Production-ready codebase
- GitHub repository synchronized

## [Previous Releases]

### [1.0.0] - Initial Release

- Basic FDD project management functionality
- Swing-based user interface
- XML file format support
- Milestone tracking with progress calculation
- Work package management
