package net.sourceforge.fddtools.util;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class LayoutPreferencesServiceTest {

    @BeforeEach
    void clear() {
        // No direct clear API; set to defaults outside accepted range to simulate empty start not possible.
        // Tests will just operate on setter/getter contract.
    }

    @Test
    void storesAndRetrievesValidPositions() {
        LayoutPreferencesService svc = LayoutPreferencesService.getInstance();
        svc.setMainDividerPosition(0.30);
        svc.setRightDividerPosition(0.65);
        assertEquals(0.30, svc.getMainDividerPosition().orElseThrow(), 0.0001);
        assertEquals(0.65, svc.getRightDividerPosition().orElseThrow(), 0.0001);
    }

    @Test
    void rejectsOutOfRangeValues() {
        LayoutPreferencesService svc = LayoutPreferencesService.getInstance();
    var beforeMain = svc.getMainDividerPosition();
    svc.setMainDividerPosition(-1); // ignored
    assertEquals(beforeMain, svc.getMainDividerPosition(), "Out-of-range main should not change value");
    var beforeRight = svc.getRightDividerPosition();
    svc.setRightDividerPosition(0.02); // ignored
    assertEquals(beforeRight, svc.getRightDividerPosition(), "Out-of-range right should not change value");
    }
}
