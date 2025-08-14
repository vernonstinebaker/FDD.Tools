package net.sourceforge.fddtools.ui.fx;

import javafx.scene.text.Font;
import com.nebulon.xml.fddi.Program;
import com.nebulon.xml.fddi.Project;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import net.sourceforge.fddtools.testutil.FxTestUtil;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Responsive layout test for FDDCanvasFX: verifies elementsInRow adapts as available width shrinks.
 */
public class FDDCanvasResponsiveLayoutTest {

    @BeforeAll
    static void initJfx() throws Exception {
    FxTestUtil.ensureStarted();
    }

    @Test
    void elementsWrapAsWidthShrinks() throws Exception {
        Program program = new Program();
        program.setName("Program");
        // Add 7 project children
        for (int i=0;i<7;i++) {
            Project p = new Project();
            p.setName("P"+i);
            program.getProject().add(p); // JAXB list
        }
        // Program extends FDDINode via generated binding hierarchy; ensure progress calc
        FDDCanvasFX canvas = new FDDCanvasFX(program, Font.font(12));

        Method calc = FDDCanvasFX.class.getDeclaredMethod("calculateCanvasHeight", double.class);
        calc.setAccessible(true);

        try {
            FxTestUtil.runOnFxAndWait(5, () -> {
            try {
                // Large width: expect all in one row
                calc.invoke(canvas, 10_000d);
                assertEquals(7, canvas.getElementsInRowForTest(), "All should fit in one row");
                // Medium width
                calc.invoke(canvas, 600d);
                int medium = canvas.getElementsInRowForTest();
                assertTrue(medium < 7 && medium > 1, "Should partially wrap for medium width");
                // Narrow width
                calc.invoke(canvas, 170d);
                assertEquals(1, canvas.getElementsInRowForTest(), "Very narrow width -> single column");
            } catch (Exception e) {
                fail(e);
            }
            });
        } catch (Exception e) {
            fail(e);
        }
    }

    // No custom test node needed; using real Program/Project types
}
