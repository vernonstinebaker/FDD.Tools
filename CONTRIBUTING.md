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
