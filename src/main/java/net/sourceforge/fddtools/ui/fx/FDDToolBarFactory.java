package net.sourceforge.fddtools.ui.fx;

import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

/** Factory for main toolbar to keep FDDMainWindowFX lean. */
public class FDDToolBarFactory {
    public interface Actions {
        void onNew();
        void onOpen();
        void onSave();
        void onCut();
        void onCopy();
        void onPaste();
        void onGoToRoot();
        void onNavigateBack();
        void onNavigateForward();
    }
    public static ToolBar build(Actions a) {
        ToolBar tb = new ToolBar();
        tb.getStyleClass().add("fdd-toolbar");
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
        
        // Navigation buttons (first group)
        Button homeBtn = makeBtn.apply(FontAwesomeIcon.HOME, "Go to Root (⌘Home)");
        homeBtn.setOnAction(e -> a.onGoToRoot());
        Button backBtn = makeBtn.apply(FontAwesomeIcon.ARROW_LEFT, "Navigate Back");
        backBtn.setOnAction(e -> a.onNavigateBack());
        backBtn.setDisable(true); // Initially disabled
        Button forwardBtn = makeBtn.apply(FontAwesomeIcon.ARROW_RIGHT, "Navigate Forward");
        forwardBtn.setOnAction(e -> a.onNavigateForward());
        forwardBtn.setDisable(true); // Initially disabled
        
        // File operations (second group)
        Button newBtn = makeBtn.apply(FontAwesomeIcon.FILE, "New Program (⌘N)");
        newBtn.setOnAction(e -> a.onNew());
        Button openBtn = makeBtn.apply(FontAwesomeIcon.FOLDER_OPEN, "Open (⌘O)");
        openBtn.setOnAction(e -> a.onOpen());
        Button saveBtn = makeBtn.apply(FontAwesomeIcon.FLOPPY_ALT, "Save (⌘S)");
        saveBtn.setOnAction(e -> a.onSave());
        
        // Edit operations (third group)
        Button cutBtn = makeBtn.apply(FontAwesomeIcon.SCISSORS, "Cut (⌘X)");
        cutBtn.setOnAction(e -> a.onCut());
        Button copyBtn = makeBtn.apply(FontAwesomeIcon.COPY, "Copy (⌘C)");
        copyBtn.setOnAction(e -> a.onCopy());
        Button pasteBtn = makeBtn.apply(FontAwesomeIcon.CLIPBOARD, "Paste (⌘V)");
        pasteBtn.setOnAction(e -> a.onPaste());
        
        // Add all buttons with separators between groups
        tb.getItems().addAll(
            homeBtn, backBtn, forwardBtn, 
            new Separator(),
            newBtn, openBtn, saveBtn, 
            new Separator(), 
            cutBtn, copyBtn, pasteBtn
        );
        
        // Store references to navigation buttons for later access
        tb.getProperties().put("backButton", backBtn);
        tb.getProperties().put("forwardButton", forwardBtn);
        
        return tb;
    }
}
