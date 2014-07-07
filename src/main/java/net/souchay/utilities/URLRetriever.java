package net.souchay.utilities;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import net.souchay.utilities.InputStreamWithProgress.InputStreamWithProgressListener;

public class URLRetriever {

    public static class URLHasNotBeenChanged extends IOException {

        /**
             * 
             */
        private static final long serialVersionUID = -7170767077495443514L;

        /**
         * Constructor
         * 
         * @param url
         * @param lastModified
         */
        public URLHasNotBeenChanged(URL url, long lastModified) {
        }

        /**
         * Other constructor
         * 
         * @param lastModified
         */
        public URLHasNotBeenChanged(long lastModified) {
        }

    }

    /**
     * Writes the content of the input URL into the given output stream. The output stream will not be closed (but
     * flushed) after this operation
     * <p>
     * If any error occurs while dealing with the URL stream, or writing into the output stream.
     * <p>
     * 
     * @param url - the {@link URL} from which the content must be packaged into the output stream.
     * @param out - the {@link OutputStream} to write on.
     * @param fetchIfNewerThan Fetches the resource only if resource is newer
     * @param userAgent The user agent to use (can be null, default will be used)
     * @param inOutHeaders If not null, the HTTP Headers to set, header from server will fill this map.
     * @param listener The listener
     * @return The last modified field
     * @throws ToolBoxException
     * @throws URLHasNotBeenChanged
     */
    public final static long packageDataFromURL(final URL url, final OutputStream out, final long fetchIfNewerThan,
            final String userAgent, final Map<String, List<String>> inOutHeaders,
            final InputStreamWithProgressListener listener) throws IOException, URLHasNotBeenChanged {
        final byte[] buf = new byte[4096];
        InputStreamWithProgress in = null;
        try {
            URLConnection urlC = url.openConnection();
            if (fetchIfNewerThan > 0) {
                urlC.setIfModifiedSince(fetchIfNewerThan);
            }

            if (inOutHeaders != null)
                for (Map.Entry<String, List<String>> h : inOutHeaders.entrySet()) {
                    for (String v : h.getValue()) {
                        urlC.setRequestProperty(h.getKey(), v);
                    }
                }
            if (userAgent != null)
                urlC.setRequestProperty("User-agent", userAgent); //$NON-NLS-1$
            urlC.connect();

            final long lm = urlC.getLastModified();
            if (inOutHeaders != null) {
                inOutHeaders.clear();
                Map<String, List<String>> newHeaders = urlC.getHeaderFields();
                for (Map.Entry<String, List<String>> en : newHeaders.entrySet()) {
                    if (en.getKey() != null) {
                        inOutHeaders.put(en.getKey(), en.getValue());
                    }
                }
            }
            in = new InputStreamWithProgress(urlC.getInputStream(), listener);
            try {
                in.setFullSize(urlC.getContentLengthLong());
            } catch (java.lang.NoSuchMethodError err) {
                in.setFullSize(urlC.getContentLength());
            }
            if (urlC instanceof HttpURLConnection) {
                final HttpURLConnection http = (HttpURLConnection) urlC;
                final int respCode = http.getResponseCode();
                if (respCode == HttpURLConnection.HTTP_NOT_MODIFIED)
                    throw new URLHasNotBeenChanged(url, fetchIfNewerThan);
                if (respCode == HttpURLConnection.HTTP_MOVED_PERM || respCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                    String newLoc = urlC.getHeaderField("Location"); //$NON-NLS-1$
                    if (newLoc != null)
                        packageDataFromURL(new URL(url, newLoc),
                                           out,
                                           fetchIfNewerThan,
                                           userAgent,
                                           inOutHeaders,
                                           listener);
                }
            }

            if (fetchIfNewerThan > 0) {
                if (lm == fetchIfNewerThan) {
                    in.close();
                    throw new URLHasNotBeenChanged(url, fetchIfNewerThan);
                }
            }

            int readData = 0;
            while ((readData = in.read(buf)) > 0) {
                out.write(buf, 0, readData);
            }
            return lm;
        } catch (IOException e) {
            String addMsg = e.getClass().getSimpleName();
            String detail = e.getMessage();
            if (detail != null && !detail.isEmpty())
                addMsg += ": " + detail; //$NON-NLS-1$
            throw new IOException("Cannot write " + url + " to output stream - " + addMsg, e); //$NON-NLS-1$ //$NON-NLS-2$
        } finally {
            try {
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (in != null)
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

        }

    }
}
