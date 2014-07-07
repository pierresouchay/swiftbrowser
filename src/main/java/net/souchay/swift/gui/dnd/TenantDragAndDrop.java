package net.souchay.swift.gui.dnd;

import java.io.Serializable;
import java.util.List;
import net.souchay.swift.net.SwiftTenant;

public class TenantDragAndDrop implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -8362222040689753315L;

    /**
     * Serialization only
     */
    public TenantDragAndDrop() {

    }

    /**
     * get the tenantPublicURL
     * 
     * @return the tenantPublicURL
     */
    public String getTenantPublicURL() {
        return tenantPublicURL;
    }

    /**
     * Constructor
     * 
     * @param tenant
     * @param files
     */
    public TenantDragAndDrop(SwiftTenant tenant, List<String> files) {
        this.tenantPublicURL = tenant.getPublicUrl();
        this.files = files;
    }

    private String tenantPublicURL;

    private List<String> files;

    /**
     * get the files
     * 
     * @return the files
     */
    public List<String> getFiles() {
        return files;
    }

    /**
     * Set the files
     * 
     * @param files the files to set
     */
    public void setFiles(List<String> files) {
        this.files = files;
    }

}
