# FDD Tools Development Roadmap (Updated Aug 15 2025)

This roadmap is intentionally concise and only tracks actionable and status‑relevant work. Historical details live in Git history & CHANGELOG.

## 1. Recently Completed (Canvas Excellence - Aug 2025)

### Canvas System Perfection

- **Perfect Fit-to-Window Algorithm**: Advanced layout calculation that eliminates all scrolling and maximizes viewport usage
- **Mac Trackpad Integration**: Native pinch-to-zoom gesture support with smooth zoom factor handling and cross-platform compatibility
- **Smart Auto-Fit on Resize**: Dynamic viewport awareness that automatically readjusts layout when window size changes
- **Dynamic Scroll Behavior**: Intelligent scroll policies based on zoom level (vertical-only at 100%, 2D when zoomed, none when fitted)
- **Professional Action Bar**: Modern zoom controls with Unicode symbols, editable percentage field, interactive slider
- **Orange Theme Consistency**: Unified hover effects (#fd7e14) and visual styling throughout the interface
- **Enhanced Cross-Platform CI**: Maven configuration improvements with platform detection for reliable builds

### Previous Core Completions  

- Core shell: `FDDApplicationFX`, `FDDMainWindowFX`, menu + toolbar with modern styling
- Tree & actions: `FDDTreeViewFX` auto-expand, drag & drop (reparent + ordered reorder) with indicators & tooltips, keyboard structural shortcuts
- Editing: `FDDElementDialogFX` decomposition (milestones & work packages), focus restoration, undo/redo command set
- Persistence UX: Recent files (MRU), divider persistence, save workflow hardening, filename normalization
- Preferences: Theme (system / light / dark / high-contrast), language, audit/perf logging toggles, zoom persistence & restore toggle, recent file limit
- Logging & diagnostics: SLF4J/Logback, audit & perf appenders, Span API, contextual audit events, preference & system property toggles
- Theming: Semantic base + variant palettes (light/dark/high-contrast/system) fully rolled out; legacy ad-hoc styles removed from tree/dialogs
- Internationalization: Live relabel infra (`I18nRegistry` + UI_LANGUAGE_CHANGED events) with smoke test
- Platform/macOS: App bundle metadata, icon pipeline, system menu (About / Preferences / Quit, Cmd+, accelerator) wired; dock icon via reflective AWT
- Undo/Redo: CommandExecutionService, generalized edit commands, work package CRUD, audit integration
- UX polish: Busy overlay delayed show (anti-flicker), feature label centering fix, tree rename immediate refresh
- Tests: Theme + language smoke test; forbidden AWT/Swing guard

## 2. In Progress

- Custom TreeCell (icons + future progress pill) – NOT STARTED

## 3. Short-Term Target Queue

1. Implement custom TreeCell (icon + extension point for progress pill)
2. Performance baseline script (synthetic large project; capture render + fit timings)
3. MRU resilience & corrupted prefs recovery test
4. Progress pill & per-node status visualization once custom cell exists

## 4. Medium Backlog

- Printing system (PrinterJob + preview dialog)
- Advanced export (PDF, SVG, DPI scaling options)
- Snapshot / visual regression harness once semantic theming stable
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

- Parameterize element rendering colors (FDDGraphicFX) via palette abstraction for theme-driven graphics
- Consolidate any remaining ad hoc enable/disable logic into property bindings
- Add unit tests for Fit algorithm + auto-fit disable on manual zoom
- Consider refactoring image export to remove final AWT dependency (optional)
- (Done) Removed obsolete `modern-style.css` after semantic theme completion

## 7. Test Coverage Gaps (Priority Order)

1. Custom TreeCell rendering (icons/progress) once implemented
2. Fit correctness (varied feature counts; ensure no unnecessary scrollbars when fitting)
3. MRU pruning & corrupted preferences recovery
4. Busy overlay timing (anti-flicker threshold)
5. Image export edge cases & palette correctness across themes

## 8. Status Summary

| Feature / Area | Status |
|----------------|--------|
| High-contrast theme & ThemeService | COMPLETE |
| Dark / Light / System switching | COMPLETE |
| Semantic stylesheet base | COMPLETE |
| Canvas Fit (column search + auto-fit) | COMPLETE |
| Logging (audit & perf w/ toggles) | COMPLETE |
| Undo/Redo core | COMPLETE |
| Theme + language smoke test | COMPLETE |
| Custom TreeCell (icons/progress) | NOT STARTED |
| Printing / Advanced export | NOT STARTED |
| Performance baseline script | NOT STARTED |

## 9. Acceptance Criteria – Semantic Theming Expansion (COMPLETE)

All met:

1. Inline style strings for migrated areas removed (tree, dialogs, panels, overlays) in favor of semantic classes
2. Tree no longer loads legacy `modern-style.css`; semantic classes + variant themes drive appearance
3. Variant stylesheets (light/dark/high-contrast/system) override semantic tokens
4. Automated theme + language smoke test added (`ThemeAndLanguageSmokeTest`)
5. Guard test prevents reintroduction of Swing/AWT compile-time deps (except reflective Mac service)
6. Mac AWT icon logic isolated reflectively in `MacOSIntegrationService`

## 10. Retired / Consolidated

Historical details: see prior roadmap versions / `CHANGELOG.md`.

---

This roadmap stays succinct: completed items appear once; active and future work is directly actionable. Historic detail: see CHANGELOG.
