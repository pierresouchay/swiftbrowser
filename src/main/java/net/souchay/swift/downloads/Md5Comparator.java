package net.souchay.swift.downloads;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import net.souchay.swift.net.FsConnection;
import net.souchay.utilities.HexUtilities;

public class Md5Comparator implements CompareStrategy, Closeable {

    private Md5Comparator() {
    }

    private final static Md5Comparator instance = new Md5Comparator();

    public static Md5Comparator getInstance() {
        return instance;
    }

    private final ThreadLocal<MessageDigest> digest = new ThreadLocal<MessageDigest>() {

        @Override
        protected MessageDigest initialValue() {
            try {
                return MessageDigest.getInstance("MD5"); //$NON-NLS-1$
            } catch (Exception err) {
                throw new RuntimeException("MD5 is not supported: " + err.getClass() + ": " + err.getMessage(), err); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

    };

    public final void cancelIfDownloadCanBeSkipped(File f, long size, String hash)
            throws FsConnection.NoNeedToDownloadException {
        if (f.length() != size)
            return;
        final String localMd5 = computeMd5(f);
        if (localMd5.equals(hash)) {
            throw new FsConnection.NoNeedToDownloadException(f);
        }
    }

    /**
     * Return a not null md5 representation.
     * 
     * @param file the file to test
     * @return empty string is file does not exists or cannot be read
     */
    public final String computeMd5(final File file) {
        if (!file.exists() || !file.canRead() || !file.isAbsolute()) {
            return ""; //$NON-NLS-1$
        }
        final MessageDigest md = digest.get();
        BufferedInputStream in = null;
        try {
            md.reset();
            final int BUF_SZ = 8192;
            final byte buf[] = new byte[BUF_SZ];
            in = new BufferedInputStream(new FileInputStream(file), BUF_SZ);
            int read;
            while (BUF_SZ == (read = in.read(buf))) {
                // OK, we fill all we can
                md.update(buf);
            }
            if (read > 0) {
                md.update(buf, 0, read);
            }
        } catch (IOException err) {
            return ""; //$NON-NLS-1$
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                    ignored.printStackTrace();
                }
            }
        }
        final byte md5[] = md.digest();
        return HexUtilities.toHexStringWithoutSeparator(md5);
    }

    @Override
    public boolean areEqual(final RemoteSpec spec, final File file) {
        if (!file.exists())
            return false;
        if (spec.size() != file.length() || file.isDirectory())
            return false;
        return computeMd5(file).equalsIgnoreCase(spec.eTag());
    }

    @Override
    public void close() {
        digest.remove();
    }
}
