# Theming & Style Guide

This project centralizes JavaFX styling to reduce duplication and keep visual behavior consistent.

## Design Tokens (documented)

Because JavaFX CSS lacks native variables, tokens are documented (not enforced) in `global-theme.css`:

```text
ORANGE_BASE_TOP        #ffb066
ORANGE_BASE_BOTTOM     #ff8a33
ORANGE_HOVER_TOP       #ff8a33
ORANGE_HOVER_BOTTOM    #e56700
ORANGE_ACTIVE_TOP      #cc5800
ORANGE_ACTIVE_BOTTOM   #e56700
LIGHT_BG_TOP           #fcfcfc
LIGHT_BG_BOTTOM        #e9e9e9
BORDER_LIGHT           #c9c9c9
HOVER_BG_TRANSLUCENT   rgba(255,138,51,0.18)
ARMED_BG_TRANSLUCENT   rgba(255,138,51,0.32)
FOCUS_RING             rgba(255,138,51,0.6)
```

Dark & high-contrast themes adapt these token intents.

## File Roles

- `global-theme.css`: Core component + icon button styling (single source of truth for hover/armed states).
- `semantic-theme.css`: Domain / semantic containers (tree, panels, action bars) & subtle layout gradients.
- `global-theme-*.css`: Variant overlays (dark, light override set, high contrast). Only put deltas here.
- `fdd-canvas.css`: Canvas-specific layout/styling; avoids redefining global icon button states.

## Icon / Action Buttons

Use the classes:

- `fdd-icon-button` (button container base)
- `fdd-action-button` (semantic grouping if needed)
- `fdd-icon` (icon glyph fill/gradient)

Do NOT add per-screen hover overrides; adjust only in `global-theme.css`.

## Adding New Buttons

1. Create `Button b = new Button();`
2. Add icon with `FontAwesomeIconView` and style class `fdd-icon`.
3. `b.getStyleClass().addAll("fdd-action-button", "fdd-icon-button");`
4. Optional semantic text color override: add a specific class (e.g. `export-button`) and declare only `-fx-text-fill` in a screen stylesheet.

## Theme Extension Steps

1. Document any new color in the token comment block in `global-theme.css`.
2. Add adjusted values in dark/highcontrast variant files ONLY if different.
3. Prefer gradients or translucent backgrounds to solid blocks for hover states.

## High Contrast Considerations

- Ensure focus rings remain visible (see `:focused` rule in high contrast file).
- Keep sufficient contrast ratio (WCAG AA). Use the orange accent (#ff8a33) sparingly for focus / affordances.

## Cleanup Conventions

- When deprecating a style file, remove its load and delete the file rather than leaving empty placeholders.
- Avoid wildcard `.button` rules; scope via class names to prevent style bleed.

## Future Ideas

- Build a small pre-process step (optional) to validate tokens.
- Introduce a screenshot regression test harness for critical UI elements.
