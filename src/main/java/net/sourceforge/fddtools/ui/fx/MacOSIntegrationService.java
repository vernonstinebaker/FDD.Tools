package net.sourceforge.fddtools.ui.fx;

import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates macOS-specific integration through reflection to keep the
 * remainder of the codebase free from direct java.awt compile-time dependencies.
 */
public final class MacOSIntegrationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MacOSIntegrationService.class);
    private static final String OS_NAME = System.getProperty("os.name", "").toLowerCase();

    private MacOSIntegrationService() {}

    public static boolean isMac() { return OS_NAME.contains("mac"); }

    public static void setEarlyMacProperties() {
        if(!isMac()) return;
        System.setProperty("apple.awt.application.name", "FDD Tools");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "FDD Tools");
        System.setProperty("apple.awt.application.title", "FDD Tools");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.macos.useScreenMenuBar", "true");
        System.setProperty("java.awt.application.name", "FDD Tools");
        System.setProperty("apple.awt.enableTemplateImages", "true");
        System.setProperty("apple.awt.application.appearance", "system");
        System.setProperty("java.awt.headless", "false");
    }

    public static void trySetDockIconFromResources(String resourcePath) {
        if(!isMac()) return;
        try {
            var stream = MacOSIntegrationService.class.getResourceAsStream(resourcePath);
            if(stream == null) { LOGGER.debug("Dock icon resource not found: {}", resourcePath); return; }
            byte[] data = stream.readAllBytes();
            Class<?> taskbarClass = Class.forName("java.awt.Taskbar");
            Object taskbar = taskbarClass.getMethod("getTaskbar").invoke(null);
            Class<?> featureEnum = Class.forName("java.awt.Taskbar$Feature");
            Object iconFeature = java.util.Arrays.stream(featureEnum.getEnumConstants())
                .filter(c -> c.toString().equals("ICON_IMAGE")).findFirst().orElse(null);
            if(iconFeature==null) return;
            boolean supported = (Boolean) taskbarClass.getMethod("isSupported", featureEnum).invoke(taskbar, iconFeature);
            if(!supported) { LOGGER.debug("Taskbar ICON_IMAGE not supported"); return; }
            Class<?> toolkitClass = Class.forName("java.awt.Toolkit");
            Object toolkit = toolkitClass.getMethod("getDefaultToolkit").invoke(null);
            var createImage = toolkitClass.getMethod("createImage", byte[].class);
            Object awtImage = createImage.invoke(toolkit, (Object) data);
            taskbarClass.getMethod("setIconImage", Class.forName("java.awt.Image")).invoke(taskbar, awtImage);
            LOGGER.info("Dock icon set via reflective Taskbar API");
        } catch (Exception ex) { LOGGER.debug("Dock icon set skipped: {}", ex.getMessage()); }
    }

    public static void applyLastWindowBounds(Stage stage) {
        try {
            var prefs = net.sourceforge.fddtools.util.PreferencesService.getInstance();
            prefs.getLastWindowBounds().ifPresent(b -> {
                stage.setX(b.x());
                stage.setY(b.y());
                stage.setWidth(b.width());
                stage.setHeight(b.height());
            });
        } catch (Exception ignored) { }
    }

    public static void persistWindowBounds(Stage stage) {
        try {
            var prefs = net.sourceforge.fddtools.util.PreferencesService.getInstance();
            prefs.setLastWindowBounds(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
            prefs.flushNow();
        } catch (Exception ignored) { }
    }
}
