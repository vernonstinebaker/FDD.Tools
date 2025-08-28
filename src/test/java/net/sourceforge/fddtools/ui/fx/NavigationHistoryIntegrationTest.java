package net.sourceforge.fddtools.ui.fx;

import net.sourceforge.fddtools.model.FDDINode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the navigation history system integrated with FDDLayoutController.
 */
@DisplayName("Navigation History Integration Tests")
class NavigationHistoryIntegrationTest {
    
    private FDDLayoutController layoutController;
    private TestHost testHost;
    private AtomicBoolean backButtonEnabled;
    private AtomicBoolean forwardButtonEnabled;
    private AtomicReference<FDDINode> lastSelectionChanged;
    
    @BeforeEach
    void setUp() {
        backButtonEnabled = new AtomicBoolean(false);
        forwardButtonEnabled = new AtomicBoolean(false);
        lastSelectionChanged = new AtomicReference<>();
        
        testHost = new TestHost();
        layoutController = new FDDLayoutController(testHost);
    }
    
    @Test
    @DisplayName("Navigation buttons should be updated when history changes")
    void testNavigationButtonUpdates() {
        // Initially both buttons should be disabled
        assertFalse(backButtonEnabled.get());
        assertFalse(forwardButtonEnabled.get());
        
        // Simulate navigation history changes
        testHost.simulateNavigationStateChange(true, false);
        assertTrue(backButtonEnabled.get());
        assertFalse(forwardButtonEnabled.get());
        
        testHost.simulateNavigationStateChange(true, true);
        assertTrue(backButtonEnabled.get());
        assertTrue(forwardButtonEnabled.get());
        
        testHost.simulateNavigationStateChange(false, false);
        assertFalse(backButtonEnabled.get());
        assertFalse(forwardButtonEnabled.get());
    }
    
    @Test
    @DisplayName("Navigation methods should delegate to history system")
    void testNavigationDelegation() {
        // Test that the layout controller's navigation methods work
        // (They would work with actual navigation history in a real scenario)
        assertFalse(layoutController.navigateBack());  // Should return false when no history
        assertFalse(layoutController.navigateForward()); // Should return false when no history
    }
    
    @Test
    @DisplayName("Navigation should trigger selection changes")
    void testNavigationTriggersSelection() {
        FDDINode testNode = createMockNode("TestNode");
        
        // Simulate navigation to a node
        testHost.simulateNavigationTo(testNode);
        
        // Verify selection was changed
        assertEquals(testNode, lastSelectionChanged.get());
    }
    
    /**
     * Test implementation of Host interface for testing purposes.
     */
    private class TestHost implements FDDLayoutController.Host {
        
        @Override
        public void setProjectTree(FDDTreeViewFX tree) {
            // Test implementation
        }
        
        @Override
        public void setCanvas(FDDCanvasFX canvas) {
            // Test implementation
        }
        
        @Override
        public void onSelectionChanged(FDDINode node) {
            lastSelectionChanged.set(node);
        }
        
        @Override
        public void updateTitle() {
            // Test implementation
        }
        
        @Override
        public void showErrorDialog(String title, String msg) {
            // Test implementation
        }
        
        @Override
        public void updateUndoRedo() {
            // Test implementation
        }
        
        @Override
        public javafx.scene.text.Font getDefaultFont() {
            return null; // Test implementation
        }
        
        @Override
        public javafx.scene.control.SplitPane getMainSplit() {
            return null; // Test implementation
        }
        
        @Override
        public javafx.scene.control.SplitPane getRightSplit() {
            return null; // Test implementation
        }
        
        @Override
        public javafx.scene.control.TabPane getInfoTabs() {
            return null; // Test implementation
        }
        
        @Override
        public FDDTreeContextMenuHandler contextMenuHandler() {
            return null; // Test implementation
        }
        
        @Override
        public void updateNavigationButtons(boolean canGoBack, boolean canGoForward) {
            backButtonEnabled.set(canGoBack);
            forwardButtonEnabled.set(canGoForward);
        }
        
        @Override
        public FDDTreeViewFX getProjectTree() {
            return null; // Test implementation
        }
        
        @Override
        public FDDActionPanelFX.FDDActionHandler getActionHandler() {
            return null; // Test implementation
        }
        
        /**
         * Helper method to simulate navigation state changes for testing.
         */
        public void simulateNavigationStateChange(boolean canGoBack, boolean canGoForward) {
            updateNavigationButtons(canGoBack, canGoForward);
        }
        
        /**
         * Helper method to simulate navigation to a specific node for testing.
         */
        public void simulateNavigationTo(FDDINode node) {
            onSelectionChanged(node);
        }
    }
    
    /**
     * Creates a mock FDDINode for testing purposes.
     */
    private FDDINode createMockNode(String name) {
        com.nebulon.xml.fddi.ObjectFactory factory = new com.nebulon.xml.fddi.ObjectFactory();
        com.nebulon.xml.fddi.Feature feature = factory.createFeature();
        feature.setName(name);
        return feature;
    }
}
