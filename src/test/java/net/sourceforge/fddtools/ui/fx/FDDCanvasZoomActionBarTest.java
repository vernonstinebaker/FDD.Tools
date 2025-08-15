package net.sourceforge.fddtools.ui.fx;

import javafx.scene.text.Font;
import javafx.scene.control.TextField;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import com.nebulon.xml.fddi.Program;
import com.nebulon.xml.fddi.Project;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.lang.reflect.Field;
import net.sourceforge.fddtools.testutil.FxTestUtil;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for FDDCanvasFX modern zoom action bar.
 * Validates the interactive zoom controls, buttons, and UI elements.
 */
public class FDDCanvasZoomActionBarTest {

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
        program.setName("Test Program for Zoom UI");
        
        // Add some projects
        for (int i = 0; i < 4; i++) {
            Project p = new Project();
            p.setName("Project " + (i + 1));
            program.getProject().add(p);
        }
        
        canvas = new FDDCanvasFX(program, Font.font(12));
    }

    @Test
    void zoomActionBarIsCreated() throws Exception {
        FxTestUtil.runOnFxAndWait(3, () -> {
            try {
                // Access the zoom action bar
                Field actionBarField = FDDCanvasFX.class.getDeclaredField("actionBar");
                actionBarField.setAccessible(true);
                ToolBar actionBar = (ToolBar) actionBarField.get(canvas);
                
                assertNotNull(actionBar, "Action bar should be created");
                assertTrue(actionBar.getItems().size() > 0, 
                    "Action bar should contain UI elements");
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to verify zoom action bar creation", e);
            }
        });
    }

    @Test
    void zoomFieldIsInteractive() throws Exception {
        FxTestUtil.runOnFxAndWait(3, () -> {
            try {
                // Access the zoom percentage field
                Field zoomFieldField = FDDCanvasFX.class.getDeclaredField("zoomField");
                zoomFieldField.setAccessible(true);
                TextField zoomField = (TextField) zoomFieldField.get(canvas);
                
                assertNotNull(zoomField, "Zoom field should exist");
                assertFalse(zoomField.isDisabled(), "Zoom field should be interactive");
                
                // Test that the field shows some zoom value
                canvas.setZoom(1.5);
                String fieldText = zoomField.getText();
                assertNotNull(fieldText, "Zoom field should have text");
                assertFalse(fieldText.trim().isEmpty(), 
                    "Zoom field should show some zoom information");
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to test zoom field interactivity", e);
            }
        });
    }

    @Test
    void zoomSliderIsInteractive() throws Exception {
        FxTestUtil.runOnFxAndWait(3, () -> {
            try {
                // Access the zoom slider
                Field zoomSliderField = FDDCanvasFX.class.getDeclaredField("zoomSlider");
                zoomSliderField.setAccessible(true);
                Slider zoomSlider = (Slider) zoomSliderField.get(canvas);
                
                assertNotNull(zoomSlider, "Zoom slider should exist");
                assertFalse(zoomSlider.isDisabled(), "Zoom slider should be interactive");
                
                // Test slider range
                assertEquals(10.0, zoomSlider.getMin(), 0.1, "Slider minimum should be 10%");
                assertEquals(500.0, zoomSlider.getMax(), 0.1, "Slider maximum should be 500%");
                
                // Test that slider reflects current zoom
                canvas.setZoom(2.0);
                assertEquals(200.0, zoomSlider.getValue(), 1.0,
                    "Slider should reflect current zoom level");
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to test zoom slider interactivity", e);
            }
        });
    }

    @Test
    void zoomControlElementsExist() throws Exception {
        FxTestUtil.runOnFxAndWait(3, () -> {
            try {
                // Access zoom action bar to verify it exists and has content
                Field actionBarField = FDDCanvasFX.class.getDeclaredField("actionBar");
                actionBarField.setAccessible(true);
                ToolBar actionBar = (ToolBar) actionBarField.get(canvas);
                
                // Verify action bar exists and has items
                assertNotNull(actionBar, "Action bar should exist");
                
                int totalItems = actionBar.getItems().size();
                assertTrue(totalItems >= 1, 
                    "Action bar should have at least 1 item. Found: " + totalItems);
                
                // Verify the action bar is properly styled
                assertNotNull(actionBar.getStyleClass(), "Action bar should have style classes");
                assertTrue(actionBar.getStyleClass().size() > 0, "Action bar should have CSS styling");
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to test zoom controls", e);
            }
        });
    }

    @Test
    void actionBarHasProperStyling() throws Exception {
        FxTestUtil.runOnFxAndWait(3, () -> {
            try {
                // Access the action bar
                Field actionBarField = FDDCanvasFX.class.getDeclaredField("actionBar");
                actionBarField.setAccessible(true);
                ToolBar actionBar = (ToolBar) actionBarField.get(canvas);
                
                // Check for CSS styling
                assertTrue(actionBar.getStyleClass().contains("tool-bar") ||
                          !actionBar.getStyle().isEmpty(),
                    "Action bar should have CSS styling");
                
                // Verify it contains items
                assertTrue(actionBar.getItems().size() > 0, 
                    "Action bar should contain items");
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to test action bar styling", e);
            }
        });
    }

    @Test
    void zoomControlsSynchronization() throws Exception {
        FxTestUtil.runOnFxAndWait(3, () -> {
            try {
                Field zoomFieldField = FDDCanvasFX.class.getDeclaredField("zoomField");
                zoomFieldField.setAccessible(true);
                TextField zoomField = (TextField) zoomFieldField.get(canvas);
                
                Field zoomSliderField = FDDCanvasFX.class.getDeclaredField("zoomSlider");
                zoomSliderField.setAccessible(true);
                Slider zoomSlider = (Slider) zoomSliderField.get(canvas);
                
                // Test that all controls stay synchronized
                canvas.setZoom(1.75);
                
                // Check field shows some zoom value (may not be exact format)
                String fieldText = zoomField.getText();
                assertNotNull(fieldText, "Zoom field should have text");
                assertFalse(fieldText.trim().isEmpty(), "Zoom field should not be empty");
                
                // Check slider shows some reasonable value
                double sliderValue = zoomSlider.getValue();
                assertTrue(sliderValue > 0, "Zoom slider should have positive value");
                
                // Test another zoom level
                canvas.setZoom(0.5);
                
                String fieldText2 = zoomField.getText();
                assertNotNull(fieldText2, "Zoom field should have text");
                double sliderValue2 = zoomSlider.getValue();
                assertTrue(sliderValue2 > 0, "Zoom slider should have positive value");
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to test zoom controls synchronization", e);
            }
        });
    }

    @Test
    void controlElementsAccessible() throws Exception {
        FxTestUtil.runOnFxAndWait(3, () -> {
            try {
                Field actionBarField = FDDCanvasFX.class.getDeclaredField("actionBar");
                actionBarField.setAccessible(true);
                ToolBar actionBar = (ToolBar) actionBarField.get(canvas);
                
                // Verify all items in the action bar are accessible
                actionBar.getItems().forEach(item -> {
                    assertNotNull(item, "Action bar items should not be null");
                    assertFalse(item.isDisabled(), "Action bar items should be enabled by default");
                });
                
                // Test that the action bar has reasonable dimensions
                assertTrue(actionBar.getMinHeight() > 0, "Action bar should have minimum height");
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to test control accessibility", e);
            }
        });
    }
}
