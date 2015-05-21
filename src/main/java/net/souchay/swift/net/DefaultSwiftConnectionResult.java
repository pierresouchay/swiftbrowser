/**
 *
 * $LastChangedBy: souchay $ - $LastChangedDate: 2015-05-20 02:51:10 +0200 (Mer, 20 mai 2015) $
 */
package net.souchay.swift.net;

import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Hashtable;
import java.util.Map;
import net.souchay.swift.gui.table.ProgressMonitorResult;
import net.souchay.swift.gui.table.ProgressMonitorTableModel;
import net.souchay.swift.net.FsConnection.NoNeedToDownloadException;
import net.souchay.swift.net.FsConnection.OnFileDownloaded;
import net.souchay.utilities.Application;

/**
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3922 $
 * 
 */
public class DefaultSwiftConnectionResult implements SwiftConnectionResultHandler, HttpConnectionListener {

    /**
     * get the progressTableModel
     * 
     * @return the progressTableModel
     */
    public ProgressMonitorTableModel getProgressTableModel() {
        return progressTableModel;
    }

    public interface DownloadProgressMonitorFactory {

        public DownloadProgressMonitor getDownloadProgressMonitor(int id, Component c, String msg);

        public void closeDownloadProgressMonitor(int id);
    }

    private final DownloadProgressMonitorFactory factory;

    private final ProgressMonitorTableModel progressTableModel = new ProgressMonitorTableModel();

    private final Component component;

    /**
     * Constructor
     * 
     * @param component parent component for GUI
     */
    public DefaultSwiftConnectionResult(Component component, DownloadProgressMonitorFactory factory) {
        this.component = component;
        this.factory = factory != null ? factory : new DefaultDownloadProgressMonitorFactory();
    }

    private class DefaultDownloadProgressMonitorFactory implements DownloadProgressMonitorFactory {

        private final Map<Integer, ProgressMonitorResult> monitors = new Hashtable<Integer, ProgressMonitorResult>();

        @Override
        public ProgressMonitorResult getDownloadProgressMonitor(int id, Component c, String msg) {
            ProgressMonitorResult m = monitors.get(id);
            if (m == null) {
                m = new ProgressMonitorResult(msg, ".", 0, 100); //$NON-NLS-1$
                monitors.put(id, m);
                progressTableModel.addRow(m);
                Application.setDockIconBadge("" + monitors.size()); //$NON-NLS-1$
            }
            return m;
        }

        @Override
        public void closeDownloadProgressMonitor(int id) {
            ProgressMonitorResult m = monitors.remove(id);
            if (m != null) {
                m.close();
                progressTableModel.removeRow(m);
                if (monitors.isEmpty())
                    Application.setDockIconBadge(null);
            }
        }

    };

    @Override
    public void onFileContent(int id, HttpConnectionListener listener, HttpURLConnection connection, InputStream out,
            String container, String path, OnFileDownloaded onDownload) {

        final long start = System.currentTimeMillis();
        int len = connection.getContentLength();
        final String eTag = connection.getHeaderField(FsConnection.ETAG);

        BufferedOutputStream outFile = null;
        BufferedInputStream in = null;
        File f = null;
        boolean success = true;
        try {
            f = onDownload.onStartDownload(container, path, len, connection.getLastModified(), eTag);
            outFile = new BufferedOutputStream(new FileOutputStream(f));
            in = new BufferedInputStream(out);
            final byte data[] = new byte[1024];
            int flen = in.read(data);
            long fullLen = 0;
            String msg = Messages.getString("operations.receiving", connection.getURL().toExternalForm()); //$NON-NLS-1$
            while (flen > 0) {
                outFile.write(data, 0, flen);
                fullLen += flen;
                listener.onProgress(Integer.valueOf(id), msg, start, fullLen, len);
                flen = in.read(data);
            }
        } catch (NoNeedToDownloadException err) {
            // Silent
            success = true;
            f = err.getFile();
        } catch (IOException err) {
            success = false;
            err.printStackTrace();
            if (f != null && f.exists())
                f.deleteOnExit();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                    ignored.printStackTrace();
                }
            }
            if (outFile != null) {
                try {
                    outFile.close();
                } catch (IOException ignored) {
                    ignored.printStackTrace();
                }
            }
            onDownload.onDownload(f, container, path, success);

        }
    }

    @Override
    public void onProgress(int id, String msg, long start, long currentBytes, long totalBytes) throws IOException {
        DownloadProgressMonitor m = factory.getDownloadProgressMonitor(id, component, msg);
        final long now = System.currentTimeMillis();
        final long delta = now - start;
        if (delta > 2000 && currentBytes > 0) {
            long kbps = currentBytes / delta;
            if (kbps > 0) {
                final long remain = (totalBytes - currentBytes) / kbps / 1000;
                if (kbps < 1000) {
                    m.setNote(Messages.getString("KbytesPerSec", kbps, delta / 1000, remain)); //$NON-NLS-1$
                } else {
                    long mbps = kbps / 1000;
                    kbps = kbps % 1000 / 100;
                    m.setNote(Messages.getString("MbytesPerSec", mbps, kbps, delta / 1000, remain)); //$NON-NLS-1$
                }
            }
        } else {
            m.setNote(" "); //$NON-NLS-1$
        }
        if (totalBytes > Integer.MAX_VALUE) {
            totalBytes /= 1048576;
            currentBytes /= 1048576;
        }

        if (totalBytes < 1)
            m.setMaximum(Integer.MAX_VALUE);
        else
            m.setMaximum((int) totalBytes);
        m.setProgress((int) currentBytes);
        if (m.isCanceled()) {
            throw new IOException(Messages.getString("DefaultSwiftConnectionResult.canceledByUserException")); //$NON-NLS-1$
        }
    }

    @Override
    public void onClose(int url) {
        factory.closeDownloadProgressMonitor(url);
    }

}
