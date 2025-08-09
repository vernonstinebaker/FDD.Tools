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
import net.sourceforge.fddtools.model.FDDINode;
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
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.command.MoveNodeCommand;
import net.sourceforge.fddtools.command.CommandExecutionService;

public class FDDTreeViewFX extends TreeView<FDDINode> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FDDTreeViewFX.class);

    private FDDTreeContextMenuHandler contextMenuHandler;
    private boolean useHighContrastStyling = false;
    private boolean enableProgramBusinessLogic = true;

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
    }
    
    /**
     * Sets whether to use high contrast styling for accessibility.
     */
    public void setHighContrastStyling(boolean useHighContrast) {
        this.useHighContrastStyling = useHighContrast;
        loadStylesheet();
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
            private FDDINode dragSource;
            private final javafx.css.PseudoClass DROP_TARGET = javafx.css.PseudoClass.getPseudoClass("drop-target");

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

                // Drag detected
                setOnDragDetected(e -> {
                    if (getItem() == null) return;
                    dragSource = (FDDINode) getItem();
                    Dragboard db = startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(dragSource.getId() != null ? dragSource.getId() : dragSource.getName());
                    db.setContent(content);
                    e.consume();
                });
                // Drag over
                setOnDragOver(e -> {
                    if (dragSource == null || getItem() == null) { e.consume(); return; }
                    if (dragSource == getItem()) { pseudoClassStateChanged(DROP_TARGET,false); e.consume(); return; }
                    boolean ok = isValidReparent(dragSource, (FDDINode) getItem());
                    if (ok) {
                        e.acceptTransferModes(TransferMode.MOVE);
                        pseudoClassStateChanged(DROP_TARGET,true);
                    } else {
                        pseudoClassStateChanged(DROP_TARGET,false);
                    }
                    e.consume();
                });
                // Drop
                setOnDragDropped(e -> {
                    boolean success = false;
                    if (dragSource != null && getItem() != null && dragSource != getItem()) {
                        FDDINode target = (FDDINode) getItem();
                        if (isValidReparent(dragSource, target)) {
                            CommandExecutionService.getInstance().execute(new MoveNodeCommand(dragSource, target));
                            // Refresh tree after move
                            getTreeView().refresh();
                            success = true;
                        }
                    }
                    pseudoClassStateChanged(DROP_TARGET,false);
                    e.setDropCompleted(success);
                    e.consume();
                });
                setOnDragExited(e -> { pseudoClassStateChanged(DROP_TARGET,false); e.consume(); });
                setOnDragDone(e -> { dragSource = null; pseudoClassStateChanged(DROP_TARGET,false); });
            }

            private void updateHoverPseudoClass(boolean hovering) {
                // Only apply pseudo-class if hovering AND cell has non-empty item
                boolean active = hovering && !isEmpty() && getItem() != null;
                pseudoClassStateChanged(HOVERED_ROW, active);
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