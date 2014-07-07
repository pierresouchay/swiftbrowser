/**
 *
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-03-01 23:02:54 +0100 (Sam 01 mar 2014) $
 */
package net.souchay.swift.net;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3764 $
 * 
 */
public class HttpDateParser {

    /**
     * US locale - all HTTP dates are in English
     */
    public final static Locale LOCALE_US = Locale.US;

    /**
     * GMT timezone - all HTTP dates are on GMT
     */
    public final static TimeZone GMT_ZONE = TimeZone.getTimeZone("GMT"); //$NON-NLS-1$

    /**
     * format for RFC 1123 date string -- "Sun, 06 Nov 1994 08:49:37 GMT"
     */
    public final static String RFC1123_PATTERN = "EEE, dd MMM yyyyy HH:mm:ss z"; //$NON-NLS-1$

    /**
     * Get a copy of DateFormat
     * 
     * @return the copy
     */
    public final static DateFormat getRfc1123Format() {
        final SimpleDateFormat df = new SimpleDateFormat(RFC1123_PATTERN, LOCALE_US);
        df.setTimeZone(GMT_ZONE);
        return df;
    }
}
