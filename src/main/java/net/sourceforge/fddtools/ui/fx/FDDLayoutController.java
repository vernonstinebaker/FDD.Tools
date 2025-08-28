package net.sourceforge.fddtools.ui.fx;

import javafx.scene.control.SplitPane;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.state.ModelState;
import net.sourceforge.fddtools.service.ProjectService;
import net.sourceforge.fddtools.service.PreferencesService;
import net.sourceforge.fddtools.persistence.FDDIXMLFileReader;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;

/** Handles (re)building the project UI (tree + canvas) and loading projects. */
public class FDDLayoutController {
    private static final Logger LOGGER = LoggerFactory.getLogger(FDDLayoutController.class);

    private final TreeNodeNavigationHistory navigationHistory = new TreeNodeNavigationHistory();

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
        /** Updates navigation button states when history changes. */
        void updateNavigationButtons(boolean canGoBack, boolean canGoForward);
        /** Gets the current project tree for navigation purposes. */
        FDDTreeViewFX getProjectTree();
    }

    private final Host host;
    
    public FDDLayoutController(Host host){ 
        this.host=host; 
        setupNavigationHistory();
    }
    
    private void setupNavigationHistory() {
        navigationHistory.setNavigationListener(new TreeNodeNavigationHistory.NavigationListener() {
            @Override
            public void onNavigationStateChanged(boolean canGoBack, boolean canGoForward) {
                host.updateNavigationButtons(canGoBack, canGoForward);
            }
            
            @Override
            public void onNavigateTo(FDDINode node) {
                // This will be called from navigation history when back/forward is used
                // We need to select the node in the tree without recording it again in history
                FDDTreeViewFX tree = host.getProjectTree();
                if (tree != null && node != null) {
                    tree.selectNode(node, true);
                }
                // Also update the model state and notify the host
                ModelState.getInstance().setSelectedNode(node);
                host.onSelectionChanged(node);
            }
        });
    }
    
    /**
     * Navigate back in the selection history.
     * @return true if navigation occurred
     */
    public boolean navigateBack() {
        return navigationHistory.goBack();
    }
    
    /**
     * Navigate forward in the selection history.
     * @return true if navigation occurred
     */
    public boolean navigateForward() {
        return navigationHistory.goForward();
    }

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
        tree.getSelectionModel().selectedItemProperty().addListener((obs,o,n)-> { 
            if (n!=null){ 
                FDDINode selectedNode = n.getValue();
                ModelState.getInstance().setSelectedNode(selectedNode); 
                host.onSelectionChanged(selectedNode);
                // Record the selection in navigation history
                navigationHistory.recordSelection(selectedNode);
            }
        });
        tree.setMinWidth(140); tree.setPrefWidth(220);
        
        FDDCanvasFX canvas = new FDDCanvasFX(rootNode, host.getDefaultFont());
        canvas.restoreLastZoomIfEnabled();
        
        // Set up Canvas-to-Tree focus integration
        canvas.setCanvasClickHandler(clickedNode -> {
            if (tree != null && clickedNode != null) {
                tree.selectNode(clickedNode, true);
                // Navigation history will be updated by the tree selection listener
            }
        });
        
        // CRITICAL FIX: Use the experiment's working layout structure
        // Put tree and canvas DIRECTLY in main split (no wrapper layers)
        SplitPane main = host.getMainSplit();
        main.getItems().clear();
        main.getItems().addAll(tree, canvas);
        
        // Configure resize behavior: tree view (left) stays fixed width, canvas (right) expands
        SplitPane.setResizableWithParent(tree, false);  // Tree stays fixed width
        SplitPane.setResizableWithParent(canvas, true); // Canvas expands with window
        
        double pos = PreferencesService.getInstance().getMainDividerPosition().orElse(0.25);
        main.setDividerPositions(pos);
        
        // Re-attach divider position listener after new items are added
        if (!main.getDividers().isEmpty()) {
            main.getDividers().get(0).positionProperty().addListener((obs, o, n) ->
                PreferencesService.getInstance().setMainDividerPosition(n.doubleValue()));
        }
        
        host.setProjectTree(tree); host.setCanvas(canvas);
        host.updateTitle(); host.updateUndoRedo();
        if ( isNew ) { ProjectService.getInstance().newProject(rootNode, rootNode.getName()); }
    }

    public void closeCurrentProject(){
        SplitPane main = host.getMainSplit();
        if (main!=null) main.getItems().clear();
        host.setProjectTree(null); host.setCanvas(null);
        // Clear navigation history when project is closed
        navigationHistory.clear();
    }

    public void loadProjectFromPath(String absolutePath){
        if (absolutePath==null) return;
        LOGGER.debug("Loading project from {}", absolutePath);
        try {
            FDDINode rootNode = (FDDINode) FDDIXMLFileReader.read(absolutePath);
            if (rootNode == null) { host.showErrorDialog("Open Project Failed","Failed to parse the selected file."); return; }
            ProjectService.getInstance().openWithRoot(absolutePath, rootNode);
            rebuildProjectUI(rootNode, false);
            
            // Add to recent files and update preferences
            PreferencesService.getInstance().addRecentFile(absolutePath);
            PreferencesService.getInstance().setLastProjectPath(absolutePath);
            PreferencesService.getInstance().flushNow();
            
            LOGGER.info("Project loaded: {}", absolutePath);
        } catch (Exception e){ LOGGER.error("Load project failed: {}", e.getMessage(), e); host.showErrorDialog("Open Project Failed", e.getMessage()); }
    }
}
