package net.sourceforge.fddtools.ui;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.Component;
import javax.swing.JSplitPane;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import net.sourceforge.fddtools.model.FDDINode;

/**
 * Manages tree view switching between Swing and JavaFX implementations
 */
public class TreeManager {
    
    private final FDDFrame parentFrame;
    private JTree projectTree;
    private javafx.scene.control.TreeView<FDDINode> projectTreeFX;
    private boolean useJavaFXTree = false;
    private JScrollPane currentTreePane;
    
    public TreeManager(FDDFrame parentFrame) {
        this.parentFrame = parentFrame;
    }
    
    public boolean isUsingJavaFXTree() {
        return useJavaFXTree;
    }
    
    public void setUseJavaFXTree(boolean useJavaFXTree) {
        this.useJavaFXTree = useJavaFXTree;
    }
    
    public JTree getProjectTree() {
        return projectTree;
    }
    
    public void setProjectTree(JTree projectTree) {
        this.projectTree = projectTree;
    }
    
    public javafx.scene.control.TreeView<FDDINode> getProjectTreeFX() {
        return projectTreeFX;
    }
    
    public void setProjectTreeFX(javafx.scene.control.TreeView<FDDINode> projectTreeFX) {
        this.projectTreeFX = projectTreeFX;
    }
    
    public JScrollPane getCurrentTreePane() {
        return currentTreePane;
    }
    
    public void setCurrentTreePane(JScrollPane currentTreePane) {
        this.currentTreePane = currentTreePane;
    }
    
    public void switchToSwingTree() {
        useJavaFXTree = false;
        System.out.println("DEBUG: Switching to Swing tree view");
        
        // Get current root data if available
        FDDINode rootNode = getCurrentRootNode();
        
        if (rootNode != null) {
            // Create new Swing tree
            JTree tree = new JTree(new DefaultTreeModel((TreeNode) rootNode));
            tree.setRootVisible(true);
            
            // Replace the tree in the UI
            replaceTreeWithSwing(tree);
        }
    }
    
    public void switchToJavaFXTree() {
        useJavaFXTree = true;
        System.out.println("DEBUG: Switching to JavaFX tree view");
        
        // Get current root data
        FDDINode rootNode = getCurrentRootNode();
        
        if (rootNode != null) {
            System.out.println("DEBUG: Root node found: " + rootNode.toString());
            replaceTreeWithJavaFX(rootNode);
        } else {
            System.err.println("ERROR: No root node found for JavaFX tree");
        }
    }
    
    public FDDINode getCurrentRootNode() {
        if (projectTree != null) {
            return (FDDINode) projectTree.getModel().getRoot();
        } else if (projectTreeFX != null && projectTreeFX.getRoot() != null) {
            return projectTreeFX.getRoot().getValue();
        }
        return null;
    }
    
    public FDDINode getCurrentSelectedNode() {
        if (useJavaFXTree && projectTreeFX != null) {
            javafx.scene.control.TreeItem<FDDINode> selectedItem = projectTreeFX.getSelectionModel().getSelectedItem();
            return selectedItem != null ? selectedItem.getValue() : null;
        } else if (projectTree != null && projectTree.getSelectionPath() != null) {
            return (FDDINode) projectTree.getSelectionPath().getLastPathComponent();
        }
        return null;
    }
    
    public void reset() {
        projectTree = null;
        projectTreeFX = null;
        useJavaFXTree = false;
        currentTreePane = null;
    }
    
    private void replaceTreeWithSwing(JTree newTree) {
        try {
            // Set up new Swing tree
            projectTree = newTree;
            projectTree.setRootVisible(true);
            projectTree.putClientProperty("JTree.lineStyle", "Angled");
            
            // Create scroll pane
            JScrollPane newTreePane = new JScrollPane(projectTree);
            newTreePane.setWheelScrollingEnabled(true);
            
            // Set up selection model
            DefaultTreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
            selectionModel.addTreeSelectionListener(parentFrame.getFddCanvasView());
            selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            projectTree.setSelectionModel(selectionModel);
            
            // Add mouse listeners for context menus
            projectTree.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(final MouseEvent e) {
                    if(e.isPopupTrigger()) {
                        parentFrame.showTreeCtxMenu(e.getComponent(), e.getX(), e.getY());
                    }
                }
                @Override
                public void mouseReleased(final MouseEvent e) {
                    mousePressed(e);
                }
            });
            
            // Find and replace the left component of the split pane
            Component[] components = parentFrame.getContentPane().getComponents();
            for (Component comp : components) {
                if (comp instanceof JSplitPane) {
                    JSplitPane splitPane = (JSplitPane) comp;
                    splitPane.setLeftComponent(newTreePane);
                    currentTreePane = newTreePane;
                    break;
                }
            }
            
            parentFrame.validate();
            parentFrame.repaint();
            
            System.out.println("DEBUG: Successfully switched to Swing tree");
            
        } catch (Exception e) {
            System.err.println("ERROR: Failed to replace with Swing tree: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void replaceTreeWithJavaFX(FDDINode rootNode) {
        try {
            Platform.runLater(() -> {
                try {
                    // Create JavaFX tree
                    projectTreeFX = new javafx.scene.control.TreeView<>();
                    javafx.scene.control.TreeItem<FDDINode> root = createTreeItem(rootNode);
                    projectTreeFX.setRoot(root);
                    projectTreeFX.setShowRoot(true);
                    
                    // Set up selection listener
                    projectTreeFX.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal != null && parentFrame.getFddCanvasView() != null) {
                            // Update canvas view
                            javax.swing.SwingUtilities.invokeLater(() -> {
                                try {
                                    // Create a TreeSelectionEvent-like update for the canvas
                                    FDDINode selectedNode = newVal.getValue();
                                    System.out.println("DEBUG: JavaFX tree selection changed to: " + selectedNode.toString());
                                    parentFrame.getFddCanvasView().valueChanged(null);
                                    parentFrame.getFddCanvasView().repaint();
                                } catch (Exception e) {
                                    System.err.println("ERROR: Failed to update canvas view: " + e.getMessage());
                                }
                            });
                        }
                    });
                    
                    // Create JFXPanel to embed in Swing
                    JFXPanel fxTreePanel = new JFXPanel();
                    
                    // Create scene with high contrast styling
                    javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(projectTreeFX);
                    scrollPane.setFitToWidth(true);
                    scrollPane.setFitToHeight(true);
                    
                    javafx.scene.Scene scene = new javafx.scene.Scene(scrollPane);
                    
                    // Add high contrast CSS
                    scene.getStylesheets().add("data:text/css," + java.net.URLEncoder.encode(
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
                    ));
                    
                    fxTreePanel.setScene(scene);
                    
                    // Replace in Swing EDT
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        JScrollPane newTreePane = new JScrollPane();
                        newTreePane.setViewportView(fxTreePanel);
                        newTreePane.setWheelScrollingEnabled(true);
                        
                        // Find and replace the left component of the split pane
                        Component[] components = parentFrame.getContentPane().getComponents();
                        for (Component comp : components) {
                            if (comp instanceof JSplitPane) {
                                JSplitPane splitPane = (JSplitPane) comp;
                                splitPane.setLeftComponent(newTreePane);
                                currentTreePane = newTreePane;
                                break;
                            }
                        }
                        
                        parentFrame.validate();
                        parentFrame.repaint();
                        
                        System.out.println("DEBUG: Successfully switched to JavaFX tree");
                        
                        // Auto-select root node (equivalent to Swing's setSelectionRow(0))
                        Platform.runLater(() -> {
                            if (projectTreeFX.getRoot() != null) {
                                projectTreeFX.getSelectionModel().select(projectTreeFX.getRoot());
                                System.out.println("DEBUG: JavaFX tree root node auto-selected");
                            }
                        });
                    });
                    
                } catch (Exception e) {
                    System.err.println("ERROR: Failed to create JavaFX tree: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
        } catch (Exception e) {
            System.err.println("ERROR: Failed to replace with JavaFX tree: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private javafx.scene.control.TreeItem<FDDINode> createTreeItem(FDDINode node) {
        javafx.scene.control.TreeItem<FDDINode> item = new javafx.scene.control.TreeItem<>(node);
        item.setExpanded(true);
        
        // Add children recursively
        for (int i = 0; i < node.getChildCount(); i++) {
            FDDINode child = (FDDINode) node.getChildAt(i);
            item.getChildren().add(createTreeItem(child));
        }
        
        return item;
    }
}
