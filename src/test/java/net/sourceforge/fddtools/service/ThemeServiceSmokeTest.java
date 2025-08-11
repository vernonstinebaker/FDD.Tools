package net.sourceforge.fddtools.service;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Basic smoke test that theme stylesheet switching works and semantic base is retained. */
public class ThemeServiceSmokeTest {
    private static boolean fxStarted = false;
    @BeforeAll
    static void initFx() throws Exception {
        if (!fxStarted) {
            CountDownLatch latch = new CountDownLatch(1);
            try { Platform.startup(latch::countDown); } catch (IllegalStateException already) { latch.countDown(); }
            latch.await();
            fxStarted = true;
        }
    }

    private Stage stage;

    @BeforeEach
    void setup() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            stage = new Stage();
            VBox root = new VBox(new Label("Hello"), new Button("Go"));
            stage.setScene(new Scene(root, 200, 100));
            stage.show();
            latch.countDown();
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @AfterEach
    void tearDown() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> { stage.close(); latch.countDown(); });
        latch.await(1, TimeUnit.SECONDS);
    }

    @Test
    void cycleThemesAddsCorrectVariantAndKeepsSemanticBase() throws Exception {
        cycleAndAssert(ThemeService.Theme.LIGHT, "global-theme-light.css");
        cycleAndAssert(ThemeService.Theme.DARK, "global-theme-dark.css");
        cycleAndAssert(ThemeService.Theme.HIGH_CONTRAST, "global-theme-highcontrast.css");
    }

    private void cycleAndAssert(ThemeService.Theme theme, String expectedCssFragment) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(() -> { ThemeService.getInstance().applyThemeTo(stage.getScene(), theme); latch.countDown(); });
    assertTrue(latch.await(2, TimeUnit.SECONDS));
        List<String> sheets = stage.getScene().getStylesheets();
        assertTrue(sheets.stream().anyMatch(s -> s.contains("semantic-theme.css")), "Semantic base should be present");
        assertTrue(sheets.stream().anyMatch(s -> s.contains(expectedCssFragment)), "Variant stylesheet should be present: "+expectedCssFragment);
        // Ensure only one variant at a time
        long variantCount = sheets.stream().filter(s -> s.contains("global-theme-" )).count();
        assertEquals(1, variantCount, "Only one variant stylesheet should be active");
    }
}
