/**
 *
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-07-04 14:51:32 +0200 (Ven 04 jul 2014) $
 */
package net.souchay.swift.gui.table;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

/**
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3830 $
 * @param <T> The type of element to use
 * 
 */
public abstract class AbstractTableModelFifoWithMaxRows<T> extends AbstractTableModel {

    /**
     * 
     */
    private static final long serialVersionUID = 8007271298946400661L;

    /**
     * Constructor
     */
    public AbstractTableModelFifoWithMaxRows() {
    }

    private int currentRow = 0;

    private final ArrayList<T> rows = new ArrayList<T>(50);

    /**
     * Re-implement this to fetch the data from the row
     * 
     * @param row
     * @param columnIndex
     * @return The data at column
     */
    protected abstract Object getValueAt(T row, int columnIndex);

    /**
     * @see javax.swing.table.TableModel#getRowCount()
     */
    @Override
    public final int getRowCount() {
        return rows.size();
    }

    /**
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    @Override
    public final Object getValueAt(int rowIndex, int columnIndex) {
        T row = getRow(rowIndex);
        if (row == null)
            return null;
        return getValueAt(row, columnIndex);
    }

    /**
     * Fires an event when a row has been changed
     * 
     * @param row The changed row
     */
    public synchronized void fireRowChanged(final int idx) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                fireTableRowsUpdated(idx, idx);
            }

        });

    }

    /**
     * Add the given row.
     * 
     * It also refreshes the views
     * 
     * @param newRow The row to add
     * @return The index of row inserted
     */
    public synchronized int addRow(T newRow) {
        final int sz = rows.size();
        final int capacity = getMaxRows();
        if (capacity == sz) {
            final int pos = currentRow;
            rows.set(currentRow, newRow);
            currentRow = (currentRow + 1) % capacity;
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    fireTableDataChanged();
                }
            });
            return pos;
        }
        rows.add(newRow);
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                fireTableDataChanged();

            }
        });
        return sz;
    }

    /**
     * Get the maximum rows contained into this model
     * 
     * @return The number of rows
     */
    public synchronized int getMaxRows() {
        return maxRows;
    }

    private int maxRows = 100;

    /**
     * Sets the maximum of rows
     * 
     * @param maxRows The new maximum of rows
     */
    public void setMaxRows(final int maxRows) {
        if (maxRows < 1)
            throw new IllegalArgumentException("Must be > 0"); //$NON-NLS-1$
        final int oldValue = getMaxRows();
        if (maxRows == oldValue)
            return;
        synchronized (this) {
            // OK, not the best way to do it, but not performed that often
            ArrayList<T> copy = new ArrayList<T>(maxRows);
            final int max = Math.min(getRowCount(), Math.min(maxRows, oldValue));
            for (int i = 0; i < max; i++) {
                copy.add(getRow(i));
            }
            rows.clear();
            for (int i = 0; i < max; i++) {
                rows.add(copy.get(i));
            }
            this.currentRow = 0;
            this.maxRows = maxRows;
        }

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                fireTableDataChanged();
                PropertyChangeEvent evt = new PropertyChangeEvent(this, PROPERTY_MAX_ROWS, oldValue, maxRows);
                for (PropertyChangeListener l : getListeners(PropertyChangeListener.class)) {
                    l.propertyChange(evt);
                }
            }
        });
    }

    /**
     * Get the row at given index
     * 
     * @param rowIndex
     * @return The current row
     */
    public synchronized T getRow(int rowIndex) {
        final int sz = getMaxRows();
        return rows.get((rowIndex + currentRow) % sz);
    }

    /**
     * Clears the table
     */
    public void clear() {
        synchronized (this) {
            rows.clear();
            currentRow = 0;
        }
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                fireTableDataChanged();
            }
        });
    }

    /**
     * The name of property emitted when max entries has been changed
     */
    public final static String PROPERTY_MAX_ROWS = "maxRows"; //$NON-NLS-1$

    /**
     * @param listener
     */
    public void addPropertyChangedListener(PropertyChangeListener listener) {
        listenerList.add(PropertyChangeListener.class, listener);
    }

    /**
     * @param listener
     */
    public void removePropertyChangedListener(PropertyChangeListener listener) {
        listenerList.remove(PropertyChangeListener.class, listener);
    }
}
