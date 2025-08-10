package net.sourceforge.fddtools.util;

/**
 * Immutable window bounds snapshot (pure JavaFX-friendly replacement for java.awt.Rectangle).
 * Width/height are integral pixel sizes; coordinates are screen (top-left origin) integers.
 */
public record WindowBounds(int x, int y, int width, int height) {
    public boolean isValid() { return width > 0 && height > 0; }
}
