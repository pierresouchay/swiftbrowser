/**
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-07-07 12:12:06 +0200 (Lun 07 jul 2014) $
 */
package net.souchay.swift.gui.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import net.souchay.utilities.Pair;

/**
 * @author Pierre Souchay <pierre@souchay.net> (last changed by $LastChangedBy: souchay $)
 * @version $Revision: 3835 $
 * 
 */
public class MapTableModel extends AbstractTableModel {

    /**
     * 
     */
    private static final long serialVersionUID = 4785784159630964414L;

    /**
     * Empty Constructor
     */
    public MapTableModel() {
    }

    public MapTableModel setMap(Map<?, ?> map) {
        Set<Pair<? extends Comparable<?>, Object>> set = new TreeSet<Pair<? extends Comparable<?>, Object>>();
        for (Map.Entry<?, ?> en : map.entrySet()) {
            Pair<String, Object> pair = new Pair<String, Object>(String.valueOf(en.getKey()), en.getValue());
            set.add(pair);
        }
        final List<Pair<? extends Comparable<?>, Object>> list = new ArrayList<Pair<? extends Comparable<?>, Object>>(set);
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                values = list;
                fireTableDataChanged();
            }
        });
        return this;
    }

    /**
     * @see javax.swing.table.TableModel#getRowCount()
     */
    @Override
    public int getRowCount() {
        return values.size();
    }

    /**
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    @Override
    public int getColumnCount() {
        return 2;
    }

    /**
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return Messages.getString("Property"); //$NON-NLS-1$
            default:
                return Messages.getString("Value"); //$NON-NLS-1$
        }
    }

    /**
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Pair<? extends Comparable<?>, Object> x = values.get(rowIndex);
        if (columnIndex == 0)
            return x.getLeft();
        return x.getRight();
    }

    private transient List<Pair<? extends Comparable<?>, Object>> values = java.util.Collections.emptyList();

}
