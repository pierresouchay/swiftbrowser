package net.souchay.swift.gui;

import javax.swing.AbstractAction;

/**
 * A virtual file action
 * 
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3830 $
 * 
 */
public abstract class VirtualFileAction extends AbstractAction {

    /**
     * 
     */
    private static final long serialVersionUID = 2883442212389484704L;

    /**
     * Computes whether action should be enabled when selection changes
     * 
     * @param selectedPaths
     */
    public abstract void enable(VirtualFile... selectedPaths);

}
