# FDD Tools Development Roadmap

## Current Status (December 2024)

### ✅ Completed Work

#### 1. macOS Integration Migration

- **Removed OSXAdapter** - Legacy Apple EAWT-based integration removed
- **Implemented ModernMacOSHandler** - Using Java 9+ Desktop API
- **Platform-specific menus** - Help→About only shows on non-macOS platforms
- **Full macOS menu integration** - About, Preferences, and Quit work correctly

#### 2. JavaFX Integration Foundation

- **Created JavaFX About Dialog** - Modern UI implementation in `AboutDialogFX.java`
- **Implemented DialogBridge** - Bridge pattern for Swing→JavaFX communication
- **Fixed AboutDialog deprecation** - Now using JavaFX version through bridge
- **JavaFX initialization** - Proper Platform.startup() handling

#### 3. Code Quality Improvements

- **Fixed Java warnings**:
  - Removed unused imports in ModernMacOSHandler and DialogBridge
  - Fixed AboutDialog deprecation warning
- **Fixed Markdown formatting**:
  - All documentation follows markdownlint standards
  - Created MARKDOWN_STYLE_GUIDE.md
  - Added .markdownlint.json configuration

### 🚧 In Progress

#### 1. JavaFX Migration (High Priority)

- **Current State**: AboutDialog migrated, foundation laid
- **Next Steps**:
  - Migrate FDDOptionView to JavaFX
  - Migrate other dialogs (File dialogs, Element dialogs)
  - Create JavaFX versions of custom components

#### 2. Deprecation Warnings

- **DeepCopy class** - Still using deprecated deep copy utility
  - Need to implement modern cloning approach
  - Consider using copy constructors or serialization

### 📋 TODO List

#### Phase 1: Complete JavaFX Dialog Migration (Priority: High)

1. **FDDOptionView** → FDDOptionViewFX
   - Convert tabbed preferences dialog
   - Maintain all existing functionality
   - Use JavaFX controls and styling

2. **File Dialogs**
   - Use JavaFX FileChooser
   - Implement through DialogBridge

3. **Element Dialogs**
   - FDDElementDialog → FDDElementDialogFX
   - Modernize UI with JavaFX controls

#### Phase 2: Address Technical Debt (Priority: Medium)

1. **Replace DeepCopy**
   - Implement modern object cloning
   - Options: Copy constructors, Cloneable, or serialization
   - Update all usages in FDDFrame

2. **Update Dependencies**
   - Review and update all Maven dependencies
   - Ensure compatibility with Java 21

3. **Code Organization**
   - Consider moving all JavaFX code to separate module
   - Improve package structure

#### Phase 3: UI Modernization (Priority: Medium)

1. **Main Window Enhancement**
   - Consider JavaFX WebView for better rendering
   - Modern toolbar with JavaFX controls
   - Improved tree view component

2. **Theming Support**
   - Implement dark mode
   - User-selectable themes
   - Modern flat UI design

#### Phase 4: Feature Enhancements (Priority: Low)

1. **Native Packaging**
   - Use jpackage for platform-specific installers
   - Code signing for macOS
   - Auto-update mechanism

2. **File Format Support**
   - Import/Export to modern formats (JSON, YAML)
   - Better Excel integration
   - Cloud storage support

3. **Collaboration Features**
   - Multi-user support
   - Change tracking
   - Comments and annotations

## Technical Considerations

### JavaFX Integration Strategy

1. **Gradual Migration** - Replace Swing components one at a time
2. **Bridge Pattern** - Use DialogBridge for all Swing→JavaFX communication
3. **Consistent Styling** - Develop JavaFX CSS theme matching current look
4. **Testing** - Ensure each migrated component works on all platforms

### Platform Support

- **Primary**: macOS, Windows, Linux
- **Java Version**: 21 (LTS)
- **JavaFX Version**: 21
- **Build System**: Maven

### Known Issues

1. **JavaFX Initialization** - Must ensure Platform is started before any FX usage
2. **Threading** - Careful management of Swing EDT and JavaFX Application Thread
3. **Native Look** - JavaFX doesn't automatically match native OS theme

## Getting Started (For Next Session)

1. **Review Current State**
   - Check `MIGRATION_COMPLETE.md` for macOS integration details
   - Review `DialogBridge.java` for JavaFX integration pattern
   - See `AboutDialogFX.java` for JavaFX dialog example

2. **Next Task: Migrate FDDOptionView**
   - Located in `src/main/java/net/sourceforge/fddtools/ui/FDDOptionView.java`
   - Create `FDDOptionViewFX.java` in `ui/fx/` package
   - Update DialogBridge to include `showOptionsDialog()`
   - Update FDDFrame.options() to use bridge

3. **Testing Checklist**
   - Test on macOS - Verify menu integration
   - Test on Windows - Ensure compatibility
   - Test on Linux - Check GTK integration
   - Verify all dialogs open/close properly
   - Check for threading issues

## Resources

### Documentation- `DESKTOP_API_MIGRATION.md` - Desktop API implementation details

- `MACOS_INTEGRATION.md` - macOS-specific integration notes
- `MARKDOWN_STYLE_GUIDE.md` - Documentation standards
- `WARNINGS_FIXED.md` - Summary of fixed warnings

### Key Classes- `ModernMacOSHandler` - macOS integration using Desktop API

- `DialogBridge` - Swing to JavaFX bridge
- `AboutDialogFX` - Example JavaFX dialog implementation
- `FDDFrame` - Main application window (contains most integration points)

## Success Metrics

- ✅ All deprecation warnings resolved
- ✅ Consistent UI across all platforms
- ✅ Modern JavaFX-based dialogs
- ✅ Clean, maintainable codebase
- ✅ Comprehensive documentation
