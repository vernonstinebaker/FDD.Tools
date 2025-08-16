package net.sourceforge.fddtools.ui.fx;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies macOS integration helper sets expected system properties when running on macOS.
 * On non-mac platforms this test is skipped (so CI remains green cross-platform).
 */
public class MacOSIntegrationServiceTest {

    @Test
    void testEarlyMacPropertiesPresent() {
        Assumptions.assumeTrue(MacOSIntegrationService.isMac(), "mac-only test");
        MacOSIntegrationService.setEarlyMacProperties();
        assertEquals("FDD Tools", System.getProperty("apple.awt.application.name"));
        assertEquals("true", System.getProperty("apple.laf.useScreenMenuBar"));
    }
}
