package net.souchay.swift.gui;

import java.util.List;
import java.util.Map;

/**
 * @author Pierre Souchay <pierre@souchay.net> (last changed by $LastChangedBy: souchay $)
 * @version $Revision: 3830 $
 * 
 */
public interface FileBuilder {

    /**
     * Creates a file in given Context
     * 
     * @param name the file to create
     * @return
     */
    public ObjectIFace createFile(String name);

    /**
     * Get the name from root, not including first slash
     * 
     * @return the path (may be empty for the tenant itself)
     */
    public String getRelativePathFromRootEscapedAsURI();

    /**
     * set headers
     * 
     * @param headers the headers
     */
    public void setHeaders(Map<String, List<String>> headers);
}
