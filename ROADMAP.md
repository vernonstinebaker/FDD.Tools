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

This roadmap stays succinct: completed items appear once; active and future work is directly actionable. Historic detail: see CHANGELOG.
