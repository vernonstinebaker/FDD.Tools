# FDD Tools Changelog

## [Unreleased]

### Fixed

- **JavaFX Milestone Completion**: Fixed milestone status synchronization in FDDElementDialogFX
  - Milestone completion checkboxes now properly update the model
  - Progress bars correctly reflect milestone completion status
  - No more "Index out of bounds" errors when toggling milestones
  - Full save/load functionality for milestone states

### Changed

- **Enhanced robustness**: Added proper bounds checking in milestone progress calculation
- **Improved debugging**: Added console output for milestone updates

## [Previous Releases]

### [1.0.0] - Initial Release

- Basic FDD project management functionality
- Swing-based user interface
- XML file format support
- Milestone tracking with progress calculation
- Work package management
