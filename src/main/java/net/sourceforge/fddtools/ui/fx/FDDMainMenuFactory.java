package net.sourceforge.fddtools.ui.fx;

import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.input.KeyCombination;
import net.sourceforge.fddtools.util.RecentFilesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.util.List;
import net.sourceforge.fddtools.util.I18n;
import net.sourceforge.fddtools.util.I18nRegistry;

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
            // Bind system app menu handlers to Actions (About, Preferences, Quit)
            MacOSIntegrationService.installMacAppMenuHandlers(
                actions::onAbout,
                actions::onPreferences,
                actions::onExit
            );
        }
    boolean isMac = System.getProperty("os.name").toLowerCase().contains("mac");
        Menu fileMenu = new Menu(I18n.get("FDDFrame.MenuFile.Caption")); I18nRegistry.registerMenu(fileMenu, "FDDFrame.MenuFile.Caption");
        MenuItem fileNew = new MenuItem(I18n.get("FDDFrame.MenuNew.Caption")); I18nRegistry.register(fileNew, "FDDFrame.MenuNew.Caption");
        fileNew.setAccelerator(KeyCombination.keyCombination("Shortcut+N")); fileNew.setOnAction(e -> actions.onNew());
        MenuItem fileOpen = new MenuItem(I18n.get("FDDFrame.MenuOpen.Caption")); I18nRegistry.register(fileOpen, "FDDFrame.MenuOpen.Caption");
        fileOpen.setAccelerator(KeyCombination.keyCombination("Shortcut+O")); fileOpen.setOnAction(e -> actions.onOpen());
        MenuItem fileSave = new MenuItem(I18n.get("FDDFrame.MenuSave.Caption")); I18nRegistry.register(fileSave, "FDDFrame.MenuSave.Caption");
        fileSave.setAccelerator(KeyCombination.keyCombination("Shortcut+S")); fileSave.setOnAction(e -> actions.onSave());
        MenuItem fileSaveAs = new MenuItem(I18n.get("FDDFrame.MenuSaveAs.Caption")); I18nRegistry.register(fileSaveAs, "FDDFrame.MenuSaveAs.Caption");
        fileSaveAs.setAccelerator(KeyCombination.keyCombination("Shortcut+Shift+S")); fileSaveAs.setOnAction(e -> actions.onSaveAs());
        Menu recentFilesMenu = new Menu(I18n.get("RecentFiles.Menu.Caption")); I18nRegistry.registerMenu(recentFilesMenu, "RecentFiles.Menu.Caption");
        populateRecentFilesMenu(recentFilesMenu, actions);
        if (!isMac) { MenuItem fileExit = new MenuItem(I18n.get("FDDFrame.MenuExit.Caption")); I18nRegistry.register(fileExit, "FDDFrame.MenuExit.Caption"); fileExit.setOnAction(e -> actions.onExit()); fileMenu.getItems().addAll(fileNew, fileOpen, recentFilesMenu, new SeparatorMenuItem(), fileSave, fileSaveAs, new SeparatorMenuItem(), fileExit); }
        else { fileMenu.getItems().addAll(fileNew, fileOpen, recentFilesMenu, new SeparatorMenuItem(), fileSave, fileSaveAs); }
        Menu editMenu = new Menu(I18n.get("FDDFrame.MenuEdit.Caption")); I18nRegistry.registerMenu(editMenu, "FDDFrame.MenuEdit.Caption");
        MenuItem editUndo = new MenuItem(I18n.get("FDDFrame.MenuUndo.Caption")); I18nRegistry.register(editUndo, "FDDFrame.MenuUndo.Caption"); editUndo.setAccelerator(KeyCombination.keyCombination("Shortcut+Z")); editUndo.setOnAction(e -> actions.onUndo());
        MenuItem editRedo = new MenuItem(I18n.get("FDDFrame.MenuRedo.Caption")); I18nRegistry.register(editRedo, "FDDFrame.MenuRedo.Caption"); editRedo.setAccelerator(KeyCombination.keyCombination("Shortcut+Shift+Z")); editRedo.setOnAction(e -> actions.onRedo());
        MenuItem editCut = new MenuItem(I18n.get("FDDFrame.MenuCut.Caption")); I18nRegistry.register(editCut, "FDDFrame.MenuCut.Caption"); editCut.setAccelerator(KeyCombination.keyCombination("Shortcut+X")); editCut.setOnAction(e -> actions.onCut());
        MenuItem editCopy = new MenuItem(I18n.get("FDDFrame.MenuCopy.Caption")); I18nRegistry.register(editCopy, "FDDFrame.MenuCopy.Caption"); editCopy.setAccelerator(KeyCombination.keyCombination("Shortcut+C")); editCopy.setOnAction(e -> actions.onCopy());
        MenuItem editPaste = new MenuItem(I18n.get("FDDFrame.MenuPaste.Caption")); I18nRegistry.register(editPaste, "FDDFrame.MenuPaste.Caption"); editPaste.setAccelerator(KeyCombination.keyCombination("Shortcut+V")); editPaste.setOnAction(e -> actions.onPaste());
        MenuItem editDelete = new MenuItem(I18n.get("FDDFrame.MenuDelete.Caption")); I18nRegistry.register(editDelete, "FDDFrame.MenuDelete.Caption"); editDelete.setAccelerator(KeyCombination.keyCombination("Delete")); editDelete.setOnAction(e -> actions.onDelete());
        MenuItem editEdit = new MenuItem(I18n.get("EditElement.Menu.Caption")); I18nRegistry.register(editEdit, "EditElement.Menu.Caption"); editEdit.setAccelerator(KeyCombination.keyCombination("Shortcut+E")); editEdit.setOnAction(e -> actions.onEdit());
    if (isMac) { editMenu.getItems().addAll(editUndo, editRedo, new SeparatorMenuItem(), editCut, editCopy, editPaste, new SeparatorMenuItem(), editDelete, editEdit); }
    else { MenuItem editPreferences = new MenuItem(I18n.get("Preferences.Menu.Caption")); I18nRegistry.register(editPreferences, "Preferences.Menu.Caption"); editPreferences.setOnAction(e -> actions.onPreferences()); editPreferences.setAccelerator(KeyCombination.keyCombination("Shortcut+,")); editMenu.getItems().addAll(editUndo, editRedo, new SeparatorMenuItem(), editCut, editCopy, editPaste, new SeparatorMenuItem(), editDelete, editEdit, new SeparatorMenuItem(), editPreferences); }
        Menu viewMenu = new Menu(I18n.get("View.Menu.Caption")); I18nRegistry.registerMenu(viewMenu, "View.Menu.Caption");
        MenuItem viewRefresh = new MenuItem(I18n.get("View.Refresh.Caption")); I18nRegistry.register(viewRefresh, "View.Refresh.Caption"); viewRefresh.setAccelerator(KeyCombination.keyCombination("F5")); viewRefresh.setOnAction(e -> actions.onRefresh()); viewMenu.getItems().add(viewRefresh);
        Menu helpMenu = new Menu(I18n.get("FDDFrame.MenuHelp.Caption")); I18nRegistry.registerMenu(helpMenu, "FDDFrame.MenuHelp.Caption");
        if (!isMac) { MenuItem helpAbout = new MenuItem(I18n.get("FDDFrame.MenuAbout.Caption")); I18nRegistry.register(helpAbout, "FDDFrame.MenuAbout.Caption"); helpAbout.setOnAction(e -> actions.onAbout()); helpMenu.getItems().add(helpAbout); }
    // On macOS with system menu bar, do NOT add a custom app menu; the system app menu is provided by macOS.
    menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu, helpMenu);
        return new FDDMainMenuFactory.MenuComponents(menuBar, recentFilesMenu, fileSave, fileSaveAs, editCut, editCopy, editPaste, editDelete, editEdit, editUndo, editRedo);
    }

    /** Repopulates the recent files submenu. */
    public static void populateRecentFilesMenu(Menu recentFilesMenu, Actions actions) {
        recentFilesMenu.getItems().clear();
        List<String> recents = RecentFilesService.getInstance().getRecentFiles();
        if (recents.isEmpty()) { MenuItem none = new MenuItem(I18n.get("RecentFiles.None.Caption")); none.setDisable(true); recentFilesMenu.getItems().add(none); } else { for(String path: recents){ File f=new File(path); String display=f.getName(); MenuItem item=new MenuItem(display); item.setOnAction(e-> actions.onOpen()); recentFilesMenu.getItems().add(item);} }
        recentFilesMenu.getItems().add(new SeparatorMenuItem());
        MenuItem clearRecent = new MenuItem(I18n.get("RecentFiles.Clear.Caption"));
        clearRecent.setOnAction(e -> { RecentFilesService.getInstance().clear(); populateRecentFilesMenu(recentFilesMenu, actions); });
        recentFilesMenu.getItems().add(clearRecent);
    }
}
