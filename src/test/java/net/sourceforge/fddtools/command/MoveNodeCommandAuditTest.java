package net.sourceforge.fddtools.command;

import com.nebulon.xml.fddi.ObjectFactory;
import com.nebulon.xml.fddi.Program;
import com.nebulon.xml.fddi.Project;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.service.LoggingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Verifies audit emission for nodeMove event. */
public class MoveNodeCommandAuditTest {
    private final ObjectFactory of = new ObjectFactory();
    private final List<String> messages = new ArrayList<>();

    private static class TestLogger implements Logger {
        private final List<String> sink;
        TestLogger(List<String> sink){ this.sink=sink; }
        // Implement minimal methods used (info/isInfoEnabled); others no-op
        @Override public String getName(){ return "test"; }
        @Override public boolean isInfoEnabled(){ return true; }
        @Override public void info(String msg){ sink.add(msg); }
        @Override public void info(String msg, Throwable t){ sink.add(msg + " | ex=" + t.getClass().getSimpleName()); }
        @Override public void info(String format, Object arg){ sink.add(format+"|"+String.valueOf(arg)); }
        @Override public void info(String format, Object arg1, Object arg2){ sink.add(format+"|"+arg1+","+arg2); }
        @Override public void info(String format, Object... arguments){ sink.add(format+"|args="+java.util.Arrays.toString(arguments)); }
        @Override public void info(org.slf4j.Marker marker, String format, Object arg){ }
        @Override public void info(org.slf4j.Marker marker, String format, Object arg1, Object arg2){ }
        @Override public void info(org.slf4j.Marker marker, String format, Object... arguments){ }
        @Override public void info(org.slf4j.Marker marker, String msg, Throwable t){ }
        // --- Unused methods below (empty implementations) ---
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
        @Override public void info(org.slf4j.Marker marker, String msg){}
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

    @BeforeEach
    void init() {
        messages.clear();
        LoggingService.getInstance().setTestAuditLogger(new TestLogger(messages));
        CommandExecutionService.getInstance().getStack().clear();
    }

    private Program program(String n){ Program p=of.createProgram(); p.setName(n); return p; }
    private Project project(String n){ Project p=of.createProject(); p.setName(n); return p; }

    @Test
    void auditEmittedForMove() {
        Program a=program("A"); Program b=program("B"); Project proj=project("P");
        a.add((FDDINode) proj);
        CommandExecutionService.getInstance().execute(new MoveNodeCommand((FDDINode)proj,(FDDINode)b));
        assertTrue(messages.stream().anyMatch(m-> m.contains("commandExecute")), "commandExecute audit should appear");
        assertTrue(messages.stream().anyMatch(m-> m.contains("nodeMove")), "nodeMove audit should appear");
    }
}
