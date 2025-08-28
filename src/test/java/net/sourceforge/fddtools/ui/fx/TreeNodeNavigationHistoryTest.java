package net.sourceforge.fddtools.ui.fx;

import net.sourceforge.fddtools.model.FDDINode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for TreeNodeNavigationHistory functionality.
 */
@DisplayName("Tree Node Navigation History Tests")
class TreeNodeNavigationHistoryTest {
    
    private TreeNodeNavigationHistory history;
    private AtomicBoolean canGoBack;
    private AtomicBoolean canGoForward;
    private AtomicReference<FDDINode> navigatedNode;
    
    @BeforeEach
    void setUp() {
        history = new TreeNodeNavigationHistory();
        canGoBack = new AtomicBoolean(false);
        canGoForward = new AtomicBoolean(false);
        navigatedNode = new AtomicReference<>();
        
        history.setNavigationListener(new TreeNodeNavigationHistory.NavigationListener() {
            @Override
            public void onNavigationStateChanged(boolean back, boolean forward) {
                canGoBack.set(back);
                canGoForward.set(forward);
            }
            
            @Override
            public void onNavigateTo(FDDINode node) {
                navigatedNode.set(node);
            }
        });
    }
    
    @Test
    @DisplayName("Initial state should be empty with no navigation possible")
    void testInitialState() {
        assertTrue(history.isEmpty());
        assertEquals(0, history.size());
        assertFalse(history.canGoBack());
        assertFalse(history.canGoForward());
        assertNull(history.getCurrentNode());
    }
    
    @Test
    @DisplayName("Recording single selection should enable no navigation")
    void testSingleSelection() {
        FDDINode node1 = createMockNode("Node1");
        
        history.recordSelection(node1);
        
        assertEquals(1, history.size());
        assertFalse(history.canGoBack());
        assertFalse(history.canGoForward());
        assertEquals(node1, history.getCurrentNode());
        assertFalse(canGoBack.get());
        assertFalse(canGoForward.get());
    }
    
    @Test
    @DisplayName("Recording multiple selections should enable back navigation")
    void testMultipleSelections() {
        FDDINode node1 = createMockNode("Node1");
        FDDINode node2 = createMockNode("Node2");
        FDDINode node3 = createMockNode("Node3");
        
        history.recordSelection(node1);
        history.recordSelection(node2);
        history.recordSelection(node3);
        
        assertEquals(3, history.size());
        assertTrue(history.canGoBack());
        assertFalse(history.canGoForward());
        assertEquals(node3, history.getCurrentNode());
        assertTrue(canGoBack.get());
        assertFalse(canGoForward.get());
    }
    
    @Test
    @DisplayName("Back navigation should work correctly")
    void testBackNavigation() {
        FDDINode node1 = createMockNode("Node1");
        FDDINode node2 = createMockNode("Node2");
        FDDINode node3 = createMockNode("Node3");
        
        history.recordSelection(node1);
        history.recordSelection(node2);
        history.recordSelection(node3);
        
        // Go back to node2
        assertTrue(history.goBack());
        assertEquals(node2, navigatedNode.get());
        assertEquals(node2, history.getCurrentNode());
        assertTrue(history.canGoBack());
        assertTrue(history.canGoForward());
        
        // Go back to node1
        assertTrue(history.goBack());
        assertEquals(node1, navigatedNode.get());
        assertEquals(node1, history.getCurrentNode());
        assertFalse(history.canGoBack());
        assertTrue(history.canGoForward());
        
        // Try to go back beyond beginning
        assertFalse(history.goBack());
        assertEquals(node1, history.getCurrentNode());
    }
    
    @Test
    @DisplayName("Forward navigation should work correctly")
    void testForwardNavigation() {
        FDDINode node1 = createMockNode("Node1");
        FDDINode node2 = createMockNode("Node2");
        FDDINode node3 = createMockNode("Node3");
        
        history.recordSelection(node1);
        history.recordSelection(node2);
        history.recordSelection(node3);
        history.goBack(); // Now at node2
        history.goBack(); // Now at node1
        
        // Go forward to node2
        assertTrue(history.goForward());
        assertEquals(node2, navigatedNode.get());
        assertEquals(node2, history.getCurrentNode());
        assertTrue(history.canGoBack());
        assertTrue(history.canGoForward());
        
        // Go forward to node3
        assertTrue(history.goForward());
        assertEquals(node3, navigatedNode.get());
        assertEquals(node3, history.getCurrentNode());
        assertTrue(history.canGoBack());
        assertFalse(history.canGoForward());
        
        // Try to go forward beyond end
        assertFalse(history.goForward());
        assertEquals(node3, history.getCurrentNode());
    }
    
    @Test
    @DisplayName("Recording selection after navigation should truncate forward history")
    void testHistoryTruncation() {
        FDDINode node1 = createMockNode("Node1");
        FDDINode node2 = createMockNode("Node2");
        FDDINode node3 = createMockNode("Node3");
        FDDINode node4 = createMockNode("Node4");
        
        history.recordSelection(node1);
        history.recordSelection(node2);
        history.recordSelection(node3);
        history.goBack(); // Now at node2
        
        // Record new selection - should truncate forward history
        history.recordSelection(node4);
        
        assertEquals(3, history.size());
        assertEquals(node4, history.getCurrentNode());
        assertTrue(history.canGoBack());
        assertFalse(history.canGoForward());
        
        // Verify we can't go forward to node3 anymore
        assertFalse(history.goForward());
        
        // Verify we can go back to node2 and node1
        assertTrue(history.goBack());
        assertEquals(node2, navigatedNode.get());
        assertTrue(history.goBack());
        assertEquals(node1, navigatedNode.get());
    }
    
    @Test
    @DisplayName("Duplicate consecutive selections should be ignored")
    void testDuplicateSelections() {
        FDDINode node1 = createMockNode("Node1");
        
        history.recordSelection(node1);
        history.recordSelection(node1); // Duplicate should be ignored
        history.recordSelection(node1); // Another duplicate
        
        assertEquals(1, history.size());
        assertEquals(node1, history.getCurrentNode());
        assertFalse(history.canGoBack());
        assertFalse(history.canGoForward());
    }
    
    @Test
    @DisplayName("Null node selection should be ignored")
    void testNullSelection() {
        FDDINode node1 = createMockNode("Node1");
        
        history.recordSelection(node1);
        history.recordSelection(null); // Should be ignored
        
        assertEquals(1, history.size());
        assertEquals(node1, history.getCurrentNode());
    }
    
    @Test
    @DisplayName("Clear should reset all navigation state")
    void testClear() {
        FDDINode node1 = createMockNode("Node1");
        FDDINode node2 = createMockNode("Node2");
        
        history.recordSelection(node1);
        history.recordSelection(node2);
        
        history.clear();
        
        assertTrue(history.isEmpty());
        assertEquals(0, history.size());
        assertFalse(history.canGoBack());
        assertFalse(history.canGoForward());
        assertNull(history.getCurrentNode());
        assertFalse(canGoBack.get());
        assertFalse(canGoForward.get());
    }
    
    @Test
    @DisplayName("History size limit should prevent memory growth")
    void testHistorySizeLimit() {
        // Create 101 nodes to exceed the 100 item limit
        for (int i = 1; i <= 101; i++) {
            history.recordSelection(createMockNode("Node" + i));
        }
        
        // Should be limited to 100 items
        assertEquals(100, history.size());
        
        // Should still be able to navigate
        assertTrue(history.canGoBack());
        assertEquals("Node101", history.getCurrentNode().getName());
        
        // Go back to verify oldest item was removed
        for (int i = 0; i < 99; i++) {
            assertTrue(history.goBack());
        }
        assertEquals("Node2", history.getCurrentNode().getName()); // Node1 should be gone
    }
    
    @Test
    @DisplayName("Navigation listener updates should be called correctly")
    void testNavigationListenerUpdates() {
        FDDINode node1 = createMockNode("Node1");
        FDDINode node2 = createMockNode("Node2");
        
        // Initial state
        assertFalse(canGoBack.get());
        assertFalse(canGoForward.get());
        
        // After first selection
        history.recordSelection(node1);
        assertFalse(canGoBack.get());
        assertFalse(canGoForward.get());
        
        // After second selection
        history.recordSelection(node2);
        assertTrue(canGoBack.get());
        assertFalse(canGoForward.get());
        
        // After going back
        history.goBack();
        assertFalse(canGoBack.get());
        assertTrue(canGoForward.get());
        
        // After going forward
        history.goForward();
        assertTrue(canGoBack.get());
        assertFalse(canGoForward.get());
        
        // After clear
        history.clear();
        assertFalse(canGoBack.get());
        assertFalse(canGoForward.get());
    }
    
    @Test
    @DisplayName("Navigation should work with complex FDD hierarchy")
    void testNavigationWithRealHierarchy() {
        FDDINode root = createTestHierarchy();
        FDDINode project = (FDDINode) root.getChildren().get(0); // First project
        FDDINode aspect = (FDDINode) project.getChildren().get(0); // First aspect
        
        // Record navigation through the hierarchy
        history.recordSelection(root);
        history.recordSelection(project);
        history.recordSelection(aspect);
        
        assertEquals(3, history.size());
        assertEquals(aspect, history.getCurrentNode());
        assertTrue(history.canGoBack());
        assertFalse(history.canGoForward());
        
        // Navigate back through hierarchy
        assertTrue(history.goBack());
        assertEquals(project, navigatedNode.get());
        assertEquals(project, history.getCurrentNode());
        
        assertTrue(history.goBack());
        assertEquals(root, navigatedNode.get());
        assertEquals(root, history.getCurrentNode());
        
        assertFalse(history.canGoBack());
        assertTrue(history.canGoForward());
    }
    
    /**
     * Creates a mock FDDINode for testing purposes.
     * Creates proper FDD model objects with valid hierarchical relationships.
     */
    private FDDINode createMockNode(String name) {
        com.nebulon.xml.fddi.ObjectFactory factory = new com.nebulon.xml.fddi.ObjectFactory();
        com.nebulon.xml.fddi.Feature feature = factory.createFeature();
        feature.setName(name);
        return feature;
    }
    
    /**
     * Creates a test FDD hierarchy for more realistic testing.
     */
    private FDDINode createTestHierarchy() {
        com.nebulon.xml.fddi.ObjectFactory factory = new com.nebulon.xml.fddi.ObjectFactory();
        
        // Create Program (root)
        com.nebulon.xml.fddi.Program program = factory.createProgram();
        program.setName("Test Program");
        
        // Create Project
        com.nebulon.xml.fddi.Project project = factory.createProject();
        project.setName("Test Project");
        project.setParentNode(program);
        program.getProject().add(project);
        
        // Create Aspect
        com.nebulon.xml.fddi.Aspect aspect = factory.createAspect();
        aspect.setName("Test Aspect");
        aspect.setParentNode(project);
        project.getAspect().add(aspect);
        
        // Create Subject
        com.nebulon.xml.fddi.Subject subject = factory.createSubject();
        subject.setName("Test Subject");
        subject.setParentNode(aspect);
        aspect.getSubject().add(subject);
        
        // Create Activity
        com.nebulon.xml.fddi.Activity activity = factory.createActivity();
        activity.setName("Test Activity");
        activity.setParentNode(subject);
        subject.getActivity().add(activity);
        
        // Create Feature
        com.nebulon.xml.fddi.Feature feature = factory.createFeature();
        feature.setName("Test Feature");
        feature.setParentNode(activity);
        activity.getFeature().add(feature);
        
        return program; // Return the root
    }
}
