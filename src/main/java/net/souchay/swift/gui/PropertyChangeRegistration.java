package net.souchay.swift.gui;

import java.beans.PropertyChangeListener;

/**
 * 
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3830 $
 * 
 */
public interface PropertyChangeRegistration {

    /**
     * Adds Property Change Listener
     * 
     * @param listener the listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * The listener to remove
     * 
     * @param listener the listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);

}
