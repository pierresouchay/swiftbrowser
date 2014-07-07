package net.souchay.swift.gui;

public class ContainerVirtualFile extends VirtualFile {

    /**
     * 
     */
    private static final long serialVersionUID = 7459587674463681777L;

    /**
     * The container Constructor
     * 
     * @param parent
     * @param name
     */
    public ContainerVirtualFile(ElementChangedSupport<VirtualFile> notifier, VirtualFile parent,
            ContainerIFace container) {
        super(notifier, parent, container.getName(), null);
        this.container = container;
    }

    private final ContainerIFace container;

    /**
     * @see net.souchay.swift.gui.VirtualFile#getSize()
     */
    @Override
    public final long getSize() {
        return getContainer().getNumberOfBytes();
    }

    /**
     * get the container
     * 
     * @return the container
     */
    @Override
    public final ContainerIFace getContainer() {
        return container;
    }

    @Override
    public final String getContainerName() {
        return getContainer().getName();
    }

}
