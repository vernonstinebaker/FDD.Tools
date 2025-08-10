/**
 * JavaFX Application entry point for FDD Tools.
 * This replaces the traditional Swing-based Main.java approach with a modern JavaFX Application.
 */
package net.sourceforge.fddtools;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.sourceforge.fddtools.ui.fx.FDDMainWindowFX;
import net.sourceforge.fddtools.ui.fx.MacOSIntegrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FDDApplicationFX extends Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(FDDApplicationFX.class);

    static { MacOSIntegrationService.setEarlyMacProperties(); }

    private FDDMainWindowFX mainWindow;

    @Override
    public void init() throws Exception {
        super.init();
        LOGGER.info("FDD Tools JavaFX application initialized");
        if (MacOSIntegrationService.isMac()) {
            LOGGER.info("macOS properties verification: apple.awt.application.name={} apple.laf.useScreenMenuBar={}",
                System.getProperty("apple.awt.application.name"), System.getProperty("apple.laf.useScreenMenuBar"));
        }
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            primaryStage.setTitle("FDD Tools");
            // Load stage icons
            try {
                primaryStage.getIcons().addAll(
                    new Image(getClass().getResourceAsStream("/FDDTools-16.png")),
                    new Image(getClass().getResourceAsStream("/FDDTools-32.png")),
                    new Image(getClass().getResourceAsStream("/FDDTools-64.png")),
                    new Image(getClass().getResourceAsStream("/FDDTools-128.png")),
                    new Image(getClass().getResourceAsStream("/FDDTools.png"))
                );
                LOGGER.info("Successfully loaded application icon");
                MacOSIntegrationService.trySetDockIconFromResources("/FDDTools-128.png");
            } catch (Exception e) {
                LOGGER.warn("Could not load application icon: {}", e.getMessage());
                try {
                    Image fallbackIcon = new Image(getClass().getResourceAsStream("/net/sourceforge/fddtools/ui/images/document-properties.png"));
                    primaryStage.getIcons().add(fallbackIcon);
                    LOGGER.info("Loaded fallback application icon");
                } catch (Exception e2) { LOGGER.warn("Could not load fallback icon either", e2); }
            }

            mainWindow = new FDDMainWindowFX(primaryStage);
            Scene scene = new Scene(mainWindow, 1400, 900);
            try { scene.getStylesheets().add(getClass().getResource("/styles/fdd-canvas.css").toExternalForm()); } catch (Exception e) { LOGGER.warn("Could not load CSS stylesheet", e); }
            try { scene.getStylesheets().add(getClass().getResource("/styles/global-theme.css").toExternalForm()); } catch (Exception ignore) {}
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800); primaryStage.setMinHeight(600);

            MacOSIntegrationService.applyLastWindowBounds(primaryStage);
            if(primaryStage.getWidth()<=0 || primaryStage.getHeight()<=0) primaryStage.centerOnScreen();
            primaryStage.show();

            primaryStage.setOnCloseRequest(event -> {
                if (mainWindow.canClose()) {
                    MacOSIntegrationService.persistWindowBounds(primaryStage);
                    Platform.exit();
                } else { event.consume(); }
            });

            LOGGER.info("FDD Tools JavaFX application started successfully");

            try {
                var prefs = net.sourceforge.fddtools.util.PreferencesService.getInstance();
                if (prefs.isAutoLoadLastProjectEnabled()) {
                    String last = prefs.getLastProjectPath();
                    if (last != null && !last.isBlank() && new java.io.File(last).isFile()) {
                        org.slf4j.LoggerFactory.getLogger(FDDApplicationFX.class).info("Auto-loading last project: {}", last);
                        try {
                            Object root = net.sourceforge.fddtools.persistence.FDDIXMLFileReader.read(last);
                            if (root instanceof net.sourceforge.fddtools.model.FDDINode) {
                                net.sourceforge.fddtools.service.ProjectService.getInstance().open(last);
                            }
                        } catch (Exception ex) {
                            org.slf4j.LoggerFactory.getLogger(FDDApplicationFX.class).warn("Auto-load failed: {}", ex.getMessage());
                        }
                    }
                }
            } catch (Exception ignore) {}
        } catch (Exception e) {
            LOGGER.error("Failed to start FDD Tools application", e);
            Platform.exit();
        }
    }

    @Override
    public void stop() throws Exception {
        if (mainWindow != null) { mainWindow.cleanup(); }
        super.stop();
    }

    public static void main(String[] args) {
        Platform.setImplicitExit(false);
        Application.launch(FDDApplicationFX.class, args);
    }
}
