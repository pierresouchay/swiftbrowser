package net.souchay.swift.gui.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import net.souchay.swift.gui.VirtualFile;
import net.souchay.swift.net.DefaultSwiftConnectionResult;
import net.souchay.swift.net.FsConnection;
import net.souchay.swift.net.SwiftConnections;

public class RemoteFilesTransferable implements Transferable {

    /**
     * get the files
     * 
     * @return the files
     */
    public List<VirtualFile> getFiles() {
        return files;
    }

    static class LazyList<T> implements List<T> {

        private final Future<List<T>> future;

        private final int len;

        /**
         * Constructor
         * 
         * @param future
         */
        public LazyList(int len, Future<List<T>> future) {
            this.len = len;
            this.future = future;
        }

        @Override
        public int size() {
            return len;
        }

        @Override
        public boolean isEmpty() {
            return len > 0;
        }

        @Override
        public boolean contains(Object o) {
            try {
                return future.get().contains(o);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Iterator<T> iterator() {
            try {
                return future.get().iterator();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Object[] toArray() {
            try {
                return future.get().toArray();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public <Y> Y[] toArray(Y[] a) {
            try {
                return future.get().toArray(a);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean add(T e) {
            try {
                return future.get().add(e);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public boolean remove(Object o) {
            try {
                return future.get().remove(o);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            try {
                return future.get().containsAll(c);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean addAll(Collection<? extends T> c) {
            try {
                return future.get().addAll(c);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean addAll(int index, Collection<? extends T> c) {
            try {
                return future.get().addAll(index, c);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            try {
                return future.get().removeAll(c);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            try {
                return future.get().retainAll(c);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void clear() {
            try {
                future.get().clear();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public T get(int index) {
            try {
                return future.get().get(index);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public T set(int index, T element) {
            try {
                return future.get().set(index, element);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void add(int index, T element) {
            try {
                future.get().add(index, element);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

        @Override
        public T remove(int index) {
            try {
                return future.get().remove(index);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public int indexOf(Object o) {
            try {
                return future.get().indexOf(o);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public int lastIndexOf(Object o) {
            try {
                return future.get().lastIndexOf(o);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public ListIterator<T> listIterator() {
            try {
                return future.get().listIterator();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public ListIterator<T> listIterator(int index) {
            try {
                return future.get().listIterator(index);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public List<T> subList(int fromIndex, int toIndex) {
            try {
                return future.get().subList(fromIndex, toIndex);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final List<VirtualFile> files;

    private final SwiftConnections connections;

    private final DefaultSwiftConnectionResult res;

    /**
     * Constructor
     * 
     * @param bar
     */
    public RemoteFilesTransferable(DefaultSwiftConnectionResult res, SwiftConnections connections,
            Collection<VirtualFile> files) {
        this.connections = connections;
        this.files = new ArrayList<VirtualFile>(files);
        this.res = res;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {

        if (net.souchay.swift.gui.dnd.Constants.virtualFilesList.match(flavor)) {
            ArrayList<String> theFiles = new ArrayList<String>(files.size());
            for (VirtualFile f : files) {
                theFiles.add(f.getUnixPathWithContainer());
            }
            TenantDragAndDrop dnd = new TenantDragAndDrop(connections.getTenant(), theFiles);
            return dnd;
        } else if (net.souchay.swift.gui.dnd.Constants.uriList.match(flavor)) {
            StringBuilder sb = new StringBuilder();
            final long expires = System.currentTimeMillis() + 3600000 * 3;
            for (VirtualFile f : files) {
                if (sb.length() > 0)
                    sb.append('\n');
                try {
                    sb.append(connections.generateTempUrl("GET", //$NON-NLS-1$
                                                          expires,
                                                          f.getFile(),
                                                          false));
                } catch (InvalidKeyException err) {
                    sb.append(connections.getTenant().getPublicUrl() + f.getUnixPathWithContainer());
                }
            }
            return sb.toString();
        } else if (DataFlavor.javaFileListFlavor.match(flavor)) {
            try {
                Future<List<File>> files = copyAllFilesAsync(null);
                return new LazyList<File>(this.files.size(), files);
            } catch (Exception err) {
                throw new IOException(err);
            }
        } else {
            if (Constants.urlFlavor.match(flavor)) {
                final VirtualFile f = files.get(0);
                try {
                    return connections.generateTempUrl("GET", System.currentTimeMillis() + 3600000, f, false).toURL(); //$NON-NLS-1$
                } catch (InvalidKeyException err) {
                    return connections.getTenant().getPublicUrl() + f.getUnixPathWithContainer();
                }
            } else if (DataFlavor.stringFlavor.match(flavor)) {
                StringBuilder sb = new StringBuilder();
                final long expires = System.currentTimeMillis() + 3600000 * 3;
                for (VirtualFile f : files) {
                    if (sb.length() > 0)
                        sb.append('\n');
                    try {
                        sb.append(connections.generateTempUrl("GET", //$NON-NLS-1$
                                                              expires,
                                                              f.getFile().getContainer()
                                                                      + FsConnection.URL_PATH_SEPARATOR
                                                                      + f.getFile().getName(),
                                                              false).toASCIIString());
                    } catch (InvalidKeyException err) {
                        sb.append(connections.getTenant().getPublicUrl() + f.getUnixPathWithContainer());
                    }
                }
                return sb.toString();
            }
        }
        throw new UnsupportedFlavorException(flavor);
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return Arrays.copyOf(Constants.exportableDataFlavors, Constants.exportableDataFlavors.length);
    }

    @Override
    public boolean isDataFlavorSupported(final DataFlavor flavor) {
        for (DataFlavor i : Constants.exportableDataFlavors) {
            if (i.match(flavor)) {
                return true;
            }
        }
        return false;
    }

    private Future<List<File>> lastFuture = null;

    public Future<List<File>> copyAllFilesAsync(final Runnable runnableAtEnd) {
        if (lastFuture != null)
            return lastFuture;

        lastFuture = GlobalExecutorService.getExecutorService().submit(new Callable<List<File>>() {

            @Override
            public List<File> call() throws Exception {
                try {
                    return copyAllFiles();
                } finally {
                    if (runnableAtEnd != null)
                        runnableAtEnd.run();
                }
            }
        });
        return lastFuture;
    }

    /**
     * Compute a file name in the temporary directory
     * 
     * @param connections
     * @param container
     * @param path
     * @return
     * @throws IOException
     */
    public static File computeFile(SwiftConnections connections, final String container, final String path)
            throws IOException {
        File dest = new File(new File(connections.getTemporaryDirectory(), container), path);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        return dest;
    }

    public List<File> copyAllFiles() throws IOException {
        final List<File> ret = new ArrayList<File>(files.size());

        try {
            for (VirtualFile f : files) {
                final VirtualFile vf = f;
                final File file = computeFile(connections, f.getFile().getContainer().getName(), f.getFile().getName());
                if (file.exists()) {
                    // NOOP
                    ret.add(file.getAbsoluteFile());
                } else {
                    connections.get(res,
                                    f.getFile().getContainer().getName(),
                                    f.getFile().getName(),
                                    new FsConnection.OnFileDownloaded() {

                                        @Override
                                        public void onDownload(File f, String container, String path, boolean success) {
                                            if (success) {
                                                file.setLastModified(vf.getLastModified());
                                            } else {
                                                System.err.println("Failed for " + container + "/" + path); //$NON-NLS-1$//$NON-NLS-2$
                                            }
                                        }

                                        @Override
                                        public File onStartDownload(String container, String path, int totalLengh)
                                                throws IOException {
                                            return file;
                                        }
                                    }, -1);
                }
            }
            return ret;
        } catch (IOException err) {
            throw err;
        }
    }
}
