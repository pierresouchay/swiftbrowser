package net.souchay.swift.gui.table;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/**
 * Panel for displaying and editing headers
 * 
 * @author Pierre Souchay <pierre@souchay.net> (last changed by $LastChangedBy: souchay $)
 * @version $Revision: 3830 $
 * 
 */
public class HeadersPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 5221123219950824381L;

    private final Set<String> blacklistedHeaders;

    private final JTable table;

    private final HeadersTableModel model;

    public URL url;

    /**
     * get the url
     * 
     * @return the url
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Set the url
     * 
     * @param url the url to set
     */
    public void setUrl(URL url) {
        this.url = url;
    }

    private final int firstColumnSize;

    public HeadersPanel(Set<String> blacklistedHeaders, Collection<String> predefinedElementsToAdd, final Action apply) {
        super(new BorderLayout(0, 0));
        this.blacklistedHeaders = blacklistedHeaders;
        this.model = new HeadersTableModel();
        this.table = new JTable(model);
        HeaderRowRenderer headerRowRender = new HeaderRowRenderer();
        HeaderRowEditor headerRowEditor = new HeaderRowEditor();
        {
            table.setDefaultEditor(String.class, headerRowEditor);
            table.setDefaultRenderer(String.class, headerRowEditor);
        }
        headerRowRender.setupRenderer(table);
        // JScrollPane pane = new JScrollPane(table);
        // pane.setPreferredSize(new Dimension(240, 96));
        // add(pane, BorderLayout.CENTER);
        table.setMinimumSize(new Dimension(240, 48));
        // table.setPreferredSize(new Dimension(240, 48));
        add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel flowPanel = new JPanel(new BorderLayout(5, 5));
        // flowPanel.setBorder(BorderFactory.createTitledBorder(Messages.getString("addNewHeader")));
        final JComboBox<String> headerName = new JComboBox<String>(new Vector<String>(predefinedElementsToAdd));
        final JTextField fieldValue = new JTextField(32);
        headerName.setEditable(true);
        flowPanel.add(headerName, BorderLayout.LINE_START);
        flowPanel.add(fieldValue, BorderLayout.CENTER);
        final Action addAction = new AbstractAction(Messages.getString("addHeader")) { //$NON-NLS-1$

            /**
             * 
             */
            private static final long serialVersionUID = 4815757964945810144L;

            @Override
            public void actionPerformed(ActionEvent e) {
                model.setHeader((String) headerName.getSelectedItem(), fieldValue.getText());
            }
        };
        addAction.setEnabled(false);
        final Runnable checkActionOK = new Runnable() {

            @Override
            public void run() {
                String v1 = (String) headerName.getSelectedItem();
                String v2 = fieldValue.getText();
                addAction.setEnabled(v1 != null && v2 != null && !v1.trim().isEmpty() && !v2.trim().isEmpty());
            }
        };
        table.setRowHeight(headerName.getPreferredSize().height);
        firstColumnSize = headerName.getPreferredSize().width;
        table.getColumnModel().getColumn(0).setPreferredWidth(firstColumnSize);

        headerName.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                checkActionOK.run();
            }
        });
        fieldValue.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkActionOK.run();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                checkActionOK.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkActionOK.run();
            }
        });
        fieldValue.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                checkActionOK.run();
                if (addAction.isEnabled())
                    addAction.actionPerformed(e);
            }
        });
        apply.setEnabled(false);
        model.addPropertyChangeListener(HeadersTableModel.PROPERTY_MODIFIED, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (HeadersTableModel.PROPERTY_MODIFIED.equals(evt.getPropertyName()))
                    apply.setEnabled(Boolean.TRUE.equals(evt.getNewValue()));
            }
        });
        flowPanel.add(new JButton(addAction), BorderLayout.LINE_END);
        {
            JPanel daSouth = new JPanel(new BorderLayout(0, 0));
            JPanel applyPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
            applyPanel.add(new JButton(apply));
            daSouth.add(applyPanel, BorderLayout.NORTH);
            daSouth.add(flowPanel, BorderLayout.SOUTH);
            add(daSouth, BorderLayout.SOUTH);
        }
        model.addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent e) {
                table.setPreferredSize(null);
                if (getPreferredSize().height < 32)
                    setPreferredSize(new Dimension(240, 32));
                invalidate();
                revalidate();
                table.getColumnModel().getColumn(0).setPreferredWidth(firstColumnSize);
            }
        });
    }

    public void setHeaders(Map<String, String> headers) {
        model.setHeaders(headers, blacklistedHeaders);
    }

    /**
     * Get all the modified headers
     * 
     * @return a collection (may be empty)
     */
    public Collection<HeaderRow> getModifiedHeaders() {
        return model.getModifiedHeaders();
    }

}
