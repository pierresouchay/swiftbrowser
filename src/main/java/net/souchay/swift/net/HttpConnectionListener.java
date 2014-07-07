/**
 *
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-01-09 16:21:53 +0100 (Jeu 09 jan 2014) $
 */
package net.souchay.swift.net;

import java.io.IOException;

/**
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3700 $
 * 
 */
public interface HttpConnectionListener {

    /**
     * Called when some progress has been made
     * 
     * @param id the id of operation
     * 
     * @param msg The URL
     * @param start the time when it started
     * @param currentBytes the current bytes
     * @param totalBytes the number of bytes
     * @throws IOException
     */
    public void onProgress(int id, String msg, long start, long currentBytes, long totalBytes) throws IOException;

    /**
     * Called when HTTP connection is over
     * 
     * @param id of operation
     */
    public void onClose(int id);
}
