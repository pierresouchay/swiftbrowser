/**
 *
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-07-29 12:05:28 +0200 (Mar 29 jul 2014) $
 */
package net.souchay.swift.gui.actions;

import java.awt.event.ActionEvent;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import net.souchay.swift.gui.ConfigurationPersistance;
import net.souchay.swift.gui.Messages;

/**
 * @author Pierre Souchay <pierre@souchay.net> (last changed by $LastChangedBy: souchay $)
 * @version $Revision: 3849 $
 * 
 */
public class InstallDesktopFileAction extends AbstractAction {

    private final static Charset UTF8 = Charset.forName("UTF-8"); //$NON-NLS-1$

    private final static String XDG_DESKTOP_DIR = "XDG_DESKTOP_DIR"; //$NON-NLS-1$

    /**
     * 
     */
    private static final long serialVersionUID = 4729706999721056662L;

    private static File findDesktopFile() {
        File desktop;
        String desktopDir = System.getenv(XDG_DESKTOP_DIR);
        if (desktopDir == null || desktopDir.trim().isEmpty()) {
            desktopDir = System.getProperty("user.home");//$NON-NLS-1$
            desktop = new File(desktopDir, "Desktop");//$NON-NLS-1$
        } else {
            desktop = new File(desktopDir);
        }

        if (desktop.exists() && desktop.isDirectory())
            return desktop;

        desktop = extractXDGDesktopDir();
        if (desktop == null) {
            System.err.println("Cannot find desktop directory"); //$NON-NLS-1$
            return null;
        }
        if (desktop.exists() && desktop.isDirectory())
            return desktop;
        System.err.println("Did find desktop=" + desktop.getAbsolutePath() //$NON-NLS-1$
                           + " but does not exist or is not a directory"); //$NON-NLS-1$
        return null;
    }

    private static final File extractXDGDesktopDir() {
        final String home = System.getProperty("user.home"); //$NON-NLS-1$
        final String file = home + "/.config/user-dirs.dirs"; //$NON-NLS-1$
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), UTF8));
            try {
                String line = reader.readLine();
                while (line != null) {
                    if (line.startsWith(XDG_DESKTOP_DIR)) {
                        line = line.replace("$HOME", home); //$NON-NLS-1$
                        Pattern p = Pattern.compile(XDG_DESKTOP_DIR + "=\"(.*)\"$"); //$NON-NLS-1$
                        Matcher m = p.matcher(line);
                        if (m.matches()) {
                            String path = m.group(1);
                            return new File(path);
                        }
                    }
                    line = reader.readLine();
                }
            } finally {
                reader.close();
            }
        } catch (IOException err) {
            System.err.println("Cannot read " + file + ": " + err.getLocalizedMessage()); //$NON-NLS-1$//$NON-NLS-2$
        }
        return null;
    }

    /**
     * Install an Action to install Application on Desktop when appropriate
     * 
     * @param menu
     * @param nameOfApp
     * @param iconResourceName
     * @param comments
     * @param mimeTypes
     * @return true if action has been added
     */
    public static boolean addInstallDesktopIfNeeded(JMenu menu, String nameOfApp, String iconResourceName,
            Map<String, String> comments, String mimeTypes) {
        String osName = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
        if (osName.contains("windows") || osName.contains("mac os")) //$NON-NLS-1$ //$NON-NLS-2$
            return false;
        File desktop = findDesktopFile();
        if (desktop == null)
            return false;
        InstallDesktopFileAction action = new InstallDesktopFileAction(nameOfApp,
                                                                       iconResourceName,
                                                                       comments,
                                                                       mimeTypes,
                                                                       null);
        menu.add(action);
        return true;
    }

    /**
     * Constructor
     * 
     * @param nameOfApp
     * @param iconResourceName
     * @param comments
     * @param mimeTypes
     * @param additionalDesktopLines
     */
    public InstallDesktopFileAction(String nameOfApp, String iconResourceName, Map<String, String> comments,
            String mimeTypes, String additionalDesktopLines) {
        this.nameOfApp = nameOfApp;
        this.comments = comments;
        this.iconResourceName = iconResourceName;
        this.mimeTypes = mimeTypes;
        this.additionalDesktopLines = additionalDesktopLines;
        putValue(Action.NAME, Messages.getString("installIconOnDesktop")); //$NON-NLS-1$
    }

    private final Map<String, String> comments;

    private final String nameOfApp;

    private final String iconResourceName;

    private final String mimeTypes;

    private final String additionalDesktopLines;

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        setEnabled(false);
        try {
            File f = installDesktopFile();
            JOptionPane.showMessageDialog(null,
                                          Messages.getString("iconHasBeenInstalledOnYourDesktop", f.getAbsolutePath())); //$NON-NLS-1$
        } catch (Exception e1) {
            e1.printStackTrace();
            JOptionPane.showMessageDialog(null, e1.getClass().getName() + ": " + e1.getLocalizedMessage()); //$NON-NLS-1$
        }
    }

    private static void copyImage(File destination, String name) throws IOException {
        BufferedOutputStream bf = new BufferedOutputStream(new FileOutputStream(destination));
        byte data[] = new byte[1024];
        InputStream imgIn = null;
        try {
            imgIn = InstallDesktopFileAction.class.getResourceAsStream(name);
            int read = 0;
            while (0 <= (read = imgIn.read(data))) {
                bf.write(data, 0, read);
            }
        } catch (Throwable err) {
            System.err.println("Error while copying icon " + name + ": " + err.getLocalizedMessage()); //$NON-NLS-1$ //$NON-NLS-2$
        } finally {
            try {
                bf.close();
            } catch (IOException ignored) {
            }
            if (imgIn != null)
                imgIn.close();
        }
    }

    @SuppressWarnings("nls")
    private File installDesktopFile() throws Exception {
        File iconsDir = new File(ConfigurationPersistance.getInstance().getConfigurationDirectory(), "icons");
        if (!iconsDir.exists())
            iconsDir.mkdirs();
        File iconFile = new File(iconsDir, nameOfApp + ".png");
        StringBuilder sb = new StringBuilder();
        sb.append("[Desktop Entry]\n");
        sb.append("Encoding=UTF-8\n");
        sb.append("Version=1.0\n");
        sb.append("Type=Application\n");
        sb.append("Terminal=false\n");
        sb.append("Exec=java -jar \"")
          .append(new File(InstallDesktopFileAction.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath())
          .append("\" %u\n");
        sb.append("Name=").append(nameOfApp).append('\n');
        sb.append("Categories=Network;Utility;FileTransfer;FileManager;FileTools;Java;\n");
        sb.append("Icon=").append(iconFile.getAbsolutePath()).append('\n');
        if (comments != null && !comments.isEmpty()) {
            String english = comments.get("en");
            if (english != null)
                sb.append("Comment=").append(english).append('\n');
            for (Map.Entry<String, String> en : comments.entrySet()) {
                sb.append("Comment[").append(en.getKey()).append("]=").append(en.getValue()).append('\n');
            }
        }
        if (mimeTypes != null)
            sb.append("MimeType=").append(mimeTypes).append("\n");
        if (additionalDesktopLines != null)
            sb.append(additionalDesktopLines);
        copyImage(iconFile, iconResourceName);
        File desktop = findDesktopFile();
        File desktopFile = new File(desktop, nameOfApp + ".desktop");
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(desktopFile), "UTF-8"));
        try {
            out.write(sb.toString());
        } finally {
            out.close();
            if (desktopFile.exists())
                desktopFile.setExecutable(true);
        }
        return desktopFile;
    }
}
