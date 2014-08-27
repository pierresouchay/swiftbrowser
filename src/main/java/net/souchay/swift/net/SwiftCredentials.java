/**
 *
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-08-27 14:23:17 +0200 (Mer 27 aoû 2014) $
 */
package net.souchay.swift.net;

import java.util.Properties;
import net.souchay.swift.net.SwiftConstantsServer.URL_TYPE;

/**
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3856 $
 * 
 */
public interface SwiftCredentials {

    /**
     * Retrieves the Swift Credential serialized
     * 
     * @return a serialized version of credentials
     */
    public String serialize();

    /**
     * Is autodiscovery enabled ?
     * 
     * @return true if auto-discovers
     */
    public boolean isAutodiscovery();

    /**
     * Clones a credential for given tenant
     * 
     * @param tenantId
     * @return a cloned object with givent tenant id
     */
    public SwiftCredentials cloneForTenantId(String tenantId);

    /**
     * Saves properties
     * 
     * @param p the properties to save to
     */
    public void saveProperties(Properties p);

    /**
     * Get the password
     * 
     * @return The password
     */
    public char[] getPassword();

    /**
     * Set the encoded password
     * 
     * @param value
     */
    public void setCryptedPassword(String value);

    /**
     * Get the encoded password
     * 
     * @return
     */
    public String getCryptedPassword();

    /**
     * Get the username
     * 
     * @return the username
     */
    public String getUser();

    /**
     * Get all the containers if specified
     * 
     * @return String[0] if empty
     */
    public String[] getContainersList();

    /**
     * Get all the containers if specified
     * 
     * @return String[0] if empty
     */
    public String getContainers();

    /**
     * Override Swift URL
     * 
     * @return the overrided SwiftURL
     */
    public String getOverridedSwiftUrl();

    /**
     * Get the URL type
     * 
     * @return the URL type to use
     */
    public URL_TYPE getUrlType();
}
