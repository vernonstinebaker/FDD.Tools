package net.sourceforge.fddtools.ui.fx;

import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.state.ModelState;
import net.sourceforge.fddtools.service.ProjectService;
import net.sourceforge.fddtools.util.LayoutPreferencesService;
import net.sourceforge.fddtools.persistence.FDDIXMLFileReader;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;

/** Handles (re)building the project UI (tree + canvas) and loading projects. */
public class FDDLayoutController {
    private static final Logger LOGGER = LoggerFactory.getLogger(FDDLayoutController.class);

    public interface Host {
        void setProjectTree(FDDTreeViewFX tree);
        void setCanvas(FDDCanvasFX canvas);
        void onSelectionChanged(FDDINode node);
        void updateTitle();
        void showErrorDialog(String title, String msg);
        void updateUndoRedo();
        javafx.scene.text.Font getDefaultFont();
        SplitPane getMainSplit();
        SplitPane getRightSplit();
        javafx.scene.control.TabPane getInfoTabs();
    /** Provides the context menu handler implementation from the main window. */
    FDDTreeContextMenuHandler contextMenuHandler();
    }

    private final Host host;
    public FDDLayoutController(Host host){ this.host=host; }

    public void rebuildProjectUI(FDDINode rootNode, boolean isNew){
        if (rootNode == null) return;
        closeCurrentProject();
    FDDTreeViewFX tree = new FDDTreeViewFX(true);
        // Obtain context menu handler via host indirection (avoids casting host implementation types)
        FDDTreeContextMenuHandler cmh = host.contextMenuHandler();
        if (cmh != null) {
            tree.setContextMenuHandler(cmh);
        } else {
            LOGGER.warn("No context menu handler provided; tree context menu actions disabled");
        }
        tree.populateTree(rootNode);
        if (tree.getRoot()!=null) tree.getSelectionModel().select(tree.getRoot());
        tree.getSelectionModel().selectedItemProperty().addListener((obs,o,n)-> { if (n!=null){ ModelState.getInstance().setSelectedNode(n.getValue()); host.onSelectionChanged(n.getValue()); }});
        tree.setMinWidth(140); tree.setPrefWidth(220);
        
        FDDCanvasFX canvas = new FDDCanvasFX(rootNode, host.getDefaultFont());
        canvas.restoreLastZoomIfEnabled();
        
        // CRITICAL FIX: Use the experiment's working layout structure
        // Put tree and canvas DIRECTLY in main split (no wrapper layers)
        SplitPane main = host.getMainSplit();
        main.getItems().clear();
        main.getItems().addAll(tree, canvas);
        
        double pos = LayoutPreferencesService.getInstance().getMainDividerPosition().orElse(0.25);
        main.setDividerPositions(pos);
        
        host.setProjectTree(tree); host.setCanvas(canvas);
        host.updateTitle(); host.updateUndoRedo();
        if ( isNew ) { ProjectService.getInstance().newProject(rootNode, rootNode.getName()); }
    }

    public void closeCurrentProject(){
        SplitPane main = host.getMainSplit();
        if (main!=null) main.getItems().clear();
        host.setProjectTree(null); host.setCanvas(null);
    }

    public void loadProjectFromPath(String absolutePath){
        if (absolutePath==null) return;
        LOGGER.debug("Loading project from {}", absolutePath);
        try {
            FDDINode rootNode = (FDDINode) FDDIXMLFileReader.read(absolutePath);
            if (rootNode == null) { host.showErrorDialog("Open Project Failed","Failed to parse the selected file."); return; }
            ProjectService.getInstance().openWithRoot(absolutePath, rootNode);
            rebuildProjectUI(rootNode, false);
            LOGGER.info("Project loaded: {}", absolutePath);
        } catch (Exception e){ LOGGER.error("Load project failed: {}", e.getMessage(), e); host.showErrorDialog("Open Project Failed", e.getMessage()); }
    }
}
