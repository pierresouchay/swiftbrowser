package net.souchay.swift.gui.table;

import java.awt.Component;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import net.souchay.swift.gui.ACLParser;
import net.souchay.swift.gui.ContainerObject;
import net.souchay.swift.gui.MimeTypeSetter;

/**
 * 
 * @author Pierre Souchay <pierre@souchay.net> (last changed by $LastChangedBy: souchay $)
 * @version $Revision: 3830 $
 * 
 */
public class HeaderRowEditor extends DefaultTableCellRenderer implements TableCellEditor, TableCellRenderer {

    /**
     * 
     */
    private static final long serialVersionUID = 3698833648167693514L;

    /**
     * Default Constructor
     */
    public HeaderRowEditor() {
        super();
        combo.setEditable(true);
    }

    private final Vector<String> values = new Vector<String>();

    private final JComboBox<String> combo = new JComboBox<String>(values);

    private final JComboBox<String> comboRo = new JComboBox<String>();

    private final DefaultCellEditor editor = new DefaultCellEditor(combo);

    private ComboBoxModel<String> EMPTY_MODEL = new DefaultComboBoxModel<String>();

    private Map<String, ComboBoxModel<String>> predefinedValues = new HashMap<String, ComboBoxModel<String>>();

    {
        comboRo.setEditable(true);
        TreeSet<String> mimes = new TreeSet<String>();
        mimes.add(MimeTypeSetter.DEFAULT_MIME);
        mimes.addAll(MimeTypeSetter.mimes.values());
        {
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>();
            for (String s : mimes) {
                model.addElement(s);
            }
            predefinedValues.put("content-type", model); //$NON-NLS-1$
        }
        {
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>();
            model.addElement("true"); //$NON-NLS-1$ 
            model.addElement("false"); //$NON-NLS-1$
            predefinedValues.put(ContainerObject.X_CONTAINER_META_LISTINGS.toLowerCase(Locale.ENGLISH), model);
        }
        {
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>();
            model.addElement(ACLParser.R_FOR_ALL);
            model.addElement(ACLParser.R_LISTINGS);
            model.addElement(ACLParser.R_FOR_ALL + ACLParser.ACL_SEPARATOR + ACLParser.R_LISTINGS);
            model.addElement("TENANT:USER"); //$NON-NLS-1$
            predefinedValues.put(ContainerObject.PROPERTY_CONTAINER_READ_ACL.toLowerCase(Locale.ENGLISH), model);
        }
        {
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>();
            model.addElement("inline"); //$NON-NLS-1$ 
            model.addElement("attachment"); //$NON-NLS-1$
            model.addElement("attachment; filename=\"file.txt\""); //$NON-NLS-1$
            predefinedValues.put("content-disposition", model); //$NON-NLS-1$
        }
    }

    @Override
    public Object getCellEditorValue() {
        return editor.getCellEditorValue();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        HeadersTableModel model = (HeadersTableModel) table.getModel();
        HeaderRow r = model.getValueAt(row);
        ComboBoxModel<String> comboBoxModel = predefinedValues.get(r.getHeaderName());
        if (comboBoxModel == null)
            comboBoxModel = EMPTY_MODEL;
        combo.setModel(comboBoxModel);
        combo.setSelectedItem(value);
        return editor.getTableCellEditorComponent(table, value, isSelected, row, column);
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return editor.isCellEditable(anEvent);
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return editor.shouldSelectCell(anEvent);
    }

    @Override
    public boolean stopCellEditing() {
        return editor.stopCellEditing();
    }

    @Override
    public void cancelCellEditing() {
        editor.cancelCellEditing();
    }

    @Override
    public void addCellEditorListener(CellEditorListener l) {
        editor.addCellEditorListener(l);
    }

    @Override
    public void removeCellEditorListener(CellEditorListener l) {
        editor.removeCellEditorListener(l);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        HeaderRow obj = ((HeadersTableModel) (table.getModel())).getValueAt(row);
        if (!obj.isEditable())
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        comboRo.setSelectedItem(value);
        return comboRo;
    }

}
