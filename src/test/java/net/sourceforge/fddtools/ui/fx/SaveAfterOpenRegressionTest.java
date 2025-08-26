package net.sourceforge.fddtools.ui.fx;

import javafx.application.Platform;
import javafx.stage.FileChooser;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.service.ProjectService;
import net.sourceforge.fddtools.state.ModelState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression test for the critical bug where opening a file and then saving
 * would incorrectly trigger a "Save As" dialog instead of saving directly
 * to the opened file.
 * 
 * This was caused by ProjectLifecycleController not updating the ProjectService
 * with the opened file path, causing getAbsolutePath() to return null.
 */
public class SaveAfterOpenRegressionTest {

    @TempDir
    Path tempDir;

    private ProjectService projectService;
    private FDDFileActions fileActions;
    private TestFileDialogStrategy dialogStrategy;
    private DummyHost host;

    // Test utilities
    static class DialogInvocation {
        final String type;
        final String title;
        DialogInvocation(String type, String title) { 
            this.type = type; 
            this.title = title; 
        }
    }

    static class TestFileDialogStrategy implements FDDFileActions.FileDialogStrategy {
        final List<DialogInvocation> invocations = new ArrayList<>();
        File fileToReturn;

        @Override 
        public synchronized File showSave(java.util.function.Consumer<FileChooser> config, javafx.stage.Window owner) {
            FileChooser fc = new FileChooser();
            config.accept(fc);
            invocations.add(new DialogInvocation("save", fc.getTitle()));
            return fileToReturn;
        }

        @Override 
        public synchronized File showOpen(java.util.function.Consumer<FileChooser> config, javafx.stage.Window owner) {
            FileChooser fc = new FileChooser();
            config.accept(fc);
            invocations.add(new DialogInvocation("open", fc.getTitle()));
            return fileToReturn;
        }
        
        public synchronized int getSaveDialogCount() {
            return (int) invocations.stream()
                    .filter(inv -> "save".equals(inv.type))
                    .count();
        }
        
        public synchronized void clearInvocations() {
            invocations.clear();
        }
    }

    private static class DummyHost implements FDDFileActions.Host {
        @Override public void showErrorDialog(String title, String message) { }
        @Override public void refreshRecentFilesMenu() { }
        @Override public void updateTitle() { }
        @Override public boolean canClose() { return true; }
        @Override public void loadProjectFromPath(String path, boolean rebuildUI) throws Exception {
            // Simulate the main window behavior: load the file and update ProjectService
            FDDINode root = (FDDINode) net.sourceforge.fddtools.persistence.FDDIXMLFileReader.read(path);
            ProjectService.getInstance().openWithRoot(path, root);
        }
        @Override public void rebuildProjectUI(FDDINode root, boolean markDirty) { }
        @Override public javafx.stage.Stage getPrimaryStage() { return new javafx.stage.Stage(); }
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
        
        dialogStrategy = new TestFileDialogStrategy();
        host = new DummyHost();
        fileActions = new FDDFileActions(host, dialogStrategy);
    }

    @Test
    void openFileThenSaveShouldNotTriggerSaveAsDialog() throws Exception {
        // 1. Create a test file with some content
        Path testFile = tempDir.resolve("test-project.fddi");
        projectService.newProject("Test Project");
        projectService.saveAs(testFile.toString());
        
        // Verify file was created and saved
        assertTrue(Files.exists(testFile), "Test file should be created");
        assertEquals(testFile.toString(), projectService.getAbsolutePath(), "Project should have the saved path");
        
        // 2. Clear the project state to simulate fresh app start
        projectService.clear();
        assertNull(projectService.getAbsolutePath(), "Project path should be cleared");
        assertNull(projectService.getRoot(), "Project root should be cleared");
        
        // 3. Open the file using the file dialog mechanism
        dialogStrategy.fileToReturn = testFile.toFile();
        
        CountDownLatch openLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            fileActions.openProject(path -> {
                try {
                    host.loadProjectFromPath(path, true);
                    openLatch.countDown();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        });
        assertTrue(openLatch.await(3, TimeUnit.SECONDS), "Open operation should complete");
        
        // 4. Verify that the project is properly loaded with the correct path
        assertNotNull(projectService.getRoot(), "Project root should be loaded");
        assertEquals(testFile.toString(), projectService.getAbsolutePath(), 
                    "Project should remember the opened file path");
        assertTrue(projectService.hasPathProperty().get(), "Project should have a saved path");
        
        // 5. Make some changes to mark the project as dirty
        projectService.markDirty();
        Thread.sleep(50); // Allow dirty state to propagate
        assertTrue(ModelState.getInstance().isDirty(), "Project should be marked as dirty");
        
        // 6. Now attempt to save - this should NOT trigger a Save As dialog
        dialogStrategy.clearInvocations(); // Clear any previous dialog invocations
        
        CountDownLatch saveLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            fileActions.saveProject();
            saveLatch.countDown();
        });
        assertTrue(saveLatch.await(3, TimeUnit.SECONDS), "Save operation should complete");
        
        // Allow some time for any async operations to complete
        Thread.sleep(100);
        
        // 7. Verify that NO save dialog was shown (the critical test)
        int saveDialogCount = dialogStrategy.getSaveDialogCount();
        assertEquals(0, saveDialogCount, 
                    "Save operation should NOT trigger a Save As dialog when file has known path");
        
        // 8. Verify that the save operation was successful
        assertFalse(ModelState.getInstance().isDirty(), "Project should no longer be dirty after save");
        assertEquals(testFile.toString(), projectService.getAbsolutePath(), 
                    "Project path should remain unchanged after save");
    }

    @Test
    void saveAsAlwaysTriggersDialog() throws Exception {
        // This test ensures Save As behavior is still correct
        Path testFile = tempDir.resolve("saveas-test.fddi");
        
        // 1. Create a project
        projectService.newProject("Save As Test");
        projectService.markDirty();
        
        // 2. Trigger Save As - should always show dialog
        dialogStrategy.fileToReturn = testFile.toFile();
        
        CountDownLatch saveAsLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            fileActions.saveProjectAs();
            saveAsLatch.countDown();
        });
        assertTrue(saveAsLatch.await(3, TimeUnit.SECONDS), "Save As operation should complete");
        
        // Allow some time for any async operations to complete
        Thread.sleep(100);
        
        // 3. Verify that a save dialog was shown
        int saveDialogCount = dialogStrategy.getSaveDialogCount();
        assertEquals(1, saveDialogCount, "Save As should always trigger a save dialog");
        
        // 4. Verify the file was saved correctly
        assertTrue(Files.exists(testFile), "File should be created by Save As");
        assertEquals(testFile.toString(), projectService.getAbsolutePath(), "Path should be updated by Save As");
        assertFalse(ModelState.getInstance().isDirty(), "Project should not be dirty after Save As");
    }

    @Test
    void newProjectThenSaveTriggersDialog() throws Exception {
        // This test ensures that new (unsaved) projects still trigger Save As
        Path testFile = tempDir.resolve("new-project.fddi");
        
        // 1. Create a new project (no file associated)
        projectService.newProject("New Project");
        assertNull(projectService.getAbsolutePath(), "New project should have no path");
        
        // 2. Mark as dirty and save
        projectService.markDirty();
        dialogStrategy.fileToReturn = testFile.toFile();
        
        CountDownLatch saveLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            fileActions.saveProject();
            saveLatch.countDown();
        });
        assertTrue(saveLatch.await(3, TimeUnit.SECONDS), "Save operation should complete");
        
        // Allow some time for any async operations to complete
        Thread.sleep(100);
        
        // 3. Verify that a save dialog was shown (Save As behavior for new projects)
        int saveDialogCount = dialogStrategy.getSaveDialogCount();
        assertEquals(1, saveDialogCount, 
                    "Save on new project should trigger Save As dialog");
    }

    @Test
    void multipleSaveAfterOpenOnlyFirstTriggersDialog() throws Exception {
        // This test verifies the expected workflow: 
        // Open file -> modify -> Save (no dialog) -> modify -> Save (no dialog)
        
        // 1. Create and save initial file
        Path testFile = tempDir.resolve("multi-save-test.fddi");
        projectService.newProject("Multi Save Test");
        projectService.saveAs(testFile.toString());
        projectService.clear();
        
        // 2. Open the file
        dialogStrategy.fileToReturn = testFile.toFile();
        CountDownLatch openLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            fileActions.openProject(path -> {
                try {
                    host.loadProjectFromPath(path, true);
                    openLatch.countDown();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        });
        assertTrue(openLatch.await(3, TimeUnit.SECONDS));
        
        // 3. Make changes and save (first save after open)
        projectService.markDirty();
        Thread.sleep(50);
        
        dialogStrategy.clearInvocations();
        CountDownLatch firstSaveLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            fileActions.saveProject();
            firstSaveLatch.countDown();
        });
        assertTrue(firstSaveLatch.await(3, TimeUnit.SECONDS));
        Thread.sleep(100); // Allow async operations to complete
        
        // Verify no dialog for first save
        assertEquals(0, dialogStrategy.getSaveDialogCount(), "First save after open should not show dialog");
        assertFalse(ModelState.getInstance().isDirty(), "Should not be dirty after first save");
        
        // 4. Make more changes and save again (second save)
        projectService.markDirty();
        Thread.sleep(50);
        
        CountDownLatch secondSaveLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            fileActions.saveProject();
            secondSaveLatch.countDown();
        });
        assertTrue(secondSaveLatch.await(3, TimeUnit.SECONDS));
        Thread.sleep(100); // Allow async operations to complete
        
        // Verify no dialog for second save either
        assertEquals(0, dialogStrategy.getSaveDialogCount(), "Second save should also not show dialog");
        assertFalse(ModelState.getInstance().isDirty(), "Should not be dirty after second save");
        
        // 5. Verify path is still correct
        assertEquals(testFile.toString(), projectService.getAbsolutePath(), 
                    "Path should remain consistent through multiple saves");
    }
}
