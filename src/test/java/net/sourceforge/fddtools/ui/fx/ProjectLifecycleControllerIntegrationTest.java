package net.sourceforge.fddtools.ui.fx;

import javafx.application.Platform;
import javafx.stage.Stage;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.service.ProjectService;
import net.sourceforge.fddtools.state.ModelState;
import net.sourceforge.fddtools.testutil.HeadlessTestUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ProjectLifecycleController to ensure that opening files
 * correctly updates the ProjectService with file path information.
 * 
 * This complements the lightweight ProjectLifecycleControllerTest with full
 * JavaFX integration testing.
 */
public class ProjectLifecycleControllerIntegrationTest {

    @TempDir
    Path tempDir;

    private ProjectService projectService;
    private ProjectLifecycleController controller;
    private TestHost host;

    private static class TestHost implements ProjectLifecycleController.Host {
        String lastErrorTitle;
        String lastErrorMessage;
        boolean canCloseResult = true;
        
        @Override
        public void showErrorDialog(String title, String message) {
            lastErrorTitle = title;
            lastErrorMessage = message;
        }
        
        @Override
        public void refreshRecentFilesMenu() { }
        
        @Override
        public void updateTitle() { }
        
        @Override
        public boolean canClose() { 
            return canCloseResult; 
        }
        
        @Override
        public void rebuildProjectUI(FDDINode root, boolean markDirty) { 
            // Simulate UI rebuild - doesn't affect ProjectService path
        }
        
        @Override
        public Stage getPrimaryStage() { 
            return new Stage(); 
        }
    }

    @BeforeAll
    static void initJavaFX() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException alreadyStarted) {
            latch.countDown();
        }
        latch.await(5, TimeUnit.SECONDS);
    }

    @BeforeEach
    void setUp() {
        projectService = ProjectService.getInstance();
        projectService.clear();
        ModelState.getInstance().setDirty(false);
        
        host = new TestHost();
        
        // Use headless strategy when running in headless mode to prevent UI dialogs
        if (HeadlessTestUtil.isHeadlessMode()) {
            ProjectLifecycleController.FileDialogStrategy headlessStrategy = 
                HeadlessTestUtil.createHeadlessProjectDialogStrategy();
            controller = new ProjectLifecycleController(host, headlessStrategy);
        } else {
            controller = new ProjectLifecycleController(host);
        }
    }

    @Test
    void openSpecificRecentUpdatesProjectServicePath() throws Exception {
        // 1. Create a test file
        Path testFile = tempDir.resolve("recent-test.fddi");
        projectService.newProject("Recent Test Project");
        projectService.saveAs(testFile.toString());
        
        // Verify file exists
        assertTrue(Files.exists(testFile), "Test file should be created");
        
        // 2. Clear project state
        projectService.clear();
        assertNull(projectService.getAbsolutePath(), "Project path should be cleared");
        assertNull(projectService.getRoot(), "Project root should be cleared");
        
        // 3. Open the file using openSpecificRecent (simulates Recent Files menu)
        CountDownLatch openLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.openSpecificRecent(testFile.toString());
            openLatch.countDown();
        });
        assertTrue(openLatch.await(3, TimeUnit.SECONDS), "Open operation should complete");
        
        // 4. Verify that ProjectService was properly updated
        assertNotNull(projectService.getRoot(), "Project root should be loaded");
        assertEquals(testFile.toString(), projectService.getAbsolutePath(), 
                    "Project should have the correct file path");
        assertTrue(projectService.hasPathProperty().get(), "Project should have a saved path");
        assertFalse(ModelState.getInstance().isDirty(), "Project should not be dirty after opening");
        
        // 5. Test that save works correctly without triggering Save As
        projectService.markDirty();
        Thread.sleep(50);
        assertTrue(ModelState.getInstance().isDirty(), "Project should be marked as dirty");
        
        // This should save directly without needing a dialog
        boolean saveResult = controller.saveBlocking();
        assertTrue(saveResult, "Save should succeed");
        assertFalse(ModelState.getInstance().isDirty(), "Project should not be dirty after save");
        assertEquals(testFile.toString(), projectService.getAbsolutePath(), 
                    "Project path should remain unchanged after save");
    }

    @Test
    void openNonExistentRecentShowsErrorAndClearsRecents() throws Exception {
        String nonExistentPath = tempDir.resolve("does-not-exist.fddi").toString();
        
        CountDownLatch openLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.openSpecificRecent(nonExistentPath);
            openLatch.countDown();
        });
        assertTrue(openLatch.await(3, TimeUnit.SECONDS), "Open operation should complete");
        
        // Verify error was shown
        assertEquals("Open Recent", host.lastErrorTitle, "Should show error dialog for missing file");
        assertTrue(host.lastErrorMessage.contains("File no longer exists"), 
                  "Error message should indicate file doesn't exist");
        
        // Verify project state remains clean
        assertNull(projectService.getRoot(), "Project root should remain null");
        assertNull(projectService.getAbsolutePath(), "Project path should remain null");
        assertFalse(projectService.hasPathProperty().get(), "Project should not have a saved path");
    }

    @Test
    void saveBlockingWithoutPathTriggersDialog() throws Exception {
        // Create a new project without saving it
        projectService.newProject("Unsaved Project");
        projectService.markDirty();
        
        // This should trigger the save dialog path since there's no existing file
        controller.saveBlocking();
        // Since we're not actually showing a dialog in test, the save will fail
        // But the important thing is that it attempts the dialog path rather than
        // trying to save to a null path
        
        // The project should still be dirty since no file was actually selected
        assertTrue(ModelState.getInstance().isDirty(), "Project should remain dirty if no file selected");
        assertNull(projectService.getAbsolutePath(), "Project should still have no path");
    }

    @Test
    void saveBlockingWithExistingPathSavesDirectly() throws Exception {
        // 1. Create and save a file
        Path testFile = tempDir.resolve("save-direct-test.fddi");
        projectService.newProject("Save Direct Test");
        projectService.saveAs(testFile.toString());
        
        // 2. Make some changes
        projectService.markDirty();
        Thread.sleep(50);
        assertTrue(ModelState.getInstance().isDirty(), "Project should be dirty");
        
        // 3. Save using controller - should save directly
        boolean saveResult = controller.saveBlocking();
        assertTrue(saveResult, "Save should succeed");
        assertFalse(ModelState.getInstance().isDirty(), "Project should not be dirty after save");
        assertEquals(testFile.toString(), projectService.getAbsolutePath(), 
                    "Project path should remain unchanged");
        
        // 4. Verify file was actually written to
        assertTrue(Files.exists(testFile), "File should still exist");
        assertTrue(Files.size(testFile) > 0, "File should have content");
    }
}
