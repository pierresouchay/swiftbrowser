/**
 *
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-07-04 14:51:32 +0200 (Ven 04 jul 2014) $
 */
package net.souchay.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A class that send notifications while reading a stream
 * 
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3830 $
 * 
 */
public class InputStreamWithProgress extends InputStream {

    /**
     * Interface to implement for being notified
     * 
     * @copyright Pierre Souchay - 2013,2014
     * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
     * @version $Revision: 3830 $
     */
    public static interface InputStreamWithProgressListener {

        /**
         * Called when stream is closed
         * 
         * @param source
         */
        void onClosed(InputStreamWithProgress source);

        /**
         * Called when bytes are read
         * 
         * @param source
         * @param bytesRead
         * @param total
         * @param fileSize = full size
         */
        void onBytesReaden(InputStreamWithProgress source, int bytesRead, long total);
    }

    private static final InputStreamWithProgressListener noopProgressListener = new InputStreamWithProgressListener() {

        @Override
        public final void onClosed(final InputStreamWithProgress source) {
            // NOOP
        }

        @Override
        public final void onBytesReaden(final InputStreamWithProgress source, final int bytesRead, final long total) {
            // NOOP
        }
    };

    /**
     * Get a NOOP {@link InputStreamWithProgressListener} object
     * 
     * @return a No-Operation Object reused
     */
    public static InputStreamWithProgressListener getNoopProgressListener() {
        return noopProgressListener;
    }

    private final AtomicLong totalBytes = new AtomicLong(0);

    private final InputStreamWithProgressListener listener;

    private final InputStream delegate;

    private long fullSize;

    /**
     * get the fullSize
     * 
     * @return the fullSize
     */
    public long getFullSize() {
        return fullSize;
    }

    /**
     * Set the fullSize
     * 
     * @param fullSize the fullSize to set
     */
    public void setFullSize(long fullSize) {
        this.fullSize = fullSize;
    }

    /**
     * Constructor
     * 
     * @param delegate
     * @param listener
     */
    public InputStreamWithProgress(InputStream delegate, InputStreamWithProgressListener listener) {
        super();
        this.delegate = delegate;
        this.listener = listener;
    }

    /**
     * get the totalBytes
     * 
     * @return the totalBytes
     */
    public long getTotalBytes() {
        return totalBytes.get();
    }

    /**
     * Convenience constructor without listener
     * 
     * @param delegate
     */
    public InputStreamWithProgress(InputStream delegate) {
        this(delegate, getNoopProgressListener());
    }

    private final void increment(int number) {
        if (number > 0) {
            long total = totalBytes.addAndGet(number);
            listener.onBytesReaden(this, number, total);
        }
    }

    /**
     * @see java.io.InputStream#read()
     */
    @Override
    public int read() throws IOException {
        int val = delegate.read();
        increment(1);
        return val;
    }

    /**
     * @see java.io.InputStream#read(byte[])
     */
    @Override
    public int read(byte[] b) throws IOException {
        int read = delegate.read(b);
        increment(read);
        return read;
    }

    /**
     * @see java.io.InputStream#read(byte[], int, int)
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = delegate.read(b, off, len);
        increment(read);
        return read;
    }

    /**
     * @see java.io.InputStream#skip(long)
     */
    @Override
    public long skip(long n) throws IOException {
        long skip = delegate.skip(n);
        increment((int) skip);
        return skip;
    }

    /**
     * @see java.io.InputStream#available()
     */
    @Override
    public int available() throws IOException {
        return delegate.available();
    }

    /**
     * @see java.io.InputStream#close()
     */
    @Override
    public void close() throws IOException {
        try {
            delegate.close();
        } finally {
            listener.onClosed(this);
        }
    }

    /**
     * @see java.io.InputStream#mark(int)
     */
    @Override
    public synchronized void mark(int readlimit) {
        delegate.mark(readlimit);
    }

    /**
     * @see java.io.InputStream#reset()
     */
    @Override
    public synchronized void reset() throws IOException {
        delegate.reset();
    }

    /**
     * @see java.io.InputStream#markSupported()
     */
    @Override
    public boolean markSupported() {
        return delegate.markSupported();
    }

}
