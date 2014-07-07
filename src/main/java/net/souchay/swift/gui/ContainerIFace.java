/**
 *
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-05-23 17:18:40 +0200 (Ven 23 mai 2014) $
 */
package net.souchay.swift.gui;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3810 $
 * 
 */
public interface ContainerIFace extends Comparable<ContainerIFace>, ObjectIFace {

    /**
     * Get the name of container
     * 
     * @return the name of container
     */
    @Override
    public String getName();

    /**
     * Interface implements by files contained within Container
     * 
     * @copyright Pierre Souchay - 2013,2014
     * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
     * @version $Revision: 3810 $
     * 
     */
    public static interface FileIFace extends ObjectIFace,
            ElementChangedListener.ElementChangedListenerRegistration<FileIFace> {

        /**
         * Get the meta data associated with file
         * 
         * @return The meta data associated
         */
        Map<String, String> getMetaData();

        /**
         * Get the canonical name of file
         * 
         * @return the container
         */
        public ContainerIFace getContainer();

        /**
         * true if file is an large object
         */
        public boolean isLargeObject();

        /**
         * Return the content type
         * 
         * @return the content type
         */
        public String getContentType();

        /**
         * Updates HTTP Headers
         * 
         * @param newHeaders
         */
        public void setHeaders(Map<String, List<String>> newHeaders);
    }

    /**
     * Implement this for being notified when files have been added/removed
     * 
     * @copyright Pierre Souchay - 2013,2014
     * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
     * @version $Revision: 3810 $
     * 
     */
    public static interface FileListener {

        void onContainerMetaDataChanged(ContainerIFace source);

        /**
         * Files have been added
         * 
         * @param source the source of container
         * @param filesAdded the files added
         */
        void onFilesAdded(ContainerIFace source, Collection<? extends FileIFace> filesAdded);

        /**
         * Called when files have been removed
         * 
         * @param source the source of event
         * @param filesAdded
         */
        void onFilesRemoved(ContainerIFace source, Collection<? extends FileIFace> filesAdded);

        // /**
        // * Called when files are modified
        // *
        // * @param source
        // * @param filesModified
        // */
        // void onFilesModified(ContainerIFace source, Collection<? extends FileIFace> filesModified);
    }

    /**
     * Adds a File Listener
     * 
     * @param listener
     */
    public void addFileListener(FileListener listener);

    /**
     * Adds a File Listener
     * 
     * @param listener
     */
    public void removeFileListener(FileListener listener);

    /**
     * Get all the files
     * 
     * @return the files
     */
    public Collection<? extends FileIFace> getFiles();

    /**
     * Get the URL of Container
     * 
     * @return The URL of Container
     */
    public String getUrl();

    /**
     * Get the number of files into the container
     * 
     * @return the number of files
     */
    public long getNumberOfFiles();

    /**
     * Get the number of bytes into the container
     * 
     * @return the number of bytes
     */
    public long getNumberOfBytes();

    /**
     * Get all headers
     * 
     * @return the headers
     */
    public Map<String, String> getHeaders();

    /**
     * Returns true if the container is public
     * 
     * @return true if public
     */
    public boolean isShared();

    /**
     * get the webListingEnabled
     * 
     * @return the webListingEnabled
     */
    public boolean isWebListingEnabled();
}
