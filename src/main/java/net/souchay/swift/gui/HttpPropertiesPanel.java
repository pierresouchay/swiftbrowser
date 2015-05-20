package net.souchay.swift.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import net.souchay.swift.gui.ContainerIFace.FileIFace;
import net.souchay.swift.gui.dnd.GlobalExecutorService;
import net.souchay.swift.gui.table.HeaderRow;
import net.souchay.swift.gui.table.HeaderRowRenderer;
import net.souchay.swift.gui.table.HeadersPanel;
import net.souchay.swift.gui.table.MapTableModel;
import net.souchay.swift.net.DefaultSwiftConnectionResult;
import net.souchay.swift.net.SwiftConnections;
import net.souchay.swift.net.SwiftTenant;
import net.souchay.utilities.URIOpen;
import net.souchay.utilities.URLParamEncoder;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.VerticalLayout;
import org.json.JSONException;

/**
 * 
 * @author Pierre Souchay <pierre@souchay.net> (last changed by $LastChangedBy: souchay $)
 * @version $Revision: 3922 $
 * 
 */
public class HttpPropertiesPanel extends JPanel implements ListSelectionListener {

    /**
     * 
     */
    private static final long serialVersionUID = -8491418865811132771L;

    private final JXHyperlink link = new JXHyperlink();

    private final JLabel path = new JLabel("xxxxxxxxxxxxxxxxxxxx"); //$NON-NLS-1$

    private final JTabbedPane tab = new JTabbedPane();

    private final JTextArea curlCommand = new JTextArea(3, 32);

    private final JXTable table;

    private final SwiftConnections conn;

    private final ScheduledExecutorService executor = GlobalExecutorService.getExecutorService();

    private final Action refreshSingleFile;

    private final JLabel numberOfBytesInContainer = new JLabel("000000000"); //$NON-NLS-1$

    private final JLabel numberOfFilesInContainer = new JLabel("000000000"); //$NON-NLS-1$

    private final JLabel containerName = new JLabel("--------"); //$NON-NLS-1$

    private final Action refreshAllAction;

    private final DefaultSwiftConnectionResult fileHandler;

    private final JTextField readACL = new JTextField(32);

    private final JCheckBox allCanRead = new JCheckBox(Messages.getString("anybodyCanRead"), //$NON-NLS-1$
                                                       false);

    private final JCheckBox allCanList = new JCheckBox(Messages.getString("anybodyCanList"), //$NON-NLS-1$
                                                       false);

    private final Action updateACL;

    private String acl = ""; //$NON-NLS-1$

    private ACLParser aclParser;

    private WeakReference<ContainerObject> containerObjectRef;

    private final DocumentListener docListener;

    private void showHideFileTab(final int numOfTabs) {
        while (tab.getTabCount() > numOfTabs) {
            int idxToRemove = tab.getTabCount() - 1;
            tab.setSelectedIndex(idxToRemove - 1);
            tab.removeTabAt(idxToRemove);
        }
        if (tab.getTabCount() == numOfTabs)
            return;
        if (tab.getTabCount() == 1 && numOfTabs > 1) {
            tab.addTab(Messages.getString("containerInfo"), containerPanel); //$NON-NLS-1$
            tab.setSelectedIndex(1);
        }
        if (tab.getTabCount() == 2 && numOfTabs > 2) {
            tab.addTab(Messages.getString("fileInfo"), headersForFilesPanel); //$NON-NLS-1$
            tab.setSelectedIndex(2);
        }
    }

    private final JTextField tokenId = new JTextField(48);

    private final JTextField tenantName = new JTextField(48);

    private final JTextField tenantId = new JTextField(48);

    private final JTextField publicUrl = new JTextField(48);

    private final JTextField tenantDescription = new JTextField();

    private final MapTableModel tenantInformations = new MapTableModel().setMap(Collections.singletonMap("Tenant", //$NON-NLS-1$
                                                                                                         "Value")); //$NON-NLS-1$

    private final JPanel containerPanel = new JPanel(new BorderLayout(0, 20));

    /**
     * Constructor
     * 
     * @param table
     * @param refreshSingleFile
     */
    public HttpPropertiesPanel(final SwiftConnections conn, JXTable table, Action refreshAllAction,
            Action refreshSingleFile, final DefaultSwiftConnectionResult fileHandler) {
        super(new BorderLayout(5, 5));
        this.table = table;
        this.refreshAllAction = refreshAllAction;
        JPanel north = new JPanel(new GridBagLayout());
        this.fileHandler = fileHandler;
        curlCommand.setWrapStyleWord(true);
        this.refreshSingleFile = refreshSingleFile;

        this.conn = conn;
        {
            int row = 0;
            JPanel hp = new JPanel(new BorderLayout(10, 0));
            JButton ref = new JButton(refreshSingleFile);
            ref.setHideActionText(true);
            hp.add(ref, BorderLayout.LINE_END);
            hp.add(link, BorderLayout.CENTER);
            LayoutUtils.addRow(north, row++, Messages.getString("URL"), hp); //$NON-NLS-1$
            LayoutUtils.addRow(north, row++, Messages.getString("path"), path); //$NON-NLS-1$
        }
        add(north, BorderLayout.NORTH);

        JPanel tenantPanel = new JPanel(new BorderLayout(0, 20));
        {
            JPanel metaDataPanel = new JPanel(new GridBagLayout());
            int row = 0;
            LayoutUtils.addRow(metaDataPanel, row++, Messages.getString("token"), tokenId); //$NON-NLS-1$
            LayoutUtils.addRow(metaDataPanel, row++, Messages.getString("tenantId"), tenantId); //$NON-NLS-1$ 
            LayoutUtils.addRow(metaDataPanel, row++, Messages.getString("tenantName"), tenantName); //$NON-NLS-1$
            LayoutUtils.addRow(metaDataPanel, row++, Messages.getString("tenantDescription"), tenantDescription); //$NON-NLS-1$
            LayoutUtils.addRow(metaDataPanel, row++, Messages.getString("publicUrl"), publicUrl); //$NON-NLS-1$
            String swiftFeatures = null;
            try {
                swiftFeatures = conn.getSwiftInformation().toString(2);
            } catch (JSONException err) {
                swiftFeatures = err.getLocalizedMessage();
            }
            JTextArea swiftFeaturesText = new JTextArea(swiftFeatures);
            swiftFeaturesText.setEditable(false);
            tenantPanel.add(metaDataPanel, BorderLayout.NORTH);
            JTable tx = new JTable(tenantInformations);
            {
                JTabbedPane subTab = new JTabbedPane();
                subTab.addTab(Messages.getString("tenantHeaders"), new JScrollPane(tx)); //$NON-NLS-1$
                subTab.addTab(Messages.getString("swiftFeatures"), new JScrollPane(swiftFeaturesText)); //$NON-NLS-1$
                tenantPanel.add(subTab, BorderLayout.CENTER);
            }
        }
        tab.addTab(Messages.getString("tenantInformation"), tenantPanel); //$NON-NLS-1$

        {
            JPanel metaDataPanel = new JPanel(new GridBagLayout());
            int row = 0;
            LayoutUtils.addRow(metaDataPanel, row++, Messages.getString("container"), containerName); //$NON-NLS-1$
            LayoutUtils.addRow(metaDataPanel,
                               row++,
                               Messages.getString("numberOfBytesInContainer"), numberOfBytesInContainer); //$NON-NLS-1$ 
            LayoutUtils.addRow(metaDataPanel,
                               row++,
                               Messages.getString("numberOfFilesInContainer"), numberOfFilesInContainer); //$NON-NLS-1$ 
            containerPanel.add(metaDataPanel, BorderLayout.NORTH);
        }

        {

            JPanel aclPanel = new JPanel(new VerticalLayout(5));
            aclPanel.setBorder(BorderFactory.createTitledBorder(Messages.getString("ACLManagement"))); //$NON-NLS-1$

            updateACL = new AbstractAction(Messages.getString("aclUpdate")) { //$NON-NLS-1$

                /**
                     * 
                     */
                private static final long serialVersionUID = -373984119848525657L;

                {
                    setEnabled(false);
                }

                @Override
                public void actionPerformed(ActionEvent e) {
                    allCanList.setEnabled(false);
                    allCanRead.setEnabled(false);
                    readACL.setEnabled(false);
                    setEnabled(false);
                    final String newAcl = readACL.getText();
                    executor.submit(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                Map<String, String> headers = new HashMap<String, String>();
                                headers.put(ContainerObject.PROPERTY_CONTAINER_READ_ACL, newAcl);
                                // Map<String, List<String>> newHeaders =
                                ContainerObject container = containerObjectRef.get();
                                if (container == null)
                                    return;
                                conn.post(fileHandler, container.getName(), null, headers);
                                container.setReadAcls(newAcl);
                                SwingUtilities.invokeLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        valueChanged(null);
                                    }
                                });
                                // refreshAllAction.actionPerformed(null);
                                // container.setHeader(newHeaders);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                allCanList.setEnabled(true);
                                allCanRead.setEnabled(true);
                                readACL.setEnabled(true);
                                setEnabled(true);

                            }

                        }
                    });
                }
            };

            allCanRead.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    final String newText = aclParser.toggleACL(ACLParser.R_FOR_ALL);
                    readACL.setText(newText);
                    updateACL.setEnabled(!newText.trim().equals(acl));
                }
            });

            allCanList.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    final String newText = aclParser.toggleACL(ACLParser.R_LISTINGS);
                    readACL.setText(newText);
                    updateACL.setEnabled(!newText.trim().equals(acl));
                }
            });

            docListener = new DocumentListener() {

                private void parseAcl() {
                    String newVal = readACL.getText().trim();
                    aclParser.updateAllACLs(newVal);
                    allCanList.setSelected(aclParser.containsACL(ACLParser.R_LISTINGS));
                    allCanRead.setSelected(aclParser.containsACL(ACLParser.R_FOR_ALL));
                    updateACL.setEnabled(!newVal.equals(acl));
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    parseAcl();
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    parseAcl();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    parseAcl();
                }
            };
            readACL.getDocument().addDocumentListener(docListener);
            {
                JPanel jp = new JPanel(new BorderLayout(5, 5));
                jp.add(readACL, BorderLayout.CENTER);
                jp.add(new JButton(updateACL), BorderLayout.LINE_END);
                aclPanel.add(jp);
            }
            {
                JPanel jp = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
                jp.add(allCanRead);
                jp.add(allCanList);
                aclPanel.add(jp);
            }
            containerPanel.add(aclPanel, BorderLayout.SOUTH);
        }
        headersForContainerPanel.setBorder(BorderFactory.createTitledBorder(Messages.getString("containerHeaders"))); //$NON-NLS-1$
        containerPanel.add(headersForContainerPanel, BorderLayout.CENTER);
        add(tab, BorderLayout.CENTER);
        add(new JScrollPane(curlCommand), BorderLayout.SOUTH);
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                refreshTenant(true);
                tab.setSelectedIndex(0);
            }
        });
    }

    private volatile FileIFace currentFileIFace = null;

    private volatile VirtualFile currentVirtualFile = null;

    private ActionListener updateContainerAction = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            // NOOP
        }
    };

    private ActionListener updateFileAction = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            // NOOP
        }
    };

    private final AbstractAction applyModifiedHeadersOnContainer = new AbstractAction(Messages.getString("updateHeadersOnContainer")) { //$NON-NLS-1$

        /**
         * 
         */
        private static final long serialVersionUID = 3846652924263525969L;

        @Override
        public void actionPerformed(ActionEvent e) {
            updateContainerAction.actionPerformed(e);
        }
    };

    private final AbstractAction applyModifiedHeadersOnFile = new AbstractAction(Messages.getString("updateHeadersOnFile")) { //$NON-NLS-1$

        /**
         * 
         */
        private static final long serialVersionUID = 2260620749563484155L;

        @Override
        public void actionPerformed(ActionEvent e) {
            updateFileAction.actionPerformed(e);
        }
    };

    private final HeadersPanel headersForContainerPanel = new HeadersPanel(HeaderRowRenderer.BLACKLISTED_HEADERS_FOR_CONTAINER,
                                                                           HeaderRowRenderer.PREDEFINED_HEADERS_FOR_CONTAINER,
                                                                           applyModifiedHeadersOnContainer);

    private final HeadersPanel headersForFilesPanel = new HeadersPanel(HeaderRowRenderer.BLACKLISTED_HEADERS_FOR_FILES,
                                                                       HeaderRowRenderer.PREDEFINED_HEADERS_FOR_FILES,
                                                                       applyModifiedHeadersOnFile);

    private ElementChangedListener<FileIFace> listener = new ElementChangedListener<ContainerIFace.FileIFace>() {

        @Override
        public void onElementChanged(FileIFace source) {
            refreshCurrentFile(source);
        }
    };

    private volatile long lastTenantUpdate = -1;

    private void refreshTenant(boolean refreshPath) {
        final long now = System.currentTimeMillis();
        if (now - lastTenantUpdate < 1000)
            return;
        try {
            SwiftTenant t = conn.getTenant();
            tenantId.setText(t.getId());
            tenantName.setText(t.getName());
            tenantDescription.setText(t.getDescription());
            tokenId.setText(t.getToken());
            publicUrl.setText(t.getPublicUrl());
            tenantInformations.setMap(conn.getAccountProperties());
            lastTenantUpdate = now;
            if (refreshPath)
                path.setText(t.getPublicUrl());
        } catch (IOException ignored) {
            // ...
            Logger.getLogger("HttpPropertiesPanel").log(Level.WARNING, "Cannot refresh tenant information", ignored); //$NON-NLS-1$//$NON-NLS-2$
        }
    }

    public void refreshVirtualFile(final VirtualFile f, final FileIFace newFile) {
        final Map<String, String> vals = new HashMap<String, String>();
        if (this.currentFileIFace != null) {
            this.currentFileIFace.removeElementChangedListener(listener);
        }
        this.currentVirtualFile = f;
        this.currentFileIFace = newFile;
        if (this.currentFileIFace != null) {
            this.currentFileIFace.addElementChangedListener(listener);
            vals.putAll(this.currentFileIFace.getMetaData());
        }
        refreshTenant(false);
        if (f == null) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    showHideFileTab(1);
                }
            });

        } else {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    int numOfTabs = 1;
                    {
                        ContainerObject containerX = (ContainerObject) (f.getContainer());
                        if (containerX != null) {
                            containerObjectRef = new WeakReference<ContainerObject>(containerX);
                            numOfTabs++;
                        }
                    }
                    String curlCommand = null;
                    // boolean isRealFile = file!=null || (file instanceof ContainerVirtualFile);
                    // if (isRealFile){
                    try {
                        StringBuilder sb = new StringBuilder();
                        sb.append(conn.getTenant().getPublicUrl()).append(VirtualFile.VIRTUAL_FILE_SEPARATOR);
                        sb.append(URLParamEncoder.encode(f.getContainerName()));
                        if (!f.isDirectory()) {
                            List<String> pathAsList = f.getUnixPathWithoutContainerAsAList();
                            for (String s : pathAsList) {
                                if (!s.isEmpty())
                                    sb.append(VirtualFile.VIRTUAL_FILE_SEPARATOR).append(URLParamEncoder.encode(s));
                            }
                        } else {
                            if (!(f instanceof ContainerVirtualFile)) {
                                sb.append(VirtualFile.VIRTUAL_FILE_SEPARATOR)
                                  .append("?delimiter=/&prefix=").append(URLEncoder.encode(f.getUnixPathWithoutContainer(), "UTF-8")).append(VirtualFile.VIRTUAL_FILE_SEPARATOR); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        }
                        final String urlTxt = sb.toString();
                        final URL url = new URL(urlTxt);
                        {
                            final String token = conn.getTenant().getToken();
                            final String urlAsText = url.toExternalForm() ;
                            // We add -g since it avoid curl to crash if URL contains patterns such as [
                            curlCommand = "curl -g -H 'X-Auth-Token: " + token + "' '" //$NON-NLS-1$ //$NON-NLS-2$
                                          + urlAsText + "'"; //$NON-NLS-1$
                        }
                        link.setAction(new AbstractAction(url.toExternalForm()) {

                            /**
                     * 
                     */
                            private static final long serialVersionUID = 1L;

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                try {
                                    if (!f.isDirectory()) {
                                        URIOpen.browse(conn.generateTempUrlWithExpirationInMs("GET", //$NON-NLS-1$
                                                                            System.currentTimeMillis() + 180000,
                                                                            f.getUnixPathWithContainer(),
                                                                            false));
                                    } else {
                                        URIOpen.browse(url.toURI());
                                    }
                                    // URIOpen.browse(url.toURI());
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            }
                        });

                    } catch (IOException err) {
                        err.printStackTrace();
                    }

                    path.setText(f.getUnixPathWithContainer());
                    showHideFileTab(f.getFile() != null ? numOfTabs + 1 : numOfTabs);
                    if (f.getFile() != null) {

                        headersForFilesPanel.setHeaders(f.getFile().getMetaData());
                        updateFileAction = new ActionListener() {

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                applyModifiedHeadersOnFile.setEnabled(false);
                                final Map<String, String> headers = new HashMap<String, String>();
                                for (HeaderRow r : headersForFilesPanel.getModifiedHeaders()) {
                                    headers.put(r.getHeaderName(), r.getHeaderValue());
                                }
                                executor.submit(new Runnable() {

                                    @Override
                                    public void run() {

                                        try {
                                            conn.post(fileHandler, f.getContainerName(), f.getFile().getName(), headers);
                                            refreshSingleFile.actionPerformed(null);
                                        } catch (IOException err) {
                                            // ignored
                                            JOptionPane.showMessageDialog(HttpPropertiesPanel.this,
                                                                          err.getLocalizedMessage());
                                        }
                                    }

                                });

                            }
                        };
                    } else {
                        updateFileAction = new ActionListener() {

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                // NOOP
                            }
                        };

                    }
                    HttpPropertiesPanel.this.curlCommand.setText(curlCommand);
                    {
                        final ContainerObject container = (ContainerObject) f.getContainer();
                        if (container != null) {
                            updateContainerAction = new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    applyModifiedHeadersOnContainer.setEnabled(false);
                                    executor.submit(new Runnable() {

                                        @Override
                                        public void run() {

                                            Map<String, String> headers = new HashMap<String, String>();
                                            for (HeaderRow r : headersForContainerPanel.getModifiedHeaders()) {
                                                headers.put(r.getHeaderName(), r.getHeaderValue());
                                            }
                                            try {
                                                conn.post(fileHandler, container.getName(), null, headers);
                                                SwingUtilities.invokeLater(new Runnable() {

                                                    @Override
                                                    public void run() {
                                                        refreshAllAction.actionPerformed(null);
                                                    }

                                                });
                                            } catch (IOException err) {
                                                // ignored
                                                JOptionPane.showMessageDialog(HttpPropertiesPanel.this,
                                                                              err.getLocalizedMessage());
                                            }
                                        }

                                    });

                                }
                            };
                            containerName.setText(container.getName());
                            numberOfBytesInContainer.setText(Messages.getString("numberOfBytesInContainerSuffix", //$NON-NLS-1$
                                                                                FileSize.valueAsKMG(container.getNumberOfBytes())));
                            numberOfFilesInContainer.setText(Messages.getString("numberOfFilesInContainerSuffix", //$NON-NLS-1$
                                                                                FileSize.valueAsKMG(container.getNumberOfFiles())));

                            headersForContainerPanel.setHeaders(container.getHeaders());

                            {
                                String _acl = container.getReadAcls();
                                acl = _acl == null ? "" : _acl; //$NON-NLS-1$
                                aclParser = new ACLParser(acl);
                                readACL.setText(acl);
                                docListener.changedUpdate(null);
                            }
                        }
                    }
                }

            });
        }
    }

    public void refreshCurrentFile(final FileIFace file) {
        refreshVirtualFile(this.currentVirtualFile, file);
    }

    @Override
    public void valueChanged(ListSelectionEvent arg0) {
        int row = table.getSelectedRow();
        if (row >= 0) {
            final VirtualFile f = (VirtualFile) table.getValueAt(row, 0);
            refreshVirtualFile(f, f.getFile());
        } else {
            refreshVirtualFile(null, null);
        }
        // metaCollapse.setCollapsed(true);
    }

}
