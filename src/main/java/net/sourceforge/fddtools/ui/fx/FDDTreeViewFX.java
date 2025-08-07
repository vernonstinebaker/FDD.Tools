package net.sourceforge.fddtools.ui.fx;

import javafx.scene.control.TreeView;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.Scene;
import javafx.embed.swing.JFXPanel;
import javafx.application.Platform;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.internationalization.Messages;
import com.nebulon.xml.fddi.Program;
import com.nebulon.xml.fddi.Project;
import com.nebulon.xml.fddi.Aspect;
import com.nebulon.xml.fddi.Subject;
import com.nebulon.xml.fddi.Activity;
import com.nebulon.xml.fddi.Feature;
import javax.swing.SwingUtilities;

public class FDDTreeViewFX extends TreeView<FDDINode> {

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
                System.out.println("DEBUG: Applied high contrast styling to JavaFX tree");
            } else {
                // Load professional modern CSS from file
                String stylesheet = getClass().getResource("/net/sourceforge/fddtools/ui/fx/modern-style.css").toExternalForm();
                getStylesheets().add(stylesheet);
                System.out.println("DEBUG: Applied modern styling to JavaFX tree");
            }
            getStyleClass().add("tree-view");
        } catch (Exception e) {
            System.err.println("Failed to load stylesheet: " + e.getMessage());
            e.printStackTrace();
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
            @Override
            protected void updateItem(FDDINode item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setContextMenu(null);
                } else {
                    setText(item.getName());
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
                    
                    addProgram.setOnAction(e -> {
                        SwingUtilities.invokeLater(() -> contextMenuHandler.addProgram(node));
                    });
                    addProject.setOnAction(e -> {
                        SwingUtilities.invokeLater(() -> contextMenuHandler.addProject(node));
                    });
                    editProgram.setOnAction(e -> {
                        SwingUtilities.invokeLater(() -> contextMenuHandler.editNode(node));
                    });
                    
                    contextMenu.getItems().addAll(addProgram, addProject, new SeparatorMenuItem(), editProgram);
                } else if (node instanceof Project) {
                    MenuItem addAspect = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_ADDASPECT_CAPTION));
                    MenuItem editProject = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_EDITPROJECT_CAPTION));
                    MenuItem deleteProject = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_DELETEPROJECT_CAPTION));
                    
                    addAspect.setOnAction(e -> {
                        SwingUtilities.invokeLater(() -> contextMenuHandler.addAspect(node));
                    });
                    editProject.setOnAction(e -> {
                        SwingUtilities.invokeLater(() -> contextMenuHandler.editNode(node));
                    });
                    deleteProject.setOnAction(e -> {
                        SwingUtilities.invokeLater(() -> contextMenuHandler.deleteNode(node));
                    });
                    
                    contextMenu.getItems().addAll(addAspect, new SeparatorMenuItem(), editProject, deleteProject);
                } else if (node instanceof Aspect) {
                    MenuItem addSubject = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_ADDSUBJECT_CAPTION));
                    MenuItem editAspect = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_EDITASPECT_CAPTION));
                    MenuItem deleteAspect = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_DELETEASPECT_CAPTION));
                    
                    addSubject.setOnAction(e -> {
                        SwingUtilities.invokeLater(() -> contextMenuHandler.addSubject(node));
                    });
                    editAspect.setOnAction(e -> {
                        SwingUtilities.invokeLater(() -> contextMenuHandler.editNode(node));
                    });
                    deleteAspect.setOnAction(e -> {
                        SwingUtilities.invokeLater(() -> contextMenuHandler.deleteNode(node));
                    });
                    
                    contextMenu.getItems().addAll(addSubject, new SeparatorMenuItem(), editAspect, deleteAspect);
                } else if (node instanceof Subject) {
                    MenuItem addActivity = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_ADDACTIVITY_CAPTION));
                    MenuItem editSubject = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_EDITSUBJECT_CAPTION));
                    MenuItem deleteSubject = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_DELETESUBJECT_CAPTION));
                    
                    addActivity.setOnAction(e -> {
                        SwingUtilities.invokeLater(() -> contextMenuHandler.addActivity(node));
                    });
                    editSubject.setOnAction(e -> {
                        SwingUtilities.invokeLater(() -> contextMenuHandler.editNode(node));
                    });
                    deleteSubject.setOnAction(e -> {
                        SwingUtilities.invokeLater(() -> contextMenuHandler.deleteNode(node));
                    });
                    
                    contextMenu.getItems().addAll(addActivity, new SeparatorMenuItem(), editSubject, deleteSubject);
                } else if (node instanceof Activity) {
                    MenuItem addFeature = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_ADDFEATURE_CAPTION));
                    MenuItem editActivity = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_EDITACTIVITY_CAPTION));
                    MenuItem deleteActivity = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_DELETEACTIVITY_CAPTION));
                    
                    addFeature.setOnAction(e -> {
                        SwingUtilities.invokeLater(() -> contextMenuHandler.addFeature(node));
                    });
                    editActivity.setOnAction(e -> {
                        SwingUtilities.invokeLater(() -> contextMenuHandler.editNode(node));
                    });
                    deleteActivity.setOnAction(e -> {
                        SwingUtilities.invokeLater(() -> contextMenuHandler.deleteNode(node));
                    });
                    
                    contextMenu.getItems().addAll(addFeature, new SeparatorMenuItem(), editActivity, deleteActivity);
                } else if (node instanceof Feature) {
                    MenuItem editFeature = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_EDITFEATURE_CAPTION));
                    MenuItem deleteFeature = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_DELETEFEATURE_CAPTION));
                    
                    editFeature.setOnAction(e -> {
                        SwingUtilities.invokeLater(() -> contextMenuHandler.editNode(node));
                    });
                    deleteFeature.setOnAction(e -> {
                        SwingUtilities.invokeLater(() -> contextMenuHandler.deleteNode(node));
                    });
                    
                    contextMenu.getItems().addAll(editFeature, deleteFeature);
                }
                
                setContextMenu(contextMenu);
            }
        });
    }

    private void setupSelectionListener() {
        getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && contextMenuHandler != null) {
                FDDINode selectedNode = newSelection.getValue();
                System.out.println("DEBUG: JavaFX tree selection changed to: " + 
                                  (selectedNode != null ? selectedNode.getName() : "null"));
                contextMenuHandler.onSelectionChanged(selectedNode);
            }
        });
    }

    /**
     * Populates the TreeView with the given root node.
     * @param rootNode the root FDDINode of the project hierarchy
     */
    public void populateTree(FDDINode rootNode) {
        if (rootNode == null) {
            System.out.println("DEBUG: Root node is null, cannot populate tree");
            return;
        }
        
        TreeItem<FDDINode> rootItem = buildTreeItem(rootNode);
        setRoot(rootItem);
        rootItem.setExpanded(true);
        setShowRoot(true);
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
        
        if (node != null && node.getChildCount() > 0 && node.children() != null) {
            java.util.Enumeration<?> enumeration = node.children();
            while (enumeration.hasMoreElements()) {
                Object childObj = enumeration.nextElement();
                if (childObj instanceof FDDINode) {
                    FDDINode childNode = (FDDINode) childObj;
                    item.getChildren().add(buildTreeItem(childNode));
                }
            }
        }
        
        // Expand all nodes by default - only collapse when explicitly toggled
        item.setExpanded(true);
        
        return item;
    }
    
    /**
     * Creates a JFXPanel containing this TreeView for embedding in Swing applications.
     * @return A JFXPanel ready to be added to Swing containers
     */
    public JFXPanel createSwingPanel() {
        JFXPanel fxPanel = new JFXPanel();
        
        Platform.runLater(() -> {
            try {
                // Create scene with ScrollPane wrapper
                ScrollPane scrollPane = new ScrollPane(this);
                scrollPane.setFitToWidth(true);
                scrollPane.setFitToHeight(true);
                
                Scene scene = new Scene(scrollPane);
                fxPanel.setScene(scene);
                
                System.out.println("DEBUG: Created JFXPanel for TreeView with styling: " + 
                                  (useHighContrastStyling ? "high-contrast" : "modern"));
            } catch (Exception e) {
                System.err.println("ERROR: Failed to create JFXPanel for TreeView: " + e.getMessage());
                e.printStackTrace();
            }
        });
        
        return fxPanel;
    }
    
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