package net.souchay.utilities;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.HeadlessException;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import net.souchay.swift.gui.Messages;

/**
 * A class that keeps Utility methods to open an URI
 * 
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3836 $
 * 
 */
public class URIOpen {

    private static volatile boolean osChecked = false;

    private static final void showCannotInstallDesktopSupport(Component parentComponent) {
        JOptionPane.showMessageDialog(parentComponent,
                                      Messages.getString("desktopSupportIsMissing"), Messages.getString("desktopSupportMissingTitle"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private static final int showInstallDesktopSupport(Component parentComponent) {
        return JOptionPane.showConfirmDialog(parentComponent,
                                             Messages.getString("desktopSupportIsMissingTryInstall"), Messages.getString("desktopSupportMissingTitle"), JOptionPane.YES_NO_OPTION); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Check desktop support
     * 
     * @param parentComponent the component
     */
    @SuppressWarnings("nls")
    public static void checkDesktop(Component parentComponent) {
        if (!Desktop.isDesktopSupported() && !osChecked) {
            osChecked = true;
            final String os = System.getProperty("os.name");
            if (os.toLowerCase().contains("linux")) {
                BufferedReader r = null;
                final Logger LOG = Logger.getLogger("swift.osDetect"); //$NON-NLS-1$
                try {
                    r = new BufferedReader(new InputStreamReader(new FileInputStream("/etc/issue")));
                    String OS = r.readLine();
                    if (OS == null)
                        OS = "";
                    final String osName = OS.toLowerCase();
                    if (osName.contains("ubuntu") || osName.contains("debian") || osName.contains("mint")
                        || osName.contains("noppix")) {
                        LOG.info("Detected Debian Variant: " + osName);
                        // debian
                        if (JOptionPane.YES_OPTION == showInstallDesktopSupport(parentComponent)) {
                            try {
                                Runtime.getRuntime()
                                       .exec("xterm -title 'Install_GNOME_support_for_java -e sudo apt-get install libgnome2-0"); //$NON-NLS-1$
                            } catch (IOException e) {
                                e.printStackTrace();
                                showCannotInstallDesktopSupport(parentComponent);
                            }
                        }
                    } else if (osName.contains("fedora") || osName.contains("red")) {
                        LOG.info("Detected Red Hat Variant: " + osName);
                        if (JOptionPane.YES_OPTION == showInstallDesktopSupport(parentComponent)) {
                            try {
                                Runtime.getRuntime()
                                       .exec("xterm -title 'Install_GNOME_support_for_java -e sudo yum install libgnome");
                            } catch (IOException e) {
                                e.printStackTrace();
                                showCannotInstallDesktopSupport(parentComponent);
                            }
                        } else if (osName.contains("mandriva") || osName.contains("mageia")) {
                            try {
                                Runtime.getRuntime()
                                       .exec("xterm -title 'Install_GNOME_support_for_java -e sudo urpmi libgnome");
                            } catch (IOException e) {
                                e.printStackTrace();
                                showCannotInstallDesktopSupport(parentComponent);
                            }
                        } else if (osName.contains("suse")) {
                            try {
                                Runtime.getRuntime()
                                       .exec("xterm -title 'Install_GNOME_support_for_java -e sudo zypper install libgnome");
                            } catch (IOException e) {
                                e.printStackTrace();
                                showCannotInstallDesktopSupport(parentComponent);
                            }
                        } else if (osName.contains("slack")) {
                            try {
                                Runtime.getRuntime()
                                       .exec("xterm -title 'Install_GNOME_support_for_java -e sudo slackpkg install libgnome");
                            } catch (IOException e) {
                                e.printStackTrace();
                                showCannotInstallDesktopSupport(parentComponent);
                            }
                        } else if (osName.contains("sabayon")) {
                            try {
                                Runtime.getRuntime()
                                       .exec("xterm -title 'Install_GNOME_support_for_java -e sudo equo install libgnome");
                            } catch (IOException e) {
                                e.printStackTrace();
                                showCannotInstallDesktopSupport(parentComponent);
                            }
                        } else if (osName.contains("arch")) {
                            try {
                                Runtime.getRuntime()
                                       .exec("xterm -title 'Install_GNOME_support_for_java -e sudo pacman -S libgnome");
                            } catch (IOException e) {
                                e.printStackTrace();
                                showCannotInstallDesktopSupport(parentComponent);
                            }
                        } else if (osName.contains("gentoo")) {
                            try {
                                Runtime.getRuntime()
                                       .exec("xterm -title 'Install_GNOME_support_for_java -e sudo emerge libgnome");
                            } catch (IOException e) {
                                e.printStackTrace();
                                showCannotInstallDesktopSupport(parentComponent);
                            }
                        }
                    } else {
                        System.err.println("Did not find a supported distribution in /etc/issue: " + osName
                                           + ", please install libgnome");
                        showCannotInstallDesktopSupport(parentComponent);
                    }
                } catch (IOException err) {
                    err.printStackTrace();
                    showCannotInstallDesktopSupport(parentComponent);
                } finally {
                    if (r != null)
                        try {
                            r.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            }
            // JOptionPane.showConfirmDialog(parentComponent, Messages.getString(key))
        }
    }

    /**
     * Open the given URI. I browsing is not supported, will popup a message that let the User Copy/Paste the URL
     * 
     * @param uriToOpen the URI to open
     * @throws IOException
     */
    public static void browse(final URI uriToOpen) throws IOException {
        try {
            Desktop.getDesktop().browse(uriToOpen);
        } catch (UnsupportedOperationException e) {
            try {
                ProcessBuilder pb = new ProcessBuilder("firefox", "-new-tab", uriToOpen.toASCIIString()); //$NON-NLS-1$//$NON-NLS-2$
                pb.start();
                return;
            } catch (IOException ignored) {
                java.util.logging.Logger.getLogger("URIOpen").warning( //$NON-NLS-1$
                "Failed to open URL with Firefox:" //$NON-NLS-1$
                        + uriToOpen.toASCIIString() + " - " + ignored.getLocalizedMessage()); //$NON-NLS-1$
            }
            String uriText = uriToOpen.toString();
            String msg = Messages.getString("URI.cannotBrowse", uriText); //$NON-NLS-1$
            try {
                StringSelection sel = new StringSelection(uriText);
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
                JOptionPane.showMessageDialog(null, msg, Messages.getString("URI.cannotBrowseTitle"), //$NON-NLS-1$
                                              JOptionPane.WARNING_MESSAGE);
            } catch (HeadlessException ex) {
                System.err.println(uriText);
            }
        } catch (RuntimeException err) {
            try {
                Desktop.getDesktop().browse(new URI(uriToOpen.toASCIIString()));
            } catch (URISyntaxException e) {
                Logger.getLogger("URIOpen").log(Level.WARNING, "URISyntax - Failed to open : '" + uriToOpen.toString() + "' - " + err.getLocalizedMessage(), err); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
                throw new IOException(e);
            } catch (RuntimeException e) {
                Logger.getLogger("URIOpen").log(Level.WARNING, "Failed to open : '" + uriToOpen.toString() + "' - " + err.getLocalizedMessage(), err); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
                throw new IOException(e);
            }

        }
    }

    public static void mail(final URI uriToOpen) throws IOException {
        try {
            Desktop.getDesktop().mail(uriToOpen);
        } catch (UnsupportedOperationException err) {
            browse(uriToOpen);
        }
    }

}
