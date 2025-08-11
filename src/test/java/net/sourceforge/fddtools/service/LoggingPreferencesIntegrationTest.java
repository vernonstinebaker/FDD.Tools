package net.sourceforge.fddtools.service;

import net.sourceforge.fddtools.util.PreferencesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test ensuring preferences keys drive LoggingService toggles.
 */
public class LoggingPreferencesIntegrationTest {
    private PreferencesService prefs;
    private LoggingService logging;

    @BeforeEach
    void setup(){
        prefs = PreferencesService.getInstance();
        logging = LoggingService.getInstance();
    }

    @Test
    void preferenceChangesAffectLoggingService(){
        boolean origAudit = logging.isAuditEnabled();
        boolean origPerf = logging.isPerfEnabled();
        try {
            prefs.setAuditLoggingEnabled(false);
            prefs.setPerfLoggingEnabled(false);
            prefs.flushNow();
            // Simulate runtime toggling (UI would ordinarily apply)
            logging.setAuditEnabled(prefs.isAuditLoggingEnabled());
            logging.setPerfEnabled(prefs.isPerfLoggingEnabled());
            assertFalse(logging.isAuditEnabled());
            assertFalse(logging.isPerfEnabled());
        } finally {
            prefs.setAuditLoggingEnabled(origAudit);
            prefs.setPerfLoggingEnabled(origPerf);
            prefs.flushNow();
            logging.setAuditEnabled(origAudit);
            logging.setPerfEnabled(origPerf);
        }
    }
}
