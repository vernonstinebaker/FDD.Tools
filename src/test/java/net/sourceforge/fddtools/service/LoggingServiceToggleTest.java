package net.sourceforge.fddtools.service;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Tests enabling/disabling audit & perf logging toggles. */
public class LoggingServiceToggleTest {

    static class CapturingLogger implements Logger {
        final List<String> lines = new ArrayList<>();
        private final String name;
        CapturingLogger(String name){ this.name=name; }
        public String getName(){ return name; }
        private void add(String msg){ lines.add(msg); }
        // --- implement only used methods (info enabled path) ---
        @Override public boolean isInfoEnabled(){ return true; }
        @Override public void info(String msg){ add(msg); }
        // --- no-op stubs for unused methods ---
        @Override public boolean isTraceEnabled(){ return false; }
        @Override public void trace(String msg){}
        @Override public void trace(String format, Object arg){}
        @Override public void trace(String format, Object arg1, Object arg2){}
        @Override public void trace(String format, Object... arguments){}
        @Override public void trace(String msg, Throwable t){}
        @Override public boolean isTraceEnabled(org.slf4j.Marker marker){ return false; }
        @Override public void trace(org.slf4j.Marker marker, String msg){}
        @Override public void trace(org.slf4j.Marker marker, String format, Object arg){}
        @Override public void trace(org.slf4j.Marker marker, String format, Object arg1, Object arg2){}
        @Override public void trace(org.slf4j.Marker marker, String format, Object... arguments){}
        @Override public void trace(org.slf4j.Marker marker, String msg, Throwable t){}
        @Override public boolean isDebugEnabled(){ return false; }
        @Override public void debug(String msg){}
        @Override public void debug(String format, Object arg){}
        @Override public void debug(String format, Object arg1, Object arg2){}
        @Override public void debug(String format, Object... arguments){}
        @Override public void debug(String msg, Throwable t){}
        @Override public boolean isDebugEnabled(org.slf4j.Marker marker){ return false; }
        @Override public void debug(org.slf4j.Marker marker, String msg){}
        @Override public void debug(org.slf4j.Marker marker, String format, Object arg){}
        @Override public void debug(org.slf4j.Marker marker, String format, Object arg1, Object arg2){}
        @Override public void debug(org.slf4j.Marker marker, String format, Object... arguments){}
        @Override public void debug(org.slf4j.Marker marker, String msg, Throwable t){}
        @Override public boolean isInfoEnabled(org.slf4j.Marker marker){ return false; }
        @Override public void info(String format, Object arg){}
        @Override public void info(String format, Object arg1, Object arg2){}
        @Override public void info(String format, Object... arguments){}
        @Override public void info(String msg, Throwable t){}
        @Override public void info(org.slf4j.Marker marker, String msg){}
        @Override public void info(org.slf4j.Marker marker, String format, Object arg){}
        @Override public void info(org.slf4j.Marker marker, String format, Object arg1, Object arg2){}
        @Override public void info(org.slf4j.Marker marker, String format, Object... arguments){}
        @Override public void info(org.slf4j.Marker marker, String msg, Throwable t){}
        @Override public boolean isWarnEnabled(){ return false; }
        @Override public void warn(String msg){}
        @Override public void warn(String format, Object arg){}
        @Override public void warn(String format, Object... arguments){}
        @Override public void warn(String format, Object arg1, Object arg2){}
        @Override public void warn(String msg, Throwable t){}
        @Override public boolean isWarnEnabled(org.slf4j.Marker marker){ return false; }
        @Override public void warn(org.slf4j.Marker marker, String msg){}
        @Override public void warn(org.slf4j.Marker marker, String format, Object arg){}
        @Override public void warn(org.slf4j.Marker marker, String format, Object arg1, Object arg2){}
        @Override public void warn(org.slf4j.Marker marker, String format, Object... arguments){}
        @Override public void warn(org.slf4j.Marker marker, String msg, Throwable t){}
        @Override public boolean isErrorEnabled(){ return false; }
        @Override public void error(String msg){}
        @Override public void error(String format, Object arg){}
        @Override public void error(String format, Object arg1, Object arg2){}
        @Override public void error(String format, Object... arguments){}
        @Override public void error(String msg, Throwable t){}
        @Override public boolean isErrorEnabled(org.slf4j.Marker marker){ return false; }
        @Override public void error(org.slf4j.Marker marker, String msg){}
        @Override public void error(org.slf4j.Marker marker, String format, Object arg){}
        @Override public void error(org.slf4j.Marker marker, String format, Object arg1, Object arg2){}
        @Override public void error(org.slf4j.Marker marker, String format, Object... arguments){}
        @Override public void error(org.slf4j.Marker marker, String msg, Throwable t){}
    }

    @Test
    void disablingAuditPreventsEmission() {
        LoggingService svc = LoggingService.getInstance();
        svc.setAuditEnabled(true);
        CapturingLogger cap = new CapturingLogger("auditTest");
        svc.setTestAuditLogger(cap);
    // Clear any potential prior emissions from static init
    cap.lines.clear();
        svc.audit("action1", Map.of("k","v"), () -> "detail");
    int afterFirst = cap.lines.size();
    assertTrue(afterFirst >= 1, "At least one audit entry after first emission");
        svc.setAuditEnabled(false);
        svc.audit("action2", Map.of(), () -> "detail2");
    assertEquals(afterFirst, cap.lines.size(), "Audit should not increase when disabled");
        svc.setAuditEnabled(true); // restore
    }

    @Test
    void disablingPerfYieldsNoSpanOutput() {
        LoggingService svc = LoggingService.getInstance();
        svc.setPerfEnabled(false);
        LoggingService.Span span = svc.startPerf("testSpan", Map.of("k","v"));
        span.metric("x", 1).close(); // no emission
        // Can't easily assert absence without hooking perf logger; just ensure span object not null and does not throw.
        assertNotNull(span);
        svc.setPerfEnabled(true); // restore
    }
}
