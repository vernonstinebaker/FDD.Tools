package net.sourceforge.fddtools.ui.fx;

import javafx.application.Platform;
import javafx.stage.Stage;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.service.ProjectService;
import net.sourceforge.fddtools.testutil.HeadlessTestUtil;
import net.sourceforge.fddtools.testutil.FxTestUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite demonstrating headless testing capabilities for UI components.
 * These tests verify that UI operations work correctly without showing actual dialogs.
 */
@DisplayName("Headless UI Testing")
class HeadlessUITest {

    private ProjectLifecycleController controller;
    private TestHost testHost;
    private Path tempDir;

    @BeforeAll
    static void initJavaFX() throws InterruptedException {
        // Configure headless environment
        HeadlessTestUtil.configureHeadlessEnvironment();
        FxTestUtil.ensureStarted();
    }

    @BeforeEach
    void setUp() throws Exception {
        // Verify we're in headless mode
        assertTrue(HeadlessTestUtil.isHeadlessMode(), "Should be running in headless mode");
        
        // Create temporary directory for test files
        tempDir = Files.createTempDirectory("headless-test");
        
        // Reset project service
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                ProjectService.getInstance().clear();
                testHost = new TestHost();
                
                // Use headless file dialog strategy
                ProjectLifecycleController.FileDialogStrategy headlessStrategy = 
                    HeadlessTestUtil.createHeadlessProjectDialogStrategy();
                controller = new ProjectLifecycleController(testHost, headlessStrategy);
                
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Setup should complete");
    }

    @Test
    @DisplayName("Should handle open dialog in headless mode without showing UI")
    void openDialogHeadless() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                // This should not show any UI dialogs
                controller.requestOpenProject();
                
                // Since headless strategy returns null, no file should be opened
                assertNull(ProjectService.getInstance().getAbsolutePath(), 
                    "No file should be opened when dialog is cancelled");
                
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(10, TimeUnit.SECONDS), "Open dialog operation should complete");
        assertEquals(0, testHost.errorCount, "No errors should be reported for cancelled dialog");
    }

    @Test
    @DisplayName("Should handle save dialog in headless mode without showing UI")
    void saveDialogHeadless() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean saveResult = new AtomicBoolean(true); // Default to true to catch if it's not set
        
        Platform.runLater(() -> {
            try {
                // Create a new project that needs saving
                ProjectService.getInstance().newProject("Test Project");
                net.sourceforge.fddtools.state.ModelState.getInstance().setDirty(true);
                
                // This should not show any UI dialogs
                boolean saved = controller.saveBlocking();
                saveResult.set(saved);
                
            } catch (Exception e) {
                System.err.println("Exception in save operation: " + e.getMessage());
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(10, TimeUnit.SECONDS), "Save dialog operation should complete");
        
        // Since headless strategy returns null, save should fail gracefully
        assertFalse(saveResult.get(), "Save should fail when no file is selected");
        
        assertEquals(0, testHost.errorCount, "No errors should be reported for cancelled save");
        assertNull(testHost.getLastErrorTitle(), "No error title should be set");
        assertNull(testHost.getLastErrorMessage(), "No error message should be set");
    }

    @Test
    @DisplayName("Should successfully simulate file operations with mock strategy")
    void mockFileOperations() throws Exception {
        // Create a test file
        Path testFile = tempDir.resolve("test-project.fddi");
        Files.writeString(testFile, createBasicFddiContent());
        
        // Create mock strategy that returns our test file
        // Create a mock strategy that returns a test file
        net.sourceforge.fddtools.ui.fx.ProjectLifecycleController.FileDialogStrategy mockStrategy = 
            HeadlessTestUtil.createMockProjectDialogStrategy(testFile.toFile());
        
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                // Create controller with mock strategy
                ProjectLifecycleController mockController = new ProjectLifecycleController(testHost, mockStrategy);
                
                // This should "open" our mock file without showing dialogs
                mockController.requestOpenProject();
                
                // Verify file was "opened"
                String openedPath = ProjectService.getInstance().getAbsolutePath();
                assertNotNull(openedPath, "File path should be set");
                assertTrue(openedPath.contains("test-project.fddi"), "Should open the mock file");
                
            } catch (Exception e) {
                fail("Mock file operation should not throw exception: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(10, TimeUnit.SECONDS), "Mock file operation should complete");
    }

    @Test
    @DisplayName("Should create headless Stage without showing window")
    void headlessStageCreation() throws Exception {
        Stage headlessStage = HeadlessTestUtil.createHeadlessStage();
        assertNotNull(headlessStage, "Should create stage instance");
        
        // In headless mode, stage should not be showing
        if (HeadlessTestUtil.isHeadlessMode()) {
            assertFalse(headlessStage.isShowing(), "Stage should not be showing in headless mode");
        }
    }

    @Test
    @DisplayName("Should run FX operations safely in headless mode")
    void safeHeadlessExecution() throws Exception {
        assertDoesNotThrow(() -> {
            HeadlessTestUtil.runOnFxThreadSafe(() -> {
                // This UI operation should work in headless mode
                Stage stage = new Stage();
                stage.setTitle("Test Stage");
                // Don't call show() to avoid UI popup
            }, 5);
        }, "FX operations should work safely in headless mode");
    }

    /**
     * Test implementation of Host interface for testing purposes
     */
    private static class TestHost implements ProjectLifecycleController.Host {
        int errorCount = 0;
        String lastErrorTitle = null;
        String lastErrorMessage = null;

        @Override
        public Stage getPrimaryStage() {
            return HeadlessTestUtil.createHeadlessStage();
        }
        
        public String getLastErrorTitle() {
            return lastErrorTitle;
        }
        
        public String getLastErrorMessage() {
            return lastErrorMessage;
        }

        @Override
        public void rebuildProjectUI(FDDINode root, boolean markDirty) {
            // No-op for testing
        }

        @Override
        public void refreshRecentFilesMenu() {
            // No-op for testing
        }

        @Override
        public void updateTitle() {
            // No-op for testing
        }

        @Override
        public boolean canClose() {
            return true;
        }

        @Override
        public void showErrorDialog(String title, String message) {
            errorCount++;
            lastErrorTitle = title;
            lastErrorMessage = message;
        }
    }

    /**
     * Creates basic FDDI XML content for testing
     */
    private String createBasicFddiContent() {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <program xmlns="http://www.nebulon.com/xml/2004/fddi" name="Test Project">
                <subject name="Test Subject" prefix="TS">
                    <activity name="Test Activity">
                        <feature name="Test Feature"/>
                    </activity>
                </subject>
            </program>
            """;
    }
}
