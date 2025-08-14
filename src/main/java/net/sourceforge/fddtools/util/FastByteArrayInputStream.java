/**
 * Adapted from:
 * http://javatechniques.com/blog/faster-deep-copies-of-java-objects/
 */

package net.sourceforge.fddtools.util;

import java.io.InputStream;

/**
 * ByteArrayInputStream implementation that does not synchronize methods.
 */
public class FastByteArrayInputStream extends InputStream
{
    /**
     * Our byte buffer
     */
    protected byte[] buf = null;
    /**
     * Number of bytes that we can read from the buffer
     */
    protected int count = 0;
    /**
     * Number of bytes that have been read from the buffer
     */
    protected int pos = 0;

    public FastByteArrayInputStream(byte[] buf, int count)
    {
        this.buf = buf;
        this.count = count;
    }

    @Override
    public final int available()
    {
        return count - pos;
    }

    @Override
    public final int read()
    {
        return (pos < count) ? (buf[pos++] & 0xff) : -1;
    }

    @Override
    public final int read(byte[] b, int off, int len)
    {
        if(pos >= count)
        {
            return -1;
        }

        if((pos + len) > count)
        {
            len = (count - pos);
        }

        System.arraycopy(buf, pos, b, off, len);
        pos += len;
        return len;
    }

    @Override
    public final long skip(long n)
    {
        long max = count - (long) pos;
        if (n > max) {
            n = max;
        }
        if (n <= 0) {
            return 0L;
        }
        int delta = (int) n; // safe due to bounding above
        pos += delta;
        return (long) delta;
    }
}