package net.souchay.utilities;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for Cryptography
 * 
 * @author Pierre Souchay <pierre@souchay.net> (last changed by $LastChangedBy: souchay $)
 * @version $Revision: 3830 $
 * 
 */
public class CryptUtilities {

    /**
     * Encodes the array of bytes into a sha256
     * 
     * @param data
     * @return The bytes encoded
     */
    public static final byte[] encodeToSha256(final byte data[]) {
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-256"); //$NON-NLS-1$
            md.update(data, 0, data.length);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convenience function
     * 
     * @param data
     * @return The sha256 String encoded in Hexadecimal
     */
    public static final String encodeToSha256AsString(final byte data[]) {
        return HexUtilities.toHexStringWithoutSeparator(encodeToSha256(data));
    }

}
