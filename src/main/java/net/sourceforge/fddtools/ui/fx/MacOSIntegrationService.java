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
    private static final String APP_NAME = System.getProperty("fddtools.app.name", "FDD Tools");
    private static final String BUNDLE_ID = System.getProperty("fddtools.bundle.id", "net.sourceforge.fddtools");

    private MacOSIntegrationService() {}

    public static boolean isMac() { return OS_NAME.contains("mac"); }

    public static void setEarlyMacProperties() {
        if(!isMac()) return;
    // Application name alignment (menu bar / dock) – overridable via -Dfddtools.app.name
    System.setProperty("apple.awt.application.name", APP_NAME);
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", APP_NAME); // legacy key still honored
    System.setProperty("apple.awt.application.title", APP_NAME);
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.macos.useScreenMenuBar", "true");
    System.setProperty("java.awt.application.name", APP_NAME);
        System.setProperty("apple.awt.enableTemplateImages", "true");
        System.setProperty("apple.awt.application.appearance", "system");
        System.setProperty("java.awt.headless", "false");
    LOGGER.info("macOS early properties set: appName='{}' bundleId='{}'", APP_NAME, BUNDLE_ID);
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

    /**
     * Bind macOS App menu handlers (About, Preferences, Quit) to provided callbacks using reflection
     * against java.awt.Desktop and java.awt.desktop.* handler interfaces. No-ops on non-macOS.
     */
    public static void installMacAppMenuHandlers(Runnable onAbout, Runnable onPreferences, Runnable onQuit) {
        if (!isMac()) return;
        try {
            ClassLoader cl = MacOSIntegrationService.class.getClassLoader();
            Class<?> desktopClass = Class.forName("java.awt.Desktop");
            Object desktop = desktopClass.getMethod("getDesktop").invoke(null);

            // About
            try {
                Class<?> aboutHandler = Class.forName("java.awt.desktop.AboutHandler");
                Object aboutProxy = java.lang.reflect.Proxy.newProxyInstance(cl, new Class[]{aboutHandler},
                    (proxy, method, args) -> { if (onAbout != null) onAbout.run(); return null; });
                desktopClass.getMethod("setAboutHandler", aboutHandler).invoke(desktop, aboutProxy);
            } catch (ClassNotFoundException ignore) { /* older JDK? */ }

            // Preferences
            try {
                Class<?> prefsHandler = Class.forName("java.awt.desktop.PreferencesHandler");
                Object prefsProxy = java.lang.reflect.Proxy.newProxyInstance(cl, new Class[]{prefsHandler},
                    (proxy, method, args) -> { if (onPreferences != null) onPreferences.run(); return null; });
                desktopClass.getMethod("setPreferencesHandler", prefsHandler).invoke(desktop, prefsProxy);
            } catch (ClassNotFoundException ignore) { }

            // Quit
            try {
                Class<?> quitHandler = Class.forName("java.awt.desktop.QuitHandler");
                Object quitProxy = java.lang.reflect.Proxy.newProxyInstance(cl, new Class[]{quitHandler},
                    (proxy, method, args) -> {
                        // signature: handleQuitRequestWith(QuitEvent, QuitResponse)
                        if (onQuit != null) onQuit.run();
                        try {
                            Object response = args != null && args.length > 1 ? args[1] : null;
                            if (response != null) {
                                // Attempt to perform quit to keep system state in sync
                                response.getClass().getMethod("performQuit").invoke(response);
                            }
                        } catch (Throwable ignored) { }
                        return null;
                    });
                desktopClass.getMethod("setQuitHandler", quitHandler).invoke(desktop, quitProxy);
            } catch (ClassNotFoundException ignore) { }

            LOGGER.info("macOS app menu handlers bound (About/Preferences/Quit)");
        } catch (Throwable t) {
            LOGGER.debug("macOS app menu handlers not bound: {}", t.getMessage());
        }
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
