/**
 *
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-05-06 09:29:22 +0200 (Mar 06 mai 2014) $
 */
package net.souchay.swift.gui;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.Future;

/**
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3798 $
 * 
 */
public class FileImport {

    /**
     * Being notified when a file or URL is dropped
     * 
     * @copyright Pierre Souchay - 2013,2014
     * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
     * @version $Revision: 3798 $
     * 
     */
    public static interface FileOrURLListener {

        /**
         * An URL has been dropped
         * 
         * @param url the URL dropped
         */
        public Future<ContainerIFace> addUrl(VirtualFile f, URL url);

        /**
         * A file has been dropped
         * 
         * @param file The file dropped
         */
        public Future<ContainerIFace> addFile(VirtualFile f, File file);

        /**
         * Copy a virtual file
         * 
         * @param file the file to copy
         * @param additionalBasePath The base path to use for copy
         */
        public Future<VirtualFile> copyVirtualFile(VirtualFile f, String additionalBasePath, String file);

        /**
         * Deletes the following files
         * 
         * @param files the files to delete
         */
        public Future<Boolean> deleteVirtualFiles(Collection<VirtualFile> files);
    }
}