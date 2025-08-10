package net.sourceforge.fddtools.ui.fx;

import com.nebulon.xml.fddi.*;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import net.sourceforge.fddtools.internationalization.Messages;
import net.sourceforge.fddtools.model.FDDINode;

/**
 * Extracted builder for the generic info panel previously implemented inline in FDDElementDialogFX.
 * Provides a Result object containing the constructed panel and the text fields so the dialog
 * can wire them for later OK handling while allowing the build logic to be unit tested.
 */
public final class GenericInfoPanelBuilder {
    private GenericInfoPanelBuilder() {}

    public static final class Result {
        public final VBox panel;
        public final TextField nameField;
        public final TextField ownerField; // nullable (only Activity / Feature)
        public final TextField prefixField; // nullable (only Subject)
        private Result(VBox panel, TextField nameField, TextField ownerField, TextField prefixField) {
            this.panel = panel; this.nameField = nameField; this.ownerField = ownerField; this.prefixField = prefixField;
        }
    }

    public static Result build(FDDINode node) {
        Messages messages = Messages.getInstance();
        TextField nameField = new TextField();
        nameField.setPrefWidth(300);
        if (node.getName() != null) nameField.setText(node.getName());

        TextField ownerField = null;
        TextField prefixField = null;

        VBox panel = new VBox(5);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 5;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);

        if (node instanceof Subject subject) {
            prefixField = new TextField(); prefixField.setPrefWidth(50);
            if (subject.getPrefix() != null) prefixField.setText(subject.getPrefix());
            Label prefixLabel = new Label(messages.getMessage(Messages.JLABEL_PREFIX_TITLE));
            grid.add(prefixLabel, 0, 0);
            grid.add(prefixField, 1, 0);
            Label nameLabel = new Label(messages.getMessage(Messages.JLABEL_NAME_CAPTION));
            grid.add(nameLabel, 0, 1);
            grid.add(nameField, 1, 1);
        } else {
            Label nameLabel = new Label(messages.getMessage(Messages.JLABEL_NAME_CAPTION));
            grid.add(nameLabel, 0, 0);
            grid.add(nameField, 1, 0);
            if (node instanceof Activity activity) {
                ownerField = new TextField(); ownerField.setPrefWidth(50);
                if (activity.getInitials() != null) ownerField.setText(activity.getInitials());
                Label ownerLabel = new Label(messages.getMessage(Messages.JLABEL_OWNER_CAPTION));
                grid.add(ownerLabel, 0, 1); grid.add(ownerField, 1, 1);
            } else if (node instanceof Feature feature) {
                ownerField = new TextField(); ownerField.setPrefWidth(50);
                if (feature.getInitials() != null) ownerField.setText(feature.getInitials());
                Label ownerLabel = new Label(messages.getMessage(Messages.JLABEL_OWNER_CAPTION));
                grid.add(ownerLabel, 0, 1); grid.add(ownerField, 1, 1);
            }
        }

        panel.getChildren().add(grid);
        return new Result(panel, nameField, ownerField, prefixField);
    }
}
