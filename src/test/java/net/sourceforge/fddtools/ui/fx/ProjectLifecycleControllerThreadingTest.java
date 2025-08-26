package net.sourceforge.fddtools.ui.fx;

import javafx.application.Platform;
import javafx.stage.Stage;
import net.sourceforge.fddtools.service.ProjectService;
import net.sourceforge.fddtools.testutil.HeadlessTestUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify that the threading fix in ProjectLifecycleController
 * prevents JavaFX thread violations when calling methods from 
 * non-JavaFX threads.
 */
public class ProjectLifecycleControllerThreadingTest {

    private ProjectService projectService;
    private ProjectLifecycleController controller;
    private TestHost host;

    private static class TestHost implements ProjectLifecycleController.Host {
        @Override
        public void showErrorDialog(String title, String message) { }
        
        @Override
        public void refreshRecentFilesMenu() { }
        
        @Override
        public void updateTitle() { }
        
        @Override
        public boolean canClose() { 
            return true; 
        }
        
        @Override
        public void rebuildProjectUI(net.sourceforge.fddtools.model.FDDINode root, boolean markDirty) { }
        
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
    void threadSafetyVerificationTest() throws Exception {
        // This test verifies the threading fix without relying on modal dialogs
        
        // Create a project with a file path so save operations don't need dialogs
        projectService.newProject("Threading Test Project");
        
        // Simulate having a file path (this avoids modal dialogs)
        try {
            java.io.File tempFile = java.io.File.createTempFile("test", ".fddi");
            tempFile.deleteOnExit();
            
            // Save the project so it has a file path
            boolean saved = projectService.saveAs(tempFile.getAbsolutePath());
            assertTrue(saved, "Should be able to save project");
            assertNotNull(projectService.getAbsolutePath(), "Project should have a file path");
            
            // Now test calling saveBlocking from different threads
            // Since the project has a path, it won't show a dialog
            
            // Test from background thread
            Exception[] exceptionHolder = new Exception[1];
            CountDownLatch backgroundLatch = new CountDownLatch(1);
            
            Thread backgroundThread = new Thread(() -> {
                try {
                    // This should work without throwing threading exceptions
                    controller.saveBlocking();
                    // We don't check the return value, just that no exception is thrown
                } catch (IllegalStateException e) {
                    if (e.getMessage().contains("Not on FX application thread")) {
                        exceptionHolder[0] = e;
                    }
                } catch (Exception e) {
                    if (e.getCause() instanceof IllegalStateException && 
                        e.getCause().getMessage().contains("Not on FX application thread")) {
                        exceptionHolder[0] = e;
                    }
                } finally {
                    backgroundLatch.countDown();
                }
            });
            
            backgroundThread.start();
            assertTrue(backgroundLatch.await(5, TimeUnit.SECONDS), "Background task should complete");
            
            // Verify no threading exception was thrown
            assertNull(exceptionHolder[0], "No threading exception should be thrown from background thread");
            
            // Test from FX thread
            CountDownLatch fxLatch = new CountDownLatch(1);
            Exception[] fxExceptionHolder = new Exception[1];
            
            Platform.runLater(() -> {
                try {
                    // This should also work without issues
                    controller.saveBlocking();
                    // We don't check the return value, just that no exception is thrown
                } catch (IllegalStateException e) {
                    if (e.getMessage().contains("Not on FX application thread")) {
                        fxExceptionHolder[0] = e;
                    }
                } catch (Exception e) {
                    if (e.getCause() instanceof IllegalStateException && 
                        e.getCause().getMessage().contains("Not on FX application thread")) {
                        fxExceptionHolder[0] = e;
                    }
                } finally {
                    fxLatch.countDown();
                }
            });
            
            assertTrue(fxLatch.await(5, TimeUnit.SECONDS), "FX task should complete");
            assertNull(fxExceptionHolder[0], "No threading exception should be thrown from FX thread");
            
        } catch (Exception e) {
            fail("Test setup failed: " + e.getMessage());
        }
    }

    @Test
    void saveBlockingWithNoProjectReturnsTrue() throws Exception {
        // When no project is loaded, saveBlocking should return true immediately
        projectService.clear();
        assertNull(projectService.getRoot(), "Should have no project");
        
        // This should return true immediately without any dialog
        boolean result = controller.saveBlocking();
        assertTrue(result, "saveBlocking should return true when no project is loaded");
    }
}
