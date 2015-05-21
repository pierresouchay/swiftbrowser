/**
 * $LastChangedBy: souchay $ - $LastChangedDate: 2015-05-21 11:35:47 -0700 (Jeu, 21 mai 2015) $
 */
package net.souchay.swift.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.CellEditorListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import net.souchay.swift.downloads.Md5Comparator;
import net.souchay.swift.gui.ContainerIFace.FileIFace;
import net.souchay.swift.gui.SwiftToVirtualFiles.OnResult;
import net.souchay.swift.gui.actions.CheckUpdatesAction;
import net.souchay.swift.gui.actions.CreateVirtualDirectoryAction;
import net.souchay.swift.gui.actions.DeleteVirtualFileAction;
import net.souchay.swift.gui.actions.InstallDesktopFileAction;
import net.souchay.swift.gui.actions.RenameVirtualFileAction;
import net.souchay.swift.gui.actions.SaveAsAction;
import net.souchay.swift.gui.actions.ShareAction;
import net.souchay.swift.gui.actions.ShareUploadAction;
import net.souchay.swift.gui.configuration.SwiftConfigurationEditor;
import net.souchay.swift.gui.dnd.Constants;
import net.souchay.swift.gui.dnd.FileTreeDragSource;
import net.souchay.swift.gui.dnd.FileTreeDragSource.FileTreeNode;
import net.souchay.swift.gui.dnd.GlobalExecutorService;
import net.souchay.swift.gui.dnd.JTransferableTree;
import net.souchay.swift.net.DefaultSwiftConnectionResult;
import net.souchay.swift.net.FsConnection;
import net.souchay.swift.net.FsConnection.DownloadStatus;
import net.souchay.swift.net.FsConnection.NoNeedToDownloadException;
import net.souchay.swift.net.FsConnection.OnFileDownloaded;
import net.souchay.swift.net.SwiftConfiguration;
import net.souchay.swift.net.SwiftConnectionBuilder;
import net.souchay.swift.net.SwiftConnectionResultHandler;
import net.souchay.swift.net.SwiftConnections;
import net.souchay.swift.net.SwiftJSonCredentials;
import net.souchay.swift.net.SwiftTenant;
import net.souchay.utilities.Application;
import net.souchay.utilities.Application.ApplicationConfiguration;
import net.souchay.utilities.Application.MacOSXHandle;
import net.souchay.utilities.TempUtils;
import net.souchay.utilities.URIOpen;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.VerticalLayout;

/**
 * Main Entry point for Swift Browser.
 * 
 * The whole application is started from here.
 * 
 * Was first a test project for learning Swift... so code structure is not perfect :-)
 * 
 * @copyright Pierre Souchay - 2013, 2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3925 $
 */
public class SwiftMain {

    /**
     * Default Encoding for everyone :-)
     */
    private final static String UTF_8 = "UTF-8"; //$NON-NLS-1$

    static {
        try {
            System.setProperty("file.encoding", UTF_8); //$NON-NLS-1$ 
            Field charset = Charset.class.getDeclaredField("defaultCharset"); //$NON-NLS-1$
            charset.setAccessible(true);
            charset.set(null, null);
        } catch (Throwable err) {
            err.printStackTrace();
        }
    }

    /**
     * Public version
     */
    public static String VERSION_SVN = "$Revision: 3925 $"; //$NON-NLS-1$

    /**
     * Private Key regeneration
     * 
     * @param conn
     * @param fileHandler
     * @throws IOException
     */
    private final static void doRegenerateSecretKey1(SwiftConnections conn, SwiftConnectionResultHandler fileHandler)
            throws IOException {
        conn.generate_account_meta_temp_url_key(fileHandler);
    }

    private final static void doRegenerateSecretKey2(SwiftConnections conn, SwiftConnectionResultHandler fileHandler)
            throws IOException {
        conn.generate_account_meta_temp_url_key2(fileHandler);
    }

    private final static void regenerateSecretKeyIfNeeded(SwiftConnections conn,
            SwiftConnectionResultHandler fileHandler) throws IOException {
        if (conn.isUsingFixedContainers())
            return;
        if (conn.getX_account_meta_temp_url_key2() == null) {
            doRegenerateSecretKey2(conn, fileHandler);
        }
        if (conn.getX_account_meta_temp_url_key() == null) {
            doRegenerateSecretKey1(conn, fileHandler);
        }
    }

    private static JFrame aboutWindows;

    private final static Action aboutAction = new AbstractAction() {

        /**
         * 
         */
        private static final long serialVersionUID = -7195896745467092294L;

        {
            final String name = Messages.getString("about.title"); //$NON-NLS-1$
            putValue(Action.NAME, name);
            putValue(Action.SHORT_DESCRIPTION, name);
            putValue(Action.SMALL_ICON, SwiftConfigurationEditor.loadIcon("about", name)); //$NON-NLS-1$
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            showAbout();
        }
    };

    private static volatile Action updateAction = null;

    private final static ImageIcon refreshIcon = SwiftConfigurationEditor.loadIcon("refresh", "refresh"); //$NON-NLS-1$ //$NON-NLS-2$

    public static Action getUpdateAction() {
        if (updateAction == null) {
            updateAction = new CheckUpdatesAction(getVersion(), getUserAgent());
            updateAction.putValue(Action.SMALL_ICON, refreshIcon);
        }
        return updateAction;
    }

    private static String _VERSION = null;

    private final static String APPLICATION_NAME = "SwiftBrowser";//$NON-NLS-1$

    private static final String _userAgent;

    static {
        String ua;
        try {
            ua = MessageFormat.format(System.getProperty("userAgent", "{0}/{1} ({2} {3}-{4} ; {5})"), //$NON-NLS-1$//$NON-NLS-2$
                                      APPLICATION_NAME,
                                      getVersion(),
                                      System.getProperty("os.name"), //$NON-NLS-1$
                                      System.getProperty("os.arch"), //$NON-NLS-1$
                                      System.getProperty("os.version"), //$NON-NLS-1$
                                      Locale.getDefault().toString());
        } catch (IllegalArgumentException e) {
            ua = APPLICATION_NAME;
        }
        _userAgent = ua;
    }

    /**
     * Get the User-Agent
     * 
     * @return The User-Agent used in HTTP Requests
     */
    public static String getUserAgent() {
        return _userAgent;
    }

    public static String getVersion() {
        if (_VERSION != null)
            return _VERSION;
        String vx = ""; //$NON-NLS-1$
        InputStream in = null;
        try {
            in = SwiftMain.class.getResourceAsStream("/net/souchay/swift/currentversion.txt"); //$NON-NLS-1$
            if (in != null) {
                BufferedReader r = null;
                try {
                    r = new BufferedReader(new InputStreamReader(in, UTF_8));
                    String v = r.readLine();
                    vx += v;
                } catch (IOException ignored) {
                    ignored.printStackTrace();
                } finally {
                    if (r != null)
                        try {
                            r.close();
                            in = null;
                        } catch (IOException e) {
                            LOG.log(Level.SEVERE, "cannot read version: " + e.getMessage(), e); //$NON-NLS-1$
                        }
                }
            }
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException e) {
                    LOG.log(Level.SEVERE, "cannot close stream for version", e); //$NON-NLS-1$
                }
        }
        vx += " - " + VERSION_SVN; //$NON-NLS-1$
        _VERSION = vx;
        return vx;
    }

    /**
     * Show about window
     */
    private final static void showAbout() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (aboutWindows != null) {
                    aboutWindows.setVisible(true);
                    return;
                }
                aboutWindows = new JFrame(Messages.getString("about.title")); //$NON-NLS-1$
                aboutWindows.setIconImage(icon.getImage());
                JPanel jp = new JPanel(new BorderLayout(25, 25));
                jp.setMinimumSize(new Dimension(320, 240));
                JLabel view = new JLabel(icon);
                view.setOpaque(false);
                view.setMinimumSize(new Dimension(128, 128));
                jp.add(view, BorderLayout.WEST);
                JEditorPane text = new JEditorPane("text/html", Messages.getString("about.text", getVersion())); //$NON-NLS-1$ //$NON-NLS-2$
                text.setEditable(false);
                text.setOpaque(false);
                text.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 25));
                text.addHyperlinkListener(new HyperlinkListener() {

                    @Override
                    public void hyperlinkUpdate(HyperlinkEvent e) {
                        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                            try {
                                URIOpen.browse(e.getURL().toURI());
                            } catch (IOException e1) {
                                LOG.log(Level.WARNING,
                                        "IOException: Cannot browse URI: " + e.getURL() + ":=" + e1.getLocalizedMessage(), e1); //$NON-NLS-1$ //$NON-NLS-2$
                                e1.printStackTrace();
                            } catch (URISyntaxException e1) {
                                LOG.log(Level.WARNING,
                                        "URISyntaxException: Cannot browse URI: " + e.getURL() + ":=" + e1.getLocalizedMessage(), e1); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        }
                    }
                });
                GlobalExecutorService.incrementReferences();
                jp.add(text, BorderLayout.CENTER);
                {
                    JPanel south = new JPanel(new BorderLayout(25, 25));
                    JButton btn = new JButton(Messages.getString("close")); //$NON-NLS-1$
                    btn.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            aboutWindows.dispatchEvent(new WindowEvent(aboutWindows, WindowEvent.WINDOW_CLOSING));
                        }
                    });
                    JButton updates = new JButton(getUpdateAction());
                    south.add(updates, BorderLayout.WEST);
                    south.add(btn, BorderLayout.EAST);
                    jp.add(south, BorderLayout.SOUTH);
                }
                aboutWindows.setContentPane(jp);
                aboutWindows.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                aboutWindows.addWindowListener(new WindowAdapter() {

                    /**
                     * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
                     */
                    @Override
                    public void windowClosed(WindowEvent e) {
                        super.windowClosed(e);
                        if (aboutWindows != null && aboutWindows.isDisplayable())
                            aboutWindows.dispose();
                        aboutWindows = null;
                        GlobalExecutorService.decrementReferences();
                    }

                });
                aboutWindows.pack();
                aboutWindows.setLocationByPlatform(true);
                aboutWindows.setVisible(true);
            }

        });

    }

    private final static Logger LOG = Logger.getLogger(SwiftMain.class.getName());

    private static void doConnect(SwiftConfiguration config) throws IOException {
        final boolean useHttp = true;
        boolean hasSuccess = false;
        List<IOException> errorsAtStartup = new LinkedList<IOException>();
        for (final SwiftConnections conn : SwiftConnectionBuilder.create(getUserAgent(), config)) {
            GlobalExecutorService.incrementReferences();
            try {
                if (useHttp) {
                    conn.auth();
                }
            } catch (IOException err) {
                errorsAtStartup.add(err);
                LOG.log(Level.WARNING, "Failed to authenticate[" + err.getClass().getName() + "]:" + err.getMessage(), //$NON-NLS-1$//$NON-NLS-2$
                        err);
                continue;
            }
            hasSuccess = true;
            final FileIfaceFactory fileFactory = new FileIfaceFactory(conn.getTenant());
            final ErrorsHandler errorsHandler = new ErrorsHandler();
            final ScheduledExecutorService executor = GlobalExecutorService.getExecutorService();
            final List<VirtualFileAction> actionsOnFiles = new LinkedList<VirtualFileAction>();
            final JPopupMenu contextualMenu = new JPopupMenu();
            final TreeCellRenderer virtualFileRenderer = new DefaultTreeCellRenderer() {

                /**
                 * Serialization
                 */
                private static final long serialVersionUID = -2062451677464143078L;

                private int LOCKED = 1;

                private int WEB = 2;

                private ImageIcon WEB_ICON = SwiftConfigurationEditor.loadIcon("web", "Web"); //$NON-NLS-1$//$NON-NLS-2$

                private ImageIcon LOCKED_ICON = SwiftConfigurationEditor.loadIcon("lock", "Locked");//$NON-NLS-1$//$NON-NLS-2$

                private Map<Integer, ImageIcon> icons = new HashMap<Integer, ImageIcon>();

                private ImageIcon containerImageDefault = SwiftConfigurationEditor.loadIcon("container_shared", //$NON-NLS-1$
                                                                                            "Container"); //$NON-NLS-1$

                /**
                 * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree,
                 *      java.lang.Object, boolean, boolean, boolean, int, boolean)
                 */
                @Override
                public Component getTreeCellRendererComponent(final JTree tree, final Object value, boolean sel,
                        boolean expanded, boolean leaf, int row, boolean hasFocus) {
                    final VirtualFile val = (VirtualFile) value;
                    JComponent c = (JComponent) super.getTreeCellRendererComponent(tree,
                                                                                   val.getName(),
                                                                                   sel,
                                                                                   expanded,
                                                                                   leaf,
                                                                                   row,
                                                                                   hasFocus);
                    final FileIFace iface = val.getFile();
                    if (!val.isDirectory()) {
                        ((JLabel) c).setIcon(SwiftConfigurationEditor.loadMimeIcon(iface.getContentType()));
                        if (iface.isLargeObject()) {
                            //((JLabel) c).setIcon(UIManager.getIcon("FileView.hardDriveIcon")); //$NON-NLS-1$
                            c.setToolTipText(Messages.getString("largeObject", iface.getName())); //$NON-NLS-1$
                        } else {
                            c.setToolTipText(iface.getName());
                        }
                    } else {
                        if (val instanceof ContainerVirtualFile) {
                            ContainerVirtualFile cv = (ContainerVirtualFile) val;
                            ContainerIFace cont = cv.getContainer();
                            final int selector;
                            if (cont == null) {
                                selector = 0;
                                c.setToolTipText(null);
                            } else {
                                c.setToolTipText(Messages.getString("containerTooltip", //$NON-NLS-1$
                                                                    cv.getName(),
                                                                    cont.getUrl(),
                                                                    cont.getNumberOfFiles(),
                                                                    cont.getNumberOfBytes(),
                                                                    (cont.isShared() ? Messages.getString("anybodyCanRead") : Messages.getString("protectedAccess")), //$NON-NLS-1$ //$NON-NLS-2$
                                                                    (cont.isWebListingEnabled() ? Messages.getString("webListingsSupport") : Messages.getString("webListingsSupportDisabled")) //$NON-NLS-1$ //$NON-NLS-2$
                                ));
                                selector = (cont.isWebListingEnabled() ? WEB : 0) + (cont.isShared() ? 0 : LOCKED);
                            }
                            ImageIcon icon = icons.get(selector);
                            if (icon == null) {
                                icon = containerImageDefault;
                                int w = icon.getIconHeight();
                                int h = icon.getIconWidth();
                                if (w < 1 || h < 1 || icon.getImageLoadStatus() != MediaTracker.COMPLETE)
                                    return c;
                                BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                                Graphics2D g = img.createGraphics();
                                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                                   RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                                icon.paintIcon(c, g, 0, 0);
                                if ((WEB & selector) == WEB) {
                                    WEB_ICON.paintIcon(c,
                                                       g,
                                                       w - WEB_ICON.getIconWidth(),
                                                       (h - WEB_ICON.getIconHeight()) / 2);
                                }
                                if ((LOCKED & selector) == LOCKED) {
                                    LOCKED_ICON.paintIcon(c, g, 0, (h - LOCKED_ICON.getIconHeight()) / 2);
                                }
                                g.dispose();
                                icon = new ImageIcon(img);
                                icons.put(selector, icon);
                            }
                            ((JLabel) c).setIcon(icon);
                        } else {
                            c.setToolTipText(val.getName());
                            ((JLabel) c).setIcon(UIManager.getIcon("FileView.directoryIcon")); //$NON-NLS-1$
                        }
                    }
                    return c;
                }

            };
            final SwiftTreeModel model = new SwiftTreeModel(conn.getTenant().getId());
            final SwiftToVirtualFiles guiBindings = new SwiftToVirtualFiles(model);
            // final JXTreeTable table = new JXTreeTable(model);
            final JTransferableTree table = new JTransferableTree(conn, model, null);

            final DefaultSwiftConnectionResult fileHandler = new DefaultSwiftConnectionResult(table, null);
            final SwiftConnectionsDownload importListener = new SwiftConnectionsDownload(executor,
                                                                                         fileHandler,
                                                                                         conn,
                                                                                         guiBindings,
                                                                                         table,
                                                                                         fileFactory);
            table.setFileListener(importListener);
            table.setRowHeight(22);
            table.setSwiftConnectionResult(fileHandler);
            final VirtualFileAction deleteFileAction = new DeleteVirtualFileAction(executor,
                                                                                   guiBindings,
                                                                                   fileHandler,
                                                                                   table,
                                                                                   conn);
            actionsOnFiles.add(deleteFileAction);

            final JXCollapsiblePane searchCollapse = new JXCollapsiblePane();

            final VirtualFileAction share = new ShareAction(errorsHandler, table, conn);

            actionsOnFiles.add(share);

            final VirtualFileAction openInBrowser = new VirtualFileAction() {

                /**
                 * 
                 */
                private static final long serialVersionUID = -8532124415472997944L;

                final String name = Messages.getString("openInBrowser"); //$NON-NLS-1$
                {
                    setEnabled(false);
                    putValue(Action.NAME, name);
                    putValue(Action.SHORT_DESCRIPTION, name);
                    putValue(Action.SMALL_ICON, SwiftConfigurationEditor.loadIcon("openInBrowser", name)); //$NON-NLS-1$
                }

                @Override
                public void actionPerformed(ActionEvent e) {

                    int srow = table.getSelectedRow();
                    if (srow >= 0) {
                        // Expiration time will work for 2 hours
                        final long expires = System.currentTimeMillis() + 7200000;
                        for (int row : table.getSelectedRows()) {
                            VirtualFile f = (VirtualFile) table.getValueAt(row, 0);
                            if (f != null && !f.isDirectory()) {
                                try {
                                    URIOpen.browse(conn.generateTempUrlWithExpirationInMs("GET", //$NON-NLS-1$
                                                                                          expires,
                                                                                          f.getFile().getContainer()
                                                                                                  + FsConnection.URL_PATH_SEPARATOR
                                                                                                  + f.getFile()
                                                                                                     .getName(),
                                                                                          false));
                                } catch (IOException e1) {
                                    LOG.log(Level.WARNING, "Cannot open temporary URL: " + e1.getMessage(), e1); //$NON-NLS-1$
                                } catch (InvalidKeyException err) {
                                    errorsHandler.handleInvalidKeyException(table, err);
                                }
                            }
                        }

                    }

                }

                @Override
                public void enable(VirtualFile... selectedPaths) {
                    setEnabled(selectedPaths.length == 1 && !selectedPaths[0].isDirectory());
                }
            };
            actionsOnFiles.add(openInBrowser);

            final Action expand = new AbstractAction() {

                /**
                 *
                 */
                private static final long serialVersionUID = 1149525794322420755L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    final int row = table.getSelectedRow();
                    if (-1 != row) {
                        if (!table.isExpanded(row))
                            table.expandRow(row);
                        else
                            table.collapseRow(row);
                    }
                }
            };

            final VirtualFileAction editAction = new VirtualFileAction() {

                /**
                 * Serialization
                 */
                private static final long serialVersionUID = -1020289804402417405L;

                {
                    String name = Messages.getString("edit"); //$NON-NLS-1$
                    putValue(Action.NAME, name);
                    putValue(Action.SHORT_DESCRIPTION, name);
                    putValue(Action.SMALL_ICON, SwiftConfigurationEditor.loadIcon("edit", name)); //$NON-NLS-1$
                    {
                        setEnabled(false);
                    }
                }

                @Override
                public void actionPerformed(ActionEvent e) {

                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        VirtualFile f = (VirtualFile) table.getValueAt(row, 0);
                        if (!f.isDirectory()) {
                            final String path = f.getFile().getName();
                            final String container = f.getFile().getContainer().getName();
                            executor.execute(new Runnable() {

                                @Override
                                public void run() {
                                    try {
                                        conn.get(fileHandler, container, path, new OnFileDownloaded() {

                                            @Override
                                            public void onDownload(File f, String container, String path,
                                                    DownloadStatus success) {
                                                if (!(DownloadStatus.ERROR.equals(success)) && f.exists()
                                                    && f.canRead() && Desktop.isDesktopSupported()) {
                                                    try {
                                                        Desktop.getDesktop().open(f);
                                                    } catch (Exception e1) {
                                                        try {
                                                            Desktop.getDesktop().edit(f);
                                                        } catch (Exception e2) {
                                                            try {
                                                                URIOpen.browse(f.toURI());
                                                            } catch (IOException e3) {
                                                                LOG.log(Level.WARNING,
                                                                        "Cannot browse URI: " + e3.getMessage(), e3); //$NON-NLS-1$
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            @Override
                                            public File onStartDownload(String container, String path, int totalLengh,
                                                    long lastModified, String eTag) throws IOException,
                                                    NoNeedToDownloadException {
                                                File tempDir = TempUtils.getTempDir();
                                                String ext = ".dat"; //$NON-NLS-1$
                                                if (path != null && !path.isEmpty()) {
                                                    int idx = path.lastIndexOf('.');
                                                    if (idx > 0)
                                                        ext = path.substring(idx);
                                                }
                                                File f;
                                                if (eTag != null && !eTag.isEmpty()) {
                                                    f = new File(tempDir, eTag + ext);
                                                    Md5Comparator md5 = Md5Comparator.getInstance();
                                                    try {
                                                        md5.cancelIfDownloadCanBeSkipped(f, totalLengh, eTag);
                                                    } finally {
                                                        md5.close();
                                                    }
                                                    return f;
                                                } else {
                                                    return File.createTempFile("temp_", ext); //$NON-NLS-1$
                                                }
                                            }

                                        },
                                                 -1);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } else {
                            expand.actionPerformed(e);
                        }
                    }

                }

                @Override
                public void enable(VirtualFile... selectedPaths) {
                    boolean enable = selectedPaths.length == 1 && !selectedPaths[0].isDirectory();
                    setEnabled(enable);
                }
            };

            final Action refreshAllFilesThatNeedsRefresh = new AbstractAction() {

                /**
                 * 
                 */
                private static final long serialVersionUID = -6879745838437765054L;

                private AtomicInteger concurrent = new AtomicInteger(0);

                @Override
                public void actionPerformed(ActionEvent e) {
                    int v = concurrent.incrementAndGet();
                    if (v < 2)
                        executor.schedule(new Callable<Boolean>() {

                            @Override
                            public Boolean call() throws Exception {
                                concurrent.set(0);
                                int v = 1;
                                do {
                                    try {
                                        List<FileIFace> toRefresh = new ArrayList<FileIFace>(guiBindings.getFilesToRefresh());
                                        for (FileIFace fx : toRefresh) {
                                            final FileIFace fxx = fx;
                                            GlobalExecutorService.getExecutorService().submit(new Runnable() {

                                                @Override
                                                public void run() {
                                                    importListener.refresh(fxx);
                                                }
                                            });
                                        }
                                    } finally {
                                        v = concurrent.decrementAndGet();
                                        if (v > 1)
                                            v = 1;
                                    }
                                } while (v > 0);
                                return Boolean.TRUE;
                            }

                        },
                                          1000,
                                          TimeUnit.MILLISECONDS);
                }

            };

            actionsOnFiles.add(editAction);

            contextualMenu.add(editAction);
            contextualMenu.add(openInBrowser);
            contextualMenu.add(share);

            {
                // InputMap input = table.getInputMap();
                // input.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, true), deleteFileAction);
                // input.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0, true), deleteFileAction);

                // input.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true), expand);
                //
                table.addKeyListener(new KeyAdapter() {

                    @Override
                    public void keyReleased(KeyEvent evt) {
                        int keyCode = evt.getKeyCode();
                        if (keyCode == KeyEvent.VK_DELETE || keyCode == KeyEvent.VK_BACK_SPACE) {
                            deleteFileAction.actionPerformed(null);
                        } else if (keyCode == KeyEvent.VK_SPACE) {
                            expand.actionPerformed(null);
                        }
                    }

                });
            }
            table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    if (e.getValueIsAdjusting())
                        return;
                    final int rows[] = table.getSelectedRows();
                    VirtualFile files[] = new VirtualFile[rows.length];
                    for (int i = 0; i < rows.length; i++) {
                        files[i] = (VirtualFile) table.getValueAt(rows[i], 0);
                    }
                    for (VirtualFileAction a : actionsOnFiles) {
                        a.enable(files);
                    }
                }
            });
            table.setTreeCellRenderer(virtualFileRenderer);
            table.setDefaultRenderer(Long.class, new FileSizeTreeRenderer());
            table.setDefaultRenderer(Date.class, new DefaultTableCellRenderer() {

                /**
             * 
             */
                private static final long serialVersionUID = -6741177460035749175L;

                private final DateFormat df = DateFormat.getDateTimeInstance();

                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                        boolean hasFocus, int row, int column) {
                    Object val = value;
                    if (value != null && (value instanceof Long)) {
                        val = df.format(new Date((Long) value));
                    }
                    return super.getTableCellRendererComponent(table, val, isSelected, hasFocus, row, column);
                }
            });

            final AbstractAction refreshAllAction = new AbstractAction() {

                /**
                 * 
                 */
                private static final long serialVersionUID = -1149967551813131613L;

                private final String name = Messages.getString("refresh"); //$NON-NLS-1$

                {
                    putValue(Action.NAME, name);
                    putValue(Action.SHORT_DESCRIPTION, name);
                    putValue(Action.SMALL_ICON, refreshIcon);
                }

                @Override
                public void actionPerformed(ActionEvent e) {
                    Runnable r = new Runnable() {

                        @Override
                        public void run() {
                            try {
                                Collection<ObjectIFace> containers = conn.listRoots(fileHandler, fileFactory);
                                List<Callable<Collection<ObjectIFace>>> results = new LinkedList<Callable<Collection<ObjectIFace>>>();
                                for (ObjectIFace container : containers) {
                                    final ContainerObject cont = fileFactory.getContainer(container.getName());
                                    results.add(new Callable<Collection<ObjectIFace>>() {

                                        @Override
                                        public Collection<ObjectIFace> call() throws IOException {
                                            guiBindings.addContainer(cont);
                                            Collection<ObjectIFace> files = conn.list(fileHandler, cont);
                                            cont.setFiles(files);
                                            SwingUtilities.invokeLater(new Runnable() {

                                                @Override
                                                public void run() {
                                                    Timer t = new Timer(300, new ActionListener() {

                                                        @Override
                                                        public void actionPerformed(ActionEvent e) {
                                                            refreshAllFilesThatNeedsRefresh.actionPerformed(null);
                                                        }
                                                    });
                                                    t.setRepeats(false);
                                                    t.start();

                                                }

                                            });
                                            return files;
                                        }
                                    });

                                }
                                executor.invokeAll(results);
                                refreshAllFilesThatNeedsRefresh.actionPerformed(null);
                                regenerateSecretKeyIfNeeded(conn, fileHandler);
                            } catch (IOException err) {
                                Logger.getLogger("io.executor").log(Level.WARNING, //$NON-NLS-1$
                                                                    "Failed to refresh roots containers", //$NON-NLS-1$
                                                                    err);
                            } catch (InterruptedException err) {
                                Logger.getLogger("io.executor").log(Level.WARNING, //$NON-NLS-1$
                                                                    "Interrupted while refreshing roots containers", //$NON-NLS-1$
                                                                    err);
                            }
                        }
                    };
                    setEnabled(false);
                    try {
                        executor.execute(r);
                    } finally {
                        setEnabled(true);
                    }

                }
            };

            final VirtualFileAction friendsUpload = new ShareUploadAction(executor,
                                                                          guiBindings,
                                                                          errorsHandler,
                                                                          fileHandler,
                                                                          conn,
                                                                          table,
                                                                          refreshAllAction);
            actionsOnFiles.add(friendsUpload);

            contextualMenu.add(friendsUpload);
            contextualMenu.addSeparator();
            contextualMenu.add(deleteFileAction);

            table.setComponentPopupMenu(contextualMenu);

            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    table.addMouseListener(new MouseAdapter() {

                        /**
                         * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
                         */
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (e.isPopupTrigger()) {
                                contextualMenu.show(table, e.getX(), e.getY());
                            } else if (e.getClickCount() == 2) {
                                editAction.actionPerformed(null);
                            }
                        }

                    });
                    String tenantName = null;
                    String tenantId = null;
                    String tenantDescription = null;
                    try {
                        SwiftTenant t = conn.getTenant();
                        tenantName = t.getName();
                        tenantId = t.getId();
                        tenantDescription = t.getDescription();
                    } catch (IOException err) {
                        err.printStackTrace();
                    }
                    final JXFrame jf = new JXFrame(Messages.getString("SwiftMain.applicationTitle", tenantId, tenantName, tenantDescription)); //$NON-NLS-1$

                    jf.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    final JDialog metaDataFrame = new JDialog(jf, Messages.getString("SwiftMain.httpHeaders")); //$NON-NLS-1$

                    final JDialog showLogsFrame = new JDialog(jf, Messages.getString("SwiftMain.logsWindow")); //$NON-NLS-1$
                    {
                        JXTable table = new JXTable(conn.getConnectionsTableModel());

                        JScrollPane p = new JScrollPane(table);
                        table.setDefaultRenderer(Date.class, new DefaultTableCellRenderer() {

                            /**
                             * 
                             */
                            private static final long serialVersionUID = -7725646613022199299L;

                            private final DateFormat df = DateFormat.getTimeInstance(DateFormat.MEDIUM);

                            @Override
                            public Component getTableCellRendererComponent(JTable table, Object value,
                                    boolean isSelected, boolean hasFocus, int row, int column) {
                                return super.getTableCellRendererComponent(table,
                                                                           df.format((Date) value),
                                                                           isSelected,
                                                                           hasFocus,
                                                                           row,
                                                                           column);
                            }

                        });
                        p.setPreferredSize(new Dimension(640, 320));
                        showLogsFrame.setContentPane(p);
                        showLogsFrame.pack();
                    }
                    JScrollPane stable = new JScrollPane(table);

                    final JXStatusBar theStatusBar = new JXStatusBar();

                    JMenuBar menubar = new JMenuBar();
                    final AbstractAction createContainer = new AbstractAction() {

                        /**
                         * Serialization
                         */
                        private static final long serialVersionUID = -9204327849178554954L;

                        private final String name = Messages.getString("createContainer"); //$NON-NLS-1$
                        {
                            putValue(Action.SHORT_DESCRIPTION, name);
                            putValue(Action.SMALL_ICON, SwiftConfigurationEditor.loadIcon("new", name)); //$NON-NLS-1$
                            putValue(Action.NAME, name);
                        }

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            String value = JOptionPane.showInputDialog(jf,
                                                                       Messages.getString("pleaseEnterANewContainerName"), //$NON-NLS-1$
                                                                       Messages.getString("exampleContainerName")); //$NON-NLS-1$
                            if (value != null && !value.trim().isEmpty()) {
                                value = value.trim();
                                final String newContainer = value;
                                executor.execute(new Runnable() {

                                    @Override
                                    public void run() {
                                        try {
                                            conn.put(fileHandler, newContainer, 0, null, null, null);
                                            refreshAllAction.actionPerformed(null);
                                        } catch (IOException err) {
                                            Logger.getLogger("io.executor").log(Level.WARNING, //$NON-NLS-1$
                                                                                "Failed to create new Container " + newContainer, //$NON-NLS-1$
                                                                                err);
                                        } catch (Throwable t) {
                                            Logger.getLogger("io.executor").log(Level.WARNING, //$NON-NLS-1$
                                                                                "Failed to create new Container " + newContainer, //$NON-NLS-1$
                                                                                t);
                                        }
                                    }
                                });
                            }
                        }
                    };
                    final AbstractAction inspectAction = new AbstractAction() {

                        /**
                         * 
                         */
                        private static final long serialVersionUID = -1628591280611211151L;

                        final String name = Messages.getString("inspectHttpHeaders"); //$NON-NLS-1$

                        {
                            putValue(Action.NAME, name);
                            putValue(Action.SHORT_DESCRIPTION, name);
                            putValue(Action.SMALL_ICON, SwiftConfigurationEditor.loadIcon("zoom", name)); //$NON-NLS-1$
                        }

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            metaDataFrame.setVisible(true);
                        }
                    };
                    final AbstractAction showLogsAction = new AbstractAction() {

                        /**
                         * 
                         */
                        private static final long serialVersionUID = -1628591280611211151L;

                        final String name = Messages.getString("showLogs"); //$NON-NLS-1$

                        {
                            putValue(Action.NAME, name);
                            putValue(Action.SHORT_DESCRIPTION, name);
                            putValue(Action.SMALL_ICON, SwiftConfigurationEditor.loadIcon("listing", name)); //$NON-NLS-1$
                        }

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            showLogsFrame.setVisible(true);
                        }
                    };
                    showLogsFrame.addWindowListener(new WindowAdapter() {

                        /**
                         * @see java.awt.event.WindowAdapter#windowOpened(java.awt.event.WindowEvent)
                         */
                        @Override
                        public void windowOpened(WindowEvent e) {
                            showLogsAction.setEnabled(false);
                            super.windowOpened(e);
                        }

                        /**
                         * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
                         */
                        @Override
                        public void windowClosing(WindowEvent e) {
                            showLogsAction.setEnabled(true);
                            super.windowClosing(e);
                        }

                    });
                    {
                        {
                            final Map<String, JLabel> statusLabels = new HashMap<String, JLabel>();
                            for (String s : SwiftConnections.X_ACCOUNT_PROPERTIES_INT) {
                                try {
                                    String translation = Messages.getStringWithException(s);
                                    Long x = conn.getAccountPropertyAsLong(s);
                                    if (x == null)
                                        translation = Messages.getString(s + "-NA"); //$NON-NLS-1$
                                    else
                                        translation = Messages.getString(s, FileSize.valueAsKMG(x));

                                    JLabel lbl = new JLabel(translation);
                                    lbl.setToolTipText(translation);
                                    statusLabels.put(s, lbl);
                                    JXStatusBar.Constraint constraints = new JXStatusBar.Constraint();
                                    // constraints.setFixedWidth(160);
                                    theStatusBar.add(lbl, constraints);
                                } catch (MissingResourceException err) {
                                }
                            }
                            // JXStatusBar.Constraint constraints = new JXStatusBar.Constraint();
                            JXStatusBar.Constraint c2 = new JXStatusBar.Constraint(JXStatusBar.Constraint.ResizeBehavior.FILL); // Fill
                                                                                                                                // with
                                                                                                                                // no
                                                                                                                                // inserts
                            {
                                final String searchText = Messages.getString("searchDefaultText"); //$NON-NLS-1$
                                final JTextField field = new JTextField(searchText);

                                final Color originalColor = field.getForeground();
                                field.setForeground(Color.LIGHT_GRAY);
                                field.addFocusListener(new FocusListener() {

                                    @Override
                                    public void focusLost(FocusEvent e) {
                                        if (field.getText() == null || field.getText().isEmpty()) {
                                            field.setForeground(Color.LIGHT_GRAY);
                                            field.setText(searchText);
                                        }
                                    }

                                    @Override
                                    public void focusGained(FocusEvent e) {
                                        if (searchText.equals(field.getText())) {
                                            field.setText(""); //$NON-NLS-1$
                                            field.setForeground(originalColor);
                                        }
                                    }
                                });
                                field.setMinimumSize(field.getPreferredSize());
                                JPanel jp = new JPanel(new BorderLayout());
                                jp.add(field);

                                final JProgressBar bar = new JProgressBar();
                                bar.setStringPainted(true);
                                bar.setPreferredSize(new Dimension(64, 8));
                                final ArrayList<VirtualFile> allResults = new ArrayList<VirtualFile>(128);
                                final AtomicInteger currentIndex = new AtomicInteger(0);
                                final JLabel searchResults = new JLabel(Messages.getString("searchResults", 100, 1000)); //$NON-NLS-1$
                                final AbstractAction previous = new AbstractAction() {

                                    /**
                                     * 
                                     */
                                    private static final long serialVersionUID = -1466835419771701968L;

                                    {
                                        final String name = Messages.getString("previous"); //$NON-NLS-1$
                                        putValue(Action.NAME, name);
                                        putValue(Action.SHORT_DESCRIPTION, name);
                                        putValue(Action.SMALL_ICON, SwiftConfigurationEditor.loadIcon("up", name)); //$NON-NLS-1$
                                        setEnabled(false);
                                    }

                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        if (currentIndex.get() > 0) {
                                            VirtualFile f = allResults.get(currentIndex.decrementAndGet());
                                            TreePath path = f.getPath();
                                            table.collapseAll();
                                            table.expandPath(path);
                                            table.getTreeSelectionModel().setSelectionPath(path);
                                            table.scrollPathToVisible(path);
                                            putValue("currentIndex", currentIndex.get()); //$NON-NLS-1$
                                            searchResults.setText(Messages.getString("searchResults", //$NON-NLS-1$
                                                                                     currentIndex.get() + 1,
                                                                                     allResults.size()));

                                        }
                                        setEnabled(currentIndex.get() > 0);
                                    }
                                };
                                final AbstractAction next = new AbstractAction() {

                                    /**
                                     * 
                                     */
                                    private static final long serialVersionUID = -5482993366562821960L;

                                    {
                                        final String name = Messages.getString("next"); //$NON-NLS-1$
                                        putValue(Action.NAME, name);
                                        putValue(Action.SMALL_ICON, SwiftConfigurationEditor.loadIcon("down", name)); //$NON-NLS-1$
                                        putValue(Action.SHORT_DESCRIPTION, name);
                                        setEnabled(false);
                                        previous.addPropertyChangeListener(new PropertyChangeListener() {

                                            @Override
                                            public void propertyChange(PropertyChangeEvent evt) {
                                                recomputeActionEnabled();
                                            }
                                        });
                                    }

                                    private void recomputeActionEnabled() {
                                        setEnabled(currentIndex.get() < (allResults.size() - 1));
                                    }

                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        final int index = currentIndex.get();
                                        if (index < (allResults.size() - 1)) {
                                            VirtualFile f = allResults.get(currentIndex.incrementAndGet());
                                            TreePath path = f.getPath();
                                            table.collapseAll();
                                            table.expandPath(path);
                                            table.getTreeSelectionModel().setSelectionPath(path);
                                            table.scrollPathToVisible(path);
                                            previous.setEnabled(index >= 0);
                                            searchResults.setText(Messages.getString("searchResults", //$NON-NLS-1$
                                                                                     currentIndex.get() + 1,
                                                                                     allResults.size()));
                                        }
                                        recomputeActionEnabled();
                                    }
                                };
                                final JButton nextButton = new JButton(next);
                                {
                                    searchCollapse.setAnimated(true);
                                    JPanel hpInside = new JPanel(new HorizontalLayout(5));
                                    hpInside.add(searchResults);
                                    hpInside.setOpaque(false);
                                    {
                                        JButton btn = new JButton(previous);
                                        btn.setHideActionText(true);
                                        hpInside.add(btn);
                                        btn.setContentAreaFilled(false);
                                        btn.setOpaque(false);
                                        btn.setBorderPainted(false);
                                    }
                                    {

                                        nextButton.setHideActionText(true);
                                        hpInside.add(nextButton);
                                        nextButton.setContentAreaFilled(false);
                                        nextButton.setOpaque(false);
                                        nextButton.setBorderPainted(false);
                                    }
                                    {
                                        AbstractAction closeColl = new AbstractAction() {

                                            /**
                                             * 
                                             */
                                            private static final long serialVersionUID = -7819848970398210119L;

                                            {
                                                final String name = Messages.getString("close"); //$NON-NLS-1$
                                                putValue(Action.NAME, name);
                                                putValue(Action.SMALL_ICON,
                                                         SwiftConfigurationEditor.loadIcon("close", name)); //$NON-NLS-1$
                                            }

                                            @Override
                                            public void actionPerformed(ActionEvent e) {
                                                searchCollapse.setCollapsed(true);
                                            }
                                        };

                                        JPanel hp = new JPanel(new BorderLayout(5, 0));
                                        hp.setBackground(Color.ORANGE.darker());
                                        hp.add(hpInside, BorderLayout.CENTER);
                                        JButton btn = new JButton(closeColl);
                                        btn.setHideActionText(true);
                                        btn.setContentAreaFilled(false);
                                        btn.setOpaque(false);
                                        btn.setBorderPainted(false);
                                        hpInside.add(btn);
                                        hp.add(btn, BorderLayout.EAST);

                                        hp.add(bar, BorderLayout.WEST);
                                        searchCollapse.setContentPane(hp);

                                    }

                                    searchCollapse.setCollapsed(true);
                                }
                                theStatusBar.add(jp, c2);
                                field.addActionListener(new ActionListener() {

                                    SwingWorker<Collection<VirtualFile>, VirtualFile> lastWorker;

                                    private String lastRequest = ""; //$NON-NLS-1$

                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        final String newReq = field.getText().trim();
                                        if (newReq.equals(lastRequest)) {
                                            searchCollapse.setCollapsed(false);
                                            if (next.isEnabled())
                                                next.actionPerformed(null);
                                            return;
                                        }
                                        lastRequest = newReq;
                                        if (lastWorker != null) {
                                            lastWorker.cancel(true);
                                        }
                                        searchResults.setText(Messages.getString("noSearchResults", newReq)); //$NON-NLS-1$
                                        searchCollapse.setCollapsed(false);
                                        bar.setIndeterminate(true);
                                        allResults.clear();
                                        currentIndex.set(-1);
                                        previous.setEnabled(false);
                                        next.setEnabled(false);
                                        lastWorker = guiBindings.searchFor(field.getText(),
                                                                           new OnResult<Collection<VirtualFile>>() {

                                                                               @Override
                                                                               public void doProcess(
                                                                                       Collection<VirtualFile> result) {
                                                                                   allResults.addAll(result);
                                                                                   next.setEnabled(true);

                                                                                   final int index = currentIndex.get();
                                                                                   if (index < 0) {
                                                                                       next.actionPerformed(null);
                                                                                   }
                                                                                   Collections.sort(allResults);
                                                                                   searchResults.setText(Messages.getString("searchResults", //$NON-NLS-1$
                                                                                                                            currentIndex.get() + 1,
                                                                                                                            allResults.size()));
                                                                               }
                                                                           });
                                        lastWorker.addPropertyChangeListener(new PropertyChangeListener() {

                                            @Override
                                            public void propertyChange(PropertyChangeEvent evt) {
                                                int p = lastWorker.getProgress();
                                                if (p > 0) {
                                                    bar.setIndeterminate(false);
                                                    bar.setValue(lastWorker.getProgress());
                                                }
                                            }
                                        });
                                        executor.execute(lastWorker);
                                    }
                                });

                                // theStatusBar.add(new JLabel(), c2);
                            }
                            // theStatusBar.add(show)
                            PropertyChangeListener connectionPropertyListener = new PropertyChangeListener() {

                                @Override
                                public final void propertyChange(final PropertyChangeEvent evt) {
                                    final String propertyName = evt.getPropertyName();
                                    final JLabel lbl = statusLabels.get(propertyName);
                                    if (lbl != null) {
                                        final Object newVal = evt.getNewValue();
                                        try {
                                            final String tr = Messages.getString(propertyName,
                                                                                 FileSize.valueAsKMG(Long.parseLong(String.valueOf(newVal))));
                                            lbl.setText(tr);
                                            lbl.setToolTipText(tr);
                                            lbl.setVisible(newVal != null);
                                        } catch (NumberFormatException e) {
                                            lbl.setVisible(false);
                                        }
                                    }
                                }
                            };
                            conn.addPropertyChangeListener(connectionPropertyListener);
                            for (String s : statusLabels.keySet()) {
                                Long p = conn.getAccountPropertyAsLong(s);
                                if (p != null) {
                                    connectionPropertyListener.propertyChange(new PropertyChangeEvent(conn,
                                                                                                      s,
                                                                                                      Long.valueOf(0),
                                                                                                      p));
                                }
                            }
                        }
                    }
                    // final FileImport fileImport = new FileImport(conn, bar, importListener);
                    // table.setTransferHandler(fileImport);
                    // {
                    // DragSource dragSource = new DragSource();
                    // TableDragSource dragListener = new TableDragSource(dragSource, fileImport);
                    // dragSource.createDefaultDragGestureRecognizer(table,
                    // DnDConstants.ACTION_COPY_OR_MOVE,
                    // dragListener);
                    // }

                    JPanel centerPanelInside = new JPanel(new BorderLayout());
                    JPanel centerPanelRoot = new JPanel(new BorderLayout());
                    final JXCollapsiblePane coll = new JXCollapsiblePane();

                    // coll.setAlpha(.75f);
                    final AbstractAction showHideProgress = new AbstractAction() {

                        /**
                         * 
                         */
                        private static final long serialVersionUID = -1801728998288947749L;

                        final String name = Messages.getString("showProgress"); //$NON-NLS-1$

                        {
                            setEnabled(false);
                            putValue(Action.NAME, name);
                            putValue(Action.SHORT_DESCRIPTION, name);
                            putValue(Action.SMALL_ICON, SwiftConfigurationEditor.loadIcon("progress", name)); //$NON-NLS-1$
                        }

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            boolean visible = coll.isCollapsed();
                            coll.setCollapsed(!coll.isCollapsed());
                            final String name = visible ? Messages.getString("hideProgress") : Messages.getString("showProgress"); //$NON-NLS-1$ //$NON-NLS-2$
                            putValue(Action.NAME, name);
                            putValue(Action.SHORT_DESCRIPTION, name);
                        }
                    };

                    {
                        JButton btn = new JButton(showLogsAction);
                        btn.setHideActionText(true);
                        btn.setBorderPainted(false);
                        btn.setOpaque(false);
                        btn.setContentAreaFilled(false);
                        theStatusBar.add(btn);
                    }
                    {
                        JButton btn = new JButton(showHideProgress);
                        btn.setHideActionText(true);
                        btn.setBorderPainted(false);
                        btn.setOpaque(false);
                        btn.setContentAreaFilled(false);
                        theStatusBar.add(btn);
                    }
                    {
                        centerPanelInside.add(stable, BorderLayout.CENTER);
                        final JXTable t = new JXTable(fileHandler.getProgressTableModel());
                        // t.setMinimumSize(new Dimension(420, 16));
                        // t.getTableHeader().setVisible(false);
                        t.setTableHeader(null);
                        final int preferredWithForCancel = 64;
                        final int preferredWithForProgressBar = 48;
                        t.setDefaultRenderer(Boolean.class, new TableCellRenderer() {

                            private final JButton btn = new JButton(Messages.getString("cancel")); //$NON-NLS-1$
                            {
                                btn.setPreferredSize(new Dimension(preferredWithForCancel, 16));
                            }

                            @Override
                            public Component getTableCellRendererComponent(JTable table, Object value,
                                    boolean isSelected, boolean hasFocus, int row, int column) {
                                Boolean v = (Boolean) value;
                                btn.setSelected(v.booleanValue());
                                return btn;
                            }
                        });
                        t.setDefaultRenderer(Integer.class, new TableCellRenderer() {

                            private final JProgressBar btn = new JProgressBar(0, 100);
                            {
                                btn.setStringPainted(true);
                                btn.setPreferredSize(new Dimension(preferredWithForProgressBar, 16));
                            }

                            @Override
                            public Component getTableCellRendererComponent(JTable table, Object value,
                                    boolean isSelected, boolean hasFocus, int row, int column) {
                                Integer v = (Integer) value;
                                btn.setValue(v);
                                return btn;
                            }
                        });
                        t.setDefaultEditor(Boolean.class, new TableCellEditor() {

                            private Boolean value = false;

                            @Override
                            public Object getCellEditorValue() {
                                return value;
                            }

                            @Override
                            public boolean isCellEditable(EventObject anEvent) {
                                return true;
                            }

                            @Override
                            public boolean shouldSelectCell(EventObject anEvent) {
                                return false;
                            }

                            @Override
                            public boolean stopCellEditing() {
                                return true;
                            }

                            @Override
                            public void cancelCellEditing() {
                            }

                            @Override
                            public void addCellEditorListener(CellEditorListener l) {
                                listeners.add(l);
                            }

                            private List<CellEditorListener> listeners = new LinkedList<CellEditorListener>();

                            @Override
                            public void removeCellEditorListener(CellEditorListener l) {
                                listeners.remove(l);
                            }

                            @Override
                            public Component getTableCellEditorComponent(final JTable table, final Object value,
                                    final boolean isSelected, final int row, final int column) {

                                final JButton btn = new JButton(Messages.getString("cancel")); //$NON-NLS-1$
                                Boolean v = (Boolean) value;
                                btn.setSelected(v.booleanValue());
                                btn.addActionListener(new ActionListener() {

                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        table.setValueAt(true, row, column);
                                        btn.setSelected(true);
                                    }
                                });
                                return btn;
                            }
                        });
                        t.getColumn(0).setPreferredWidth(preferredWithForProgressBar);
                        t.getColumn(0).setMaxWidth(preferredWithForProgressBar);
                        t.getColumn(3).setPreferredWidth(preferredWithForCancel);
                        t.getColumn(3).setMaxWidth(preferredWithForCancel);
                        // {
                        // JScrollPane sp = new JScrollPane(t);
                        // sp.setPreferredSize(new Dimension(240, 48));
                        // coll.getContentPane().add(sp);
                        // }
                        coll.getContentPane().add(t);
                        centerPanelRoot.add(searchCollapse, BorderLayout.SOUTH);
                        centerPanelInside.add(coll, BorderLayout.SOUTH);
                        t.getModel().addTableModelListener(new TableModelListener() {

                            private boolean isScheduled = false;

                            boolean wasHidden = coll.isCollapsed();

                            @Override
                            public void tableChanged(TableModelEvent e) {
                                switch (e.getType()) {
                                    case TableModelEvent.DELETE:
                                    case TableModelEvent.INSERT:
                                        SwingUtilities.invokeLater(new Runnable() {

                                            @Override
                                            public void run() {
                                                if (!isScheduled) {

                                                    isScheduled = true;
                                                    final ActionListener timerAction = new ActionListener() {

                                                        @Override
                                                        public void actionPerformed(ActionEvent e) {
                                                            try {
                                                                int newRowCount = t.getRowCount();
                                                                boolean newHidden = newRowCount < 1;
                                                                if (newHidden != wasHidden) {
                                                                    if (coll.isCollapsed() != newHidden) {
                                                                        showHideProgress.actionPerformed(null);
                                                                    }
                                                                    wasHidden = newHidden;
                                                                }
                                                                showHideProgress.setEnabled(!newHidden);
                                                            } finally {
                                                                isScheduled = false;
                                                            }
                                                        }
                                                    };
                                                    {
                                                        final Timer tx = new Timer(500, timerAction);
                                                        tx.setRepeats(false);
                                                        tx.start();
                                                    }
                                                    showHideProgress.setEnabled(t.getRowCount() > 0);
                                                }
                                            }
                                        });
                                        break;
                                    default:
                                        break;
                                }

                            }
                        });
                        coll.setCollapsed(true);
                        coll.setAnimated(true);
                    }

                    final Action revokeInvitations = new AbstractAction() {

                        /**
                         * 
                         */
                        private static final long serialVersionUID = -8731498748567055602L;

                        {
                            putValue(Action.NAME, Messages.getString("revokeInvitations.name")); //$NON-NLS-1$
                            putValue(Action.SHORT_DESCRIPTION, Messages.getString("revokeInvitations.description"));//$NON-NLS-1$
                            putValue(Action.SMALL_ICON, SwiftConfigurationEditor.loadIcon("revoke", "revoke"));//$NON-NLS-1$ //$NON-NLS-2$
                        }

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            setEnabled(false);
                            JPanel jp = new JPanel(new VerticalLayout(5));
                            jp.add(new JLabel(Messages.getString("revokeMessage"))); //$NON-NLS-1$
                            final JCheckBox key1 = new JCheckBox(Messages.getString("revokeKey1")); //$NON-NLS-1$
                            final JCheckBox key2 = new JCheckBox(Messages.getString("revokeKey2")); //$NON-NLS-1$
                            jp.add(key1);
                            jp.add(key2);
                            if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(table,
                                                                                       jp,
                                                                                       String.valueOf(getValue(Action.NAME)),
                                                                                       JOptionPane.OK_CANCEL_OPTION)) {

                                Thread t = new Thread("Revoking") { //$NON-NLS-1$

                                    @Override
                                    public void run() {
                                        try {
                                            if (key1.isSelected())
                                                doRegenerateSecretKey1(conn, fileHandler);
                                            if (key2.isSelected())
                                                doRegenerateSecretKey2(conn, fileHandler);
                                        } catch (IOException err) {
                                            err.printStackTrace();
                                        } finally {
                                            setEnabled(true);
                                        }
                                    }
                                };
                                t.setDaemon(true);
                                t.start();
                            }
                        }
                    };
                    VirtualFileAction saveAs = new SaveAsAction(importListener, table);
                    actionsOnFiles.add(saveAs);
                    contextualMenu.add(saveAs);

                    {
                        JMenu fileMenu = new JMenu(Messages.getString("fileMenu")); //$NON-NLS-1$
                        JMenu editMenu = new JMenu(Messages.getString("editMenu")); //$NON-NLS-1$
                        // editMenu.add(fileImport.getCutAction());
                        editMenu.add(table.getCopyAction());
                        editMenu.add(table.getPasteAction());
                        editMenu.addSeparator();
                        editMenu.add(revokeInvitations);
                        // editMenu.add(fileImport.getPasteAction());
                        JMenu viewMenu = new JMenu(Messages.getString("viewMenu")); //$NON-NLS-1$

                        viewMenu.add(inspectAction);
                        viewMenu.add(showHideProgress);
                        viewMenu.add(showLogsAction);
                        viewMenu.addSeparator();
                        viewMenu.add(openInBrowser);
                        viewMenu.add(share);
                        viewMenu.add(friendsUpload);
                        viewMenu.addSeparator();
                        viewMenu.add(refreshAllAction);

                        // editMenu.add(fileImport.getImagePasteAction());

                        menubar.add(fileMenu);
                        menubar.add(editMenu);
                        menubar.add(viewMenu);
                        fileMenu.add(createContainer);
                        fileMenu.addSeparator();
                        fileMenu.add(deleteFileAction);
                        fileMenu.addSeparator();
                        fileMenu.add(saveAs);
                        if (!Application.isMacOs()) {
                            JMenu menu = new JMenu(Messages.getString("windowMenu")); //$NON-NLS-1$
                            menu.add(new AbstractAction() {

                                /**
                                 * 
                                 */
                                private static final long serialVersionUID = -7556861877400464870L;

                                {
                                    final String name = Messages.getString("showConnections"); //$NON-NLS-1$
                                    putValue(Action.NAME, name);
                                    putValue(Action.SHORT_DESCRIPTION, name);
                                    putValue(Action.SMALL_ICON, SwiftConfigurationEditor.loadIcon("preferences", name)); //$NON-NLS-1$
                                }

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    showSwiftConfigurationPanel();
                                }
                            });
                            menu.add(aboutAction);
                            menubar.add(menu);
                            {
                                Map<String, String> comments = new HashMap<String, String>();
                                comments.put("en", "OpenStack Swift Browser - Explore your cloud files"); //$NON-NLS-1$//$NON-NLS-2$
                                comments.put("fr", "Explorateur OpenStack Swift - Explorez vos fichiers dans le Cloud"); //$NON-NLS-1$//$NON-NLS-2$
                                InstallDesktopFileAction.addInstallDesktopIfNeeded(menu,
                                                                                   APPLICATION_NAME,
                                                                                   APPLICATION_ICON_PATH,
                                                                                   comments,
                                                                                   null);
                            }
                        }

                        jf.setJMenuBar(menubar);
                    }
                    jf.addWindowListener(new WindowAdapter() {

                        @Override
                        public void windowActivated(WindowEvent e) {
                            super.windowActivated(e);
                            // fileImport.flavorsChanged(null);
                        }

                        @Override
                        public void windowClosing(WindowEvent e) {
                            super.windowClosing(e);
                            metaDataFrame.setVisible(false);
                            metaDataFrame.dispose();
                            showLogsFrame.setVisible(false);
                            showLogsFrame.dispose();
                            SwingUtilities.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    jf.dispose();
                                }
                            });
                            GlobalExecutorService.decrementReferences();
                            guiBindings.cleanup();
                            model.clear();
                        }

                    });

                    VirtualFileAction renameFile = new RenameVirtualFileAction(executor,
                                                                               guiBindings,
                                                                               fileHandler,
                                                                               table,
                                                                               conn);
                    actionsOnFiles.add(renameFile);
                    contextualMenu.add(renameFile);

                    JXPanel mainPanel = new JXPanel(new BorderLayout());

                    {
                        JToolBar toolbar = new JToolBar(Messages.getString("actions")); //$NON-NLS-1$
                        toolbar.add(createContainer);
                        toolbar.add(refreshAllAction);
                        toolbar.addSeparator();
                        // toolbar.add(fileImport.getImagePasteAction());
                        toolbar.add(saveAs);
                        toolbar.add(renameFile);
                        toolbar.add(deleteFileAction);
                        toolbar.add(inspectAction);
                        toolbar.add(openInBrowser);
                        toolbar.add(share);
                        toolbar.add(friendsUpload);
                        mainPanel.add(toolbar, BorderLayout.NORTH);
                    }
                    mainPanel.add(theStatusBar, BorderLayout.SOUTH);
                    // table.setDragEnabled(true);

                    table.setColumnControlVisible(true);
                    table.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
                    jf.setPreferredSize(new Dimension(800, 480));
                    {
                        // table.getColumn(0).setPreferredWidth(480);
                        table.getColumn(1).setPreferredWidth(64);
                        table.getColumn(1).setMaxWidth(96);
                        table.getColumn(2).setPreferredWidth(164);
                        table.getColumn(2).setMaxWidth(192);
                    }
                    metaDataFrame.setPreferredSize(new Dimension(240, 160));
                    metaDataFrame.addWindowListener(new WindowListener() {

                        @Override
                        public void windowOpened(WindowEvent e) {
                            inspectAction.setEnabled(false);
                        }

                        @Override
                        public void windowClosing(WindowEvent e) {
                            inspectAction.setEnabled(true);
                        }

                        @Override
                        public void windowClosed(WindowEvent e) {
                            inspectAction.setEnabled(false);
                        }

                        @Override
                        public void windowIconified(WindowEvent e) {
                            inspectAction.setEnabled(true);
                        }

                        @Override
                        public void windowDeiconified(WindowEvent e) {
                            inspectAction.setEnabled(false);
                        }

                        @Override
                        public void windowActivated(WindowEvent e) {
                            inspectAction.setEnabled(false);
                        }

                        @Override
                        public void windowDeactivated(WindowEvent e) {
                        }
                    });
                    {
                        final FileTreeDragSource source = new FileTreeDragSource(System.getProperty("user.home"), false); //$NON-NLS-1$
                        final JTree tree = source.getTree();
                        JPanel fs = new JPanel(new BorderLayout());
                        fs.add(new JScrollPane(tree), BorderLayout.CENTER);
                        {
                            JToolBar tools = new JToolBar("filesystem", JToolBar.VERTICAL); //$NON-NLS-1$
                            tools.setLayout(new VerticalLayout(25));
                            tools.setFloatable(false);
                            tools.setBorderPainted(false);
                            {
                                JButton btn = new JButton(source.getRefreshAction());
                                btn.setOpaque(false);
                                btn.setHideActionText(true);
                                btn.setBorderPainted(false);
                                btn.setContentAreaFilled(false);
                                tools.add(btn);

                            }
                            // tools.add(source.getRefreshAction());
                            fs.add(tools, BorderLayout.WEST);

                            VirtualFileAction copyToCloud = new VirtualFileAction() {

                                /**
                                 * 
                                 */
                                private static final long serialVersionUID = -2082716046190289234L;

                                {
                                    final String name = Messages.getString("copyToCloud"); //$NON-NLS-1$
                                    putValue(Action.NAME, name);
                                    putValue(Action.SHORT_DESCRIPTION, name);
                                    putValue(Action.SMALL_ICON, SwiftConfigurationEditor.loadIcon("left", name)); //$NON-NLS-1$
                                    setEnabled(false);
                                    tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {

                                        @Override
                                        public void valueChanged(TreeSelectionEvent e) {
                                            fileTreeIsOk = tree.getSelectionCount() > 0;
                                            setEnabled(fileTreeIsOk && cloudTreeIsOk);
                                        }
                                    });
                                }

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    final List<Future<ContainerIFace>> futures = new LinkedList<Future<ContainerIFace>>();
                                    final VirtualFile fx = (VirtualFile) table.getValueAt(table.getSelectedRow(), 0);
                                    for (TreePath p : tree.getSelectionModel().getSelectionPaths()) {
                                        FileTreeNode n = (FileTreeNode) p.getLastPathComponent();
                                        File file = new File(n.getFullName());
                                        futures.add(importListener.addFile(fx, file));
                                    }
                                    Runnable rx = new Runnable() {

                                        @Override
                                        public void run() {
                                            for (Future<ContainerIFace> iface : futures) {
                                                while (!iface.isDone()) {
                                                    try {
                                                        Thread.sleep(500);
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                            {
                                                final ContainerObject containerToRefresh = (ContainerObject) fx.getContainer();
                                                GlobalExecutorService.getExecutorService().submit(new Runnable() {

                                                    @Override
                                                    public void run() {
                                                        try {
                                                            Collection<ObjectIFace> newFiles = conn.list(fileHandler,
                                                                                                         containerToRefresh);
                                                            containerToRefresh.setFiles(newFiles);
                                                        } catch (IOException e) {
                                                            LOG.log(Level.WARNING,
                                                                    "Failed to refresh container " + containerToRefresh, e); //$NON-NLS-1$
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    };
                                    executor.execute(rx);
                                }

                                private boolean fileTreeIsOk = false;

                                private boolean cloudTreeIsOk = false;

                                @Override
                                public void enable(VirtualFile... selectedPaths) {
                                    this.cloudTreeIsOk = selectedPaths.length == 1 && selectedPaths[0].isDirectory();
                                    setEnabled(fileTreeIsOk && cloudTreeIsOk);
                                }
                            };
                            actionsOnFiles.add(copyToCloud);

                            VirtualFileAction createVirtualDirectory = new CreateVirtualDirectoryAction(fileHandler,
                                                                                                        conn,
                                                                                                        guiBindings,
                                                                                                        table,
                                                                                                        jf);
                            actionsOnFiles.add(createVirtualDirectory);
                            contextualMenu.add(createVirtualDirectory);

                            VirtualFileAction copyToLocal = new VirtualFileAction() {

                                /**
                                 * 
                                 */
                                private static final long serialVersionUID = -8980051181408020591L;

                                {
                                    final String name = Messages.getString("copyToLocal"); //$NON-NLS-1$
                                    putValue(Action.NAME, name);
                                    putValue(Action.SHORT_DESCRIPTION, name);
                                    setEnabled(false);
                                    putValue(Action.SMALL_ICON, SwiftConfigurationEditor.loadIcon("right", name)); //$NON-NLS-1$
                                    tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {

                                        @Override
                                        public void valueChanged(TreeSelectionEvent e) {
                                            fileTreeIsOk = tree.getSelectionCount() == 1;
                                            setEnabled(fileTreeIsOk && cloudTreeIsOk);
                                        }
                                    });
                                }

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    final List<Future<File>> results = new LinkedList<Future<File>>();
                                    TreePath p = tree.getSelectionModel().getSelectionPath();
                                    FileTreeNode n = (FileTreeNode) p.getLastPathComponent();
                                    File file = new File(n.getFullName());
                                    if (file.isDirectory()) {
                                        for (int row : table.getSelectedRows()) {
                                            VirtualFile fx = (VirtualFile) table.getValueAt(row, 0);
                                            results.addAll(importListener.saveAs(fx, new File(file, fx.getName())));
                                        }
                                    }
                                    if (!results.isEmpty()) {
                                        Thread t = new Thread("waitForResult") { //$NON-NLS-1$

                                            @Override
                                            public void run() {
                                                Runnable rx = new Runnable() {

                                                    @Override
                                                    public void run() {
                                                        source.getRefreshAction().actionPerformed(null);
                                                    }

                                                };
                                                long lastUpdate = 0;
                                                for (Future<File> r : results) {
                                                    try {
                                                        if (!r.isDone()) {
                                                            r.get();
                                                            final long now = System.currentTimeMillis();
                                                            if (now - lastUpdate > 5000)
                                                                SwingUtilities.invokeLater(rx);
                                                            lastUpdate = now;
                                                        }
                                                    } catch (Exception err) {
                                                        break;
                                                    }
                                                }
                                                SwingUtilities.invokeLater(rx);
                                            }
                                        };
                                        t.setDaemon(true);
                                        t.start();

                                    }
                                }

                                private boolean fileTreeIsOk = false;

                                private boolean cloudTreeIsOk = false;

                                @Override
                                public void enable(VirtualFile... selectedPaths) {
                                    this.cloudTreeIsOk = selectedPaths.length > 0;
                                    setEnabled(fileTreeIsOk && cloudTreeIsOk);
                                }
                            };
                            actionsOnFiles.add(copyToLocal);
                            {
                                {
                                    JButton btn = new JButton(copyToCloud);
                                    btn.setOpaque(false);
                                    btn.setHideActionText(true);
                                    btn.setBorderPainted(false);
                                    btn.setContentAreaFilled(false);
                                    tools.add(btn);

                                }
                                {
                                    JButton btn = new JButton(copyToLocal);
                                    btn.setOpaque(false);
                                    btn.setHideActionText(true);
                                    btn.setBorderPainted(false);
                                    btn.setContentAreaFilled(false);
                                    tools.add(btn);

                                }
                            }
                            contextualMenu.addSeparator();
                            contextualMenu.add(copyToCloud);
                            contextualMenu.add(copyToLocal);
                            // contextualMenu.addSeparator();
                        }
                        final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, centerPanelRoot, fs);

                        centerPanelRoot.add(centerPanelInside, BorderLayout.CENTER);
                        mainPanel.add(split, BorderLayout.CENTER);
                        split.setDividerLocation(600);
                        split.setResizeWeight(0.5);
                        tree.setDropMode(DropMode.ON);
                        tree.setDropTarget(new DropTarget(tree, DnDConstants.ACTION_COPY, new DropTargetListener() {

                            @Override
                            public void dropActionChanged(DropTargetDragEvent dtde) {
                            }

                            @Override
                            public void drop(DropTargetDropEvent dtde) {
                                Transferable t = dtde.getTransferable();
                                if (t.isDataFlavorSupported(Constants.virtualFilesList)) {
                                    // drop.get
                                    // TenantDragAndDrop drop = (TenantDragAndDrop)
                                    // t.getTransferData(Constants.virtualFilesList);
                                }
                            }

                            @Override
                            public void dragOver(DropTargetDragEvent dtde) {
                            }

                            @Override
                            public void dragExit(DropTargetEvent dte) {
                            }

                            @Override
                            public void dragEnter(DropTargetDragEvent dtde) {
                                if (dtde.isDataFlavorSupported(Constants.uriList))
                                    dtde.acceptDrag(DnDConstants.ACTION_COPY);
                            }
                        }));
                    }
                    final VirtualFileAction refreshSingleFile = new VirtualFileAction() {

                        /**
                         * 
                         */
                        private static final long serialVersionUID = 1101517587425048799L;

                        {
                            final String name = Messages.getString("file.refreshAction"); //$NON-NLS-1$
                            putValue(Action.NAME, name);
                            putValue(Action.SHORT_DESCRIPTION, name);
                            putValue(Action.SMALL_ICON, refreshIcon);
                        }

                        @Override
                        public void enable(VirtualFile... selectedPaths) {
                            setEnabled(selectedPaths.length > 0);
                        }

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            for (int row : table.getSelectedRows()) {
                                VirtualFile fx = (VirtualFile) table.getValueAt(row, 0);
                                final FileIFace file = fx.getFile();
                                if (file == null)
                                    continue;
                                executor.execute(new Runnable() {

                                    @Override
                                    public void run() {
                                        importListener.refresh(file);
                                    }

                                });

                            }
                        }

                    };
                    {
                        HttpPropertiesPanel httpPropertiesPanel = new HttpPropertiesPanel(conn,
                                                                                          table,
                                                                                          refreshAllAction,
                                                                                          refreshSingleFile,
                                                                                          fileHandler);
                        // JScrollPane p = new JScrollPane(httpPropertiesPanel);

                        metaDataFrame.setContentPane(httpPropertiesPanel);
                        metaDataFrame.pack();
                        metaDataFrame.setSize(new Dimension(800, 640));
                        table.getSelectionModel().addListSelectionListener(httpPropertiesPanel);
                    }
                    actionsOnFiles.add(refreshSingleFile);
                    contextualMenu.add(refreshSingleFile);
                    contextualMenu.addSeparator();
                    contextualMenu.add(inspectAction);
                    jf.setContentPane(mainPanel);
                    jf.pack();
                    jf.setIconImage(icon.getImage());
                    jf.setLocationByPlatform(true);
                    jf.setVisible(true);

                }

            });

            Collection<ObjectIFace> containers = conn.listRoots(fileHandler, fileFactory);
            for (ObjectIFace container : containers) {
                final ContainerObject cont = fileFactory.getContainer(container.getName());
                guiBindings.addContainer(cont);
                //                System.out.println("-----------\nListing for " + container); //$NON-NLS-1$
                executor.execute(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            final Collection<ObjectIFace> files = conn.list(fileHandler, cont);
                            cont.setFiles(files);
                            refreshAllFilesThatNeedsRefresh.actionPerformed(null);
                        } catch (IOException err) {
                            LOG.log(Level.WARNING, "Cannot list " + cont.getName(), err); //$NON-NLS-1$
                        }
                    }
                });
            }
            regenerateSecretKeyIfNeeded(conn, fileHandler);

        }
        if (!hasSuccess) {
            if (errorsAtStartup.isEmpty()) {
                JOptionPane.showMessageDialog(null, Messages.getString("errors.cannotLoginNoTenant")); //$NON-NLS-1$
            } else {
                // TODO: display nice error message
                LOG.log(Level.SEVERE, "Could not login, number of error(s): " + errorsAtStartup.size()); //$NON-NLS-1$
            }
        } else {
            if (!errorsAtStartup.isEmpty())
                LOG.log(Level.WARNING, "We could login, but we had " + errorsAtStartup.size() + " error(s)."); //$NON-NLS-1$//$NON-NLS-2$
        }
    }

    public static void showSwiftConfigurationPanel() {

        SwingUtilities.invokeLater(new Runnable() {

            SwiftConfigurationPanel panel;

            @Override
            public void run() {
                if (swiftConfigurationPanel != null && swiftConfigurationPanel.isDisplayable()) {
                    swiftConfigurationPanel.setVisible(true);
                    return;
                }
                GlobalExecutorService.incrementReferences();
                final Action connect = new AbstractAction() {

                    /**
                     * 
                     */
                    private static final long serialVersionUID = -2736296292074759562L;

                    {
                        final String name = Messages.getString("Connect"); //$NON-NLS-1$
                        putValue(Action.NAME, name);
                        putValue(Action.SHORT_DESCRIPTION, name);
                        putValue(Action.SMALL_ICON, SwiftConfigurationEditor.loadIcon("connect", name)); //$NON-NLS-1$
                    }

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setEnabled(false);
                        final AbstractAction self = this;
                        try {
                            final SwiftConfiguration configuration = panel.getCurrentConfiguration();

                            Thread t = new Thread() {

                                @Override
                                public void run() {

                                    try {
                                        doConnect(configuration);
                                    } catch (IOException ex) {
                                        JOptionPane.showMessageDialog(panel,
                                                                      Messages.getString("cannotAuthenticateMessage", //$NON-NLS-1$
                                                                                         ex.getLocalizedMessage(),
                                                                                         configuration.getTokenUrlAsUrl(),
                                                                                         configuration.getCredential()
                                                                                                      .getUser()),
                                                                      Messages.getString("cannotAuthenticateTitle", //$NON-NLS-1$
                                                                                         configuration.getTokenUrlAsUrl()),
                                                                      JOptionPane.ERROR_MESSAGE);
                                    } finally {
                                        self.setEnabled(true);
                                    }
                                }
                            };
                            t.setDaemon(true);
                            t.start();
                        } catch (MalformedURLException err) {
                            JOptionPane.showMessageDialog(panel, err.getLocalizedMessage());
                        }
                    }
                };
                Action[] additionalActions;
                if (!Application.isMacOs()) {
                    additionalActions = new Action[] { aboutAction };
                } else {
                    additionalActions = null;
                }
                panel = new SwiftConfigurationPanel(connect, additionalActions);
                JFrame f = new JFrame(Messages.getString("listOfConnections")); //$NON-NLS-1$
                f.setIconImage(icon.getImage());
                f.setContentPane(panel);
                f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                f.pack();
                f.setLocationByPlatform(true);
                f.setVisible(true);
                f.setIconImage(icon.getImage());
                f.addWindowListener(new WindowAdapter() {

                    /**
                     * @see java.awt.event.WindowAdapter#windowClosed(java.awt.event.WindowEvent)
                     */
                    @Override
                    public void windowClosed(WindowEvent e) {
                        super.windowClosed(e);
                        GlobalExecutorService.decrementReferences();
                    }

                });
                swiftConfigurationPanel = f;
                URIOpen.checkDesktop(f);
            }

        });
    }

    private final static String APPLICATION_ICON_PATH = "/net/souchay/swift/gui/openstack.png"; //$NON-NLS-1$

    private final static ImageIcon icon = SwiftConfigurationEditor.loadIconFromFullPath(APPLICATION_ICON_PATH,
                                                                                        "Swift Browser"); //$NON-NLS-1$

    private static JFrame swiftConfigurationPanel;

    /**
     * Entry point
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String... args) throws Exception {
        Application.setApplicationName(APPLICATION_NAME, true);

        Application.registerOSXApplicationMenu(new ApplicationConfiguration(new MacOSXHandle() {

            @Override
            public boolean handlePreferences() {
                showSwiftConfigurationPanel();
                return true;
            }

            /**
             * @see net.souchay.utilities.Application.MacOSXHandle#handleAbout()
             */
            @Override
            public boolean handleAbout() {
                aboutAction.actionPerformed(null);
                return true;
            }

        }, true, icon.getImage()));
        if (Boolean.getBoolean("ssl.trustAll")) //$NON-NLS-1$
            trustAll();
        if (args.length == 3) {
            SwiftJSonCredentials credentials = new SwiftJSonCredentials(args[1], args[2].toCharArray());
            doConnect(new SwiftConfiguration(credentials, new URL(args[0])));
        } else if (args.length > 4) {
            SwiftJSonCredentials credentials = new SwiftJSonCredentials(args[1],
                                                                        args[2].toCharArray(),
                                                                        args[3],
                                                                        args[4],
                                                                        null,
                                                                        null,
                                                                        SwiftJSonCredentials.DEFAULT_URL_TYPE);
            doConnect(new SwiftConfiguration(credentials, new URL(args[0])));
        } else if (args.length > 0) {
            SwiftJSonCredentials credentials = new SwiftJSonCredentials(args[1],
                                                                        args[2].toCharArray(),
                                                                        SwiftJSonCredentials.DEFAULT_TENANT_TYPE,
                                                                        args[3],
                                                                        null,
                                                                        null,
                                                                        SwiftJSonCredentials.DEFAULT_URL_TYPE);
            doConnect(new SwiftConfiguration(credentials, new URL(args[0])));
        } else {
            showSwiftConfigurationPanel();
        }

    }

    final static void listRec(final ContainerObject container, final File root, final List<FileIFace> list, String path) {
        File f = new File(root, path);
        if (f.isDirectory()) {
            String[] li = f.list();
            for (String s : li) {
                listRec(container, root, list, path + VirtualFile.VIRTUAL_FILE_SEPARATOR + s);
            }
        } else if (f.isFile()) {
            list.add(new RealFile(container, f));
        }
    }

    private static void trustAll() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                // No need to implement.
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                // No need to implement.
            }
        } };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL"); //$NON-NLS-1$
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
