/**
 *
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-01-09 16:21:53 +0100 (Jeu 09 jan 2014) $
 */
package net.souchay.swift.gui;

import java.util.Map;
import net.souchay.swift.gui.ContainerIFace.FileIFace;

/**
 * 
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3700 $
 * 
 */
public interface RefreshFile {

    /**
     * @param listener
     * @param file
     */
    void refresh(RefreshInfoListener listener, FileIFace file);

    /**
     * Refresh information
     * 
     * @copyright Pierre Souchay - 2013,2014
     * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
     * @version $Revision: 3700 $
     * 
     */
    public static interface RefreshInfoListener {

        /**
         * Called when file has been refreshed
         * 
         * @param file
         * @param lastModified
         * @param size
         * @param metaData
         */
        public void onRefreshComplete(FileIFace file, long lastModified, long size, Map<String, String> metaData);
    }

}
