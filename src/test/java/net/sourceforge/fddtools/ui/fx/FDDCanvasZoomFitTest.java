package net.sourceforge.fddtools.ui.fx;

import javafx.scene.text.Font;
import javafx.scene.control.ScrollPane;
import javafx.scene.canvas.Canvas;
import com.nebulon.xml.fddi.Program;
import com.nebulon.xml.fddi.Project;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import net.sourceforge.fddtools.testutil.FxTestUtil;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for FDDCanvasFX zoom and fit functionality.
 * Validates the advanced canvas features including perfect fit-to-window,
 * zoom controls, scroll behavior, and viewport responsiveness.
 */
public class FDDCanvasZoomFitTest {

    private Program program;
    private FDDCanvasFX canvas;

    @BeforeAll
    static void initJfx() throws Exception {
        FxTestUtil.ensureStarted();
    }

    @BeforeEach
    void setUp() {
        // Create test program with multiple projects
        program = new Program();
        program.setName("Test Program");
        
        // Add multiple projects to test layout calculations
        for (int i = 0; i < 12; i++) {
            Project p = new Project();
            p.setName("Project " + (i + 1));
            program.getProject().add(p);
        }
        
        canvas = new FDDCanvasFX(program, Font.font(12));
    }

    @Test
    void zoomLevelConstraintsAreEnforced() throws Exception {
        FxTestUtil.runOnFxAndWait(3, () -> {
            // Test minimum zoom constraint
            canvas.setZoom(0.05); // Below MIN_ZOOM (0.1)
            assertEquals(0.1, canvas.getZoom(), 0.001, "Zoom should be clamped to minimum");
            
            // Test maximum zoom constraint  
            canvas.setZoom(10.0); // Above MAX_ZOOM (5.0)
            assertEquals(5.0, canvas.getZoom(), 0.001, "Zoom should be clamped to maximum");
            
            // Test valid zoom values
            canvas.setZoom(1.5);
            assertEquals(1.5, canvas.getZoom(), 0.001, "Valid zoom should be set exactly");
        });
    }

    @Test
    void zoomInOutFunctionsWorkCorrectly() throws Exception {
        FxTestUtil.runOnFxAndWait(3, () -> {
            // Start at 100%
            canvas.setZoom(1.0);
            
            // Test zoom in
            double initialZoom = canvas.getZoom();
            canvas.zoomIn();
            assertTrue(canvas.getZoom() > initialZoom, "Zoom in should increase zoom level");
            
            // Test zoom out
            double zoomedInLevel = canvas.getZoom();
            canvas.zoomOut();
            assertTrue(canvas.getZoom() < zoomedInLevel, "Zoom out should decrease zoom level");
            
            // Test reset zoom
            canvas.setZoom(2.5);
            canvas.resetZoom();
            assertEquals(1.0, canvas.getZoom(), 0.001, "Reset zoom should return to 100%");
        });
    }

    @Test
    void scrollBehaviorAdaptsToZoomLevel() throws Exception {
        FxTestUtil.runOnFxAndWait(3, () -> {
            try {
                // Access private scroll pane for testing
                Field scrollPaneField = FDDCanvasFX.class.getDeclaredField("scrollPane");
                scrollPaneField.setAccessible(true);
                ScrollPane scrollPane = (ScrollPane) scrollPaneField.get(canvas);
                
                Method updateScrollBehavior = FDDCanvasFX.class.getDeclaredMethod("updateScrollBehavior");
                updateScrollBehavior.setAccessible(true);
                
                // Test 100% zoom behavior (vertical only)
                canvas.setZoom(1.0);
                updateScrollBehavior.invoke(canvas);
                assertEquals(ScrollPane.ScrollBarPolicy.NEVER, scrollPane.getHbarPolicy(), 
                    "Horizontal scrollbar should be disabled at 100% zoom");
                assertEquals(ScrollPane.ScrollBarPolicy.AS_NEEDED, scrollPane.getVbarPolicy(),
                    "Vertical scrollbar should be available at 100% zoom");
                
                // Test zoomed behavior (2D scrolling)
                canvas.setZoom(2.0);
                updateScrollBehavior.invoke(canvas);
                assertEquals(ScrollPane.ScrollBarPolicy.AS_NEEDED, scrollPane.getHbarPolicy(),
                    "Horizontal scrollbar should be available when zoomed");
                assertEquals(ScrollPane.ScrollBarPolicy.AS_NEEDED, scrollPane.getVbarPolicy(),
                    "Vertical scrollbar should be available when zoomed");
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to test scroll behavior", e);
            }
        });
    }

    @Test
    void fitToWindowEliminatesScrolling() throws Exception {
        FxTestUtil.runOnFxAndWait(5, () -> {
            try {
                // Access private fields for testing
                Field scrollPaneField = FDDCanvasFX.class.getDeclaredField("scrollPane");
                scrollPaneField.setAccessible(true);
                ScrollPane scrollPane = (ScrollPane) scrollPaneField.get(canvas);
                
                Field autoFitActiveField = FDDCanvasFX.class.getDeclaredField("autoFitActive");
                autoFitActiveField.setAccessible(true);
                
                // Simulate viewport bounds (mock a reasonable size)
                scrollPane.setPrefSize(800, 600);
                
                // Call fit to window
                canvas.fitToWindow();
                
                // Verify auto-fit is active
                boolean autoFitActive = (Boolean) autoFitActiveField.get(canvas);
                assertTrue(autoFitActive, "Auto-fit should be active after fitToWindow()");
                
                // Verify scroll policies are disabled in fit mode
                assertEquals(ScrollPane.ScrollBarPolicy.NEVER, scrollPane.getHbarPolicy(),
                    "Horizontal scrollbar should be disabled in fit mode");
                assertEquals(ScrollPane.ScrollBarPolicy.NEVER, scrollPane.getVbarPolicy(),
                    "Vertical scrollbar should be disabled in fit mode");
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to test fit functionality", e);
            }
        });
    }

    @Test
    void manualZoomDisablesAutoFit() throws Exception {
        FxTestUtil.runOnFxAndWait(3, () -> {
            try {
                Field autoFitActiveField = FDDCanvasFX.class.getDeclaredField("autoFitActive");
                autoFitActiveField.setAccessible(true);
                
                // Enable auto-fit
                canvas.fitToWindow();
                assertTrue((Boolean) autoFitActiveField.get(canvas), "Auto-fit should be active");
                
                // Manual zoom should disable auto-fit
                canvas.zoomIn();
                assertFalse((Boolean) autoFitActiveField.get(canvas), 
                    "Manual zoom in should disable auto-fit");
                
                canvas.fitToWindow(); // Re-enable
                canvas.zoomOut();
                assertFalse((Boolean) autoFitActiveField.get(canvas),
                    "Manual zoom out should disable auto-fit");
                
                canvas.fitToWindow(); // Re-enable  
                canvas.resetZoom();
                assertFalse((Boolean) autoFitActiveField.get(canvas),
                    "Manual reset zoom should disable auto-fit");
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to test auto-fit disable behavior", e);
            }
        });
    }

    @Test
    void fitCalculatesOptimalLayout() throws Exception {
        FxTestUtil.runOnFxAndWait(3, () -> {
            try {
                // Access elements in row field
                Field elementsInRowField = FDDCanvasFX.class.getDeclaredField("elementsInRow");
                elementsInRowField.setAccessible(true);
                
                // Test with different viewport sizes
                Field scrollPaneField = FDDCanvasFX.class.getDeclaredField("scrollPane");
                scrollPaneField.setAccessible(true);
                ScrollPane scrollPane = (ScrollPane) scrollPaneField.get(canvas);
                
                // Wide viewport should allow more elements per row
                scrollPane.setPrefSize(1200, 400);
                canvas.fitToWindow();
                int wideLayoutElements = (Integer) elementsInRowField.get(canvas);
                
                // Narrow viewport should use fewer elements per row
                scrollPane.setPrefSize(600, 600);
                canvas.fitToWindow();
                int narrowLayoutElements = (Integer) elementsInRowField.get(canvas);
                
                assertTrue(wideLayoutElements >= narrowLayoutElements,
                    "Wide viewport should allow same or more elements per row than narrow viewport");
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to test layout calculation", e);
            }
        });
    }

    @Test
    void drawingLogicAdaptsToFitMode() throws Exception {
        FxTestUtil.runOnFxAndWait(3, () -> {
            try {
                Field autoFitActiveField = FDDCanvasFX.class.getDeclaredField("autoFitActive");
                autoFitActiveField.setAccessible(true);
                
                Field canvasField = FDDCanvasFX.class.getDeclaredField("canvas");
                canvasField.setAccessible(true);
                Canvas canvasNode = (Canvas) canvasField.get(canvas);
                
                // Test normal mode vs fit mode canvas sizing
                canvas.setZoom(1.0);
                canvas.reflow(); // Normal mode
                double normalWidth = canvasNode.getWidth();
                
                canvas.fitToWindow(); // Fit mode
                double fitWidth = canvasNode.getWidth();
                
                // In fit mode, canvas should be sized differently (typically to viewport)
                // The exact relationship depends on the viewport mock, but they should differ
                assertNotEquals(normalWidth, fitWidth, 0.1,
                    "Canvas width should differ between normal and fit modes");
                
                assertTrue((Boolean) autoFitActiveField.get(canvas),
                    "Auto-fit should be active in fit mode");
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to test drawing adaptation", e);
            }
        });
    }
}
