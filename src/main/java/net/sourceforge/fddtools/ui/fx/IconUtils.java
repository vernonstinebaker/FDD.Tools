package net.sourceforge.fddtools.ui.fx;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

/**
 * Utility class for creating and managing icons in JavaFX.
 * Provides modern icon support using SVG paths and images.
 */
public class IconUtils {
    
    // Icon sizes
    public static final double SMALL_ICON_SIZE = 16.0;
    public static final double MEDIUM_ICON_SIZE = 20.0;
    public static final double LARGE_ICON_SIZE = 24.0;
    
    // Modern Material Design-inspired SVG paths
    private static final String NEW_DOCUMENT_PATH = "M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20Z";
    private static final String OPEN_FOLDER_PATH = "M10,4H4C2.89,4 2,4.89 2,6V18A2,2 0 0,0 4,20H20A2,2 0 0,0 22,18V8C22,6.89 21.1,6 20,6H12L10,4Z";
    private static final String SAVE_PATH = "M15,9H5V5H15M12,19A3,3 0 0,1 9,16A3,3 0 0,1 12,13A3,3 0 0,1 15,16A3,3 0 0,1 12,19M17,3H5C3.89,3 3,3.9 3,5V19A2,2 0 0,0 5,21H19A2,2 0 0,0 21,19V7L17,3Z";
    private static final String PRINT_PATH = "M18,3H6V7H18M19,12A1,1 0 0,1 18,11A1,1 0 0,1 19,10A1,1 0 0,1 20,11A1,1 0 0,1 19,12M16,19H8V14H16M19,8H5A3,3 0 0,0 2,11V17H6V21H18V17H22V11A3,3 0 0,0 19,8Z";
    private static final String CUT_PATH = "M9.64,7.64C10.37,6.91 10.37,5.73 9.64,5C8.91,4.27 7.73,4.27 7,5C6.27,5.73 6.27,6.91 7,7.64C7.73,8.37 8.91,8.37 9.64,7.64M21.64,2.64C22.37,3.37 22.37,4.55 21.64,5.28L18.36,8.56C17.63,9.29 16.45,9.29 15.72,8.56C14.99,7.83 14.99,6.65 15.72,5.92L19,2.64C19.73,1.91 20.91,1.91 21.64,2.64M8.56,18.36C9.29,17.63 9.29,16.45 8.56,15.72C7.83,14.99 6.65,14.99 5.92,15.72L2.64,19C1.91,19.73 1.91,20.91 2.64,21.64C3.37,22.37 4.55,22.37 5.28,21.64L8.56,18.36M13.5,9L15,10.5L4.5,21L3,19.5L13.5,9Z";
    private static final String COPY_PATH = "M19,21H8V7H19M19,5H8A2,2 0 0,0 6,7V21A2,2 0 0,0 8,23H19A2,2 0 0,0 21,21V7A2,2 0 0,0 19,5M16,1H4A2,2 0 0,0 2,3V17H4V3H16V1Z";
    private static final String PASTE_PATH = "M19,20H5V4H7V7H17V4H19M12,2A1,1 0 0,1 13,3A1,1 0 0,1 12,4A1,1 0 0,1 11,3A1,1 0 0,1 12,2M19,2H14.82C14.4,0.84 13.3,0 12,0C10.7,0 9.6,0.84 9.18,2H5A2,2 0 0,0 3,4V20A2,2 0 0,0 5,22H19A2,2 0 0,0 21,20V4A2,2 0 0,0 19,2Z";
    private static final String ADD_PATH = "M19,13H13V19H11V13H5V11H11V5H13V11H19V13Z";
    private static final String DELETE_PATH = "M19,4H15.5L14.5,3H9.5L8.5,4H5V6H19M6,19A2,2 0 0,0 8,21H16A2,2 0 0,0 18,19V7H6V19Z";
    private static final String EDIT_PATH = "M20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18,2.9 17.35,2.9 16.96,3.29L15.12,5.12L18.87,8.87M3,17.25V21H6.75L17.81,9.93L14.06,6.18L3,17.25Z";
    
    /**
     * Creates an SVG icon with the specified path and size.
     */
    public static Node createSVGIcon(String svgPath, double size) {
        return createSVGIcon(svgPath, size, Color.BLACK);
    }
    
    /**
     * Creates an SVG icon with the specified path, size, and color.
     */
    public static Node createSVGIcon(String svgPath, double size, Color color) {
        SVGPath svg = new SVGPath();
        svg.setContent(svgPath);
        svg.setFill(color);
        
        // Scale the icon to the desired size
        double scale = size / 24.0; // SVG paths are designed for 24x24 viewBox
        svg.setScaleX(scale);
        svg.setScaleY(scale);
        
        return svg;
    }
    
    /**
     * Creates a new document icon.
     */
    public static Node createNewDocumentIcon(double size) {
        return createSVGIcon(NEW_DOCUMENT_PATH, size);
    }
    
    /**
     * Creates an open folder icon.
     */
    public static Node createOpenFolderIcon(double size) {
        return createSVGIcon(OPEN_FOLDER_PATH, size);
    }
    
    /**
     * Creates a save icon.
     */
    public static Node createSaveIcon(double size) {
        return createSVGIcon(SAVE_PATH, size);
    }
    
    /**
     * Creates a print icon.
     */
    public static Node createPrintIcon(double size) {
        return createSVGIcon(PRINT_PATH, size);
    }
    
    /**
     * Creates a cut icon.
     */
    public static Node createCutIcon(double size) {
        return createSVGIcon(CUT_PATH, size);
    }
    
    /**
     * Creates a copy icon.
     */
    public static Node createCopyIcon(double size) {
        return createSVGIcon(COPY_PATH, size);
    }
    
    /**
     * Creates a paste icon.
     */
    public static Node createPasteIcon(double size) {
        return createSVGIcon(PASTE_PATH, size);
    }
    
    /**
     * Creates an add/plus icon.
     */
    public static Node createAddIcon(double size) {
        return createSVGIcon(ADD_PATH, size);
    }
    
    /**
     * Creates a delete/trash icon.
     */
    public static Node createDeleteIcon(double size) {
        return createSVGIcon(DELETE_PATH, size);
    }
    
    /**
     * Creates an edit/pencil icon.
     */
    public static Node createEditIcon(double size) {
        return createSVGIcon(EDIT_PATH, size);
    }
    
    /**
     * Loads an image icon from resources.
     */
    public static ImageView loadImageIcon(String resourcePath, double size) {
        try {
            Image image = new Image(IconUtils.class.getResourceAsStream(resourcePath));
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(size);
            imageView.setFitHeight(size);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            return imageView;
        } catch (Exception e) {
            System.err.println("Failed to load icon: " + resourcePath);
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Creates a styled button with an icon and optional text.
     */
    public static javafx.scene.control.Button createIconButton(Node icon, String text, String tooltip) {
        javafx.scene.control.Button button = new javafx.scene.control.Button(text, icon);
        
        if (tooltip != null && !tooltip.isEmpty()) {
            button.setTooltip(new javafx.scene.control.Tooltip(tooltip));
        }
        
        // Apply modern button styling
        button.getStyleClass().add("icon-button");
        
        return button;
    }
    
    /**
     * Creates a toolbar-style button with just an icon.
     */
    public static javafx.scene.control.Button createToolbarButton(Node icon, String tooltip) {
        javafx.scene.control.Button button = new javafx.scene.control.Button("", icon);
        
        if (tooltip != null && !tooltip.isEmpty()) {
            button.setTooltip(new javafx.scene.control.Tooltip(tooltip));
        }
        
        // Apply toolbar button styling
        button.getStyleClass().addAll("toolbar-button", "icon-button");
        button.setMinSize(32, 32);
        button.setPrefSize(32, 32);
        
        return button;
    }
}