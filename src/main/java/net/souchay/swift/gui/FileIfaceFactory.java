package net.souchay.swift.gui;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.souchay.swift.net.SwiftTenant;

public class FileIfaceFactory implements FileBuilder {

    public FileIfaceFactory(SwiftTenant tenant) {
        this.tenant = tenant;
    }

    private final SwiftTenant tenant;

    private final WeakHashMap<String, ContainerObject> containers = new WeakHashMap<String, ContainerObject>();

    public ContainerObject getContainer(final String name) {
        ContainerObject container;
        synchronized (containers) {
            container = containers.get(name);
            if (container == null) {
                container = new ContainerObject(tenant, name);
                containers.put(name, container);
            }
        }
        return container;
    }

    /**
     * Factory method
     * 
     * @param container
     * @param pathFromContainer
     * @return
     */
    public static final SwiftFile getFile(final ContainerIFace container, final String pathFromContainer) {
        return ((ContainerObject) container).getOrCreateFile(pathFromContainer, false);
    }

    @Override
    public ObjectIFace createFile(String name) {
        return getContainer(name);
    }

    @Override
    public String getRelativePathFromRootEscapedAsURI() {
        throw new RuntimeException(NOT_IMPLEMENTED);
    }

    @Override
    public void setHeaders(Map<String, List<String>> headers) {
        throw new RuntimeException(NOT_IMPLEMENTED);
    }

    private final static String NOT_IMPLEMENTED = "NotImplementedException"; //$NON-NLS-1$

}
