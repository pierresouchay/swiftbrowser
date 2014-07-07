package net.souchay.swift.gui.dnd;

import java.awt.datatransfer.DataFlavor;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Constants for Drag And Drop
 * 
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3830 $
 * 
 */
public class Constants {

    public final static DataFlavor uriList;

    public static final DataFlavor urlFlavor;

    public static final DataFlavor virtualFilesList;
    static {
        virtualFilesList = new DataFlavor(TenantDragAndDrop.class, "Swift Virtual Files"); //$NON-NLS-1$
        try {
            urlFlavor = new DataFlavor("application/x-java-url;class=java.net.URL"); //$NON-NLS-1$
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        try {
            uriList = new DataFlavor("text/uri-list;class=java.lang.String"); //$NON-NLS-1$
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }

    }

    public final static DataFlavor exportableDataFlavors[] = { virtualFilesList, uriList,
    /* DataFlavor.javaFileListFlavor, */urlFlavor };

    public final static Set<DataFlavor> importableDataFlavors = new HashSet<DataFlavor>();

    static {
        importableDataFlavors.add(DataFlavor.javaFileListFlavor);
        importableDataFlavors.add(DataFlavor.stringFlavor);
        importableDataFlavors.add(uriList);
        importableDataFlavors.add(virtualFilesList);
        importableDataFlavors.add(urlFlavor);
    }

    /**
     * Returns the first dataflavor that matches
     * 
     * @param flavors
     * @return
     */
    public static DataFlavor supportsDataFlavor(DataFlavor... flavors) {
        for (DataFlavor f : flavors) {
            if (importableDataFlavors.contains(f))
                return f;
        }
        return null;
    }

    /**
     * Returns the first dataflavor that matches
     * 
     * @param flavors
     * @return
     */
    public static DataFlavor supportsDataFlavor(Collection<DataFlavor> flavors) {
        for (DataFlavor f : flavors) {
            if (importableDataFlavors.contains(f))
                return f;
        }
        return null;
    }

}
