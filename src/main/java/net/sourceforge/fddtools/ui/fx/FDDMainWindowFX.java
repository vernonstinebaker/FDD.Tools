/**
 * JavaFX Main Window for FDD Tools.
 * This class replaces the Swing-based FDDFrame with a pure JavaFX implementation.
 */
package net.sourceforge.fddtools.ui.fx;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.stage.Stage;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.persistence.FDDIXMLFileReader;
import net.sourceforge.fddtools.persistence.FDDIXMLFileWriter;
import com.nebulon.xml.fddi.ObjectFactory;
import com.nebulon.xml.fddi.Program;
import com.nebulon.xml.fddi.Project;
import com.nebulon.xml.fddi.Aspect;

import java.util.logging.Logger;
import com.nebulon.xml.fddi.Subject;
import com.nebulon.xml.fddi.Activity;
import com.nebulon.xml.fddi.Feature;

import net.sourceforge.fddtools.util.ObjectCloner;
import java.awt.Font;
import java.io.File;
import java.util.Optional;
import java.util.List;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import net.sourceforge.fddtools.util.RecentFilesService;
import net.sourceforge.fddtools.util.LayoutPreferencesService;
import net.sourceforge.fddtools.state.ModelState;
import net.sourceforge.fddtools.command.*; // Added command pattern imports
import net.sourceforge.fddtools.commands.EditNodeCommand;

public class FDDMainWindowFX extends BorderPane implements FDDTreeContextMenuHandler {
    private static final Logger LOGGER = Logger.getLogger(FDDMainWindowFX.class.getName());
    
    // Core components
    private final Stage primaryStage;
    // Former options model removed; keep default font settings locally
    private static final Font DEFAULT_AWT_FONT = new Font("SansSerif", Font.PLAIN, 12);
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
    private Label statusLabel;
    private Label undoStatusLabel;
    private Label redoStatusLabel;
    
    // Menu items that need to be enabled/disabled
    private MenuItem fileSave;
    private MenuItem fileSaveAs;
    private MenuItem editCut;
    private MenuItem editCopy;
    private MenuItem editPaste;
    private MenuItem editDelete;
    private MenuItem editEdit;
    private Menu recentFilesMenu; // dynamic recent files submenu
    private MenuItem editUndo; // Undo menu item
    private MenuItem editRedo; // Redo menu item

    // Command infrastructure
    private final CommandStack commandStack; // Added command stack

    public FDDMainWindowFX(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.commandStack = new CommandStack(); // initialize command stack
        
    // Options system removed; using default font configuration
        
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
        // Restore saved divider position or default to 0.25
        double mainDivider = LayoutPreferencesService.getInstance()
            .getMainDividerPosition().orElse(0.25);
    mainSplitPane.setDividerPositions(mainDivider);
    // Listener will be attached after items added (dividers created)
        // Create right split pane for canvas and info panels
        rightSplitPane = new SplitPane();
        rightSplitPane.setOrientation(javafx.geometry.Orientation.VERTICAL);
        double rightDivider = LayoutPreferencesService.getInstance()
            .getRightDividerPosition().orElse(0.7);
    rightSplitPane.setDividerPositions(rightDivider);
    // Attach listener later once divider exists
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
        // Ensure split pane has two items so divider exists
        if (mainSplitPane.getItems().size() == 0) {
            mainSplitPane.getItems().addAll(new Label(), rightSplitPane); // placeholder left
        }
        // Attach listeners safely
        if (!mainSplitPane.getDividers().isEmpty()) {
            mainSplitPane.getDividers().get(0).positionProperty().addListener((obs, o, n) ->
                LayoutPreferencesService.getInstance().setMainDividerPosition(n.doubleValue()));
        }
        if (!rightSplitPane.getDividers().isEmpty()) {
            rightSplitPane.getDividers().get(0).positionProperty().addListener((obs, o, n) ->
                LayoutPreferencesService.getInstance().setRightDividerPosition(n.doubleValue()));
        }
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
        
        // Recent Files submenu (populated dynamically)
        recentFilesMenu = new Menu("Open Recent");
        refreshRecentFilesMenu();
        // Clear Recent item
        MenuItem clearRecent = new MenuItem("Clear Recent");
        clearRecent.setOnAction(e -> {
            RecentFilesService.getInstance().clear();
            refreshRecentFilesMenu();
        });
        recentFilesMenu.getItems().add(new SeparatorMenuItem());
        recentFilesMenu.getItems().add(clearRecent);
        
        MenuItem fileExit = new MenuItem("Exit");
        fileExit.setOnAction(e -> exitApplication());
        
        fileMenu.getItems().addAll(
            fileNew, fileOpen, recentFilesMenu, new SeparatorMenuItem(),
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
        
        // Insert undo/redo before assembling menu
        editUndo = new MenuItem("Undo");
        editUndo.setAccelerator(KeyCombination.keyCombination("Shortcut+Z"));
        editUndo.setOnAction(e -> performUndo());
        editUndo.setDisable(true);
        editRedo = new MenuItem("Redo");
        editRedo.setAccelerator(KeyCombination.keyCombination("Shortcut+Shift+Z"));
        editRedo.setOnAction(e -> performRedo());
        editRedo.setDisable(true);
        
        // Replace original addAll call for editMenu
        editMenu.getItems().clear();
        editMenu.getItems().addAll(
            editUndo, editRedo, new SeparatorMenuItem(),
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

        // Bind enable/disable states to ModelState (initial bootstrap)
        ModelState ms = ModelState.getInstance();
        ms.selectedNodeProperty().addListener((o, oldV, newV) -> {
            boolean has = newV != null;
            editCut.setDisable(!has);
            editCopy.setDisable(!has);
            editDelete.setDisable(!has);
            editEdit.setDisable(!has);
        });
        ms.clipboardNotEmptyProperty().addListener((o, ov, nv) -> editPaste.setDisable(!nv));
        ms.dirtyProperty().addListener((o, ov, nv) -> fileSave.setDisable(!nv));
        ms.undoAvailableProperty().addListener((o, ov, nv) -> editUndo.setDisable(!nv));
        ms.redoAvailableProperty().addListener((o, ov, nv) -> editRedo.setDisable(!nv));
    }
    
    private void createToolBar() {
        toolBar = new ToolBar();
        toolBar.getStyleClass().add("fdd-toolbar");
        java.util.function.BiFunction<FontAwesomeIcon,String,Button> makeBtn = (icon, tip) -> {
            FontAwesomeIconView view = new FontAwesomeIconView(icon);
            view.setGlyphSize(18);
            view.getStyleClass().addAll("fdd-toolbar-icon","fdd-icon");
            Button b = new Button();
            b.setGraphic(view);
            b.getStyleClass().addAll("fdd-toolbar-button","fdd-icon-button");
            b.setTooltip(new Tooltip(tip));
            b.setFocusTraversable(false);
            b.setPrefSize(32,32);
            b.setMinSize(32,32);
            b.setMaxSize(32,32);
            return b;
        };
        Button newBtn = makeBtn.apply(FontAwesomeIcon.FILE, "New Program (⌘N)");
        newBtn.setOnAction(e -> newProject());
        Button openBtn = makeBtn.apply(FontAwesomeIcon.FOLDER_OPEN, "Open (⌘O)");
        openBtn.setOnAction(e -> openProject());
        Button saveBtn = makeBtn.apply(FontAwesomeIcon.FLOPPY_ALT, "Save (⌘S)");
        saveBtn.setOnAction(e -> saveProject());
        Button cutBtn = makeBtn.apply(FontAwesomeIcon.SCISSORS, "Cut (⌘X)");
        cutBtn.setOnAction(e -> cutSelectedNode());
        Button copyBtn = makeBtn.apply(FontAwesomeIcon.COPY, "Copy (⌘C)");
        copyBtn.setOnAction(e -> copySelectedNode());
        Button pasteBtn = makeBtn.apply(FontAwesomeIcon.CLIPBOARD, "Paste (⌘V)");
        pasteBtn.setOnAction(e -> pasteNode());
        toolBar.getItems().addAll(newBtn, openBtn, saveBtn, new Separator(), cutBtn, copyBtn, pasteBtn);
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
        
    statusLabel = new Label("Ready");
    statusLabel.setStyle("-fx-font-size: 12px;");
    undoStatusLabel = new Label("");
    undoStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #555;");
    redoStatusLabel = new Label("");
    redoStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #555;");
    VBox undoRedoBox = new VBox(2, undoStatusLabel, redoStatusLabel);
    undoRedoBox.setPadding(new Insets(2,0,0,4));
    statusBar.getChildren().addAll(actionPanel, statusLabel, undoRedoBox);
    updateUndoRedoStatusBar();
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
                projectTreeFX = new FDDTreeViewFX(false, true); // use modern (orange) styling
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
                        ModelState.getInstance().setSelectedNode(selectedNode);
                        onTreeSelectionChanged(selectedNode);
                    }
                });
                
                // Create and setup canvas  
                if (canvasFX != null) {
                    mainSplitPane.getItems().remove(canvasFX);
                }
                // Convert AWT Font to JavaFX Font
                Font awtFont = DEFAULT_AWT_FONT;
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
                // Re-apply stored divider position after repopulating items
                double pos = LayoutPreferencesService.getInstance().getMainDividerPosition().orElse(0.25);
                mainSplitPane.setDividerPositions(pos);
                // Reset state
                currentProject = "New Program";
                currentProjectPath = null;
                modelDirty = false;
                updateTitle();
                updateMenuStates();
                
                // After resetting state and before logging, ensure undo/redo state reflects empty stack
                updateUndoRedoState();
                
                LOGGER.info("New project created");
                
            } catch (Exception e) {
                LOGGER.severe("Failed to create new project: " + e.getMessage());
                showErrorDialog("Failed to create new project", e.getMessage());
            }
        });
    }
    
    private void refreshRecentFilesMenu() {
        if (recentFilesMenu == null) return;
        recentFilesMenu.getItems().clear();
        List<String> recents = RecentFilesService.getInstance().getRecentFiles();
        if (recents.isEmpty()) {
            MenuItem none = new MenuItem("(None)");
            none.setDisable(true);
            recentFilesMenu.getItems().add(none);
        } else {
            for (String path : recents) {
                File f = new File(path);
                String display = f.getName();
                MenuItem item = new MenuItem(display);
                item.setOnAction(e -> openSpecificProject(path));
                recentFilesMenu.getItems().add(item);
            }
        }
        // Append clear option at end
        recentFilesMenu.getItems().add(new SeparatorMenuItem());
        MenuItem clearRecent = new MenuItem("Clear Recent");
        clearRecent.setOnAction(e -> {
            RecentFilesService.getInstance().clear();
            refreshRecentFilesMenu();
        });
        recentFilesMenu.getItems().add(clearRecent);
    }
    
    private void openSpecificProject(String path) {
        File selectedFile = new File(path);
        if (!selectedFile.exists()) {
            showErrorDialog("Open Recent", "File no longer exists: " + path);
            RecentFilesService.getInstance().clear(); // prune all to simplify; could prune single
            refreshRecentFilesMenu();
            return;
        }
        try {
            FDDINode rootNode = (FDDINode) FDDIXMLFileReader.read(selectedFile.getAbsolutePath());
            if (rootNode != null) {
                closeCurrentProject();
                currentProject = selectedFile.getName();
                currentProjectPath = selectedFile.getAbsolutePath();
                projectTreeFX = new FDDTreeViewFX(false, true);
                projectTreeFX.setContextMenuHandler(this);
                projectTreeFX.populateTree(rootNode);
                if (projectTreeFX.getRoot() != null) {
                    projectTreeFX.getSelectionModel().select(projectTreeFX.getRoot());
                }
                projectTreeFX.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                    if (newSel != null) {
                        ModelState.getInstance().setSelectedNode(newSel.getValue());
                        onTreeSelectionChanged(newSel.getValue());
                    }
                });
                Font awtFont = DEFAULT_AWT_FONT;
                javafx.scene.text.Font fxFont = javafx.scene.text.Font.font(awtFont.getName(), awtFont.getSize());
                canvasFX = new FDDCanvasFX(rootNode, fxFont);
                rightSplitPane.getItems().clear();
                rightSplitPane.getItems().add(canvasFX);
                if (infoPanelContainer.isVisible()) {
                    rightSplitPane.getItems().add(infoPanelContainer);
                    rightSplitPane.setDividerPositions(0.7);
                } else {
                    rightSplitPane.setDividerPositions(1.0);
                }
                mainSplitPane.getItems().clear();
                mainSplitPane.getItems().addAll(projectTreeFX, rightSplitPane);
                // Reapply stored divider
                double pos = LayoutPreferencesService.getInstance().getMainDividerPosition().orElse(0.25);
                mainSplitPane.setDividerPositions(pos);
                modelDirty = false;
                updateTitle();
                updateMenuStates();
                RecentFilesService.getInstance().addRecentFile(path);
                refreshRecentFilesMenu();
                LOGGER.info("Opened project from recent: " + path);
            } else {
                showErrorDialog("Open Project Failed", "Failed to parse the selected file.");
            }
        } catch (Exception ex) {
            LOGGER.severe("Failed to load project file: " + ex.getMessage());
            showErrorDialog("Open Project Failed", "Error loading file: " + ex.getMessage());
        }
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
    Platform.runLater(() -> fileSaveAs.setDisable(currentProject == null));
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
                            projectTreeFX = new FDDTreeViewFX(false, true);
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
                                    ModelState.getInstance().setSelectedNode(selectedNode);
                                    onTreeSelectionChanged(selectedNode);
                                }
                            });
                            
                            // Create and setup canvas
                            Font awtFont = DEFAULT_AWT_FONT;
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
                            // Reapply stored divider
                            double pos = LayoutPreferencesService.getInstance().getMainDividerPosition().orElse(0.25);
                            mainSplitPane.setDividerPositions(pos);
                            modelDirty = false;
                            updateTitle();
                            updateMenuStates();
                            RecentFilesService.getInstance().addRecentFile(selectedFile.getAbsolutePath());
                            refreshRecentFilesMenu();
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
                LOGGER.warning("Failed to save project: " + e.getMessage());
                showErrorDialog("Save Project Failed", e.getMessage());
            }
        });
    }
    
    private boolean saveToFile(String fileName) {
        try {
            // Get root node from the tree
            final Object rootNode;
            if (projectTreeFX != null && projectTreeFX.getRoot() != null) {
                rootNode = projectTreeFX.getRoot().getValue();
            } else {
                rootNode = null;
            }
            
            if (rootNode == null) {
                showErrorDialog("Save Error", "No project data to save.");
                return false;
            }
            
            LOGGER.fine(() -> "About to save to file: " + fileName);
            LOGGER.fine(() -> "Root node type: " + (rootNode != null ? rootNode.getClass().getName() : "null"));
            
            // Debug: Print tree structure before saving
            if (rootNode instanceof Program) {
                Program program = (Program) rootNode;
                LOGGER.finer(() -> "Program name: " + program.getName());
                LOGGER.finer(() -> "Number of projects: " + (program.getProject() != null ? program.getProject().size() : 0));
                if (program.getProject() != null && !program.getProject().isEmpty()) {
                    for (Project project : program.getProject()) {
                        LOGGER.finest(() -> "  Project: " + project.getName());
                        if (project.getAspect() != null) {
                            for (Aspect aspect : project.getAspect()) {
                                LOGGER.finest(() -> "    Aspect: " + aspect.getName());
                                if (aspect.getSubject() != null) {
                                    for (Subject subject : aspect.getSubject()) {
                                        LOGGER.finest(() -> "      Subject: " + subject.getName());
                                        if (subject.getActivity() != null) {
                                            for (Activity activity : subject.getActivity()) {
                                                LOGGER.finest(() -> "        Activity: " + activity.getName());
                                                if (activity.getFeature() != null) {
                                                    LOGGER.finest(() -> "          Features count: " + activity.getFeature().size());
                                                    for (Feature feature : activity.getFeature()) {
                                                        LOGGER.finest(() -> "          Feature: " + feature.getName());
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
            LOGGER.finer(() -> "File exists before save: " + file.exists());
            LOGGER.finer(() -> "File size before save: " + beforeSize);
            LOGGER.finer(() -> "Last modified before save: " + beforeTime);
            
            // FDDINode implements the JAXB objects directly, so rootNode should be the Program object
            // Use the existing FDDIXMLFileWriter to save
            boolean success = FDDIXMLFileWriter.write(rootNode, fileName);
            
            // Check file after save
            long afterTime = file.exists() ? file.lastModified() : 0;
            long afterSize = file.exists() ? file.length() : 0;
            LOGGER.finer(() -> "File exists after save: " + file.exists());
            LOGGER.finer(() -> "File size after save: " + afterSize);
            LOGGER.finer(() -> "Last modified after save: " + afterTime);
            LOGGER.finer(() -> "File timestamp changed: " + (afterTime > beforeTime));
            LOGGER.finer(() -> "FDDIXMLFileWriter.write returned: " + success);
            
            if (success) {
                modelDirty = false;
                LOGGER.info(() -> "Saved project to: " + fileName);
                // No success alert (avoid redundancy). Title bar & dirty flag update are sufficient feedback.
                RecentFilesService.getInstance().addRecentFile(fileName);
                refreshRecentFilesMenu();
                return true;
            } else {
                showErrorDialog("Save Error", "Failed to save the project file.");
                return false;
            }
            
        } catch (Exception e) {
            LOGGER.severe("Error saving project: " + e.getMessage());
            LOGGER.log(java.util.logging.Level.FINEST, "Stacktrace", e);
            showErrorDialog("Save Error", "An error occurred while saving: " + e.getMessage());
            return false;
        }
    }
    
    private void cutSelectedNode() {
        FDDINode selected = getSelectedNode();
        if (selected == null) return;

        // Disallow cutting root
        if (projectTreeFX != null && projectTreeFX.getRoot() != null && projectTreeFX.getRoot().getValue() == selected) {
            showErrorDialog("Cut Not Allowed", "Cannot cut the root element.");
            return;
        }

        clipboard = (FDDINode) ObjectCloner.deepClone(selected);
        uniqueNodeVersion = false;
        if (clipboard == null) {
            LOGGER.severe("Failed to deep clone for cut: " + selected.getClass().getSimpleName());
            showErrorDialog("Cut Error", "Unable to copy node to clipboard.");
            return;
        }
        ModelState.getInstance().setClipboardNotEmpty(true);
        FDDINode parent = (FDDINode) selected.getParentNode();
        if (parent != null) {
            commandStack.execute(new DeleteNodeCommand(selected));
            afterModelMutation(parent);
        }
        updateMenuStates();
        LOGGER.info("Cut (removed) node via command: " + selected.getClass().getSimpleName());
    }
    
    private void copySelectedNode() {
        FDDINode selected = getSelectedNode();
        if (selected != null) {
            clipboard = (FDDINode) ObjectCloner.deepClone(selected);
            uniqueNodeVersion = false;
            if (clipboard != null) {
                ModelState.getInstance().setClipboardNotEmpty(true);
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
                    PasteNodeCommand cmd = new PasteNodeCommand(selected, clipboard, !uniqueNodeVersion);
                    commandStack.execute(cmd);
                    uniqueNodeVersion = false;
                    if (projectTreeFX != null) {
                        projectTreeFX.refresh();
                        if (cmd.getPasted() != null) projectTreeFX.selectNode(cmd.getPasted());
                    }
                    if (canvasFX != null) canvasFX.redraw();
                    markDirty();
                    LOGGER.info("Pasted node via command: " + clipboard.getClass().getSimpleName());
                } catch (Exception e) {
                    LOGGER.severe("Failed to paste node: " + e.getMessage());
                    showErrorDialog("Paste Error", "An error occurred while pasting: " + e.getMessage());
                }
            }
        }
    }
    
    private void deleteSelectedNode() {
        FDDINode selected = getSelectedNode();
        if (selected == null) return;
        if (projectTreeFX != null && projectTreeFX.getRoot() != null && projectTreeFX.getRoot().getValue() == selected) {
            showErrorDialog("Delete Not Allowed", "Cannot delete the root element.");
            return;
        }
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Node");
            alert.setHeaderText("Delete " + selected.getClass().getSimpleName());
            alert.setContentText("Are you sure you want to delete this node?");
            configureDialogCentering(alert);
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    FDDINode parent = (FDDINode) selected.getParentNode();
                    if (parent != null) {
                        commandStack.execute(new DeleteNodeCommand(selected));
                        afterModelMutation(parent);
                        LOGGER.info("Deleted node via command: " + selected.getClass().getSimpleName());
                    }
                }
            });
        });
    }
    
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
    newNode.setParentNode(selectedNode);
        
        LOGGER.info("Creating new " + newNode.getClass().getSimpleName() + " for parent: " + selectedNode.getName());
        
        // Show element dialog for the new node
        Platform.runLater(() -> {
            FDDElementDialogFX dlg = new FDDElementDialogFX(primaryStage, newNode);
            configureDialogCentering(dlg);
            dlg.showAndWait();
            if (dlg.getAccept()) {
                // Add the new node to the parent
                commandStack.execute(new AddChildCommand(selectedNode, newNode));
                afterModelMutation(newNode);
                LOGGER.info("Added new node via command: " + newNode.getClass().getSimpleName());
            } else {
                LOGGER.info("User cancelled adding new " + newNode.getClass().getSimpleName());
            }
        });
    }
    
    private void performUndo() {
        commandStack.undo();
        refreshView();
        markDirty();
    updateUndoRedoState();
    updateUndoRedoStatusBar();
    }
    private void performRedo() {
        commandStack.redo();
        refreshView();
        markDirty();
    updateUndoRedoState();
    updateUndoRedoStatusBar();
    }
    private void updateUndoRedoState() {
        ModelState ms = ModelState.getInstance();
        ms.setUndoAvailable(commandStack.canUndo());
        ms.setRedoAvailable(commandStack.canRedo());
        if (editUndo != null) {
            editUndo.setText("Undo");
        }
        if (editRedo != null) {
            editRedo.setText("Redo");
        }
    }

    private void updateUndoRedoStatusBar() {
        if (undoStatusLabel != null) {
            undoStatusLabel.setText(commandStack.canUndo() ? "Next Undo: " + commandStack.peekUndoDescription() : "No Undo");
        }
        if (redoStatusLabel != null) {
            redoStatusLabel.setText(commandStack.canRedo() ? "Next Redo: " + commandStack.peekRedoDescription() : "No Redo");
        }
    }
    private void markDirty() {
        modelDirty = true;
        ModelState ms = ModelState.getInstance();
        ms.setDirty(true);
        updateTitle();
        updateUndoRedoState();
        updateMenuStates();
    }
    private void afterModelMutation(FDDINode nodeToSelect) {
        if (projectTreeFX != null) {
            projectTreeFX.refresh();
            projectTreeFX.selectNode(nodeToSelect);
        }
        if (canvasFX != null) {
            canvasFX.setCurrentNode(nodeToSelect);
            canvasFX.redraw();
        }
        markDirty();
    }
    // ===== Restored methods & interface implementations (previously truncated) =====

    // FDDTreeContextMenuHandler implementation
    @Override
    public void onSelectionChanged(FDDINode selectedNode) { onTreeSelectionChanged(selectedNode); }
    @Override
    public void addProgram(FDDINode parentNode) { addFDDElementNode(parentNode, "Program"); }
    @Override
    public void addProject(FDDINode parentNode) { addFDDElementNode(parentNode, "Project"); }
    @Override
    public void addAspect(FDDINode parentNode) { addFDDElementNode(parentNode, "Aspect"); }
    @Override
    public void addSubject(FDDINode parentNode) { addFDDElementNode(parentNode, "Subject"); }
    @Override
    public void addActivity(FDDINode parentNode) { addFDDElementNode(parentNode, "Activity"); }
    @Override
    public void addFeature(FDDINode parentNode) { addFDDElementNode(parentNode, "Feature"); }
    @Override
    public void editNode(FDDINode node) { editSelectedNode(node); }
    @Override
    public void deleteNode(FDDINode node) { deleteSelectedNode(); }

    private void editSelectedNode() { editSelectedNode(getSelectedNode()); }
    private void editSelectedNode(FDDINode node) {
        if (node != null) {
            Platform.runLater(() -> {
                var beforeSnapshot = EditNodeCommand.capture(node);
                FDDElementDialogFX dlg = new FDDElementDialogFX(primaryStage, node);
                configureDialogCentering(dlg);
                dlg.showAndWait();
                if (dlg.getAccept()) {
                    var afterSnapshot = EditNodeCommand.capture(node);
                    boolean changed = !beforeSnapshot.getName().equals(afterSnapshot.getName()) ||
                        (beforeSnapshot.getPrefix() == null ? afterSnapshot.getPrefix() != null : !beforeSnapshot.getPrefix().equals(afterSnapshot.getPrefix())) ||
                        (beforeSnapshot.getOwnerInitials() == null ? afterSnapshot.getOwnerInitials() != null : !beforeSnapshot.getOwnerInitials().equals(afterSnapshot.getOwnerInitials())) ||
                        // Milestone changes
                        (beforeSnapshot.getMilestoneStatuses() != null && afterSnapshot.getMilestoneStatuses() != null &&
                            java.util.Arrays.equals(beforeSnapshot.getMilestoneStatuses(), afterSnapshot.getMilestoneStatuses()) == false) ||
                        // Work package assignment change
                        ((beforeSnapshot.getWorkPackageName() == null ? "" : beforeSnapshot.getWorkPackageName())
                            .equals(afterSnapshot.getWorkPackageName() == null ? "" : afterSnapshot.getWorkPackageName()) == false);
                    if (changed) {
                        // Revert to before state
                        node.setName(beforeSnapshot.getName());
                        if (node instanceof com.nebulon.xml.fddi.Subject subj) subj.setPrefix(beforeSnapshot.getPrefix());
                        if (node instanceof com.nebulon.xml.fddi.Activity act) act.setInitials(beforeSnapshot.getOwnerInitials());
                        else if (node instanceof com.nebulon.xml.fddi.Feature feat) {
                            feat.setInitials(beforeSnapshot.getOwnerInitials());
                            // Revert milestones
                            if (beforeSnapshot.getMilestoneStatuses() != null) {
                                var milestones = feat.getMilestone();
                                for (int i = 0; i < Math.min(milestones.size(), beforeSnapshot.getMilestoneStatuses().length); i++) {
                                    milestones.get(i).setStatus(beforeSnapshot.getMilestoneStatuses()[i]);
                                }
                            }
                            // Revert work package membership if changed
                            if (afterSnapshot.getWorkPackageName() != null || beforeSnapshot.getWorkPackageName() != null) {
                                var proj = getOwningProject(feat);
                                if (proj != null) {
                                    // Remove from all
                                    for (var wp : proj.getWorkPackages()) {
                                        wp.getFeatureList().remove(Integer.valueOf(feat.getSeq()));
                                    }
                                    // Re-add original if not empty
                                    if (beforeSnapshot.getWorkPackageName() != null && !beforeSnapshot.getWorkPackageName().isEmpty()) {
                                        for (var wp : proj.getWorkPackages()) {
                                            if (beforeSnapshot.getWorkPackageName().equals(wp.getName())) {
                                                if (!wp.getFeatureList().contains(feat.getSeq())) wp.addFeature(feat.getSeq());
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        commandStack.execute(new EditNodeCommand(node, beforeSnapshot, afterSnapshot));
                        updateUndoRedoState();
                    }
                    markDirty();
                    if (canvasFX != null) canvasFX.redraw();
                }
            });
        }
    }

    private FDDINode getSelectedNode() { return projectTreeFX != null ? projectTreeFX.getSelectedNode() : null; }

    private com.nebulon.xml.fddi.Project getOwningProject(com.nebulon.xml.fddi.Feature feat) {
        net.sourceforge.fddtools.model.FDDTreeNode current = feat.getParentNode();
        while (current != null) {
            if (current instanceof com.nebulon.xml.fddi.Project p) return p;
            current = current.getParentNode();
        }
        return null;
    }

    private void onTreeSelectionChanged(FDDINode selectedNode) {
        if (canvasFX != null && selectedNode != null) {
            canvasFX.setCurrentNode(selectedNode);
        }
        updateInfoPanels(selectedNode);
        updateMenuStates();
    }

    private void updateInfoPanels(FDDINode selectedNode) {
        if (infoPanelContainer.isVisible()) {
            infoPanelContainer.setVisible(false);
            rightSplitPane.getItems().remove(infoPanelContainer);
            rightSplitPane.setDividerPositions(1.0);
        }
    }

    private void refreshView() {
        Platform.runLater(() -> {
            if (projectTreeFX != null) projectTreeFX.refresh();
            if (canvasFX != null) canvasFX.redraw();
        });
    }

    public void showPreferencesDialog() {
        Platform.runLater(() -> {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Preferences");
            dialog.setHeaderText("FDD Tools Preferences");
            TabPane tabPane = new TabPane();
            tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            Tab generalTab = new Tab("General");
            VBox generalContent = new VBox(10);
            generalContent.setPadding(new Insets(10));
            CheckBox highContrastCheck = new CheckBox("High contrast tree display");
            highContrastCheck.setSelected(true);
            CheckBox showTooltipsCheck = new CheckBox("Show tooltips");
            showTooltipsCheck.setSelected(true);
            generalContent.getChildren().addAll(new Label("Display Options:"), highContrastCheck, showTooltipsCheck);
            generalTab.setContent(generalContent);
            Tab languageTab = new Tab("Language");
            VBox languageContent = new VBox(10);
            languageContent.setPadding(new Insets(10));
            ComboBox<String> languageCombo = new ComboBox<>();
            languageCombo.getItems().addAll("English", "Spanish", "Japanese", "Chinese");
            languageCombo.setValue("English");
            languageContent.getChildren().addAll(new Label("Language:"), languageCombo, new Label("(Requires restart)"));
            languageTab.setContent(languageContent);
            tabPane.getTabs().addAll(generalTab, languageTab);
            dialog.getDialogPane().setContent(tabPane);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            configureDialogCentering(dialog);
            dialog.showAndWait();
        });
    }

    private void showAboutDialog() {
        Platform.runLater(() -> {
            AboutDialogFX dlg = new AboutDialogFX(primaryStage);
            configureDialogCentering(dlg);
            dlg.showAndWait();
        });
    }

    private void exitApplication() { if (canClose()) Platform.exit(); }

    private void showErrorDialog(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            configureDialogCentering(alert);
            alert.showAndWait();
        });
    }

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
            configureDialogCentering(alert);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == saveButton) { saveProject(); return true; }
                else if (result.get() == dontSaveButton) { return true; } else { return false; }
            }
            return false;
        }
        return true;
    }

    public void cleanup() { LOGGER.info("FDDMainWindowFX cleanup completed"); }

    private void configureDialogCentering(Dialog<?> dialog) {
        if (primaryStage != null) dialog.initOwner(primaryStage);
        dialog.setOnShown(e -> {
            Window win = dialog.getDialogPane().getScene().getWindow();
            if (win == null) return;
            centerWindowOverCanvas(win);
        });
    }

    private void configureDialogCentering(Stage stage) {
        if (primaryStage != null && stage.getOwner() == null) stage.initOwner(primaryStage);
        stage.setOnShown(e -> centerWindowOverCanvas(stage));
    }

    private void centerWindowOverCanvas(Window win) {
        Node anchor = (canvasFX != null) ? canvasFX : this;
        Bounds b = anchor.localToScreen(anchor.getBoundsInLocal());
        if (b != null) {
            double x = b.getMinX() + (b.getWidth() - win.getWidth()) / 2.0;
            double y = b.getMinY() + (b.getHeight() - win.getHeight()) / 3.0;
            win.setX(x);
            win.setY(y);
            Platform.runLater(() -> {
                Bounds b2 = anchor.localToScreen(anchor.getBoundsInLocal());
                if (b2 != null) {
                    double x2 = b2.getMinX() + (b2.getWidth() - win.getWidth()) / 2.0;
                    double y2 = b2.getMinY() + (b2.getHeight() - win.getHeight()) / 3.0;
                    win.setX(x2);
                    win.setY(y2);
                }
            });
        } else if (primaryStage != null) {
            win.setX(primaryStage.getX() + (primaryStage.getWidth() - win.getWidth()) / 2.0);
            win.setY(primaryStage.getY() + (primaryStage.getHeight() - win.getHeight()) / 2.0);
        }
    }
}
