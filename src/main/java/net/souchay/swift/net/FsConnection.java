/**
 *
 * $LastChangedBy: souchay $ - $LastChangedDate: 2015-05-20 02:51:10 +0200 (Mer, 20 mai 2015) $
 */
package net.souchay.swift.net;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.souchay.swift.gui.FileBuilder;
import net.souchay.swift.gui.ObjectIFace;

/**
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3922 $
 * 
 */
public interface FsConnection {

    /**
     * No need to download the given file
     */
    public final static class NoNeedToDownloadException extends Exception {

        /**
         * 
         */
        private static final long serialVersionUID = 3959808646783876508L;

        public NoNeedToDownloadException(File f) {
            super();
            this.file = f;
        }

        private final File file;

        public File getFile() {
            return file;
        }
    };

    /**
     * Interface to implement to save files
     * 
     * @copyright Pierre Souchay - 2013,2014
     * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
     * @version $Revision: 3922 $
     * 
     */
    public interface OnFileDownloaded {

        public File onStartDownload(String container, String path, int totalLengh, long lastModified, String eTag)
                throws IOException, NoNeedToDownloadException;

        /**
         * Called when file has been downloaded...
         * 
         * @param f
         * @param success true if download successful
         */
        public void onDownload(File f, String container, String path, boolean success);
    }

    public File getTemporaryDirectory() throws IOException;

    /**
     * The URL separator
     */
    public static final String URL_PATH_SEPARATOR = "/"; //$NON-NLS-1$

    /**
     * Content Type String
     */
    public final static String CONTENT_LENGTH = "Content-Length"; //$NON-NLS-1$

    /**
     * Content Type String
     */
    public final static String CONTENT_TYPE = "Content-Type"; //$NON-NLS-1$

    /**
     * ETAG HEADER
     */
    public final static String ETAG = "etag"; //$NON-NLS-1$

    /**
     * ETAG HEADER
     */
    public final static String DATE = "date"; //$NON-NLS-1$

    /**
     * ETAG HEADER
     */
    public final static String X_OBJECT_MANIFEST = "x-object-manifest"; //$NON-NLS-1$

    /**
     * The Last-Modified header
     */
    public final static String LAST_MODIFIED_HEADER = "Last-Modified"; //$NON-NLS-1$

    /**
     * Connect to system
     * 
     * @throws IOException
     */
    public void connect() throws IOException;

    /**
     * Authenticates to system
     * 
     * @param credentials
     * 
     * @throws IOException
     */
    public void auth() throws IOException;

    /**
     * List roots
     * 
     * @param listener
     * @return List the roots
     * @throws IOException
     */
    public Collection<ObjectIFace> listRoots(HttpConnectionListener listener, FileBuilder fileBuilder)
            throws IOException;

    /**
     * List the given root
     * 
     * @param listener the listener
     * 
     * @param container
     * @return all the files
     * @throws IOException
     */
    public Collection<ObjectIFace> list(HttpConnectionListener listener, FileBuilder container) throws IOException;

    /**
     * GET the given file
     * 
     * @param handler
     * @param container
     * @param file
     * @return the headers
     * @throws IOException
     */
    public Map<String, List<String>> get(SwiftConnectionResultHandler handler, String container, String file,
            OnFileDownloaded onFileDownloaded, long ifNewerThan) throws IOException;

    /**
     * GET the given file
     * 
     * @param handler
     * @param container
     * @param file
     * @return The headers
     * @throws IOException
     */
    public Map<String, List<String>> del(HttpConnectionListener handler, String container, String file)
            throws IOException;

    /**
     * Put the given output stream
     * 
     * @param listener
     * @param container
     * @param len
     * @param file
     * @param in
     * @param headers
     * @return the headers
     * @throws IOException
     */
    public Map<String, List<String>> put(HttpConnectionListener listener, String container, long len, String file,
            InputStream in, Map<String, String> headers) throws IOException;

    /**
     * Put the given output stream
     * 
     * @param listener
     * @param sourceContainer
     * @param sourceFile
     * @param len of file to copy
     * @param destinationContainer
     * @param destinationFile
     * @return the headers
     * @throws IOException
     */
    public Map<String, List<String>> copy(HttpConnectionListener listener, String sourceContainer, String sourceFile,
            String destinationContainer, String destinationFile) throws IOException;

    /**
     * Get the list of fixed containers
     * 
     * @return
     */
    public String[] getFixedContainers();

    /**
     * Return true if using fixed containers
     * 
     * @return true if fixed containers mode is enabled
     */
    public boolean isUsingFixedContainers();
}
