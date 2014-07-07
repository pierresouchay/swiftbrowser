package net.souchay.swift.gui;

import java.awt.Component;
import java.security.InvalidKeyException;
import javax.swing.JOptionPane;

/**
 * 
 * @author Pierre Souchay <pierre@souchay.net> (last changed by $LastChangedBy: souchay $)
 * @version $Revision: 3830 $
 * 
 */
public class ErrorsHandler {

    /**
     * Default constructor
     */
    public ErrorsHandler() {
        // TODO Auto-generated constructor stub
    }

    public void handleInvalidKeyException(Component parent, InvalidKeyException err) {
        JOptionPane.showMessageDialog(parent, Messages.getString("cannotGenerateTemporaryURL", //$NON-NLS-1$
                                                                 err.getLocalizedMessage()));
    }

}
