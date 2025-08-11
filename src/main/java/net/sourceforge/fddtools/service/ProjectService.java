package net.sourceforge.fddtools.service;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.state.ModelState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Higher-level facade over ProjectFileService that tracks the current
 * in-memory root, its display name/path, and dirty state. UI layers can
 * bind to ModelState.dirtyProperty while this service centralizes mutation.
 */
public final class ProjectService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectService.class);
    private static final ProjectService INSTANCE = new ProjectService();
    public static ProjectService getInstance() { return INSTANCE; }

    private final ProjectFileService fileService = ProjectFileService.getInstance();
    private FDDINode root;
    private String displayName; // e.g. filename only or "New Program"
    private String absolutePath; // full path when saved/opened
    private final BooleanProperty hasPath = new SimpleBooleanProperty(false);
    private final BooleanProperty hasProject = new SimpleBooleanProperty(false);

    private ProjectService() {}

    public FDDINode getRoot() { return root; }
    public String getDisplayName() { return displayName; }
    public String getAbsolutePath() { return absolutePath; }
    public BooleanProperty hasPathProperty() { return hasPath; }
    public BooleanProperty hasProjectProperty() { return hasProject; }

    public void newProject(String name) {
    root = fileService.createNewRoot(name);
    displayName = name == null ? "New Program" : name;
    absolutePath = null;
    hasProject.set(true);
    hasPath.set(false);
    setDirty(false);
    MDC.put("action", "newProject");
    MDC.put("projectPath", "<unsaved>");
    LOGGER.info("Created new project: {}", displayName);
    LoggingService.getInstance().audit("projectNew", java.util.Map.of("projectPath","<unsaved>"), () -> displayName);
    MDC.clear();
    }

    /** Create a new project using an externally prepared root node (UI must supply). */
    public void newProject(FDDINode existingRoot, String name) {
        root = existingRoot;
        displayName = name == null ? "New Program" : name;
        absolutePath = null;
        hasProject.set(true);
        hasPath.set(false);
        setDirty(false);
        MDC.put("action", "newProject");
        MDC.put("projectPath", "<unsaved>");
        LOGGER.info("Created new project (provided root): {}", displayName);
        LoggingService.getInstance().audit("projectNew", java.util.Map.of("projectPath","<unsaved>"), () -> displayName);
        MDC.clear();
    }

    public boolean open(String path) {
        try {
            FDDINode loaded = fileService.open(path);
            root = loaded;
            absolutePath = path;
            int idx = path.lastIndexOf('/');
            displayName = idx >= 0 ? path.substring(idx + 1) : path;
            hasProject.set(true);
            hasPath.set(true);
            setDirty(false);
            MDC.put("action", "openProject");
            MDC.put("projectPath", path);
            LOGGER.info("Opened project: {}", displayName);
            LoggingService.getInstance().audit("projectOpen", java.util.Map.of("projectPath", path), () -> displayName);
            MDC.clear();
            return true;
        } catch (Exception e) {
            MDC.put("action", "openProject");
            MDC.put("projectPath", path);
            LOGGER.error("Open failed: {}", e.getMessage(), e);
            MDC.clear();
            return false;
        }
    }

    /** Open project using an already-loaded root (avoid double file parse and retain same instance used in UI). */
    public boolean openWithRoot(String path, FDDINode loaded) {
        try {
            root = loaded;
            absolutePath = path;
            int idx = path.lastIndexOf('/');
            displayName = idx >= 0 ? path.substring(idx + 1) : path;
            hasProject.set(true);
            hasPath.set(true);
            setDirty(false);
            MDC.put("action", "openProject");
            MDC.put("projectPath", path);
            LOGGER.info("Opened project (provided root): {}", displayName);
            LoggingService.getInstance().audit("projectOpen", java.util.Map.of("projectPath", path), () -> displayName);
            MDC.clear();
            return true;
        } catch (Exception e) {
            MDC.put("action", "openProject");
            MDC.put("projectPath", path);
            LOGGER.error("Open provided root failed: {}", e.getMessage(), e);
            MDC.clear();
            return false;
        }
    }

    public boolean save() throws Exception {
        if (root == null) throw new IllegalStateException("No project loaded");
        if (absolutePath == null) throw new IllegalStateException("No target path set (use saveAs)");
        boolean ok = fileService.save(root, absolutePath);
        if (ok) {
            setDirty(false);
            hasPath.set(true);
            MDC.put("action", "saveProject");
            MDC.put("projectPath", absolutePath);
            LOGGER.info("Saved project to existing path: {}", absolutePath);
            LoggingService.getInstance().audit("projectSave", java.util.Map.of("projectPath", absolutePath), () -> displayName);
            MDC.clear();
        }
        return ok;
    }

    public boolean saveAs(String path) throws Exception {
        if (root == null) throw new IllegalStateException("No project loaded");
        boolean ok = fileService.save(root, path);
        if (ok) {
            absolutePath = path;
            int idx = path.lastIndexOf('/');
            displayName = idx >= 0 ? path.substring(idx + 1) : path;
            setDirty(false);
            hasPath.set(true);
            MDC.put("action", "saveAsProject");
            MDC.put("projectPath", path);
            LOGGER.info("Saved project (saveAs) to: {}", path);
            LoggingService.getInstance().audit("projectSaveAs", java.util.Map.of("projectPath", path), () -> displayName);
            MDC.clear();
        }
        return ok;
    }

    public void markDirty() { setDirty(true); }

    private void setDirty(boolean dirty) {
        if (Platform.isFxApplicationThread()) {
            ModelState.getInstance().setDirty(dirty);
        } else {
            try {
                Platform.runLater(() -> ModelState.getInstance().setDirty(dirty));
            } catch (IllegalStateException e) {
                // FX toolkit not initialized or shutting down; fallback to direct set (test or non-UI context)
                ModelState.getInstance().setDirty(dirty);
            }
        }
    }

    public void clear() {
        root = null;
        displayName = null;
        absolutePath = null;
    hasProject.set(false);
    hasPath.set(false);
        setDirty(false);
    }
}
