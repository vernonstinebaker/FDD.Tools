package net.sourceforge.fddtools.testutil;

import javafx.application.Platform;
import javafx.stage.Stage;
import net.sourceforge.fddtools.ui.fx.FDDFileActions;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Utilities for headless testing that prevent UI dialogs from appearing during automated tests.
 * This class provides mock implementations and headless-safe alternatives for UI components.
 */
public final class HeadlessTestUtil {
    
    private HeadlessTestUtil() {}
    
    /**
     * Checks if we're running in headless mode (no UI should be displayed).
     * @return true if headless mode is detected
     */
    public static boolean isHeadlessMode() {
        // Check explicit headless flags
        if ("true".equalsIgnoreCase(System.getProperty("java.awt.headless"))) {
            return true;
        }
        if ("true".equalsIgnoreCase(System.getProperty("testfx.headless"))) {
            return true;
        }
        
        // Check CI environment variables
        return System.getenv("CI") != null || 
               System.getenv("CONTINUOUS_INTEGRATION") != null ||
               System.getenv("GITHUB_ACTIONS") != null ||
               System.getenv("JENKINS_URL") != null;
    }
    
    /**
     * Creates a no-op FileDialogStrategy for headless testing (FDDFileActions version).
     * This prevents actual file dialogs from appearing during tests.
     * @return A strategy that returns null for all dialog operations
     */
    public static FDDFileActions.FileDialogStrategy createHeadlessFileDialogStrategy() {
        return new FDDFileActions.FileDialogStrategy() {
            @Override
            public File showSave(Consumer<FileChooser> config, javafx.stage.Window owner) {
                // In headless mode, don't show dialogs - return null to simulate cancel
                return null;
            }
            
            @Override
            public File showOpen(Consumer<FileChooser> config, javafx.stage.Window owner) {
                // In headless mode, don't show dialogs - return null to simulate cancel
                return null;
            }
        };
    }
    
    /**
     * Creates a no-op FileDialogStrategy for headless testing (ProjectLifecycleController version).
     * This prevents actual file dialogs from appearing during tests.
     * @return A strategy that returns null for all dialog operations
     */
    public static net.sourceforge.fddtools.ui.fx.ProjectLifecycleController.FileDialogStrategy createHeadlessProjectDialogStrategy() {
        return new net.sourceforge.fddtools.ui.fx.ProjectLifecycleController.FileDialogStrategy() {
            @Override
            public File showOpenDialog(Consumer<FileChooser> config, javafx.stage.Window owner) {
                // In headless mode, don't show dialogs - return null to simulate cancel
                return null;
            }
            
            @Override
            public File showSaveDialog(Consumer<FileChooser> config, javafx.stage.Window owner) {
                // In headless mode, don't show dialogs - return null to simulate cancel
                return null;
            }
        };
    }
    
    /**
     * Creates a mock FileDialogStrategy that returns predetermined files for testing (FDDFileActions version).
     * @param fileToReturn The file to return from dialog operations (null simulates cancel)
     * @return A strategy that returns the specified file
     */
    public static FDDFileActions.FileDialogStrategy createMockFileDialogStrategy(File fileToReturn) {
        return new FDDFileActions.FileDialogStrategy() {
            @Override
            public File showSave(Consumer<FileChooser> config, javafx.stage.Window owner) {
                // Still call config for coverage/validation, but don't show dialog
                if (config != null) {
                    FileChooser fc = new FileChooser();
                    config.accept(fc);
                }
                return fileToReturn;
            }
            
            @Override
            public File showOpen(Consumer<FileChooser> config, javafx.stage.Window owner) {
                // Still call config for coverage/validation, but don't show dialog
                if (config != null) {
                    FileChooser fc = new FileChooser();
                    config.accept(fc);
                }
                return fileToReturn;
            }
        };
    }
    
    /**
     * Creates a mock FileDialogStrategy that returns predetermined files for testing (ProjectLifecycleController version).
     * @param fileToReturn The file to return from dialog operations (null simulates cancel)
     * @return A strategy that returns the specified file
     */
    public static net.sourceforge.fddtools.ui.fx.ProjectLifecycleController.FileDialogStrategy createMockProjectDialogStrategy(File fileToReturn) {
        return new net.sourceforge.fddtools.ui.fx.ProjectLifecycleController.FileDialogStrategy() {
            @Override
            public File showOpenDialog(Consumer<FileChooser> config, javafx.stage.Window owner) {
                // Still call config for coverage/validation, but don't show dialog
                if (config != null) {
                    FileChooser fc = new FileChooser();
                    config.accept(fc);
                }
                return fileToReturn;
            }
            
            @Override
            public File showSaveDialog(Consumer<FileChooser> config, javafx.stage.Window owner) {
                // Still call config for coverage/validation, but don't show dialog
                if (config != null) {
                    FileChooser fc = new FileChooser();
                    config.accept(fc);
                }
                return fileToReturn;
            }
        };
    }
    
    /**
     * Creates a headless-safe Stage for testing.
     * In headless mode, this won't attempt to show the stage.
     * @return A Stage instance suitable for testing
     */
    public static Stage createHeadlessStage() {
        if (isHeadlessMode()) {
            // In headless mode, create stage but don't show it
            try {
                CountDownLatch latch = new CountDownLatch(1);
                Stage[] stageRef = new Stage[1];
                Platform.runLater(() -> {
                    try {
                        stageRef[0] = new Stage();
                        // Don't call show() in headless mode
                    } finally {
                        latch.countDown();
                    }
                });
                latch.await(5, TimeUnit.SECONDS);
                return stageRef[0];
            } catch (Exception e) {
                // Fallback: return a basic stage
                return new Stage();
            }
        } else {
            // Normal mode - can show stage if needed
            return new Stage();
        }
    }
    
    /**
     * Shows a Stage only if not in headless mode.
     * Use this instead of calling stage.show() directly in tests.
     * @param stage the stage to show
     */
    public static void showStageIfNotHeadless(Stage stage) {
        if (!isHeadlessMode()) {
            stage.show();
        }
        // In headless mode, don't show the stage but allow scene attachment for testing
    }
    
    /**
     * Configures the environment for headless testing.
     * This should be called in test setup methods.
     */
    public static void configureHeadlessEnvironment() {
        // Set system properties for headless operation
        System.setProperty("java.awt.headless", "true");
        System.setProperty("testfx.headless", "true");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
        
        // Ensure JavaFX toolkit is initialized in headless mode
        try {
            if (!Platform.isImplicitExit()) {
                Platform.setImplicitExit(true);
            }
        } catch (Exception ignored) {
            // Platform might already be configured
        }
    }
    
    /**
     * Executes a runnable on the FX thread safely, even in headless mode.
     * @param runnable The code to execute
     * @param timeoutSeconds Maximum time to wait
     * @throws Exception If execution fails or times out
     */
    public static void runOnFxThreadSafe(Runnable runnable, int timeoutSeconds) throws Exception {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
            return;
        }
        
        CountDownLatch latch = new CountDownLatch(1);
        Exception[] exception = new Exception[1];
        
        Platform.runLater(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                exception[0] = e;
            } finally {
                latch.countDown();
            }
        });
        
        boolean completed = latch.await(timeoutSeconds, TimeUnit.SECONDS);
        if (!completed) {
            throw new IllegalStateException("FX thread operation timed out after " + timeoutSeconds + " seconds");
        }
        
        if (exception[0] != null) {
            throw exception[0];
        }
    }
}
