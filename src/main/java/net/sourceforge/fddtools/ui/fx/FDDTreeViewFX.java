package net.sourceforge.fddtools.ui.fx;

import javafx.scene.control.TreeView;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.internationalization.Messages;
import com.nebulon.xml.fddi.Program;
import com.nebulon.xml.fddi.Project;
import com.nebulon.xml.fddi.Aspect;
import com.nebulon.xml.fddi.Subject;
import com.nebulon.xml.fddi.Activity;
import com.nebulon.xml.fddi.Feature;

public class FDDTreeViewFX extends TreeView<FDDINode> {

    private FDDTreeContextMenuHandler contextMenuHandler;

    public FDDTreeViewFX() {
        super();
        loadStylesheet();
        setupCellFactory();
        setupSelectionListener();
    }
    
    private void loadStylesheet() {
        try {
            String stylesheet = getClass().getResource("/net/sourceforge/fddtools/ui/fx/modern-style.css").toExternalForm();
            getStylesheets().add(stylesheet);
            getStyleClass().add("tree-view");
        } catch (Exception e) {
            System.err.println("Failed to load stylesheet: " + e.getMessage());
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
                    MenuItem addProgram = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_ADDPROGRAM_CAPTION));
                    MenuItem addProject = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_ADDPROJECT_CAPTION));
                    MenuItem editProgram = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_EDITPROGRAM_CAPTION));
                    
                    addProgram.setOnAction(e -> contextMenuHandler.addProgram(node));
                    addProject.setOnAction(e -> contextMenuHandler.addProject(node));
                    editProgram.setOnAction(e -> contextMenuHandler.editNode(node));
                    
                    contextMenu.getItems().addAll(addProgram, addProject, editProgram);
                } else if (node instanceof Project) {
                    MenuItem addAspect = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_ADDASPECT_CAPTION));
                    MenuItem editProject = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_EDITPROJECT_CAPTION));
                    MenuItem deleteProject = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_DELETEPROJECT_CAPTION));
                    
                    addAspect.setOnAction(e -> contextMenuHandler.addAspect(node));
                    editProject.setOnAction(e -> contextMenuHandler.editNode(node));
                    deleteProject.setOnAction(e -> contextMenuHandler.deleteNode(node));
                    
                    contextMenu.getItems().addAll(addAspect, editProject, deleteProject);
                } else if (node instanceof Aspect) {
                    MenuItem addSubject = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_ADDSUBJECT_CAPTION));
                    MenuItem editAspect = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_EDITASPECT_CAPTION));
                    MenuItem deleteAspect = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_DELETEASPECT_CAPTION));
                    
                    addSubject.setOnAction(e -> contextMenuHandler.addSubject(node));
                    editAspect.setOnAction(e -> contextMenuHandler.editNode(node));
                    deleteAspect.setOnAction(e -> contextMenuHandler.deleteNode(node));
                    
                    contextMenu.getItems().addAll(addSubject, editAspect, deleteAspect);
                } else if (node instanceof Subject) {
                    MenuItem addActivity = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_ADDACTIVITY_CAPTION));
                    MenuItem editSubject = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_EDITSUBJECT_CAPTION));
                    MenuItem deleteSubject = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_DELETESUBJECT_CAPTION));
                    
                    addActivity.setOnAction(e -> contextMenuHandler.addActivity(node));
                    editSubject.setOnAction(e -> contextMenuHandler.editNode(node));
                    deleteSubject.setOnAction(e -> contextMenuHandler.deleteNode(node));
                    
                    contextMenu.getItems().addAll(addActivity, editSubject, deleteSubject);
                } else if (node instanceof Activity) {
                    MenuItem addFeature = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_ADDFEATURE_CAPTION));
                    MenuItem editActivity = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_EDITACTIVITY_CAPTION));
                    MenuItem deleteActivity = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_DELETEACTIVITY_CAPTION));
                    
                    addFeature.setOnAction(e -> contextMenuHandler.addFeature(node));
                    editActivity.setOnAction(e -> contextMenuHandler.editNode(node));
                    deleteActivity.setOnAction(e -> contextMenuHandler.deleteNode(node));
                    
                    contextMenu.getItems().addAll(addFeature, editActivity, deleteActivity);
                } else if (node instanceof Feature) {
                    MenuItem editFeature = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_EDITFEATURE_CAPTION));
                    MenuItem deleteFeature = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_DELETEFEATURE_CAPTION));
                    
                    editFeature.setOnAction(e -> contextMenuHandler.editNode(node));
                    deleteFeature.setOnAction(e -> contextMenuHandler.deleteNode(node));
                    
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
        
        return item;
    }
}