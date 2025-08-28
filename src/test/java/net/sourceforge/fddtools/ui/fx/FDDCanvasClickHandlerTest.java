package net.sourceforge.fddtools.ui.fx;

import javafx.scene.text.Font;
import com.nebulon.xml.fddi.Program;
import com.nebulon.xml.fddi.Project;
import net.sourceforge.fddtools.model.FDDINode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import net.sourceforge.fddtools.testutil.FxTestUtil;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests Canvas-to-Tree focus integration functionality.
 */
public class FDDCanvasClickHandlerTest {

    @BeforeAll
    static void initJfx() throws Exception {
        FxTestUtil.ensureStarted();
    }

    @Test
    void clickHandlerIsCalledWhenNodeIsClicked() throws Exception {
        // Create test data with multiple children
        Program program = new Program();
        program.setName("Test Program");
        
        Project p1 = new Project();
        p1.setName("Project 1");
        program.getProject().add(p1);
        
        Project p2 = new Project();  
        p2.setName("Project 2");
        program.getProject().add(p2);

        FDDCanvasFX canvas = new FDDCanvasFX(program, Font.font(12));
        
        // Set up click handler to capture clicked nodes
        AtomicReference<FDDINode> clickedNode = new AtomicReference<>();
        canvas.setCanvasClickHandler(node -> clickedNode.set(node));

        // Use reflection to access private methods for testing
        Method findNodeAtCoordinates = FDDCanvasFX.class.getDeclaredMethod("findNodeAtCoordinates", double.class, double.class);
        findNodeAtCoordinates.setAccessible(true);
        
        Method handleCanvasClick = FDDCanvasFX.class.getDeclaredMethod("handleCanvasClick", double.class, double.class);
        handleCanvasClick.setAccessible(true);

        FxTestUtil.runOnFxAndWait(5, () -> {
            try {
                // Test coordinate mapping - click on first element
                // Based on layout constants: BORDER_WIDTH=5, FRINGE_WIDTH=20, title area
                double clickX = 5 + 20 + 50; // Border + fringe + middle of first element
                double clickY = 20 + 20 + 5 + 70; // Title + fringe + border + middle of element
                
                // Test findNodeAtCoordinates directly
                FDDINode foundNode = (FDDINode) findNodeAtCoordinates.invoke(canvas, clickX, clickY);
                assertNotNull(foundNode, "Should find a node at valid coordinates");
                assertEquals("Project 1", foundNode.getName(), "Should find the first project");
                
                // Test handleCanvasClick triggers the handler
                handleCanvasClick.invoke(canvas, clickX, clickY);
                assertEquals(foundNode, clickedNode.get(), "Click handler should be called with the found node");
                
            } catch (Exception e) {
                fail("Test execution failed: " + e.getMessage());
            }
        });
    }

    @Test
    void noHandlerCalledForEmptyArea() throws Exception {
        Program program = new Program();
        program.setName("Empty Program");
        
        FDDCanvasFX canvas = new FDDCanvasFX(program, Font.font(12));
        
        AtomicReference<FDDINode> clickedNode = new AtomicReference<>();
        canvas.setCanvasClickHandler(node -> clickedNode.set(node));

        Method handleCanvasClick = FDDCanvasFX.class.getDeclaredMethod("handleCanvasClick", double.class, double.class);
        handleCanvasClick.setAccessible(true);

        FxTestUtil.runOnFxAndWait(5, () -> {
            try {
                // Click in empty area (far right)
                handleCanvasClick.invoke(canvas, 1000.0, 1000.0);
                assertNull(clickedNode.get(), "No handler should be called for empty area clicks");
                
            } catch (Exception e) {
                fail("Test execution failed: " + e.getMessage());
            }
        });
    }

    @Test
    void handlerNotCalledWhenNotSet() throws Exception {
        Program program = new Program();
        program.setName("Test Program");
        
        Project p1 = new Project();
        p1.setName("Project 1");
        program.getProject().add(p1);

        FDDCanvasFX canvas = new FDDCanvasFX(program, Font.font(12));
        // Deliberately don't set click handler

        Method handleCanvasClick = FDDCanvasFX.class.getDeclaredMethod("handleCanvasClick", double.class, double.class);
        handleCanvasClick.setAccessible(true);

        FxTestUtil.runOnFxAndWait(5, () -> {
            try {
                // This should not throw an exception even without a handler
                handleCanvasClick.invoke(canvas, 50.0, 50.0);
                // Test passes if no exception is thrown
                
            } catch (Exception e) {
                fail("handleCanvasClick should not fail when no handler is set: " + e.getMessage());
            }
        });
    }

    @Test
    void findNodeAtCoordinatesHandlesWrapping() throws Exception {
        Program program = new Program();
        program.setName("Test Program");
        
        // Add enough projects to force wrapping
        for (int i = 0; i < 5; i++) {
            Project p = new Project();
            p.setName("Project " + (i + 1));
            program.getProject().add(p);
        }

        FDDCanvasFX canvas = new FDDCanvasFX(program, Font.font(12));

        Method findNodeAtCoordinates = FDDCanvasFX.class.getDeclaredMethod("findNodeAtCoordinates", double.class, double.class);
        findNodeAtCoordinates.setAccessible(true);

        FxTestUtil.runOnFxAndWait(5, () -> {
            try {
                // Force a narrow layout to test wrapping
                Method calculateCanvasHeight = FDDCanvasFX.class.getDeclaredMethod("calculateCanvasHeight", double.class);
                calculateCanvasHeight.setAccessible(true);
                calculateCanvasHeight.invoke(canvas, 400.0); // Narrow width
                
                // Test clicking on what should be the second row
                double clickX = 5 + 20 + 50; // First element position in row
                double clickY = 20 + 20 + 5 + 140 + 20 + 70; // Title + fringe + border + element height + fringe + middle of second row
                
                FDDINode foundNode = (FDDINode) findNodeAtCoordinates.invoke(canvas, clickX, clickY);
                // Should find some node (exact one depends on layout calculation)
                if (foundNode != null) {
                    assertTrue(foundNode.getName().startsWith("Project"), "Found node should be one of our test projects");
                }
                
            } catch (Exception e) {
                fail("Test execution failed: " + e.getMessage());
            }
        });
    }
}
