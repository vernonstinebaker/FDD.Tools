# Canvas-to-Tree Focus Integration

## Overview

This document describes the implementation of the Canvas-to-Tree focus integration feature, which allows users to click on FDD elements in the canvas to automatically focus and highlight the corresponding node in the tree view.

## Implementation Details

### 1. Canvas Click Handler Interface

Added a new interface `CanvasClickHandler` in `FDDCanvasFX.java`:

```java
public interface CanvasClickHandler {
    void onCanvasNodeClicked(FDDINode clickedNode);
}
```

This interface defines the contract for handling canvas click events and communicating with the tree view.

### 2. Mouse Event Integration

Enhanced the `setupHandlers()` method in `FDDCanvasFX.java` to detect primary mouse button clicks:

```java
canvas.setOnMouseClicked(e -> {
    if (e.getButton() == MouseButton.PRIMARY) {
        handleCanvasClick(e.getX(), e.getY());
    } else if (e.getButton() == MouseButton.SECONDARY) {
        ensureContextMenu();
        sharedContextMenu.show(canvas, e.getScreenX(), e.getScreenY());
        e.consume();
    }
});
```

### 3. Coordinate-to-Node Mapping

Implemented two key methods for mapping canvas coordinates to FDD nodes:

#### `handleCanvasClick(double canvasX, double canvasY)`
- Entry point for processing canvas clicks
- Delegates to coordinate mapping logic
- Triggers the click handler if a node is found

#### `findNodeAtCoordinates(double canvasX, double canvasY)`
- Maps screen coordinates to FDD nodes
- Mirrors the layout logic from `drawChildren()` method
- Uses the same positioning constants and logic to reverse-map clicks to elements:
  - `FRINGE_WIDTH = 20`
  - `FEATURE_ELEMENT_WIDTH = 100`
  - `FEATURE_ELEMENT_HEIGHT = 140`
  - `BORDER_WIDTH = 5`

### 4. Layout Controller Integration

Modified `FDDLayoutController.java` to wire up the Canvas-to-Tree communication:

```java
// Set up Canvas-to-Tree focus integration
canvas.setCanvasClickHandler(clickedNode -> {
    if (tree != null && clickedNode != null) {
        tree.selectNode(clickedNode, true);
    }
});
```

This creates the connection between canvas clicks and tree node selection, utilizing the existing `selectNode()` method in `FDDTreeViewFX`.

## Architecture Design

### Communication Flow

1. **User clicks** on a canvas element
2. **Mouse event handler** captures the click coordinates
3. **Coordinate mapper** identifies which FDD node was clicked
4. **Click handler** is notified with the clicked node
5. **Tree selection** is triggered via `tree.selectNode(clickedNode, true)`
6. **Tree view** scrolls to and highlights the corresponding node

### Key Design Principles

- **Separation of Concerns**: Canvas handles coordinate mapping, tree handles selection
- **Interface-based Communication**: Clean contract via `CanvasClickHandler` interface
- **Reuse Existing APIs**: Leverages existing `selectNode()` method in tree view
- **Layout Consistency**: Uses same positioning logic as drawing code to ensure accuracy

## Benefits

1. **Enhanced User Experience**: Direct manipulation of canvas elements
2. **Improved Navigation**: Quick way to locate elements in the tree structure
3. **Visual Feedback**: Tree automatically scrolls and highlights selected items
4. **Intuitive Interaction**: Natural click-to-select behavior users expect

## Technical Considerations

### Coordinate Accuracy
- The coordinate mapping precisely mirrors the `drawChildren()` layout logic
- Handles both auto-fit and fixed layout modes
- Accounts for borders, fringes, and element sizing

### Performance
- Minimal overhead: only processes primary mouse clicks
- Efficient coordinate calculation using existing layout constants
- No impact on existing canvas rendering performance

### Compatibility
- Maintains full backward compatibility
- Doesn't interfere with existing secondary-button context menus
- Preserves all existing canvas functionality

## Future Enhancements

This implementation provides the foundation for additional canvas interaction features:

1. **Multi-selection**: Could be extended to support Ctrl+click for multiple selections
2. **Hover Effects**: Could add hover highlighting before click selection
3. **Keyboard Navigation**: Could integrate with keyboard shortcuts for enhanced accessibility
4. **Custom Cursor**: Could show different cursors to indicate clickable elements

## Testing

The implementation has been verified to:
- Compile successfully without errors
- Maintain compatibility with existing functionality
- Preserve all existing mouse event handling (context menus, zoom, etc.)

The feature is ready for user testing and feedback.
