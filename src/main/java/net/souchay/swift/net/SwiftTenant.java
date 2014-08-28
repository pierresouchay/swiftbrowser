package net.souchay.swift.net;

import java.io.Serializable;

/**
 * Swift Tenant connection information
 * 
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3857 $
 * 
 */
public class SwiftTenant implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3838882725189288176L;

    /**
     * get the enabled
     * 
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    public final String id, name, publicUrl, description;

    private final boolean enabled;

    private transient String token;

    private final long expirationtime;

    /**
     * Creates a SwiftTenant
     * 
     * @param id
     * @param name
     * @param publicUrl
     * @param token
     * @param description
     */
    public SwiftTenant(String id, String name, String publicUrl, String token, String description,
            long expirationTimeInLocalTime, boolean enabled) {
        super();
        this.id = id;
        this.name = name;
        this.publicUrl = publicUrl;
        this.token = token;
        this.description = description;
        this.expirationtime = System.currentTimeMillis() + EXPIRATION_DELAY;
        this.enabled = enabled;
    }

    public final static long EXPIRATION_DELAY = 3600000; // One hour by default

    /**
     * ID of tenant
     * 
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * Name of tenant
     * 
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * get the description
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Public URL of tenant
     * 
     * @return
     */
    public String getPublicUrl() {
        return publicUrl;
    }

    /**
     * Token for tenant
     * 
     * @return
     */
    public String getToken() {
        return token;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof SwiftTenant))
            return false;
        SwiftTenant o = (SwiftTenant) other;
        return publicUrl.equals(o.getPublicUrl());
    }

    @Override
    public int hashCode() {
        return publicUrl.hashCode();
    }

    /**
     * Returns true is expiration time of token has been reached
     * 
     * @return true is auth is required again
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > expirationtime;
    }

}
