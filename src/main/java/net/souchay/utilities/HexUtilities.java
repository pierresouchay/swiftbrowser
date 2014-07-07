/**
 *
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-07-04 14:51:32 +0200 (Ven 04 jul 2014) $
 */
package net.souchay.utilities;

import java.util.LinkedList;

/**
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3830 $
 * 
 */
public class HexUtilities {

    /**
     * Parses an Integer from String with value 0x...
     * 
     * @param val The value (should start with 0x)
     * @return The value parsed
     * @throws NumberFormatException if val is null or value cannot be parsed
     */
    public final static int parseHexInt(String val) throws NumberFormatException {
        if (val == null) {
            throw new NumberFormatException("Cannot parse Null Value to get an Hex Value"); //$NON-NLS-1$
        }
        if (val.length() > 2) {
            String pfx = val.substring(0, 2);
            if ("0x".equalsIgnoreCase(pfx)) //$NON-NLS-1$
                val = val.substring(2);
        }
        return Integer.parseInt(val, 16);

    }

    /**
     * Dump a hex string
     * 
     * @param tab
     * @return a hex string
     */
    public final static String toHexString(final byte[] tab) {
        final StringBuilder sb = new StringBuilder(3 * tab.length);
        for (int i = 0; i < tab.length; i++) {
            if (i != 0)
                sb.append(':');
            byte b = tab[i];
            sb.append(hexStr[(b & 0xf0) >> 4]);
            sb.append(hexStr[b & 0x0f]);
        }
        return sb.toString();
    }

    /**
     * Creates a hex representation with padding
     * 
     * @param data
     * @param stringSize
     * @return The hexadecimal representation of with at least stringSize digits
     */
    public final static String toHexString(final int data, final int stringSize) {
        String s = Integer.toHexString(data);
        while (s.length() < stringSize)
            s = '0' + s;
        return s;
    }

    /**
     * Dump a hex string
     * 
     * @param tab
     * @return a hex string
     */
    public final static String toHexStringWithoutSeparator(final byte[] tab) {
        final StringBuilder sb = new StringBuilder(tab.length * 2);
        for (int i = 0; i < tab.length; i++) {
            byte b = tab[i];
            sb.append(hexStr[(b & 0xf0) >> 4]);
            sb.append(hexStr[b & 0x0f]);
        }
        return sb.toString();
    }

    /**
     * Convert a String array of Hex Bytes into a byte array
     * 
     * @param data
     * @return The bytes
     * @throws NumberFormatException
     */
    public final static byte[] fromHexStringToBytes(final String data) throws NumberFormatException {
        final LinkedList<Byte> bytes = new LinkedList<Byte>();
        for (int i = 0; i < data.length(); i++) {
            char c = data.charAt(i);
            if (c == ' ' || c == ':')
                continue;
            byte b = Byte.valueOf(data.substring(i, i + 2), 16);
            i++;
            bytes.add(b);
        }
        final int len = bytes.size();
        final byte[] ret = new byte[len];
        int i = 0;
        for (Byte b : bytes) {
            ret[i++] = b.byteValue();
        }
        return ret;
    }

    private final static char hexStr[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
                                          'f' };
}
