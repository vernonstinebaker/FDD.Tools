package net.sourceforge.fddtools.ui.fx;

import javafx.application.Platform;
import javafx.stage.FileChooser;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.service.ProjectService;
import net.sourceforge.fddtools.service.PreferencesService;
import net.sourceforge.fddtools.persistence.FDDIXMLFileWriter;
import net.sourceforge.fddtools.util.FileNameUtil;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import java.io.File;

/** Extracted file open/save operations from FDDMainWindowFX. */
public class FDDFileActions {
    private static final Logger LOGGER = LoggerFactory.getLogger(FDDFileActions.class);

    public interface Host {
        void showErrorDialog(String title, String message);
        void refreshRecentFilesMenu();
        void updateTitle();
        boolean canClose();
        void loadProjectFromPath(String path, boolean rebuildUI) throws Exception;
        void rebuildProjectUI(FDDINode root, boolean markDirty);
        javafx.stage.Stage getPrimaryStage();
    }

    private final Host host;
    public FDDFileActions(Host host){ this.host = host; }

    public void saveProject() { // existing semantics
        String currentPath = ProjectService.getInstance().getAbsolutePath();
        if (currentPath != null) saveToFile(currentPath); else saveProjectAs();
    }

    public void saveProjectAs() {
        Platform.runLater(() -> {
            try {
                FileChooser fc = new FileChooser();
                fc.setTitle("Save FDD Project");
                fc.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("FDD Files", "*.fddi"),
                    new FileChooser.ExtensionFilter("XML Files", "*.xml"),
                    new FileChooser.ExtensionFilter("All Files", "*.*")
                );
                fc.setInitialFileName(FileNameUtil.buildDefaultSaveFileName(ProjectService.getInstance().getDisplayName()));
                File selected = fc.showSaveDialog(host.getPrimaryStage());
                if (selected != null) {
                    String path = FileNameUtil.ensureFddiOrXmlExtension(FileNameUtil.stripDuplicateFddi(selected.getAbsolutePath()));
                    saveToFile(path);
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to save project: {}", e.getMessage());
                host.showErrorDialog("Save Project Failed", e.getMessage());
            }
        });
    }

    public boolean saveToFile(String fileName) {
        Object root = ProjectService.getInstance().getRoot();
        if (root == null) return false;
        var ps = ProjectService.getInstance();
        String currentPath = ps.getAbsolutePath();
        boolean isSaveAs = currentPath == null || !currentPath.equals(fileName);
        String normalized = FileNameUtil.ensureFddiOrXmlExtension(FileNameUtil.stripDuplicateFddi(fileName));
        
        try {
            // Direct synchronous save - no async overlay needed
            boolean success = FDDIXMLFileWriter.write(root, normalized);
            if (success) {
                if (isSaveAs) {
                    ps.saveAs(normalized);
                    PreferencesService.getInstance().addRecentFile(normalized);
                    host.refreshRecentFilesMenu();
                } else {
                    ps.save();
                }
                net.sourceforge.fddtools.service.PreferencesService.getInstance().setLastProjectPath(ps.getAbsolutePath());
                net.sourceforge.fddtools.service.PreferencesService.getInstance().flushNow();
                host.updateTitle();
                return true;
            } else {
                host.showErrorDialog("Save Error", "Failed to save the project file.");
                return false;
            }
        } catch (Exception ex) {
            LOGGER.warn("Save operation failed: {}", ex.getMessage(), ex);
            host.showErrorDialog("Save Error", "An error occurred while saving: " + ex.getMessage());
            return false;
        }
    }

    public void openProject(java.util.function.Consumer<String> loadPathConsumer) {
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Open FDD Project");
            fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("FDD Files", "*.fddi", "*.xml"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
            );
            File selected = fc.showOpenDialog(host.getPrimaryStage());
            if (selected != null) loadPathConsumer.accept(selected.getAbsolutePath());
        } catch (Exception e) {
            LOGGER.error("Failed to load project: {}", e.getMessage(), e);
            host.showErrorDialog("Open Project Failed", "Error loading file: " + e.getMessage());
        }
    }
}
