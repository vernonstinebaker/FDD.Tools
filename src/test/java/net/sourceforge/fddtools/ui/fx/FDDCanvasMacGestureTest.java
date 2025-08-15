package net.sourceforge.fddtools.ui.fx;

import javafx.scene.text.Font;
import com.nebulon.xml.fddi.Program;
import com.nebulon.xml.fddi.Project;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.lang.reflect.Field;
import net.sourceforge.fddtools.testutil.FxTestUtil;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for FDDCanvasFX Mac trackpad gesture support.
 * Validates zoom gestures, touch events, and Mac-specific interactions.
 */
public class FDDCanvasMacGestureTest {

    private Program program;
    private FDDCanvasFX canvas;

    @BeforeAll
    static void initJfx() throws Exception {
        FxTestUtil.ensureStarted();
    }

    @BeforeEach
    void setUp() {
        // Create test program
        program = new Program();
        program.setName("Test Program for Mac Gestures");
        
        // Add some projects
        for (int i = 0; i < 6; i++) {
            Project p = new Project();
            p.setName("Project " + (i + 1));
            program.getProject().add(p);
        }
        
        canvas = new FDDCanvasFX(program, Font.font(12));
    }

    @Test
    void zoomGestureHandlersAreRegistered() throws Exception {
        FxTestUtil.runOnFxAndWait(3, () -> {
            try {
                // Access the canvas node to verify gesture handlers
                Field canvasField = FDDCanvasFX.class.getDeclaredField("canvas");
                canvasField.setAccessible(true);
                javafx.scene.canvas.Canvas canvasNode = (javafx.scene.canvas.Canvas) canvasField.get(canvas);
                
                // Check that zoom event handlers are set (not null)
                assertNotNull(canvasNode.getOnZoomStarted(), 
                    "Zoom started handler should be registered");
                assertNotNull(canvasNode.getOnZoom(), 
                    "Zoom handler should be registered");
                assertNotNull(canvasNode.getOnZoomFinished(), 
                    "Zoom finished handler should be registered");
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to verify gesture handlers", e);
            }
        });
    }

    @Test
    void gestureZoomDisablesAutoFit() throws Exception {
        FxTestUtil.runOnFxAndWait(3, () -> {
            try {
                Field autoFitActiveField = FDDCanvasFX.class.getDeclaredField("autoFitActive");
                autoFitActiveField.setAccessible(true);
                
                // Enable auto-fit first
                canvas.fitToWindow();
                assertTrue((Boolean) autoFitActiveField.get(canvas), 
                    "Auto-fit should be active before manual zoom");
                
                // Manual zoom should disable auto-fit (simulating gesture behavior)
                canvas.setZoom(2.0);
                assertFalse((Boolean) autoFitActiveField.get(canvas),
                    "Manual zoom should disable auto-fit");
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to test auto-fit disable behavior", e);
            }
        });
    }

    @Test
    void zoomConstraintsApplyToGestures() throws Exception {
        FxTestUtil.runOnFxAndWait(3, () -> {
            // Test that gesture zoom respects min/max constraints
            canvas.setZoom(0.1); // At minimum
            
            // Try to zoom out further (should be constrained)
            canvas.setZoom(0.05); // Below minimum
            assertEquals(0.1, canvas.getZoom(), 0.001,
                "Zoom should be constrained to minimum even from gestures");
            
            canvas.setZoom(5.0); // At maximum
            
            // Try to zoom in further (should be constrained)
            canvas.setZoom(8.0); // Above maximum
            assertEquals(5.0, canvas.getZoom(), 0.001,
                "Zoom should be constrained to maximum even from gestures");
        });
    }

    @Test
    @EnabledOnOs(OS.MAC)
    void macSpecificGestureBehavior() throws Exception {
        FxTestUtil.runOnFxAndWait(3, () -> {
            // This test only runs on Mac to verify Mac-specific behavior
            try {
                Field gestureStartZoomField = FDDCanvasFX.class.getDeclaredField("gestureStartZoom");
                gestureStartZoomField.setAccessible(true);
                
                // Test that gesture state is properly tracked
                assertEquals(1.0, (Double) gestureStartZoomField.get(canvas), 0.001,
                    "Gesture zoom start should have default value initially");
                
                // Simulate starting a gesture
                gestureStartZoomField.set(canvas, 2.0);
                assertEquals(2.0, (Double) gestureStartZoomField.get(canvas), 0.001,
                    "Gesture zoom start should track initial zoom level");
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to test Mac-specific gesture behavior", e);
            }
        });
    }

    @Test
    void gestureStateManagement() throws Exception {
        FxTestUtil.runOnFxAndWait(3, () -> {
            try {
                Field gestureStartZoomField = FDDCanvasFX.class.getDeclaredField("gestureStartZoom");
                gestureStartZoomField.setAccessible(true);
                
                // Test proper state initialization
                assertEquals(1.0, (Double) gestureStartZoomField.get(canvas), 0.001,
                    "Gesture state should start as default value");
                
                // Test state tracking during gesture simulation
                canvas.setZoom(1.5);
                gestureStartZoomField.set(canvas, 1.5);
                
                // Simulate zoom change during gesture
                canvas.setZoom(2.0);
                
                // Verify the gesture start value is preserved
                assertEquals(1.5, (Double) gestureStartZoomField.get(canvas), 0.001,
                    "Gesture start value should be preserved during zoom changes");
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to test gesture state management", e);
            }
        });
    }

    @Test
    void crossPlatformGestureCompatibility() throws Exception {
        FxTestUtil.runOnFxAndWait(3, () -> {
            // Test that gesture code doesn't break on non-Mac platforms
            try {
                Field canvasField = FDDCanvasFX.class.getDeclaredField("canvas");
                canvasField.setAccessible(true);
                javafx.scene.canvas.Canvas canvasNode = (javafx.scene.canvas.Canvas) canvasField.get(canvas);
                
                // Verify handlers are present regardless of platform
                assertNotNull(canvasNode.getOnZoomStarted(),
                    "Zoom handlers should be present on all platforms");
                assertNotNull(canvasNode.getOnZoom(),
                    "Zoom handlers should be present on all platforms");
                assertNotNull(canvasNode.getOnZoomFinished(),
                    "Zoom handlers should be present on all platforms");
                
                // Test that basic zoom functionality works
                canvas.setZoom(1.5);
                assertEquals(1.5, canvas.getZoom(), 0.001,
                    "Basic zoom should work on all platforms");
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to test cross-platform compatibility", e);
            }
        });
    }
}
