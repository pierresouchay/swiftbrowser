/**
 * 
 */
package net.souchay.swift.downloads;

/**
 * @author pierre
 *
 */
public interface RemoteSpec {

    /**
     * Size of element
     * 
     * @return the size in bytes
     */
    public long size();

    /**
     * Get the last byte as an Unix timestamp in milliseconds
     * 
     * @return the timestamp in milliseconds
     */
    public long lastModified();

    /**
     * Get the eTag of resource aka md5sum
     * 
     * @return the md5sum
     */
    public String eTag();

    /**
     * Get the path of resource
     * 
     * @return the path from the
     */
    public String path();

}
