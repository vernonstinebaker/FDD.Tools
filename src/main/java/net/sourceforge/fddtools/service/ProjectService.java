package net.sourceforge.fddtools.service;

import javafx.application.Platform;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.state.ModelState;

import java.util.logging.Logger;

/**
 * Higher-level facade over ProjectFileService that tracks the current
 * in-memory root, its display name/path, and dirty state. UI layers can
 * bind to ModelState.dirtyProperty while this service centralizes mutation.
 */
public final class ProjectService {
    private static final Logger LOGGER = Logger.getLogger(ProjectService.class.getName());
    private static final ProjectService INSTANCE = new ProjectService();
    public static ProjectService getInstance() { return INSTANCE; }

    private final ProjectFileService fileService = ProjectFileService.getInstance();
    private FDDINode root;
    private String displayName; // e.g. filename only or "New Program"
    private String absolutePath; // full path when saved/opened

    private ProjectService() {}

    public FDDINode getRoot() { return root; }
    public String getDisplayName() { return displayName; }
    public String getAbsolutePath() { return absolutePath; }

    public void newProject(String name) {
        root = fileService.createNewRoot(name);
        displayName = name == null ? "New Program" : name;
        absolutePath = null;
        setDirty(false);
    }

    public boolean open(String path) {
        try {
            FDDINode loaded = fileService.open(path);
            root = loaded;
            absolutePath = path;
            int idx = path.lastIndexOf('/');
            displayName = idx >= 0 ? path.substring(idx + 1) : path;
            setDirty(false);
            return true;
        } catch (Exception e) {
            LOGGER.severe("Open failed: " + e.getMessage());
            return false;
        }
    }

    public boolean save() throws Exception {
        if (root == null) throw new IllegalStateException("No project loaded");
        if (absolutePath == null) throw new IllegalStateException("No target path set (use saveAs)");
        boolean ok = fileService.save(root, absolutePath);
        if (ok) setDirty(false);
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
        }
        return ok;
    }

    public void markDirty() { setDirty(true); }

    private void setDirty(boolean dirty) {
        Platform.runLater(() -> ModelState.getInstance().setDirty(dirty));
    }

    public void clear() {
        root = null;
        displayName = null;
        absolutePath = null;
        setDirty(false);
    }
}
