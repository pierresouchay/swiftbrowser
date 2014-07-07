package net.souchay.swift.gui;

/**
 * File size calculation based on IS notation (base 10)
 * 
 * @author Pierre Souchay <pierre@souchay.net> (last changed by $LastChangedBy: souchay $)
 * @version $Revision: 3830 $
 * 
 */
public class FileSize {

    /**
     * Get file size as Human readable values
     * 
     * @param value the size to display
     * @return the size in human readable format
     */
    public final static Object[] valueAsKMG(final long value) {
        if (value < 1000) {
            return new Object[] { value, "" }; //$NON-NLS-1$
        } else if (value < 1000000) {
            return new Object[] { value / 1000f, "K" }; //$NON-NLS-1$
        } else if (value < 1000000000) {
            return new Object[] { value / 1000000f, "M" }; //$NON-NLS-1$
        } else {
            return new Object[] { value / 1000000000f, "G" }; //$NON-NLS-1$
        }
    }

}
