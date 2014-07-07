/**
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-07-07 12:12:06 +0200 (Lun 07 jul 2014) $
 */
package net.souchay.swift.gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author Pierre Souchay <pierre@souchay.net> (last changed by $LastChangedBy: souchay $)
 * @version $Revision: 3835 $
 * 
 */
public class LayoutUtils {

    /**
     * 
     */
    public LayoutUtils() {
        // TODO Auto-generated constructor stub
    }

    /**
     * 
     * @param p
     * @param row
     * @param name
     * @param v
     */
    public final static void addRow(JPanel p, int row, String name, Component v) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        JLabel lbl = new JLabel(name);
        c.anchor = GridBagConstraints.BELOW_BASELINE_TRAILING;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        c.ipadx = 3;
        p.add(new JLabel(Messages.getString("SwiftMain.headerDesc", name)), c); //$NON-NLS-1$
        c.anchor = GridBagConstraints.BELOW_BASELINE_LEADING;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        lbl.setLabelFor(v);
        p.add(v, c);
    }

    /**
     * 
     * @param p
     * @param row
     * @param name
     * @param value
     */
    public final static void addRow(JPanel p, int row, String name, String value) {
        final JLabel lbl = new JLabel(value);
        addRow(p, row, name, lbl);
    }

}
