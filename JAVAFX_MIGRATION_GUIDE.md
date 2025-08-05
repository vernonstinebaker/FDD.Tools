# JavaFX Migration Guide for FDD Tools

## Overview

This guide outlines the incremental migration strategy from Swing to JavaFX for the FDD Tools application.

## Migration Principles

1. **Incremental Approach**: Migrate components one at a time, starting with leaf components
2. **Backward Compatibility**: Maintain functionality throughout the migration
3. **Bridge Pattern**: Use SwingNode and JFXPanel for interoperability
4. **Test Continuously**: Ensure each migrated component works before moving to the next

## Component Migration Order

### Phase 1: Foundation (Current)

- ✅ Added JavaFX dependencies to pom.xml
- ✅ Created SwingFXBridge utility class
- ✅ Created DialogBridge for dialog migration
- ✅ Implemented AboutDialogFX as proof of concept

### Phase 2: Dialogs (Priority: High, Complexity: Low)

- [x] AboutDialog → AboutDialogFX ✅ (Completed)
- [ ] FDDElementDialog → FDDElementDialogFX
- [ ] FDDOptionView → FDDOptionViewFX
- [ ] File dialogs → JavaFX FileChooser

### Phase 3: Custom Components (Priority: Medium, Complexity: Medium)

- [ ] FDDCanvasView → FDDCanvasFX (using Canvas)
- [ ] FDDGraphic → FDDGraphicFX
- [ ] CenteredTextDrawer → Use JavaFX text rendering

### Phase 4: Panels (Priority: Medium, Complexity: Medium)

- [ ] AspectInfoPanel → AspectInfoPanelFX
- [ ] WorkPackagePanel → WorkPackagePanelFX
- [ ] Tree view (JTree → TreeView)

### Phase 5: Main Frame (Priority: Low, Complexity: High)

- [ ] FDDFrame → FDDFrameFX
- [ ] Menu system migration
- [ ] Toolbar migration
- [ ] Status bar migration

### Phase 6: Application Entry Point

- [ ] Main class migration
- [ ] Complete removal of Swing dependencies

## Usage Examples

### Using the Bridge to Show JavaFX Dialog from Swing

```java
// In FDDFrame.java, replace the About dialog call:
// Old Swing way:
// new AboutDialog(this).setVisible(true);

// New JavaFX way (implemented):
DialogBridge.showAboutDialog(this);
```

### Implementation Notes

#### AboutDialog Migration (Completed)

1. Created `AboutDialogFX` in the `ui.fx` package with JavaFX components
2. Updated `FDDFrame.about()` method to use `DialogBridge.showAboutDialog()`
3. Marked original `AboutDialog` as `@Deprecated`
4. macOS integration automatically uses the new JavaFX dialog

### Embedding JavaFX Content in Swing

```java
// Create JavaFX content in a Swing panel
JFXPanel jfxPanel = SwingFXBridge.createJFXPanel(() -> {
    Button button = new Button("JavaFX Button");
    Scene scene = new Scene(new StackPane(button), 300, 200);
    jfxPanel.setScene(scene);
});

// Add to Swing container
swingPanel.add(jfxPanel);
```

### Embedding Swing Content in JavaFX

```java
// Embed existing Swing component in JavaFX
JPanel swingPanel = new MySwingPanel();
SwingNode swingNode = SwingFXBridge.embedSwingComponent(swingPanel);

// Add to JavaFX container
BorderPane root = new BorderPane();
root.setCenter(swingNode);
```

## Best Practices

1. **Threading**: Always use appropriate thread for UI updates
   - Swing: `SwingUtilities.invokeLater()`
   - JavaFX: `Platform.runLater()`
   - Use bridge methods for cross-thread operations

2. **Styling**: Prepare for CSS-based styling in JavaFX
   - Create stylesheets for consistent look
   - Map Swing Look & Feel concepts to JavaFX themes

3. **Data Binding**: Consider using JavaFX properties
   - More powerful than Swing's property change support
   - Built-in observable collections

4. **Testing**: Create parallel tests
   - Keep Swing tests until migration complete
   - Add JavaFX tests for new components

## Common Pitfalls and Solutions

### Problem: JavaFX not initialized

**Solution**: Always call `SwingFXBridge.initializeJavaFX()` before using JavaFX components

### Problem: Thread conflicts

**Solution**: Use bridge utility methods for thread-safe operations

### Problem: Different event models

**Solution**: Create adapter classes to translate between Swing and JavaFX events

### Problem: Layout differences

**Solution**: Use JavaFX layout panes that match Swing behavior (GridPane for GridLayout, etc.)

## Migration Checklist for Each Component

- [ ] Create JavaFX version in `ui.fx` package
- [ ] Create bridge/adapter if needed
- [ ] Update calling code to use new component
- [ ] Test thoroughly
- [ ] Update documentation
- [ ] Mark old Swing component as deprecated
- [ ] Remove Swing component after full migration

## Resources

- [JavaFX Documentation](https://openjfx.io/)
- [JavaFX CSS Reference](https://openjfx.io/javadoc/21/javafx.graphics/javafx/scene/doc-files/cssref.html)
- [Swing to JavaFX Migration Guide](https://docs.oracle.com/javafx/2/swing/swing-fx-interoperability.htm)
