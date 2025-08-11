package net.sourceforge.fddtools.guard;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ensures no accidental reintroduction of javax.swing / java.awt compile-time usages.
 * Allows reflective references inside MacOSIntegrationService.
 */
public class ForbiddenAWTSwingUsageTest {
    @Test
    void noForbiddenSwingOrAwt() throws IOException {
        Path srcRoot = Path.of("src/main/java");
        assertTrue(Files.isDirectory(srcRoot));
        try (Stream<Path> paths = Files.walk(srcRoot)) {
            List<Path> offenders = paths
                .filter(p -> p.toString().endsWith(".java"))
                .filter(p -> !p.getFileName().toString().equals("MacOSIntegrationService.java"))
                .filter(p -> {
                    try {
                        // Only treat as offender if there is an actual import usage, not just Javadoc text.
                        return Files.readAllLines(p).stream().anyMatch(line -> {
                            String trimmed = line.stripLeading();
                            if (trimmed.startsWith("//") || trimmed.startsWith("* ") || trimmed.startsWith("/*") || trimmed.startsWith("*")) return false; // skip comments
                            return trimmed.startsWith("import javax.swing") || trimmed.startsWith("import java.awt.");
                        });
                    } catch (IOException e) { return true; }
                })
                .toList();
            if (!offenders.isEmpty()) fail("Forbidden AWT/Swing imports found in: " + offenders);
        }
    }
}
