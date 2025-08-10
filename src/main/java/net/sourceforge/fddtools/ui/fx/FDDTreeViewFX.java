package net.sourceforge.fddtools.ui.fx;

import javafx.scene.control.TreeView;
import javafx.application.Platform;
import javafx.scene.control.TreeCell;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.sourceforge.fddtools.state.ModelState;
import net.sourceforge.fddtools.service.LoggingService;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.input.TransferMode;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.DataFormat;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.command.MoveNodeCommand;
import net.sourceforge.fddtools.command.CommandExecutionService;

public class FDDTreeViewFX extends TreeView<FDDINode> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FDDTreeViewFX.class);
    /** Custom DataFormat for intra-tree drag recognition (optional fallback). */
    private static final DataFormat FDD_NODE_FORMAT = new DataFormat("application/x-fdd-node-id");
    private enum DropType { INTO, BEFORE, AFTER }

    private FDDTreeContextMenuHandler contextMenuHandler;
    private boolean useHighContrastStyling = false;
    private boolean enableProgramBusinessLogic = true;
    /** Drag source node tracked across cells for correct DnD lifecycle. */
    private FDDINode dragSourceNode;

    public FDDTreeViewFX() {
        this(false, true);
    }
    
    public FDDTreeViewFX(boolean useHighContrastStyling, boolean enableProgramBusinessLogic) {
        super();
        this.useHighContrastStyling = useHighContrastStyling;
        this.enableProgramBusinessLogic = enableProgramBusinessLogic;
        loadStylesheet();
        setupCellFactory();
        setupSelectionListener();
    setupKeyboardShortcuts();
    }
    
    /**
     * Sets whether to use high contrast styling for accessibility.
     */
    public void setHighContrastStyling(boolean useHighContrast) {
        this.useHighContrastStyling = useHighContrast;
        loadStylesheet();
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
                            refresh();
                            selectNode(node);
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
                                refresh();
                                selectNode(node);
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
        refresh();
        selectNode(node);
    }
    
    /**
     * Sets whether to enable Program business logic (enable/disable based on content).
     */
    public void setProgramBusinessLogic(boolean enableBusinessLogic) {
        this.enableProgramBusinessLogic = enableBusinessLogic;
    }
    
    private void loadStylesheet() {
        try {
            // Clear existing stylesheets
            getStylesheets().clear();
            
            if (useHighContrastStyling) {
                // Add high contrast inline CSS for accessibility
                String highContrastCSS = "data:text/css," + java.net.URLEncoder.encode(
                    ".tree-view .tree-cell:selected { " +
                    "    -fx-background-color: #000080 !important; " +
                    "    -fx-text-fill: white !important; " +
                    "    -fx-background-radius: 0; " +
                    "} " +
                    ".tree-view .tree-cell:focused { " +
                    "    -fx-background-color: #000060 !important; " +
                    "    -fx-text-fill: white !important; " +
                    "} " +
                    ".tree-view:focused .tree-cell:selected { " +
                    "    -fx-background-color: #000080 !important; " +
                    "    -fx-text-fill: white !important; " +
                    "}", "UTF-8"
                );
                getStylesheets().add(highContrastCSS);
                LOGGER.debug("Applied high contrast styling to JavaFX tree");
            } else {
                // Load professional modern CSS from file
                String stylesheet = getClass().getResource("/net/sourceforge/fddtools/ui/fx/modern-style.css").toExternalForm();
                getStylesheets().add(stylesheet);
                LOGGER.debug("Applied modern styling to JavaFX tree");
                // Force inline style override for selection (last resort if external CSS loses specificity)
                setStyle("-fx-selection-bar: #ff8a33; -fx-accent: #ff8a33;");
            }
            getStyleClass().add("tree-view");
        } catch (Exception e) {
            LOGGER.warn("Failed to load stylesheet: {}", e.getMessage());
        }
    }

    /**
     * Sets the context menu handler for this tree.
     */
    public void setContextMenuHandler(FDDTreeContextMenuHandler handler) {
        this.contextMenuHandler = handler;
    }

    private void setupCellFactory() {
        // Set up cell factory to display FDDINode names and handle context menus
        setCellFactory(tv -> new TreeCell<FDDINode>() {
            private final javafx.css.PseudoClass HOVERED_ROW = javafx.css.PseudoClass.getPseudoClass("row-hover");
            private final HBox container = new HBox(4);
            private final FontAwesomeIconView iconView = new FontAwesomeIconView();
            private final Label nameLabel = new Label();
            private final ProgressBar progress = new ProgressBar();
            private final javafx.css.PseudoClass DROP_TARGET = javafx.css.PseudoClass.getPseudoClass("drop-target");
            private final javafx.css.PseudoClass INSERT_BEFORE = javafx.css.PseudoClass.getPseudoClass("drop-insert-before");
            private final javafx.css.PseudoClass INSERT_AFTER = javafx.css.PseudoClass.getPseudoClass("drop-insert-after");
            private DropType currentDropType;
            private PauseTransition expandDelay; // used to auto-expand collapsed parents while dragging

            {
                // Add listeners for hover to apply only when non-empty
                hoverProperty().addListener((obs, was, isNow) -> updateHoverPseudoClass(isNow));
                itemProperty().addListener((obs, oldItem, newItem) -> updateHoverPseudoClass(isHover()));
                emptyProperty().addListener((obs, wasEmpty, isEmpty) -> updateHoverPseudoClass(isHover()));
                progress.setPrefWidth(60);
                progress.setMaxWidth(60);
                progress.setVisible(false); // placeholder until per-node progress implemented
                HBox.setHgrow(nameLabel, Priority.ALWAYS);
                container.getChildren().addAll(iconView, nameLabel, progress);

                // Drag detected: begin move with snapshot drag view and store source centrally
                setOnDragDetected(e -> {
                    FDDINode item = getItem();
                    if (item == null) return;
                    ((FDDTreeViewFX) getTreeView()).dragSourceNode = item;
                    Dragboard db = startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    String id = item.getId() != null ? item.getId() : item.getName();
                    content.put(FDD_NODE_FORMAT, id);
                    content.putString(id);
                    db.setContent(content);
                    try {
                        var img = snapshot(null, null);
                        if (img != null) db.setDragView(img, img.getWidth() / 4, img.getHeight() / 2);
                    } catch (Exception ex) {
                        LOGGER.debug("Snapshot for drag view failed: {}", ex.getMessage());
                    }
                    e.consume();
                });
                // Drag over
                setOnDragOver(e -> {
                    FDDINode dragSource = ((FDDTreeViewFX) getTreeView()).dragSourceNode;
                    if (dragSource == null || getItem() == null) { e.consume(); return; }
                    if (dragSource == getItem()) { pseudoClassStateChanged(DROP_TARGET,false); e.consume(); return; }
                    boolean okInto = isValidReparent(dragSource, (FDDINode) getItem());
                    // Determine drop intent based on cursor Y position relative to cell height
                    DropType dropType = DropType.INTO;
                    double y = e.getY();
                    double h = getHeight() <= 0 ? 24 : getHeight();
                    if (y < h * 0.25) dropType = DropType.BEFORE; else if (y > h * 0.75) dropType = DropType.AFTER; else dropType = DropType.INTO;
                    boolean ok = switch (dropType) {
                        case INTO -> okInto;
                        case BEFORE, AFTER -> canInsertSibling(dragSource, (FDDINode) getItem());
                    };
                    if (ok) {
                        e.acceptTransferModes(TransferMode.MOVE);
                        applyDropPseudoClasses(dropType, true);
                        scheduleAutoExpand();
                    } else {
                        clearDropPseudoClasses();
                        cancelAutoExpand();
                    }
                    currentDropType = ok ? dropType : null;
                    e.consume();
                });
                // Drop
                setOnDragDropped(e -> {
                    boolean success = false;
                    FDDINode dragSource = ((FDDTreeViewFX) getTreeView()).dragSourceNode;
                    if (dragSource != null && getItem() != null && dragSource != getItem()) {
                        FDDINode targetNode = (FDDINode) getItem();
                        DropType dt = currentDropType == null ? DropType.INTO : currentDropType;
                        switch (dt) {
                            case INTO -> {
                                if (isValidReparent(dragSource, targetNode)) {
                                    CommandExecutionService.getInstance().execute(new MoveNodeCommand(dragSource, targetNode));
                                    success = true;
                                }
                            }
                            case BEFORE, AFTER -> {
                                FDDINode parent = (FDDINode) targetNode.getParentNode();
                                if (parent != null && canInsertSibling(dragSource, targetNode)) {
                                    int targetIndex = parent.getChildren().indexOf(targetNode);
                                    if (dt == DropType.AFTER) targetIndex += 1;
                                    CommandExecutionService.getInstance().execute(new MoveNodeCommand(dragSource, parent, targetIndex));
                                    success = true;
                                }
                            }
                        }
                        if (!success) {
                            showTransientTooltip(this, invalidReason(dragSource, targetNode, dt));
                            LOGGER.debug("Invalid drop {} from {} to {}", dt, dragSource.getName(), targetNode.getName());
                        }
                    }
                    clearDropPseudoClasses();
                    e.setDropCompleted(success);
                    if (success) getTreeView().refresh();
                    e.consume();
                });
                setOnDragEntered(e -> {
                    FDDINode dragSource = ((FDDTreeViewFX) getTreeView()).dragSourceNode;
                    if (dragSource != null && dragSource != getItem() && getItem()!=null) {
                        // rely on dragOver logic; here just schedule expand if potential container
                        if (isValidReparent(dragSource, getItem())) scheduleAutoExpand();
                    }
                    e.consume();
                });
                setOnDragExited(e -> { clearDropPseudoClasses(); cancelAutoExpand(); e.consume(); });
                setOnDragDone(e -> { ((FDDTreeViewFX) getTreeView()).dragSourceNode = null; clearDropPseudoClasses(); cancelAutoExpand(); });
            }

            private void updateHoverPseudoClass(boolean hovering) {
                // Only apply pseudo-class if hovering AND cell has non-empty item
                boolean active = hovering && !isEmpty() && getItem() != null;
                pseudoClassStateChanged(HOVERED_ROW, active);
            }

            private boolean canInsertSibling(FDDINode dragSource, FDDINode reference) {
                if (dragSource == null || reference == null) return false;
                FDDINode parent = (FDDINode) reference.getParentNode();
                if (parent == null) return false; // cannot insert around root
                if (dragSource == reference) return false;
                if (dragSource.getParentNode() == parent && parent.getChildren().size() == 1) return false; // no change
                return hierarchyAccepts(parent, dragSource) && !isDescendant(reference, dragSource);
            }

            private void applyDropPseudoClasses(DropType type, boolean active) {
                clearDropPseudoClasses();
                if (!active) return;
                switch (type) {
                    case INTO -> pseudoClassStateChanged(DROP_TARGET, true);
                    case BEFORE -> pseudoClassStateChanged(INSERT_BEFORE, true);
                    case AFTER -> pseudoClassStateChanged(INSERT_AFTER, true);
                }
            }
            private void clearDropPseudoClasses() {
                pseudoClassStateChanged(DROP_TARGET,false);
                pseudoClassStateChanged(INSERT_BEFORE,false);
                pseudoClassStateChanged(INSERT_AFTER,false);
            }

            private String invalidReason(FDDINode dragSource, FDDINode target, DropType type) {
                if (type == DropType.INTO && !isValidReparent(dragSource, target)) return "Cannot move under this target (hierarchy rule)";
                if ((type == DropType.BEFORE || type == DropType.AFTER) && !canInsertSibling(dragSource, target)) return "Cannot reorder here";
                return "Invalid drop";
            }

            private void showTransientTooltip(TreeCell<FDDINode> cell, String text) {
                if (text == null || text.isBlank()) return;
                Tooltip tip = new Tooltip(text);
                Tooltip.install(cell, tip);
                PauseTransition hide = new PauseTransition(Duration.seconds(1.5));
                hide.setOnFinished(ev -> Tooltip.uninstall(cell, tip));
                hide.play();
            }

            private void scheduleAutoExpand() {
                if (expandDelay != null) return; // already scheduled
                if (getTreeItem() == null) return;
                if (getTreeItem().isExpanded()) return;
                expandDelay = new PauseTransition(Duration.millis(600));
                expandDelay.setOnFinished(ev -> {
                    try {
                        if (getTreeItem()!=null && !getTreeItem().isExpanded()) getTreeItem().setExpanded(true);
                    } catch (Exception ex) {
                        LOGGER.debug("Auto-expand failed: {}", ex.getMessage());
                    } finally {
                        expandDelay = null;
                    }
                });
                expandDelay.play();
            }

            private void cancelAutoExpand() {
                if (expandDelay != null) {
                    expandDelay.stop();
                    expandDelay = null;
                }
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
    private boolean isValidReparent(FDDINode child, FDDINode newParent) {
        if (child == null || newParent == null) return false;
        // Disallow moving root or into its own descendant
        if (child.getParentNode() == null) return false;
        if (isDescendant(newParent, child)) return false;
        return hierarchyAccepts(newParent, child);
    }

    private boolean hierarchyAccepts(FDDINode parent, FDDINode child) {
        // Mirror creation/paste logic: Program->Program/Project (exclusive already enforced by UI), Project->Aspect, Aspect->Subject, Subject->Activity, Activity->Feature
        if (parent instanceof Program) {
            boolean typeAllowed = (child instanceof Program) || (child instanceof Project);
            if (!typeAllowed) return false;
            if (enableProgramBusinessLogic) {
                Program prog = (Program) parent;
                int programChildren = prog.getProgram() == null ? 0 : prog.getProgram().size();
                int projectChildren = prog.getProject() == null ? 0 : prog.getProject().size();
                if (child instanceof Program && projectChildren > 0) return false; // exclusivity: can't add Program when Projects exist
                if (child instanceof Project && programChildren > 0) return false; // exclusivity: can't add Project when Programs exist
            }
            return true;
        }
        if (parent instanceof Project) return (child instanceof Aspect);
        if (parent instanceof Aspect) return (child instanceof Subject);
        if (parent instanceof Subject) return (child instanceof Activity);
        if (parent instanceof Activity) return (child instanceof Feature);
        return false;
    }

    private boolean isDescendant(FDDINode candidateParent, FDDINode potentialChild) {
        FDDINode p = (FDDINode) candidateParent.getParentNode();
        while (p != null) {
            if (p == potentialChild) return true;
            p = (FDDINode) p.getParentNode();
        }
        return false;
    }

    private void setupSelectionListener() {
        getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && contextMenuHandler != null) {
                FDDINode selectedNode = newSelection.getValue();
                String name = selectedNode != null ? selectedNode.getName() : "null";
                if (LOGGER.isTraceEnabled()) LOGGER.trace("Tree selection changed to: {}", name);
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
            LOGGER.debug("Root node is null, cannot populate tree");
            return;
        }
        
        TreeItem<FDDINode> rootItem = buildTreeItem(rootNode);
        setRoot(rootItem);
        rootItem.setExpanded(true);
        setShowRoot(true);
    // Re-apply stylesheet after root assignment to ensure highest precedence
    loadStylesheet();
    getStyleClass().add("fdd-tree-view");
    if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Tree populated and stylesheet reloaded (classes={})", getStyleClass());
    }
    }

    /**
     * Refreshes the tree by repopulating it from the current root.
     */
    public void refresh() {
        if (getRoot() != null) {
            FDDINode rootNode = getRoot().getValue();
            populateTree(rootNode);
        }
    }

    /**
     * Recursively builds a TreeItem hierarchy from the FDDINode hierarchy.
     */
    private TreeItem<FDDINode> buildTreeItem(FDDINode node) {
        TreeItem<FDDINode> item = new TreeItem<>(node);
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
    public void selectNode(FDDINode nodeToSelect) {
        if (nodeToSelect != null && getRoot() != null) {
            TreeItem<FDDINode> itemToSelect = findTreeItem(getRoot(), nodeToSelect);
            if (itemToSelect != null) {
                getSelectionModel().select(itemToSelect);
                // Ensure the selected item is visible
                scrollTo(getSelectionModel().getSelectedIndex());
            }
        }
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
}