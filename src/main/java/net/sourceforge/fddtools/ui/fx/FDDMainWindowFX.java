/**
 * JavaFX Main Window for FDD Tools.
 * This class replaces the Swing-based FDDFrame with a pure JavaFX implementation.
 */
package net.sourceforge.fddtools.ui.fx;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.ui.FDDOptionEvent;
import net.sourceforge.fddtools.ui.FDDOptionListener;
import net.sourceforge.fddtools.ui.FDDOptionModel;
import net.sourceforge.fddtools.ui.bridge.DialogBridgeFX;
import net.sourceforge.fddtools.ui.fx.AspectInfoPanelFX;
import net.sourceforge.fddtools.ui.fx.WorkPackagePanelFX;
import net.sourceforge.fddtools.ui.fx.FDDTreeContextMenuHandler;
import net.sourceforge.fddtools.ui.fx.FDDActionPanelFX;
import net.sourceforge.fddtools.persistence.FDDIXMLFileReader;
import net.sourceforge.fddtools.persistence.FDDIXMLFileWriter;
import net.sourceforge.fddtools.ui.ExtensionFileFilter;
import com.nebulon.xml.fddi.ObjectFactory;
import com.nebulon.xml.fddi.Program;
import com.nebulon.xml.fddi.Project;
import com.nebulon.xml.fddi.Aspect;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.nebulon.xml.fddi.Subject;
import com.nebulon.xml.fddi.Activity;
import com.nebulon.xml.fddi.Feature;

import net.sourceforge.fddtools.util.ObjectCloner;
import com.nebulon.xml.fddi.Feature;

import java.awt.Font;
import java.io.File;
import java.util.Optional;
import java.util.List;
import java.util.logging.Logger;

public class FDDMainWindowFX extends BorderPane implements FDDOptionListener, FDDTreeContextMenuHandler {
    private static final Logger LOGGER = Logger.getLogger(FDDMainWindowFX.class.getName());
    
    // Core components
    private final Stage primaryStage;
    private FDDOptionModel options;
    private FDDINode clipboard;
    private boolean uniqueNodeVersion = false; // Track if clipboard node has unique version numbers
    private String currentProject; // Display name for title bar
    private String currentProjectPath; // Full path for saving
    private boolean modelDirty = false;
    
    // UI Components
    private MenuBar menuBar;
    private ToolBar toolBar;
    private SplitPane mainSplitPane;
    private SplitPane rightSplitPane; // For canvas and panels
    private TabPane infoPanelContainer; // Container for aspect and work package panels
    private FDDTreeViewFX projectTreeFX;
    private FDDCanvasFX canvasFX;
    private VBox statusBar;
    
    // Menu items that need to be enabled/disabled
    private MenuItem fileSave;
    private MenuItem fileSaveAs;
    private MenuItem editCut;
    private MenuItem editCopy;
    private MenuItem editPaste;
    private MenuItem editDelete;
    private MenuItem editEdit;
    
    public FDDMainWindowFX(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // Initialize options
        options = new FDDOptionModel();
        options.addFDDOptionListener(this);
        
        // Setup macOS integration FIRST
        setupMacOSIntegration();
        
        // Build the UI
        initializeComponents();
        layoutComponents();
        
        // Create new project
        newProject();
        
        LOGGER.info("FDDMainWindowFX initialized successfully");
    }
    
    private void setupMacOSIntegration() {
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            try {
                // Set up JavaFX-compatible macOS integration
                boolean success = MacOSHandlerFX.setupMacOSHandlers(this, primaryStage);
                if (success) {
                    LOGGER.info("macOS Desktop API handlers set up successfully");
                } else {
                    LOGGER.warning("Some macOS handlers could not be set");
                }
            } catch (Exception e) {
                LOGGER.warning("Failed to setup macOS integration: " + e.getMessage());
            }
        }
    }
    
    private void initializeComponents() {
        // Create menu bar
        createMenuBar();
        
        // Create toolbar
        createToolBar();
        
        // Create main split pane
        mainSplitPane = new SplitPane();
        mainSplitPane.setDividerPositions(0.25); // 25% for tree, 75% for right side
        
        // Create right split pane for canvas and info panels
        rightSplitPane = new SplitPane();
        rightSplitPane.setOrientation(javafx.geometry.Orientation.VERTICAL);
        rightSplitPane.setDividerPositions(0.7); // 70% for canvas, 30% for info panels
        
        // Create info panel container with tabs
        createInfoPanelContainer();
        
        // Create status bar
        createStatusBar();
    }
    
    private void layoutComponents() {
        // Set up the main layout
        setTop(new VBox(menuBar, toolBar));
        setCenter(mainSplitPane);
        setBottom(statusBar);
    }
    
    private void createMenuBar() {
        menuBar = new MenuBar();
        
        // macOS specific: Use system menu bar - properties already set in FDDApplicationFX static block
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            menuBar.setUseSystemMenuBar(true);
            LOGGER.info("Configured JavaFX MenuBar to use system menu bar on macOS");
        }
        
        // File Menu
        Menu fileMenu = new Menu("File");
        
        MenuItem fileNew = new MenuItem("New");
        fileNew.setAccelerator(KeyCombination.keyCombination("Shortcut+N"));
        fileNew.setOnAction(e -> newProject());
        
        MenuItem fileOpen = new MenuItem("Open...");
        fileOpen.setAccelerator(KeyCombination.keyCombination("Shortcut+O"));
        fileOpen.setOnAction(e -> openProject());
        
        fileSave = new MenuItem("Save");
        fileSave.setAccelerator(KeyCombination.keyCombination("Shortcut+S"));
        fileSave.setOnAction(e -> saveProject());
        fileSave.setDisable(true);
        
        fileSaveAs = new MenuItem("Save As...");
        fileSaveAs.setAccelerator(KeyCombination.keyCombination("Shortcut+Shift+S"));
        fileSaveAs.setOnAction(e -> saveProjectAs());
        fileSaveAs.setDisable(true);
        
        MenuItem fileExit = new MenuItem("Exit");
        fileExit.setOnAction(e -> exitApplication());
        
        fileMenu.getItems().addAll(
            fileNew, fileOpen, new SeparatorMenuItem(),
            fileSave, fileSaveAs, new SeparatorMenuItem(),
            fileExit
        );
        
        // Edit Menu
        Menu editMenu = new Menu("Edit");
        
        editCut = new MenuItem("Cut");
        editCut.setAccelerator(KeyCombination.keyCombination("Shortcut+X"));
        editCut.setOnAction(e -> cutSelectedNode());
        editCut.setDisable(true);
        
        editCopy = new MenuItem("Copy");
        editCopy.setAccelerator(KeyCombination.keyCombination("Shortcut+C"));
        editCopy.setOnAction(e -> copySelectedNode());
        editCopy.setDisable(true);
        
        editPaste = new MenuItem("Paste");
        editPaste.setAccelerator(KeyCombination.keyCombination("Shortcut+V"));
        editPaste.setOnAction(e -> pasteNode());
        editPaste.setDisable(true);
        
        editDelete = new MenuItem("Delete");
        editDelete.setAccelerator(KeyCombination.keyCombination("Delete"));
        editDelete.setOnAction(e -> deleteSelectedNode());
        editDelete.setDisable(true);
        
        editEdit = new MenuItem("Edit...");
        editEdit.setAccelerator(KeyCombination.keyCombination("Shortcut+E"));
        editEdit.setOnAction(e -> editSelectedNode());
        editEdit.setDisable(true);
        
        MenuItem editPreferences = new MenuItem("Preferences...");
        editPreferences.setOnAction(e -> showPreferencesDialog());
        
        editMenu.getItems().addAll(
            editCut, editCopy, editPaste, new SeparatorMenuItem(),
            editDelete, editEdit, new SeparatorMenuItem(),
            editPreferences
        );
        
        // View Menu
        Menu viewMenu = new Menu("View");
        
        MenuItem viewRefresh = new MenuItem("Refresh");
        viewRefresh.setAccelerator(KeyCombination.keyCombination("F5"));
        viewRefresh.setOnAction(e -> refreshView());
        
        viewMenu.getItems().add(viewRefresh);
        
        // Help Menu
        Menu helpMenu = new Menu("Help");
        
        MenuItem helpAbout = new MenuItem("About FDD Tools");
        helpAbout.setOnAction(e -> showAboutDialog());
        
        helpMenu.getItems().add(helpAbout);
        
        // Add menus to menu bar
        menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu, helpMenu);
    }
    
    private void createToolBar() {
        toolBar = new ToolBar();
        
        // File operations only - remove redundant edit operations since they're in the action bar
        Button newBtn = new Button("New");
        newBtn.setOnAction(e -> newProject());
        
        Button openBtn = new Button("Open");
        openBtn.setOnAction(e -> openProject());
        
        Button saveBtn = new Button("Save");
        saveBtn.setOnAction(e -> saveProject());
        
        // Keep copy/paste in toolbar since they're commonly used
        Button cutBtn = new Button("Cut");
        cutBtn.setOnAction(e -> cutSelectedNode());
        
        Button copyBtn = new Button("Copy");
        copyBtn.setOnAction(e -> copySelectedNode());
        
        Button pasteBtn = new Button("Paste");
        pasteBtn.setOnAction(e -> pasteNode());
        
        // Remove Delete and Edit buttons from toolbar since they're in the action bar
        toolBar.getItems().addAll(
            newBtn, openBtn, saveBtn, new Separator(),
            cutBtn, copyBtn, pasteBtn
        );
    }
    
    private void createStatusBar() {
        statusBar = new VBox();
        statusBar.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 2px 5px;");
        
        // Use the existing FDDActionPanelFX instead of simple buttons
        FDDActionPanelFX actionPanel = new FDDActionPanelFX();
        actionPanel.setActionHandler(new FDDActionPanelFX.FDDActionHandler() {
            @Override
            public void onAdd() {
                FDDINode selectedNode = getSelectedNode();
                if (selectedNode != null) {
                    // Add appropriate child based on selected node type
                    if (selectedNode instanceof Program) {
                        // For Program nodes, should show menu (handled by button click logic)
                        addProject(selectedNode); // Default to project
                    } else if (selectedNode instanceof Project) {
                        addAspect(selectedNode);
                    } else if (selectedNode instanceof Aspect) {
                        addSubject(selectedNode);
                    } else if (selectedNode instanceof Subject) {
                        addActivity(selectedNode);
                    } else if (selectedNode instanceof Activity) {
                        addFeature(selectedNode);
                    }
                }
            }
            
            @Override
            public void onDelete() {
                FDDINode selectedNode = getSelectedNode();
                if (selectedNode != null) {
                    deleteNode(selectedNode);
                }
            }
            
            @Override
            public void onEdit() {
                FDDINode selectedNode = getSelectedNode();
                if (selectedNode != null) {
                    editNode(selectedNode);
                }
            }
            
            @Override
            public void onAddProgram() {
                FDDINode selectedNode = getSelectedNode();
                if (selectedNode != null) {
                    addProgram(selectedNode);
                }
            }
            
            @Override
            public void onAddProject() {
                FDDINode selectedNode = getSelectedNode();
                if (selectedNode != null) {
                    addProject(selectedNode);
                }
            }
            
            @Override
            public FDDINode getSelectedNode() {
                return FDDMainWindowFX.this.getSelectedNode();
            }
        });
        
        Label statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-font-size: 12px;");
        
        statusBar.getChildren().addAll(actionPanel, statusLabel);
    }
    
    private void createInfoPanelContainer() {
        infoPanelContainer = new TabPane();
        infoPanelContainer.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        infoPanelContainer.setPrefHeight(250);
        infoPanelContainer.setVisible(false); // Hidden by default until a relevant node is selected
        
        // Container starts empty - tabs will be added dynamically based on node selection
    }
    
    private void newProject() {
        // Create new project logic
        Platform.runLater(() -> {
            try {
                // Create root node
                FDDINode rootNode = createNewRootNode();
                
                // Create and setup tree
                if (projectTreeFX != null) {
                    mainSplitPane.getItems().remove(projectTreeFX);
                }
                projectTreeFX = new FDDTreeViewFX(true, true); // high contrast, enable program logic
                projectTreeFX.setContextMenuHandler(this); // Set this as the context menu handler
                projectTreeFX.populateTree(rootNode);
                
                // Select the root node by default
                if (projectTreeFX.getRoot() != null) {
                    projectTreeFX.getSelectionModel().select(projectTreeFX.getRoot());
                }
                
                // Add selection listener
                projectTreeFX.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        FDDINode selectedNode = newSelection.getValue();
                        onTreeSelectionChanged(selectedNode);
                    }
                });
                
                // Create and setup canvas  
                if (canvasFX != null) {
                    mainSplitPane.getItems().remove(canvasFX);
                }
                // Convert AWT Font to JavaFX Font
                Font awtFont = options.getTextFont();
                javafx.scene.text.Font fxFont = javafx.scene.text.Font.font(
                    awtFont.getName(), 
                    awtFont.getSize()
                );
                canvasFX = new FDDCanvasFX(rootNode, fxFont);
                
                // Add to split pane
                // Set up the new split pane layout
                rightSplitPane.getItems().clear();
                rightSplitPane.getItems().add(canvasFX);
                // Only add info panel container if it has content and is visible
                if (infoPanelContainer.isVisible()) {
                    rightSplitPane.getItems().add(infoPanelContainer);
                    rightSplitPane.setDividerPositions(0.7); // 70% canvas, 30% panels
                } else {
                    rightSplitPane.setDividerPositions(1.0); // 100% canvas
                }
                
                mainSplitPane.getItems().clear();
                mainSplitPane.getItems().addAll(projectTreeFX, rightSplitPane);
                
                // Reset state
                currentProject = "New Program";
                currentProjectPath = null;
                modelDirty = false;
                updateTitle();
                updateMenuStates();
                
                LOGGER.info("New project created");
                
            } catch (Exception e) {
                LOGGER.severe("Failed to create new project: " + e.getMessage());
                showErrorDialog("Failed to create new project", e.getMessage());
            }
        });
    }
    
    private FDDINode createNewRootNode() {
        try {
            ObjectFactory factory = new ObjectFactory();
            Program program = factory.createProgram();
            program.setName("New Program");
            return (FDDINode) program;
        } catch (Exception e) {
            LOGGER.severe("Failed to create new root node: " + e.getMessage());
            return null;
        }
    }
    
    private void updateMenuStates() {
        boolean hasSelection = getSelectedNode() != null;
        boolean hasClipboard = clipboard != null;
        
        Platform.runLater(() -> {
            editCut.setDisable(!hasSelection);
            editCopy.setDisable(!hasSelection);
            editPaste.setDisable(!hasClipboard);
            editDelete.setDisable(!hasSelection);
            editEdit.setDisable(!hasSelection);
            
            fileSave.setDisable(!modelDirty);
            fileSaveAs.setDisable(currentProject == null);
        });
    }
    
    private void updateTitle() {
        Platform.runLater(() -> {
            String title = "FDD Tools";
            if (currentProject != null) {
                title += " - " + currentProject;
                if (modelDirty) {
                    title += " *";
                }
            }
            primaryStage.setTitle(title);
        });
    }
    
    // Action methods
    private void openProject() {
        Platform.runLater(() -> {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Open FDD Project");
                fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("FDD Files", "*.fddi", "*.xml"),
                    new FileChooser.ExtensionFilter("All Files", "*.*")
                );
                
                File selectedFile = fileChooser.showOpenDialog(primaryStage);
                if (selectedFile != null) {
                    // Actually load the file using the existing XML reader
                    try {
                        FDDINode rootNode = (FDDINode) FDDIXMLFileReader.read(selectedFile.getAbsolutePath());
                        if (rootNode != null) {
                            // Close current project first
                            closeCurrentProject();
                            
                            // Set up the new project
                            currentProject = selectedFile.getName(); // Display name
                            currentProjectPath = selectedFile.getAbsolutePath(); // Full path for saving
                            
                            // Create and setup tree with loaded data
                            projectTreeFX = new FDDTreeViewFX(true, true);
                            projectTreeFX.setContextMenuHandler(this);
                            projectTreeFX.populateTree(rootNode);
                            
                            // Select the root node
                            if (projectTreeFX.getRoot() != null) {
                                projectTreeFX.getSelectionModel().select(projectTreeFX.getRoot());
                            }
                            
                            // Add selection listener
                            projectTreeFX.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                                if (newSelection != null) {
                                    FDDINode selectedNode = newSelection.getValue();
                                    onTreeSelectionChanged(selectedNode);
                                }
                            });
                            
                            // Create and setup canvas
                            Font awtFont = options.getTextFont();
                            javafx.scene.text.Font fxFont = javafx.scene.text.Font.font(
                                awtFont.getName(), 
                                awtFont.getSize()
                            );
                            canvasFX = new FDDCanvasFX(rootNode, fxFont);
                            
                            // Set up the layout
                            rightSplitPane.getItems().clear();
                            rightSplitPane.getItems().add(canvasFX);
                            // Only add info panel container if it has content and is visible
                            if (infoPanelContainer.isVisible()) {
                                rightSplitPane.getItems().add(infoPanelContainer);
                                rightSplitPane.setDividerPositions(0.7); // 70% canvas, 30% panels
                            } else {
                                rightSplitPane.setDividerPositions(1.0); // 100% canvas
                            }
                            
                            mainSplitPane.getItems().clear();
                            mainSplitPane.getItems().addAll(projectTreeFX, rightSplitPane);
                            
                            // Update state
                            modelDirty = false;
                            updateTitle();
                            updateMenuStates();
                            
                            LOGGER.info("Successfully opened project: " + selectedFile.getAbsolutePath());
                        } else {
                            showErrorDialog("Open Project Failed", "Failed to parse the selected file.");
                        }
                    } catch (Exception e) {
                        LOGGER.severe("Failed to load project file: " + e.getMessage());
                        showErrorDialog("Open Project Failed", "Error loading file: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                LOGGER.severe("Failed to open project: " + e.getMessage());
                showErrorDialog("Open Project Failed", e.getMessage());
            }
        });
    }
    
    private void closeCurrentProject() {
        // Clear the split pane contents
        if (mainSplitPane != null) {
            mainSplitPane.getItems().clear();
        }
        if (rightSplitPane != null) {
            rightSplitPane.getItems().clear();
        }
        
        projectTreeFX = null;
        canvasFX = null;
        currentProject = null;
        currentProjectPath = null;
        modelDirty = false;
    }
    
    private void saveProject() {
        if (currentProjectPath != null && !currentProjectPath.equals("New Program")) {
            // Save to existing file using full path
            saveToFile(currentProjectPath);
        } else {
            // No file selected, do Save As
            saveProjectAs();
        }
    }
    
    private void saveProjectAs() {
        Platform.runLater(() -> {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save FDD Project");
                fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("FDD Files", "*.fddi"),
                    new FileChooser.ExtensionFilter("XML Files", "*.xml"),
                    new FileChooser.ExtensionFilter("All Files", "*.*")
                );
                
                // Set default filename
                if (currentProject != null && !currentProject.equals("New Program")) {
                    fileChooser.setInitialFileName(currentProject);
                } else {
                    fileChooser.setInitialFileName("New Program.fddi");
                }
                
                File selectedFile = fileChooser.showSaveDialog(primaryStage);
                if (selectedFile != null) {
                    String filePath = selectedFile.getAbsolutePath();
                    if (saveToFile(filePath)) {
                        currentProject = selectedFile.getName(); // Display name
                        currentProjectPath = filePath; // Full path for saving
                        updateTitle();
                        updateMenuStates();
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to save project: " + e.getMessage());
                showErrorDialog("Save Project Failed", e.getMessage());
            }
        });
    }
    
    private boolean saveToFile(String fileName) {
        try {
            // Get root node from the tree
            Object rootNode = null;
            if (projectTreeFX != null && projectTreeFX.getRoot() != null) {
                rootNode = projectTreeFX.getRoot().getValue();
            }
            
            if (rootNode == null) {
                showErrorDialog("Save Error", "No project data to save.");
                return false;
            }
            
            System.out.println("DEBUG: About to save to file: " + fileName);
            System.out.println("DEBUG: Root node type: " + (rootNode != null ? rootNode.getClass().getName() : "null"));
            
            // Debug: Print tree structure before saving
            if (rootNode instanceof Program) {
                Program program = (Program) rootNode;
                System.out.println("DEBUG: Program name: " + program.getName());
                System.out.println("DEBUG: Number of projects: " + (program.getProject() != null ? program.getProject().size() : 0));
                if (program.getProject() != null && !program.getProject().isEmpty()) {
                    for (Project project : program.getProject()) {
                        System.out.println("DEBUG:   Project: " + project.getName());
                        if (project.getAspect() != null) {
                            for (Aspect aspect : project.getAspect()) {
                                System.out.println("DEBUG:     Aspect: " + aspect.getName());
                                if (aspect.getSubject() != null) {
                                    for (Subject subject : aspect.getSubject()) {
                                        System.out.println("DEBUG:       Subject: " + subject.getName());
                                        if (subject.getActivity() != null) {
                                            for (Activity activity : subject.getActivity()) {
                                                System.out.println("DEBUG:         Activity: " + activity.getName());
                                                if (activity.getFeature() != null) {
                                                    System.out.println("DEBUG:           Features count: " + activity.getFeature().size());
                                                    for (Feature feature : activity.getFeature()) {
                                                        System.out.println("DEBUG:           Feature: " + feature.getName());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Check if file exists before save
            File file = new File(fileName);
            long beforeTime = file.exists() ? file.lastModified() : 0;
            long beforeSize = file.exists() ? file.length() : 0;
            System.out.println("DEBUG: File exists before save: " + file.exists());
            System.out.println("DEBUG: File size before save: " + beforeSize);
            System.out.println("DEBUG: Last modified before save: " + beforeTime);
            
            // FDDINode implements the JAXB objects directly, so rootNode should be the Program object
            // Use the existing FDDIXMLFileWriter to save
            boolean success = FDDIXMLFileWriter.write(rootNode, fileName);
            
            // Check file after save
            long afterTime = file.exists() ? file.lastModified() : 0;
            long afterSize = file.exists() ? file.length() : 0;
            System.out.println("DEBUG: File exists after save: " + file.exists());
            System.out.println("DEBUG: File size after save: " + afterSize);
            System.out.println("DEBUG: Last modified after save: " + afterTime);
            System.out.println("DEBUG: File timestamp changed: " + (afterTime > beforeTime));
            System.out.println("DEBUG: FDDIXMLFileWriter.write returned: " + success);
            
            if (success) {
                modelDirty = false;
                System.out.println("Successfully saved project to: " + fileName);
                
                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Save Successful");
                alert.setHeaderText("Project Saved");
                alert.setContentText("Project successfully saved to:\n" + fileName);
                alert.showAndWait();
                
                return true;
            } else {
                showErrorDialog("Save Error", "Failed to save the project file.");
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("Error saving project: " + e.getMessage());
            e.printStackTrace();
            showErrorDialog("Save Error", "An error occurred while saving: " + e.getMessage());
            return false;
        }
    }
    
    private void cutSelectedNode() {
        FDDINode selected = getSelectedNode();
        if (selected != null) {
            // Use deep copy like the Swing version
            clipboard = (FDDINode) ObjectCloner.deepClone(selected);
            uniqueNodeVersion = false;
            
            if (clipboard != null) {
                // TODO: Implement actual removal from tree (for now just copy like the original)
                updateMenuStates();
                LOGGER.info("Cut node: " + selected.getClass().getSimpleName());
            } else {
                LOGGER.severe("Failed to create deep copy for cut operation: " + selected.getClass().getSimpleName());
                showErrorDialog("Cut Error", "Failed to cut the selected node.");
            }
        }
    }
    
    private void copySelectedNode() {
        FDDINode selected = getSelectedNode();
        if (selected != null) {
            // Use deep copy from ObjectCloner to properly clone the entire node structure
            clipboard = (FDDINode) ObjectCloner.deepClone(selected);
            uniqueNodeVersion = false;
            
            if (clipboard != null) {
                updateMenuStates();
                // Enable paste
                editPaste.setDisable(false);
                LOGGER.info("Copied node: " + selected.getClass().getSimpleName());
            } else {
                LOGGER.severe("Failed to create deep copy of node: " + selected.getClass().getSimpleName());
                showErrorDialog("Copy Error", "Failed to copy the selected node.");
            }
        }
    }
    
    private void pasteNode() {
        if (clipboard != null) {
            FDDINode selected = getSelectedNode();
            if (selected != null) {
                try {
                    // Create a deep copy of the clipboard to paste
                    FDDINode newNode = (FDDINode) ObjectCloner.deepClone(clipboard);
                    
                    if (newNode != null) {
                        // Update feature sequence numbers if not unique version
                        if (!uniqueNodeVersion) {
                            List<Feature> features = newNode.getFeaturesForNode();
                            for (Feature feature : features) {
                                feature.setSeq(feature.getNextSequence());
                            }
                        }
                        
                        // Add the new node to the selected parent
                        selected.add(newNode);
                        uniqueNodeVersion = false;
                        
                        // Calculate progress for the new node
                        newNode.calculateProgress();
                        
                        // Refresh the tree and select the new node
                        if (projectTreeFX != null) {
                            projectTreeFX.refresh();
                            projectTreeFX.selectNode(newNode);
                        }
                        
                        // Refresh canvas
                        if (canvasFX != null) {
                            canvasFX.redraw();
                        }
                        
                        // Mark model as dirty
                        modelDirty = true;
                        updateTitle();
                        updateMenuStates();
                        
                        LOGGER.info("Pasted node: " + clipboard.getClass().getSimpleName());
                    } else {
                        showErrorDialog("Paste Error", "Failed to create copy of clipboard content.");
                    }
                    
                } catch (ClassCastException e) {
                    LOGGER.warning("Cannot paste node here: " + e.getMessage());
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Paste Error");
                    alert.setHeaderText("Cannot paste node");
                    alert.setContentText("Unable to paste this type of node here.");
                    alert.showAndWait();
                } catch (Exception e) {
                    LOGGER.severe("Failed to paste node: " + e.getMessage());
                    showErrorDialog("Paste Error", "An error occurred while pasting: " + e.getMessage());
                }
            }
        }
    }
    
    private void deleteSelectedNode() {
        FDDINode selected = getSelectedNode();
        if (selected != null) {
            // Show confirmation dialog
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Node");
                alert.setHeaderText("Delete " + selected.getClass().getSimpleName());
                alert.setContentText("Are you sure you want to delete this node?");
                
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        // TODO: Implement actual deletion in next phase
                        modelDirty = true;
                        updateTitle();
                        updateMenuStates();
                        LOGGER.info("Would delete node: " + selected.getClass().getSimpleName());
                    }
                });
            });
        }
    }
    
    private void editSelectedNode() {
        FDDINode selected = getSelectedNode();
        if (selected != null) {
            DialogBridgeFX.showElementDialog(primaryStage, selected, accepted -> {
                if (accepted) {
                    modelDirty = true;
                    updateTitle();
                    updateMenuStates();
                }
            });
        }
    }
    
    private FDDINode getSelectedNode() {
        if (projectTreeFX != null) {
            return projectTreeFX.getSelectedNode();
        }
        return null;
    }
    
    private void onTreeSelectionChanged(FDDINode selectedNode) {
        // Update canvas with selected node
        if (canvasFX != null && selectedNode != null) {
            canvasFX.setCurrentNode(selectedNode);
        }
        
        // Update info panels based on selected node type
        updateInfoPanels(selectedNode);
        
        // Update menu states
        updateMenuStates();
    }
    
    private void updateInfoPanels(FDDINode selectedNode) {
        // NOTE: AspectInfoPanel and WorkPackagePanel should ONLY appear in element dialogs,
        // not in the main window. This matches the Swing implementation where these panels
        // are only created within FDDElementDialog, never in FDDFrame.
        // 
        // The main window should only show the tree and canvas, with specialized panels
        // appearing only when editing specific node types through dialogs.
        
        // Hide the panel container since we're not showing any info panels in main window
        if (infoPanelContainer.isVisible()) {
            infoPanelContainer.setVisible(false);
            rightSplitPane.getItems().remove(infoPanelContainer);
            rightSplitPane.setDividerPositions(1.0); // 100% canvas
        }
    }
    
    private void refreshView() {
        Platform.runLater(() -> {
            if (projectTreeFX != null) {
                projectTreeFX.refresh();
            }
            if (canvasFX != null) {
                canvasFX.redraw();
            }
        });
    }
    
    public void showPreferencesDialog() {
        Platform.runLater(() -> {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Preferences");
            dialog.setHeaderText("FDD Tools Preferences");
            
            // Create tabbed content
            TabPane tabPane = new TabPane();
            tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            
            // General tab
            Tab generalTab = new Tab("General");
            VBox generalContent = new VBox(10);
            generalContent.setPadding(new Insets(10));
            
            CheckBox highContrastCheck = new CheckBox("High contrast tree display");
            highContrastCheck.setSelected(true); // Default from our current setup
            
            CheckBox showTooltipsCheck = new CheckBox("Show tooltips");
            showTooltipsCheck.setSelected(true);
            
            generalContent.getChildren().addAll(
                new Label("Display Options:"),
                highContrastCheck,
                showTooltipsCheck
            );
            generalTab.setContent(generalContent);
            
            // Language tab
            Tab languageTab = new Tab("Language");
            VBox languageContent = new VBox(10);
            languageContent.setPadding(new Insets(10));
            
            ComboBox<String> languageCombo = new ComboBox<>();
            languageCombo.getItems().addAll("English", "Spanish", "Japanese", "Chinese");
            languageCombo.setValue("English");
            
            languageContent.getChildren().addAll(
                new Label("Language:"),
                languageCombo,
                new Label("(Requires restart)")
            );
            languageTab.setContent(languageContent);
            
            tabPane.getTabs().addAll(generalTab, languageTab);
            
            dialog.getDialogPane().setContent(tabPane);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            dialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // TODO: Save preferences in next phase
                    LOGGER.info("Preferences saved (placeholder)");
                }
            });
        });
    }
    
    private void showAboutDialog() {
        DialogBridgeFX.showAboutDialog(primaryStage);
    }
    
    private void exitApplication() {
        if (canClose()) {
            Platform.exit();
        }
    }
    
    private void showErrorDialog(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    // Public methods for application lifecycle
    public boolean canClose() {
        if (modelDirty) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Unsaved Changes");
            alert.setHeaderText("Save changes before closing?");
            alert.setContentText("You have unsaved changes. Do you want to save them?");
            
            ButtonType saveButton = new ButtonType("Save");
            ButtonType dontSaveButton = new ButtonType("Don't Save");
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            
            alert.getButtonTypes().setAll(saveButton, dontSaveButton, cancelButton);
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == saveButton) {
                    saveProject();
                    return true;
                } else if (result.get() == dontSaveButton) {
                    return true;
                } else {
                    return false; // Cancel - don't close
                }
            }
            return false;
        }
        return true;
    }
    
    public void cleanup() {
        if (options != null) {
            options.removeFDDOptionListener(this);
        }
        LOGGER.info("FDDMainWindowFX cleanup completed");
    }
    
    // FDDOptionListener implementation
    @Override
    public void optionChanged(FDDOptionEvent e) {
        // Handle option changes - for now just update canvas font
        if (canvasFX != null) {
            // Convert AWT Font to JavaFX Font
            Font awtFont = options.getTextFont();
            javafx.scene.text.Font fxFont = javafx.scene.text.Font.font(
                awtFont.getName(), 
                awtFont.getSize()
            );
            canvasFX.setTextFont(fxFont);
        }
    }
    
    // FDDTreeContextMenuHandler implementation
    @Override
    public void onSelectionChanged(FDDINode selectedNode) {
        // This is the same as our existing onTreeSelectionChanged method
        onTreeSelectionChanged(selectedNode);
    }
    
    @Override
    public void addProgram(FDDINode parentNode) {
        addFDDElementNode(parentNode, "Program");
    }
    
    @Override
    public void addProject(FDDINode parentNode) {
        addFDDElementNode(parentNode, "Project");
    }
    
    @Override
    public void addAspect(FDDINode parentNode) {
        addFDDElementNode(parentNode, "Aspect");
    }
    
    @Override
    public void addSubject(FDDINode parentNode) {
        addFDDElementNode(parentNode, "Subject");
    }
    
    @Override
    public void addActivity(FDDINode parentNode) {
        addFDDElementNode(parentNode, "Activity");
    }
    
    @Override
    public void addFeature(FDDINode parentNode) {
        addFDDElementNode(parentNode, "Feature");
    }
    
    /**
     * Generic method to add FDD element nodes based on parent type and requested type.
     * This mirrors the logic from the Swing FDDFrame.addFDDElementNode() method.
     */
    private void addFDDElementNode(FDDINode parentNode, String requestedType) {
        FDDINode currentNode = parentNode;
        if (currentNode == null) {
            currentNode = getSelectedNode();
        }
        
        final FDDINode selectedNode = currentNode; // Make effectively final for lambda
        
        if (selectedNode == null) {
            LOGGER.severe("ERROR: No node selected");
            return;
        }
        
        ObjectFactory of = new ObjectFactory();
        final FDDINode newNode;
        
        // Create appropriate child node based on parent type
        if (selectedNode instanceof Program) {
            if ("Program".equals(requestedType)) {
                newNode = (FDDINode) of.createProgram();
                newNode.setName("New Program");
            } else {
                newNode = (FDDINode) of.createProject();
                newNode.setName("New Project");
            }
        } else if (selectedNode instanceof Project) {
            newNode = (FDDINode) of.createAspect();
            newNode.setName("New Aspect");
        } else if (selectedNode instanceof Aspect) {
            newNode = (FDDINode) of.createSubject();
            newNode.setName("New Subject");
        } else if (selectedNode instanceof Subject) {
            newNode = (FDDINode) of.createActivity();
            newNode.setName("New Activity");
        } else if (selectedNode instanceof Activity) {
            newNode = (FDDINode) of.createFeature();
            newNode.setName("New Feature");
        } else {
            LOGGER.warning("Cannot add child to node type: " + selectedNode.getClass().getSimpleName());
            return;
        }
        
        // Set parent relationship
        newNode.setParent(selectedNode);
        
        LOGGER.info("Creating new " + newNode.getClass().getSimpleName() + " for parent: " + selectedNode.getName());
        
        // Show element dialog for the new node
        Platform.runLater(() -> {
            DialogBridgeFX.showElementDialog(primaryStage, newNode, accepted -> {
                if (accepted) {
                    // Add the new node to the parent
                    selectedNode.add(newNode);
                    
                    // Refresh the tree to show the new node
                    if (projectTreeFX != null) {
                        projectTreeFX.refresh();
                        // Select the new node
                        projectTreeFX.selectNode(newNode);
                    }
                    
                    // Update canvas
                    if (canvasFX != null) {
                        canvasFX.redraw();
                    }
                    
                    // Mark model as dirty
                    modelDirty = true;
                    updateTitle();
                    updateMenuStates();
                    
                    LOGGER.info("Successfully added new " + newNode.getClass().getSimpleName() + ": " + newNode.getName());
                } else {
                    LOGGER.info("User cancelled adding new " + newNode.getClass().getSimpleName());
                }
            });
        });
    }
    
    @Override
    public void editNode(FDDINode node) {
        // Use existing edit logic
        if (node != null) {
            DialogBridgeFX.showElementDialog(primaryStage, node, accepted -> {
                if (accepted) {
                    modelDirty = true;
                    updateTitle();
                    updateMenuStates();
                }
            });
        }
    }
    
    @Override
    public void deleteNode(FDDINode node) {
        if (node != null) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Node");
                alert.setHeaderText("Delete " + node.getClass().getSimpleName());
                alert.setContentText("Are you sure you want to delete this node?");
                
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        // Actual deletion logic (from Swing version)
                        FDDINode parentNode = (FDDINode) node.getParent();
                        if (parentNode != null) {
                            parentNode.remove(node);
                            
                            // Update tree display
                            if (projectTreeFX != null) {
                                // Select parent and refresh tree
                                Platform.runLater(() -> {
                                    projectTreeFX.selectNode(parentNode);
                                    projectTreeFX.refresh();
                                });
                            }
                            
                            // Update canvas
                            if (canvasFX != null) {
                                canvasFX.setCurrentNode(parentNode);
                                canvasFX.redraw();
                            }
                            
                            modelDirty = true;
                            updateTitle();
                            updateMenuStates();
                            LOGGER.info("Deleted node: " + node.getClass().getSimpleName());
                        }
                    }
                });
            });
        }
    }
}
