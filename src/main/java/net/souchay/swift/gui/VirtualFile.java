/**
 *
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-07-28 10:36:08 +0200 (Lun 28 jul 2014) $
 */
package net.souchay.swift.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.tree.TreePath;
import net.souchay.swift.gui.ContainerIFace.FileIFace;
import net.souchay.swift.net.FsConnection;

/**
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3841 $
 * 
 */
public class VirtualFile implements Comparable<VirtualFile>, ElementChangedListener<FileIFace>, Serializable {

    @Override
    public boolean equals(Object other) {
        if (other != null && (other instanceof VirtualFile)) {
            VirtualFile f = (VirtualFile) other;
            return getUnixPathFromSwiftRoot().equals(f.getUnixPathFromSwiftRoot());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * Returns whether element is a directory
     * 
     * @return true is directory like element
     */
    public boolean isDirectory() {
        return getFile() == null
               || (VirtualFile.INODE_DIRECTORY_MIME_TYPE.equals(getFile().getContentType()) && getFile().getSize() < 1);
    }

    /**
     * Mime-Type for directories in Swift
     */
    public final static String INODE_DIRECTORY_MIME_TYPE = "inode/directory"; //$NON-NLS-1$

    /**
     * 
     */
    private static final long serialVersionUID = -2836139545613102708L;

    /**
     * Constructor
     * 
     * @param parent
     * @param name
     * @param file
     */
    public VirtualFile(ElementChangedSupport<VirtualFile> virtualFileNotifier, final VirtualFile parent,
            final String name, final FileIFace file) {
        this.name = name;
        this._parent = new WeakReference<VirtualFile>(parent);
        this.file = file;
        if (parent == null) {
            path = new TreePath(this);
        } else {
            path = parent.getPath().pathByAddingChild(this);
            parent.addChild(this);
        }
        if (file != null) {
            file.addElementChangedListener(this);
        }
    }

    /**
     * Cleanup
     */
    public void delete() {
        if (file != null)
            file.removeElementChangedListener(this);
        children.clear();
        rebuildCacheLater(true, false);
    }

    /**
     * Get the size of file
     * 
     * @return the size in bytes
     */
    public long getSize() {
        return file == null ? 0 : file.getSize();
    }

    /**
     * @return the last modified
     */
    public final long getLastModified() {
        return file == null ? 0 : file.getLastModified();
    }

    /**
     * get the file
     * 
     * @return the file
     */
    public final FileIFace getFile() {
        return file;
    }

    private FileIFace file;

    private final String name;

    private final TreePath path;

    private transient WeakReference<List<VirtualFile>> _cache = new WeakReference<List<VirtualFile>>(null);

    /**
     * get the path
     * 
     * @return the path
     */
    public final TreePath getPath() {
        return path;
    }

    private final transient WeakReference<VirtualFile> _parent;

    private final Set<VirtualFile> children = Collections.synchronizedSet(new TreeSet<VirtualFile>());

    private final List<VirtualFile> getCache() {
        List<VirtualFile> c = _cache.get();
        if (c == null) {
            c = new ArrayList<VirtualFile>(children);
            this._cache = new WeakReference<List<VirtualFile>>(c);
        }
        return c;
    }

    /**
     * Get the child at given position
     * 
     * @param index The index
     * @return The child if any
     */
    public final VirtualFile getChildAt(int index) {
        return getCache().get(index);
    }

    /**
     * Get the Child count
     * 
     * @return The number of children
     */
    public final int getChildCount() {
        return getCache().size();
    }

    /**
     * Get the index of given child
     * 
     * @param child the element to search for
     * @return -1 if not found, the position otherwise
     */
    public final int getIndexOfChild(VirtualFile child) {
        return getCache().indexOf(child);
    }

    /**
     * get the name
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the parent node
     * 
     * @return null if root
     */
    public VirtualFile getParent() {
        return _parent.get();
    }

    /**
     * get the children
     * 
     * @return the children
     */
    public Collection<VirtualFile> getChildren() {
        return children;
    }

    private volatile boolean rebuildingCache = false;

    /**
     * Returns true if currently rebuilding the cache
     * 
     * @return true if rebuilding the cache...
     */
    public boolean isRebuildingCache() {
        return rebuildingCache;
    }

    private final void rebuildCacheLater(boolean canPostPone, final boolean notify) {
        if (rebuildingCache)
            return;
        rebuildingCache = true;
        final Runnable r = new Runnable() {

            @Override
            public void run() {
                _cache.clear();
                rebuildingCache = false;
                if (notify)
                    virtualFileNotifier.fireElementChanged(VirtualFile.this);
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            if (canPostPone) {
                Timer tx = new Timer(150, new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        r.run();
                    }
                });
                tx.setRepeats(false);
                tx.start();
            } else {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {

                        @Override
                        public void run() {
                            r.run();
                        }
                    });
                } catch (InvocationTargetException e) {
                    // OOPS, this is really bad, we are probably not thread-safe
                    // e.printStackTrace();
                    r.run();
                } catch (InterruptedException err) {
                    err.printStackTrace();
                }
            }
        }

    }

    /**
     * Returns true if child has been removed
     * 
     * @param f the child to remove
     * @return true if removed, false if ignored
     */
    public boolean removeChild(VirtualFile f) {
        final boolean b = children.remove(f);
        if (b) {
            rebuildCacheLater(true, true);
        }
        return b;
    }

    /**
     * Adds a Child
     * 
     * @param f the file to add
     * @return true if added
     */
    final boolean addChild(final VirtualFile f) {
        final boolean b = children.add(f);
        if (b)
            rebuildCacheLater(false, true);
        return b;
    }

    /**
     * get the virtualfilenotifier
     * 
     * @return the virtualfilenotifier
     */
    public ElementChangedListenerRegistration<VirtualFile> getVirtualfilenotifier() {
        return virtualFileNotifier;
    }

    /**
     * Tells whether this is a leaf
     * 
     * @return true if a leaf
     */
    public boolean isLeaf() {
        return children.isEmpty() && getFile() != null;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(VirtualFile o) {
        return name.compareTo(o.name);
    }

    /**
     * @see net.souchay.swift.gui.ElementChangedListener#onElementChanged(java.lang.Object)
     */
    @Override
    public void onElementChanged(FileIFace source) {
        if (getFile() == source) {
            rebuildCacheLater(true, true);
        }
    }

    /**
     * The Virtual File Separator aka /
     */
    public static final String VIRTUAL_FILE_SEPARATOR = FsConnection.URL_PATH_SEPARATOR;

    /**
     * Get the Unix path starting at container
     * 
     * @return the Unix-like path
     */
    public String getUnixPathFromSwiftRoot() {
        final VirtualFile parent = _parent.get();
        if (parent == null)
            return getName();
        else
            return parent.getUnixPathFromSwiftRoot() + VIRTUAL_FILE_SEPARATOR + getName();
    }

    /**
     * Get the Unix path starting at container
     * 
     * @return the Unix-like path
     */
    public String getUnixPathWithContainer() {
        final VirtualFile parent = _parent.get();
        if (parent == null || parent.getParent() == null)
            return getName();
        else
            return parent.getUnixPathWithContainer() + VIRTUAL_FILE_SEPARATOR + getName();
    }

    /**
     * Get the Unix path starting at container
     * 
     * @return the Unix-like path
     */
    public List<String> getUnixPathWithoutContainerAsAList() {
        final VirtualFile parent = _parent.get();
        if (parent == null || parent.getParent() == null)
            return new LinkedList<String>();
        else {
            final List<String> p = parent.getUnixPathWithoutContainerAsAList();
            p.add(getName());
            return p;
        }
    }

    /**
     * Get the Unix path starting at container
     * 
     * @return the Unix-like path
     */
    public String getUnixPathWithoutContainer() {
        final VirtualFile parent = _parent.get();
        if (parent == null || parent.getParent() == null)
            return ""; //$NON-NLS-1$
        else {
            final String p = parent.getUnixPathWithoutContainer();
            if (p.isEmpty())
                return getName();
            else
                return p + VIRTUAL_FILE_SEPARATOR + getName();
        }
    }

    /**
     * Get the Unix path starting at container
     * 
     * @return the Unix-like path
     */
    public String getContainerName() {
        final VirtualFile parent = _parent.get();
        if (parent == null)
            return null;
        return parent.getContainerName();
    }

    /**
     * Get the Unix path starting at container
     * 
     * @return the Unix-like path
     */
    public ContainerIFace getContainer() {
        final VirtualFile parent = _parent.get();
        if (parent == null)
            return null;
        return parent.getContainer();
    }

    /**
     * Get all the children that are actually files
     * 
     * @param files The files collection
     * @return the files collection
     */
    public Set<VirtualFile> getAllFileChildren(Set<VirtualFile> files) {
        FileIFace ifce = getFile();
        if (ifce != null) {
            files.add(this);
            if (ifce.isLargeObject())
                return files;
        }
        final Collection<VirtualFile> children = getChildren();
        if (children != null) {
            for (VirtualFile fx : children) {
                fx.getAllFileChildren(files);
            }
        }
        return files;
    }

    /**
     * Set the file
     * 
     * @param newFile
     */
    public void setFile(FileIFace newFile) {
        if (this.file == newFile)
            return;
        if (this.file != null)
            this.file.removeElementChangedListener(this);
        this.file = newFile;
        rebuildCacheLater(true, true);
    }

    private final transient ElementChangedSupport<VirtualFile> virtualFileNotifier = new ElementChangedSupport<VirtualFile>();
}
