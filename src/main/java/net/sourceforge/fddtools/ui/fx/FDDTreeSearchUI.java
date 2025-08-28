package net.sourceforge.fddtools.ui.fx;

import net.sourceforge.fddtools.search.FDDTreeSearchController;
import net.sourceforge.fddtools.search.FDDTreeSearchEngine;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Priority;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.util.List;

/**
 * Search UI component for the FDD tree toolbar.
 * Provides search field with navigation controls and match display.
 */
public class FDDTreeSearchUI extends HBox implements FDDTreeSearchController.SearchListener {
    
    private StackPane searchFieldContainer;
    private TextField searchField;
    private Button clearButton;
    private Button previousButton;
    private Button nextButton;
    private Label matchLabel;
    
    private FDDTreeSearchController searchController;
    
    public FDDTreeSearchUI() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        // Create search field container with embedded clear button
        searchFieldContainer = new StackPane();
        searchFieldContainer.setPrefWidth(200);
        searchFieldContainer.setMaxWidth(250);
        searchFieldContainer.setMinWidth(150);
        
        // Search field
        searchField = new TextField();
        searchField.setPromptText("Search features...");
        searchField.getStyleClass().add("search-field");
        // Add right padding to make room for clear button
        searchField.setStyle("-fx-padding: 4px 28px 4px 8px;");
        
        // Clear button (embedded inside search field)
        clearButton = new Button("✕");
        clearButton.setTooltip(new Tooltip("Clear search (Escape)"));
        clearButton.getStyleClass().addAll("search-clear-button-embedded");
        clearButton.setDisable(true);
        clearButton.setVisible(false); // Hidden until there's text
        StackPane.setAlignment(clearButton, Pos.CENTER_RIGHT);
        StackPane.setMargin(clearButton, new Insets(0, 6, 0, 0));
        
        // Add both to container
        searchFieldContainer.getChildren().addAll(searchField, clearButton);
        
        // Navigation buttons
        previousButton = new Button("◀");
        previousButton.setTooltip(new Tooltip("Previous match (Shift+F3)"));
        previousButton.getStyleClass().addAll("search-nav-button", "search-prev-button");
        previousButton.setDisable(true);
        
        nextButton = new Button("▶");
        nextButton.setTooltip(new Tooltip("Next match (F3)"));
        nextButton.getStyleClass().addAll("search-nav-button", "search-next-button");
        nextButton.setDisable(true);
        
        // Match count label
        matchLabel = new Label();
        matchLabel.getStyleClass().add("search-match-label");
        matchLabel.setVisible(false);
    }
    
    private void setupLayout() {
        getStyleClass().add("search-container");
        setSpacing(4);
        setPadding(new Insets(2));
        
        // Add components (search field container instead of individual field)
        getChildren().addAll(searchFieldContainer, previousButton, nextButton, matchLabel);
        
        // Search field container should not grow
        HBox.setHgrow(searchFieldContainer, Priority.NEVER);
        HBox.setHgrow(matchLabel, Priority.NEVER);
    }
    
    private void setupEventHandlers() {
        // Search field text changes
        searchField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                // Show/hide clear button based on text content
                boolean hasText = newValue != null && !newValue.trim().isEmpty();
                clearButton.setVisible(hasText);
                clearButton.setDisable(!hasText);
                
                if (searchController != null) {
                    if (!hasText) {
                        searchController.clearSearch();
                    } else {
                        searchController.search(newValue);
                    }
                }
            }
        });
        
        // Navigation button actions
        previousButton.setOnAction(e -> {
            if (searchController != null) {
                searchController.navigateToPrevious();
            }
        });
        
        nextButton.setOnAction(e -> {
            if (searchController != null) {
                searchController.navigateToNext();
            }
        });
        
        clearButton.setOnAction(e -> {
            searchField.clear();
            searchField.requestFocus();
        });
        
        // Keyboard shortcuts
        searchField.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case F3:
                    if (e.isShiftDown()) {
                        if (searchController != null) {
                            searchController.navigateToPrevious();
                        }
                    } else {
                        if (searchController != null) {
                            searchController.navigateToNext();
                        }
                    }
                    e.consume();
                    break;
                case ESCAPE:
                    searchField.clear();
                    e.consume();
                    break;
                case ENTER:
                    if (searchController != null) {
                        searchController.navigateToNext();
                    }
                    e.consume();
                    break;
                default:
                    // Let other keys pass through normally
                    break;
            }
        });
    }
    
    /**
     * Sets the search controller to use for search operations.
     */
    public void setSearchController(FDDTreeSearchController controller) {
        this.searchController = controller;
        if (controller != null) {
            controller.setSearchListener(this);
        }
    }
    
    /**
     * Gets the search text field for external focus management.
     */
    public TextField getSearchField() {
        return searchField;
    }
    
    // SearchListener implementation
    
    @Override
    public void onSearchResults(String query, List<FDDTreeSearchEngine.SearchMatch> matches) {
        updateUIForSearchResults(matches.size());
    }
    
    @Override
    public void onCurrentMatchChanged(int matchIndex, int totalMatches) {
        updateMatchLabel(matchIndex, totalMatches);
        updateNavigationButtons(matchIndex, totalMatches);
    }
    
    @Override
    public void onSearchCleared() {
        updateUIForSearchResults(0);
        matchLabel.setVisible(false);
        clearButton.setVisible(false);
        clearButton.setDisable(true);
    }
    
    // Private UI update methods
    
    private void updateUIForSearchResults(int matchCount) {
        boolean hasMatches = matchCount > 0;
        boolean hasSearch = !searchField.getText().trim().isEmpty();
        
        previousButton.setDisable(!hasMatches);
        nextButton.setDisable(!hasMatches);
        clearButton.setDisable(!hasSearch);
        
        if (hasMatches) {
            matchLabel.setVisible(true);
        } else if (hasSearch) {
            matchLabel.setText("No matches");
            matchLabel.setVisible(true);
        } else {
            matchLabel.setVisible(false);
        }
    }
    
    private void updateMatchLabel(int currentIndex, int totalMatches) {
        if (totalMatches > 0) {
            matchLabel.setText(String.format("%d of %d", currentIndex + 1, totalMatches));
            matchLabel.setVisible(true);
        } else {
            matchLabel.setVisible(false);
        }
    }
    
    private void updateNavigationButtons(int currentIndex, int totalMatches) {
        boolean hasMatches = totalMatches > 0;
        previousButton.setDisable(!hasMatches);
        nextButton.setDisable(!hasMatches);
    }
}
