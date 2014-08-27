/**
 *
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-08-27 14:23:17 +0200 (Mer 27 aoû 2014) $
 */
package net.souchay.swift.net;

/**
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3856 $
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

    public static enum URL_TYPE {
        /**
         * Use the publicURL as defined in keystone
         */
        publicURL("publicURL"), //$NON-NLS-1$
        /**
         * Use Internal URL as defined by keystine
         */
        internalUrl("internalURL"), //$NON-NLS-1$
        /**
         * Use adminURL as defined by keystone
         */
        adminUrl("adminURL"), //$NON-NLS-1$
        /**
         * Use Override URL
         */
        overrideUrl("overrideURL"); //$NON-NLS-1$

        /**
         * Constructor
         * 
         * @param type
         */
        private URL_TYPE(String type) {
            this.type = type;
        }

        /**
         * Find a value from its type
         * 
         * @param type
         * @return an URL type, null if not found
         */
        public static URL_TYPE findValue(String type) {
            for (URL_TYPE t : URL_TYPE.values()) {
                if (t.getType().equals(type)) {
                    return t;
                }
            }
            return null;
        }

        /**
         * get the type
         * 
         * @return the type
         */
        public String getType() {
            return type;
        }

        private final String type;
    };

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
