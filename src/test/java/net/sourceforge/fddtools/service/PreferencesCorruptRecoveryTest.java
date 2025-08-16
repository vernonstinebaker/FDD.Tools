package net.sourceforge.fddtools.service;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;

public class PreferencesCorruptRecoveryTest {
    @Test
    void handlesCorruptFileGracefully() throws IOException {
        PreferencesService prefs = PreferencesService.getInstance();
        var path = prefs.getStorePath();
        // Write junk
        Files.writeString(path, "NOT=PROPERTIES\n\u0000\u0001\u0002garbage");
        // Reload
        prefs.reload();
        // Should fall back to default recents limit without throwing
        int limit = prefs.getRecentFilesLimit();
        assertTrue(limit >= 1, "Should recover with positive recents limit");
    }
}
