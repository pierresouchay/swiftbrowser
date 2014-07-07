/**
 *
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-06-30 14:26:25 +0200 (Lun 30 jui 2014) $
 */
package net.souchay.swift.gui;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.souchay.swift.net.SwiftConnections;
import net.souchay.swift.net.SwiftTenant;

/**
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3826 $
 * 
 */
public class ContainerObject implements ContainerIFace, Serializable, FileBuilder, ObjectIFace {

    /**
     * 
     */
    private static final long serialVersionUID = -9216086458912455726L;

    private long numberOfFiles = 0;

    private long numberOfBytes = 0;

    private volatile Map<String, String> headers = new HashMap<String, String>();

    /**
     * get the readAcls
     * 
     * @return the readAcls
     */
    public String getReadAcls() {
        return readAcls;
    }

    private volatile boolean webListingEnabled = false;

    /**
     * get the webListingEnabled
     * 
     * @return the webListingEnabled
     */
    @Override
    public boolean isWebListingEnabled() {
        return webListingEnabled;
    }

    /**
     * Set the webListingEnabled
     * 
     * @param webListingEnabled the webListingEnabled to set
     */
    public void setWebListingEnabled(boolean webListingEnabled) {
        this.webListingEnabled = webListingEnabled;
    }

    private volatile List<String> readAclsList;

    /**
     * Get the Read ACL as a list of ACLs
     * 
     * @return the list of ACLs
     */
    public List<String> getReadAclsAsList() {
        return readAclsList;
    }

    /**
     * Prefix for container meta-data
     */
    public final static String X_CONTAINER_META_PREFIX = "x-container-meta-"; //$NON-NLS-1$

    /**
     * Web listings
     */
    public final static String X_CONTAINER_META_LISTINGS = "x-container-meta-web-listings"; //$NON-NLS-1$

    /**
     * Web listings
     */
    public final static String X_CONTAINER_META_LISTINGS_CSS = "x-container-meta-web-listings-css"; //$NON-NLS-1$

    /**
     * get the writeAcls
     * 
     * @return the writeAcls
     */
    public String getWriteAcls() {
        return writeAcls;
    }

    private String readAcls = ""; //$NON-NLS-1$

    private String writeAcls = ""; //$NON-NLS-1$

    @Override
    public long getNumberOfFiles() {
        return numberOfFiles;
    }

    /**
     * Set the numberOfFiles
     * 
     * @param numberOfFiles the numberOfFiles to set
     */
    public void setNumberOfFiles(long numberOfFiles) {
        this.numberOfFiles = numberOfFiles;
    }

    @Override
    public long getNumberOfBytes() {
        return numberOfBytes;
    }

    /**
     * Set the numberOfBytes
     * 
     * @param numberOfBytes the numberOfBytes to set
     */
    public void setNumberOfBytes(long numberOfBytes) {
        this.numberOfBytes = numberOfBytes;
    }

    /**
     * Constructor
     * 
     * @param executor
     * @param connection
     * @param name
     * @param fileRefresher
     */
    ContainerObject(final SwiftTenant tenant, final String name) {
        this.name = name;
        this.tenant = tenant;
    }

    private final SwiftTenant tenant;

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public final boolean equals(final Object obj) {
        if (!(obj instanceof ContainerObject))
            return false;
        final ContainerObject x = (ContainerObject) obj;
        return this.name.equals(x.getName());
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public final int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public final String getName() {
        return name;
    }

    private final String name;

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public final int compareTo(final ContainerIFace o) {
        return this.name.compareTo(o.getName());
    }

    @Override
    public String toString() {
        return this.name;
    }

    private volatile Map<String, SwiftFile> _files = new HashMap<String, SwiftFile>();

    private final WeakHashMap<String, SwiftFile> allFiles = new WeakHashMap<String, SwiftFile>();

    public FileIFace addFile(String pathFromContainer, boolean notifies) {
        SwiftFile ret = getOrCreateFile(pathFromContainer, notifies);
        return ret;
    }

    /**
     * Set all the files of container
     * 
     * @param paths
     */
    public void setFiles(Iterable<ObjectIFace> paths) {
        final Map<String, SwiftFile> oldFiles;
        final List<SwiftFile> addFiles = new LinkedList<SwiftFile>();
        synchronized (_files) {
            oldFiles = new HashMap<String, SwiftFile>(_files);
            final Map<String, SwiftFile> newFiles = new HashMap<String, SwiftFile>();
            // final LinkedList<SwiftFile> modifiedFiles = new LinkedList<SwiftFile>();

            for (ObjectIFace p : paths) {
                SwiftFile f = oldFiles.remove(p.getName());
                if (f == null) {
                    f = (SwiftFile) p;
                    addFiles.add(f);
                } else {
                    if (f.getLastModified() != p.getLastModified() || f.getSize() != p.getSize()) {
                        f.setLastModified(p.getLastModified());
                        f.setSize(p.getSize());
                        // modifiedFiles.add(f);
                    }
                }
                newFiles.put(p.getName(), f);
            }
            _files.clear();
            _files.putAll(newFiles);
        }
        if (addFiles.isEmpty() && oldFiles.isEmpty()) {
            // No change
            return;
        } else {
            List<WeakReference<FileListener>> toRemove = null;
            for (WeakReference<FileListener> liR : fileListeners) {
                final FileListener li = liR.get();
                if (li == null) {
                    if (toRemove == null) {
                        toRemove = new LinkedList<WeakReference<FileListener>>();
                    }
                    toRemove.add(liR);
                } else {
                    li.onFilesAdded(this, addFiles);
                    li.onFilesRemoved(this, oldFiles.values());
                }
            }
            if (toRemove != null) {
                fileListeners.removeAll(toRemove);
            }
        }
    }

    public void deleteFile(FileIFace removed) {
        SwiftFile f;
        synchronized (_files) {
            f = _files.remove(removed.getName());
        }
        if (f != null) {
            List<WeakReference<FileListener>> toRemove = null;
            for (WeakReference<FileListener> liR : fileListeners) {
                final FileListener li = liR.get();
                if (li == null) {
                    if (toRemove == null) {
                        toRemove = new LinkedList<WeakReference<FileListener>>();
                    }
                    toRemove.add(liR);
                } else {
                    li.onFilesRemoved(this, Collections.singletonList(f));
                }
            }
            if (toRemove != null)
                fileListeners.removeAll(toRemove);
        }
    }

    private final transient List<WeakReference<FileListener>> fileListeners = new LinkedList<WeakReference<ContainerIFace.FileListener>>();

    /**
     * @see net.souchay.swift.gui.ContainerIFace#addFileListener(net.souchay.swift.gui.ContainerIFace.FileListener)
     */
    @Override
    public void addFileListener(FileListener listener) {
        fileListeners.add(new WeakReference<ContainerIFace.FileListener>(listener));
    }

    /**
     * @see net.souchay.swift.gui.ContainerIFace#removeFileListener(net.souchay.swift.gui.ContainerIFace.FileListener)
     */
    @Override
    public void removeFileListener(FileListener listener) {
        List<WeakReference<FileListener>> toRemove = null;
        for (WeakReference<FileListener> liR : fileListeners) {
            final FileListener li = liR.get();
            if (li == listener) {
                if (toRemove == null) {
                    toRemove = new LinkedList<WeakReference<FileListener>>();
                }
                toRemove.add(liR);
            }
        }
        if (toRemove != null)
            fileListeners.removeAll(toRemove);
    }

    /**
     * @see net.souchay.swift.gui.ContainerIFace#getFiles()
     */
    @Override
    public Collection<? extends FileIFace> getFiles() {
        List<FileIFace> ret;
        synchronized (_files) {
            ret = new ArrayList<ContainerIFace.FileIFace>(_files.values());
        }
        return ret;
    }

    @Override
    public String getUrl() {
        return tenant.getPublicUrl() + SwiftConnections.URL_PATH_SEPARATOR
               + SwiftConnections.escapeURIPathParam(getName());
    }

    /**
     * Get the tenant
     * 
     * @return the tenant associated with this container
     */
    public SwiftTenant getTenant() {
        return tenant;
    }

    private static long doParseHeaders(Map<String, String> headers, String prop) {
        String v = headers.get(prop);
        try {
            return Long.parseLong(v);
        } catch (NumberFormatException err) {
            Logger.getLogger("ContainerObject").log(Level.WARNING, "cannot parse " + prop + ": " + v, err); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
            return 0;
        }
    }

    /**
     * Property ACL Container
     */
    public final static String PROPERTY_CONTAINER_READ_ACL = "x-container-read"; //$NON-NLS-1$

    /**
     * Property ACL Container
     */
    public final static String PROPERTY_CONTAINER_WRITE_ACL = "x-container-write"; //$NON-NLS-1$

    /**
     * Set the readAcls
     * 
     * @param newValue the readAcls to set
     */
    public boolean setReadAcls(String newValue) {
        if (newValue == null)
            newValue = ""; //$NON-NLS-1$
        if (newValue.equals(this.readAcls)) {
            return false;
        }
        this.readAcls = newValue;
        readAclsList = ACLParser.parseAcls(newValue);
        readAcls = newValue;
        shared = readAclsList.contains(ACLParser.R_FOR_ALL);
        return true;
    }

    /**
     * Set the writeAcls
     * 
     * @param newValue the writeAcls to set
     */
    public boolean setWriteAcls(String newValue) {
        if (newValue == null)
            newValue = ""; //$NON-NLS-1$
        if (newValue.equals(this.writeAcls)) {
            return false;
        }
        this.writeAcls = newValue;
        return true;
    }

    @Override
    public void setHeaders(final Map<String, List<String>> _headers) {
        final Map<String, String> headers = new HashMap<String, String>();
        for (Map.Entry<String, List<String>> en : _headers.entrySet()) {
            String key = en.getKey();
            if (key == null)
                continue;
            key = key.trim().toLowerCase(Locale.ENGLISH);
            List<String> value = en.getValue();
            if (value == null || value.isEmpty())
                continue;
            headers.put(key, value.get(0));
        }

        long nv = doParseHeaders(headers, "x-container-object-count"); //$NON-NLS-1$
        this.numberOfFiles = nv;
        nv = doParseHeaders(headers, "x-container-bytes-used"); //$NON-NLS-1$
        this.numberOfBytes = nv;
        {
            String v = headers.get(PROPERTY_CONTAINER_READ_ACL);
            String newValue;
            if (v == null || v.isEmpty())
                newValue = ""; //$NON-NLS-1$
            else
                newValue = v;
            setReadAcls(newValue);
        }
        this.webListingEnabled = Boolean.parseBoolean(headers.get(X_CONTAINER_META_LISTINGS));
        {
            String v = headers.get(PROPERTY_CONTAINER_WRITE_ACL);
            String newValue;
            if (v == null || v.isEmpty())
                newValue = ""; //$NON-NLS-1$
            else
                newValue = v;
            setWriteAcls(newValue);
        }

        this.headers = headers;
        List<WeakReference<FileListener>> toRemove = null;
        for (WeakReference<FileListener> liR : fileListeners) {
            final FileListener li = liR.get();
            if (li == null) {
                if (toRemove == null) {
                    toRemove = new LinkedList<WeakReference<FileListener>>();
                }
                toRemove.add(liR);
            } else {
                li.onContainerMetaDataChanged(this);
            }
        }
        if (toRemove != null)
            fileListeners.removeAll(toRemove);
    }

    private boolean shared;

    @Override
    public boolean isShared() {
        return shared;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Get or create a file in this container
     * 
     * @param pathFromContainer
     * @return
     */
    public final SwiftFile getOrCreateFile(final String pathFromContainer, boolean notifies) {
        SwiftFile f;
        synchronized (allFiles) {
            f = allFiles.get(pathFromContainer);
            if (f == null) {
                f = new SwiftFile(this, pathFromContainer);
                allFiles.put(pathFromContainer, f);
            }
        }
        if (notifies) {
            for (WeakReference<FileListener> liR : fileListeners) {
                FileListener li = liR.get();
                if (li != null)
                    li.onFilesAdded(this, Collections.singleton(f));
            }
        }
        return f;
    }

    /**
     * @see net.souchay.swift.gui.ContainerIFace#setLastModified(long)
     */
    @Override
    public boolean setLastModified(long lastModified) {
        if (this.lastModified == lastModified)
            return false;
        this.lastModified = lastModified;
        return true;
    }

    private volatile long lastModified;

    /**
     * @see net.souchay.swift.gui.ContainerIFace#getLastModified()
     */
    @Override
    public long getLastModified() {
        return this.lastModified;
    }

    @Override
    public SwiftFile createFile(String name) {
        return getOrCreateFile(name, false);
    }

    @Override
    public boolean setSize(long size) {
        if (this.size == size)
            return false;
        this.size = size;
        return true;
    }

    @Override
    public void setContentType(String contentType) {
    }

    @Override
    public boolean setMetaData(Map<String, String> newMeta) {
        return false;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public String getRelativePathFromRootEscapedAsURI() {
        return SwiftConnections.escapeURIPathParam(getName());
    }

    private long size;
}
