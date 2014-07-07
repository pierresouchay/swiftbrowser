/**
 *
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-01-11 02:39:12 +0100 (Sam 11 jan 2014) $
 */
package net.souchay.swift.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import net.souchay.swift.gui.configuration.SwiftConfigurationEditor;
import net.souchay.swift.net.SwiftConfiguration;
import net.souchay.swift.net.SwiftJSonCredentials;
import org.jdesktop.swingx.JXCollapsiblePane;

/**
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3704 $
 * 
 */
public class SwiftConfigurationPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = -1737475875305678470L;

    private final JList<SwiftConfiguration> allConfigurations;

    private final JToolBar toolbar = new JToolBar(Messages.getString("SwiftConfigurationPanel.configurations")); //$NON-NLS-1$

    private final DefaultListModel<SwiftConfiguration> configs = new DefaultListModel<SwiftConfiguration>();

    private SwiftConfiguration createNewConfig() {
        SwiftConfiguration c = new SwiftConfiguration(new SwiftJSonCredentials(Messages.getString("SwiftConfigurationPanel.exampleAccount"), new char[0]), null); //$NON-NLS-1$
        c.setName(Messages.getString("SwiftConfigurationPanel.defaultConfigurationName", configs.size() + 1)); //$NON-NLS-1$
        return c;
    }

    private void autoAddConfig() {
        if (configs.isEmpty()) {
            addAction.actionPerformed(null);
            allConfigurations.setSelectedIndex(0);
        }
        updateActionsOnSelection();
    }

    // private final Action moveUpAction = new AbstractAction() {
    //
    // /**
    // *
    // */
    // private static final long serialVersionUID = 7370977397473429425L;
    //
    // {
    //            final String name = Messages.getString("SwiftConfigurationPanel.moveUp"); //$NON-NLS-1$
    // putValue(Action.NAME, name);
    // putValue(Action.SHORT_DESCRIPTION, name);
    //            putValue(Action.SMALL_ICON, SwiftConfigurationEditor.loadIcon("up", "SwiftConfigurationPanel.moveUp")); //$NON-NLS-1$ //$NON-NLS-2$
    // }
    //
    // @Override
    // public void actionPerformed(ActionEvent e) {
    // int index = allConfigurations.getSelectedIndex();
    // if (index < 1)
    // return;
    // SwiftConfiguration cfg = configs.remove(index);
    // configs.add(index - 1, cfg);
    // updateActionsOnSelection();
    // }
    // };

    // private final Action moveDownAction = new AbstractAction() {
    //
    // /**
    // *
    // */
    // private static final long serialVersionUID = 8703575388092415624L;
    //
    // {
    //            final String name = Messages.getString("SwiftConfigurationPanel.moveDown"); //$NON-NLS-1$
    // putValue(Action.NAME, name);
    // putValue(Action.SHORT_DESCRIPTION, name);
    //            putValue(Action.SMALL_ICON, SwiftConfigurationEditor.loadIcon("down", name)); //$NON-NLS-1$
    // }
    //
    // @Override
    // public void actionPerformed(ActionEvent e) {
    // int index = allConfigurations.getSelectedIndex();
    // if (index > (configs.size() - 1))
    // return;
    // SwiftConfiguration cfg = configs.remove(index);
    // configs.add(index + 1, cfg);
    // updateActionsOnSelection();
    // }
    // };

    private void updateActionsOnSelection() {
        int idx = allConfigurations.getSelectedIndex();
        if (idx < 0) {
            // moveDownAction.setEnabled(false);
            // moveUpAction.setEnabled(false);
            deleteAction.setEnabled(false);
            return;
        }
        // moveDownAction.setEnabled(idx < (configs.size() - 1));
        // moveUpAction.setEnabled(idx > 0);
        deleteAction.setEnabled(true);
    }

    private final Action deleteAction = new AbstractAction() {

        /**
         * 
         */
        private static final long serialVersionUID = 7106443883431779847L;

        {
            final String name = Messages.getString("SwiftConfigurationPanel.delete"); //$NON-NLS-1$
            putValue(Action.NAME, name);
            putValue(Action.SHORT_DESCRIPTION, name);
            putValue(Action.SMALL_ICON, SwiftConfigurationEditor.loadIcon("delete", name)); //$NON-NLS-1$
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            final int index = allConfigurations.getSelectedIndex();
            if (index < 0)
                return;
            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(SwiftConfigurationPanel.this,
                                                                        Messages.getString("SwiftConfigurationPanel.youAreAboutToDelete"), //$NON-NLS-1$
                                                                        Messages.getString("SwiftConfigurationPanel.areYourSureToDelete"), //$NON-NLS-1$
                                                                        JOptionPane.YES_NO_OPTION)) {
                SwiftConfiguration cfg = configs.remove(index);
                if (cfg != null && cfg.getId() != null)
                    ConfigurationPersistance.getInstance().deleteConfiguration(cfg);
                autoAddConfig();
            }

        }
    };

    private final Action addAction = new AbstractAction() {

        /**
         * 
         */
        private static final long serialVersionUID = -7141773341014521885L;

        {
            final String name = Messages.getString("SwiftConfigurationPanel.new"); //$NON-NLS-1$
            putValue(Action.NAME, name);
            putValue(Action.SHORT_DESCRIPTION, name);
            putValue(Action.SMALL_ICON, SwiftConfigurationEditor.loadIcon("connectionAdd", name)); //$NON-NLS-1$
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int index = allConfigurations.getSelectedIndex();
            if (index < 0) {
                index = configs.size();
            } else {
                index++;
            }
            configs.add(index, createNewConfig());
            updateActionsOnSelection();
        }
    };

    private final SwiftConfigurationEditor editor;

    /**
     * Constructor
     */
    public SwiftConfigurationPanel(final Action connectAction) {
        super(new BorderLayout());
        JLabel loadingConfigs = new JLabel(Messages.getString("loadingConfigurations")); //$NON-NLS-1$
        allConfigurations = new JList<SwiftConfiguration>(configs);
        allConfigurations.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        final JXCollapsiblePane south = new JXCollapsiblePane();
        final JScrollPane js = new JScrollPane(loadingConfigs);
        js.setPreferredSize(new Dimension(320, 180));
        add(js, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
        south.setCollapsed(true);
        south.setAnimated(true);
        Action actions[] = new Action[] { connectAction };
        editor = new SwiftConfigurationEditor(actions);
        connectAction.setEnabled(editor.isReadyToConnect());
        editor.getPassword().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (connectAction.isEnabled())
                    connectAction.actionPerformed(e);
            }
        });
        editor.addPropertyChangeListener(SwiftConfigurationEditor.PROPERTY_READY_TO_CONNECT,
                                         new PropertyChangeListener() {

                                             @Override
                                             public void propertyChange(PropertyChangeEvent evt) {
                                                 connectAction.setEnabled(editor.isReadyToConnect());
                                             }
                                         });
        south.add(editor);
        editor.addSaveActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int index = allConfigurations.getSelectedIndex();
                SwiftConfiguration cfg = editor.getConfiguration();
                if (index < 0) {
                    configs.add(configs.size(), cfg);
                } else {
                    configs.set(index, cfg);
                }
            }
        });
        allConfigurations.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                SwiftConfiguration config = allConfigurations.getSelectedValue();
                editor.setConfiguration(config);
                boolean validConfig = config != null;
                south.setCollapsed(!validConfig);
                deleteAction.setEnabled(validConfig);
                updateActionsOnSelection();
            }
        });
        toolbar.add(addAction);
        toolbar.add(deleteAction);
        // toolbar.add(moveDownAction);
        // toolbar.add(moveUpAction);
        super.add(toolbar, BorderLayout.PAGE_START);
        updateActionsOnSelection();
        Thread t = new Thread("LoadConfigurations") { //$NON-NLS-1$

            @Override
            public void run() {
                final List<SwiftConfiguration> loadedConfigs = ConfigurationPersistance.getInstance().loadConfigs();
                Collections.sort(loadedConfigs);
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        for (SwiftConfiguration cfg : loadedConfigs) {
                            configs.addElement(cfg);
                        }
                        autoAddConfig();
                        js.setViewportView(allConfigurations);
                    }
                });
            }
        };
        t.start();
    }

    /**
     * Get the toolbar
     * 
     * @return
     */
    public JToolBar getToolbar() {
        return toolbar;
    }

    /**
     * Get the currently selected value
     * 
     * @return
     */
    public SwiftConfiguration getSelectedSavedConfiguration() {
        return allConfigurations.getSelectedValue();
    }

    /**
     * Get the currently selected value
     * 
     * @return
     */
    public SwiftConfiguration getCurrentConfiguration() throws MalformedURLException {
        return editor.getCurrentConfiguration();
    }
}
