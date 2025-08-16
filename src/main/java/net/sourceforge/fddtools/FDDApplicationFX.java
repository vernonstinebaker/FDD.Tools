/**
 * JavaFX Application entry point for FDD Tools.
 * This replaces the traditional Swing-based Main.java approach with a modern JavaFX Application.
 */
package net.sourceforge.fddtools;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.SnapshotParameters;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import net.sourceforge.fddtools.ui.fx.FDDMainWindowFX;
import net.sourceforge.fddtools.ui.fx.MacOSIntegrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FDDApplicationFX extends Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(FDDApplicationFX.class);

    static { MacOSIntegrationService.setEarlyMacProperties(); }
    static {
        try {
            var logging = net.sourceforge.fddtools.service.LoggingService.getInstance();
            var prefs = net.sourceforge.fddtools.service.PreferencesService.getInstance();
            // System properties override persisted preferences if provided
            String sysAudit = System.getProperty("fddtools.log.audit");
            String sysPerf = System.getProperty("fddtools.log.perf");
            boolean auditEnabled = sysAudit != null ? Boolean.parseBoolean(sysAudit) : prefs.isAuditLoggingEnabled();
            boolean perfEnabled = sysPerf != null ? Boolean.parseBoolean(sysPerf) : prefs.isPerfLoggingEnabled();
            logging.setAuditEnabled(auditEnabled);
            logging.setPerfEnabled(perfEnabled);
            org.slf4j.LoggerFactory.getLogger(FDDApplicationFX.class).info("Logging toggles initialized: audit={} perf={}", auditEnabled, perfEnabled);
        } catch (Exception ignored) {}
    }

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
                LOGGER.warn("Could not load application icon: {}", e.toString());
                try {
                    Image fallbackIcon = createFontAwesomeIcon(FontAwesomeIcon.GEAR, 64);
                    primaryStage.getIcons().add(fallbackIcon);
                    LOGGER.info("Loaded fallback application icon from FontAwesome");
                } catch (Exception e2) { LOGGER.warn("Could not create fallback FontAwesome icon either", e2); }
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
                var prefs = net.sourceforge.fddtools.service.PreferencesService.getInstance();
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
                            var log = org.slf4j.LoggerFactory.getLogger(FDDApplicationFX.class);
                            log.warn("Auto-load failed: {}", ex.getMessage());
                            if (LOGGER.isDebugEnabled()) LOGGER.debug("Auto-load failure stacktrace", ex);
                        }
                    }
                }
            } catch (Exception ignore) {}
        } catch (Exception e) {
            LOGGER.error("Failed to start FDD Tools application", e);
            Platform.exit();
        }
    }

    /**
     * Creates an Image from a FontAwesome icon for use as application icons.
     * This allows us to use FontAwesome icons consistently throughout the application,
     * including for window icons where Image objects are required.
     */
    private Image createFontAwesomeIcon(FontAwesomeIcon icon, int size) {
        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setGlyphSize(size);
        iconView.setFill(Color.DARKSLATEGRAY);  // Use a neutral color for app icon
        
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);  // Transparent background
        
        WritableImage writableImage = iconView.snapshot(params, null);
        return writableImage;
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
