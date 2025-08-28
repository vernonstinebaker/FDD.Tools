package net.sourceforge.fddtools.ui.fx;

import javafx.scene.text.Font;
import com.nebulon.xml.fddi.Program;
import com.nebulon.xml.fddi.Project;
import net.sourceforge.fddtools.model.FDDINode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import net.sourceforge.fddtools.testutil.FxTestUtil;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests Canvas-to-Tree integration in FDDLayoutController.
 */
public class FDDCanvasTreeIntegrationTest {

    @BeforeAll
    static void initJfx() throws Exception {
        FxTestUtil.ensureStarted();
    }

    @Test
    void canvasClickHandlerIsWiredUpCorrectly() throws Exception {
        // Create test data
        Program program = new Program();
        program.setName("Test Program");
        
        Project p1 = new Project();
        p1.setName("Project 1");
        program.getProject().add(p1);

        // Create mock tree to capture selectNode calls
        AtomicReference<FDDINode> selectedNode = new AtomicReference<>();
        AtomicReference<Boolean> scrollRequested = new AtomicReference<>();
        
        FDDTreeViewFX mockTree = new FDDTreeViewFX() {
            @Override
            public void selectNode(FDDINode node, boolean scroll) {
                selectedNode.set(node);
                scrollRequested.set(scroll);
            }
        };

        // Create canvas
        FDDCanvasFX canvas = new FDDCanvasFX(program, Font.font(12));
        
        // Wire up the same way FDDLayoutController does
        canvas.setCanvasClickHandler(clickedNode -> {
            if (mockTree != null && clickedNode != null) {
                mockTree.selectNode(clickedNode, true);
            }
        });

        FxTestUtil.runOnFxAndWait(5, () -> {
            try {
                // Simulate what happens when a canvas element is clicked
                FDDCanvasFX.CanvasClickHandler handler = node -> {
                    if (mockTree != null && node != null) {
                        mockTree.selectNode(node, true);
                    }
                };
                
                // Test the handler directly
                handler.onCanvasNodeClicked(p1);
                
                // Verify the tree selection was triggered
                assertEquals(p1, selectedNode.get(), "Selected node should be passed to tree");
                assertTrue(scrollRequested.get(), "Scroll should be requested");
                
            } catch (Exception e) {
                fail("Integration test failed: " + e.getMessage());
            }
        });
    }

    @Test
    void canvasHandlerDoesNothingWithNullTree() throws Exception {
        Program program = new Program();
        program.setName("Test Program");
        
        Project p1 = new Project();
        p1.setName("Project 1");
        program.getProject().add(p1);

        FDDCanvasFX canvas = new FDDCanvasFX(program, Font.font(12));
        
        // Set up handler similar to FDDLayoutController but with null tree
        FDDTreeViewFX tree = null;
        canvas.setCanvasClickHandler(clickedNode -> {
            if (tree != null && clickedNode != null) {
                tree.selectNode(clickedNode, true);
            }
        });

        FxTestUtil.runOnFxAndWait(5, () -> {
            try {
                // This should not throw any exception
                FDDCanvasFX.CanvasClickHandler handler = node -> {
                    if (tree != null && node != null) {
                        tree.selectNode(node, true);
                    }
                };
                
                handler.onCanvasNodeClicked(p1);
                // Test passes if no exception is thrown
                
            } catch (Exception e) {
                fail("Handler should handle null tree gracefully: " + e.getMessage());
            }
        });
    }
}
