package net.sourceforge.fddtools.search;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;

import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.ui.fx.FDDTreeViewFX;
import net.sourceforge.fddtools.testutil.HeadlessTestUtil;
import com.nebulon.xml.fddi.Program;
import com.nebulon.xml.fddi.Project;
import com.nebulon.xml.fddi.Feature;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class FDDTreeSearchControllerTest {
    
    private static boolean fxStarted = false;
    private FDDTreeSearchController searchController;
    private FDDTreeViewFX treeView;
    private TreeItem<FDDINode> rootItem;
    private TestSearchListener listener;
    
    @BeforeAll
    static void initializeJavaFX() throws Exception {
        if (!fxStarted) {
            // Configure headless environment
            HeadlessTestUtil.configureHeadlessEnvironment();
            
            // Initialize JavaFX toolkit
            CountDownLatch latch = new CountDownLatch(1);
            try { 
                Platform.startup(latch::countDown); 
            } catch (IllegalStateException alreadyStarted) { 
                latch.countDown(); 
            }
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new RuntimeException("JavaFX initialization timeout");
            }
            fxStarted = true;
        }
    }
    
    // Test search listener to capture events
    private static class TestSearchListener implements FDDTreeSearchController.SearchListener {
        public String lastQuery = null;
        public List<FDDTreeSearchEngine.SearchMatch> lastMatches = null;
        public int lastMatchIndex = -1;
        public int lastTotalMatches = 0;
        public boolean searchCleared = false;
        
        @Override
        public void onSearchResults(String query, List<FDDTreeSearchEngine.SearchMatch> matches) {
            this.lastQuery = query;
            this.lastMatches = matches;
        }
        
        @Override
        public void onCurrentMatchChanged(int matchIndex, int totalMatches) {
            this.lastMatchIndex = matchIndex;
            this.lastTotalMatches = totalMatches;
        }
        
        @Override
        public void onSearchCleared() {
            this.searchCleared = true;
        }
        
        public void reset() {
            lastQuery = null;
            lastMatches = null;
            lastMatchIndex = -1;
            lastTotalMatches = 0;
            searchCleared = false;
        }
    }
    
    @BeforeEach
    void setUp() throws Exception {
        // Create test tree structure
        Program program = new Program();
        program.setName("Test Program");
        
        Project project = new Project();
        project.setName("User Management");
        
        Feature feature1 = new Feature();
        feature1.setName("User Registration");
        
        Feature feature2 = new Feature();
        feature2.setName("User Authentication");
        
        Feature feature3 = new Feature();
        feature3.setName("Password Reset");
        
        // Build tree structure and initialize UI components on FX thread
        CountDownLatch setupLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                // Create real tree view
                treeView = new FDDTreeViewFX(true);
                
                // Build tree structure
                rootItem = new TreeItem<>(program);
                TreeItem<FDDINode> projectItem = new TreeItem<>(project);
                TreeItem<FDDINode> feature1Item = new TreeItem<>(feature1);
                TreeItem<FDDINode> feature2Item = new TreeItem<>(feature2);
                TreeItem<FDDINode> feature3Item = new TreeItem<>(feature3);
                
                rootItem.getChildren().add(projectItem);
                projectItem.getChildren().add(feature1Item);
                projectItem.getChildren().add(feature2Item);
                projectItem.getChildren().add(feature3Item);
                
                // Expand all items
                rootItem.setExpanded(true);
                projectItem.setExpanded(true);
                
                // Set tree root
                treeView.setRoot(rootItem);
                
                // Create controller and listener
                searchController = new FDDTreeSearchController(treeView);
                listener = new TestSearchListener();
                searchController.setSearchListener(listener);
            } finally {
                setupLatch.countDown();
            }
        });
        
        if (!setupLatch.await(5, TimeUnit.SECONDS)) {
            throw new RuntimeException("Setup timeout");
        }
    }
    
    @Test
    void testBasicSearch() {
        searchController.search("User");
        
        // Verify search results were found
        assertNotNull(listener.lastMatches, "Search should produce results");
        assertTrue(listener.lastMatches.size() > 0, "Should find User-related items");
        assertEquals("User", listener.lastQuery, "Query should be stored");
        assertEquals(0, listener.lastMatchIndex, "Should start at first match");
        assertEquals(listener.lastMatches.size(), listener.lastTotalMatches, "Total matches should be correct");
    }
    
    @Test
    void testEmptySearch() {
        searchController.search("");
        
        assertTrue(listener.searchCleared, "Empty search should clear results");
        assertEquals(-1, searchController.getCurrentMatchIndex(), "Match index should be reset");
        assertEquals(0, searchController.getCurrentMatches().size(), "Should have no matches");
    }
    
    @Test
    void testNullSearch() {
        searchController.search(null);
        
        assertTrue(listener.searchCleared, "Null search should clear results");
        assertEquals("", searchController.getCurrentQuery(), "Query should be empty");
    }
    
    @Test
    void testNavigationBetweenMatches() {
        // Start with a search that has multiple results
        searchController.search("User");
        int totalMatches = listener.lastTotalMatches;
        assertTrue(totalMatches > 1, "Need multiple matches for navigation test");
        
        // Test next navigation
        listener.reset();
        boolean result = searchController.navigateToNext();
        assertTrue(result, "Next navigation should succeed");
        assertEquals(1, listener.lastMatchIndex, "Should move to second match");
        assertEquals(totalMatches, listener.lastTotalMatches, "Total should remain same");
        
        // Test wrapping at end
        // We're currently at index 1, need to get to the last index (totalMatches - 1)
        for (int i = 1; i < totalMatches - 1; i++) {
            searchController.navigateToNext();
        }
        // Now we should be at the last index (totalMatches - 1)
        listener.reset();
        searchController.navigateToNext(); // Should wrap to 0
        assertEquals(0, listener.lastMatchIndex, "Should wrap to first match");
        
        // Test previous navigation
        listener.reset();
        result = searchController.navigateToPrevious();
        assertTrue(result, "Previous navigation should succeed");
        assertEquals(totalMatches - 1, listener.lastMatchIndex, "Should wrap to last match");
    }
    
    @Test
    void testNavigationWithNoMatches() {
        searchController.search("NonExistent");
        
        boolean nextResult = searchController.navigateToNext();
        boolean prevResult = searchController.navigateToPrevious();
        
        assertFalse(nextResult, "Next should fail with no matches");
        assertFalse(prevResult, "Previous should fail with no matches");
    }
    
    @Test
    void testIsHighlighted() {
        searchController.search("User Registration");
        
        List<FDDTreeSearchEngine.SearchMatch> matches = searchController.getCurrentMatches();
        assertTrue(matches.size() > 0, "Should have matches");
        
        TreeItem<FDDINode> highlightedItem = matches.get(0).getTreeItem();
        assertTrue(searchController.isHighlighted(highlightedItem), "Matched item should be highlighted");
        
        // Create a non-matched item
        Feature nonMatchedFeature = new Feature();
        nonMatchedFeature.setName("Non Matched Feature");
        TreeItem<FDDINode> nonMatchedItem = new TreeItem<>(nonMatchedFeature);
        
        assertFalse(searchController.isHighlighted(nonMatchedItem), "Non-matched item should not be highlighted");
    }
    
    @Test
    void testClearSearch() {
        // Start with a search
        searchController.search("User");
        assertTrue(searchController.getCurrentMatches().size() > 0, "Should have matches");
        
        // Clear search
        listener.reset();
        searchController.clearSearch();
        
        assertTrue(listener.searchCleared, "Clear event should be fired");
        assertEquals("", searchController.getCurrentQuery(), "Query should be cleared");
        assertEquals(0, searchController.getCurrentMatches().size(), "Matches should be cleared");
        assertEquals(-1, searchController.getCurrentMatchIndex(), "Match index should be reset");
    }
    
    @Test
    void testSearchStateAfterClear() {
        // Search, navigate, then clear
        searchController.search("User");
        searchController.navigateToNext();
        int beforeClearIndex = searchController.getCurrentMatchIndex();
        assertTrue(beforeClearIndex >= 0, "Should have valid match index");
        
        searchController.clearSearch();
        
        // Verify all state is reset
        assertEquals("", searchController.getCurrentQuery());
        assertEquals(0, searchController.getCurrentMatches().size());
        assertEquals(-1, searchController.getCurrentMatchIndex());
    }
    
    @Test
    void testConsecutiveSearches() throws Exception {
        // First search
        searchController.search("User");
        int firstSearchMatches = listener.lastTotalMatches;
        
        // Second search (different query)
        listener.reset();
        searchController.search("Reset"); // Use "Reset" which should match "Password Reset"
        
        // Wait a bit for any async operations to complete
        Thread.sleep(100);
        
        int secondSearchMatches = listener.lastTotalMatches;
        
        assertNotEquals(firstSearchMatches, secondSearchMatches, "Different searches should have different results");
        assertEquals("Reset", searchController.getCurrentQuery(), "Query should be updated");
        assertEquals(0, searchController.getCurrentMatchIndex(), "Should reset to first match");
    }

    @Test
    void testListenerNotification() {
        AtomicInteger searchResultsCount = new AtomicInteger(0);
        AtomicInteger matchChangedCount = new AtomicInteger(0);
        AtomicInteger clearCount = new AtomicInteger(0);
        
        FDDTreeSearchController.SearchListener countingListener = new FDDTreeSearchController.SearchListener() {
            @Override
            public void onSearchResults(String query, List<FDDTreeSearchEngine.SearchMatch> matches) {
                searchResultsCount.incrementAndGet();
            }
            
            @Override
            public void onCurrentMatchChanged(int matchIndex, int totalMatches) {
                matchChangedCount.incrementAndGet();
            }
            
            @Override
            public void onSearchCleared() {
                clearCount.incrementAndGet();
            }
        };
        
        searchController.setSearchListener(countingListener);
        
        // Perform operations
        searchController.search("User");
        searchController.navigateToNext();
        searchController.navigateToPrevious();
        searchController.clearSearch();
        
        assertEquals(1, searchResultsCount.get(), "Should notify search results once");
        assertEquals(3, matchChangedCount.get(), "Should notify match changes 3 times (initial + 2 navigations)");
        assertEquals(1, clearCount.get(), "Should notify clear once");
    }
}
