/**
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-07-07 12:12:06 +0200 (Lun 07 jul 2014) $
 */
package net.souchay.swift.gui.actions;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ScheduledExecutorService;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import net.souchay.swift.gui.ContainerVirtualFile;
import net.souchay.swift.gui.ErrorsHandler;
import net.souchay.swift.gui.LayoutUtils;
import net.souchay.swift.gui.Messages;
import net.souchay.swift.gui.SharingDuration;
import net.souchay.swift.gui.SwiftMain;
import net.souchay.swift.gui.SwiftToVirtualFiles;
import net.souchay.swift.gui.VirtualFile;
import net.souchay.swift.gui.VirtualFileAction;
import net.souchay.swift.gui.configuration.SwiftConfigurationEditor;
import net.souchay.swift.gui.dnd.JTransferableTree;
import net.souchay.swift.net.DefaultSwiftConnectionResult;
import net.souchay.swift.net.SwiftConnections;
import net.souchay.utilities.URIOpen;
import net.souchay.utilities.URLParamEncoder;
import org.jdesktop.swingx.VerticalLayout;

/**
 * @author Pierre Souchay <pierre@souchay.net> (last changed by $LastChangedBy: souchay $)
 * @version $Revision: 3835 $
 * 
 */
public class ShareUploadAction extends VirtualFileAction {

    /**
     * 
     * @param conn
     * @param file
     * @param max_file_size
     * @param max_file_count
     * @param expires
     * @param signature if null, password protection, otherwise generate a password field
     * @return
     * @throws IOException
     * @throws InvalidKeyException
     */
    private final static File generateUploadFile(SwiftConnections conn, VirtualFile file, final long max_file_size,
            final int max_file_count, long expires, String path, String signature) throws IOException,
            InvalidKeyException {
        File temp = File.createTempFile("upload_", ".html");//$NON-NLS-1$ //$NON-NLS-2$
        final Charset UTF_8 = Charset.forName("UTF-8");//$NON-NLS-1$
        BufferedReader in = null;
        StringBuilder sb = new StringBuilder(16384);
        try {
            in = new BufferedReader(new InputStreamReader(SwiftMain.class.getResourceAsStream("/net/souchay/swift/tmpl/upload.html"),//$NON-NLS-1$
                                                          UTF_8));
            String s = in.readLine();

            while (s != null) {
                sb.append(s).append('\n');
                s = in.readLine();
            }
        } finally {
            if (in != null)
                in.close();
        }
        String data = sb.toString();

        data = data.replaceAll("___UPLOADPATH___", path);//$NON-NLS-1$
        data = data.replaceAll("___max_file_size___", String.valueOf(max_file_size));//$NON-NLS-1$
        data = data.replaceAll("___max_file_count___", String.valueOf(max_file_count));//$NON-NLS-1$
        data = data.replaceAll("___signature___", String.valueOf(signature));//$NON-NLS-1$
        data = data.replaceAll("___expires___", String.valueOf(expires));//$NON-NLS-1$
        if (signature == null) {
            data = data.replaceAll("___signature_field___", //$NON-NLS-1$
                                   "<label id=\"passwordLabel\" for=\"signature\">Password:</label><input type=\"password\" name=\"signature\" id=\"signature\" />"); //$NON-NLS-1$
        } else {
            data = data.replaceAll("___signature_field___", //$NON-NLS-1$
                                   "<input type=\"hidden\" name=\"signature\" id=\"signature\" value=\"" + signature //$NON-NLS-1$
                                           + "\"/>"); //$NON-NLS-1$
        }
        BufferedWriter w = null;
        try {
            w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(temp), UTF_8));
            w.write(data);
        } finally {
            if (w != null)
                w.close();
        }
        return temp;
    }

    private final ScheduledExecutorService executor;

    private final SwiftToVirtualFiles guiBindings;

    private final ErrorsHandler errorsHandler;

    private final DefaultSwiftConnectionResult fileHandler;

    private final SwiftConnections conn;

    private final JTransferableTree table;

    private final AbstractAction refreshAllAction;

    /**
     * 
     */
    private static final long serialVersionUID = -8532124415472997944L;

    private final String name = Messages.getString("friends.upload"); //$NON-NLS-1$

    private final Icon icon = SwiftConfigurationEditor.loadIcon("friends", name); //$NON-NLS-1$
    {
        setEnabled(false);
        putValue(Action.NAME, name);
        putValue(Action.SHORT_DESCRIPTION, name);
        putValue(Action.SMALL_ICON, icon);
    }

    private Long numberOfFilesMaxDefault = Long.valueOf(10L);

    private Long maxSizeOfUploadDefault = Long.valueOf(50L);

    public ShareUploadAction(ScheduledExecutorService executor, SwiftToVirtualFiles guiBindings,
            ErrorsHandler errorsHandler, DefaultSwiftConnectionResult fileHandler, SwiftConnections conn,
            JTransferableTree table, AbstractAction refreshAllAction) {
        this.executor = executor;
        this.guiBindings = guiBindings;
        this.errorsHandler = errorsHandler;
        this.fileHandler = fileHandler;
        this.conn = conn;
        this.table = table;
        this.refreshAllAction = refreshAllAction;
    }

    private final JCheckBox protectedByPassword = new JCheckBox(Messages.getString("passwordProtectedUpload"), true); //$NON-NLS-1$

    private final JCheckBox useSecondaryKey = new JCheckBox(Messages.getString("sharingKeyUseSecondaryKey"), true);//$NON-NLS-1$

    {
        useSecondaryKey.setToolTipText(Messages.getString("sharingKeyUseSecondaryKeyTooltip")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            int srow = table.getSelectedRow();
            if (srow > 0) {
                VirtualFile _file = (VirtualFile) table.getValueAt(srow, 0);
                if (_file instanceof ContainerVirtualFile) {
                    Vector<String> files = new Vector<String>();
                    for (VirtualFile f : _file.getChildren()) {
                        if (f.isDirectory()) {
                            files.add(f.getName());
                        }
                    }
                    JComboBox<String> box = new JComboBox<String>(files);
                    box.setEditable(true);
                    JPanel panel = new JPanel(new VerticalLayout(10));
                    panel.add(new JLabel(Messages.getString("pleaseSelectASubDirectoryToShareMessage", //$NON-NLS-1$
                                                            _file.getName())));
                    panel.add(box);
                    box.setSelectedItem(Messages.getString("defaultSharedDirectoryName")); //$NON-NLS-1$
                    if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(table,
                                                                               panel,
                                                                               Messages.getString("pleaseSelectASubDirectoryToShareTitle"), //$NON-NLS-1$
                                                                               JOptionPane.OK_CANCEL_OPTION,
                                                                               JOptionPane.QUESTION_MESSAGE,
                                                                               icon)) {
                        return;
                    }
                    if (box.getSelectedItem() == null) {
                        return;
                    }
                    String val = (String) box.getSelectedItem();
                    String dirName = val.trim().replaceAll("/", ""); //$NON-NLS-1$ //$NON-NLS-2$
                    if (dirName.isEmpty())
                        return;
                    if (!files.contains(dirName)) {
                        _file = guiBindings.createNewDirectory(_file, dirName);
                    } else {
                        for (VirtualFile f : _file.getChildren()) {
                            if (f.getFile() == null && dirName.equals(f.getName())) {
                                _file = f;
                                break;
                            }
                        }
                    }
                }
                final VirtualFile file = _file;

                final long now = System.currentTimeMillis();
                JPanel limits = new JPanel(new GridBagLayout());
                Vector<Long> preDefinedValues = new Vector<Long>();
                preDefinedValues.add(Long.valueOf(1));
                preDefinedValues.add(Long.valueOf(10));
                preDefinedValues.add(Long.valueOf(50));
                preDefinedValues.add(Long.valueOf(100));
                preDefinedValues.add(Long.valueOf(1000));
                preDefinedValues.add(Long.valueOf(10000));
                final JComboBox<Long> numberOfFilesMax = new JComboBox<Long>(preDefinedValues);
                numberOfFilesMax.setEditable(true);
                Vector<Long> preDefinedValues2 = new Vector<Long>(preDefinedValues);
                final JComboBox<Long> maxSizeOfUpload = new JComboBox<Long>(preDefinedValues2);
                maxSizeOfUpload.setEditable(true);
                {
                    limits.setBorder(BorderFactory.createTitledBorder(Messages.getString("uploadLimits"))); //$NON-NLS-1$
                    int row = 0;
                    LayoutUtils.addRow(limits, row++, Messages.getString("numberOfFilesMax"), numberOfFilesMax); //$NON-NLS-1$
                    LayoutUtils.addRow(limits, row++, Messages.getString("maxSizeOfUpload"), maxSizeOfUpload); //$NON-NLS-1$

                    LayoutUtils.addRow(limits, row++, Messages.getString("maxSizeOfUpload"), maxSizeOfUpload); //$NON-NLS-1$
                    LayoutUtils.addRow(limits, row++, Messages.getString("sharingKey"), useSecondaryKey); //$NON-NLS-1$
                    numberOfFilesMax.setSelectedItem(numberOfFilesMaxDefault);
                    maxSizeOfUpload.setSelectedItem(maxSizeOfUploadDefault);
                    {
                        GridBagConstraints c = new GridBagConstraints();
                        c.gridy = row++;
                        c.gridx = 0;
                        c.gridwidth = 2;
                        c.anchor = GridBagConstraints.BASELINE_LEADING;
                        c.fill = GridBagConstraints.HORIZONTAL;
                        limits.add(protectedByPassword, c);
                    }
                }

                // limits.setBorder(BorderFactory.create);
                final Long duration = SharingDuration.getExpirationDate(table,
                                                                        SharingDuration.MAIL_UPLOAD_SHARING_DEFAULT,
                                                                        now,
                                                                        limits,
                                                                        icon);
                if (duration == null)
                    return;
                if (numberOfFilesMax.getSelectedItem() != null && (numberOfFilesMax.getSelectedItem() instanceof Long)) {
                    numberOfFilesMaxDefault = (Long) numberOfFilesMax.getSelectedItem();
                    if (numberOfFilesMaxDefault < 1)
                        numberOfFilesMaxDefault = 1L;
                }

                if (maxSizeOfUpload.getSelectedItem() != null && (maxSizeOfUpload.getSelectedItem() instanceof Long)) {
                    maxSizeOfUploadDefault = (Long) maxSizeOfUpload.getSelectedItem();
                    if (maxSizeOfUploadDefault < 1)
                        maxSizeOfUploadDefault = 1L;
                }
                final long expires = now + duration.longValue();
                // StringBuilder sb = new StringBuilder();

                Thread shareThread = new Thread("shareThread") { //$NON-NLS-1$

                    @Override
                    public void run() {
                        try {
                            URL root = new URL(conn.getTenant().getPublicUrl());
                            String path = root.getPath() + VirtualFile.VIRTUAL_FILE_SEPARATOR
                                          + file.getUnixPathWithContainer() + VirtualFile.VIRTUAL_FILE_SEPARATOR;
                            long max_file_size = maxSizeOfUploadDefault * 1024 * 1024;
                            int max_file_count = numberOfFilesMaxDefault.intValue();
                            final String signature = conn.generateSignatureForPostUpload(path,
                                                                                         "", max_file_size, max_file_count, expires, useSecondaryKey.isSelected()); //$NON-NLS-1$

                            File tmp = generateUploadFile(conn,
                                                          file,
                                                          max_file_size,
                                                          max_file_count,
                                                          expires,
                                                          path,
                                                          protectedByPassword.isSelected() ? null : signature);
                            FileInputStream in = null;
                            try {
                                in = new FileInputStream(tmp);
                                HashMap<String, String> headers = new HashMap<String, String>();
                                headers.put("X-Delete-At", String.valueOf(expires)); //$NON-NLS-1$
                                headers.put("Content-Type", "text/html; charset=UTF-8"); //$NON-NLS-1$//$NON-NLS-2$
                                headers.put("Content-Disposition", "inline"); //$NON-NLS-1$//$NON-NLS-2$
                                String uploadFileName = "upload_" + expires //$NON-NLS-1$
                                                        + ".html"; //$NON-NLS-1$
                                final String fullName = file.getUnixPathWithoutContainer()
                                                        + VirtualFile.VIRTUAL_FILE_SEPARATOR + uploadFileName;
                                final String fullNameWithContainer = file.getUnixPathWithContainer()
                                                                     + VirtualFile.VIRTUAL_FILE_SEPARATOR
                                                                     + uploadFileName;
                                conn.put(fileHandler, file.getContainerName(), tmp.length(), fullName, in, headers);

                                executor.submit(new Runnable() {

                                    @Override
                                    public void run() {
                                        refreshAllAction.actionPerformed(null);
                                        try {
                                            final long expires = System.currentTimeMillis() + 3600000 * 24 * 7;
                                            StringBuilder sb = new StringBuilder();
                                            sb.append(Messages.getString("friends.main", SharingDuration.computeDurationAsString(now, duration))).append('\n'); //$NON-NLS-1$
                                            sb.append("\n ") //$NON-NLS-1$
                                              .append(conn.generateTempUrl("GET", //$NON-NLS-1$ 
                                                                           expires,
                                                                           fullNameWithContainer,
                                                                           useSecondaryKey.isSelected())
                                                          .toURL()
                                                          .toExternalForm())
                                              //$NON-NLS-1$ //$NON-NLS-2$
                                              .append('\n');
                                            sb.append("\n"); //$NON-NLS-1$
                                            if (protectedByPassword.isSelected()) {
                                                sb.append(Messages.getString("friends.password", signature)); //$NON-NLS-1$
                                                sb.append('\n');
                                            }
                                            URIOpen.mail(new URI("mailto:?subject=" //$NON-NLS-1$
                                                                 + URLParamEncoder.encode(Messages.getString("friends.subject")) //$NON-NLS-1$
                                                                 + "&body=" + URLParamEncoder.encode(sb.toString()))); //$NON-NLS-1$
                                        } catch (MalformedURLException err) {
                                            err.printStackTrace();
                                        } catch (InvalidKeyException err) {
                                            errorsHandler.handleInvalidKeyException(table, err);
                                        } catch (Exception err) {
                                            err.printStackTrace();
                                        }
                                    }
                                });
                            } finally {
                                if (in != null)
                                    in.close();
                            }
                        } catch (IOException err) {
                            err.printStackTrace();
                        } catch (InvalidKeyException err) {
                            errorsHandler.handleInvalidKeyException(table, err);
                        }
                    }
                };
                shareThread.setDaemon(true);
                shareThread.start();
                //                            sb.append("\n"); //$NON-NLS-1$
                //                            URIOpen.mail(new URI("mailto:?subject=" //$NON-NLS-1$
                //                                                 + URLParamEncoder.encode(Messages.getString("share.subject")) //$NON-NLS-1$
                //                                                 + "&body=" + URLParamEncoder.encode(sb.toString()))); //$NON-NLS-1$
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    @Override
    public void enable(VirtualFile... selectedPaths) {
        setEnabled(selectedPaths.length == 1 && selectedPaths[0].isDirectory());
    }

}
