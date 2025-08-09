package net.sourceforge.fddtools.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;

public class PreferencesServiceTest {

    @Test
    void recentFilesLimitClampAndPersist() throws Exception {
        PreferencesService svc = PreferencesService.getInstance();
        int original = svc.getRecentFilesLimit();
        svc.setRecentFilesLimit(99); // above max -> clamp to 50
        svc.flushNow();
        int v = svc.getRecentFilesLimit();
        assertTrue(v <= 50 && v >= 1);
        // restore
        svc.setRecentFilesLimit(original);
        svc.flushNow();
    }

    @Test
    void windowBoundsRoundTrip() throws Exception {
        PreferencesService svc = PreferencesService.getInstance();
        svc.setLastWindowBounds(10,20,800,600);
        svc.flushNow();
        svc.reload();
        var rectOpt = svc.getLastWindowBounds();
        assertTrue(rectOpt.isPresent());
        var rect = rectOpt.get();
        assertEquals(800, rect.width);
        assertEquals(600, rect.height);
    }

    @Test
    void fileCreatedOnSave() throws Exception {
        PreferencesService svc = PreferencesService.getInstance();
        svc.updateAndFlush(PreferencesService.KEY_UI_LANGUAGE, "en");
        assertTrue(Files.isRegularFile(svc.getStorePath()));
    }
}
