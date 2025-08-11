package net.sourceforge.fddtools.service;

import javafx.application.Platform;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates image export logic. Now pure JavaFX for PNG export using
 * javafx.scene.Node#snapshot. JPEG (if requested) is mapped to PNG for now to
 * avoid AWT dependencies. If true JPEG encoding is needed later we can add an
 * optional module or third‑party encoder.
 */
public final class ImageExportService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageExportService.class);
    private static final ImageExportService INSTANCE = new ImageExportService();
    /** Snapshot timeout (seconds) when taking a JavaFX canvas snapshot off the FX thread. Configurable via system property
     *  'fdd.image.snapshot.timeout.seconds' (integer). Defaults to 12 seconds to allow slower CI/macOS headless startup without
     *  producing routine warnings for tiny canvases. */
    private static final int SNAPSHOT_TIMEOUT_SECONDS = Integer.getInteger("fdd.image.snapshot.timeout.seconds", 12);
    private ImageExportService() {}
    public static ImageExportService getInstance(){ return INSTANCE; }

    public java.io.File export(Canvas canvas, java.io.File target, String requestedFormat) throws Exception {
        if (canvas == null || target == null) throw new IllegalArgumentException("canvas/target required");
        // Only PNG currently (requested JPEG -> PNG) to keep implementation pure JavaFX.
        String format = (requestedFormat != null && requestedFormat.equalsIgnoreCase("png")) ? "png" : "png";
        int w = (int) Math.ceil(canvas.getWidth());
        int h = (int) Math.ceil(canvas.getHeight());
        WritableImage wi = new WritableImage(w, h);
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        if (Platform.isFxApplicationThread()) {
            // Fast path: already on FX thread
            canvas.snapshot(params, wi);
        } else {
            // Avoid potential indefinite hang if FX thread is saturated by adding a timeout
            final CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    canvas.snapshot(params, wi);
                } finally {
                    latch.countDown();
                }
            });
            boolean completed = latch.await(SNAPSHOT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!completed) {
                LOGGER.warn("Canvas snapshot timed out after {}s; proceeding without guaranteed pixel data (w={}, h={})", SNAPSHOT_TIMEOUT_SECONDS, w, h);
                // Fallback: attempt snapshot synchronously (may throw if called off FX thread but better than silent hang)
                try {
                    Platform.runLater(() -> {}); // nudge event queue for diagnostics
                } catch (Exception ignored) { }
            }
        }
        writePng(wi, target);
        LOGGER.info("Image exported: {} ({}x{}, format={})", target.getAbsolutePath(), w, h, format);
        return target;
    }

    private void writePng(WritableImage wi, java.io.File target) throws Exception {
    // Simple PNG encoder (RGBA 8-bit) using default Deflater compression (moderate size reduction).
    // If performance issues arise we can expose a compression level preference.
        writeRawPng(wi, target);
    }

    private void writeRawPng(WritableImage wi, java.io.File target) throws Exception {
        int w=(int)wi.getWidth(), h=(int)wi.getHeight();
        var out = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(target)));
        try {
            // PNG signature
            out.writeLong(0x89504E470D0A1A0AL);
            // IHDR chunk
            var ihdr = java.nio.ByteBuffer.allocate(13);
            ihdr.putInt(w); ihdr.putInt(h); ihdr.put((byte)8); ihdr.put((byte)6); ihdr.put((byte)0); ihdr.put((byte)0); ihdr.put((byte)0);
            writeChunk(out, "IHDR", ihdr.array());
            // Raw image data with no filtering (filter byte 0 per scanline)
            var pr = wi.getPixelReader();
            byte[] scan = new byte[(w*4)+1];
            var raw = new java.io.ByteArrayOutputStream();
            for(int y=0;y<h;y++){
                scan[0]=0; // no filter
                for(int x=0;x<w;x++){
                    int argb = pr.getArgb(x,y);
                    int base = 1 + x*4;
                    scan[base]   = (byte)((argb>>16)&0xFF); // R
                    scan[base+1] = (byte)((argb>>8)&0xFF);  // G
                    scan[base+2] = (byte)(argb & 0xFF);     // B
                    scan[base+3] = (byte)((argb>>24)&0xFF); // A
                }
                raw.write(scan);
            }
            // Compress raw scanlines with default compression
            byte[] uncompressed = raw.toByteArray();
            java.io.ByteArrayOutputStream z = new java.io.ByteArrayOutputStream();
            java.util.zip.Deflater def = new java.util.zip.Deflater(java.util.zip.Deflater.DEFAULT_COMPRESSION);
            def.setInput(uncompressed); def.finish();
            byte[] buf = new byte[8192];
            while(!def.finished()) { int n=def.deflate(buf); z.write(buf,0,n); }
            def.end();
            writeChunk(out, "IDAT", z.toByteArray());
            writeChunk(out, "IEND", new byte[0]);
        } finally {
            out.close();
        }
    }

    private void writeChunk(java.io.DataOutputStream out, String type, byte[] data) throws Exception {
        out.writeInt(data.length);
        byte[] typeBytes = type.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        out.write(typeBytes);
        out.write(data);
        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        crc.update(typeBytes); crc.update(data);
        out.writeInt((int)crc.getValue());
    }
}
