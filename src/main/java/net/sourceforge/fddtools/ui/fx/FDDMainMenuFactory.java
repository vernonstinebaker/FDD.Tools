package net.sourceforge.fddtools.ui.fx;

import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.input.KeyCombination;
import net.sourceforge.fddtools.util.RecentFilesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.util.List;

/**
 * Factory for constructing the main application MenuBar and returning references
 * to key menu items for enablement binding in the main window. This isolates a
 * large, previously inlined block from FDDMainWindowFX improving readability.
 */
public final class FDDMainMenuFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(FDDMainMenuFactory.class);
    private FDDMainMenuFactory() {}

    /** Actions contract implemented by the main window. */
    public interface Actions {
        void onNew();
        void onOpen();
        void onSave();
        void onSaveAs();
        void onExit();
        void onUndo();
        void onRedo();
        void onCut();
        void onCopy();
        void onPaste();
        void onDelete();
        void onEdit();
        void onPreferences();
        void onRefresh();
        void onAbout();
    }

    /** Record (Java 21) holding built menu components for assignment. */
    public record MenuComponents(MenuBar menuBar,
                                 Menu recentFilesMenu,
                                 MenuItem fileSave,
                                 MenuItem fileSaveAs,
                                 MenuItem editCut,
                                 MenuItem editCopy,
                                 MenuItem editPaste,
                                 MenuItem editDelete,
                                 MenuItem editEdit,
                                 MenuItem editUndo,
                                 MenuItem editRedo) {}

    public static MenuComponents build(Actions actions, Stage stage) {
        MenuBar menuBar = new MenuBar();
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            menuBar.setUseSystemMenuBar(true);
            LOGGER.info("Configured MenuBar to use system menu bar (macOS)");
        }

        // File Menu
        Menu fileMenu = new Menu("File");
        MenuItem fileNew = new MenuItem("New");
        fileNew.setAccelerator(KeyCombination.keyCombination("Shortcut+N"));
        fileNew.setOnAction(e -> actions.onNew());

        MenuItem fileOpen = new MenuItem("Open...");
        fileOpen.setAccelerator(KeyCombination.keyCombination("Shortcut+O"));
        fileOpen.setOnAction(e -> actions.onOpen());

        MenuItem fileSave = new MenuItem("Save");
        fileSave.setAccelerator(KeyCombination.keyCombination("Shortcut+S"));
        fileSave.setOnAction(e -> actions.onSave());

        MenuItem fileSaveAs = new MenuItem("Save As...");
        fileSaveAs.setAccelerator(KeyCombination.keyCombination("Shortcut+Shift+S"));
        fileSaveAs.setOnAction(e -> actions.onSaveAs());

        Menu recentFilesMenu = new Menu("Open Recent");
        populateRecentFilesMenu(recentFilesMenu, actions);

        MenuItem fileExit = new MenuItem("Exit");
        fileExit.setOnAction(e -> actions.onExit());
        fileMenu.getItems().addAll(fileNew, fileOpen, recentFilesMenu, new SeparatorMenuItem(), fileSave, fileSaveAs, new SeparatorMenuItem(), fileExit);

        // Edit Menu
        Menu editMenu = new Menu("Edit");
        MenuItem editUndo = new MenuItem("Undo");
        editUndo.setAccelerator(KeyCombination.keyCombination("Shortcut+Z"));
        editUndo.setOnAction(e -> actions.onUndo());
        MenuItem editRedo = new MenuItem("Redo");
        editRedo.setAccelerator(KeyCombination.keyCombination("Shortcut+Shift+Z"));
        editRedo.setOnAction(e -> actions.onRedo());

        MenuItem editCut = new MenuItem("Cut");
        editCut.setAccelerator(KeyCombination.keyCombination("Shortcut+X"));
        editCut.setOnAction(e -> actions.onCut());
        MenuItem editCopy = new MenuItem("Copy");
        editCopy.setAccelerator(KeyCombination.keyCombination("Shortcut+C"));
        editCopy.setOnAction(e -> actions.onCopy());
        MenuItem editPaste = new MenuItem("Paste");
        editPaste.setAccelerator(KeyCombination.keyCombination("Shortcut+V"));
        editPaste.setOnAction(e -> actions.onPaste());
        MenuItem editDelete = new MenuItem("Delete");
        editDelete.setAccelerator(KeyCombination.keyCombination("Delete"));
        editDelete.setOnAction(e -> actions.onDelete());
        MenuItem editEdit = new MenuItem("Edit...");
        editEdit.setAccelerator(KeyCombination.keyCombination("Shortcut+E"));
        editEdit.setOnAction(e -> actions.onEdit());
        MenuItem editPreferences = new MenuItem("Preferences...");
        editPreferences.setOnAction(e -> actions.onPreferences());
        editMenu.getItems().addAll(editUndo, editRedo, new SeparatorMenuItem(), editCut, editCopy, editPaste, new SeparatorMenuItem(), editDelete, editEdit, new SeparatorMenuItem(), editPreferences);

        // View Menu
        Menu viewMenu = new Menu("View");
        MenuItem viewRefresh = new MenuItem("Refresh");
        viewRefresh.setAccelerator(KeyCombination.keyCombination("F5"));
        viewRefresh.setOnAction(e -> actions.onRefresh());
        viewMenu.getItems().add(viewRefresh);

        // Help Menu
        Menu helpMenu = new Menu("Help");
        MenuItem helpAbout = new MenuItem("About FDD Tools");
        helpAbout.setOnAction(e -> actions.onAbout());
        helpMenu.getItems().add(helpAbout);

        menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu, helpMenu);

        return new MenuComponents(menuBar, recentFilesMenu, fileSave, fileSaveAs, editCut, editCopy, editPaste, editDelete, editEdit, editUndo, editRedo);
    }

    /** Repopulates the recent files submenu. */
    public static void populateRecentFilesMenu(Menu recentFilesMenu, Actions actions) {
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
                item.setOnAction(e -> {
                    // delegate back through open action after updating MRU (main window handles logic)
                    actions.onOpen(); // open dialog path; improvement: add direct path open method later
                });
                recentFilesMenu.getItems().add(item);
            }
        }
        recentFilesMenu.getItems().add(new SeparatorMenuItem());
        MenuItem clearRecent = new MenuItem("Clear Recent");
        clearRecent.setOnAction(e -> {
            RecentFilesService.getInstance().clear();
            populateRecentFilesMenu(recentFilesMenu, actions);
        });
        recentFilesMenu.getItems().add(clearRecent);
    }
}
