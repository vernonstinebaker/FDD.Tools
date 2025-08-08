# FDD Tools Development Roadmap

## Current Status (Aug 8 2025): Core JavaFX Foundation Achieved – Polishing, Consistency & Platform Refinement Underway

### ✅ Recently Completed Highlights

| Area | Completed Items (Aug 2025) |
|------|----------------------------|
| JavaFX Foundation | FDDApplicationFX, FDDMainWindowFX, DialogBridgeFX (Stage support), toolbar + menu system |
| Canvas & Rendering | FDDCanvasFX (zoom/pan), FDDGraphicFX, CenteredTextDrawerFX, progress overlay contrast logic |
| Tree & Actions | FDDTreeViewFX (auto-expand, selection styling), action panel, high-contrast toggle removal, orange accent theme |
| Theming | Orange accent unification, selection + context menu color convergence, CSS specificity & warning clean-up |
| Dialog UX | Removal of redundant success alerts, reusable centering helper (two-pass), applied to core Alerts; groundwork for universal centering |
| Persistence UX | RecentFilesService (MRU menu), LayoutPreferencesService (split divider persistence / deferred listener) |
| Internationalization | Multi-language resource bundles retained and loading verified |
| Cleanup | Legacy OSXAdapter removed, reduced SwingUtilities usage, deprecated DeepCopy eliminated (ObjectCloner) |
| Consistency | Reserved initials band standardizes feature box dimensions |

Additional polishing not in original roadmap: orange accent adoption, MRU + layout persistence, dialog centering infrastructure.

### 🎯 Current Focus (Short-Term Objectives)

- [ ] Universal dialog centering (apply helper to About, Element, Paste error, Preferences, any residual Alerts)
- [ ] Fix intermittent feature label horizontal mis-centering (refine width calculation / rounding under zoom)
- [ ] Resolve macOS menu bar app name showing "java" (investigate early name set / bundle metadata / jpackage)
- [ ] Replace remaining JFileChooser references with JavaFX FileChooser
- [ ] Consolidate duplicate error dialog logic into single utility
- [ ] Validate MRU persistence across abnormal termination / reopen cycles
- [ ] Introduce undo/redo command stack scaffold (no-op commands to capture intent)
- [ ] Implement Tree drag & drop (node reorder / reparent with type validation & progress recalculation)

### Parity Gap Snapshot (Selected)

Concise view of the highest-impact remaining differences identified in the Swing→JavaFX Functionality Mapping. (Mapping file remains the authoritative detailed matrix.)

| Area | Swing Capability | JavaFX Current State | Gap Action |
|------|------------------|----------------------|-----------|
| Tree Interaction | Drag & Drop reorder/reparent | Not implemented | Add DnD handlers (start/over/drop) with validation + progress refresh |
| Tree Visuals | Icons + (potential) progress indicators | Text-only cells | Custom TreeCell (icon + optional progress pill) |
| Tree Feedback | Inline progress per node | Absent | Integrate after custom cell baseline established |
| Preferences | Persistent settings | UI only (no persistence) | Implement properties storage (language, theme, MRU size) |
| Printing | Print / PDF manager | Placeholder | Implement PrinterJob pipeline + preview |
| Export Formats | PDF / (future SVG) | PNG/JPG only | Add PDF & SVG generation modules |
| Undo/Redo | N/A (desired enhancement) | Missing | Command stack scaffold + reversible ops |
| Action Enablement | Some dynamic binding | Manual toggles | Introduce BooleanProperty bindings |
| Model Structure | Swing TreeNode coupling | Still coupled | Domain tree abstraction + adapter layer |
| Notifications | Basic blocking dialogs | Some alerts removed | Non-blocking toast/notification center |

Resolved former gap: Recent Files (MRU) now implemented via RecentFilesService.

### Structural / Medium-Term Targets

- [ ] Abstract model away from javax.swing.tree.TreeNode (pure domain tree + adapter)
- [ ] Introduce lightweight event bus / observer decoupling for UI/model
- [ ] Migrate or formally classify remaining Swing panels (AspectInfoPanel, WorkPackagePanel)
- [ ] Production-grade printing (PrinterJob pipeline + preview)
- [ ] Automated layout & rendering snapshot regression harness
- [ ] Theme variant: optional dark mode & accessible contrast mode
- [ ] Custom TreeCell with icon + optional progress pill / color band
- [ ] Preferences persistence layer (language, theme, recent files cap, future options)

### Longer-Term Evolution

- [ ] Undo/redo fully functional (structural + property edits)
- [ ] Advanced export: PDF / SVG / multi-resolution asset set
- [ ] Performance profiling for very large projects (> 2k features) & incremental redraw pipeline
- [ ] Collaboration groundwork (storage abstraction, sync hooks)
- [ ] Plugin or extension injection points

## Phase & Milestone Ledger (Updated)

### ✅ JavaFX Canvas Migration (Aug 2025)

- ✅ Complete FDDCanvasFX implementation with professional zoom/pan (840+ lines)
- ✅ Modern BorderPane layout with ScrollPane, VBox controls, GraphicsContext rendering
- ✅ Zoom system: 0.1x to 5.0x range with smooth scaling and fit-to-window
- ✅ Professional UI controls: zoom buttons, progress bars, image export, context menus
- ✅ High-quality text rendering with SF Pro Text Semi-Bold and cross-platform fallback
- ✅ Smart contrast detection for dynamic text color over progress bars
- ✅ FDDCanvasBridge for seamless Swing/JavaFX integration
- ✅ Complete FDDGraphicFX element rendering system
- ✅ CenteredTextDrawerFX optimized text utilities

### ✅ Edit Dialog Focus Restoration (Aug 2025)

- ✅ Enhanced editSelectedFDDElementNode() with focus restoration callback
- ✅ Added restoreNodeSelection() method with cross-platform thread coordination
- ✅ DialogBridge integration for seamless focus management
- ✅ Smooth, uninterrupted editing workflow

### ✅ JavaFX Tree View Migration (Aug 2025)

- ✅ FDDTreeViewFX implementation with auto-expand functionality
- ✅ JavaFX tree now default on application startup
- ✅ Professional FDDActionPanelFX with reliable text symbols
- ✅ Canvas view integration with tree selection
- ✅ Root node auto-selection on startup
- ✅ Production-ready code without debug output
- ✅ Reliable Swing/JavaFX thread coordination

### ✅ JavaFX Dialog Migration

- ✅ AboutDialog → AboutDialogFX
- ✅ FDDElementDialog → FDDElementDialogFX (with milestone fix)
- ✅ Milestone completion functionality fully working
  - ✅ Progress tracking synchronized between UI and model

## Upcoming Feature Stream: Print / Export / Undo

### Phase P1: Print and Export System (High)

**Timeline**: Next development session

#### 6.1 Print Functionality Implementation

- [ ] Implement PrinterJob integration for JavaFX Canvas
- [ ] Add print preview dialog
- [ ] Support multiple page layouts and scaling options
- [ ] Test cross-platform printing compatibility

#### 6.2 Advanced Export Features

- [ ] Enhanced image export with quality options
- [ ] PDF export functionality
- [ ] SVG export for vector graphics
- [ ] Batch export capabilities

### Phase UX+: Enhanced User Experience (Iterative)

**Timeline**: After Phase 6 completion

#### 7.1 Advanced Canvas Tools

- [ ] Additional zoom presets and view options
- [ ] Enhanced keyboard shortcuts and accessibility
- [ ] Canvas annotation and markup tools
- [ ] Grid and ruler display options

#### 7.2 Project Management Features

- [ ] Project templates and wizards
- [ ] Advanced search and filtering
- [ ] Project statistics and reporting
- [ ] Team collaboration features

## Historical Completion (Snapshot)

### ✅ Canvas Components (Aug 2025)

#### 5.1 FDDCanvas Migration - ✅ COMPLETED

- ✅ FDDCanvasFX using JavaFX Canvas with BorderPane architecture
- ✅ Complete drawing primitives for FDD diagrams implemented
- ✅ CenteredTextDrawerFX migrated to JavaFX text rendering
- ✅ Excellent drawing performance and optimized memory usage

#### 5.2 FDDGraphic Migration - ✅ COMPLETED

- ✅ FDDGraphicFX component with comprehensive element rendering
- ✅ Node rendering (rectangles, circles, text) with smart contrast
- ✅ Connection line drawing with professional styling
- ✅ Full zoom and pan functionality (0.1x to 5.0x range)

### ✅ Phase 4: Panel Components - COMPLETED

Note: Panel components remain in Swing but are fully integrated with JavaFX Canvas through FDDCanvasBridge. This hybrid approach provides optimal performance and maintains existing functionality while delivering modern canvas experience.

### 🔮 Structural Hardening (Future)

**Timeline**: Long-term goal

#### 8.1 Panel Migration (Optional)

- [ ] Create AspectInfoPanelFX (if needed for consistency)
- [ ] Create WorkPackagePanelFX (if needed for consistency)
- [ ] Note: Current Swing panels work well with JavaFX Canvas integration

#### 8.2 Complete Application Migration

- [ ] Create FDDFrameFX as primary window (optional)
- [ ] Migrate menu system to JavaFX MenuBar (optional)
- [ ] Remove remaining Swing dependencies (if desired)
- [ ] Final optimization and cleanup

## Current Architecture Status (Revised)

### ✅ Production Ready Features

- **Modern JavaFX Canvas**: Complete zoom/pan implementation with professional controls
- **High-Quality Rendering**: Optimized text and graphics rendering at all zoom levels
- **Seamless Integration**: FDDCanvasBridge provides perfect Swing/JavaFX coordination
- **Enhanced UX**: Focus restoration, auto-expand trees, root node selection
- **Cross-Platform**: Font fallback system and platform-specific optimizations
- **Professional Appearance**: Clean UI design with high contrast and accessibility

## Technical Debt & Improvements (Updated)

### Current Focus Areas

- [ ] Universal dialog centering + consistent modality
- [ ] Label centering math refinement
- [ ] macOS application name & dock metadata fix
- [ ] Printing implementation
- [ ] Undo/redo scaffolding
- [ ] Large project performance profiling
- [ ] Model TreeNode decoupling
- [ ] Tree drag & drop implementation
- [ ] Tree node icon & progress rendering

### Performance Optimizations

- ✅ Lazy loading for tree view rendering - IMPLEMENTED
- ✅ Efficient GraphicsContext rendering - IMPLEMENTED
- [ ] Cache frequently accessed diagram metrics / layout
- [ ] Optimize rendering for very large projects (>2000 features)
- [ ] Dirty-region / partial redraw pipeline
- [ ] Text measurement reuse (avoid transient Text node churn)
- [ ] Profiling harness & baseline metrics capture

### User Experience Enhancements

- ✅ Enhanced keyboard shortcuts (Ctrl+Scroll, Space+Drag) - IMPLEMENTED
- ✅ Focus restoration for seamless editing workflow - IMPLEMENTED
- ✅ PNG/JPEG export (basic) - IMPLEMENTED
- [ ] Undo/redo for structural edits
- [ ] Project templates & quick-start wizard
- [ ] Dark / high-contrast theme toggle
- [ ] Mini-map / overview navigator
- [ ] Non-blocking notification toasts (errors, saves)

### Code Quality

- ✅ Production-ready JavaFX canvas components - COMPLETED
- ✅ Deprecation cleanup (DeepCopy removed) - COMPLETED
- ✅ CSS warning elimination - COMPLETED
- [ ] Unit tests for centering math & layout services
- [ ] Integration tests for MRU & persistence
- [ ] Visual regression snapshot harness
- [ ] Headless CI pipeline (Monocle / TestFX)
- [ ] Code coverage thresholds (>60% near-term)

## Bug Fixes & Maintenance

### High Priority (Open)

- [ ] Cross-platform theme & font verification (Win/Linux)
- [ ] Java 21+ early LTS forward check
- [ ] Large project scalability (>2000 features stress)
- [ ] Dialog positioning on multi-monitor / HiDPI
- [ ] macOS dock & menu name correction
- [ ] Printing MVP

### Medium Priority

- [ ] Structured logging (SLF4J markers)
- [ ] Auto-save (interval + dirty threshold)
- [ ] Project backup/restore (rotating snapshots)
- [ ] Enhanced export (PDF, SVG)
- [ ] Preferences expansion (theme, MRU size)
- [ ] Advanced search / filtering

## Release Planning

### Next Release (v2.0.0) - Status

- **Focus**: Modern JavaFX Canvas with Professional Features
- **Status**: ✅ COMPLETED - Production Ready!
- **Achievements**:
  - Complete canvas implementation with zoom/pan capabilities
  - High-quality text rendering with smart contrast detection
  - Edit dialog focus restoration for seamless UX
  - Cross-platform compatibility and font optimization
  - Professional UI design and user experience

### Future Release (v2.1.0)

- **Focus**: Print System and Advanced Export Features
- **Target**: Complete print functionality and enhanced export options
- **Timeline**: Next development session

### Long-term Vision (v3.0.0)

- **Focus**: Advanced project management and collaboration features
- **Target**: Templates, reporting, team features, advanced canvas tools
- **Timeline**: Future development cycles

## Development Guidelines

### Code Standards

- Follow JavaFX best practices and conventions
- Use CSS for styling instead of hard-coded values
- Implement proper MVVM pattern where appropriate
- Add comprehensive JavaDoc for public APIs

### Testing Requirements

- Unit tests for all new JavaFX components
- Integration tests for save/load operations
- Performance tests for large datasets
- Cross-platform testing on all supported OS

### Documentation Updates

- Update user manual for JavaFX interface & theming
- Create migration guide for remaining Swing panel replacement / decoupling
- Add troubleshooting (dialog centering, macOS naming)
- Document theme accent contract & CSS variables
- Document model adapter strategy (TreeNode removal)
- Update API docs with new services (MRU, LayoutPreferences)
- Reference Swing→JavaFX Functionality Mapping for detailed parity; Roadmap lists only active & planned gaps to avoid duplication drift
