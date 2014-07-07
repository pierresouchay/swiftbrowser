/**
 *
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-06-18 01:40:28 +0200 (Mer 18 jui 2014) $
 */
package net.souchay.swift.gui;

import java.lang.ref.WeakReference;
import javax.swing.event.DocumentEvent.ElementChange;
import net.souchay.swift.gui.ElementChangedListener.ElementChangedListenerRegistration;

/**
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3822 $
 * @param <E>
 * 
 */
public class ElementChangedSupport<E> implements ElementChangedListenerRegistration<E> {

    /**
     * Constructor
     */
    public ElementChangedSupport() {
    }

    /**
     * @see net.souchay.swift.gui.ElementChangedListener.ElementChangedListenerRegistration#addElementChangedListener(net.souchay.swift.gui.ElementChangedListener)
     */
    @Override
    public void addElementChangedListener(ElementChangedListener<E> listener) {
        WeakRef<E> x = head;
        if (x == null)
            head = new WeakRef<E>(listener);
        else {
            while (x.next != null) {
                x = x.next;
            }
            x.next = new WeakRef<E>(listener);
        }
    }

    /**
     * @see net.souchay.swift.gui.ElementChangedListener.ElementChangedListenerRegistration#removeElementChangedListener(net.souchay.swift.gui.ElementChangedListener)
     */
    @Override
    public void removeElementChangedListener(ElementChangedListener<E> listener) {
        WeakRef<E> prev = null;
        WeakRef<E> first = head;
        while (first != null) {
            ElementChangedListener<E> e = first.ref.get();
            if (e == listener) {
                if (prev == null) {
                    head = first.next;
                } else {
                    prev.next = first.next;
                }
            }
            prev = first;
            first = prev.next;
        }
    }

    /**
     * Support for firing {@link ElementChange}
     * 
     * @param source
     */
    public void fireElementChanged(E source) {
        WeakRef<E> prev = null;
        WeakRef<E> first = head;
        while (first != null) {
            ElementChangedListener<E> e = first.ref.get();
            if (e == null) {
                if (prev == null) {
                    head = first.next;
                } else {
                    prev.next = first.next;
                }
            } else {
                e.onElementChanged(source);
            }
            prev = first;
            first = prev.next;
        }
    }

    private volatile WeakRef<E> head;

    private static final class WeakRef<E> extends Object {

        private WeakRef<E> next;

        private final WeakReference<ElementChangedListener<E>> ref;

        /**
         * Constructor
         * 
         * @param x
         */
        public WeakRef(final ElementChangedListener<E> x) {
            this.ref = new WeakReference<ElementChangedListener<E>>(x);
        }
    }

    /**
     * Return true if no listener has been registered
     * 
     * @return
     */
    public final boolean isEmpty() {
        return head == null;
    }
}
