package net.sourceforge.fddtools.search;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import javafx.scene.control.TreeItem;
import net.sourceforge.fddtools.model.FDDINode;
import com.nebulon.xml.fddi.Program;
import com.nebulon.xml.fddi.Project;
import com.nebulon.xml.fddi.Feature;

import java.util.List;

class FDDTreeSearchEngineTest {
    
    private FDDTreeSearchEngine searchEngine;
    private TreeItem<FDDINode> rootItem;
    
    @BeforeEach
    void setUp() {
        searchEngine = new FDDTreeSearchEngine();
        
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
    }
    
        @Test
    void testExactMatch() {
        List<FDDTreeSearchEngine.SearchMatch> matches = searchEngine.search(rootItem, "User Registration");
        
        assertTrue(matches.size() >= 1, "Should find at least the exact match");
        
        // Find the exact match (highest score should be first due to sorting)
        FDDTreeSearchEngine.SearchMatch exactMatch = matches.get(0);
        assertEquals("User Registration", exactMatch.getNode().getName());
        assertEquals(1.0, exactMatch.getScore(), 0.001, "Exact match should have score 1.0");
    }
    
    @Test
    void testFuzzyMatch() {
        List<FDDTreeSearchEngine.SearchMatch> matches = searchEngine.search(rootItem, "Registration");
        
        assertTrue(matches.size() > 0, "Should find fuzzy matches for 'Registration'");
        
        // Should find "User Registration"
        boolean foundTarget = matches.stream()
            .anyMatch(match -> "User Registration".equals(match.getNode().getName()));
        assertTrue(foundTarget, "Should find 'User Registration' when searching for 'Registration'");
        
        // All matches should have reasonable scores
        for (FDDTreeSearchEngine.SearchMatch match : matches) {
            assertTrue(match.getScore() > 0, "All matches should have positive scores");
        }
    }
    
    @Test
    void testStartsWithMatch() {
        List<FDDTreeSearchEngine.SearchMatch> matches = searchEngine.search(rootItem, "User");
        
        assertEquals(3, matches.size(), "Should find all items starting with 'User'");
        
        // Verify all matches contain "User"
        for (FDDTreeSearchEngine.SearchMatch match : matches) {
            assertTrue(match.getNode().getName().toLowerCase().contains("user"));
        }
        
        // Verify sorting by score (highest first)
        for (int i = 1; i < matches.size(); i++) {
            assertTrue(matches.get(i - 1).getScore() >= matches.get(i).getScore(),
                "Matches should be sorted by score (highest first)");
        }
    }
    
    @Test
    void testContainsMatch() {
        List<FDDTreeSearchEngine.SearchMatch> matches = searchEngine.search(rootItem, "Management");
        
        assertEquals(1, matches.size(), "Should find item containing 'Management'");
        assertEquals("User Management", matches.get(0).getNode().getName());
        assertTrue(matches.get(0).getScore() > 0.5, "Contains match should have good score");
    }
    
    @Test
    void testNoMatch() {
        List<FDDTreeSearchEngine.SearchMatch> matches = searchEngine.search(rootItem, "NonExistent");
        
        assertEquals(0, matches.size(), "Should find no matches for non-existent term");
    }
    
    @Test
    void testEmptyQuery() {
        List<FDDTreeSearchEngine.SearchMatch> matches = searchEngine.search(rootItem, "");
        
        assertEquals(0, matches.size(), "Empty query should return no matches");
    }
    
    @Test
    void testNullQuery() {
        List<FDDTreeSearchEngine.SearchMatch> matches = searchEngine.search(rootItem, null);
        
        assertEquals(0, matches.size(), "Null query should return no matches");
    }
    
    @Test
    void testCaseInsensitive() {
        List<FDDTreeSearchEngine.SearchMatch> matches = searchEngine.search(rootItem, "USER");
        
        assertTrue(matches.size() > 0, "Search should be case insensitive");
        
        // Verify case insensitive matching
        boolean foundUserItem = matches.stream()
            .anyMatch(match -> match.getNode().getName().toLowerCase().contains("user"));
        assertTrue(foundUserItem, "Should find items with 'user' regardless of query case");
    }
    
    @Test
    void testGetNodePath() {
        // Find a feature item
        List<FDDTreeSearchEngine.SearchMatch> matches = searchEngine.search(rootItem, "User Registration");
        assertTrue(matches.size() >= 1, "Should find the User Registration item");
        
        // Find the exact match with score 1.0
        FDDTreeSearchEngine.SearchMatch exactMatch = matches.stream()
            .filter(match -> "User Registration".equals(match.getNode().getName()))
            .findFirst()
            .orElse(null);
        assertNotNull(exactMatch, "Should find exact match for User Registration");
        
        TreeItem<FDDINode> featureItem = exactMatch.getTreeItem();
        List<String> path = searchEngine.getNodePath(featureItem);
        
        assertEquals(3, path.size(), "Path should include program, project, and feature");
        assertEquals("Test Program", path.get(0));
        assertEquals("User Management", path.get(1));
        assertEquals("User Registration", path.get(2));
    }
}
