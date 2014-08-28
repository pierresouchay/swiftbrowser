/**
 *
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-08-28 11:48:42 +0200 (Jeu 28 aoû 2014) $
 */
package net.souchay.swift.net;

import java.beans.PropertyChangeSupport;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.UUID;

/**
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3857 $
 * 
 */
public class SwiftConfiguration implements Comparable<SwiftConfiguration> {

    private String id = UUID.randomUUID().toString();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        String oldValue = this.id;
        this.id = id;
        support.firePropertyChange(PROPERTY_ID, oldValue, id);
    }

    private String name = "default"; //$NON-NLS-1$

    public String getName() {
        return name;
    }

    public void setName(String name) {
        String oldValue = this.name;
        this.name = name;
        support.firePropertyChange(PROPERTY_NAME, oldValue, name);
    }

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    private SwiftCredentials credential;

    private String tokenUrlAsString;

    private URL tokenUrlAsUrl;

    /**
     * get the credential
     * 
     * @return the credential
     */
    public SwiftCredentials getCredential() {
        return credential;
    }

    /**
     * Set the credential
     * 
     * @param credential the credential to set
     */
    public void setCredential(SwiftCredentials credential) {
        final SwiftCredentials oldValue = this.credential;
        this.credential = credential;
        support.firePropertyChange(PROPERTY_CREDENTIAL, oldValue, credential);
    }

    /**
     * get the tokenUrl
     * 
     * @return the tokenUrl
     */
    public URL getTokenUrlAsUrl() {
        return tokenUrlAsUrl;
    }

    /**
     * get the tokenUrl
     * 
     * @return the tokenUrl
     */
    public String getTokenUrlAsString() {
        return tokenUrlAsString;
    }

    /**
     * Property name fired when changing value
     */
    public final static String PROPERTY_CREDENTIAL = "credential"; //$NON-NLS-1$

    /**
     * Property name fired when changing value
     */
    public final static String PROPERTY_TOKEN_URL = "tokenUrl"; //$NON-NLS-1$

    /**
     * Property name fired when changing value
     */
    public final static String PROPERTY_ID = "id"; //$NON-NLS-1$

    /**
     * Property name fired when changing value
     */
    public final static String PROPERTY_NAME = "name"; //$NON-NLS-1$

    /**
     * Set the tokenUrl
     * 
     * @param tokenUrl the tokenUrl to set
     */
    public void setTokenUrl(URL tokenUrl) {
        final String newValue = tokenUrl == null ? null : tokenUrl.toExternalForm();
        final String oldValue = this.tokenUrlAsString;
        this.tokenUrlAsUrl = tokenUrl;
        this.tokenUrlAsString = newValue;
        support.firePropertyChange(PROPERTY_TOKEN_URL, oldValue, newValue);
    }

    /**
     * @param credential
     * @param tokenUrl
     */
    public SwiftConfiguration(SwiftCredentials credential, URL tokenUrl) {
        super();
        this.credential = credential;
        setTokenUrl(tokenUrl);
    }

    /**
     * Construcutor
     * 
     * @param p
     * @throws MalformedURLException
     */
    public SwiftConfiguration(Properties p) throws MalformedURLException {
        String u = p.getProperty(PROPERTY_TOKEN_URL);
        setTokenUrl(new URL(u));
        this.id = p.getProperty(PROPERTY_ID);
        this.name = p.getProperty(PROPERTY_NAME, u);
        this.credential = new SwiftJSonCredentials(p);
    }

    /**
     * Save as properties
     * 
     * @param p
     */
    public void saveProperties(Properties p) {
        p.setProperty(PROPERTY_TOKEN_URL,
                      getTokenUrlAsString() == null ? "http://example.com/v2/tokens" : getTokenUrlAsString()); //$NON-NLS-1$
        p.setProperty(PROPERTY_ID, getId());
        p.setProperty(PROPERTY_NAME, getName());
        this.credential.saveProperties(p);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(SwiftConfiguration o) {
        if (o == null) {
            return 1;
        }
        return getName().compareTo(o.getName());
    }

}
