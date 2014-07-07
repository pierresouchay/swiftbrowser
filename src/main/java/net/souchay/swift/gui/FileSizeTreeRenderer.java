/**
 *
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-01-09 16:21:53 +0100 (Jeu 09 jan 2014) $
 */
package net.souchay.swift.gui;

import java.awt.Component;
import java.text.MessageFormat;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3700 $
 * 
 */
public class FileSizeTreeRenderer extends DefaultTableCellRenderer {

    private final static long MB = 1024 * 1024;

    private final static long GB = 1024 * 1024 * 1024;

    /**
     * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable,
     *      java.lang.Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        String x = ""; //$NON-NLS-1$
        if (value != null && value instanceof Number) {
            long v = ((Number) value).longValue();
            if (v < 1024) {
                x = MessageFormat.format(Messages.getString("FileSizeTreeRenderer.bytes"), v); //$NON-NLS-1$
            } else if (v < MB) {
                x = MessageFormat.format(Messages.getString("FileSizeTreeRenderer.kbytes"), v / 1024, (v % 1024) / 100); //$NON-NLS-1$
            } else if (v < GB) {
                x = MessageFormat.format(Messages.getString("FileSizeTreeRenderer.mbytes"), v / MB, (v % MB) / 1024 / 100); //$NON-NLS-1$
            } else {
                x = MessageFormat.format(Messages.getString("FileSizeTreeRenderer.gbytes"), v / GB, (v % GB) / MB / 100); //$NON-NLS-1$
            }
        }
        final JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, x, isSelected, hasFocus, row, column);
        lbl.setHorizontalAlignment(SwingConstants.TRAILING);
        lbl.setToolTipText(value == null ? "" : value.toString()); //$NON-NLS-1$
        return lbl;
    }

    /**
     * 
     */
    private static final long serialVersionUID = -6808423745758860822L;

}
