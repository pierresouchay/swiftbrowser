/**
 *
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-05-05 13:18:33 +0200 (Lun 05 mai 2014) $
 */
package net.souchay.swift.gui;

/**
 * Notitications of changes
 * 
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3795 $
 * @param <X>
 * 
 */
public interface ElementChangedListener<X> {

    /**
     * the element has been changed
     * 
     * @param source
     */
    void onElementChanged(X source);

    /**
     * 
     * @copyright Pierre Souchay - 2013,2014
     * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
     * @version $Revision: 3795 $
     * 
     * @param <W>
     */
    public static interface ElementChangedListenerRegistration<W> {

        /**
         * 
         * @param listener
         */
        void addElementChangedListener(ElementChangedListener<W> listener);

        /**
         * 
         * @param listener
         */
        void removeElementChangedListener(ElementChangedListener<W> listener);
    }

}
