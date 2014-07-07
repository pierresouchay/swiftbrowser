/**
 *
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-01-09 16:21:53 +0100 (Jeu 09 jan 2014) $
 */
package net.souchay.swift.net;

import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3700 $
 * 
 */
public interface SwiftConnectionResultHandler extends HttpConnectionListener {

    /**
     * Called when file content is available
     * 
     * @param id
     * 
     * @param listener
     * @param connection
     * @param out
     * @param container
     * @param path
     * @param onDownload
     */
    void onFileContent(int id, HttpConnectionListener listener, HttpURLConnection connection, InputStream out,
            String container, String path, FsConnection.OnFileDownloaded onDownload);
}
