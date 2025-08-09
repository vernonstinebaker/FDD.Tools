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

    public Logger getLogger(Class<?> type) { return LoggerFactory.getLogger(type); }

    public void withContext(Map<String,String> ctx, Runnable r) {
        if (ctx != null) ctx.forEach(MDC::put);
        try { r.run(); } finally { if (ctx != null) ctx.keySet().forEach(MDC::remove); }
    }

    public void info(Logger log, Supplier<String> msg, Map<String,String> ctx) { logWith(log::isInfoEnabled, log::info, msg, ctx); }
    public void debug(Logger log, Supplier<String> msg, Map<String,String> ctx) { logWith(log::isDebugEnabled, log::debug, msg, ctx); }
    public void warn(Logger log, Supplier<String> msg, Map<String,String> ctx) { logWith(log::isWarnEnabled, log::warn, msg, ctx); }
    public void error(Logger log, Supplier<String> msg, Map<String,String> ctx, Throwable t) { if (log.isErrorEnabled()) withContext(ctx, () -> log.error(msg.get(), t)); }

    private void logWith(Supplier<Boolean> enabled, java.util.function.Consumer<String> sink, Supplier<String> msg, Map<String,String> ctx) {
        if (enabled.get()) withContext(ctx, () -> sink.accept(msg.get()));
    }
}
