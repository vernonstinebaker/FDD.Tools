# Save Functionality Fix - Post 3.0.0 Regression

## Issue Description

After the 3.0.0 tag, the save functionality was broken. When a user:

1. Opened an existing file
2. Made changes to the project
3. Pressed Command+S (or used File > Save)

**Expected behavior**: The file should save directly to the opened file location.

**Actual behavior**: A "Save As" dialog was popping up, treating the save operation as if it were a new, unsaved project.

## Root Cause Analysis

The issue was in the `ProjectLifecycleController` class, specifically in two methods:

1. `openProjectDialog()` - Used when opening files via File > Open
2. `openRecentInternal()` - Used when opening files via Recent Files menu

Both methods were:

- Loading the file content successfully
- Updating the UI correctly
- **BUT NOT updating the ProjectService with the file path**

This meant that `ProjectService.getInstance().getAbsolutePath()` returned `null`, causing the save logic in `FDDFileActions.saveProject()` to think the project was new and trigger a "Save As" dialog instead of saving directly.

### The Problematic Code

**Before (Broken)**:

```java
private void openProjectDialog(){ 
    // ... dialog code ...
    if(f!=null){ 
        FDDINode root=(FDDINode)FDDIXMLFileReader.read(f.getAbsolutePath()); 
        host.rebuildProjectUI(root, false);  // UI updated
        // Missing: ProjectService not informed of the file path!
        // ... other code ...
    }
}
```markdown

**After (Fixed)**:

```java
private void openProjectDialog(){ 
    // ... dialog code ...
    if(f!=null){ 
        FDDINode root=(FDDINode)FDDIXMLFileReader.read(f.getAbsolutePath()); 
        // CRITICAL FIX: Update ProjectService with the opened file path
        ProjectService.getInstance().openWithRoot(f.getAbsolutePath(), root);
        host.rebuildProjectUI(root, false);
        // ... other code ...
    }
}
```markdown

## Save Logic Flow

The correct save logic in `FDDFileActions.saveProject()` is:

```java
public void saveProject() {
    String currentPath = ProjectService.getInstance().getAbsolutePath();
    if (currentPath != null) {
        saveToFile(currentPath);  // Direct save - CORRECT
    } else {
        saveProjectAs();          // Save As dialog - For new projects only
    }
}
```

When `currentPath` was `null` due to the bug, it always triggered the `saveProjectAs()` branch.

## Additional Threading Issue Fixed

During testing, I discovered a secondary issue where the `ProjectLifecycleController.saveCurrentProjectBlocking()` method could throw a `IllegalStateException: Not on FX application thread` exception when trying to show a Save As dialog from a non-JavaFX thread.

**Error**:

```java
java.lang.IllegalStateException: Not on FX application thread; currentThread = main
```

java.lang.IllegalStateException: Not on FX application thread; currentThread = main

```

This was fixed by:

1. Checking if the current thread is the JavaFX Application Thread using `Platform.isFxApplicationThread()`
2. If already on the FX thread, showing the dialog directly
3. If not on the FX thread, using `Platform.runLater()` with a `CompletableFuture` to wait for the result
4. Extracting the dialog logic into a separate `showSaveAsDialog()` method for better organization

## Files Modified

### Core Fix

- `src/main/java/net/sourceforge/fddtools/ui/fx/ProjectLifecycleController.java`
  - Fixed `openProjectDialog()` method
  - Fixed `openRecentInternal()` method
  - Both now properly call `ProjectService.getInstance().openWithRoot(path, root)`

### Test Improvements

- `src/test/java/net/sourceforge/fddtools/ui/fx/SaveAfterOpenRegressionTest.java` (NEW)
  - Comprehensive regression tests for the specific save-after-open scenario
  - Tests open→modify→save workflow
  - Tests that Save As still works correctly
  - Tests multiple save cycles after opening
  
- `src/test/java/net/sourceforge/fddtools/ui/fx/ProjectLifecycleControllerIntegrationTest.java` (NEW)
  - Integration tests for ProjectLifecycleController
  - Tests `openSpecificRecent()` functionality
  - Tests save behavior with and without file paths
  
- `src/test/java/net/sourceforge/fddtools/ui/fx/ProjectLifecycleControllerTest.java` (UPDATED)
  - Improved test isolation by clearing ProjectService state

## Test Coverage Summary

The fix is now protected by comprehensive tests covering:

1. **Primary Regression**: Open file → modify → save should not show dialog
2. **Save As Behavior**: Save As should always show dialog regardless of project state
3. **New Project Behavior**: New projects should show Save As dialog on first save
4. **Multiple Save Cycles**: Subsequent saves after opening should remain silent
5. **Recent Files Integration**: Opening via Recent Files menu should work correctly
6. **Error Handling**: Missing files in recent list should be handled gracefully

## Testing Commands

To verify the fix works:

```bash
# Run the main regression test
mvn test -Dtest=SaveAfterOpenRegressionTest

# Run all save-related UI tests
mvn test -Dtest="*Save*Test"

# Run all UI tests to ensure no regressions
mvn test -Dtest="net.sourceforge.fddtools.ui.fx.*"
```

## Manual Testing Verification

To manually verify the fix:

1. **Open existing file test**:
   - Start the application
   - Open an existing .fddi file (File > Open or Recent Files)
   - Make any change to the project
   - Press Command+S (Mac) or Ctrl+S (Windows/Linux)
   - **Expected**: File saves directly without showing Save As dialog

2. **New project test**:
   - Start the application  
   - Create a new project (File > New)
   - Make changes
   - Press Command+S
   - **Expected**: Save As dialog appears (correct behavior)

3. **Save As test**:
   - Open any project (new or existing)
   - Use File > Save As
   - **Expected**: Save As dialog always appears (correct behavior)

## Impact Assessment

- ✅ **Fixes critical user workflow**: Save now works as expected after opening files
- ✅ **Maintains existing behavior**: Save As still works correctly
- ✅ **No breaking changes**: All existing functionality preserved
- ✅ **Comprehensive test coverage**: Prevents future regressions
- ✅ **Backwards compatible**: No changes to file formats or APIs

This fix restores the expected save behavior that users rely on for efficient project editing workflows.
