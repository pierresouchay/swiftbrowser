/**
 *
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-01-09 16:21:53 +0100 (Jeu 09 jan 2014) $
 */
package net.souchay.swift.net;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3700 $
 * 
 */
public class Messages {

    private static final String BUNDLE_NAME = "net.souchay.swift.net.messages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    private Messages() {
    }

    /**
     * Get a translated message
     * 
     * @param key the key to translate
     * @return the translated message
     */
    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    /**
     * Get a translated message
     * 
     * @param key the key to translate
     * @param objects
     * @return the translated message
     */
    public static String getString(String key, Object... objects) {
        try {
            return MessageFormat.format(RESOURCE_BUNDLE.getString(key), objects);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
