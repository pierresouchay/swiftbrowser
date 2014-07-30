/**
 * $URL$
 *
 * $LastChangedBy$ - $LastChangedDate$
 */
package net.souchay.swift.gui.actions;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import net.souchay.swift.gui.Messages;
import net.souchay.utilities.URIOpen;

/**
 * @author Pierre Souchay <pierre@souchay.net> (last changed by $LastChangedBy$)
 * @version $Revision$
 * 
 */
public class CheckUpdatesAction extends AbstractAction {

    /**
     * 
     */
    private static final long serialVersionUID = 8474716963056545917L;

    /**
     * Constructor
     */
    public CheckUpdatesAction(String version, String userAgent) {
        this.currentVersion = version;
        this.userAgent = userAgent;
        TITLE = Messages.getString("updatesChecker"); //$NON-NLS-1$
        putValue(Action.NAME, TITLE);
        putValue(Action.LONG_DESCRIPTION, Messages.getString("updatesChecker.description")); //$NON-NLS-1$
    }

    /**
     * get the userAgent
     * 
     * @return the userAgent
     */
    public String getUserAgent() {
        return userAgent;
    }

    private final String TITLE;

    private final String currentVersion;

    private final String userAgent;

    /**
     * Get the Current version
     * 
     * @return the version as String
     */
    public String getCurrentVersion() {
        return currentVersion;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        setEnabled(false);
        Thread t = new Thread("CheckVersion") { //$NON-NLS-1$

            @Override
            public void run() {
                final String updateServerUrl = "https://swiftbrowser.souchay.net/version.txt"; //$NON-NLS-1$
                BufferedReader reader = null;
                try {
                    URL url = new URL(updateServerUrl);
                    HttpURLConnection c = (HttpURLConnection) url.openConnection();
                    c.setUseCaches(false);
                    c.setRequestProperty("User-Agent", getUserAgent()); //$NON-NLS-1$
                    c.setReadTimeout(30000);
                    c.setConnectTimeout(30000);
                    reader = new BufferedReader(new InputStreamReader(c.getInputStream(), "UTF-8")); //$NON-NLS-1$
                    String swiftBrowser = reader.readLine();
                    if (!"SwiftBrowserVersion".equals(swiftBrowser)) { //$NON-NLS-1$
                        throw new IOException(updateServerUrl
                                              + " was found, but first line content is incorrect: '" + swiftBrowser + "', we expected something else. Are you behind a misconfigured proxy ?"); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    String newVersion = reader.readLine();
                    System.out.println(newVersion);
                    String urlToFetch = reader.readLine();
                    if (getCurrentVersion().compareTo(newVersion) > 0) {
                        if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null,
                                                                                    Messages.getString("UpdatesChecker.needsUpdate", newVersion), //$NON-NLS-1$
                                                                                    TITLE,
                                                                                    JOptionPane.YES_NO_OPTION)) {
                            URIOpen.browse(new URI(urlToFetch));
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, Messages.getString("UpdatesChecker.upToDate"), //$NON-NLS-1$
                                                      TITLE,
                                                      JOptionPane.INFORMATION_MESSAGE);
                    }

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null,
                                                  Messages.getString("UpdatesChecker.failedToContactUpdateServer", updateServerUrl, ex.getClass().getSimpleName() + ": " + ex.getLocalizedMessage()), //$NON-NLS-1$ //$NON-NLS-2$
                                                  TITLE,
                                                  JOptionPane.ERROR_MESSAGE);
                    // Failed
                    Logger.getLogger("swift.CheckUpdatesAction").log(Level.WARNING, //$NON-NLS-1$
                                                                     "Failed to contact Update server: " //$NON-NLS-1$
                                                                             + ex.getClass().getSimpleName() + " " //$NON-NLS-1$
                                                                             + ex.getMessage(),
                                                                     (ex instanceof UnknownHostException) ? null : ex);
                } finally {
                    setEnabled(true);
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException ignored) {
                            ignored.printStackTrace();
                        }
                    }
                }
            }
        };
        t.start();
    }

}
