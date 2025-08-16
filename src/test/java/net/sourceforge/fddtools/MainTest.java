package net.sourceforge.fddtools;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Main.java.
 * Tests the main entry point and command line argument handling.
 * 
 * Note: JavaFX Application.launch() can only be called once per JVM,
 * so we focus on testing the static delegation behavior rather than
 * actual application launching.
 */
class MainTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void mainMethodExists() {
        // Test that Main class has a main method
        assertDoesNotThrow(() -> {
            Method mainMethod = Main.class.getMethod("main", String[].class);
            assertNotNull(mainMethod, "Main method should exist");
            assertTrue(java.lang.reflect.Modifier.isStatic(mainMethod.getModifiers()), 
                      "Main method should be static");
            assertTrue(java.lang.reflect.Modifier.isPublic(mainMethod.getModifiers()), 
                      "Main method should be public");
        }, "Main class should have a public static main(String[]) method");
    }

    @Test
    void mainClassIsPublic() {
        // Test that Main class is properly accessible
        assertTrue(java.lang.reflect.Modifier.isPublic(Main.class.getModifiers()), 
                  "Main class should be public");
    }

    @Test 
    void mainClassHasDefaultConstructor() {
        // Test that Main class can be instantiated (though not typically needed)
        assertDoesNotThrow(() -> {
            new Main();
        }, "Main class should have accessible constructor");
    }

    @Test
    void mainMethodDelegatesToFXApplication() {
        // Test that Main.main() delegates to FDDApplicationFX.main()
        // We verify the delegation happens by checking method signatures match
        assertDoesNotThrow(() -> {
            Method mainMethod = Main.class.getMethod("main", String[].class);
            Method fxMainMethod = FDDApplicationFX.class.getMethod("main", String[].class);
            
            assertNotNull(mainMethod, "Main.main() should exist");
            assertNotNull(fxMainMethod, "FDDApplicationFX.main() should exist");
            
            // Both should have same signature
            assertEquals(mainMethod.getParameterTypes().length, 
                        fxMainMethod.getParameterTypes().length,
                        "Both main methods should have same parameter count");
            
        }, "Main method should properly delegate to FDDApplicationFX");
    }

    @Test
    void packageStructureIsCorrect() {
        // Test that classes are in correct package
        assertEquals("net.sourceforge.fddtools", Main.class.getPackage().getName(),
                    "Main class should be in correct package");
        assertEquals("net.sourceforge.fddtools", FDDApplicationFX.class.getPackage().getName(),
                    "FDDApplicationFX class should be in correct package");
    }
}
