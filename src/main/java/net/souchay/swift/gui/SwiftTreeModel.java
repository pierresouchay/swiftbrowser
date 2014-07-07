/**
 *
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-07-07 12:12:06 +0200 (Lun 07 jul 2014) $
 */
package net.souchay.swift.gui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

/**
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3835 $
 * 
 */
public class SwiftTreeModel extends AbstractTreeTableModel {

    /**
     * @see org.jdesktop.swingx.treetable.AbstractTreeTableModel#getRoot()
     */
    @Override
    public VirtualFile getRoot() {
        return (VirtualFile) super.getRoot();
    }

    /**
     * Constructor
     * 
     * @param tenant The tenant name
     */
    public SwiftTreeModel(String tenant) {
        super(new VirtualFile(new ElementChangedSupport<VirtualFile>(),
                              null,
                              Messages.getString("SwiftTreeModel.rootContainerName", tenant), null)); //$NON-NLS-1$
    }

    /**
     * Updates the root (aka the containers)
     * 
     * @param f The Virtual file to add
     */
    public void updateRoot(final VirtualFile f) {
        final Runnable r = new Runnable() {

            @Override
            public void run() {
                modelSupport.fireChildAdded(getRoot().getPath(), getRoot().getIndexOfChild(f), f);
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(r);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Updates the given Virtuakl File
     * 
     * @param f The file to update
     */
    public void updateVirtualFile(final VirtualFile f) {
        if (f == null)
            return;
        final VirtualFile parent = f.getParent();
        if (parent == null)
            return;
        if (SwingUtilities.isEventDispatchThread())
            modelSupport.fireChildChanged(f.getParent().getPath(), getIndexOfChild(parent, f), f);
        else {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    try {

                        modelSupport.fireChildChanged(parent.getPath(), getIndexOfChild(parent, f), f);
                    } catch (ArrayIndexOutOfBoundsException err) {
                        if (parent.isRebuildingCache()) {
                            // NOOP
                            System.err.println("Try to refresh too early for: " + parent.getUnixPathWithContainer()); //$NON-NLS-1$
                        } else {
                            System.err.println(f.getUnixPathWithContainer() + ":" + f.isRebuildingCache() //$NON-NLS-1$
                                               + " ; parent=" + parent.getUnixPathWithContainer() + " err:" //$NON-NLS-1$//$NON-NLS-2$
                                               + err.getMessage());
                            SwingUtilities.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    modelSupport.fireNewRoot();
                                }
                            });
                        }
                    }
                }
            });
        }
    }

    /**
     * Clear the model
     */
    public void clear() {
        if (root != null) {
            VirtualFile f = (VirtualFile) root;
            while (f.getChildCount() > 0) {
                f.removeChild(f.getChildAt(0));
            }
        }
        root = null;
    }

    public void removeItems(Collection<VirtualFile> items) {
        for (final VirtualFile f : items) {
            final VirtualFile parent = f.getParent();
            final TreePath pPath;
            if (parent == null) {
                Runnable r = new Runnable() {

                    @Override
                    public void run() {
                        modelSupport.fireNewRoot();
                    }

                };
                if (SwingUtilities.isEventDispatchThread())
                    r.run();
                else {
                    SwingUtilities.invokeLater(r);
                }
            } else {
                pPath = parent.getPath();
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        int idx = parent.getIndexOfChild(f);
                        if (parent.removeChild(f))
                            if (idx >= 0) {
                                modelSupport.fireChildRemoved(pPath, idx, f);
                            }
                    }
                });
            }

        }
    }

    private void addBigNumberOfItems(Collection<VirtualFile> items, final boolean isEDT) {
        if (items.isEmpty())
            return;
        LinkedList<VirtualFile> ancestors = null;
        for (VirtualFile item : items) {
            if (ancestors == null) {
                ancestors = new LinkedList<VirtualFile>();
                VirtualFile parent = item;
                while (parent != null) {
                    ancestors.add(parent); // adds the root at end
                    parent = parent.getParent();
                }
            } else {
                VirtualFile parent = item;
                VirtualFile commonParent = null;
                while (parent != null) {
                    int idx = -1;
                    if ((idx = ancestors.indexOf(parent)) > -1) {
                        // We found it, we remove the not-common ancestors
                        for (int i = 0; i < idx; i++) {
                            ancestors.remove(0);
                        }
                        commonParent = parent;
                        break;
                    } else {
                        // We go to next one...
                        parent = parent.getParent();
                    }
                }
                if (commonParent == null) {
                    // System.err.println("No common parent");
                    ancestors.clear();
                }
            }
        }
        if (ancestors == null || ancestors.isEmpty()) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    modelSupport.fireTreeStructureChanged(null);
                }
            });
        } else {
            final VirtualFile theCommonParent = ancestors.get(0);
            if (isEDT) {
                modelSupport.fireTreeStructureChanged(theCommonParent.getPath());
                // We can update now !
            } else {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        modelSupport.fireTreeStructureChanged(theCommonParent.getPath());
                    }
                });
            }
        }

    }

    /**
     * @param items
     */
    public void addItems(Collection<VirtualFile> items, final boolean isEDT) {
        if (items.size() > 100) {
            addBigNumberOfItems(items, isEDT);
            return;
        }
        final List<Runnable> toRun = isEDT ? Collections.<Runnable> emptyList() : new ArrayList<Runnable>(items.size());
        for (final VirtualFile f : items) {
            final VirtualFile parent = f.getParent();
            final TreePath pPath;
            if (parent == null) {
                final Runnable r = new Runnable() {

                    @Override
                    public void run() {
                        modelSupport.fireNewRoot();
                    }
                };
                if (isEDT)
                    r.run();
                else {
                    try {
                        SwingUtilities.invokeAndWait(r);
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                pPath = parent.getPath();
                final Runnable r = new Runnable() {

                    @Override
                    public void run() {
                        final int idx = parent.getIndexOfChild(f);
                        modelSupport.fireChildAdded(pPath, idx, f);
                    }
                };
                if (isEDT)
                    r.run();
                else {
                    toRun.add(r);
                }
            }
        }
        if (!toRun.isEmpty()) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    for (Runnable r : toRun)
                        r.run();
                }

            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getColumnClass(int column) {
        switch (column) {
            case 0:
                return VirtualFile.class;
            case 1:
                return Long.class;
            case 2:
                return Date.class;
            default:
                return super.getColumnClass(column);
        }
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return Messages.getString("SwiftTreeModel.name"); //$NON-NLS-1$
            case 1:
                return Messages.getString("SwiftTreeModel.size"); //$NON-NLS-1$
            case 2:
                return Messages.getString("SwiftTreeModel.lastModified"); //$NON-NLS-1$
            default:
                return super.getColumnName(column);
        }
    }

    /**
     * @see org.jdesktop.swingx.treetable.TreeTableModel#getValueAt(java.lang.Object, int)
     */
    @Override
    public Object getValueAt(Object node, int column) {
        VirtualFile f = (VirtualFile) node;
        switch (column) {
            case 0:
                return f;
            case 1:
                final long sz = f.getSize();
                if (sz < 1)
                    return null;
                return sz;
            case 2:
                long lm = f.getLastModified();
                if (lm < 1)
                    return null;
                return lm;
                // return f.getLastModified();
            default:
                return null;
        }
    }

    /**
     * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
     */
    @Override
    public VirtualFile getChild(Object parent, int index) {
        return ((VirtualFile) parent).getChildAt(index);
    }

    /**
     * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
     */
    @Override
    public int getChildCount(Object parent) {
        return ((VirtualFile) parent).getChildCount();
    }

    /**
     * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
     */
    @Override
    public int getIndexOfChild(Object parent, Object child) {
        final VirtualFile p = (VirtualFile) parent;
        final VirtualFile c = (VirtualFile) child;
        return p.getIndexOfChild(c);
    }
}
