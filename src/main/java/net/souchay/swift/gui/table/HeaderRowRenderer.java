package net.souchay.swift.gui.table;

import java.awt.Component;
import java.awt.Font;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Row renderer and editor
 * 
 * @author Pierre Souchay <pierre@souchay.net> (last changed by $LastChangedBy: souchay $)
 * @version $Revision: 3830 $
 * 
 */
public class HeaderRowRenderer extends DefaultTableCellRenderer {

    /**
     * 
     */
    private static final long serialVersionUID = 1738986657909307247L;

    /**
     * Constructor
     */
    public HeaderRowRenderer() {
    }

    /**
     * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable,
     *      java.lang.Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        HeaderRow obj = (HeaderRow) value;
        JLabel lbl = (JLabel) super.getTableCellRendererComponent(table,
                                                                  obj.getHeaderDisplayName(),
                                                                  isSelected,
                                                                  hasFocus,
                                                                  row,
                                                                  column);
        lbl.setToolTipText(obj.getHeaderName());
        if (obj.isEditable()) {
            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        } else {
            lbl.setFont(lbl.getFont().deriveFont(Font.ITALIC));
        }
        return lbl;
    }

    public void setupRenderer(JTable t) {
        t.setDefaultRenderer(HeaderRow.class, this);
    }

    /**
     * Blacklisted headers
     */
    public final static Set<String> BLACKLISTED_HEADERS_FOR_CONTAINER;

    /**
     * Blacklisted headers
     */
    public final static Set<String> PREDEFINED_HEADERS_FOR_CONTAINER;

    /**
     * Blacklisted headers
     */
    public final static Set<String> PREDEFINED_HEADERS_FOR_FILES;

    /**
     * Blacklisted headers
     */
    public final static Set<String> BLACKLISTED_HEADERS_FOR_FILES;

    static {
        {
            Set<String> common = new HashSet<String>();
            common.add("accept-ranges"); //$NON-NLS-1$
            common.add("connection");//$NON-NLS-1$
            common.add("keep-alive");//$NON-NLS-1$
            common.add("x-timestamp");//$NON-NLS-1$
            common.add("x-trans-id");//$NON-NLS-1$
            common.add("server");//$NON-NLS-1$
            common.add("x-hostname");//$NON-NLS-1$
            BLACKLISTED_HEADERS_FOR_FILES = Collections.unmodifiableSet(new HashSet<String>(common));
            common.add("content-length");//$NON-NLS-1$
            common.add("content-type");//$NON-NLS-1$
            common.add("x-container-bytes-used");//$NON-NLS-1$
            common.add("x-account-bytes-used");//$NON-NLS-1$
            common.add("x-account-meta-quota-bytes");//$NON-NLS-1$
            common.add("x-account-container-count");//$NON-NLS-1$
            common.add("x-account-object-count");//$NON-NLS-1$
            common.add("x-container-object-count");//$NON-NLS-1$
            common.add("x-container-read");//$NON-NLS-1$

            BLACKLISTED_HEADERS_FOR_CONTAINER = Collections.unmodifiableSet(new HashSet<String>(common));
        }
        {
            Set<String> common = new HashSet<String>();
            common.add("Content-Type");//$NON-NLS-1$
            common.add("Content-Disposition");//$NON-NLS-1$
            common.add("X-Delete-At");//$NON-NLS-1$
            PREDEFINED_HEADERS_FOR_FILES = Collections.unmodifiableSet(common);
        }
        {
            Set<String> common = new HashSet<String>();
            common.add("X-Container-Meta-Web-Listings");//$NON-NLS-1$
            common.add("X-Container-Meta-Web-Listings-CSS");//$NON-NLS-1$
            common.add("X-Container-Meta-Web-Index ");//$NON-NLS-1$
            common.add("X-Container-Meta-Web-Error");//$NON-NLS-1$
            //common.add("X-Container-Read");//$NON-NLS-1$
            common.add("X-Container-Write");//$NON-NLS-1$
            PREDEFINED_HEADERS_FOR_CONTAINER = Collections.unmodifiableSet(common);
        }

    }
}
