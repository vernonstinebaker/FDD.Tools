package net.sourceforge.fddtools.service;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for LoggingService MDC context application & cleanup. */
public class LoggingServiceTest {

    @Test
    void withContextPushesAndCleansKeys() {
        LoggingService svc = LoggingService.getInstance();
        Map<String,String> ctx = new HashMap<>();
        ctx.put("action", "testAction");
        ctx.put("projectPath", "/tmp/demo");
        assertNull(MDC.get("action"));
        final StringBuilder inside = new StringBuilder();
        svc.withContext(ctx, () -> inside.append(MDC.get("action")).append('|').append(MDC.get("projectPath")));
        assertEquals("testAction|/tmp/demo", inside.toString());
        assertNull(MDC.get("action"), "MDC key should be cleared after context");
        assertNull(MDC.get("projectPath"));
    }
}
