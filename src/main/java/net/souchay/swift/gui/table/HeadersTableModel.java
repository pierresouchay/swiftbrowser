/**
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-07-07 12:12:06 +0200 (Lun 07 jul 2014) $
 */
package net.souchay.swift.gui.table;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import net.souchay.swift.net.FsConnection;
import net.souchay.swift.net.SwiftConnections;

/**
 * @author Pierre Souchay <pierre@souchay.net> (last changed by $LastChangedBy: souchay $)
 * @version $Revision: 3835 $
 * 
 */
public class HeadersTableModel extends AbstractTableModel {

    /**
     * 
     */
    private static final long serialVersionUID = 7820322521976615686L;

    /**
     * Default constructor
     */
    public HeadersTableModel() {
    }

    /**
     * @see javax.swing.table.TableModel#getRowCount()
     */
    @Override
    public int getRowCount() {
        return rows.size();
    }

    /**
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    @Override
    public int getColumnCount() {
        return 2;
    }

    /**
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        final HeaderRow r = getValueAt(rowIndex);
        switch (columnIndex) {
            case 0:
                return r;
            case 1:
                return r.getHeaderValue();
            default:
                return null;
        }
    }

    /**
     * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
     */
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex != 1)
            return;
        HeaderRow row = getValueAt(rowIndex);
        setHeader(row.getHeaderName(), (String) aValue);

    }

    /**
     * Get the header at given index
     * 
     * @param rowIndex the row index
     * @return the header
     */
    public HeaderRow getValueAt(int rowIndex) {
        return rows.get(rowIndex);
    }

    /**
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return Messages.getString("header.name"); //$NON-NLS-1$
            case 1:
                return Messages.getString("header.value"); //$NON-NLS-1$
            default:
                return null;
        }
    }

    /**
     * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
     */
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0)
            return HeaderRow.class;
        return String.class;
    }

    /**
     * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1 && getValueAt(rowIndex).isEditable();
    }

    private final static Set<String> unmodifiableHeaders;

    static {
        List<String> list = new LinkedList<String>(Arrays.asList(new String[] {
                                                                               "accept-ranges", FsConnection.DATE, FsConnection.ETAG, "content-length", "last-modified", "accept-range", "keep-alive", "x-timestamp", "x-trans-id", "connection" })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
        for (String s : SwiftConnections.X_ACCOUNT_PROPERTIES_INT) {
            list.add(s.toLowerCase(Locale.ENGLISH));
        }
        Collections.sort(list);
        unmodifiableHeaders = Collections.unmodifiableSet(new HashSet<String>(list));
        // = Collections.unmodifiableSet(new HashSet<String>();
    }

    private List<HeaderRow> rows = new ArrayList<HeaderRow>();

    private static final HeaderRow addHeaderRow(List<HeaderRow> r, String k, String value,
            Set<String> blacklistedHeaders) {
        if (k != null) {
            k = k.toLowerCase(Locale.ENGLISH);
            if (!blacklistedHeaders.contains(k)) {
                HeaderRow rx = new HeaderRow(k, value, !unmodifiableHeaders.contains(k));
                r.add(rx);
                return rx;
            }
        }
        return null;
    }

    /**
     * Set the headers. Can be called within or outside EDT
     * 
     * @param headers the headers to use
     */
    public void setHeaders(final Map<String, String> headers, Set<String> blacklistedHeaders) {
        final List<HeaderRow> r;
        if (headers == null) {
            r = Collections.emptyList();
        } else {
            r = new ArrayList<HeaderRow>(headers.size());
            for (Map.Entry<String, String> en : headers.entrySet()) {
                String k = en.getKey();
                addHeaderRow(r, k, en.getValue(), blacklistedHeaders);
            }
            Collections.sort(r);
        }
        final Runnable run = new Runnable() {

            @Override
            public void run() {
                rows = r;
                fireTableDataChanged();
                support.firePropertyChange(PROPERTY_MODIFIED, true, false);
            }

        };
        if (SwingUtilities.isEventDispatchThread())
            run.run();
        else
            SwingUtilities.invokeLater(run);
    }

    /**
     * Get the list of modified headers
     * 
     * @return the list of modified values
     */
    public List<HeaderRow> getModifiedHeaders() {
        LinkedList<HeaderRow> modified = new LinkedList<HeaderRow>();
        for (HeaderRow h : rows) {
            if (h.isModified()) {
                modified.add(h);
            }
        }
        return modified;
    }

    public void setHeader(final String header, final String headerVal) {
        final String headerValue = headerVal == null ? "" : headerVal; //$NON-NLS-1$
        final String headerName = header.toLowerCase(Locale.ENGLISH);
        Runnable r = new Runnable() {

            @Override
            public void run() {
                boolean oldValue = isModified();
                int rowIndex = 0;
                for (HeaderRow r : rows) {
                    if (r.getHeaderName().equals(headerName)) {
                        // modified
                        r.setHeaderValue(headerValue);
                        fireTableCellUpdated(rowIndex, 1);
                        support.firePropertyChange(PROPERTY_MODIFIED, oldValue, isModified());
                        return;
                    }
                    rowIndex++;
                }
                final String h = headerName.trim().toLowerCase(Locale.ENGLISH);
                HeaderRow rx = addHeaderRow(rows, h, "", Collections.<String> emptySet()); //$NON-NLS-1$
                if (rx != null) {
                    rx.setHeaderValue(headerValue);
                    int rowCount = rows.size() - 1;
                    fireTableRowsInserted(rowCount, rowCount);
                }
                support.firePropertyChange(PROPERTY_MODIFIED, oldValue, isModified());
            }

        };
        if (SwingUtilities.isEventDispatchThread())
            r.run();
        else
            SwingUtilities.invokeLater(r);
    }

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String property, PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    /**
     * Return true if there are some modified values
     * 
     * @return
     */
    public boolean isModified() {
        return !getModifiedHeaders().isEmpty();
    }

    public final static String PROPERTY_MODIFIED = "modified"; //$NON-NLS-1$
}
