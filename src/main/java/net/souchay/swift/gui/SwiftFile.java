/**
 *
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-05-23 17:18:40 +0200 (Ven 23 mai 2014) $
 */
package net.souchay.swift.gui;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.souchay.swift.gui.ContainerIFace.FileIFace;
import net.souchay.swift.net.FsConnection;

/**
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3810 $
 * 
 */
public class SwiftFile implements FileIFace, Comparable<SwiftFile>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -5050982103355558168L;

    /**
     * Constructor
     * 
     * @param container
     * @param realName
     */
    SwiftFile(ContainerObject container, String realName) {
        this.container = container;
        this.name = realName;
    }

    private final ContainerObject container;

    private boolean largeObject;

    private String contentType = "octet/stream"; //$NON-NLS-1$

    /**
     * get the largeObject
     * 
     * @return the largeObject
     */
    @Override
    public boolean isLargeObject() {
        return largeObject;
    }

    private final String name;

    /**
     * get the container
     * 
     * @return the container
     */
    @Override
    public ContainerIFace getContainer() {
        return container;
    }

    /**
     * get the realName
     * 
     * @return the realName
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public final int compareTo(final SwiftFile other) {
        if (container == null) {
            if (other.container != null)
                return 1;
        } else {
            final int containerComp = container.compareTo(other.container);
            if (containerComp != 0)
                return containerComp;
        }
        return name.compareTo(other.name);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public final boolean equals(final Object obj) {
        if (!(obj instanceof SwiftFile))
            return false;
        final SwiftFile other = (SwiftFile) obj;
        return other.container.equals(container) && other.name.equals(name);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public final int hashCode() {
        return name.hashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * @see net.souchay.swift.gui.ContainerIFace.FileIFace#getMetaData()
     */
    @Override
    public Map<String, String> getMetaData() {
        return Collections.unmodifiableMap(metaData);
    }

    /**
     * Updates the meta data
     * 
     * @param newMeta
     * @return true if meta data has been modified
     */
    @Override
    public boolean setMetaData(final Map<String, String> newMeta) {
        if (metaData.size() == newMeta.size()) {
            boolean modified = false;
            for (Map.Entry<String, String> en : metaData.entrySet()) {
                String v = newMeta.get(en.getKey());
                if (!en.getValue().equals(v)) {
                    modified = true;
                    break;
                }
            }
            if (!modified)
                return false;
        }
        largeObject = newMeta.containsKey(FsConnection.X_OBJECT_MANIFEST);
        metaData.clear();
        metaData.putAll(newMeta);
        return true;
    }

    @Override
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    private final Map<String, String> metaData = new HashMap<String, String>();

    /**
     * @see net.souchay.swift.gui.ContainerIFace.FileIFace#getSize()
     */
    @Override
    public long getSize() {
        return size;
    }

    private long lastModified;

    /**
     * get the lastModified
     * 
     * @return the lastModified
     */
    @Override
    public long getLastModified() {
        return lastModified;
    }

    /**
     * Set the lastModified
     * 
     * @param lastModified the lastModified to set
     * @return true if values have been modified
     */
    @Override
    public boolean setLastModified(long lastModified) {
        if (this.lastModified == lastModified)
            return false;
        this.lastModified = lastModified;
        return true;
    }

    private long size = -1;

    @Override
    public boolean setSize(long size) {
        if (this.size == size)
            return false;
        this.size = size;
        return true;
    }

    /**
     * Fires the event modified
     */
    public void fireModified() {
        if (swiftFileModificationChangeSupport != null)
            swiftFileModificationChangeSupport.fireElementChanged(this);
    }

    /**
     * @see net.souchay.swift.gui.ElementChangedListener.ElementChangedListenerRegistration#addElementChangedListener(net.souchay.swift.gui.ElementChangedListener)
     */
    @Override
    public void addElementChangedListener(ElementChangedListener<FileIFace> listener) {
        if (swiftFileModificationChangeSupport == null)
            swiftFileModificationChangeSupport = new ElementChangedSupport<ContainerIFace.FileIFace>();
        swiftFileModificationChangeSupport.addElementChangedListener(listener);
    }

    /**
     * @see net.souchay.swift.gui.ElementChangedListener.ElementChangedListenerRegistration#removeElementChangedListener(net.souchay.swift.gui.ElementChangedListener)
     */
    @Override
    public void removeElementChangedListener(ElementChangedListener<FileIFace> listener) {
        if (swiftFileModificationChangeSupport != null) {
            swiftFileModificationChangeSupport.removeElementChangedListener(listener);
            if (swiftFileModificationChangeSupport.isEmpty()) {
                swiftFileModificationChangeSupport = null;
            }
        }
    }

    private volatile transient ElementChangedSupport<FileIFace> swiftFileModificationChangeSupport = null;

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public void setHeaders(Map<String, List<String>> newHeaders) {
        Map<String, String> vals = new HashMap<String, String>();
        for (Map.Entry<String, List<String>> en : newHeaders.entrySet()) {
            String v = ""; //$NON-NLS-1$
            for (String s : en.getValue()) {
                v += s;
            }
            final String k = en.getKey();
            if (k != null)
                vals.put(k.toLowerCase(Locale.ENGLISH).trim().intern(), v);
        }
        {
            final long sz = SwiftConnectionsDownload.parseLong(newHeaders, FsConnection.CONTENT_LENGTH);
            final long lastModified = SwiftConnectionsDownload.parseDate(newHeaders, FsConnection.LAST_MODIFIED_HEADER);
            boolean notify = setSize(sz);
            boolean notify2 = setLastModified(lastModified);
            boolean notify3 = setMetaData(vals);
            if (notify || notify2 || notify3)
                fireModified();
        }

    }

}
