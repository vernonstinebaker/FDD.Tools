package net.sourceforge.fddtools.ui.fx;

import com.nebulon.xml.fddi.ObjectFactory;
import com.nebulon.xml.fddi.Program;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.testutil.FxTestUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for FDDTreeContainerFX functionality.
 */
public class FDDTreeContainerTest {
    
    private FDDTreeContainerFX treeContainer;
    
    @BeforeAll
    public static void initJavaFX() {
        FxTestUtil.ensureStarted();
    }
    
    @BeforeEach
    public void setUp() throws Exception {
        FxTestUtil.runOnFxAndWait(5, () -> {
            treeContainer = new FDDTreeContainerFX(true);
        });
    }
    
    @Test
    public void testTreeContainerCreation() throws Exception {
        FxTestUtil.runOnFxAndWait(5, () -> {
            assertNotNull(treeContainer);
            assertNotNull(treeContainer.getTreeView());
            assertNotNull(treeContainer.getActionPanel());
            
            // Verify layout structure: search toolbar, tree view, action panel
            assertEquals(3, treeContainer.getChildren().size());
            // Search toolbar is first (index 0)
            assertEquals(treeContainer.getTreeView(), treeContainer.getChildren().get(1));
            assertEquals(treeContainer.getActionPanel(), treeContainer.getChildren().get(2));
        });
    }
    
    @Test
    public void testTreeContainerSizing() throws Exception {
        FxTestUtil.runOnFxAndWait(5, () -> {
            // Check that minimum and preferred widths are set correctly
            assertEquals(140, treeContainer.getMinWidth(), 0.1);
            assertEquals(220, treeContainer.getPrefWidth(), 0.1);
        });
    }
    
    @Test
    public void testActionHandlerSetting() throws Exception {
        FxTestUtil.runOnFxAndWait(5, () -> {
            TestActionHandler handler = new TestActionHandler();
            treeContainer.setActionHandler(handler);
            
            // The action handler should be set on the action panel
            // We can't directly verify this, but we can test that it doesn't throw
            assertDoesNotThrow(() -> treeContainer.setActionHandler(handler));
        });
    }
    
    @Test
    public void testTreePopulation() throws Exception {
        FxTestUtil.runOnFxAndWait(5, () -> {
            // Create a test FDD structure
            ObjectFactory factory = new ObjectFactory();
            Program program = factory.createProgram();
            program.setName("Test Program");
            
            assertDoesNotThrow(() -> treeContainer.populateTree(program));
        });
    }
    
    @Test
    public void testConvenienceMethods() throws Exception {
        FxTestUtil.runOnFxAndWait(5, () -> {
            // Create a test FDD structure
            ObjectFactory factory = new ObjectFactory();
            Program program = factory.createProgram();
            program.setName("Test Program");
            
            // Test convenience methods don't throw exceptions
            assertDoesNotThrow(() -> {
                treeContainer.populateTree(program);
                treeContainer.setContextMenuHandler(null);
                treeContainer.selectNode(program, false);
            });
        });
    }
    
    /**
     * Test implementation of action handler for testing purposes.
     */
    private static class TestActionHandler implements FDDActionPanelFX.FDDActionHandler {
        @Override
        public void onAdd() {
            // Test implementation
        }
        
        @Override
        public void onDelete() {
            // Test implementation
        }
        
        @Override
        public void onEdit() {
            // Test implementation
        }
        
        @Override
        public void onAddProgram() {
            // Test implementation
        }
        
        @Override
        public void onAddProject() {
            // Test implementation
        }
        
        @Override
        public FDDINode getSelectedNode() {
            return null; // Test implementation
        }
    }
    
    @Test
    public void testSearchComponentsIntegration() throws Exception {
        FxTestUtil.runOnFxAndWait(5, () -> {
            // Verify that search components are properly integrated
            assertNotNull(treeContainer.getSearchController(), "Search controller should be available");
            assertNotNull(treeContainer.getSearchUI(), "Search UI should be available");
            assertNotNull(treeContainer.getSearchUI().getSearchField(), "Search field should be available");
            
            // Verify layout includes search toolbar
            assertEquals(3, treeContainer.getChildren().size(), "Should have search toolbar, tree view, and action panel");
        });
    }
    
    @Test
    public void testSearchMethods() throws Exception {
        FxTestUtil.runOnFxAndWait(5, () -> {
            // Test search convenience methods
            assertDoesNotThrow(() -> {
                treeContainer.focusSearch();
            }, "Focus search should not throw");
            
            assertDoesNotThrow(() -> {
                treeContainer.clearSearch();
            }, "Clear search should not throw");
            
            // Verify search controller state after clear
            assertEquals("", treeContainer.getSearchController().getCurrentQuery(), 
                "Query should be empty after clear");
            assertEquals(0, treeContainer.getSearchController().getCurrentMatches().size(), 
                "Matches should be empty after clear");
        });
    }
    
    @Test
    public void testSearchWithPopulatedTree() throws Exception {
        FxTestUtil.runOnFxAndWait(5, () -> {
            // Populate tree with test data
            ObjectFactory factory = new ObjectFactory();
            Program program = factory.createProgram();
            program.setName("Test Program");
            
            treeContainer.populateTree(program);
            
            // Perform search
            treeContainer.getSearchController().search("Test");
            
            // Verify search found the program
            assertTrue(treeContainer.getSearchController().getCurrentMatches().size() > 0, 
                "Search should find matches in populated tree");
            assertEquals("Test", treeContainer.getSearchController().getCurrentQuery(),
                "Search query should be stored");
        });
    }
}
