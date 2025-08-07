# FDD Tools Changelog

## [Unreleased]

### Added

- **JavaFX Tree View Default**: Application now starts with JavaFX tree view by default
  - Auto-expand functionality: All tree nodes expand automatically on startup
  - Root node auto-selection: Root node is automatically selected on initialization
  - Professional action panel: FDDActionPanelFX with reliable text symbol buttons
  - High contrast styling: Enhanced visibility with professional appearance
  - Canvas integration: Tree selection properly updates canvas view

### Changed

- **Startup sequence**: Modified to use Swing tree initially, then automatically switch to JavaFX
- **Thread coordination**: Enhanced JavaFX/Swing integration using proper Platform.runLater() calls
- **Selection handling**: Improved onSelectionChanged() to support both JavaFX and Swing trees
- **Production code**: Removed all debug output for clean professional codebase

### Technical Details

- **New Files**: FDDTreeViewFX.java, FDDActionPanelFX.java
- **Enhanced Files**: FDDFrame.java with automatic JavaFX tree switching
- **Thread Safety**: Proper coordination between JavaFX and Swing EDT threads
- **TreePath Creation**: Custom implementation for JavaFX tree selections

### Fixed

- **JavaFX Milestone Completion**: Fixed milestone status synchronization in FDDElementDialogFX
  - Milestone completion checkboxes now properly update the model
  - Progress bars correctly reflect milestone completion status
  - No more "Index out of bounds" errors when toggling milestones
  - Full save/load functionality for milestone states

### Changed (Previous)

- **Enhanced robustness**: Added proper bounds checking in milestone progress calculation
- **Improved debugging**: Added console output for milestone updates

## [Previous Releases]

### [1.0.0] - Initial Release

- Basic FDD project management functionality
- Swing-based user interface
- XML file format support
- Milestone tracking with progress calculation
- Work package management
