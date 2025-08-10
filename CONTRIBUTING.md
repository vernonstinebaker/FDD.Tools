# Contributing to FDD Tools

Thank you for your interest in contributing! This guide covers workflow and quality expectations.

## Quick Start

1. Fork & clone
2. Create a branch: `git checkout -b feature/short-description`
3. Make focused commits
4. Run tests: `mvn -q test`
5. Open a Pull Request with a concise summary

## Commit Message Convention

Use prefixes:

- feat: new functionality
- fix: bug fix
- chore: build/tooling/docs/refactor no behavior change
- test: tests only
- refactor: structural change w/o behavior change
- perf: performance improvement
- docs: documentation change

Examples:

```text
feat: add JSON export option to canvas
fix: correct feature progress roll-up when milestones missing
chore: ignore runtime log files
```

## Code Style

- Java 21 features allowed when they improve clarity
- Keep methods cohesive; extract helpers instead of long blocks
- SLF4J for logging (parameterized)
- No System.out in production code

## UI & Drag & Drop Architecture

The tree view drag & drop (DnD) system is intentionally modular:

- Controller: All DnD lifecycle logic lives in `FDDTreeDragAndDropController` (derive drop type, visuals, auto-expand, command execution). Do not reintroduce DnD handlers into cells or the tree view class.
- Tree Helpers: Hierarchy / validity helpers (`hierarchyAccepts`, `isValidReparent`, `isDescendant`) remain in `FDDTreeViewFX` (package‑scope) to centralize domain rules. Extend these when adding new node types rather than duplicating checks in the controller.
- Commands: Reordering / reparenting always uses `MoveNodeCommand` (with `targetIndex` for ordered sibling insertion) to preserve undo/redo semantics.
- Visual State: Pseudo-classes applied by the controller only: `dnd-drop-valid`, `dnd-drop-invalid`, `dnd-insert-before`, `dnd-insert-after`, `dnd-hover-expanding`. Add new pseudo-classes sparingly and document in README + stylesheet.
- Auto-Expand: Hover-based expansion uses a scheduled task with a delay (constant in controller). If you tune timing, mention rationale in the CHANGELOG and consider adding a timing tolerance test.
- Tooltips / Feedback: Invalid drops surface a concise reason string from the controller. Expand `invalidReason(...)` instead of scattering tooltip logic.
- Accessibility / Keyboard: All structural edits must remain available via keyboard (Alt+Arrow moves). Any new structural operation introduced for DnD must have a keyboard analogue or be explicitly non-structural.
- Testing: Prefer unit tests on controller logic for drop classification (e.g., BEFORE/AFTER/root edge cases) plus existing command tests for model mutations. Avoid heavy UI harness unless necessary.
- Adding Features: New behaviors (e.g., multi-select drag, copy vs move) should branch inside the controller (strategy/helper methods) and emit existing commands or new command types—never mutate the model directly.

Follow-Ups (open items): incremental tree refresh optimization, controller-focused tests, selection/expansion preservation, accessibility audit for screen readers.

## Tests

Add/adjust tests for behavior changes:

- Happy path + one edge case
- Regression tests for fixed bugs

```bash
mvn -q test
```

## Documentation

- Update README for user-visible features
- Archive superseded long-form docs under `archive/`

## PR Checklist

- [ ] Tests added/updated
- [ ] All tests pass
- [ ] No new warnings
- [ ] Docs updated (if user-visible)
- [ ] Conventional commits used

## Logging & Observability

- Use MDC keys (projectPath, selectedNode, action) when meaningful
- Use perf/audit appenders—avoid ad-hoc timing prints

## Issue Reports

Include: environment, reproduction steps, expected vs actual, sanitized stack trace.

 
## License

Contributions are under the project’s existing license.

Questions? Open an issue or discussion.
