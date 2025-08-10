package net.sourceforge.fddtools.command;

import com.nebulon.xml.fddi.ObjectFactory;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.state.ModelEventBus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;

public class AddDeleteEventEmissionTest {
    private final ObjectFactory of = new ObjectFactory();

    @Test
    void addAndDeletePublishTreeStructureChanged() {
        FDDINode program = (FDDINode) of.createProgram();
        FDDINode project = (FDDINode) of.createProject();
        AtomicInteger count = new AtomicInteger();
        try (AutoCloseable sub = ModelEventBus.get().subscribe(ev -> { if(ev.type== ModelEventBus.EventType.TREE_STRUCTURE_CHANGED) count.incrementAndGet(); })) {
            AddChildCommand add = new AddChildCommand(program, project);
            add.execute();
            DeleteNodeCommand del = new DeleteNodeCommand(project);
            del.execute();
            assertTrue(count.get() >= 2, "Expected at least 2 TREE_STRUCTURE_CHANGED events");
        } catch (Exception e) { fail(e); }
    }
}
