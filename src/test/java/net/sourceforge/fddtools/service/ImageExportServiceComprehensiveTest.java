package net.sourceforge.fddtools.service;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for ImageExportService covering:
 * - Singleton pattern validation
 * - PNG export functionality
 * - Canvas snapshot handling
 * - Thread safety (FX thread vs other threads)
 * - Timeout configuration and handling
 * - File system integration
 * - Error handling and edge cases
 * - Various canvas sizes and content
 * - Format handling (JPEG mapped to PNG)
 */
@DisplayName("ImageExportService Comprehensive Tests")
class ImageExportServiceComprehensiveTest {

    private ImageExportService imageExportService;
    
    @TempDir
    Path tempDir;

    @BeforeAll
    static void initJavaFX() throws InterruptedException {
        // Initialize JavaFX toolkit if not already done
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException alreadyStarted) {
            latch.countDown();
        }
        assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX should initialize");
    }

    @BeforeEach
    void setUp() {
        imageExportService = ImageExportService.getInstance();
    }

    @Test
    @DisplayName("Should enforce singleton pattern")
    void singletonPattern() {
        ImageExportService instance1 = ImageExportService.getInstance();
        ImageExportService instance2 = ImageExportService.getInstance();
        
        assertSame(instance1, instance2, "ImageExportService should be singleton");
        assertSame(imageExportService, instance1, "All instances should be the same");
    }

    @Test
    @DisplayName("Should export basic canvas to PNG")
    @Timeout(10)
    void basicPngExport() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final Canvas[] canvasRef = new Canvas[1];
        final Exception[] exceptionRef = new Exception[1];
        
        Platform.runLater(() -> {
            try {
                Canvas canvas = new Canvas(100, 100);
                GraphicsContext gc = canvas.getGraphicsContext2D();
                
                // Draw simple content
                gc.setFill(Color.BLUE);
                gc.fillRect(10, 10, 80, 80);
                gc.setFill(Color.RED);
                gc.fillOval(20, 20, 60, 60);
                
                canvasRef[0] = canvas;
            } catch (Exception e) {
                exceptionRef[0] = e;
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Canvas creation should complete");
        if (exceptionRef[0] != null) {
            throw exceptionRef[0];
        }
        
        Canvas canvas = canvasRef[0];
        assertNotNull(canvas, "Canvas should be created");
        
        File targetFile = tempDir.resolve("test-export.png").toFile();
        File result = imageExportService.export(canvas, targetFile, "png");
        
        assertSame(targetFile, result, "Should return the same target file");
        assertTrue(targetFile.exists(), "Exported file should exist");
        assertTrue(targetFile.length() > 0, "Exported file should have content");
        
        // Basic PNG file signature check
        byte[] header = Files.readAllBytes(targetFile.toPath());
        assertTrue(header.length >= 8, "File should have at least PNG header");
        assertEquals((byte)0x89, header[0], "PNG signature byte 1");
        assertEquals((byte)0x50, header[1], "PNG signature byte 2 (P)");
        assertEquals((byte)0x4E, header[2], "PNG signature byte 3 (N)");
        assertEquals((byte)0x47, header[3], "PNG signature byte 4 (G)");
    }

    @Test
    @DisplayName("Should handle JPEG format by converting to PNG")
    @Timeout(10)
    void jpegToPngConversion() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final Canvas[] canvasRef = new Canvas[1];
        
        Platform.runLater(() -> {
            try {
                Canvas canvas = new Canvas(50, 50);
                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.setFill(Color.GREEN);
                gc.fillRect(0, 0, 50, 50);
                canvasRef[0] = canvas;
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Canvas creation should complete");
        
        File targetFile = tempDir.resolve("test-jpeg.jpg").toFile();
        File result = imageExportService.export(canvasRef[0], targetFile, "jpeg");
        
        assertTrue(result.exists(), "File should be created");
        
        // Should still be PNG format internally
        byte[] header = Files.readAllBytes(result.toPath());
        assertEquals((byte)0x89, header[0], "Should be PNG format even when JPEG requested");
    }

    @Test
    @DisplayName("Should handle various canvas sizes")
    @Timeout(15)
    void variousCanvasSizes() throws Exception {
        int[] sizes = {1, 10, 100, 500}; // Test different sizes
        
        for (int size : sizes) {
            CountDownLatch latch = new CountDownLatch(1);
            final Canvas[] canvasRef = new Canvas[1];
            
            Platform.runLater(() -> {
                try {
                    Canvas canvas = new Canvas(size, size);
                    GraphicsContext gc = canvas.getGraphicsContext2D();
                    
                    // Fill with different colors based on size
                    Color color = switch (size) {
                        case 1 -> Color.BLACK;
                        case 10 -> Color.RED;
                        case 100 -> Color.GREEN;
                        case 500 -> Color.BLUE;
                        default -> Color.GRAY;
                    };
                    
                    gc.setFill(color);
                    gc.fillRect(0, 0, size, size);
                    canvasRef[0] = canvas;
                } finally {
                    latch.countDown();
                }
            });
            
            assertTrue(latch.await(5, TimeUnit.SECONDS), "Canvas creation should complete for size " + size);
            
            File targetFile = tempDir.resolve("test-" + size + "x" + size + ".png").toFile();
            File result = imageExportService.export(canvasRef[0], targetFile, "png");
            
            assertTrue(result.exists(), "File should exist for size " + size);
            assertTrue(result.length() > 0, "File should have content for size " + size);
        }
    }

    @Test
    @DisplayName("Should handle transparent canvas")
    @Timeout(10)
    void transparentCanvas() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final Canvas[] canvasRef = new Canvas[1];
        
        Platform.runLater(() -> {
            try {
                Canvas canvas = new Canvas(100, 100);
                // Don't draw anything - should be transparent
                canvasRef[0] = canvas;
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Canvas creation should complete");
        
        File targetFile = tempDir.resolve("transparent.png").toFile();
        File result = imageExportService.export(canvasRef[0], targetFile, "png");
        
        assertTrue(result.exists(), "Transparent canvas should export successfully");
        assertTrue(result.length() > 0, "Even transparent canvas should produce some file content");
    }

    @Test
    @DisplayName("Should handle fractional canvas sizes")
    @Timeout(10)
    void fractionalCanvasSizes() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final Canvas[] canvasRef = new Canvas[1];
        
        Platform.runLater(() -> {
            try {
                // Canvas with fractional dimensions
                Canvas canvas = new Canvas(100.7, 200.3);
                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.setFill(Color.PURPLE);
                gc.fillRect(0, 0, 100.7, 200.3);
                canvasRef[0] = canvas;
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Canvas creation should complete");
        
        File targetFile = tempDir.resolve("fractional.png").toFile();
        File result = imageExportService.export(canvasRef[0], targetFile, "png");
        
        assertTrue(result.exists(), "Fractional size canvas should export");
        assertTrue(result.length() > 0, "Fractional size should produce content");
    }

    @Test
    @DisplayName("Should handle null parameters gracefully")
    void nullParameterHandling() {
        Canvas canvas = new Canvas(10, 10);
        File targetFile = tempDir.resolve("null-test.png").toFile();
        
        // Null canvas
        assertThrows(IllegalArgumentException.class, () -> {
            imageExportService.export(null, targetFile, "png");
        }, "Null canvas should throw IllegalArgumentException");
        
        // Null target file
        assertThrows(IllegalArgumentException.class, () -> {
            imageExportService.export(canvas, null, "png");
        }, "Null target should throw IllegalArgumentException");
        
        // Null format should default to PNG and work
        assertDoesNotThrow(() -> {
            imageExportService.export(canvas, targetFile, null);
        }, "Null format should be handled gracefully");
    }

    @Test
    @DisplayName("Should handle invalid file paths")
    void invalidFilePathHandling() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final Canvas[] canvasRef = new Canvas[1];
        
        Platform.runLater(() -> {
            try {
                Canvas canvas = new Canvas(10, 10);
                canvasRef[0] = canvas;
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Canvas creation should complete");
        
        // Invalid directory path
        File invalidFile = new File("/invalid/path/that/cannot/exist/test.png");
        
        Exception exception = assertThrows(Exception.class, () -> {
            imageExportService.export(canvasRef[0], invalidFile, "png");
        }, "Invalid path should throw exception");
        
        assertTrue(exception.getMessage() != null || exception.getCause() != null, 
                   "Exception should have meaningful message or cause");
    }

    @Test
    @DisplayName("Should respect timeout configuration")
    @Timeout(15)
    void timeoutConfiguration() throws Exception {
        // Set short timeout for testing
        String originalTimeout = System.getProperty("fdd.image.snapshot.timeout.seconds");
        System.setProperty("fdd.image.snapshot.timeout.seconds", "1");
        
        try {
            CountDownLatch latch = new CountDownLatch(1);
            final Canvas[] canvasRef = new Canvas[1];
            
            Platform.runLater(() -> {
                try {
                    Canvas canvas = new Canvas(100, 100);
                    GraphicsContext gc = canvas.getGraphicsContext2D();
                    gc.setFill(Color.ORANGE);
                    gc.fillRect(0, 0, 100, 100);
                    canvasRef[0] = canvas;
                } finally {
                    latch.countDown();
                }
            });
            
            assertTrue(latch.await(5, TimeUnit.SECONDS), "Canvas creation should complete");
            
            File targetFile = tempDir.resolve("timeout-test.png").toFile();
            
            // Export from non-FX thread (should use timeout)
            File result = imageExportService.export(canvasRef[0], targetFile, "png");
            
            // Should still succeed (timeout is for waiting, not failing)
            assertTrue(result.exists(), "Export should succeed even with short timeout");
            
        } finally {
            // Restore original timeout
            if (originalTimeout != null) {
                System.setProperty("fdd.image.snapshot.timeout.seconds", originalTimeout);
            } else {
                System.clearProperty("fdd.image.snapshot.timeout.seconds");
            }
        }
    }

    @Test
    @DisplayName("Should export from FX thread (fast path)")
    @Timeout(10)
    void fxThreadFastPath() throws Exception {
        CountDownLatch exportLatch = new CountDownLatch(1);
        final Exception[] exceptionRef = new Exception[1];
        final File[] resultRef = new File[1];
        
        Platform.runLater(() -> {
            try {
                Canvas canvas = new Canvas(75, 75);
                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.setFill(Color.CYAN);
                gc.fillOval(10, 10, 55, 55);
                
                File targetFile = tempDir.resolve("fx-thread-export.png").toFile();
                File result = imageExportService.export(canvas, targetFile, "png");
                resultRef[0] = result;
                
            } catch (Exception e) {
                exceptionRef[0] = e;
            } finally {
                exportLatch.countDown();
            }
        });
        
        assertTrue(exportLatch.await(8, TimeUnit.SECONDS), "Export from FX thread should complete");
        
        if (exceptionRef[0] != null) {
            throw exceptionRef[0];
        }
        
        File result = resultRef[0];
        assertNotNull(result, "Result should not be null");
        assertTrue(result.exists(), "File should exist");
        assertTrue(result.length() > 0, "File should have content");
    }

    @Test
    @DisplayName("Should handle concurrent exports safely")
    @Timeout(20)
    void concurrentExports() throws Exception {
        int exportCount = 5;
        CountDownLatch setupLatch = new CountDownLatch(exportCount);
        CountDownLatch exportLatch = new CountDownLatch(exportCount);
        final Canvas[] canvases = new Canvas[exportCount];
        final Exception[] exceptions = new Exception[exportCount];
        
        // Create canvases on FX thread
        for (int i = 0; i < exportCount; i++) {
            final int index = i;
            Platform.runLater(() -> {
                try {
                    Canvas canvas = new Canvas(50 + index * 10, 50 + index * 10);
                    GraphicsContext gc = canvas.getGraphicsContext2D();
                    
                    // Different colors for each canvas
                    Color color = Color.hsb(index * 60, 0.8, 0.9);
                    gc.setFill(color);
                    gc.fillRect(5, 5, 40 + index * 10, 40 + index * 10);
                    
                    canvases[index] = canvas;
                } catch (Exception e) {
                    exceptions[index] = e;
                } finally {
                    setupLatch.countDown();
                }
            });
        }
        
        assertTrue(setupLatch.await(10, TimeUnit.SECONDS), "Canvas setup should complete");
        
        // Check for setup exceptions
        for (int i = 0; i < exportCount; i++) {
            if (exceptions[i] != null) {
                throw exceptions[i];
            }
        }
        
        // Start concurrent exports
        for (int i = 0; i < exportCount; i++) {
            final int index = i;
            new Thread(() -> {
                try {
                    File targetFile = tempDir.resolve("concurrent-" + index + ".png").toFile();
                    File result = imageExportService.export(canvases[index], targetFile, "png");
                    
                    if (!result.exists() || result.length() == 0) {
                        exceptions[index] = new RuntimeException("Export " + index + " failed");
                    }
                } catch (Exception e) {
                    exceptions[index] = e;
                } finally {
                    exportLatch.countDown();
                }
            }).start();
        }
        
        assertTrue(exportLatch.await(15, TimeUnit.SECONDS), "All exports should complete");
        
        // Check results
        for (int i = 0; i < exportCount; i++) {
            if (exceptions[i] != null) {
                throw new AssertionError("Export " + i + " failed", exceptions[i]);
            }
            
            File expectedFile = tempDir.resolve("concurrent-" + i + ".png").toFile();
            assertTrue(expectedFile.exists(), "File " + i + " should exist");
            assertTrue(expectedFile.length() > 0, "File " + i + " should have content");
        }
    }

    @Test
    @DisplayName("Should handle zero-size canvas")
    @Timeout(10)
    void zeroSizeCanvas() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final Canvas[] canvasRef = new Canvas[1];
        
        Platform.runLater(() -> {
            try {
                Canvas canvas = new Canvas(0, 0);
                canvasRef[0] = canvas;
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Canvas creation should complete");
        
        File targetFile = tempDir.resolve("zero-size.png").toFile();
        
        // JavaFX does not allow zero-size images - this will throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            imageExportService.export(canvasRef[0], targetFile, "png");
        }, "Zero-size canvas should throw IllegalArgumentException as per JavaFX specification");
    }

    @Test
    @DisplayName("Should handle complex canvas content")
    @Timeout(10)
    void complexCanvasContent() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final Canvas[] canvasRef = new Canvas[1];
        
        Platform.runLater(() -> {
            try {
                Canvas canvas = new Canvas(200, 150);
                GraphicsContext gc = canvas.getGraphicsContext2D();
                
                // Draw complex content
                gc.setFill(Color.WHITE);
                gc.fillRect(0, 0, 200, 150);
                
                // Gradient-like effect with multiple shapes
                for (int i = 0; i < 50; i++) {
                    double alpha = 1.0 - (i / 50.0);
                    gc.setFill(Color.color(0.2, 0.5, 0.8, alpha));
                    gc.fillOval(i * 2, i, 50 - i, 30 - i/2);
                }
                
                // Some text (if font is available)
                gc.setFill(Color.BLACK);
                gc.fillText("Test Export", 10, 130);
                
                // Lines and strokes
                gc.setStroke(Color.RED);
                gc.setLineWidth(2);
                gc.strokeLine(0, 0, 200, 150);
                gc.strokeLine(200, 0, 0, 150);
                
                canvasRef[0] = canvas;
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Complex canvas creation should complete");
        
        File targetFile = tempDir.resolve("complex-content.png").toFile();
        File result = imageExportService.export(canvasRef[0], targetFile, "png");
        
        assertTrue(result.exists(), "Complex content should export successfully");
        assertTrue(result.length() > 1000, "Complex content should produce substantial file size");
    }

    @Test
    @DisplayName("Should handle file overwrite scenarios")
    @Timeout(10)
    void fileOverwriteScenarios() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final Canvas[] canvasRef = new Canvas[1];
        
        Platform.runLater(() -> {
            try {
                Canvas canvas = new Canvas(30, 30);
                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.setFill(Color.YELLOW);
                gc.fillRect(0, 0, 30, 30);
                canvasRef[0] = canvas;
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Canvas creation should complete");
        
        File targetFile = tempDir.resolve("overwrite-test.png").toFile();
        
        // First export
        File result1 = imageExportService.export(canvasRef[0], targetFile, "png");
        assertTrue(result1.exists(), "First export should succeed");
        
        // Modify canvas and export again (overwrite)
        Platform.runLater(() -> {
            GraphicsContext gc = canvasRef[0].getGraphicsContext2D();
            gc.setFill(Color.MAGENTA);
            gc.fillRect(0, 0, 30, 30);
        });
        
        Thread.sleep(100); // Small delay to ensure modification
        
        File result2 = imageExportService.export(canvasRef[0], targetFile, "png");
        assertTrue(result2.exists(), "Second export should succeed");
        
        // Files should be the same reference but content might be different
        assertSame(result1, result2, "Should return same file reference");
        // Size might be different due to different content compression
        assertTrue(result2.length() > 0, "Overwritten file should have content");
    }
}
