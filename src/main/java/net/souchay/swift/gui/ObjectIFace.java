package net.souchay.swift.gui;

import java.util.Map;

public interface ObjectIFace {

    /**
     * The name
     * 
     * @return
     */
    public String getName();

    /**
     * Get the UNIX last modified
     * 
     * @return the UNIX timestamp
     */
    public long getLastModified();

    /**
     * Set the last modified
     * 
     * @param lastModified
     * @return true if value has been modified
     */
    public boolean setLastModified(long lastModified);

    /**
     * Set the size in bytes
     * 
     * @param size the size
     * @return true if modified
     */
    public boolean setSize(long size);

    /**
     * Set the content-type of Object
     * 
     * @param contentType
     */
    public void setContentType(String contentType);

    /**
     * Updates the meta data
     * 
     * @param newMeta
     * @return true if meta data has been modified
     */
    public boolean setMetaData(final Map<String, String> newMeta);

    /**
     * The size in bytes of object
     * 
     * @return the size in bytes
     */
    public long getSize();

}
