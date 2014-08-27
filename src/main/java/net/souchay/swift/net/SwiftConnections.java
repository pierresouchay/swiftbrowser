/**
 *
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-08-27 14:23:17 +0200 (Mer 27 aoû 2014) $
 */
package net.souchay.swift.net;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import net.souchay.swift.gui.ContainerIFace.FileIFace;
import net.souchay.swift.gui.FileBuilder;
import net.souchay.swift.gui.ObjectIFace;
import net.souchay.swift.gui.VirtualFile;
import net.souchay.swift.gui.table.ConnectionLog;
import net.souchay.swift.gui.table.ConnectionsTableModel;
import net.souchay.swift.net.SwiftConstantsServer.URL_TYPE;
import net.souchay.utilities.CryptUtilities;
import net.souchay.utilities.HexUtilities;
import net.souchay.utilities.InputStreamWithProgress;
import net.souchay.utilities.URLParamEncoder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3856 $
 * 
 */
public class SwiftConnections implements FsConnection {

    volatile JSONObject swiftInformation = new JSONObject();

    {
        try {
            swiftInformation = new JSONObject("{\"error\":\"No information available\"}"); //$NON-NLS-1$
        } catch (JSONException err) {
            LOG.log(Level.SEVERE, "Cannot Parse JSON Data", err); //$NON-NLS-1$
        }
    }

    /**
     * Get SwiftInformation
     * 
     * @return The SwiftInformation
     */
    public JSONObject getSwiftInformation() {
        return swiftInformation;
    }

    private final static Logger LOG = Logger.getLogger("swift"); //$NON-NLS-1$

    private final AtomicInteger idGenerator = new AtomicInteger();

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    private volatile SwiftTenant tenant;

    private final ConnectionsTableModel connectionsTableModel = new ConnectionsTableModel();

    private final String userAgent;

    private class ConnectionBuilder {

        private final int idx;

        ConnectionBuilder(URL url, String method) {
            log = new ConnectionLog(method, url, new Date());
            idx = connectionsTableModel.addRow(log) - 1;
        }

        public HttpURLConnection buildConnection() throws IOException {
            try {
                HttpURLConnection c = (HttpURLConnection) log.getUrl().openConnection();
                c.setRequestMethod(log.getMethod());
                c.setRequestProperty("User-Agent", userAgent); //$NON-NLS-1$
                c.setReadTimeout(30000);
                c.setConnectTimeout(30000);
                return c;
            } catch (IOException err) {
                onError(err);
                throw err;
            }
        }

        /**
         * To call when an error did occured
         * 
         * @param t the error
         */
        public void onError(Throwable t) {
            log.updateWithError(t);
            connectionsTableModel.fireRowChanged(idx);
        }

        public void onSuccess(int httpResponseCode, String msg, long len) {
            log.updateWithSuccess(httpResponseCode, len, msg);
            connectionsTableModel.fireRowChanged(idx);
        }

        private final ConnectionLog log;
    }

    public ConnectionsTableModel getConnectionsTableModel() {
        return connectionsTableModel;
    }

    private ConnectionBuilder startConnection(URL connection, String method) {
        ConnectionBuilder log = new ConnectionBuilder(connection, method);

        return log;
    }

    @SuppressWarnings("nls")
    final static String[] hex = { "%00", "%01", "%02", "%03", "%04", "%05", "%06", "%07", "%08", "%09", "%0a", "%0b",
                                 "%0c", "%0d", "%0e", "%0f", "%10", "%11", "%12", "%13", "%14", "%15", "%16", "%17",
                                 "%18", "%19", "%1a", "%1b", "%1c", "%1d", "%1e", "%1f", "%20", "%21", "%22", "%23",
                                 "%24", "%25", "%26", "%27", "%28", "%29", "%2a", "%2b", "%2c", "%2d", "%2e", "%2f",
                                 "%30", "%31", "%32", "%33", "%34", "%35", "%36", "%37", "%38", "%39", "%3a", "%3b",
                                 "%3c", "%3d", "%3e", "%3f", "%40", "%41", "%42", "%43", "%44", "%45", "%46", "%47",
                                 "%48", "%49", "%4a", "%4b", "%4c", "%4d", "%4e", "%4f", "%50", "%51", "%52", "%53",
                                 "%54", "%55", "%56", "%57", "%58", "%59", "%5a", "%5b", "%5c", "%5d", "%5e", "%5f",
                                 "%60", "%61", "%62", "%63", "%64", "%65", "%66", "%67", "%68", "%69", "%6a", "%6b",
                                 "%6c", "%6d", "%6e", "%6f", "%70", "%71", "%72", "%73", "%74", "%75", "%76", "%77",
                                 "%78", "%79", "%7a", "%7b", "%7c", "%7d", "%7e", "%7f", "%80", "%81", "%82", "%83",
                                 "%84", "%85", "%86", "%87", "%88", "%89", "%8a", "%8b", "%8c", "%8d", "%8e", "%8f",
                                 "%90", "%91", "%92", "%93", "%94", "%95", "%96", "%97", "%98", "%99", "%9a", "%9b",
                                 "%9c", "%9d", "%9e", "%9f", "%a0", "%a1", "%a2", "%a3", "%a4", "%a5", "%a6", "%a7",
                                 "%a8", "%a9", "%aa", "%ab", "%ac", "%ad", "%ae", "%af", "%b0", "%b1", "%b2", "%b3",
                                 "%b4", "%b5", "%b6", "%b7", "%b8", "%b9", "%ba", "%bb", "%bc", "%bd", "%be", "%bf",
                                 "%c0", "%c1", "%c2", "%c3", "%c4", "%c5", "%c6", "%c7", "%c8", "%c9", "%ca", "%cb",
                                 "%cc", "%cd", "%ce", "%cf", "%d0", "%d1", "%d2", "%d3", "%d4", "%d5", "%d6", "%d7",
                                 "%d8", "%d9", "%da", "%db", "%dc", "%dd", "%de", "%df", "%e0", "%e1", "%e2", "%e3",
                                 "%e4", "%e5", "%e6", "%e7", "%e8", "%e9", "%ea", "%eb", "%ec", "%ed", "%ee", "%ef",
                                 "%f0", "%f1", "%f2", "%f3", "%f4", "%f5", "%f6", "%f7", "%f8", "%f9", "%fa", "%fb",
                                 "%fc", "%fd", "%fe", "%ff" };

    /**
     * Escape URI parameters
     * 
     * @param input the parameters to escape
     * @return the escaped path
     */
    public final static String escapeURIPathParam2(final String input) {
        StringBuilder sbuf = new StringBuilder();
        int len = input.length();
        for (int i = 0; i < len; i++) {
            int ch = input.charAt(i);
            if ('A' <= ch && ch <= 'Z') { // 'A'..'Z'
                sbuf.append((char) ch);
            } else if ('a' <= ch && ch <= 'z') { // 'a'..'z'
                sbuf.append((char) ch);
            } else if ('0' <= ch && ch <= '9') { // '0'..'9'
                sbuf.append((char) ch);
            } else if (ch == ' ') { // space
                sbuf.append('+');
            } else if (ch == '-' || ch == '_' // unreserved
                       || ch == '.' || ch == '!' || ch == '~' || ch == '*' || ch == '\'' || ch == '(' || ch == ')') {
                sbuf.append((char) ch);
            } else if (ch <= 0x007f) { // other ASCII
                sbuf.append(hex[ch]);
            } else if (ch <= 0x07FF) { // non-ASCII <= 0x7FF
                sbuf.append(hex[0xc0 | (ch >> 6)]);
                sbuf.append(hex[0x80 | (ch & 0x3F)]);
            } else { // 0x7FF < ch <= 0xFFFF
                sbuf.append(hex[0xe0 | (ch >> 12)]);
                sbuf.append(hex[0x80 | ((ch >> 6) & 0x3F)]);
                sbuf.append(hex[0x80 | (ch & 0x3F)]);
            }
        }
        return sbuf.toString();

    }

    /**
     * Escape URI params
     * 
     * @param input
     * @return
     */
    public final static String escapeURIPathParam(final String input) {
        final StringBuilder resultStr = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (isUnsafe(ch)) {
                resultStr.append('%');
                resultStr.append(toHex(ch / 16));
                resultStr.append(toHex(ch % 16));
            } else {
                resultStr.append(ch);
            }
        }
        return resultStr.toString();
    }

    private final static char toHex(final int ch) {
        return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
    }

    private final static boolean isUnsafe(final char ch) {
        return " %$&+,:;=?@<>#%".indexOf(ch) >= 0; //$NON-NLS-1$
    }

    /**
     * Property for Temp URLs
     */
    public final static String X_ACCOUNT_META_TEMP_URL_KEY = "X-Account-Meta-Temp-Url-Key"; //$NON-NLS-1$

    public final static String X_ACCOUNT_META_TEMP_URL_KEY_2 = "X-Account-Meta-Temp-Url-Key-2"; //$NON-NLS-1$

    public final static String X_ACCOUNT_PROPERTIES_INT[] = { "X-Account-Bytes-Used", //$NON-NLS-1$
                                                             "X-Account-Meta-Quota-Bytes", "X-Account-Container-Count", //$NON-NLS-1$ //$NON-NLS-2$
                                                             "X-Account-Object-Count" }; //$NON-NLS-1$};

    public final static String X_ACCOUNT_PROPERTIES_STR[] = { X_ACCOUNT_META_TEMP_URL_KEY,
                                                             X_ACCOUNT_META_TEMP_URL_KEY_2 };

    private Map<String, String> accountProperties = new Hashtable<String, String>();

    /**
     * get the accountProperties
     * 
     * @return the accountProperties
     */
    public Map<String, String> getAccountProperties() {
        return accountProperties;
    }

    /**
     * Set an Account property
     * 
     * @param property the property to set
     * @param value the value to set
     */
    private void setAccountProperty(String property, String value) {
        final String oldValue = accountProperties.remove(property);
        if (value != null)
            accountProperties.put(property, value);
        support.firePropertyChange(property, oldValue, value);
    }

    /**
     * Get an account property
     * 
     * @param property The property to get
     * @return The property
     */
    public String getAccountProperty(String property) {
        return accountProperties.get(property);
    }

    /**
     * Get an account property
     * 
     * @param property The property to get
     * @return The property
     */
    public Long getAccountPropertyAsLong(String property) {
        final String x = accountProperties.get(property);
        if (x == null)
            return null;
        try {
            return Long.parseLong(x);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Adds a property change Listener
     * 
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    /**
     * Removes a property change listener
     * 
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    static final Charset CHARSET_UTF8 = Charset.forName("UTF-8"); //$NON-NLS-1$

    /**
     * Convenience method
     * 
     * @param method
     * @param expires
     * @param f
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    public URI generateTempUrl(String method, long expires, VirtualFile f, boolean useSecondaryKey)
            throws MalformedURLException, IOException, InvalidKeyException {
        return generateTempUrl(method, expires, f.getFile(), useSecondaryKey);
    }

    /**
     * Convenience method
     * 
     * @param method
     * @param expires
     * @param f
     * @param useSecondaryKey to use the secondary key instead of first one
     * @return
     * @throws MalformedURLException
     * @throws IOException
     * @throws InvalidKeyException if secret key has not been set
     */
    public URI generateTempUrl(String method, long expires, FileIFace f, boolean useSecondaryKey)
            throws MalformedURLException, IOException, InvalidKeyException {
        return generateTempUrl(method,
                               expires,
                               f.getContainer() + FsConnection.URL_PATH_SEPARATOR + f.getName(),
                               useSecondaryKey);
    }

    /**
     * Generates a temporary URL
     * 
     * @param method
     * @param expires
     * @param path a temporary URL
     * @return
     */
    public URI generateTempUrl(String method, long expires, String rpath, boolean useSecondaryKey)
            throws MalformedURLException, IOException, InvalidKeyException {
        URL u0 = new URL(getTenant().getPublicUrl());
        String path = u0.getPath() + URL_PATH_SEPARATOR + rpath;
        final StringBuilder sb = new StringBuilder();
        sb.append(method).append('\n').append(expires).append('\n').append(path);
        try {
            StringBuilder query = new StringBuilder().append("temp_url_sig=") //$NON-NLS-1$
                                                     .append(getSignature(sb.toString(), useSecondaryKey))
                                                     .append("&temp_url_expires=") //$NON-NLS-1$
                                                     .append(expires);
            URI u = u0.toURI();
            return new URI(new URI(u.getScheme(),
                                   u.getUserInfo(),
                                   u.getHost(),
                                   u.getPort(),
                                   path,
                                   query.toString(),
                                   null).toASCIIString());
        } catch (URISyntaxException e) {
            throw new IOException("Error while generating temporary URI: " + e.getLocalizedMessage(), e); //$NON-NLS-1$
        }
    }

    public String getSignature(String data, boolean useSecondayKey) throws InvalidKeyException {
        try {
            final String algorithm = "HmacSHA1"; //$NON-NLS-1$
            String secretKey = useSecondayKey ? getX_account_meta_temp_url_key2() : getX_account_meta_temp_url_key();
            if (secretKey == null || secretKey.trim().isEmpty()) {
                // If first key is empty, we try the second one
                secretKey = useSecondayKey ? getX_account_meta_temp_url_key() : getX_account_meta_temp_url_key2();
                if (secretKey == null || secretKey.trim().isEmpty()) {
                    throw new InvalidKeyException("Properties " + X_ACCOUNT_META_TEMP_URL_KEY + " and " + X_ACCOUNT_META_TEMP_URL_KEY_2 //$NON-NLS-1$ //$NON-NLS-2$
                                                  + " do not exist, cannot create a signature!"); //$NON-NLS-1$
                }
            }
            final SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(CHARSET_UTF8), algorithm);
            Mac mac;
            mac = Mac.getInstance(algorithm);
            mac.init(keySpec);
            byte[] result = mac.doFinal(data.getBytes(CHARSET_UTF8));
            return HexUtilities.toHexStringWithoutSeparator(result);
        } catch (NoSuchAlgorithmException err) {
            throw new InvalidKeyException(err);
        }
    }

    public String generateSignatureForPostUpload(String path, String redirectUrl, long max_file_size,
            int max_file_count, long expires, boolean useSecondayKey) throws InvalidKeyException {
        StringBuilder sb = new StringBuilder();
        sb.append(path)
          .append('\n')
          .append(redirectUrl)
          .append('\n')
          .append(max_file_size)
          .append('\n')
          .append(max_file_count)
          .append('\n')
          .append(expires);
        return getSignature(sb.toString(), useSecondayKey);
    }

    /**
     * Get the tenant
     * 
     * @return
     * @throws IOException
     */
    public SwiftTenant getTenant() throws IOException {
        if (tenant == null || tenant.isExpired()) {
            tenant = authenticate(configuration);
        }
        return tenant;
    }

    private final SwiftConfiguration configuration;

    @Override
    public boolean isUsingFixedContainers() {
        return (configuration.getCredential().getContainersList().length > 0);
    }

    @Override
    public String[] getFixedContainers() {
        return configuration.getCredential().getContainersList();
    }

    /**
     * Constructor
     * 
     * @param configuration
     */
    public SwiftConnections(final String userAgent, SwiftConfiguration configuration) {
        this(userAgent, configuration, null);
    }

    /**
     * Constructor
     * 
     * @param configuration
     * @param tenant
     */
    public SwiftConnections(String userAgent, SwiftConfiguration configuration, SwiftTenant tenant) {
        this.userAgent = userAgent;
        this.configuration = configuration;
        this.tenant = tenant;
    }

    /**
     * POST Method
     */
    public final static String HTTP_METHOD_POST = "POST"; //$NON-NLS-1$

    /**
     * PUT Method
     */
    public final static String HTTP_METHOD_PUT = "PUT"; //$NON-NLS-1$

    /**
     * X-Auth-Token
     */
    public final static String X_AUTH_TOKEN = "X-Auth-Token"; //$NON-NLS-1$

    /**
     * Accept Header
     */
    public final static String ACCEPT = "Accept"; //$NON-NLS-1$

    /**
     * Accept All Header Value
     */
    public final static String ACCEPT_ALL = "*/*"; //$NON-NLS-1$

    /**
     * Content-Type value for JSON
     */
    public final static String CONTENT_TYPE_JSON = "application/json"; //$NON-NLS-1$

    /**
     * 
     */
    public final static String CONTENT_TYPE_FORM_URL_ENCODED = "application/x-www-form-urlencoded"; //$NON-NLS-1$

    private final static Charset PASSWORD_ENCODING = CHARSET_UTF8; // UTF-8

    /**
     * Authenticates
     * 
     * @throws IOException
     */
    @Override
    public void auth() throws IOException {
        if (tenant == null || tenant.getPublicUrl() == null) {
            this.tenant = authenticate(configuration);
        }
    }

    private static BufferedReader openBufferedReader(HttpURLConnection connection) throws IOException {
        final String contentEncoding = connection.getHeaderField("Content-Encoding"); //$NON-NLS-1$
        if ("gzip".equalsIgnoreCase(contentEncoding)) { //$NON-NLS-1$
            return new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream()),
                                                            CHARSET_UTF8));
        } else if ("deflate".equalsIgnoreCase(contentEncoding)) { //$NON-NLS-1$
            return new BufferedReader(new InputStreamReader(new InflaterInputStream(connection.getInputStream()),
                                                            CHARSET_UTF8));
        } else {
            return new BufferedReader(new InputStreamReader(connection.getInputStream(), CHARSET_UTF8));
        }
    }

    /**
     * Authenticates
     * 
     * @throws IOException
     */
    public static SwiftTenant authenticate(SwiftConfiguration configuration) throws IOException {
        HttpURLConnection connection = null;
        LOG.fine("Starting authentication..."); //$NON-NLS-1$
        try {
            final String urlParameters = configuration.getCredential().serialize();
            connection = (HttpURLConnection) configuration.getTokenUrlAsUrl().openConnection();
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(31000);
            // connection.setRequestProperty(CONTENT_TYPE, );
            connection.setRequestMethod(HTTP_METHOD_POST);
            connection.setRequestProperty(CONTENT_TYPE, CONTENT_TYPE_JSON);
            connection.setRequestProperty(ACCEPT, CONTENT_TYPE_JSON);
            final byte passwordBytes[] = urlParameters.getBytes(PASSWORD_ENCODING);
            connection.setRequestProperty(CONTENT_LENGTH, String.valueOf(passwordBytes.length));
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setAllowUserInteraction(false);
            Map<String, List<String>> headers = connection.getRequestProperties();
            connection.setFixedLengthStreamingMode(passwordBytes.length);
            // Send request
            DataOutputStream wr = new DataOutputStream(new BufferedOutputStream(connection.getOutputStream(),
                                                                                passwordBytes.length));
            wr.write(passwordBytes);
            wr.flush();
            wr.close();
            if (200 != connection.getResponseCode()) {
                StringBuilder sb = new StringBuilder();
                sb.append(configuration.getTokenUrlAsString()).append(": ").append(connection.getResponseCode()) //$NON-NLS-1$
                  .append(": ").append(connection.getResponseMessage()); //$NON-NLS-1$ 
                sb.append("\n-- Post\n").append(urlParameters); //$NON-NLS-1$
                sb.append("\n-- Request"); //$NON-NLS-1$
                for (Map.Entry<String, List<String>> m : headers.entrySet()) {
                    for (String s : m.getValue()) {
                        sb.append("\n\t").append(m.getKey()).append(": ").append(s); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
                sb.append("\n-- Response"); //$NON-NLS-1$
                headers = connection.getHeaderFields();
                for (Map.Entry<String, List<String>> m : headers.entrySet()) {
                    for (String s : m.getValue()) {
                        sb.append("\n\t").append(m.getKey()).append(": ").append(s); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
                if (LOG.isLoggable(Level.FINE))
                    LOG.fine(sb.toString());
            }
            // Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line).append('\n');
                if (LOG.isLoggable(Level.FINER))
                    LOG.finer(line);
            }
            rd.close();
            try {
                return extractInformation(configuration.getCredential(), response.toString());
            } catch (JSONException ex) {
                throw new IOException(ex);
            }
        } catch (IOException e) {
            throw e;
        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Extract given listing
     * 
     * @param data
     * @throws IOException
     * @throws JSONException
     */
    public static SwiftTenant extractInformation(SwiftCredentials credentials, String data) throws IOException,
            JSONException {
        LOG.info(data);
        String val = data;
        JSONObject obj = new JSONObject(val);
        JSONObject root = obj.getJSONObject(SwiftConstantsServer.ACCESS_OBJECT);
        JSONObject tokenObj = root.getJSONObject(SwiftConstantsServer.TOKEN_OBJECT);

        final String token = tokenObj.getString(SwiftConstantsServer.ID);
        String name = null;
        String id = null;
        String description = null;
        long expirationTimeForTenant = System.currentTimeMillis() + 3600000;
        try {
            JSONObject tenant = tokenObj.getJSONObject("tenant"); //$NON-NLS-1$
            if (tenant != null) {
                id = tenant.getString(SwiftConstantsServer.ID);
                name = tenant.getString(SwiftConstantsServer.NAME);
                description = tenant.getString("description"); //$NON-NLS-1$
            }
            DateFormat df = getDateFormatForMicroseconds();
            try {
                long issued_at = parseUTCTimeWithMicroseconds(df, tokenObj.getString("issued_at")); //$NON-NLS-1$
                long diff = System.currentTimeMillis() - issued_at + 60000; // We apply a security margin of 1
                                                                            // minute
                String expiresString = tokenObj.getString("expires"); //$NON-NLS-1$
                long expires = df.parse(expiresString).getTime();
                if (LOG.isLoggable(Level.FINE))
                    LOG.fine("Expires real=" + expiresString + " parsed=" + df.format(expires) + " computed=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                             + df.format(expires - diff));
                expirationTimeForTenant = expires - diff;
            } catch (ParseException err) {
                LOG.log(Level.SEVERE, "Failed to parse expiration time in " + tokenObj.toString(), err); //$NON-NLS-1$
            }
        } catch (JSONException ignored) {
            LOG.fine("Missing tenant, assuming root token"); //$NON-NLS-1$
        }
        if (description == null || description.trim().isEmpty())
            description = name;
        String publicURLForSwift = null;
        String urlType = SwiftConstantsServer.URL_TYPE.publicURL.getType();
        if (credentials != null) {
            urlType = credentials.getUrlType().getType();
        }
        if (credentials != null && urlType.equals(URL_TYPE.overrideUrl.getType())
            && credentials.getOverridedSwiftUrl() != null) {
            publicURLForSwift = credentials.getOverridedSwiftUrl();
            LOG.info("Overriding Swift URL : " + publicURLForSwift); //$NON-NLS-1$
        } else {
            JSONArray endpoints = (JSONArray) extractValue(root, SwiftConstantsServer.SERVICE_CATALOG_OBJECT);
            for (int i = 0; i < endpoints.length(); i++) {
                Object ox = endpoints.get(i);
                if (ox instanceof JSONObject) {
                    JSONObject endPoint = (JSONObject) ox;
                    if (SwiftConstantsServer.NAME_SWIFT_VALUE.equalsIgnoreCase(endPoint.getString(SwiftConstantsServer.NAME))
                        && SwiftConstantsServer.TYPE_SWIFT_VALUE.equals(endPoint.getString(SwiftConstantsServer.TYPE))) {
                        JSONArray endPoints = endPoint.getJSONArray(SwiftConstantsServer.ENDPOINTS_OBJECT);
                        for (int j = 0; j < endPoints.length(); j++) {
                            JSONObject cur = (JSONObject) endPoints.get(j);
                            try {
                                publicURLForSwift = cur.getString(urlType);
                                if (publicURLForSwift != null && !publicURLForSwift.trim().isEmpty()) {
                                    LOG.info("Found " + urlType + "=" + publicURLForSwift); //$NON-NLS-1$ //$NON-NLS-2$
                                    break;
                                }
                            } catch (JSONException ignoredPathNotFound) {
                            }
                        }
                        if (publicURLForSwift == null) {
                            throw new IOException("Could not find URL " + urlType + " in:\n'" + endPoint.toString(2) //$NON-NLS-1$//$NON-NLS-2$
                                                  + "'"); //$NON-NLS-1$
                        }
                    }
                }
            }
        }
        return new SwiftTenant(id, name, publicURLForSwift, token, description, expirationTimeForTenant);
    }

    @Override
    public Collection<ObjectIFace> listRoots(HttpConnectionListener listener, final FileBuilder fb) throws IOException {
        FileBuilder fileBuilder = new FileBuilder() {

            @Override
            public String getRelativePathFromRootEscapedAsURI() {
                return ""; //$NON-NLS-1$
            }

            @Override
            public ObjectIFace createFile(String name) {
                return fb.createFile(name);
            }

            @Override
            public void setHeaders(Map<String, List<String>> headers) {
                for (Map.Entry<String, List<String>> en : headers.entrySet()) {
                    if (en.getKey() != null) {
                        String v;
                        List<String> currentValue = en.getValue();
                        if (currentValue.isEmpty())
                            v = ""; //$NON-NLS-1$
                        else
                            v = currentValue.get(0);
                        setAccountProperty(en.getKey(), v);
                    }
                }
            }
        };
        Collection<ObjectIFace> containers;
        if (isUsingFixedContainers()) {
            String[] conts = getFixedContainers();
            List<ObjectIFace> daConts = new ArrayList<ObjectIFace>(conts.length);
            for (String s : conts) {
                s = s.trim().intern();
                daConts.add(fileBuilder.createFile(s));
            }
            containers = daConts;
        } else {
            containers = list(listener, fileBuilder);
        }
        return containers;
    }

    /**
     * Get the headers
     * 
     * @param u the URL Connection
     * @return the headers as a string
     */
    public final static String headersToString(URLConnection u) {
        StringBuilder sb = new StringBuilder();
        sb.append("-- Response: ").append(u.getHeaderField(null)); //$NON-NLS-1$
        for (Map.Entry<String, List<String>> en : u.getHeaderFields().entrySet()) {
            String header = en.getKey();
            if (header != null) {
                sb.append("\n ").append(en.getKey()).append(':'); //$NON-NLS-1$
                for (String s : en.getValue()) {
                    sb.append(' ').append(s);
                }
            }
        }
        return sb.toString();
    }

    private static final long getContentLength(final URLConnection u) {
        try {
            return u.getContentLengthLong();
        } catch (NoSuchMethodError err) {
            return u.getContentLength();
        }
    }

    private static final long parseUTCTimeWithMicroseconds(final DateFormat df, final String lm) throws ParseException {
        Date d = df.parse(lm);
        int idx = lm.lastIndexOf("."); //$NON-NLS-1$
        if (idx > 0) {
            try {
                long micro = Long.parseLong(lm.substring(idx + 1));
                d = new Date(d.getTime() + micro / 1000);
            } catch (NumberFormatException err) {
                err.printStackTrace();
            }
        }
        return d.getTime();
    }

    /**
     * Get the DateFormat suitable for parsing microseconds
     * 
     * @return
     */
    private final static DateFormat getDateFormatForMicroseconds() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US); //$NON-NLS-1$
        df.setTimeZone(TimeZone.getTimeZone("UTC")); //$NON-NLS-1$
        return df;
    }

    @Override
    public Collection<ObjectIFace> list(HttpConnectionListener listener, FileBuilder fileBuilder) throws IOException {
        String txtUrl = getTenant().getPublicUrl();
        if (!txtUrl.endsWith(VirtualFile.VIRTUAL_FILE_SEPARATOR)) {
            txtUrl += VirtualFile.VIRTUAL_FILE_SEPARATOR;
        }
        txtUrl += fileBuilder.getRelativePathFromRootEscapedAsURI();
        boolean hasMore = true;
        final int maxLimit = 10000;
        final String limitSuffit = "?limit=" + maxLimit; //$NON-NLS-1$
        String suffix = limitSuffit;
        String marker = null;
        SortedSet<ObjectIFace> files = new TreeSet<ObjectIFace>();
        DateFormat df = getDateFormatForMicroseconds();
        final long start = System.currentTimeMillis();
        final int id = idGenerator.incrementAndGet();
        final String msg = fileBuilder.getRelativePathFromRootEscapedAsURI().isEmpty() ? Messages.getString("operation.listRoot") : Messages.getString("operation.list", fileBuilder.getRelativePathFromRootEscapedAsURI());//$NON-NLS-1$ //$NON-NLS-2$
        listener.onProgress(id, msg, start, 0, 1024);
        long allBytes = 0;
        long allTotalBytes = 0;
        try {
            while (hasMore) {
                URL url = new URL(txtUrl + suffix);
                HttpURLConnection connection = null;

                ConnectionBuilder b = startConnection(url, "GET"); //$NON-NLS-1$
                try {
                    connection = b.buildConnection();
                    connection.setRequestProperty("Vary", "Accept-Encoding"); //$NON-NLS-1$//$NON-NLS-2$
                    connection.setRequestProperty("Accept-Encoding", "gzip, deflate"); //$NON-NLS-1$ //$NON-NLS-2$
                    connection.setRequestProperty(X_AUTH_TOKEN, getTenant().getToken());
                    connection.setUseCaches(false);
                    connection.setRequestProperty(CONTENT_LENGTH, String.valueOf(0));
                    connection.setRequestProperty(ACCEPT, CONTENT_TYPE_JSON);
                    final long totalBytes = getContentLength(connection);
                    allTotalBytes += totalBytes;
                    BufferedReader rd = openBufferedReader(connection);
                    fileBuilder.setHeaders(connection.getHeaderFields());

                    {

                        try {
                            final StringBuilder sb = new StringBuilder(262144);
                            final char[] buffer = new char[8192];
                            int read = 0;
                            while ((read = rd.read(buffer)) > 0) {
                                sb.append(buffer, 0, read);
                                allBytes += read;
                                listener.onProgress(id, msg, start, allBytes, allTotalBytes);
                            }
                            b.onSuccess(connection.getResponseCode(),
                                        connection.getResponseMessage(),
                                        getContentLength(connection));
                            hasMore = false;
                            int fileNumber = 0;
                            try {
                                JSONArray listing = new JSONArray(sb.toString());
                                for (int i = 0; i < listing.length(); i++) {
                                    JSONObject o = listing.getJSONObject(i);
                                    Map<String, String> meta = new HashMap<String, String>();
                                    final String name = o.getString("name").intern(); //$NON-NLS-1$
                                    fileNumber++;
                                    marker = name;
                                    ObjectIFace f = fileBuilder.createFile(name);
                                    try {
                                        String lm = o.getString("last_modified"); //$NON-NLS-1$
                                        meta.put(LAST_MODIFIED_HEADER, lm);
                                        if (lm != null) {
                                            try {
                                                f.setLastModified(parseUTCTimeWithMicroseconds(df, lm));
                                            } catch (ParseException err) {
                                                LOG.log(Level.WARNING, "Failed to parse last modified for " + lm, err); //$NON-NLS-1$
                                            }
                                        }
                                    } catch (JSONException ignored) {
                                    }
                                    Long size = o.getLong("bytes"); //$NON-NLS-1$
                                    meta.put(CONTENT_LENGTH, String.valueOf(size));
                                    if (size != null)
                                        f.setSize(size);
                                    try {
                                        String hash = o.getString("hash"); //$NON-NLS-1$
                                        if (hash != null)
                                            hash = hash.intern();
                                        meta.put(FsConnection.ETAG, hash);
                                    } catch (JSONException ignored) {

                                    }
                                    try {
                                        String hash = o.getString("content_type"); //$NON-NLS-1$
                                        if (hash != null)
                                            hash = hash.intern();
                                        meta.put(CONTENT_TYPE, hash);
                                        f.setContentType(hash);
                                    } catch (JSONException ignored) {

                                    }
                                    f.setMetaData(meta);
                                    files.add(f);
                                }
                            } catch (JSONException err) {
                                throw new IOException("Cannot parse JSON while listing files" + err.getLocalizedMessage(), err); //$NON-NLS-1$
                            }
                            if (fileNumber >= maxLimit) {
                                hasMore = true;
                                suffix = limitSuffit + "&marker=" + URLParamEncoder.encode(marker); //$NON-NLS-1$
                            }
                        } finally {
                            rd.close();
                        }
                    }

                } catch (IOException err) {
                    b.onError(err);
                    throw err;
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }

                }
            }
        } finally {
            listener.onClose(id);
        }
        return files;
    }

    /**
     * Post some properties
     * 
     * @param handler
     * @param container
     * @param file
     * @return
     * @throws IOException
     */
    public Map<String, List<String>> post(SwiftConnectionResultHandler handler, String container, String file,
            Map<String, String> headers) throws IOException {
        return method(handler, HTTP_METHOD_POST, container, file, headers);
    }

    @Override
    public Map<String, List<String>> get(SwiftConnectionResultHandler handler, String container, String file,
            OnFileDownloaded onDownload, long ifNewerThan) throws IOException {
        String txtUrl = getTenant().getPublicUrl();

        txtUrl += escapeURIPathParam(URL_PATH_SEPARATOR + container + URL_PATH_SEPARATOR + file);
        URL url = new URL(txtUrl);
        HttpURLConnection connection = null;
        final long start = System.currentTimeMillis();
        final int id = idGenerator.incrementAndGet();
        final String msg = Messages.getString("operation.get", container, file); //$NON-NLS-1$
        ConnectionBuilder b = startConnection(url, "GET"); //$NON-NLS-1$
        try {
            handler.onProgress(id, msg, start, 0, -1);
            connection = b.buildConnection();
            connection.setRequestProperty(X_AUTH_TOKEN, getTenant().getToken());
            connection.setUseCaches(false);
            connection.setRequestProperty(CONTENT_LENGTH, String.valueOf(0));
            connection.setRequestProperty(ACCEPT, ACCEPT_ALL);
            if (ifNewerThan > 0) {
                connection.setIfModifiedSince(ifNewerThan);
            }
            final int responseCode = connection.getResponseCode();
            final String responseMessage = connection.getResponseMessage();
            final String contentType = connection.getContentType();
            if (LOG.isLoggable(Level.FINE))
                LOG.fine(responseCode + ": " + responseMessage + "\t; content-type: " + contentType + " -\t" + txtUrl); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            final long totalBytes = getContentLength(connection);
            handler.onProgress(id, msg, start, 0, totalBytes);
            handler.onFileContent(id, handler, connection, connection.getInputStream(), container, file, onDownload);
            b.onSuccess(connection.getResponseCode(), connection.getResponseMessage(), totalBytes);
            return connection.getHeaderFields();
        } catch (IOException err) {
            b.onError(err);
            throw err;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            handler.onClose(id);
        }
    }

    private final static Object extractValue(JSONObject object, final String... path) throws JSONException {
        Object val = null;
        for (String s : path) {
            if (object == null)
                return null;
            Object o = object.get(s);
            val = o;
            if (o instanceof JSONObject)
                object = (JSONObject) o;
            else
                object = null;
        }
        return val;
    }

    /**
     * @see net.souchay.swift.net.FsConnection#connect()
     */
    @Override
    public void connect() throws IOException {
        // NOOP
    }

    private Map<String, List<String>> method(HttpConnectionListener listener, String method, String container,
            String file, Map<String, String> optionalHeaders) throws IOException {
        String txtUrl = getTenant().getPublicUrl();
        if (container != null)
            txtUrl += escapeURIPathParam(URL_PATH_SEPARATOR + container
                                         + (file == null ? "" : URL_PATH_SEPARATOR + file)); //$NON-NLS-1$
        URL url = new URL(txtUrl);
        HttpURLConnection connection = null;
        final long start = System.currentTimeMillis();
        final int id = idGenerator.incrementAndGet();
        ConnectionBuilder b = startConnection(url, method);
        try {
            listener.onProgress(id,
                                Messages.getString("operation." + method, container, (file == null ? "" : file)), start, 0, -1); //$NON-NLS-1$ //$NON-NLS-2$
            connection = b.buildConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty(X_AUTH_TOKEN, getTenant().getToken());
            connection.setUseCaches(false);
            // connection.setRequestProperty(CONTENT_LENGTH, String.valueOf(0));
            connection.setRequestProperty(ACCEPT, ACCEPT_ALL);
            for (Map.Entry<String, String> en : optionalHeaders.entrySet()) {
                connection.setRequestProperty(en.getKey(), en.getValue());
            }
            final int responseCode = connection.getResponseCode();
            final String responseMessage = connection.getResponseMessage();
            final String contentType = connection.getContentType();
            b.onSuccess(responseCode, responseMessage, getContentLength(connection));
            if (LOG.isLoggable(Level.FINE))
                LOG.fine(responseCode + ": " + responseMessage + "\t; content-type: " + contentType + " -\t" + txtUrl); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return connection.getHeaderFields();
        } catch (IOException err) {
            b.onError(err);
            throw err;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            listener.onClose(id);
        }
    }

    /**
     * Get the X_ACCOUNT_META_TEMP_URL_KEY to create temporary URL
     * 
     * @return The X_ACCOUNT_META_TEMP_URL_KEY
     */
    public String getX_account_meta_temp_url_key() {
        return getAccountProperty(X_ACCOUNT_META_TEMP_URL_KEY);
    }

    /**
     * Get the X_ACCOUNT_META_TEMP_URL_KEY to create temporary URL
     * 
     * @return The X_ACCOUNT_META_TEMP_URL_KEY
     */
    public String getX_account_meta_temp_url_key2() {
        return getAccountProperty(X_ACCOUNT_META_TEMP_URL_KEY_2);
    }

    public void generate_account_meta_temp_url_key2(HttpConnectionListener listener) throws IOException {
        String newKey = CryptUtilities.encodeToSha256AsString(UUID.randomUUID().toString().getBytes());
        HashMap<String, String> optionalHeaders = new HashMap<String, String>();
        optionalHeaders.put(X_ACCOUNT_META_TEMP_URL_KEY_2, newKey);
        //optionalHeaders.put("X-Account-Meta-Temp-Url-Key2", newKey); //$NON-NLS-1$
        method(listener, HTTP_METHOD_POST, null, null, optionalHeaders);
        setAccountProperty(X_ACCOUNT_META_TEMP_URL_KEY_2, newKey);
    }

    public void generate_account_meta_temp_url_key(HttpConnectionListener listener) throws IOException {
        String newKey = CryptUtilities.encodeToSha256AsString(UUID.randomUUID().toString().getBytes());
        HashMap<String, String> optionalHeaders = new HashMap<String, String>();
        optionalHeaders.put(X_ACCOUNT_META_TEMP_URL_KEY, newKey);
        //optionalHeaders.put("X-Account-Meta-Temp-Url-Key2", newKey); //$NON-NLS-1$
        method(listener, HTTP_METHOD_POST, null, null, optionalHeaders);
        setAccountProperty(X_ACCOUNT_META_TEMP_URL_KEY, newKey);
    }

    /**
     * Perform a head
     * 
     * @param listener
     * @param container
     * @param file
     * @return the headers
     * @throws IOException
     */
    public Map<String, List<String>> head(HttpConnectionListener listener, String container, String file,
            Map<String, String> optionalHeaders) throws IOException {
        return method(listener, "HEAD", container, file, optionalHeaders); //$NON-NLS-1$
    }

    public final static long LARGE_OBJECT_MAX_SIZE;

    static {
        final String PROP_NAME = "net.souchay.swift.largeobject.size"; //$NON-NLS-1$
        long v;
        try {
            v = Long.parseLong(System.getProperty(PROP_NAME, String.valueOf(1024 * 1024 * 1024)));
        } catch (NumberFormatException err) {
            LOG.warning("Cannot parse properly property " + PROP_NAME); //$NON-NLS-1$
            v = 4 * 1024 * 1024 * 1024;
        }
        LARGE_OBJECT_MAX_SIZE = v;
    }

    @Override
    public Map<String, List<String>> put(HttpConnectionListener listener, String container, final long len,
            String file, InputStream in, Map<String, String> headers) throws IOException {
        if (len < 1) {
            return _put(listener, container, len, file, in, headers);
        }
        Map<String, List<String>> res = new HashMap<String, List<String>>();
        final long OFFSET = LARGE_OBJECT_MAX_SIZE;
        int largeObject = -1;
        if (len > OFFSET)
            largeObject = 0;
        try {

            for (long i = 0; i < len; i += OFFSET) {
                final long theLen = Math.min(OFFSET, len - i);
                InputStreamWithProgress split = new InputStreamWithProgress(in) {

                    /**
                     * @see net.souchay.utilities.InputStreamWithProgress#read(byte[], int, int)
                     */
                    @Override
                    public int read(byte[] b, int off, int len) throws IOException {
                        long total = getTotalBytes();
                        if (total + len > theLen) {
                            len = (int) (theLen - total);
                            if (len < 0)
                                len = 0;
                        }
                        return super.read(b, off, len);
                    }

                    /**
                     * @see net.souchay.utilities.InputStreamWithProgress#read(byte[])
                     */
                    @Override
                    public int read(byte[] b) throws IOException {
                        return read(b, 0, b.length);
                    }

                    /**
                     * @see net.souchay.utilities.InputStreamWithProgress#available()
                     */
                    @Override
                    public int available() throws IOException {
                        return (int) (theLen - getTotalBytes());
                    }

                    /**
                     * @see net.souchay.utilities.InputStreamWithProgress#close()
                     */
                    @Override
                    public void close() throws IOException {
                        // Noop
                    }

                };
                split.setFullSize(theLen);
                String fname = file;
                if (largeObject > -1) {
                    NumberFormat nf = NumberFormat.getIntegerInstance(Locale.ENGLISH);
                    nf.setMinimumIntegerDigits(3);
                    nf.setMaximumFractionDigits(3);
                    fname = file + VirtualFile.VIRTUAL_FILE_SEPARATOR + ".part-" + len + "-" + nf.format(largeObject); //$NON-NLS-1$ //$NON-NLS-2$
                    headers.put("If-None-Match", "*"); //$NON-NLS-1$//$NON-NLS-2$
                    largeObject++;
                }
                res.putAll(_put(listener, container, theLen, fname, split, headers));
            }
            if (largeObject > -1) {
                headers.put(FsConnection.X_OBJECT_MANIFEST, container + VirtualFile.VIRTUAL_FILE_SEPARATOR + file
                                                            + VirtualFile.VIRTUAL_FILE_SEPARATOR + ".part-" + len + "-"); //$NON-NLS-1$ //$NON-NLS-2$
                res.putAll(_put(listener, container, 0, file, null, headers));
            }
            return res;
        } finally {
            if (in != null)
                in.close();
        }
    }

    private Map<String, List<String>> _put(HttpConnectionListener listener, String container, long len, String file,
            InputStream in, Map<String, String> headers) throws IOException {
        String txtUrl = getTenant().getPublicUrl();
        txtUrl += escapeURIPathParam("/" + container + (file == null ? "" : ("/" + file))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        HttpURLConnection connection = null;
        BufferedOutputStream wr = null;
        final long start = System.currentTimeMillis();
        URL url = new URL(txtUrl);
        final int id = idGenerator.incrementAndGet();
        ConnectionBuilder b = startConnection(url, HTTP_METHOD_PUT);
        try {
            listener.onProgress(id, Messages.getString("operation.put", container, file, 0, len), start, 0, len); //$NON-NLS-1$
            connection = b.buildConnection();
            connection.setRequestProperty(X_AUTH_TOKEN, getTenant().getToken());
            connection.setUseCaches(false);
            if (headers != null)
                for (Map.Entry<String, String> en : headers.entrySet()) {
                    connection.setRequestProperty(en.getKey(), en.getValue());
                }

            if (len >= 0) {
                if (file != null) {
                    try {
                        connection.setFixedLengthStreamingMode(len);
                    } catch (NoSuchMethodError err) {
                        connection.setFixedLengthStreamingMode((int) len);
                    }
                }

                connection.setDoInput(true);
                connection.setDoOutput(true);

                // Send request
                wr = new BufferedOutputStream(connection.getOutputStream());
                if (in != null) {
                    byte data[] = new byte[1024];
                    long written = 0;
                    int read = in.read(data);
                    while (read > 0) {
                        wr.write(data, 0, read);
                        written += read;
                        listener.onProgress(id,
                                            Messages.getString("operation.put", container, file, written, len), start, written, len); //$NON-NLS-1$
                        read = in.read(data);
                    }
                }
                wr.flush();
                wr.close();
            } else {
            }
            wr = null;
            int responseCode = connection.getResponseCode();
            b.onSuccess(responseCode, connection.getResponseMessage(), len + getContentLength(connection));
            if (responseCode != 200) {
                if (responseCode >= 400) {
                    if (responseCode >= 500) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Server Error for ").append(txtUrl); //$NON-NLS-1$
                        if (headers != null)
                            for (Map.Entry<String, String> en : headers.entrySet()) {
                                sb.append('\n').append(en.getKey()).append(": ").append(en.getValue()); //$NON-NLS-1$
                            }
                        LOG.warning(sb.toString());
                    }
                    throw new IOException("HTTP [" + responseCode + "]: " + connection.getResponseMessage()); //$NON-NLS-1$//$NON-NLS-2$
                }
            }
            return connection.getHeaderFields();
        } catch (IOException e) {
            b.onError(e);
            throw e;
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
            if (wr != null) {
                try {
                    wr.close();
                } catch (IOException ignored) {
                    ignored.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
            listener.onClose(id);
        }
    }

    /**
     * @see net.souchay.swift.net.FsConnection#del(net.souchay.swift.net.SwiftConnectionResultHandler, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public Map<String, List<String>> del(HttpConnectionListener handler, String container, String file)
            throws IOException {
        return method(handler, "DELETE", container, file, Collections.<String, String> emptyMap()); //$NON-NLS-1$
    }

    @Override
    public Map<String, List<String>> copy(HttpConnectionListener listener, String sourceContainer, String sourceFile,
            String destinationContainer, String destinationFile) throws IOException {
        String txtUrl = getTenant().getPublicUrl();
        txtUrl += escapeURIPathParam("/" + destinationContainer + (destinationFile == null ? "" : ("/" + destinationFile))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        HttpURLConnection connection = null;
        final long start = System.currentTimeMillis();
        URL url = new URL(txtUrl);
        final int id = idGenerator.incrementAndGet();
        ConnectionBuilder b = startConnection(url, HTTP_METHOD_PUT);
        try {
            listener.onProgress(id,
                                Messages.getString("operation.put", destinationContainer, destinationFile, 0, 0), start, 0, 0); //$NON-NLS-1$
            connection = b.buildConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty(X_AUTH_TOKEN, getTenant().getToken());
            connection.setFixedLengthStreamingMode(0);
            String source = escapeURIPathParam(URL_PATH_SEPARATOR + sourceContainer + URL_PATH_SEPARATOR + sourceFile);
            connection.addRequestProperty("X-Copy-From", source); //$NON-NLS-1$
            connection.setUseCaches(false);
            connection.getOutputStream().close();
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                if (responseCode >= 400) {
                    if (responseCode >= 500) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Server Error for ").append(txtUrl); //$NON-NLS-1$
                        LOG.warning(sb.toString());
                    }
                    throw new IOException("HTTP [" + responseCode + "]: " + connection.getResponseMessage()); //$NON-NLS-1$//$NON-NLS-2$
                }
            }
            return connection.getHeaderFields();
        } catch (IOException e) {
            b.onError(e);
            throw e;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            listener.onClose(id);
        }
    }

    @Override
    public File getTemporaryDirectory() throws IOException {
        String path = CryptUtilities.encodeToSha256AsString(getTenant().getPublicUrl().getBytes(CHARSET_UTF8));
        File tmp = File.createTempFile("xxxxx", ".dat"); //$NON-NLS-1$//$NON-NLS-2$
        tmp.delete();
        File dir = new File(tmp.getParentFile(), path);
        dir.mkdirs();
        return dir;
    }
}
