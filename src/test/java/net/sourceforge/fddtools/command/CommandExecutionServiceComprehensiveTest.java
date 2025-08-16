package net.sourceforge.fddtools.command;

import com.nebulon.xml.fddi.ObjectFactory;
import com.nebulon.xml.fddi.Program;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.state.ModelState;
import net.sourceforge.fddtools.state.ModelEventBus;
import net.sourceforge.fddtools.service.ProjectService;
import net.sourceforge.fddtools.service.LoggingService;
import org.junit.jupiter.api.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for CommandExecutionService covering singleton pattern,
 * command execution lifecycle, undo/redo functionality, ModelState integration,
 * audit logging, and thread safety.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CommandExecutionServiceComprehensiveTest {
    
    private static CommandExecutionService service;
    private ObjectFactory objectFactory;
    
    @BeforeAll
    static void initializeService() {
        service = CommandExecutionService.getInstance();
    }
    
    @BeforeEach
    void resetServiceState() {
        // Clear command stack to start fresh
        service.getStack().clear();
        
        // Reset ModelState to clean slate
        ModelState ms = ModelState.getInstance();
        ms.setDirty(false);
        ms.setUndoAvailable(false);
        ms.setRedoAvailable(false);
        ms.setNextUndoDescription("");
        ms.setNextRedoDescription("");
        ms.setSelectedNode(null);
        
        objectFactory = new ObjectFactory();
    }
    
    @Test
    @Order(1)
    void singletonPatternConsistency() {
        CommandExecutionService instance1 = CommandExecutionService.getInstance();
        CommandExecutionService instance2 = CommandExecutionService.getInstance();
        
        assertSame(instance1, instance2, "CommandExecutionService should return same instance");
        assertSame(service, instance1, "Service should maintain singleton consistency");
        assertNotNull(instance1, "Service instance should not be null");
        assertNotNull(instance1.getStack(), "Service should provide CommandStack access");
    }
    
    @Test
    @Order(2)
    void commandStackAccessAndInitialState() {
        CommandStack stack = service.getStack();
        
        assertNotNull(stack, "CommandStack should be accessible");
        assertFalse(stack.canUndo(), "Initial state should have no undo available");
        assertFalse(stack.canRedo(), "Initial state should have no redo available");
        assertNull(stack.peekUndoDescription(), "Initial state should have null undo description");
        assertNull(stack.peekRedoDescription(), "Initial state should have null redo description");
    }
    
    @Test
    @Order(3)
    void nullCommandHandling() {
        // Should handle null command gracefully without throwing
        assertDoesNotThrow(() -> service.execute(null), 
                "Should handle null command without throwing exception");
        
        // ModelState should remain unchanged after null command
        ModelState ms = ModelState.getInstance();
        assertFalse(ms.isDirty(), "Null command should not mark model dirty");
        assertFalse(ms.undoAvailableProperty().get(), "Null command should not enable undo");
        assertFalse(ms.redoAvailableProperty().get(), "Null command should not enable redo");
    }
    
    @Test
    @Order(4)
    void basicCommandExecutionLifecycle() {
        FDDINode root = createTestNode("Root");
        FDDINode child = createTestNode("Child");
        
        Command addCommand = new AddChildCommand(root, child);
        
        // Execute command
        service.execute(addCommand);
        
        // Verify command was executed
        assertEquals(1, root.getChildren().size(), "Command should be executed");
        assertEquals(child, root.getChildren().get(0), "Child should be added to root");
        
        // Verify ModelState updates
        ModelState ms = ModelState.getInstance();
        assertTrue(ms.isDirty(), "Model should be marked dirty after command execution");
        assertTrue(ms.undoAvailableProperty().get(), "Undo should be available after command execution");
        assertFalse(ms.redoAvailableProperty().get(), "Redo should not be available after initial execution");
        assertNotNull(ms.getNextUndoDescription(), "Undo description should be set");
        assertEquals("", ms.getNextRedoDescription(), "Redo description should be empty");
    }
    
    @Test
    @Order(5)
    void undoRedoLifecycleComplete() {
        FDDINode root = createTestNode("Root");
        FDDINode child1 = createTestNode("Child1");
        FDDINode child2 = createTestNode("Child2");
        
        // Execute two commands
        service.execute(new AddChildCommand(root, child1));
        service.execute(new AddChildCommand(root, child2));
        
        assertEquals(2, root.getChildren().size(), "Both children should be added");
        ModelState ms = ModelState.getInstance();
        assertTrue(ms.undoAvailableProperty().get(), "Undo should be available");
        assertNotNull(ms.getNextUndoDescription(), "Undo description should not be null");
        assertFalse(ms.getNextUndoDescription().isEmpty(), "Undo description should not be empty");
        
        // First undo
        service.undo();
        assertEquals(1, root.getChildren().size(), "First undo should remove child2");
        assertTrue(ms.undoAvailableProperty().get(), "Undo should still be available");
        assertTrue(ms.redoAvailableProperty().get(), "Redo should now be available");
        assertNotNull(ms.getNextRedoDescription(), "Redo description should not be null");
        assertFalse(ms.getNextRedoDescription().isEmpty(), "Redo description should not be empty");
        
        // Second undo
        service.undo();
        assertEquals(0, root.getChildren().size(), "Second undo should remove child1");
        assertFalse(ms.undoAvailableProperty().get(), "Undo should no longer be available");
        assertTrue(ms.redoAvailableProperty().get(), "Redo should still be available");
        assertNotNull(ms.getNextRedoDescription(), "Redo description should not be null");
        assertFalse(ms.getNextRedoDescription().isEmpty(), "Redo description should not be empty");
        
        // First redo
        service.redo();
        assertEquals(1, root.getChildren().size(), "First redo should restore child1");
        assertTrue(ms.undoAvailableProperty().get(), "Undo should be available again");
        assertTrue(ms.redoAvailableProperty().get(), "Redo should still be available");
        
        // Second redo
        service.redo();
        assertEquals(2, root.getChildren().size(), "Second redo should restore child2");
        assertTrue(ms.undoAvailableProperty().get(), "Undo should be available");
        assertFalse(ms.redoAvailableProperty().get(), "Redo should no longer be available");
    }
    
    @Test
    @Order(6)
    void undoWithoutAvailableCommands() {
        // Attempting undo when none available should be safe
        assertDoesNotThrow(() -> service.undo(), 
                "Undo without available commands should not throw");
        
        ModelState ms = ModelState.getInstance();
        assertFalse(ms.isDirty(), "Model should remain clean");
        assertFalse(ms.undoAvailableProperty().get(), "Undo should remain unavailable");
    }
    
    @Test
    @Order(7)
    void redoWithoutAvailableCommands() {
        // Attempting redo when none available should be safe
        assertDoesNotThrow(() -> service.redo(), 
                "Redo without available commands should not throw");
        
        ModelState ms = ModelState.getInstance();
        assertFalse(ms.isDirty(), "Model should remain clean");
        assertFalse(ms.redoAvailableProperty().get(), "Redo should remain unavailable");
    }
    
    @Test
    @Order(8)
    void modelStateIntegrationComprehensive() {
        FDDINode root = createTestNode("StateTest");
        FDDINode child = createTestNode("StateChild");
        
        ModelState ms = ModelState.getInstance();
        
        // Set initial selection
        ms.setSelectedNode(root);
        
        // Execute command
        Command command = new AddChildCommand(root, child);
        service.execute(command);
        
        // Verify all ModelState properties are correctly updated
        assertTrue(ms.isDirty(), "Dirty flag should be set");
        assertTrue(ms.undoAvailableProperty().get(), "Undo available should be true");
        assertFalse(ms.redoAvailableProperty().get(), "Redo available should be false");
        assertEquals(command.description(), ms.getNextUndoDescription(), "Undo description should match command");
        assertEquals("", ms.getNextRedoDescription(), "Redo description should be empty");
        
        // Undo and verify state changes
        service.undo();
        assertTrue(ms.isDirty(), "Should remain dirty after undo");
        assertFalse(ms.undoAvailableProperty().get(), "Undo should no longer be available");
        assertTrue(ms.redoAvailableProperty().get(), "Redo should now be available");
        assertEquals("", ms.getNextUndoDescription(), "Undo description should be empty");
        assertEquals(command.description(), ms.getNextRedoDescription(), "Redo description should match command");
    }
    
    @Test
    @Order(9)
    void modelEventBusIntegration() throws Exception {
        FDDINode root = createTestNode("EventTest");
        FDDINode child = createTestNode("EventChild");
        
        ModelState ms = ModelState.getInstance();
        ms.setSelectedNode(root); // Set selection for event publishing
        
        CountDownLatch eventLatch = new CountDownLatch(1);
        AtomicReference<FDDINode> eventNode = new AtomicReference<>();
        
        // Subscribe to events
        try (var subscription = ModelEventBus.get().subscribe((event) -> {
            if (event.type == ModelEventBus.EventType.NODE_UPDATED) {
                eventNode.set((FDDINode) event.payload);
                eventLatch.countDown();
            }
        })) {
            // Execute command which should trigger event
            service.execute(new AddChildCommand(root, child));
            
            // Wait for event
            assertTrue(eventLatch.await(2, TimeUnit.SECONDS), "Should receive NODE_UPDATED event");
            
            // The event payload should be the updated node (could be root or child depending on implementation)
            FDDINode actualNode = eventNode.get();
            assertNotNull(actualNode, "Event should contain a node");
            assertTrue(actualNode == root || actualNode == child, 
                "Event should contain either the parent or child node, got: " + actualNode.getName());
        }
    }
    
    @Test
    @Order(10)
    void auditLoggingIntegration() {
        FDDINode root = createTestNode("AuditTest");
        FDDINode child = createTestNode("AuditChild");
        
        // Enable audit logging for this test
        LoggingService loggingService = LoggingService.getInstance();
        boolean originalAuditState = loggingService.isAuditEnabled();
        
        try {
            loggingService.setAuditEnabled(true);
            
            Command command = new AddChildCommand(root, child);
            
            // Execute, undo, and redo to test all audit paths
            assertDoesNotThrow(() -> service.execute(command), 
                    "Command execution with audit should not throw");
            assertDoesNotThrow(() -> service.undo(), 
                    "Command undo with audit should not throw");
            assertDoesNotThrow(() -> service.redo(), 
                    "Command redo with audit should not throw");
            
        } finally {
            // Restore original audit state
            loggingService.setAuditEnabled(originalAuditState);
        }
    }
    
    @Test
    @Order(11)
    void moveNodeCommandSpecialHandling() {
        FDDINode root = createTestNode("MoveRoot");
        FDDINode source = createTestNode("Source");
        FDDINode target = createTestNode("Target");
        FDDINode child = createTestNode("MoveChild");
        
        // Set up hierarchy: root contains source and target, source contains child
        root.add(source);
        root.add(target);
        source.add(child);
        
        // Enable audit to test special MoveNodeCommand handling
        LoggingService loggingService = LoggingService.getInstance();
        boolean originalAuditState = loggingService.isAuditEnabled();
        
        try {
            loggingService.setAuditEnabled(true);
            
            // Execute MoveNodeCommand (special audit handling)
            MoveNodeCommand moveCommand = new MoveNodeCommand(child, target);
            assertDoesNotThrow(() -> service.execute(moveCommand), 
                    "MoveNodeCommand execution should not throw");
            
            // Verify the move occurred
            assertFalse(source.getChildren().contains(child), "Child should be removed from source");
            assertTrue(target.getChildren().contains(child), "Child should be added to target");
            
        } finally {
            loggingService.setAuditEnabled(originalAuditState);
        }
    }
    
    @Test
    @Order(12)
    void contextBuildingWithProjectAndSelection() {
        FDDINode testNode = createTestNode("ContextTest");
        
        // Set up project and selection context
        ProjectService projectService = ProjectService.getInstance();
        ModelState modelState = ModelState.getInstance();
        
        // Create a test project
        projectService.newProject("TestProject");
        modelState.setSelectedNode(testNode);
        
        try {
            Command command = new AddChildCommand(testNode, createTestNode("ContextChild"));
            
            // Execute command - context building should include project path and selected node
            assertDoesNotThrow(() -> service.execute(command), 
                    "Command execution with context should not throw");
            
            // Verify the command was executed successfully
            assertEquals(1, testNode.getChildren().size(), "Command should execute despite context building");
            
        } finally {
            // Clean up
            projectService.clear();
            modelState.setSelectedNode(null);
        }
    }
    
    @Test
    @Order(13)
    void threadSafetyForSingleton() throws Exception {
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicReference<Exception> exception = new AtomicReference<>();
        CommandExecutionService[] instances = new CommandExecutionService[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            new Thread(() -> {
                try {
                    instances[index] = CommandExecutionService.getInstance();
                    
                    // Also test concurrent command execution
                    FDDINode root = createTestNode("ThreadTest" + index);
                    FDDINode child = createTestNode("ThreadChild" + index);
                    instances[index].execute(new AddChildCommand(root, child));
                    
                } catch (Exception e) {
                    exception.set(e);
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Thread safety test timeout");
        assertNull(exception.get(), "Should not throw exception during concurrent access");
        
        // All instances should be the same
        for (CommandExecutionService instance : instances) {
            assertSame(service, instance, "All instances should be identical in concurrent access");
        }
    }
    
    @Test
    @Order(14)
    void commandStackCapacityAndTrimming() {
        FDDINode root = createTestNode("CapacityTest");
        
        // Execute many commands to test stack capacity (CommandStack default is 100)
        for (int i = 0; i < 105; i++) {
            FDDINode child = createTestNode("Child" + i);
            service.execute(new AddChildCommand(root, child));
        }
        
        // Should have all 105 children
        assertEquals(105, root.getChildren().size(), "All commands should be executed");
        
        // Undo should be available but limited by stack size
        assertTrue(service.getStack().canUndo(), "Undo should be available");
        
        // Try to undo more than the stack capacity
        int undoCount = 0;
        while (service.getStack().canUndo() && undoCount < 110) {
            service.undo();
            undoCount++;
        }
        
        assertTrue(undoCount <= 100, "Should not be able to undo more than stack capacity");
        assertTrue(undoCount >= 90, "Should be able to undo most recent commands");
    }
    
    @Test
    @Order(15)
    void errorHandlingInCommands() {
        // Test with a command that throws during execution
        Command faultyCommand = new Command() {
            @Override
            public void execute() {
                throw new RuntimeException("Test execution error");
            }
            
            @Override
            public void undo() {
                throw new RuntimeException("Test undo error");
            }
            
            @Override
            public String description() {
                return "Faulty Test Command";
            }
        };
        
        // The service should not catch command exceptions, they should propagate
        assertThrows(RuntimeException.class, () -> service.execute(faultyCommand), 
                "Command execution errors should propagate");
        
        // ModelState should remain clean after failed command
        ModelState ms = ModelState.getInstance();
        assertFalse(ms.isDirty(), "Model should not be dirty after failed command");
        assertFalse(ms.undoAvailableProperty().get(), "Undo should not be available after failed command");
    }
    
    @Test
    @Order(16)
    void serviceApiCompleteness() {
        // Verify all expected public methods are available and working
        assertNotNull(service.getStack(), "getStack() should be available");
        
        assertDoesNotThrow(() -> service.execute(null), "execute() should handle null gracefully");
        assertDoesNotThrow(() -> service.undo(), "undo() should be available");
        assertDoesNotThrow(() -> service.redo(), "redo() should be available");
        
        // Verify singleton
        assertSame(service, CommandExecutionService.getInstance(), 
                "getInstance() should return consistent singleton");
    }
    
    @Test
    @Order(17)
    void complexCommandSequenceIntegrity() {
        FDDINode root = createTestNode("ComplexRoot");
        FDDINode branch1 = createTestNode("Branch1");
        FDDINode branch2 = createTestNode("Branch2");
        FDDINode leaf1 = createTestNode("Leaf1");
        FDDINode leaf2 = createTestNode("Leaf2");
        
        // Execute complex sequence of commands
        service.execute(new AddChildCommand(root, branch1));
        service.execute(new AddChildCommand(root, branch2));
        service.execute(new AddChildCommand(branch1, leaf1));
        service.execute(new AddChildCommand(branch2, leaf2));
        
        // Verify complex hierarchy
        assertEquals(2, root.getChildren().size(), "Root should have 2 branches");
        assertEquals(1, branch1.getChildren().size(), "Branch1 should have 1 leaf");
        assertEquals(1, branch2.getChildren().size(), "Branch2 should have 1 leaf");
        
        // Test partial undo/redo
        service.undo(); // Remove leaf2
        assertEquals(0, branch2.getChildren().size(), "Branch2 should be empty after undo");
        
        service.undo(); // Remove leaf1
        assertEquals(0, branch1.getChildren().size(), "Branch1 should be empty after undo");
        
        service.redo(); // Restore leaf1
        assertEquals(1, branch1.getChildren().size(), "Branch1 should have leaf1 again");
        
        // Verify ModelState consistency throughout
        ModelState ms = ModelState.getInstance();
        assertTrue(ms.isDirty(), "Model should remain dirty");
        assertTrue(ms.undoAvailableProperty().get(), "Undo should be available");
        assertTrue(ms.redoAvailableProperty().get(), "Redo should be available");
    }
    
    private FDDINode createTestNode(String name) {
        Program program = objectFactory.createProgram();
        program.setName(name);
        return (FDDINode) program;
    }
}
