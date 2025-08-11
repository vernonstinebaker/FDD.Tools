# FDD Tools Development Roadmap (Audited Aug 11 2025)

This roadmap is intentionally concise and only tracks actionable and status‑relevant work. Historical details live in Git history & CHANGELOG.

## 1. Recently Completed (Post-Migration Highlights)

- Core shell: `FDDApplicationFX`, `FDDMainWindowFX`, menu + toolbar, BusyService overlay (async tasks)
- Canvas: `FDDCanvasFX` zoom system, new column-search Fit algorithm, auto-fit on resize, async image export
- Tree & actions: `FDDTreeViewFX` auto-expand, drag & drop (reparent + ordered reorder) with indicators & tooltips, keyboard structural shortcuts
- Editing: `FDDElementDialogFX` decomposition (milestones & work packages), focus restoration, undo/redo command set
- Persistence UX: Recent files (MRU), divider persistence, save workflow hardening, filename normalization
- Preferences: Theme (system / light / dark / high-contrast), language, audit/perf logging toggles, zoom persistence & restore toggle, recent file limit
- Logging & diagnostics: SLF4J/Logback, audit & perf appenders, Span API, contextual audit events, preference & system property toggles
- Theming foundation: High-contrast variant, centralized `ThemeService`, semantic base stylesheet (`semantic-theme.css`) seeded (canvas action bar + scroll pane migrated)
- Internationalization: Live relabel infra (`I18nRegistry` + UI_LANGUAGE_CHANGED events)
- Platform/macOS: App bundle metadata, icon pipeline, system menu (About / Preferences / Quit, Cmd+, accelerator) wired
- Undo/Redo: CommandExecutionService, generalized edit commands, work package CRUD, audit integration
- UX polish: Busy overlay delayed show (anti-flicker), feature label centering fix, tree rename immediate refresh

## 2. In Progress

- Semantic theme expansion (remaining canvas elements, dialogs, tree, overlays, refactor `fdd-canvas.css`, remove inline colors) – PARTIAL
- Automated theme swap smoke test – NOT STARTED
- Custom TreeCell (icons + future progress pill) – NOT STARTED

## 3. Short-Term Target Queue

1. Finish semantic class rollout & purge inline color literals
2. Theme + language swap smoke tests (stylesheet presence + representative node color / label text assertions)
3. Implement custom TreeCell (icon + extension point for progress pill)
4. Performance baseline script (synthetic large project; capture render + fit timings)
5. MRU resilience & corrupted prefs recovery test

## 4. Medium Backlog

- Printing system (PrinterJob + preview dialog)
- Advanced export (PDF, SVG, DPI scaling options)
- Snapshot / visual regression harness once semantic theming stable
- Progress pill & per-node status visualization inside custom TreeCell
- Accessibility refinements (focus ring review, high-contrast palette adjustments post-semantic rollout)
- Partial redraw / dirty-region prototype & large project (>2k features) perf stress tests
- Structured logging markers & optional JSON encoder (deferred)
- Auto-save & project snapshot/backup rotation
- Optional reintroduce panning (now disabled) as user toggle if demanded

## 5. Long-Term / Deferred

- Collaboration / sync groundwork
- Templates & project wizard; advanced search / filter
- Plugin / extension architecture
- Canvas advanced tooling (annotations, grid / ruler, mini-map) after perf + theming stability

## 6. Technical Debt & Cleanup

- Eliminate residual AWT (currently isolated in image export) – OPTIONAL
- Parameterize element rendering colors (FDDGraphicFX) via palette abstraction for theme-driven graphics
- Consolidate any remaining ad hoc enable/disable logic into property bindings
- Add unit tests for Fit algorithm + auto-fit disable on manual zoom

## 7. Test Coverage Gaps (Priority Order)

1. Theme swap smoke (system → light → dark → high-contrast) with node style assertions
2. Language relabel propagation test (publish UI_LANGUAGE_CHANGED and verify menu text updates)
3. Fit correctness (varied feature counts; ensure no unnecessary scrollbars when fitting)
4. MRU pruning & corrupted preferences recovery
5. Busy overlay timing (anti-flicker threshold)

## 8. Status Summary

| Feature / Area | Status |
|----------------|--------|
| High-contrast theme & ThemeService | COMPLETE |
| Dark / Light / System switching | COMPLETE |
| Semantic stylesheet base | PARTIAL (canvas subset) |
| Canvas Fit (column search + auto-fit) | COMPLETE |
| Logging (audit & perf w/ toggles) | COMPLETE |
| Undo/Redo core | COMPLETE |
| Custom TreeCell (icons/progress) | NOT STARTED |
| Printing / Advanced export | NOT STARTED |
| Automated theme test | NOT STARTED |
| Performance baseline script | NOT STARTED |

## 9. Acceptance Criteria – Semantic Theming Expansion

Mark COMPLETE when all are true:

1. All inline style strings (canvas, dialogs, tree, overlays) replaced by semantic classes
2. `fdd-canvas.css` rewritten to reference only semantic classes (no raw hex outside palette definitions)
3. Variant stylesheets supply full palette parity (light / dark / high-contrast)
4. Automated theme swap test passes in CI
5. Zero JavaFX CSS warnings for semantic classes

## 10. Retired / Consolidated

Removed repetitive historical lists (undo foundation, logging migration, macOS integration, high-contrast intro, busy overlay improvements). Consult prior roadmap versions or `CHANGELOG.md` for chronology if needed.

---

This roadmap stays succinct: completed items appear once; active and future work is directly actionable.

### Parity Gap Snapshot (Selected)

Concise view of the highest-impact remaining differences identified in the Swing→JavaFX Functionality Mapping. (Mapping file remains the authoritative detailed matrix.)

| Area | Swing Capability | JavaFX Current State | Gap Action |
|------|------------------|----------------------|-----------|
| Tree Interaction | Drag & Drop reorder/reparent | Implemented (reparent + ordered before/after + into, tooltips, auto-expand) | Future: per-node progress pill & advanced multi-select drag |
| Tree Visuals | Icons + (potential) progress indicators | Text-only cells | Custom TreeCell (icon + optional progress pill) |
| Tree Feedback | Inline progress per node | Absent | Integrate after custom cell baseline established |
| Preferences | Window bounds, last project path, zoom persistence (with restore toggle), auto-load last project | Core persistence present; theme & MRU size not yet exposed | Implement remaining preference fields (language/theme selector UI, MRU size), add validation & corruption recovery tests |
| Printing | Print / PDF manager | Placeholder | Implement PrinterJob pipeline + preview |
| Export Formats | PDF / (future SVG) | PNG/JPG only | Add PDF & SVG generation modules |
| Undo/Redo | N/A (desired enhancement) | Foundation implemented (add/delete/paste/edit + work package CRUD + milestone & membership snapshots) | Extend to drag/drop, preferences, panel edits |
| Action Enablement | Some dynamic binding | Manual toggles | Introduce BooleanProperty bindings |
| Model Structure | Swing TreeNode coupling | Pure JavaFX domain tree (FDDTreeNode) | ✅ Complete |
| Notifications | Basic blocking dialogs | Some alerts removed | Non-blocking toast/notification center |

Resolved former gap: Recent Files (MRU) now implemented via RecentFilesService.

### Structural / Medium-Term Targets (Post-Foundation Wave)

- [x] Abstract model away from javax.swing.tree.TreeNode (pure domain tree + adapter) ✅
- [ ] (If not completed in Immediate Focus) Migrate / formally classify remaining Swing panels (AspectInfoPanel, WorkPackagePanel)
- [ ] Production-grade printing (PrinterJob pipeline + preview)
- [ ] Automated layout & rendering snapshot regression harness
- [ ] Theme variant: optional dark mode & accessible contrast mode
- [ ] Custom TreeCell with icon + optional progress pill / color band
- [ ] Preferences persistence layer (language, theme, recent files cap, future options)

### Longer-Term Evolution (Begins After Foundation & Medium Structural Tasks)

- [ ] Extended undo/redo coverage (drag/drop, preferences, batch ops, work package table edits)
- [ ] Advanced export: PDF / SVG / multi-resolution asset set
- [ ] Performance profiling for very large projects (> 2k features) & incremental redraw pipeline
- [ ] Collaboration groundwork (storage abstraction, sync hooks)
- [ ] Plugin or extension injection points

### Prioritized Refactor & Enhancement Backlog (Aug 2025)

Ordered sequence for upcoming work (execute top-to-bottom; periodically re-evaluate after each block):

1. Refactor: Split `FDDMainWindowFX` (MenuBarBuilder, ToolBarBuilder, StatusBarPane, SplitPaneLayout, FileActions, CommandBindings)
2. Refactor: Unify `addChild` / `insertChildAt` patterns via utility helper to eliminate duplication
3. Refactor: Preserve tree expansion & selection without full rebuild (incremental move updates)
4. Refactor: Clarify move semantics (`MoveNodeCommand` → enum MoveType or split into Reparent vs Reorder commands with enriched audit)
5. Refactor: LoggingService span/withContext helper methods to reduce MDC boilerplate
6. Enhancement: Expansion & selection preservation (tests) – confirm incremental path reliability
7. Enhancement: Add audit fields for reorder (originalIndex/newIndex/moveType)
8. Enhancement: Tooltip reuse cache for DnD invalid feedback (reduce object churn)
9. Enhancement: Accessibility – add accessible text/help + Shortcuts dialog (F1 / Help menu)
10. Enhancement: Performance instrumentation for large tree operations (baseline metrics)
11. Testing: Reorder edge case tests (first↔last, multi-level, no-op moves) across all container types
12. Testing: Event-driven UI relabel & theme swap smoke tests
13. Hardening: Defensive null/self checks in hierarchy validation & drag logic
14. Hardening: Confirm drag snapshot cleanup (memory log / weak ref test)
15. UX: Shortcut discoverability hint (status bar / help icon near tree)
16. UX: Progress pill / % badge in tree cells (visual roll-up)
17. Feature (Quick Win): Inline rename (F2) with undo support
18. Feature (Quick Win): Shortcuts & Tips dialog
19. Feature (Next): Multi-select tree operations (batch move/delete) – depends on incremental move reliability
20. Completed: Extract drag & drop logic into dedicated controller (archived)
21. Completed: FDDElementDialogFX decomposition phases 1–5 (archived)

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

### ✅ JavaFX Dialog & Editing Enhancements

- ✅ AboutDialog → AboutDialogFX
- ✅ FDDElementDialog → FDDElementDialogFX (with milestone & work package integration)
- ✅ Milestone completion functionality fully working (statuses undoable)
  - ✅ Progress tracking synchronized between UI and model
  - ✅ Generalized EditNodeCommand (name/prefix/owner/milestones/work package)
  - ✅ Status bar next Undo / Redo preview labels (now property-bound)
  - ✅ Work Package Add/Delete/Rename commands with undo/redo + tests

## Deferred Feature Stream: Print / Export / Advanced UX

### Phase P1 (Deferred): Print and Export System

Will commence only after: command stack scaffold, property bindings, centralized services, and baseline tests are in place.

#### Print Functionality (Planned)

Note: Legacy AWT-based print stubs (FDDPrintManager / FDDImagePrinter) have been removed to avoid carrying obsolete patterns. A fresh JavaFX-native printing implementation (PrinterJob + layout scaling + preview) will be built when this phase begins.

- [ ] PrinterJob integration for JavaFX Canvas
- [ ] Print preview dialog (scaling, orientation, margins)
- [ ] Multi-page tiling / fit strategies
- [ ] Cross-platform validation (macOS / Windows / Linux)

#### Advanced Export (Planned)

- [ ] Enhanced image export (DPI, transparency, scaling)
- [ ] PDF export
- [ ] SVG vector export
- [ ] Batch export templates

### Phase UX+: Enhanced User Experience (Follows Print / Export)

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

Note: All core Swing components removed. Any residual panel migration tasks now optional cosmetic improvements.

### 🔮 Structural Hardening (Future)

**Timeline**: Long-term goal

#### 8.1 Panel Migration (Optional)

- [ ] Create AspectInfoPanelFX (if needed for consistency)
- [ ] Create WorkPackagePanelFX (if needed for consistency)
- [ ] Note: Current Swing panels work well with JavaFX Canvas integration

#### 8.2 Complete Application Migration

- [ ] Create FDDFrameFX as primary window (optional)
- [ ] Migrate menu system to JavaFX MenuBar (optional)
- [x] Remove remaining Swing dependencies (pruned from pom.xml)
- [ ] Final optimization and cleanup

## Current Architecture Status (Revised)

### ✅ Production Ready Features

- **Modern JavaFX Canvas**: Complete zoom/pan implementation with professional controls
- **High-Quality Rendering**: Optimized text and graphics rendering at all zoom levels
- **Seamless Integration**: FDDCanvasBridge provides perfect Swing/JavaFX coordination
- **Enhanced UX**: Focus restoration, auto-expand trees, root node selection
- **Cross-Platform**: Font fallback system and platform-specific optimizations
- **Professional Appearance**: Clean UI design with high contrast and accessibility

## Technical Debt & Improvements (Reordered for Foundation First)

### Current Focus Areas

(Moved here only if not captured in Immediate Focus list above.)

- [ ] Label centering math refinement
- [ ] Large project performance profiling (baseline BEFORE Print/Export)
- [x] Undo/redo foundation (core implemented; integration ongoing)
- [x] Tree drag & drop implementation (after property binding refactor) – COMPLETE (logic extracted to FDDTreeDragAndDropController)
- [ ] Tree node icon & progress rendering (after custom cell infra)
- [ ] Printing implementation (deferred)

### Performance Optimizations (Foundational Ordering)

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
- [x] Undo/redo structural edits foundation (add/delete/paste/edit)
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

- **Focus**: Foundation Hardening & Architectural Refactor
- **Target**: Decomposition of monolithic window, property bindings, undo/redo scaffold, preferences persistence, centralized services, baseline tests & performance metrics
- **Timeline**: After completion of immediate focus items (quality gate: green tests + documented architecture)

### Planned Release (v2.2.0)

- **Focus**: Print System and Advanced Export Features (unblocked by stable foundation)
- **Prerequisite**: v2.1.0 foundation objectives accepted

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
