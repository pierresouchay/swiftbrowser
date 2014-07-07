/**
 *
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-06-05 10:51:11 +0200 (Jeu 05 jui 2014) $
 */
package net.souchay.swift.net;

import java.awt.Component;
import java.util.Arrays;
import java.util.Properties;
import net.souchay.swift.gui.MasterPasswordService;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3813 $
 * 
 */
public class SwiftJSonCredentials implements SwiftCredentials {

    @Override
    public boolean isAutodiscovery() {
        return tenantType == null;
    }

    /**
     * Property
     */
    public final static String PROPERTY_USER = "user"; //$NON-NLS-1$

    /**
     * Property
     */
    public final static String PROPERTY_TENANT_TYPE = "tenantType"; //$NON-NLS-1$

    /**
     * Property
     */
    public final static String PROPERTY_TENANT_VALUE = "tenantValue"; //$NON-NLS-1$

    /**
     * Property
     */
    public final static String PROPERTY_CONTAINERS_VALUE = "containers"; //$NON-NLS-1$

    /**
     * Property name for Overriding Swift URL
     */
    public final static String PROPERTY_OVERRIDED_SWIFT_URL = "overridedSwiftUrl"; //$NON-NLS-1$

    /**
     * Property name
     */
    public final static String PROPERTY_CRYPTED_PASSWORD = "cryptedPassword"; //$NON-NLS-1$

    /**
     * The user
     * 
     * @return the user
     */
    @Override
    public String getUser() {
        return user;
    }

    /**
     * The tenant type
     * 
     * @return the tenant type
     */
    public String getTenantType() {
        return tenantType;
    }

    /**
     * Get the possible tenant types
     * 
     * @return The possible tenant types
     */
    public static String[] getPossibleTenantTypes() {
        return new String[] { null, DEFAULT_TENANT_TYPE, DEFAULT_TENANT_TYPE_ID };
    }

    /**
     * Get the tenant value
     * 
     * @return The tenant value
     */
    public String getTenantValue() {
        return tenantValue;
    }

    /**
     * Get the password
     * 
     * @return the password
     */
    public char[] getPassword(Component c) {
        if (password != null) {
            return password;
        }
        String ePass = getCryptedPassword();
        if (ePass != null) {
            try {
                return MasterPasswordService.getInstance(c).decode(c, ePass);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return password;
    }

    /**
     * Get the password
     * 
     * @return the password
     */
    @Override
    public char[] getPassword() {
        return password;
    }

    private String cryptedPassword;

    private final String overridedSwiftUrl;

    /**
     * Autodiscovery
     * 
     * @param user
     * @param password
     */
    public SwiftJSonCredentials(String user, char[] password) {
        this(user, password, null, null, null, null);
    }

    /**
     * Constructor
     * 
     * @param user
     * @param password
     * @param tenantType
     * @param tenantValue
     */
    public SwiftJSonCredentials(String user, char[] password, String tenantType, String tenantValue, String containers,
            String overridedSwiftUrl) {
        this.user = user;
        this.password = password;
        this.tenantType = tenantType;
        this.tenantValue = tenantValue;
        this.containers = containers;
        this.overridedSwiftUrl = overridedSwiftUrl;
    }

    public final static String DEFAULT_TENANT_TYPE = SwiftConstantsClient.TENANT_NAME;

    public final static String DEFAULT_TENANT_TYPE_ID = "tenantId"; //$NON-NLS-1$

    /**
     * Constructor
     * 
     * @param user
     * @param password
     * @param tenantName
     * @deprecated use {@link #SwiftJSonCredentials(String, char[], String, String)} instead by using
     *             {@link #DEFAULT_TENANT_TYPE} for tenantType
     */
    @Deprecated
    public SwiftJSonCredentials(String user, char[] password, String tenantName, String containers,
            String overridedSwiftUrl) {
        this(user, password, DEFAULT_TENANT_TYPE, tenantName, containers, overridedSwiftUrl);
    }

    /**
     * Constructor using properties
     * 
     * @param p The properties
     */
    public SwiftJSonCredentials(Properties p) {
        this(p.getProperty(PROPERTY_USER),
             new char[0],
             p.getProperty(PROPERTY_TENANT_TYPE),
             p.getProperty(PROPERTY_TENANT_VALUE),
             p.getProperty(PROPERTY_CONTAINERS_VALUE),
             p.getProperty(PROPERTY_OVERRIDED_SWIFT_URL));
        setCryptedPassword(p.getProperty(PROPERTY_CRYPTED_PASSWORD));
    }

    /**
     * Save object as properties
     * 
     * @param p the properties to save to
     */
    @Override
    public void saveProperties(Properties p) {
        p.setProperty(PROPERTY_USER, getUser());
        if (getTenantType() != null) {
            p.setProperty(PROPERTY_TENANT_TYPE, getTenantType());
            p.setProperty(PROPERTY_TENANT_VALUE, getTenantValue());
        }
        {
            final String pw = getCryptedPassword();
            if (pw != null) {
                p.setProperty(PROPERTY_CRYPTED_PASSWORD, pw);
            }
        }
        if (getContainers() != null && !(getContainers().trim().isEmpty())) {
            p.setProperty(PROPERTY_CONTAINERS_VALUE, getContainers());
        }
        if (getOverridedSwiftUrl() != null && getOverridedSwiftUrl().startsWith("http")) { //$NON-NLS-1$
            p.setProperty(PROPERTY_OVERRIDED_SWIFT_URL, getOverridedSwiftUrl());
        }
    }

    /**
     * get the overridedSwiftUrl
     * 
     * @return the overridedSwiftUrl
     */
    @Override
    public String getOverridedSwiftUrl() {
        return overridedSwiftUrl;
    }

    @Override
    public String getCryptedPassword() {
        return cryptedPassword;
    }

    @Override
    public void setCryptedPassword(String cryptedPassword) {
        this.cryptedPassword = cryptedPassword;
    }

    private final String user, tenantType, tenantValue;

    private final String containers;

    private final char[] password;

    @Override
    public String serialize() {
        try {
            JSONObject auth = new JSONObject();
            JSONObject passwordCredentials = new JSONObject();
            passwordCredentials.put(SwiftConstantsClient.USERNAME, user);
            passwordCredentials.put(SwiftConstantsClient.PASSWORD, new String(getPassword(null)));

            auth.put(SwiftConstantsClient.PASSWORD_CREDENTIALS_OBJECT, passwordCredentials);
            if (tenantType != null && tenantValue != null && !(tenantValue.trim().isEmpty()))
                auth.put(tenantType, tenantValue);
            JSONObject obj = new JSONObject();
            obj.put(SwiftConstantsClient.AUTH_OBJECT, auth);
            return obj.toString(1);
        } catch (JSONException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    @Override
    public SwiftJSonCredentials cloneForTenantId(String tenantId) {
        return new SwiftJSonCredentials(user, password, DEFAULT_TENANT_TYPE_ID, tenantId, containers, overridedSwiftUrl);
    }

    /**
     * get the containers
     * 
     * @return the containers
     */
    @Override
    public String[] getContainersList() {
        if (containers == null || containers.trim().isEmpty())
            return new String[0];
        if (containersList == null) {
            String data[] = containers.trim().split(","); //$NON-NLS-1$
            String newData[] = new String[data.length];
            int i = 0;
            for (String s : data) {
                newData[i++] = s.trim();
            }
            this.containersList = newData;
        }
        return Arrays.copyOf(containersList, containersList.length);

    }

    private String[] containersList = null;

    /**
     * get the containers
     * 
     * @return the containers
     */
    @Override
    public String getContainers() {
        return containers;
    }
}
