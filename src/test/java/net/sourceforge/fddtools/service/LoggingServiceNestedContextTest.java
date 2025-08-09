package net.sourceforge.fddtools.service;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Verifies nested withContext restores outer values. */
public class LoggingServiceNestedContextTest {

    @Test
    void nestedContextsRestoreOuterValues() {
        LoggingService svc = LoggingService.getInstance();
        assertNull(MDC.get("action"));
        final StringBuilder trace = new StringBuilder();
        svc.withContext(Map.of("action","outer"), () -> {
            trace.append(MDC.get("action")).append("->");
            svc.withContext(Map.of("action","inner"), () -> trace.append(MDC.get("action")).append("->"));
            trace.append(MDC.get("action")).append("->end");
            assertEquals("outer", MDC.get("action"));
        });
        assertNull(MDC.get("action"));
        assertEquals("outer->inner->outer->end", trace.toString());
    }
}
