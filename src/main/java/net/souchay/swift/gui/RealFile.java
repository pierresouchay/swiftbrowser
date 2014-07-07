package net.souchay.swift.gui;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.souchay.swift.gui.ContainerIFace.FileIFace;

public class RealFile implements FileIFace {

    private final File f;

    private final ContainerIFace container;

    public RealFile(ContainerIFace container, File f) {
        this.f = f;
        this.container = container;
    }

    @Override
    public void addElementChangedListener(ElementChangedListener<FileIFace> listener) {
    }

    @Override
    public void removeElementChangedListener(ElementChangedListener<FileIFace> listener) {
    }

    @Override
    public String getName() {
        return f.getName();
    }

    @Override
    public Map<String, String> getMetaData() {
        return Collections.emptyMap();
    }

    @Override
    public long getSize() {
        return f.length();
    }

    @Override
    public long getLastModified() {
        return f.lastModified();
    }

    @Override
    public ContainerIFace getContainer() {
        return container;
    }

    @Override
    public boolean isLargeObject() {
        return false;
    }

    @Override
    public String getContentType() {
        return "octet/stream"; //$NON-NLS-1$
    }

    @Override
    public void setHeaders(Map<String, List<String>> newHeaders) {
    }

    @Override
    public boolean setLastModified(long lastModified) {
        return f.setLastModified(lastModified);
    }

    @Override
    public boolean setSize(long size) {
        return false;
    }

    @Override
    public void setContentType(String contentType) {
    }

    @Override
    public boolean setMetaData(Map<String, String> newMeta) {
        return false;
    }
}
