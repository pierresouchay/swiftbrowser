/**
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-07-07 12:12:06 +0200 (Lun 07 jul 2014) $
 */
package net.souchay.swift.gui.table;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;
import net.souchay.swift.gui.PropertyChangeRegistration;

/**
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3835 $
 * 
 */
public abstract class AbstractPropertyChangeTableModel<T extends PropertyChangeRegistration> extends AbstractTableModel
        implements PropertyChangeListener, ListModel<T> {

    /**
     * 
     */
    private static final long serialVersionUID = 5139420218076791109L;

    @Override
    public int getSize() {
        return getRowCount();
    }

    @Override
    public T getElementAt(int index) {
        return getRow(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        listListeners.add(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        listListeners.remove(l);
    }

    private final List<ListDataListener> listListeners = new LinkedList<ListDataListener>();

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        final Runnable r = new Runnable() {

            @Override
            public void run() {
                Object o = evt.getSource();
                final int idx = list.indexOf(o);
                for (ListDataListener l : listListeners) {
                    l.contentsChanged(new ListDataEvent(AbstractPropertyChangeTableModel.this,
                                                        ListDataEvent.CONTENTS_CHANGED,
                                                        idx,
                                                        idx));
                }
                fireTableRowsUpdated(idx, idx);
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    private final List<T> list = new ArrayList<T>();

    /**
     * @see javax.swing.table.TableModel#getRowCount()
     */
    @Override
    public int getRowCount() {
        return list.size();
    }

    /**
     * Get the row
     * 
     * @param rowIndex
     * @return the row
     */
    public T getRow(int rowIndex) {
        return list.get(rowIndex);
    }

    /**
     * Adds a row
     * 
     * @param row
     */
    public synchronized void addRow(final T row) {
        final Runnable r = new Runnable() {

            @Override
            public void run() {
                if (list.add(row)) {
                    final int idx = list.size() - 1;
                    for (ListDataListener l : listListeners) {
                        l.intervalAdded(new ListDataEvent(AbstractPropertyChangeTableModel.this,
                                                          ListDataEvent.INTERVAL_ADDED,
                                                          idx,
                                                          idx));
                    }
                    fireTableRowsInserted(idx, idx);
                    row.addPropertyChangeListener(AbstractPropertyChangeTableModel.this);
                }
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    /**
     * Adds a row
     * 
     * @param row the row to add
     */
    public synchronized void removeRow(final T row) {
        final Runnable r = new Runnable() {

            @Override
            public void run() {
                final int idx = list.indexOf(row);
                row.removePropertyChangeListener(AbstractPropertyChangeTableModel.this);
                if (idx >= 0) {
                    list.remove(idx);
                    for (ListDataListener l : listListeners) {
                        l.intervalRemoved(new ListDataEvent(AbstractPropertyChangeTableModel.this,
                                                            ListDataEvent.INTERVAL_REMOVED,
                                                            idx,
                                                            idx));
                    }
                    fireTableRowsDeleted(idx, idx);
                }
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

}
