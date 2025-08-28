package net.sourceforge.fddtools.ui.fx;

import javafx.scene.control.TreeView;
import javafx.scene.control.ScrollBar;
import javafx.application.Platform;
import javafx.scene.control.TreeCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.control.ProgressBar;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import net.sourceforge.fddtools.internationalization.Messages;
import com.nebulon.xml.fddi.Program;
import com.nebulon.xml.fddi.Project;
import com.nebulon.xml.fddi.Aspect;
import com.nebulon.xml.fddi.Subject;
import com.nebulon.xml.fddi.Activity;
import com.nebulon.xml.fddi.Feature;
import net.sourceforge.fddtools.state.ModelState;
import java.util.HashMap;
import java.util.Map;
import java.util.IdentityHashMap;
// Drag & drop specific imports removed (handled by FDDTreeDragAndDropController)
import javafx.scene.input.KeyEvent;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.command.MoveNodeCommand;
import net.sourceforge.fddtools.command.CommandExecutionService;
import net.sourceforge.fddtools.service.LoggingService;

public class FDDTreeViewFX extends TreeView<FDDINode> {
    // DnD DataFormat & enum moved to controller
    private FDDTreeContextMenuHandler contextMenuHandler;
    private boolean enableProgramBusinessLogic = true;
    /** Drag source tracked for DnD (package visibility for controller). */
    FDDINode dragSourceNode;
    private FDDTreeDragAndDropController dndController;
    /** Fast lookup from domain node identity to its TreeItem for incremental updates. */
    private final Map<FDDINode, TreeItem<FDDINode>> nodeItemIndex = new IdentityHashMap<>();
    /** Flag to suppress automatic scrolling during drag and drop operations */
    private boolean suppressAutoScroll = false;

    public FDDTreeViewFX() {
        this(true);
    }

    /**
     * @param enableProgramBusinessLogic whether to apply program add/disable business logic
     */
    public FDDTreeViewFX(boolean enableProgramBusinessLogic) {
        super();
        this.enableProgramBusinessLogic = enableProgramBusinessLogic;
        // Semantic theming: rely on scene-level semantic + variant stylesheets, just add role classes
        getStyleClass().addAll("fdd-tree", "selection-accent-orange");
        dndController = new FDDTreeDragAndDropController(this);
        setupCellFactory();
        setupSelectionListener();
        setupKeyboardShortcuts();
        setupTreeViewDragHandlers(); // Setup TreeView-level drag detection for edge scrolling
        // Lazy diagnostic hook once added to a scene
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> {
                    try {
                        // Force baseline theme application to inject semantic stylesheet if not present
                        net.sourceforge.fddtools.service.ThemeService.getInstance().applyThemeTo(newScene, net.sourceforge.fddtools.service.ThemeService.Theme.SYSTEM);
                    } catch (Exception ignored) {}
                });
            }
        });
    }

    /**
     * Deprecated no-op retained for binary compatibility. High contrast is now handled globally by ThemeService variants.
     */
    @Deprecated(forRemoval = false)
    public void setHighContrastStyling(boolean ignored) {
        // High contrast handled by ThemeService variant styles
    }

    private void setupKeyboardShortcuts() {
        addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (!(e.isAltDown())) return; // use Alt as modifier to avoid clashing with built-ins
            TreeItem<FDDINode> selected = getSelectionModel().getSelectedItem();
            if (selected == null) return;
            FDDINode node = selected.getValue();
            FDDINode parent = (FDDINode) node.getParentNode();
            switch (e.getCode()) {
                case UP -> { // Alt+Up: move earlier among siblings
                    if (parent != null) reorderWithinParent(node, parent, -1);
                }
                case DOWN -> { // Alt+Down: move later among siblings
                    if (parent != null) reorderWithinParent(node, parent, +1);
                }
                case LEFT -> { // Alt+Left: reparent to grandparent (pull out)
                    if (parent != null && parent.getParentNode() != null) {
                        FDDINode grand = (FDDINode) parent.getParentNode();
                        if (hierarchyAccepts(grand, node)) {
                            int idx = grand.getChildren().indexOf(parent); // insert before parent
                            CommandExecutionService.getInstance().execute(new MoveNodeCommand(node, grand, idx));
                            updateAfterMove(node, grand, idx);
                        }
                    }
                }
                case RIGHT -> { // Alt+Right: if previous sibling can accept node, nest inside it
                    if (parent != null) {
                        int idx = parent.getChildren().indexOf(node);
                        if (idx > 0) {
                            FDDINode prevSibling = (FDDINode) parent.getChildren().get(idx - 1);
                            if (hierarchyAccepts(prevSibling, node)) {
                                CommandExecutionService.getInstance().execute(new MoveNodeCommand(node, prevSibling));
                                updateAfterMove(node, prevSibling, -1); // append inside previous sibling
                            }
                        }
                    }
                }
                default -> { return; }
            }
            e.consume();
        });
    }

    private void reorderWithinParent(FDDINode node, FDDINode parent, int delta) {
        int currentIndex = parent.getChildren().indexOf(node);
        if (currentIndex < 0) return;
        int newIndex = currentIndex + delta;
        if (newIndex < 0 || newIndex >= parent.getChildren().size()) return; // boundary
    CommandExecutionService.getInstance().execute(new MoveNodeCommand(node, parent, newIndex));
    updateAfterMove(node, parent, newIndex);
    }
    
    /**
     * Setup TreeView-level drag handlers to detect edge scrolling when dragging
     * outside of visible cells (below last item or above first item).
     */
    private void setupTreeViewDragHandlers() {
        // Handle drag over events at the TreeView level to catch edge cases
        // where the user drags outside the bounds of visible cells
        setOnDragOver(e -> {
            // Only handle if this is our internal drag operation
            if (dragSourceNode != null && e.getDragboard().hasContent(FDDTreeDragAndDropController.FDD_NODE_FORMAT)) {
                e.acceptTransferModes(javafx.scene.input.TransferMode.MOVE);
                // Delegate to the drag controller for edge scrolling detection
                dndController.handleTreeViewDragOver(e);
                e.consume();
            }
        });
    }
    
    /**
     * Sets whether to enable Program business logic (enable/disable based on content).
     */
    public void setProgramBusinessLogic(boolean enableBusinessLogic) {
        this.enableProgramBusinessLogic = enableBusinessLogic;
    }
    // Package-visible accessor for internal controllers/tests.
    boolean isProgramBusinessLogicEnabled(){ return enableProgramBusinessLogic; }
    
    // Previous loadStylesheet removed; semantic + variant styles apply via ThemeService.

    /**
     * Sets the context menu handler for this tree.
     */
    public void setContextMenuHandler(FDDTreeContextMenuHandler handler) {
        this.contextMenuHandler = handler;
    }

    private void setupCellFactory() {
    // Track currently hovered cell explicitly (some platforms unreliable with hoverProperty timing)
    final TreeCell<?>[] currentHoverRef = new TreeCell<?>[1];
        // Set up cell factory to display FDDINode names and handle context menus
        setCellFactory(tv -> new TreeCell<FDDINode>() {
            private final javafx.css.PseudoClass HOVERED_ROW = javafx.css.PseudoClass.getPseudoClass("row-hover");
            private final HBox container = new HBox(4);
            private final FontAwesomeIconView iconView = new FontAwesomeIconView();
            private final Label nameLabel = new Label();
            private final ProgressBar progress = new ProgressBar();
            // Inline hover fallback removed after stabilization
            // Drop pseudo-classes now applied by controller; only hover tracked here

            {
                // Removed reactive hoverProperty listeners (caused immediate clearing on some platforms)
                // Explicit mouse enter/exit to enforce a single hovered cell regardless of hoverProperty race conditions
                // Re-introduce a lightweight hoverProperty listener for redundancy & diagnostics.
                hoverProperty().addListener((o, was, isNow) -> {
                    if (isNow && !isEmpty() && getItem()!=null && !isSelected()) {
                        updateHoverPseudoClass(true);
                    } else if (!isNow) {
                        updateHoverPseudoClass(false);
                    }
                });
                addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
                    if (!isEmpty() && getItem() != null && !isSelected()) {
                        TreeCell<?> prev = currentHoverRef[0];
                        if (prev != this && prev != null) {
                            prev.pseudoClassStateChanged(HOVERED_ROW, false);
                            prev.getStyleClass().remove("fdd-hover-active");
                            if (prev.getProperties().containsKey("__origStyle")) {
                                Object os = prev.getProperties().remove("__origStyle");
                                if (os instanceof String) prev.setStyle((String) os);
                            }
                        }
                        currentHoverRef[0] = this;
                        updateHoverPseudoClass(true);
                    }
                });
                addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
                    if (currentHoverRef[0] == this) {
                        updateHoverPseudoClass(false);
                        currentHoverRef[0] = null;
                    }
                });
                getStyleClass().add("fdd-tree-cell"); // debug/style hook
                // Retain simple handlers for tests that invoke getOnMouseEntered directly
                setOnMouseEntered(e -> updateHoverPseudoClass(true));
                setOnMouseExited(e -> updateHoverPseudoClass(false));
                progress.setPrefWidth(60);
                progress.setMaxWidth(60);
                progress.setVisible(false); // placeholder until per-node progress implemented
                HBox.setHgrow(nameLabel, Priority.ALWAYS);
                container.getChildren().addAll(iconView, nameLabel, progress);

                // Attach DnD behavior via controller
                dndController.attachTo(this);
            }

            private void updateHoverPseudoClass(boolean hovering) {
                boolean active = hovering && !isEmpty() && getItem() != null;
                pseudoClassStateChanged(HOVERED_ROW, active);
                boolean inlineDisabled = Boolean.getBoolean("fdd.hover.inlineDisabled");
                if (active) {
                    if (!getStyleClass().contains("fdd-hover-active")) getStyleClass().add("fdd-hover-active");
                    if (!inlineDisabled && !isSelected()) {
                        setStyle("-fx-background-color: linear-gradient(to right, rgba(255,138,51,0.62), rgba(255,138,51,0.36)); -fx-border-color:#ff8a33; -fx-border-width:0 0 0 4;");
                    }
                } else {
                    getStyleClass().remove("fdd-hover-active");
                    if (!inlineDisabled && !isSelected()) setStyle("");
                }
                // logging removed after stabilization
            }
            @Override
            protected void updateItem(FDDINode item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setContextMenu(null);
                    // ensure hover style removed if cell becomes empty
                    pseudoClassStateChanged(HOVERED_ROW, false);
                    setStyle("");
                } else {
                    setText(null);
                    nameLabel.setText(item.getName());
                    // Choose icon by type
                    if (item instanceof Program) iconView.setIcon(FontAwesomeIcon.BOOK);
                    else if (item instanceof Project) iconView.setIcon(FontAwesomeIcon.FOLDER);
                    else if (item instanceof Aspect) iconView.setIcon(FontAwesomeIcon.CUBES);
                    else if (item instanceof Subject) iconView.setIcon(FontAwesomeIcon.TAGS);
                    else if (item instanceof Activity) iconView.setIcon(FontAwesomeIcon.TASKS);
                    else if (item instanceof Feature) iconView.setIcon(FontAwesomeIcon.STAR);
                    iconView.setGlyphSize(14);
                    setGraphic(container);
                    setupContextMenu(item);
                    if (isSelected()) setStyle("");
                }
            }

            private void setupContextMenu(FDDINode node) {
                if (contextMenuHandler == null) return;

                ContextMenu contextMenu = new ContextMenu();
                
                if (node instanceof Program) {
                    Program program = (Program) node;
                    MenuItem addProgram = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_ADDPROGRAM_CAPTION));
                    MenuItem addProject = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_ADDPROJECT_CAPTION));
                    MenuItem editProgram = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_EDITPROGRAM_CAPTION));
                    
                    // Apply Program business logic if enabled
                    if (enableProgramBusinessLogic) {
                        addProgram.setDisable(program.getProject().size() != 0);
                        addProject.setDisable(program.getProgram().size() != 0);
                    }
                    
                    addProgram.setOnAction(e -> Platform.runLater(() -> contextMenuHandler.addProgram(node)));
                    addProject.setOnAction(e -> Platform.runLater(() -> contextMenuHandler.addProject(node)));
                    editProgram.setOnAction(e -> Platform.runLater(() -> contextMenuHandler.editNode(node)));
                    
                    contextMenu.getItems().addAll(addProgram, addProject, new SeparatorMenuItem(), editProgram);
                } else if (node instanceof Project) {
                    MenuItem addAspect = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_ADDASPECT_CAPTION));
                    MenuItem editProject = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_EDITPROJECT_CAPTION));
                    MenuItem deleteProject = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_DELETEPROJECT_CAPTION));
                    
                    addAspect.setOnAction(e -> Platform.runLater(() -> contextMenuHandler.addAspect(node)));
                    editProject.setOnAction(e -> Platform.runLater(() -> contextMenuHandler.editNode(node)));
                    deleteProject.setOnAction(e -> Platform.runLater(() -> contextMenuHandler.deleteNode(node)));
                    
                    contextMenu.getItems().addAll(addAspect, new SeparatorMenuItem(), editProject, deleteProject);
                } else if (node instanceof Aspect) {
                    MenuItem addSubject = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_ADDSUBJECT_CAPTION));
                    MenuItem editAspect = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_EDITASPECT_CAPTION));
                    MenuItem deleteAspect = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_DELETEASPECT_CAPTION));
                    
                    addSubject.setOnAction(e -> Platform.runLater(() -> contextMenuHandler.addSubject(node)));
                    editAspect.setOnAction(e -> Platform.runLater(() -> contextMenuHandler.editNode(node)));
                    deleteAspect.setOnAction(e -> Platform.runLater(() -> contextMenuHandler.deleteNode(node)));
                    
                    contextMenu.getItems().addAll(addSubject, new SeparatorMenuItem(), editAspect, deleteAspect);
                } else if (node instanceof Subject) {
                    MenuItem addActivity = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_ADDACTIVITY_CAPTION));
                    MenuItem editSubject = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_EDITSUBJECT_CAPTION));
                    MenuItem deleteSubject = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_DELETESUBJECT_CAPTION));
                    
                    addActivity.setOnAction(e -> Platform.runLater(() -> contextMenuHandler.addActivity(node)));
                    editSubject.setOnAction(e -> Platform.runLater(() -> contextMenuHandler.editNode(node)));
                    deleteSubject.setOnAction(e -> Platform.runLater(() -> contextMenuHandler.deleteNode(node)));
                    
                    contextMenu.getItems().addAll(addActivity, new SeparatorMenuItem(), editSubject, deleteSubject);
                } else if (node instanceof Activity) {
                    MenuItem addFeature = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_ADDFEATURE_CAPTION));
                    MenuItem editActivity = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_EDITACTIVITY_CAPTION));
                    MenuItem deleteActivity = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_DELETEACTIVITY_CAPTION));
                    
                    addFeature.setOnAction(e -> Platform.runLater(() -> contextMenuHandler.addFeature(node)));
                    editActivity.setOnAction(e -> Platform.runLater(() -> contextMenuHandler.editNode(node)));
                    deleteActivity.setOnAction(e -> Platform.runLater(() -> contextMenuHandler.deleteNode(node)));
                    
                    contextMenu.getItems().addAll(addFeature, new SeparatorMenuItem(), editActivity, deleteActivity);
                } else if (node instanceof Feature) {
                    MenuItem editFeature = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_EDITFEATURE_CAPTION));
                    MenuItem deleteFeature = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_DELETEFEATURE_CAPTION));
                    
                    editFeature.setOnAction(e -> Platform.runLater(() -> contextMenuHandler.editNode(node)));
                    deleteFeature.setOnAction(e -> Platform.runLater(() -> contextMenuHandler.deleteNode(node)));
                    
                    contextMenu.getItems().addAll(editFeature, deleteFeature);
                }
                
                setContextMenu(contextMenu);
            }
        });
    }

    // Validate same hierarchy rules used for paste: only allow adding under legal parent type
    boolean isValidReparent(FDDINode child, FDDINode newParent) { return FDDHierarchyRules.isValidReparent(child, newParent, enableProgramBusinessLogic); }
    boolean hierarchyAccepts(FDDINode parent, FDDINode child) { return FDDHierarchyRules.hierarchyAccepts(parent, child, enableProgramBusinessLogic); }
    boolean isDescendant(FDDINode candidateParent, FDDINode potentialChild) { return FDDHierarchyRules.isDescendant(candidateParent, potentialChild); }

    private void setupSelectionListener() {
        getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && contextMenuHandler != null) {
                FDDINode selectedNode = newSelection.getValue();
                String name = selectedNode != null ? selectedNode.getName() : "null";
                // Update global model state
                ModelState.getInstance().setSelectedNode(selectedNode);
                // MDC context for selection
                Map<String,String> ctx = new HashMap<>();
                ctx.put("action", "selectNode");
                ctx.put("selectedNode", name);
                LoggingService.getInstance().withContext(ctx, () -> {
                    if (contextMenuHandler != null) contextMenuHandler.onSelectionChanged(selectedNode);
                });
            }
        });
    }

    /**
     * Populates the TreeView with the given root node.
     * @param rootNode the root FDDINode of the project hierarchy
     */
    public void populateTree(FDDINode rootNode) {
        if (rootNode == null) {
            return;
        }
        
    nodeItemIndex.clear();
    TreeItem<FDDINode> rootItem = buildTreeItem(rootNode);
        setRoot(rootItem);
        rootItem.setExpanded(true);
        setShowRoot(true);
    // Re-apply stylesheet after root assignment to ensure highest precedence
        // loadStylesheet(); // Removed invalid call
    getStyleClass().add("fdd-tree-view");
    }

    /**
     * Refreshes the tree by repopulating it from the current root.
     */
    public void refresh() { // legacy full rebuild
        if (getRoot() == null) return;
        // Preserve expansion state & selection
        FDDINode selected = getSelectedNode();
        Map<FDDINode, Boolean> expanded = snapshotExpansion();
        FDDINode rootNode = getRoot().getValue();
        populateTree(rootNode);
        restoreExpansion(expanded);
        if (selected != null) selectNode(selected);
    }

    /**
     * Recursively builds a TreeItem hierarchy from the FDDINode hierarchy.
     */
    private TreeItem<FDDINode> buildTreeItem(FDDINode node) {
        TreeItem<FDDINode> item = new TreeItem<>(node);
        nodeItemIndex.put(node, item);
    if (node != null && !node.getChildren().isEmpty()) {
            for (net.sourceforge.fddtools.model.FDDTreeNode tn : node.getChildren()) {
                FDDINode childNode = (FDDINode) tn; // transitional cast
                item.getChildren().add(buildTreeItem(childNode));
            }
        }
        item.setExpanded(true); // expand by default
        return item;
    }
    
    // Removed Swing embedding (JFXPanel) for pure JavaFX deployment
    
    /**
     * Gets the currently selected node.
     * @return The selected FDDINode or null if nothing is selected
     */
    public FDDINode getSelectedNode() {
        TreeItem<FDDINode> selectedItem = getSelectionModel().getSelectedItem();
        return selectedItem != null ? selectedItem.getValue() : null;
    }
    
    /**
     * Selects a specific node in the tree.
     * @param nodeToSelect The node to select
     */
    public void selectNode(FDDINode nodeToSelect) { selectNode(nodeToSelect, true); }

    /** Internal selection helper with optional scrolling. */
    void selectNode(FDDINode nodeToSelect, boolean scroll) {
        if (nodeToSelect != null && getRoot() != null) {
            TreeItem<FDDINode> itemToSelect = findTreeItem(getRoot(), nodeToSelect);
            if (itemToSelect != null) {
                if (suppressAutoScroll) {
                    // During drag and drop, use a different approach to prevent scrolling
                    // Instead of trying to restore scroll position after selection causes scrolling,
                    // we'll temporarily override the scrollTo method completely
                    
                    // Set a flag to indicate we're in critical selection mode
                    boolean wasAlreadySuppressing = suppressAutoScroll;
                    suppressAutoScroll = true; // This will make our overridden scrollTo() method return immediately
                    
                    try {
                        // Perform selection - any internal scrollTo() calls will be suppressed
                        getSelectionModel().select(itemToSelect);
                    } finally {
                        // Restore the suppression state
                        suppressAutoScroll = wasAlreadySuppressing;
                    }
                    
                } else {
                    // Normal selection behavior
                    getSelectionModel().select(itemToSelect);
                    if (scroll) {
                        // Ensure the selected item is visible
                        scrollTo(getSelectionModel().getSelectedIndex());
                    }
                }
            }
        }
    }

    @Override
    public void scrollTo(int index) {
        // Suppress automatic scrolling during drag and drop operations
        if (suppressAutoScroll) {
            return;
        }
        super.scrollTo(index);
    }
    
    @Override
    protected void layoutChildren() {
        // During drag and drop operations, preserve scroll position to prevent auto-scrolling
        if (suppressAutoScroll) {
            ScrollBar vbar = null;
            double savedPosition = -1;
            try {
                var bars = lookupAll(".scroll-bar");
                for (var n : bars) {
                    if (n instanceof ScrollBar sb && sb.getOrientation() == javafx.geometry.Orientation.VERTICAL) {
                        vbar = sb;
                        savedPosition = sb.getValue();
                        break;
                    }
                }
            } catch (Exception ignored) {}
            
            // Call parent layout
            super.layoutChildren();
            
            // Immediately restore scroll position if it was changed by layout or any other operation
            if (vbar != null && savedPosition >= 0) {
                final ScrollBar finalVbar = vbar;
                final double finalPosition = savedPosition;
                // Immediate restoration
                if (Math.abs(finalVbar.getValue() - finalPosition) > 0.001) {
                    finalVbar.setValue(finalPosition);
                }
                // Also schedule delayed restoration in case other operations interfere
                Platform.runLater(() -> {
                    try {
                        if (Math.abs(finalVbar.getValue() - finalPosition) > 0.001) {
                            finalVbar.setValue(finalPosition);
                        }
                    } catch (Exception ignored) {}
                });
            }
        } else {
            super.layoutChildren();
        }
    }
    
    @Override
    public void requestFocus() {
        // During drag and drop, prevent focus changes that might trigger scrolling
        if (!suppressAutoScroll) {
            super.requestFocus();
        }
    }

    /** Package-visible method to control auto-scroll suppression during drag and drop */
    void setSuppressAutoScroll(boolean suppress) {
        this.suppressAutoScroll = suppress;
    }
    
    /**
     * Recursively searches for a TreeItem containing the target node.
     */
    private TreeItem<FDDINode> findTreeItem(TreeItem<FDDINode> parent, FDDINode target) {
        if (parent.getValue() == target) {
            return parent;
        }
        
        for (TreeItem<FDDINode> child : parent.getChildren()) {
            TreeItem<FDDINode> result = findTreeItem(child, target);
            if (result != null) {
                return result;
            }
        }
        
        return null;
    }

    /** Incrementally update UI after a MoveNodeCommand to avoid full tree rebuild. */
    void updateAfterMove(FDDINode node, FDDINode newParent, int newIndex) {
    if (node == null || newParent == null) { /* fallback */ refresh(); return; }
    // Guard against illegal self-parenting or descendant cycles (should not occur if command validated)
    if (node == newParent) { return; }
    if (isDescendant(newParent, node)) { return; }
        TreeItem<FDDINode> item = nodeItemIndex.get(node);
        TreeItem<FDDINode> newParentItem = nodeItemIndex.get(newParent);
    if (item == null || newParentItem == null) { /* fallback */ refresh(); return; }
    TreeItem<FDDINode> oldParentItem = item.getParent();
    var oldList = oldParentItem != null ? oldParentItem.getChildren() : null;
    var newList = newParentItem.getChildren();
    TreeMoveHelper.move(oldList, newList, item, newIndex);
    // ensure mapping consistent (parent unchanged) and expand new parent
        newParentItem.setExpanded(true);
    // During drag and drop, delay selection to avoid auto-scrolling - the drag controller will handle scroll restoration
    if (suppressAutoScroll) {
        // Don't select immediately - let the drag controller restore scroll position first
        Platform.runLater(() -> {
            Platform.runLater(() -> {
                if (!suppressAutoScroll) { // Only select if suppression has been turned off
                    selectNode(node, false);
                }
            });
        });
    } else {
        // Normal behavior - select the moved node
        selectNode(node, false);
    }
    announceStatus("Moved '"+node.getName()+"'");
    }

    private Map<FDDINode, Boolean> snapshotExpansion(){
        Map<FDDINode, Boolean> map = new IdentityHashMap<>();
        snapshotExpansionRec(getRoot(), map);
        return map;
    }
    private void snapshotExpansionRec(TreeItem<FDDINode> item, Map<FDDINode, Boolean> map){
        if (item == null) return; map.put(item.getValue(), item.isExpanded());
        for (TreeItem<FDDINode> child : item.getChildren()) snapshotExpansionRec(child, map);
    }
    private void restoreExpansion(Map<FDDINode, Boolean> state){
        if (state==null) return; restoreExpansionRec(getRoot(), state);
    }
    private void restoreExpansionRec(TreeItem<FDDINode> item, Map<FDDINode, Boolean> state){
        if (item==null) return; Boolean exp = state.get(item.getValue()); if (exp!=null) item.setExpanded(exp);
        for (TreeItem<FDDINode> c: item.getChildren()) restoreExpansionRec(c, state);
    }

    // Hook for status announcements (status bar / accessibility). Overridden by layout controller injection.
    void announceStatus(String message){ /* default no-op; wired externally */ }
}