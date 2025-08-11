package net.sourceforge.fddtools.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Central logging helper. Provides:
 * - Consistent logger acquisition
 * - Simple contextual logging with MDC (projectPath, selectedNode, action)
 * - Lambda suppliers to defer expensive message construction
 */
public final class LoggingService {
    private static final LoggingService INSTANCE = new LoggingService();
    public static LoggingService getInstance() { return INSTANCE; }
    private LoggingService() {}

    private final Logger auditLogger = LoggerFactory.getLogger("audit");
    private final Logger perfLogger = LoggerFactory.getLogger("perf");
    private volatile boolean auditEnabled = true;
    private volatile boolean perfEnabled = true;

    /** Enable/disable audit logging at runtime (default true). */
    public void setAuditEnabled(boolean enabled){ this.auditEnabled = enabled; }
    public boolean isAuditEnabled(){ return auditEnabled; }
    /** Enable/disable performance span logging at runtime (default true). */
    public void setPerfEnabled(boolean enabled){ this.perfEnabled = enabled; }
    public boolean isPerfEnabled(){ return perfEnabled; }

    public Logger getLogger(Class<?> type) { return LoggerFactory.getLogger(type); }

    public void withContext(Map<String,String> ctx, Runnable r) {
        if (ctx == null || ctx.isEmpty()) { r.run(); return; }
        // Support nested contexts by remembering prior values and restoring afterwards
        java.util.Map<String,String> previous = new java.util.HashMap<>();
        ctx.forEach((k,v) -> {
            previous.put(k, MDC.get(k));
            MDC.put(k, v);
        });
        try { r.run(); } finally {
            ctx.forEach((k,v) -> {
                String prior = previous.get(k);
                if (prior == null) MDC.remove(k); else MDC.put(k, prior);
            });
        }
    }

    public void info(Logger log, Supplier<String> msg, Map<String,String> ctx) { logWith(log::isInfoEnabled, log::info, msg, ctx); }
    public void debug(Logger log, Supplier<String> msg, Map<String,String> ctx) { logWith(log::isDebugEnabled, log::debug, msg, ctx); }
    public void warn(Logger log, Supplier<String> msg, Map<String,String> ctx) { logWith(log::isWarnEnabled, log::warn, msg, ctx); }
    public void error(Logger log, Supplier<String> msg, Map<String,String> ctx, Throwable t) { if (log.isErrorEnabled()) withContext(ctx, () -> log.error(msg.get(), t)); }

    private void logWith(Supplier<Boolean> enabled, java.util.function.Consumer<String> sink, Supplier<String> msg, Map<String,String> ctx) {
        if (enabled.get()) withContext(ctx, () -> sink.accept(msg.get()));
    }

    // --- Audit helpers ---
    private Logger testAuditOverride;
    public void setTestAuditLogger(Logger logger){ this.testAuditOverride = logger; }
    public void audit(String action, Map<String,String> ctx, Supplier<String> detail) {
        if(!auditEnabled) return;
        if (testAuditOverride != null && testAuditOverride.isInfoEnabled()) {
            Map<String,String> merged = ctx==null? new java.util.HashMap<>() : new java.util.HashMap<>(ctx);
            merged.put("auditAction", action);
            withContext(merged, () -> testAuditOverride.info(buildMessage(action, detail==null?null:detail.get(), merged)));
            return;
        }
        if (!auditLogger.isInfoEnabled()) return;
        Map<String,String> merged = ctx==null? new java.util.HashMap<>() : new java.util.HashMap<>(ctx);
        merged.put("auditAction", action);
        withContext(merged, () -> auditLogger.info(buildMessage(action, detail==null?null:detail.get(), merged)));
    }

    // --- Performance span helpers ---
    public Span startPerf(String name, Map<String,String> ctx) { return perfEnabled ? new Span(name, ctx) : NO_OP_SPAN; }

    private final Span NO_OP_SPAN = new NoOpSpan();

    private final class NoOpSpan extends Span {
        NoOpSpan(){ super("noop", java.util.Collections.emptyMap()); }
        @Override public Span metric(String k, Object v){ return this; }
        @Override public void close() { /* no-op */ }
    }

    public class Span implements AutoCloseable {
        private final long start = System.nanoTime();
        private final String name;
        private final Map<String,String> ctx;
        private boolean closed;
        private final Map<String,Object> metrics = new java.util.HashMap<>();
        Span(String name, Map<String,String> ctx){ this.name=name; this.ctx = ctx==null?java.util.Collections.emptyMap():ctx; }
        public Span metric(String k, Object v){ if(v!=null) metrics.put(k,v); return this; }
        @Override public void close(){ if(closed) return; closed=true; long durMs=(System.nanoTime()-start)/1_000_000L; metrics.put("durationMs", durMs); emit(durMs); }
        private void emit(long durMs){ if(!perfLogger.isInfoEnabled()) return; Map<String,String> m=new java.util.HashMap<>(ctx); m.put("perfSpan", name); m.put("durationMs", String.valueOf(durMs)); withContext(m, () -> perfLogger.info(buildMessage(name, null, m) + formatMetrics(metrics))); }
    }

    private String buildMessage(String action, String detail, Map<String,?> ctx){ StringBuilder sb=new StringBuilder(); sb.append(action); if(detail!=null && !detail.isBlank()){ sb.append(" | ").append(detail); } return sb.toString(); }
    private String formatMetrics(Map<String,Object> metrics){ if(metrics==null||metrics.isEmpty()) return ""; StringBuilder sb=new StringBuilder(); sb.append(" | "); boolean first=true; for(var e: metrics.entrySet()){ if(!first) sb.append(' '); first=false; sb.append(e.getKey()).append('='); Object v=e.getValue(); sb.append(v); } return sb.toString(); }
}
