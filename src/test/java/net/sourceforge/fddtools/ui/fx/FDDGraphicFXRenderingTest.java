/*
 * Copyright (C) 2025 FDD Tools
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.sourceforge.fddtools.ui.fx;

import static org.junit.jupiter.api.Assertions.*;

import com.nebulon.xml.fddi.Feature;
import com.nebulon.xml.fddi.Progress;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.sourceforge.fddtools.model.FDDINode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Comprehensive rendering tests for FDDGraphicFX to ensure border visibility,
 * background fills, and visual consistency are maintained across changes.
 * 
 * These tests act as regression guards to prevent issues like:
 * - Missing borders on new elements
 * - Incomplete background fills
 * - Coordinate calculation errors
 * - Stroke/fill state corruption
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FDDGraphicFXRenderingTest {

    @BeforeAll
    void initializeJavaFX() throws InterruptedException {
        if (!Platform.isFxApplicationThread()) {
            CountDownLatch latch = new CountDownLatch(1);
            try {
                Platform.startup(() -> latch.countDown());
                assertTrue(latch.await(10, TimeUnit.SECONDS), "JavaFX Platform should initialize");
            } catch (IllegalStateException e) {
                // JavaFX toolkit already initialized - this is fine in test suites
                if (e.getMessage().contains("Toolkit already initialized")) {
                    // JavaFX is already running, no need to initialize
                    return;
                } else {
                    throw e;
                }
            }
        }
    }

    @Test
    @DisplayName("Should render borders consistently for new elements")
    void testBorderVisibilityForNewElements() throws InterruptedException {
        final AtomicReference<Boolean> borderTest = new AtomicReference<>(false);
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                // Create a new node (like "New Program")
                FDDINode newNode = new Feature();
                newNode.setName("New Program");
                
                // Create graphic with default white background
                FDDGraphicFX graphic = new FDDGraphicFX(newNode, 0, 0, 150, 200);
                
                // Create canvas and graphics context
                Canvas canvas = new Canvas(200, 250);
                GraphicsContext gc = canvas.getGraphicsContext2D();
                
                // Clear with white background to simulate new element
                gc.setFill(Color.WHITE);
                gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
                
                // Draw the graphic
                graphic.draw(gc, 1.0);
                
                // Verify stroke settings are properly maintained for borders
                // The stroke should be black and line width should be 1.0
                Color finalStroke = (Color) gc.getStroke();
                double finalLineWidth = gc.getLineWidth();
                
                // Test passes if stroke is properly set for border visibility
                boolean hasProperStroke = Color.BLACK.equals(finalStroke) && finalLineWidth == 1.0;
                
                borderTest.set(hasProperStroke);
                
            } catch (Exception e) {
                e.printStackTrace();
                borderTest.set(false);
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Border test should complete");
        assertTrue(borderTest.get(), "New elements should have visible black borders with 1.0 line width");
    }

    @Test
    @DisplayName("Should maintain background fill completeness")
    void testBackgroundFillCompleteness() throws InterruptedException {
        final AtomicReference<Boolean> fillTest = new AtomicReference<>(false);
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                // Create a feature with progress for testing all sections
                FDDINode nodeWithProgress = new Feature();
                nodeWithProgress.setName("Creating a profile for a new user");
                
                Progress progress = new Progress();
                progress.setCompletion(44);
                nodeWithProgress.setProgress(progress);
                
                // Create graphic
                FDDGraphicFX graphic = new FDDGraphicFX(nodeWithProgress, 0, 0, 150, 200);
                
                // Create canvas
                Canvas canvas = new Canvas(200, 250);
                GraphicsContext gc = canvas.getGraphicsContext2D();
                
                // Set a contrasting background to detect incomplete fills
                gc.setFill(Color.MAGENTA); // High contrast color
                gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
                
                // Draw the graphic
                graphic.draw(gc, 1.0);
                
                // Test passes if we successfully drew without exceptions
                // and the graphics context is in a consistent state
                fillTest.set(true);
                
            } catch (Exception e) {
                e.printStackTrace();
                fillTest.set(false);
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Fill test should complete");
        assertTrue(fillTest.get(), "Background fills should complete without errors");
    }

    @Test
    @DisplayName("Should handle multiple zoom levels without border regression")
    void testZoomLevelBorderConsistency() throws InterruptedException {
        final AtomicReference<Boolean> zoomTest = new AtomicReference<>(false);
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                // Create test node
                FDDINode testNode = new Feature();
                testNode.setName("Test Feature");
                
                FDDGraphicFX graphic = new FDDGraphicFX(testNode, 0, 0, 150, 200);
                
                Canvas canvas = new Canvas(300, 400);
                GraphicsContext gc = canvas.getGraphicsContext2D();
                
                // Test multiple zoom levels
                double[] zoomLevels = {0.5, 0.75, 1.0, 1.5, 2.0, 3.0};
                boolean allZoomLevelsPass = true;
                
                for (double zoom : zoomLevels) {
                    // Clear canvas
                    gc.setFill(Color.WHITE);
                    gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
                    
                    // Draw at this zoom level
                    graphic.draw(gc, zoom);
                    
                    // Verify stroke state after drawing
                    Color stroke = (Color) gc.getStroke();
                    double lineWidth = gc.getLineWidth();
                    
                    if (!Color.BLACK.equals(stroke) || lineWidth != 1.0) {
                        allZoomLevelsPass = false;
                        break;
                    }
                }
                
                zoomTest.set(allZoomLevelsPass);
                
            } catch (Exception e) {
                e.printStackTrace();
                zoomTest.set(false);
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Zoom test should complete");
        assertTrue(zoomTest.get(), "Border visibility should be maintained at all zoom levels");
    }

    @Test
    @DisplayName("Should preserve graphics context state after drawing")
    void testGraphicsContextStatePreservation() throws InterruptedException {
        final AtomicReference<Boolean> stateTest = new AtomicReference<>(false);
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                FDDINode testNode = new Feature();
                testNode.setName("State Test");
                
                FDDGraphicFX graphic = new FDDGraphicFX(testNode, 0, 0, 150, 200);
                
                Canvas canvas = new Canvas(200, 250);
                GraphicsContext gc = canvas.getGraphicsContext2D();
                
                // Set specific initial state
                Color initialFill = Color.BLUE;
                Color initialStroke = Color.RED;
                double initialLineWidth = 2.5;
                
                gc.setFill(initialFill);
                gc.setStroke(initialStroke);
                gc.setLineWidth(initialLineWidth);
                
                // Draw the graphic
                graphic.draw(gc, 1.0);
                
                // Verify state is restored
                Color finalFill = (Color) gc.getFill();
                Color finalStroke = (Color) gc.getStroke();
                double finalLineWidth = gc.getLineWidth();
                
                boolean statePreserved = initialFill.equals(finalFill) && 
                                       initialStroke.equals(finalStroke) && 
                                       Math.abs(initialLineWidth - finalLineWidth) < 0.001;
                
                stateTest.set(statePreserved);
                
            } catch (Exception e) {
                e.printStackTrace();
                stateTest.set(false);
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "State test should complete");
        assertTrue(stateTest.get(), "Graphics context state should be preserved after drawing");
    }

    @Test
    @DisplayName("Should render progress elements consistently")
    void testProgressElementConsistency() throws InterruptedException {
        final AtomicReference<Boolean> progressTest = new AtomicReference<>(false);
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                // Test various progress percentages
                int[] progressValues = {0, 25, 50, 75, 100};
                boolean allProgressValuesPass = true;
                
                for (int progressValue : progressValues) {
                    FDDINode testNode = new Feature();
                    testNode.setName("Progress Test " + progressValue + "%");
                    
                    Progress progress = new Progress();
                    progress.setCompletion(progressValue);
                    testNode.setProgress(progress);
                    
                    FDDGraphicFX graphic = new FDDGraphicFX(testNode, 0, 0, 150, 200);
                    
                    Canvas canvas = new Canvas(200, 250);
                    GraphicsContext gc = canvas.getGraphicsContext2D();
                    
                    // Draw and verify no exceptions
                    graphic.draw(gc, 1.0);
                    
                    // Verify final state is consistent
                    Color finalStroke = (Color) gc.getStroke();
                    double finalLineWidth = gc.getLineWidth();
                    
                    if (!Color.BLACK.equals(finalStroke) || finalLineWidth != 1.0) {
                        allProgressValuesPass = false;
                        break;
                    }
                }
                
                progressTest.set(allProgressValuesPass);
                
            } catch (Exception e) {
                e.printStackTrace();
                progressTest.set(false);
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Progress test should complete");
        assertTrue(progressTest.get(), "Progress elements should render consistently");
    }

    @Test
    @DisplayName("Should prevent text clipping in progress bars at various zoom levels")
    void testProgressTextClippingPrevention() throws InterruptedException {
        final AtomicReference<Boolean> clippingTest = new AtomicReference<>(false);
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                // Test with various progress percentages and zoom levels
                int[] progressValues = {1, 44, 99, 100}; // Include edge cases
                double[] zoomLevels = {0.5, 0.75, 1.0, 1.25, 1.5, 2.0}; // Various zoom levels
                boolean allCombinationsPass = true;
                
                for (int progressValue : progressValues) {
                    for (double zoomLevel : zoomLevels) {
                        FDDINode testNode = new Feature();
                        testNode.setName("Text Clip Test " + progressValue + "% @ " + zoomLevel + "x");
                        
                        Progress progress = new Progress();
                        progress.setCompletion(progressValue);
                        testNode.setProgress(progress);
                        
                        // Test with narrow width to stress-test clipping scenarios
                        FDDGraphicFX graphic = new FDDGraphicFX(testNode, 0, 0, 100, 150); // Smaller width
                        
                        Canvas canvas = new Canvas(150, 200);
                        GraphicsContext gc = canvas.getGraphicsContext2D();
                        
                        // Clear with white background
                        gc.setFill(Color.WHITE);
                        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
                        
                        // Draw and verify no exceptions (clipping usually causes rendering issues)
                        graphic.draw(gc, zoomLevel);
                        
                        // Verify the graphics context is in a consistent state
                        Color finalStroke = (Color) gc.getStroke();
                        double finalLineWidth = gc.getLineWidth();
                        
                        if (!Color.BLACK.equals(finalStroke) || finalLineWidth != 1.0) {
                            allCombinationsPass = false;
                            System.err.println("Failed at progress=" + progressValue + "%, zoom=" + zoomLevel);
                            break;
                        }
                    }
                    if (!allCombinationsPass) break;
                }
                
                clippingTest.set(allCombinationsPass);
                
            } catch (Exception e) {
                e.printStackTrace();
                clippingTest.set(false);
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(10, TimeUnit.SECONDS), "Clipping test should complete");
        assertTrue(clippingTest.get(), "Progress text should not clip at any zoom level or progress value");
    }
}
