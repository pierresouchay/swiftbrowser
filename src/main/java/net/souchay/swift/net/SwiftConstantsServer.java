/**
 *
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-01-09 16:21:53 +0100 (Jeu 09 jan 2014) $
 */
package net.souchay.swift.net;

/**
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3700 $
 * 
 */
public interface SwiftConstantsServer extends SwiftConstants {

    /**
     * root returned when asking for a token
     */
    public static final String ACCESS_OBJECT = "access"; //$NON-NLS-1$

    /**
     * Service Catalog object
     */
    public static final String SERVICE_CATALOG_OBJECT = "serviceCatalog"; //$NON-NLS-1$

    /**
     * End Points
     */
    public static final String ENDPOINTS_OBJECT = "endpoints"; //$NON-NLS-1$

    /**
     * Public URL
     */
    public static final String PUBLIC_URL = "publicURL"; //$NON-NLS-1$

    /**
     * Service Catalog object
     */
    public static final String TOKEN_OBJECT = "token"; //$NON-NLS-1$

    /**
     * Service Type of object
     */
    public static final String TYPE = "type"; //$NON-NLS-1$

    /**
     * Service name of object
     */
    public static final String NAME = "name"; //$NON-NLS-1$

    /**
     * Service Type of object
     */
    public static final String TYPE_SWIFT_VALUE = "object-store"; //$NON-NLS-1$

    /**
     * Service name of object
     */
    public static final String NAME_SWIFT_VALUE = "swift"; //$NON-NLS-1$

    /**
     * ID
     */
    public static final String ID = "id"; //$NON-NLS-1$

}
