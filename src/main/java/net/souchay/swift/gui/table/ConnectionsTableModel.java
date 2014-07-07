package net.souchay.swift.gui.table;

import java.net.URL;
import java.util.Date;

/**
 * 
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3830 $
 * 
 */
public class ConnectionsTableModel extends AbstractTableModelFifoWithMaxRows<ConnectionLog> {

    /**
     * 
     */
    private static final long serialVersionUID = 5802879843095782713L;

    @Override
    public int getColumnCount() {
        return 7;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return Messages.getString("ConnectionsTableModel.method"); //$NON-NLS-1$
            case 1:
                return Messages.getString("ConnectionsTableModel.url"); //$NON-NLS-1$
            case 2:
                return Messages.getString("ConnectionsTableModel.time"); //$NON-NLS-1$
            case 3:
                return Messages.getString("ConnectionsTableModel.latency"); //$NON-NLS-1$
            case 4:
                return Messages.getString("ConnectionsTableModel.response"); //$NON-NLS-1$
            case 5:
                return Messages.getString("ConnectionsTableModel.message"); //$NON-NLS-1$
            case 6:
                return Messages.getString("ConnectionsTableModel.length"); //$NON-NLS-1$
            case 7:
                return Messages.getString("ConnectionsTableModel.error"); //$NON-NLS-1$
            default:
                return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return String.class;
            case 1:
                return URL.class;
            case 2:
                return Date.class;
            case 3:
                return Long.class;
            case 4:
                return Integer.class;
            case 5:
                return String.class;
            case 6:
                return Long.class;
            case 7:
                return Throwable.class;
            default:
                return null;
        }
    }

    @Override
    protected Object getValueAt(ConnectionLog row, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return row.getMethod();
            case 1:
                return row.getUrl();
            case 2:
                return row.getStartTime();
            case 3:
                return row.getLatency();
            case 4:
                return row.getHttpResponseCode();
            case 5:
                return row.getMsg();
            case 6:
                return row.getLen();
            case 7:
                return row.getError();
            default:
                return null;
        }
    }

}
