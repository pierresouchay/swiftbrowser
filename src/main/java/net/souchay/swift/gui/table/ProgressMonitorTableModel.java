/**
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-07-07 12:12:06 +0200 (Lun 07 jul 2014) $
 */
package net.souchay.swift.gui.table;

/**
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3835 $
 * 
 */
public class ProgressMonitorTableModel extends AbstractPropertyChangeTableModel<ProgressMonitorResult> {

    /**
     * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
     */
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 3 && aValue instanceof Boolean) {
            Boolean newVal = (Boolean) aValue;
            ProgressMonitorResult m = getRow(rowIndex);
            m.setCanceled(newVal);
            return;
        }
        super.setValueAt(aValue, rowIndex, columnIndex);
    }

    /**
     * 
     */
    private static final long serialVersionUID = -3528418962057287228L;

    @Override
    public int getColumnCount() {
        return 4;
    }

    /**
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 1:
                return Messages.getString("operation"); //$NON-NLS-1$
            case 0:
                return Messages.getString("progress"); //$NON-NLS-1$
            case 2:
                return Messages.getString("note"); //$NON-NLS-1$
            case 3:
                return Messages.getString("canceled"); //$NON-NLS-1$
            default:
                return super.getColumnName(column);
        }
    }

    /**
     * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 3)
            return true;
        return super.isCellEditable(rowIndex, columnIndex);
    }

    /**
     * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
     */
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 1:
                return String.class;
            case 0:
                return Integer.class;
            case 2:
                return String.class;
            case 3:
                return Boolean.class;
            default:
                return super.getColumnClass(columnIndex);
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ProgressMonitorResult r = getRow(rowIndex);
        if (r == null)
            return null;
        switch (columnIndex) {
            case 1:
                return r.getMessage();
            case 0:
                return (int) (100L * r.getProgress() / r.getMaximum());
            case 2:
                return r.getNote();
            case 3:
                return r.isCanceled();
            default:
                return null;
        }
    }

}
