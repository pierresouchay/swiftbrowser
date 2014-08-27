package net.souchay.swift.gui.configuration;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import net.souchay.swift.gui.ConfigurationPersistance;
import net.souchay.swift.gui.MasterPasswordService;
import net.souchay.swift.gui.MasterPasswordService.MasterPasswordServiceNotAvailableException;
import net.souchay.swift.gui.Messages;
import net.souchay.swift.net.SwiftConfiguration;
import net.souchay.swift.net.SwiftConstantsServer.URL_TYPE;
import net.souchay.swift.net.SwiftJSonCredentials;

/**
 * Panel to edit a SwiftConfiguration
 * 
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3856 $
 * 
 */
public class SwiftConfigurationEditor extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 9218781062578205540L;

    private static class MapedValue {

        private MapedValue(String name, String url) {
            this.name = name;
            this.url = url;
        }

        final String name;

        @Override
        public String toString() {
            return url;
        }

        final String url;
    }

    private final static List<MapedValue> staticPredefined;

    private static MapedValue CLOUDWATT = new MapedValue(Messages.getString("SwiftConfigurationEditor.CloudWatt"), "https://identity.fr1.cloudwatt.com/v2.0/tokens");//$NON-NLS-1$ //$NON-NLS-2$

    static {
        final List<MapedValue> st = new LinkedList<MapedValue>();
        st.add(CLOUDWATT);
        st.add(new MapedValue("Rackspace Cloud Files", "https://lon.auth.api.rackspacecloud.com:443/v2.0/tokens")); //$NON-NLS-1$ //$NON-NLS-2$
        staticPredefined = Collections.unmodifiableList(st);
    }

    private final Vector<MapedValue> comboVector = new Vector<MapedValue>(staticPredefined);

    private final JComboBox<String> tenantType = new JComboBox<String>(SwiftJSonCredentials.getPossibleTenantTypes());

    private final JComboBox<MapedValue> predefinedURLs = new JComboBox<MapedValue>(comboVector);

    private final JTextField fixedContainers = new JTextField("", 16); //$NON-NLS-1$

    private final JTextField overridedSwiftURL = new JTextField(""); //$NON-NLS-1$

    private final JTextField name = new JTextField();

    private final JTextField username = new JTextField();

    private final JRadioButton usePublicURL = new JRadioButton(Messages.getString("SwiftConfigurationEditor.usePublicUrl", //$NON-NLS-1$
                                                                                  true));

    private final JRadioButton usePrivateURL = new JRadioButton(Messages.getString("SwiftConfigurationEditor.usePrivateUrl", //$NON-NLS-1$
                                                                                   false));

    private final JRadioButton forceSpecificUrl = new JRadioButton(Messages.getString("SwiftConfigurationEditor.useOverrideSwiftURL", //$NON-NLS-1$
                                                                                      false));

    private final JPasswordField password = new JPasswordField();

    private Color regularBgColor = null;

    public SwiftConfiguration getCurrentConfiguration() throws MalformedURLException {
        SwiftJSonCredentials creds;
        if (tenantType.getSelectedItem() == null) {
            creds = new SwiftJSonCredentials(username.getText(), password.getPassword());
        } else {
            String override = null;
            try {
                String urlTxt = overridedSwiftURL.getText();
                if (urlTxt != null) {
                    urlTxt = urlTxt.trim();
                    if (urlTxt.startsWith("http")) { //$NON-NLS-1$ 
                        URL ux = new URL(urlTxt);
                        if (ux.getHost() != null) {
                            override = urlTxt;
                        }
                    }
                }
                if (regularBgColor != null) {
                    overridedSwiftURL.setBackground(regularBgColor);
                }
            } catch (MalformedURLException err) {
                // ignored
                if (regularBgColor == null)
                    regularBgColor = overridedSwiftURL.getBackground();
                overridedSwiftURL.setBackground(Color.RED);
            }
            creds = new SwiftJSonCredentials(username.getText(),
                                             password.getPassword(),
                                             String.valueOf(tenantType.getSelectedItem()),
                                             tenant.getText(),
                                             fixedContainers.getText(),
                                             override,
                                             forceSpecificUrl.isSelected() ? URL_TYPE.overrideUrl : (usePrivateURL.isSelected() ? URL_TYPE.internalUrl : URL_TYPE.publicURL));
        }
        // Noop
        SwiftConfiguration configuration;
        try {
            configuration = new SwiftConfiguration(creds, new URL(String.valueOf(predefinedURLs.getSelectedItem())));
            configuration.setName(name.getText());
            return configuration;
        } catch (MalformedURLException e1) {
            JOptionPane.showMessageDialog(SwiftConfigurationEditor.this, e1.getLocalizedMessage());
            throw e1;
        }
    }

    private final Action saveAction = new AbstractAction(Messages.getString("save")) { //$NON-NLS-1$

        /**
         * 
         */
        private static final long serialVersionUID = -1718146012680020425L;

        {
            putValue(Action.SMALL_ICON, loadIcon("save", Messages.getString("save"))); //$NON-NLS-1$//$NON-NLS-2$
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                SwiftConfiguration cfg = SwiftConfigurationEditor.this.configuration;
                setConfiguration(getCurrentConfiguration());
                if (cfg != null) {
                    SwiftConfigurationEditor.this.configuration.setId(cfg.getId());
                }
                char[] pwd = SwiftConfigurationEditor.this.configuration.getCredential().getPassword();
                if (pwd != null && pwd.length > 0) {
                    try {
                        String encodedPassword = MasterPasswordService.getInstance(SwiftConfigurationEditor.this)
                                                                      .encode(SwiftConfigurationEditor.this, pwd);
                        SwiftConfigurationEditor.this.configuration.getCredential().setCryptedPassword(encodedPassword);
                    } catch (MasterPasswordServiceNotAvailableException e1) {
                        e1.printStackTrace();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }

                }
                Thread t = new Thread("SaveConfiguration") { //$NON-NLS-1$

                    @Override
                    public void run() {
                        try {
                            ConfigurationPersistance.getInstance().saveConfig(configuration);
                        } catch (IOException e) {
                            JOptionPane.showMessageDialog(SwiftConfigurationEditor.this,
                                                          e.getLocalizedMessage(),
                                                          Messages.getString("failedToSaveConfigurationFile"), //$NON-NLS-1$
                                                          JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };
                t.setDaemon(true);
                t.start();
                for (ActionListener a : listeners) {
                    a.actionPerformed(e);
                }

            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            }

        }
    };

    private boolean checkReadyToConnect() {
        return predefinedURLs.getSelectedItem() != null && (password.getPassword() != null)
               && (password.getPassword().length > 0) && (username.getText() != null)
               && !username.getText().trim().isEmpty();
    }

    private final JTextField tenant = new JTextField();

    public SwiftConfigurationEditor(Action... actions) {
        super(new GridBagLayout());
        saveAction.setEnabled(false);
        tenantType.setRenderer(new ListCellRenderer<String>() {

            private final DefaultListCellRenderer render = new DefaultListCellRenderer();

            @Override
            public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                if (value == null)
                    value = Messages.getString("autodiscover"); //$NON-NLS-1$
                return render.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        tenantType.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enable = tenantType.getSelectedItem() != null;
                tenant.setEnabled(enable);
                fixedContainers.setEnabled(enable);
                // overridedSwiftURL.setEnabled(enable);
            }
        });
        fixedContainers.setToolTipText(Messages.getString("fixedContainersHelp")); //$NON-NLS-1$
        ListCellRenderer<MapedValue> renderer = new ListCellRenderer<SwiftConfigurationEditor.MapedValue>() {

            private final DefaultListCellRenderer render = new DefaultListCellRenderer();

            @Override
            public Component getListCellRendererComponent(JList<? extends MapedValue> list, MapedValue value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                Component c = render.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (index < 0)
                    return c;
                final MapedValue v = list.getModel().getElementAt(index);
                if (c instanceof JLabel) {
                    ((JLabel) c).setText(v.name);
                    ((JLabel) c).setToolTipText(v.url);
                } else if (c instanceof JTextComponent) {
                    ((JTextComponent) c).setText(v.url);
                    ((JTextComponent) c).setToolTipText(v.name);
                }
                return c;
            }
        };
        predefinedURLs.setEditable(true);
        // predefinedURLs.setEditor(predefinedURLs.getEditor());
        predefinedURLs.setRenderer(renderer);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.BASELINE_TRAILING;
        c.weightx = 0;
        c.gridx = 0;
        c.gridy = 0;
        c.ipadx = 5;
        {
            JLabel lbl = new JLabel(Messages.getString("SwiftConfigurationEditor.nameLabel")); //$NON-NLS-1$
            lbl.setLabelFor(name);
            super.add(lbl, c);
            c.gridy++;
        }
        {
            JLabel lbl = new JLabel(Messages.getString("SwiftConfigurationEditor.urlLabel")); //$NON-NLS-1$
            lbl.setLabelFor(predefinedURLs);
            super.add(lbl, c);
            c.gridy++;
        }

        {
            JLabel lbl = new JLabel(Messages.getString("SwiftConfigurationEditor.userLabel")); //$NON-NLS-1$
            lbl.setLabelFor(username);
            super.add(lbl, c);
            c.gridy++;
        }

        {
            JLabel lbl = new JLabel(Messages.getString("SwiftConfigurationEditor.passwordLabel")); //$NON-NLS-1$
            lbl.setLabelFor(password);
            super.add(lbl, c);
            c.gridy++;
        }

        {
            c.gridwidth = 2;
            c.gridx = 1;
            c.ipadx = 50;
            c.weightx = 2;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.BASELINE_LEADING;
            JLabel lbl = new JLabel(Messages.getString("SwiftConfigurationEditor.advancedSettings")); //$NON-NLS-1$
            super.add(lbl, c);
            c.gridx = 0;
            c.weightx = 0;
            c.gridwidth = 1;
            c.ipadx = 5;
            c.fill = GridBagConstraints.NONE;
            c.anchor = GridBagConstraints.BASELINE_TRAILING;
            c.gridy++;
        }

        {
            super.add(tenantType, c);
            c.gridy++;
        }

        {
            c.gridx = 0;
            c.gridwidth = 3;
            c.anchor = GridBagConstraints.CENTER;
            c.fill = GridBagConstraints.HORIZONTAL;
            JPanel jp = new JPanel(new FlowLayout());
            jp.add(usePublicURL);
            jp.add(usePrivateURL);
            jp.add(forceSpecificUrl);
            super.add(jp, c);
            c.gridy++;
        }

        {
            c.gridx = 0;
            c.gridwidth = 1;
            c.anchor = GridBagConstraints.LINE_END;
            c.fill = GridBagConstraints.NONE;
            JLabel lbl = new JLabel(Messages.getString("SwiftConfigurationEditor.overrideSwiftURL")); //$NON-NLS-1$
            lbl.setLabelFor(overridedSwiftURL);
            super.add(lbl, c);
            c.gridx = 1;
            c.gridwidth = 2;
            c.anchor = GridBagConstraints.LINE_START;
            c.fill = GridBagConstraints.HORIZONTAL;
            super.add(overridedSwiftURL, c);
            c.gridy++;
        }

        {
            ButtonGroup group = new ButtonGroup();
            group.add(usePublicURL);
            group.add(forceSpecificUrl);
            group.add(usePrivateURL);
            forceSpecificUrl.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(ItemEvent e) {
                    boolean enable = forceSpecificUrl.isSelected();
                    overridedSwiftURL.setEnabled(enable);
                    if (enable)
                        overridedSwiftURL.requestFocus();
                }
            });
        }

        c.gridy = 0;
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridwidth = 2;
        super.add(name, c);
        c.gridy++;
        super.add(predefinedURLs, c);
        c.gridy++;
        super.add(username, c);
        c.gridy++;
        super.add(password, c);
        c.gridy++;
        predefinedURLs.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (configuration != null) {
                    Object o = predefinedURLs.getSelectedItem();
                    if (o != null) {
                        try {
                            configuration.setTokenUrl(new URL(String.valueOf(o)));
                        } catch (MalformedURLException e1) {
                            e1.printStackTrace();
                        }
                    }

                }
            }
        });

        {

            c.gridwidth = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            // c.insets = new Insets(0, 0, 0, 5);
            c.gridy++;
            super.add(tenant, c);
            c.insets = new Insets(0, 5, 0, 0);
            c.gridx++;
            super.add(fixedContainers, c);
            c.gridy++;
            c.insets = new Insets(0, 0, 0, 0);
        }

        c.gridy += 2;
        c.gridx = 0;
        c.anchor = GridBagConstraints.SOUTHEAST;
        c.fill = GridBagConstraints.NONE;
        c.weighty = 2;
        c.gridwidth = 3;
        ActionListener a = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                saveAction.setEnabled(true);
            }
        };
        tenantType.addActionListener(a);
        usePrivateURL.addActionListener(a);
        usePublicURL.addActionListener(a);
        forceSpecificUrl.addActionListener(a);
        predefinedURLs.addActionListener(a);
        DocumentListener doc = new DocumentListener() {

            private void d() {
                saveAction.setEnabled(true);
                setReadyToConnect(checkReadyToConnect());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                d();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                d();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                d();
            }
        };
        username.getDocument().addDocumentListener(doc);
        fixedContainers.getDocument().addDocumentListener(doc);
        overridedSwiftURL.getDocument().addDocumentListener(doc);
        password.getDocument().addDocumentListener(doc);
        name.getDocument().addDocumentListener(doc);
        tenant.getDocument().addDocumentListener(doc);

        JPanel flowLayout = new JPanel(new FlowLayout(FlowLayout.TRAILING, 20, 5));
        JButton btn = new JButton(saveAction);
        flowLayout.add(btn);
        for (Action ab : actions) {
            JButton ax = new JButton(ab);
            flowLayout.add(ax);
        }
        super.add(flowLayout, c);
    }

    public JPasswordField getPassword() {
        return password;
    }

    public boolean readyToConnect = false;

    public boolean isReadyToConnect() {
        return readyToConnect;
    }

    /**
     * Set the ready to connect property
     * 
     * @param readyToConnect
     */
    public void setReadyToConnect(boolean readyToConnect) {
        boolean oldValue = this.readyToConnect;
        this.readyToConnect = readyToConnect;
        firePropertyChange(PROPERTY_READY_TO_CONNECT, oldValue, readyToConnect);
    }

    private SwiftConfiguration configuration;

    /**
     * Get the configuration
     * 
     * @return teh new configuration
     */
    public SwiftConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Set the configuration
     * 
     * @param configuration
     */
    public void setConfiguration(SwiftConfiguration configuration) {
        this.configuration = configuration;
        this.name.setText(configuration == null ? null : configuration.getName());
        try {
            if (configuration == null || configuration.getCredential() == null) {
                this.password.setText(null);
                this.username.setText(null);
                this.tenantType.setSelectedItem(null);
                this.tenant.setText(null);
                this.fixedContainers.setText(""); //$NON-NLS-1$
                this.overridedSwiftURL.setText(""); //$NON-NLS-1$
                this.usePublicURL.setSelected(true);
                this.usePrivateURL.setSelected(false);
                this.forceSpecificUrl.setSelected(false);
            } else {
                SwiftJSonCredentials c = (SwiftJSonCredentials) configuration.getCredential();
                char[] password = c.getPassword();
                if (password == null || password.length == 0) {
                    String encoded = c.getCryptedPassword();
                    if (encoded != null) {
                        try {
                            password = MasterPasswordService.getInstance(this).decode(this, encoded);
                        } catch (MasterPasswordServiceNotAvailableException e) {
                            e.printStackTrace();
                        }
                        this.password.setText(new String(password));
                    }
                } else {
                    this.password.setText(new String(password));
                }
                this.username.setText(c.getUser());
                this.tenant.setToolTipText(Messages.getString("tenantNameOrIdHint")); //$NON-NLS-1$
                this.tenant.setText(c.getTenantValue());
                this.tenantType.setSelectedItem(c.getTenantType());
                this.fixedContainers.setText(c.getContainers());
                this.overridedSwiftURL.setText(c.getOverridedSwiftUrl());
                this.usePublicURL.setSelected(URL_TYPE.publicURL.equals(c.getUrlType()));
                this.usePrivateURL.setSelected(URL_TYPE.internalUrl.equals(c.getUrlType()));
                this.forceSpecificUrl.setSelected(URL_TYPE.overrideUrl.equals(c.getUrlType()));
                String url = configuration.getTokenUrlAsString() == null ? CLOUDWATT.url : configuration.getTokenUrlAsString();
                for (MapedValue en : staticPredefined) {
                    if (en.url.equals(url)) {
                        this.predefinedURLs.setSelectedItem(en);
                        return;
                    }
                }
                this.predefinedURLs.removeAllItems();
                this.predefinedURLs.addItem(new MapedValue(Messages.getString("customUrl"), url)); //$NON-NLS-1$
                this.predefinedURLs.setSelectedIndex(0);
                for (MapedValue en : staticPredefined) {
                    this.predefinedURLs.addItem(en);
                }
            }
        } finally {
            boolean readyToConnect = checkReadyToConnect();
            setReadyToConnect(readyToConnect);
            saveAction.setEnabled(false);
            if (readyToConnect)
                password.requestFocusInWindow();
        }
    }

    private java.util.List<ActionListener> listeners = new LinkedList<ActionListener>();

    public final static String PROPERTY_READY_TO_CONNECT = "readyToConnect"; //$NON-NLS-1$

    /**
     * Add a save action listener
     * 
     * @param listener
     */
    public void addSaveActionListener(ActionListener listener) {
        listeners.add(listener);
    }

    /**
     * Loads an icon
     * 
     * @param name
     * @param alt
     * @return
     */
    public static ImageIcon loadIconFromFullPath(String fullPath, String alt) {
        URL imageURL = SwiftConfiguration.class.getResource(fullPath);
        if (imageURL == null)
            return null;
        ImageIcon icon = new ImageIcon(imageURL, alt);
        return icon;
    }

    /**
     * Loads an icon
     * 
     * @param name
     * @param alt
     * @return
     */
    public static ImageIcon loadIcon(String name, String alt) {
        return loadIconFromFullPath(iconsBase + name + ".png", alt); //$NON-NLS-1$ 
    }

    private final static String iconsBase = "/net/souchay/swift/gui/"; //$NON-NLS-1$

    private final static ImageIcon fileIcon = loadIcon("file", "file"); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Loads a given mime icon
     * 
     * @param mime the mime type to load
     * @return the mime-type
     */
    public static ImageIcon loadMimeIcon(String mime) {
        if (mime == null)
            return fileIcon;
        ImageIcon icon = mimeIcons.get(mime);
        String mime2 = mimeReplacements.get(mime);
        if (mime2 != null)
            mime = mime2;
        if (icon == null) {
            String split[] = mime.split("/"); //$NON-NLS-1$
            if (split.length != 2) {
                icon = fileIcon;
            } else {
                mime = mime.replaceAll("-", "_"); //$NON-NLS-1$ //$NON-NLS-2$
                icon = loadIconFromFullPath(iconsBase + "mime/" + mime.replace('/', '_') + ".png", mime); //$NON-NLS-1$ //$NON-NLS-2$
                if (icon == null) {
                    icon = loadIconFromFullPath(iconsBase + "mime/" + split[0] + ".png", mime); //$NON-NLS-1$ //$NON-NLS-2$
                    if (icon == null)
                        icon = fileIcon;
                }
            }
        }
        mimeIcons.put(mime, icon);
        return icon;
    }

    private static Hashtable<String, ImageIcon> mimeIcons = new Hashtable<String, ImageIcon>();

    private static final Map<String, String> mimeReplacements = new HashMap<String, String>();

    static {
        mimeReplacements.put("application/msword", //$NON-NLS-1$
                             "application/vnd.ms-word"); //$NON-NLS-1$
        mimeReplacements.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", //$NON-NLS-1$
                             "application/vnd.ms-word"); //$NON-NLS-1$
        mimeReplacements.put("application/vnd.openxmlformats-officedocument.wordprocessingml.template", //$NON-NLS-1$
                             "application/vnd.ms-word"); //$NON-NLS-1$
        mimeReplacements.put("application/vnd.ms-word.document.macroEnabled.12", //$NON-NLS-1$
                             "application/vnd.ms-word"); //$NON-NLS-1$
        mimeReplacements.put("application/vnd.ms-word.template.macroEnabled.12", //$NON-NLS-1$
                             "application/vnd.ms-word"); //$NON-NLS-1$
        mimeReplacements.put("application/vnd.ms-excel", "application/vnd.ms-excel");//$NON-NLS-2$ //$NON-NLS-1$
        mimeReplacements.put("application/vnd.ms-excel", "application/vnd.ms-excel");//$NON-NLS-2$ //$NON-NLS-1$
        mimeReplacements.put("application/vnd.ms-excel", "application/vnd.ms-excel");//$NON-NLS-2$ //$NON-NLS-1$
        mimeReplacements.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", //$NON-NLS-1$
                             "application/vnd.ms-excel"); //$NON-NLS-1$
        mimeReplacements.put("application/vnd.openxmlformats-officedocument.spreadsheetml.template", //$NON-NLS-1$
                             "application/vnd.ms-excel"); //$NON-NLS-1$
        mimeReplacements.put("application/vnd.ms-excel.sheet.macroEnabled.12", "application/vnd.ms-excel");//$NON-NLS-2$ //$NON-NLS-1$
        mimeReplacements.put("application/vnd.ms-excel.template.macroEnabled.12", "application/vnd.ms-excel");//$NON-NLS-2$ //$NON-NLS-1$
        mimeReplacements.put("application/vnd.ms-excel.addin.macroEnabled.12", "application/vnd.ms-excel");//$NON-NLS-2$ //$NON-NLS-1$
        mimeReplacements.put("application/vnd.ms-excel.sheet.binary.macroEnabled.12", "application/vnd.ms-excel");//$NON-NLS-2$ //$NON-NLS-1$

        mimeReplacements.put("application/vnd.openxmlformats-officedocument.presentationml.presentation",//$NON-NLS-1$
                             "application/vnd.ms-powerpoint");//$NON-NLS-1$
        mimeReplacements.put("application/vnd.openxmlformats-officedocument.presentationml.template",//$NON-NLS-1$
                             "application/vnd.ms-powerpoint");//$NON-NLS-1$
        mimeReplacements.put("application/vnd.openxmlformats-officedocument.presentationml.slideshow",//$NON-NLS-1$
                             "application/vnd.ms-powerpoint");//$NON-NLS-1$
        mimeReplacements.put("application/vnd.ms-powerpoint.addin.macroEnabled.12", "application/vnd.ms-powerpoint");//$NON-NLS-2$ //$NON-NLS-1$
        mimeReplacements.put("application/vnd.ms-powerpoint.presentation.macroEnabled.12",//$NON-NLS-1$
                             "application/vnd.ms-powerpoint");//$NON-NLS-1$
        mimeReplacements.put("application/vnd.ms-powerpoint.template.macroEnabled.12", "application/vnd.ms-powerpoint");//$NON-NLS-2$ //$NON-NLS-1$
        mimeReplacements.put("application/vnd.ms-powerpoint.slideshow.macroEnabled.12", "application/vnd.ms-powerpoint");//$NON-NLS-2$ //$NON-NLS-1$
        mimeReplacements.put("application/x-compressed", "application/x-compressed-tar"); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
