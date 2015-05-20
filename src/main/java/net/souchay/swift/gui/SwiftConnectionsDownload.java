package net.souchay.swift.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import net.souchay.swift.downloads.Md5Comparator;
import net.souchay.swift.gui.ContainerIFace.FileIFace;
import net.souchay.swift.gui.FileImport.FileOrURLListener;
import net.souchay.swift.gui.table.HeaderRowRenderer;
import net.souchay.swift.net.FsConnection;
import net.souchay.swift.net.FsConnection.NoNeedToDownloadException;
import net.souchay.swift.net.HttpDateParser;
import net.souchay.swift.net.SwiftConnectionResultHandler;
import net.souchay.swift.net.SwiftConnections;
import net.souchay.utilities.InputStreamWithProgress;
import net.souchay.utilities.URLParamEncoder;
import net.souchay.utilities.URLRetriever;
import org.jdesktop.swingx.JXTreeTable;

public class SwiftConnectionsDownload implements FileOrURLListener {

    private final static Logger LOG = Logger.getLogger("swift.io"); //$NON-NLS-1$

    public SwiftConnectionsDownload(ExecutorService executor, SwiftConnectionResultHandler handler,
            SwiftConnections connection, SwiftToVirtualFiles guiBindings, JXTreeTable table, FileBuilder fileBuilder) {
        this.conn = connection;
        this.fileHandler = handler;
        this.executor = executor;
        this.guiBindings = guiBindings;
        this.table = table;
        this.fileBuilder = fileBuilder;
    }

    private final FileBuilder fileBuilder;

    private final JXTreeTable table;

    private final SwiftToVirtualFiles guiBindings;

    private final ExecutorService executor;

    private final SwiftConnections conn;

    private final MimeTypeSetter mimeResolver = new MimeTypeSetter();

    private final SwiftConnectionResultHandler fileHandler;

    /**
     * Save a Virtual File to local file system
     * 
     * @param virtualFile
     * @param toSaveAs
     * @return a future with results
     */
    public List<Future<File>> saveAs(final VirtualFile virtualFile, final File toSaveAs) {
        LinkedList<Future<File>> result = new LinkedList<Future<File>>();
        if (virtualFile.isDirectory()) {
            // Directory
            if (!toSaveAs.exists())
                toSaveAs.mkdirs();
        } else {
            final long lastModified;
            if (toSaveAs.exists()) {
                long lm = toSaveAs.lastModified();
                lastModified = lm;
            } else {
                lastModified = -1;
            }
            result.add(executor.submit(new Callable<File>() {

                @Override
                public File call() throws Exception {
                    conn.get(fileHandler,
                             virtualFile.getFile().getContainer().getName(),
                             virtualFile.getFile().getName(),
                             new FsConnection.OnFileDownloaded() {

                                 @Override
                                 public File onStartDownload(String container, String path, int totalLengh,
                                         long lastModified, String eTag) throws IOException, NoNeedToDownloadException {
                                     final Md5Comparator md5 = Md5Comparator.getInstance();
                                     try {
                                         md5.cancelIfDownloadCanBeSkipped(toSaveAs, totalLengh, eTag);
                                     } finally {
                                         md5.close();
                                     }
                                     return toSaveAs;
                                 }

                                 @Override
                                 public void onDownload(File f, String container, String path, boolean success) {
                                     if (success)
                                         toSaveAs.setLastModified(virtualFile.getLastModified());
                                     else
                                         toSaveAs.delete();
                                 }
                             }, lastModified);
                    return toSaveAs;
                }

            }));

        }
        for (VirtualFile f : virtualFile.getChildren()) {
            String name = f.getName();
            result.addAll(saveAs(f, new File(toSaveAs, name)));
        }
        return result;
    }

    @Override
    public Future<ContainerIFace> addUrl(VirtualFile node, final URL url) {
        if (url.getProtocol().startsWith("http")) { //$NON-NLS-1$
            final VirtualFile fx = node == null ? resolve() : node;
            if (fx == null) {
                LOG.warning("addUrl(): No Virtual File found for " + url.toExternalForm()); //$NON-NLS-1$
                return null;
            }
            String unixPath = fx.getUnixPathFromSwiftRoot();
            int split1 = unixPath.indexOf(VirtualFile.VIRTUAL_FILE_SEPARATOR);
            int split2 = unixPath.indexOf(VirtualFile.VIRTUAL_FILE_SEPARATOR, split1 + 1);
            final String container;
            // final String basePath;
            if (split2 < 1) {
                container = unixPath.substring(split1 + 1);
                // basePath = null;
            } else {
                container = unixPath.substring(split1 + 1, split2);
                // basePath = unixPath.substring(split2 + 1);
            }
            // final ContainerObject o = (ContainerObject) (guiBindings.getContainer(container));
            return executor.submit(new Callable<ContainerIFace>() {

                @Override
                public ContainerIFace call() throws Exception {
                    PipedInputStream theInPipe = null;
                    final PipedOutputStream out = new PipedOutputStream();
                    try {
                        theInPipe = new PipedInputStream(out, 8192 * 128);
                        final PipedInputStream pipeIn = theInPipe;
                        // out.connect(pipeIn);
                        HashMap<String, List<String>> inOutHeaders = new HashMap<String, List<String>>();
                        final AtomicLong fullSize = new AtomicLong(-1);
                        int idx = url.getPath().lastIndexOf(VirtualFile.VIRTUAL_FILE_SEPARATOR);
                        if (idx > url.getPath().length() - 3) {
                            idx = url.getPath().lastIndexOf(VirtualFile.VIRTUAL_FILE_SEPARATOR, idx);
                        }
                        final String file;
                        if (idx < 0 || idx > url.getPath().length() - 3) {
                            file = url.toExternalForm().replaceAll(VirtualFile.VIRTUAL_FILE_SEPARATOR, "_") //$NON-NLS-1$
                                      .replaceAll(":", "_") //$NON-NLS-1$//$NON-NLS-2$
                                      .replaceAll("\\?", "_"); //$NON-NLS-1$//$NON-NLS-2$
                        } else {
                            file = URLParamEncoder.decode(url.getPath().substring(idx + 1));
                        }
                        final AtomicBoolean resultProcessed = new AtomicBoolean(false);
                        final Callable<ContainerIFace> calledOnEnd = new Callable<ContainerIFace>() {

                            @Override
                            public ContainerIFace call() throws IOException {
                                try {
                                    conn.put(fileHandler,
                                             container,
                                             fullSize.longValue(),
                                             file,
                                             pipeIn,
                                             new HashMap<String, String>());
                                    return fx.getContainer();
                                } catch (IOException err) {
                                    LOG.log(Level.WARNING, "Failed to put file " + file, err); //$NON-NLS-1$
                                    throw err;
                                } finally {
                                    resultProcessed.set(true);
                                }
                            }
                        };
                        URLRetriever.packageDataFromURL(url, out, -1, "swiftBrowser", //$NON-NLS-1$
                                                        inOutHeaders,
                                                        new InputStreamWithProgress.InputStreamWithProgressListener() {

                                                            @Override
                                                            public void onClosed(InputStreamWithProgress source) {
                                                                // closed
                                                            }

                                                            private volatile boolean connected = false;

                                                            @Override
                                                            public void onBytesReaden(InputStreamWithProgress source,
                                                                    int bytesRead, final long total) {
                                                                fullSize.set(source.getFullSize());
                                                                if (!connected && fullSize.longValue() > 0) {
                                                                    connected = true;

                                                                    executor.submit(calledOnEnd);
                                                                }
                                                            }
                                                        });
                        while (!resultProcessed.get()) {
                            Thread.sleep(33);
                        }
                        return fx.getContainer();
                    } finally {
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (theInPipe != null) {
                            try {
                                theInPipe.close();
                            } catch (IOException err2) {
                                LOG.log(Level.WARNING, "Failed to close pipe after error", err2); //$NON-NLS-1$
                            }
                        }
                    }
                }
            });
        } else {
            URI uri;
            try {
                uri = url.toURI();
                try {
                    File f = new File(uri);
                    return addFile(node, f);
                } catch (IllegalArgumentException err) {
                    String newUri = uri.toString();
                    final String uriDetect = "file://localhost"; //$NON-NLS-1$
                    if (newUri != null && newUri.startsWith(uriDetect)) {
                        newUri = "file://" + newUri.substring(uriDetect.length()); //$NON-NLS-1$
                        uri = new URI(newUri);
                        File f = new File(uri);
                        return addFile(node, f);
                    }
                    throw err;
                }
            } catch (URISyntaxException e) {
                LOG.log(Level.WARNING, "cannot drop file because uri could not be resolved.", e); //$NON-NLS-1$
                return null;
            } catch (IllegalArgumentException err) {
                LOG.log(Level.WARNING, url.toExternalForm() + " : " + err.getLocalizedMessage(), err); //$NON-NLS-1$
                return null;
            }
        }
    }

    private void putFileRec(File f, ContainerIFace container, String basePath) {
        final String remoteFName;
        if (basePath == null)
            remoteFName = f.getName();
        else
            remoteFName = basePath
                          + (basePath.endsWith(VirtualFile.VIRTUAL_FILE_SEPARATOR) ? "" : VirtualFile.VIRTUAL_FILE_SEPARATOR) //$NON-NLS-1$
                          + f.getName();
        if (f.isDirectory()) {
            String childs[] = f.list();
            for (String s : childs) {
                putFileRec(new File(f, s), container, remoteFName);
            }
        } else if (f.isFile()) {
            FileInputStream in = null;
            Map<String, String> headers = new HashMap<String, String>();
            VirtualFile vf = guiBindings.findVirtualFile(container, remoteFName);
            if (vf != null) {
                FileIFace iface = vf.getFile();
                if (iface != null) {
                    headers.putAll(iface.getMetaData());
                    headers.remove(FsConnection.CONTENT_LENGTH);
                    headers.remove(FsConnection.ETAG);
                    headers.remove(FsConnection.LAST_MODIFIED_HEADER);
                    headers.remove(FsConnection.DATE);
                    headers.remove(FsConnection.X_OBJECT_MANIFEST);
                    for (String s : HeaderRowRenderer.BLACKLISTED_HEADERS_FOR_FILES) {
                        headers.remove(s);
                    }
                }
            }
            try {
                in = new FileInputStream(f);
                long len = f.length();

                headers.put(FsConnection.CONTENT_LENGTH, String.valueOf(len));
                if (!headers.containsKey(FsConnection.CONTENT_TYPE)) {
                    final String mime = mimeResolver.getMimeType(f);
                    if ("text/html".equals(mime) || "application/pdf".equals(mime) || mime.startsWith("image")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        headers.put("Content-Disposition", "inline"); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    headers.put(FsConnection.CONTENT_TYPE, mime);
                }
                conn.put(fileHandler, container.getName(), len, remoteFName, in, headers);
                in.close();
                in = null;
            } catch (IOException err) {
                JOptionPane.showMessageDialog(null,
                                              err.getLocalizedMessage(),
                                              Messages.getString("failedToUploadFile", //$NON-NLS-1$
                                                                 remoteFName,
                                                                 container.getName()),
                                              JOptionPane.WARNING_MESSAGE);

            } finally {
                if (in != null)
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }
    }

    @Override
    public Future<ContainerIFace> addFile(VirtualFile node, final File file) {
        final VirtualFile fx = node == null ? resolve() : node;
        if (fx != null) {
            String unixPath = fx.getUnixPathFromSwiftRoot();
            int split1 = unixPath.indexOf(VirtualFile.VIRTUAL_FILE_SEPARATOR);
            int split2 = unixPath.indexOf(VirtualFile.VIRTUAL_FILE_SEPARATOR, split1 + 1);
            final String container;
            final String basePath;
            if (split2 < 1) {
                container = unixPath.substring(split1 + 1);
                basePath = null;
            } else {
                container = unixPath.substring(split1 + 1, split2);
                basePath = unixPath.substring(split2 + 1);
            }
            final ContainerObject o = (ContainerObject) (guiBindings.getContainer(container));
            Callable<ContainerIFace> r = new Callable<ContainerIFace>() {

                @Override
                public ContainerIFace call() throws Exception {
                    putFileRec(file, o, basePath);
                    return o;
                }
            };
            return executor.submit(r);
        } else {
            return null;
        }
    }

    private VirtualFile resolve() {
        int row = table.getSelectedRow();
        if (row > -1) {
            VirtualFile file = (VirtualFile) table.getValueAt(row, 0);
            // FileIFace vfile = file.getFile();
            return doResolveVirtualFile(file);
        }
        return null;
    }

    private VirtualFile doResolveVirtualFile(VirtualFile file) {
        if (file == null) {
            return null;
        }
        if (file.isDirectory()) {
            return file;
        }
        return doResolveVirtualFile(file.getParent());
    }

    @Override
    public Future<VirtualFile> copyVirtualFile(VirtualFile node, final String additionalBasePath, final String file) {
        VirtualFile fx = node == null ? resolve() : node;
        if (fx != null) {
            if (!fx.isDirectory()) {
                fx = fx.getParent();
                if (fx == null) {
                    return null;
                }
            }
            String unixPath = fx.getUnixPathFromSwiftRoot();
            int split1 = unixPath.indexOf(VirtualFile.VIRTUAL_FILE_SEPARATOR);
            int split2 = unixPath.indexOf(VirtualFile.VIRTUAL_FILE_SEPARATOR, split1 + 1);
            final String container;
            final String basePath;
            if (split2 < 1) {
                container = unixPath.substring(split1 + 1);
                basePath = null;
            } else {
                container = unixPath.substring(split1 + 1, split2);
                basePath = unixPath.substring(split2 + 1);
            }
            final ContainerObject o = (ContainerObject) (guiBindings.getContainer(container));
            return executor.submit(new Callable<VirtualFile>() {

                @Override
                public VirtualFile call() {
                    try {
                        final int idx = file.indexOf(VirtualFile.VIRTUAL_FILE_SEPARATOR);
                        if (idx < 0) {
                            return null;
                        }
                        String sourceContainer = file.substring(0, idx);
                        String sourceFile = file.substring(idx + 1);
                        int lastSep = sourceFile.lastIndexOf(VirtualFile.VIRTUAL_FILE_SEPARATOR);
                        String endOfFile = sourceFile;
                        if (lastSep >= 0) {
                            endOfFile = sourceFile.substring(lastSep + 1);
                        }
                        final String fname = (basePath != null ? basePath + VirtualFile.VIRTUAL_FILE_SEPARATOR : "") //$NON-NLS-1$
                                             + additionalBasePath + endOfFile;
                        // fx.getFile().getSize();
                        conn.copy(fileHandler, sourceContainer, sourceFile, o.getName(), fname);
                        Collection<ObjectIFace> files = conn.list(fileHandler, o);
                        o.setFiles(files);
                        return guiBindings.findVirtualFile(o, fname);
                    } catch (IOException err) {
                        Logger.getLogger("io.executor").log(Level.WARNING, //$NON-NLS-1$
                                                            "Failed to list directory", //$NON-NLS-1$
                                                            err);
                        return null;
                    }
                }

            });

        } else {
            return null;
        }
    }

    @Override
    public Future<Boolean> deleteVirtualFiles(final Collection<VirtualFile> files) {
        return executor.submit(new Callable<Boolean>() {

            @Override
            public Boolean call() {
                Boolean ret = Boolean.TRUE;
                for (VirtualFile fv : files) {
                    try {
                        FileIFace fx = fv.getFile();
                        conn.del(fileHandler, fx.getContainer().getName(), fx.getName());
                        ((ContainerObject) fx.getContainer()).deleteFile(fx);
                        // conn.list(fileHandler, fx.getContainer());
                    } catch (IOException e) {
                        e.printStackTrace();
                        ret = Boolean.FALSE;
                    }
                }
                try {
                    conn.list(fileHandler, fileBuilder);
                } catch (IOException e) {
                    e.printStackTrace();
                    return Boolean.FALSE;
                }
                return ret;
            }
        });
    }

    public void refresh(FileIFace file) {
        try {
            long lm = file.getLastModified();
            Map<String, String> inMap = new HashMap<String, String>();
            if (lm > 0) {
                inMap.put("If-Modified-Since", HttpDateParser.getRfc1123Format().format(new Date(lm))); //$NON-NLS-1$
            }
            Map<String, List<String>> map = conn.head(fileHandler, file.getContainer().getName(), file.getName(), inMap);
            // Update data...
            List<String> val = map.get(null);
            if (val != null && !val.isEmpty()) {
                String vx = val.get(0);
                if (vx.startsWith("HTTP/1.1 404")) { //$NON-NLS-1$
                    LOG.warning("File " + file.getName() + " has been deleted"); //$NON-NLS-1$ //$NON-NLS-2$
                    // File has been deleted
                    guiBindings.onFilesRemoved(file.getContainer(), Collections.singleton(file));
                    return;
                }
            }
            file.setHeaders(map);
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Failed to update file " + file.getName(), ex); //$NON-NLS-1$
        }
    }

    /**
     * Parse a RFC date
     * 
     * @param headers
     * @param val
     * @return
     */
    public final static long parseDate(final Map<String, List<String>> headers, final String val) {
        List<String> vals = headers.get(val);

        if (vals != null && !vals.isEmpty()) {
            try {
                String valx = vals.get(0);
                try {
                    final Date d = HttpDateParser.getRfc1123Format().parse(valx);
                    return d.getTime();
                } catch (ParseException e) {
                    LOG.log(Level.WARNING, "Failed to parse due to parseException: " + valx, e); //$NON-NLS-1$
                    return -1;
                } catch (NumberFormatException e) {
                    LOG.log(Level.WARNING, "Failed to parse due to NumberFormatException: " + valx, e); //$NON-NLS-1$
                    return -1;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    /**
     * Parse a long header
     * 
     * @param headers
     * @param val
     * @return
     */
    public final static long parseLong(final Map<String, List<String>> headers, final String val) {
        List<String> vals = headers.get(val);

        if (vals != null && !vals.isEmpty()) {
            try {
                return Long.parseLong(vals.get(0));
            } catch (NumberFormatException e) {
                LOG.log(Level.WARNING, "Cannnot parse long '" + vals.get(0) + "'", e); //$NON-NLS-1$//$NON-NLS-2$
                e.printStackTrace();
            }
        }
        return -1;
    }

}
