package net.sourceforge.fddtools.ui.fx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.sourceforge.fddtools.internationalization.Messages;

/**
 * JavaFX version of the About Dialog.
 * This is a reimplementation of AboutDialog using JavaFX components.
 */
public class AboutDialogFX extends Stage {
    private static final Logger LOGGER = LoggerFactory.getLogger(AboutDialogFX.class);
    
    public AboutDialogFX(Stage owner) {
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.DECORATED);
        setTitle(Messages.getInstance().getMessage(Messages.ABOUT_TITLE));
        setResizable(false);
        
    BorderPane root = new BorderPane();
    root.getStyleClass().add("dialog-root");
        // Create tabbed pane
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // About tab
    Tab aboutTab = new Tab(Messages.getInstance().getMessage(Messages.ABOUT_TAB_ABOUT_CAPTION));
        TextArea aboutText = new TextArea();
        aboutText.setText(
            "FDD Tools Version 2.0.0\n\n" + 
            "FDD Tools supports Project Tracking using the\n" +
            "Feature Driven Development methodology.\n\n" +
            "Released under the Apache Software License 1.1\n\n" + 
            "Contributors:\n" +
            "\tVernon Stinebaker\n" + 
            "\tKenneth Jiang\n" + 
            "\tJames Hwong\n" + 
            "\tAndres Acosta\n" + 
            "\tOuyang Jiezi\n"
        );
        aboutText.setEditable(false);
        aboutText.setWrapText(true);
        aboutText.setFont(Font.font("System", 12));
    aboutText.getStyleClass().addAll("panel","panel-bordered");
        
        VBox aboutContent = new VBox(aboutText);
    aboutContent.setPadding(new Insets(10));
    aboutContent.getStyleClass().add("panel");
    aboutTab.setContent(aboutContent);
        
        // Copyright tab
    Tab copyrightTab = new Tab(Messages.getInstance().getMessage(Messages.ABOUT_TAB_COPYRIGHT_CAPTION));
        TextArea copyrightText = new TextArea();
        copyrightText.setText(
            "FDD Tools\n\tCopyright (c) 2004 - 2025 Vernon Stinebaker, Kenneth Jiang,\n" +
            "\tJames Hwang, Andres Acosta, Ouyang Jiezi\n\tAll rights reserved.\n\n" +
            "ExtensionFileFilter\n\tCopyright (c) 2001 Marty Hall and Larry Brown\n" +
            "\t(http://www.corewebprogramming.com)\n\tAll rights reserved.\n\n" +
            "OpenCSV\n\tCopyright (c) 2005 - 2009 Apache 2.0\n" +
            "\t(http://opencsv.sourceforge.net/)\n\tAll Rights Reserved.\n\n" +
            "FontAwesome\n\tCopyright (c) FontAwesome Free License\n" +
            "\t(https://fontawesome.com/)\n\tAll rights reserved.\n\n" +
            "Icons\n\tFontAwesome icons used throughout application\n\tThank you to the FontAwesome project\n"
        );
        copyrightText.setEditable(false);
        copyrightText.setWrapText(true);
        copyrightText.setFont(Font.font("System", 12));
    copyrightText.getStyleClass().addAll("panel","panel-bordered");
        
        VBox copyrightContent = new VBox(copyrightText);
    copyrightContent.setPadding(new Insets(10));
    copyrightContent.getStyleClass().add("panel");
    copyrightTab.setContent(copyrightContent);
        
        tabPane.getTabs().addAll(aboutTab, copyrightTab);
        
        // OK button
        Button okButton = new Button(Messages.getInstance().getMessage(Messages.JBUTTON_OK_CAPTION));
        okButton.setDefaultButton(true);        okButton.setPrefWidth(80);        okButton.setOnAction(e -> close());
        
        HBox buttonBox = new HBox(okButton);
        buttonBox.setAlignment(Pos.CENTER);
    buttonBox.setPadding(new Insets(10));
    buttonBox.getStyleClass().add("dialog-button-bar");
        root.setCenter(tabPane);
        root.setBottom(buttonBox);
        
        Scene scene = new Scene(root, 550, 450);
        setScene(scene);
        
        // The dialog will be centered after it's shown
    }
    
    /**
     * Shows the dialog and waits for it to be closed.
     */
    public void showDialog() {
    LOGGER.trace("showDialog() called");
        centerOnScreen();
    LOGGER.trace("About to show dialog...");
        showAndWait();
    LOGGER.trace("Dialog closed");
    }
}