package net.sourceforge.fddtools.ui.fx;

import javafx.application.Platform;
import javafx.scene.text.Font;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Verifies horizontal centering math remains symmetric within a tolerance across widths. */
public class CenteredTextDrawerFXTest {
    @BeforeAll
    static void initJfx() throws Exception {
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        try { Platform.startup(latch::countDown); } catch (IllegalStateException already) { latch.countDown(); }
        latch.await();
    }

    @Test
    void centeredXWithinHalfPixelTolerance() throws Exception {
        Font font = Font.font("System", 14);
        String text = "Feature Name";
        double[] widths = {80, 123, 200, 257.5};
        for (double w : widths) {
            double x = 10;
            double centered = CenteredTextDrawerFX.computeCenteredX(text, font, x, w);
            double textWidth = measure(text, font);
            double leftPad = centered - x;
            double rightPad = (x + w) - (centered + textWidth);
            double diff = Math.abs(leftPad - rightPad);
            assertTrue(diff <= 0.75, "Padding imbalance > 0.75px for width=" + w + " diff=" + diff);
        }
    }

    private double measure(String s, Font f) {
        javafx.scene.text.Text t = new javafx.scene.text.Text(s);
        t.setFont(f);
        return t.getBoundsInLocal().getWidth();
    }
}
