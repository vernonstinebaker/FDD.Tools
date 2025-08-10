package net.sourceforge.fddtools.ui.fx;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for drop type classification thresholds in FDDTreeDragAndDropController.
 */
public class FDDTreeDragAndDropControllerTest {

    @Test
    void deriveDropTypeBeforeMiddleAfter() {
        double h = 40; // arbitrary cell height
        assertEquals(FDDTreeDragAndDropController.DropType.BEFORE, FDDTreeDragAndDropController.deriveDropType(0, h));
        assertEquals(FDDTreeDragAndDropController.DropType.BEFORE, FDDTreeDragAndDropController.deriveDropType(h * FDDTreeDragAndDropController.BEFORE_THRESHOLD - 0.01, h));
        assertEquals(FDDTreeDragAndDropController.DropType.INTO, FDDTreeDragAndDropController.deriveDropType(h * FDDTreeDragAndDropController.BEFORE_THRESHOLD, h));
        assertEquals(FDDTreeDragAndDropController.DropType.INTO, FDDTreeDragAndDropController.deriveDropType(h * 0.5, h));
        assertEquals(FDDTreeDragAndDropController.DropType.INTO, FDDTreeDragAndDropController.deriveDropType(h * FDDTreeDragAndDropController.AFTER_THRESHOLD, h));
        assertEquals(FDDTreeDragAndDropController.DropType.AFTER, FDDTreeDragAndDropController.deriveDropType(h * FDDTreeDragAndDropController.AFTER_THRESHOLD + 0.01, h));
        assertEquals(FDDTreeDragAndDropController.DropType.AFTER, FDDTreeDragAndDropController.deriveDropType(h-1, h));
    }

    @Test
    void deriveDropTypeFallbackHeight() {
        // zero/negative height should fallback to 24 logical pixels
        assertEquals(FDDTreeDragAndDropController.DropType.BEFORE, FDDTreeDragAndDropController.deriveDropType(0, 0));
        // pick a y clearly in middle band relative to fallback
        assertEquals(FDDTreeDragAndDropController.DropType.INTO, FDDTreeDragAndDropController.deriveDropType(12, 0));
        assertEquals(FDDTreeDragAndDropController.DropType.AFTER, FDDTreeDragAndDropController.deriveDropType(23, 0));
    }
}
