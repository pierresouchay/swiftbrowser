package net.souchay.swift.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SwiftConnectionBuilder {

    private final static Logger LOG = Logger.getLogger(SwiftConnectionBuilder.class.getName());

    public static SwiftConnections[] create(final String userAgent, SwiftConfiguration configuration)
            throws IOException {

        SwiftTenant tenant = SwiftConnections.authenticate(configuration);
        if (tenant.getPublicUrl() == null) {
            // Root, we have to list the tenants
            return listTenants(userAgent, configuration, tenant.getToken()).toArray(new SwiftConnections[0]);
        }
        return new SwiftConnections[] { new SwiftConnections(userAgent, configuration, tenant) };
    }

    private static List<SwiftConnections> listTenants(final String userAgent, SwiftConfiguration configuration,
            String token) throws IOException {
        HttpURLConnection connection = null;
        String tenantsUrl = configuration.getTokenUrl().toExternalForm().replace("/tokens", "/tenants"); //$NON-NLS-1$//$NON-NLS-2$
        LOG.fine("Listing tenants..."); //$NON-NLS-1$
        try {
            // final String urlParameters = configuration.getCredential().serialize();
            connection = (HttpURLConnection) new URL(tenantsUrl).openConnection();
            // connection.setRequestProperty(CONTENT_TYPE, );
            // connection.setRequestMethod(SwiftConnections.HTTP_METHOD_GET);
            // connection.setRequestProperty(SwiftConnections.CONTENT_TYPE, SwiftConnections.ONTENT_TYPE_JSON);
            connection.setRequestProperty(SwiftConnections.ACCEPT, SwiftConnections.CONTENT_TYPE_JSON);
            // connection.setRequestProperty(SwiftConnections.CONTENT_TYPE, SwiftConnections.CONTENT_TYPE_JSON);
            connection.setRequestProperty(SwiftConnections.X_AUTH_TOKEN, token);
            // connection.setRequestProperty(SwiftConnections.CONTENT_LENGTH, String.valueOf(passwordBytes.length));
            connection.setUseCaches(false);
            // connection.setDoInput(true);
            // connection.setDoOutput(true);
            Map<String, List<String>> headers = connection.getRequestProperties();
            if (200 != connection.getResponseCode()) {
                StringBuilder sb = new StringBuilder();
                sb.append(tenantsUrl).append(": ").append(connection.getResponseCode()) //$NON-NLS-1$
                  .append(": ").append(connection.getResponseMessage()); //$NON-NLS-1$ 
                //sb.append("\n-- Post\n").append(urlParameters); //$NON-NLS-1$
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
                LOG.warning(sb.toString());
            }
            // Get Response
            InputStream is = connection.getInputStream();

            StringBuffer response = new StringBuffer();
            {
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                try {
                    String line;
                    while ((line = rd.readLine()) != null) {
                        response.append(line).append('\n');
                    }
                } finally {
                    rd.close();
                    is = null;
                }
            }
            try {
                JSONObject obj = new JSONObject(response.toString());
                JSONArray tenants = obj.getJSONArray("tenants"); //$NON-NLS-1$
                List<SwiftConnections> connections = new LinkedList<SwiftConnections>();
                for (int i = 0; i < tenants.length(); i++) {
                    JSONObject t = tenants.getJSONObject(i);
                    SwiftConfiguration cfg = new SwiftConfiguration(configuration.getCredential()
                                                                                 .cloneForTenantId(t.getString(SwiftConstantsServer.ID)),
                                                                    configuration.getTokenUrl());
                    connections.add(new SwiftConnections(userAgent, cfg));
                }
                return connections;
            } catch (JSONException ex) {
                throw new IOException("Error parsing JSON for URL " + tenantsUrl + ": " + ex.getLocalizedMessage(), ex); //$NON-NLS-1$ //$NON-NLS-2$
            } finally {
                if (is != null)
                    is.close();
            }
        } catch (IOException e) {
            throw e;
        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
