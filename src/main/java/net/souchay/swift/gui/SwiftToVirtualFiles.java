/**
 *
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-07-07 12:15:19 +0200 (Lun 07 jul 2014) $
 */
package net.souchay.swift.gui;

import java.text.CollationElementIterator;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.text.RuleBasedCollator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import net.souchay.swift.gui.ContainerIFace.FileIFace;
import net.souchay.swift.gui.ContainerIFace.FileListener;

/**
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3836 $
 * 
 */
public class SwiftToVirtualFiles implements FileListener, ElementChangedListener<VirtualFile> {

    /**
     * Constructor
     * 
     * @param fileFactory
     * @param model
     */
    public SwiftToVirtualFiles(SwiftTreeModel model) {
        this.model = model;
        virtualFileNotifier.addElementChangedListener(this);
    }

    /**
     * Interface called when intermediate results are available
     * 
     * @copyright Pierre Souchay - 2013,2014
     * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
     * @version $Revision: 3836 $
     * 
     * @param <T>
     */
    public static interface OnResult<T> {

        /**
         * Called when results are available
         * 
         * @param result
         */
        public void doProcess(T result);
    }

    private static final Pattern removeDiac = Pattern.compile("\\p{InCombiningDiacriticalMarks}+"); //$NON-NLS-1$

    /**
     * Removes accents
     * 
     * @param val the value
     * @return the value without accents
     */
    private static final String removeAccents(final String val) {
        final String v2 = val.toLowerCase();
        final String normalized = Normalizer.normalize(v2, Form.NFD);
        final Matcher m = removeDiac.matcher(normalized);
        return m.replaceAll(""); //$NON-NLS-1$

    }

    public static final int indexOf(RuleBasedCollator rbc, String source, CollationElementIterator patCEI) {
        CollationElementIterator textCEI = rbc.getCollationElementIterator(source);
        patCEI.reset();
        // e1 will contain the collation element for the source
        // e2 will contain the collation element for the pattern
        int e1;
        int startMatch = -1;

        // initialize e2 with the first collation element in the pattern
        int e2 = patCEI.next();

        while ((e1 = textCEI.next()) != CollationElementIterator.NULLORDER) {
            if (e1 == e2) { // if the elements match
                if (startMatch == -1)
                    startMatch = textCEI.getOffset();
                e2 = patCEI.next(); // increment to the next element
                if (e2 == CollationElementIterator.NULLORDER)
                    break;
            } else { // elements do not match
                if (startMatch != -1) {
                    patCEI.reset();
                    e2 = patCEI.next();
                    startMatch = -1;
                }
            }
        }
        return startMatch;
    }

    public SwingWorker<Collection<VirtualFile>, VirtualFile> searchFor(final String patternToSearch,
            final OnResult<Collection<VirtualFile>> toRunOnProcess) {
        return new SwingWorker<Collection<VirtualFile>, VirtualFile>() {

            @Override
            protected Collection<VirtualFile> doInBackground() throws Exception {
                final String pattern = removeAccents(patternToSearch);
                // RuleBasedCollator rbc = (RuleBasedCollator) Collator.getInstance();
                // rbc.setStrength(Collator.SECONDARY);
                final Collection<VirtualFile> allValues = virtualFiles.values();
                int i = 0;
                final int count = allValues.size();
                LinkedList<VirtualFile> files = new LinkedList<VirtualFile>();
                // CollationElementIterator patternIterator = rbc.getCollationElementIterator(pattern);

                for (VirtualFile kx : allValues) {
                    final String k = removeAccents(kx.getName());
                    int idx = k.indexOf(pattern);
                    if (idx >= 0) {
                        publish(kx);
                        files.add(kx);
                    }
                    i++;
                    setProgress(100 * i / count);
                }
                setProgress(100);
                return files;
            }

            /**
             * @see javax.swing.SwingWorker#process(java.util.List)
             */
            @Override
            protected void process(List<VirtualFile> chunks) {
                toRunOnProcess.doProcess(chunks);
            }

        };

    }

    private final SwiftTreeModel model;

    private final Map<String, VirtualFile> virtualFiles = new ConcurrentSkipListMap<String, VirtualFile>();

    private final Map<ContainerIFace, VirtualFile> containers = new HashMap<ContainerIFace, VirtualFile>();

    private final static String VIRTUAL_PATH_SEPARATOR = "/";//$NON-NLS-1$

    /**
     * Find the given Virtual File if any
     * 
     * @param file
     * @return The Virtual File
     */
    public VirtualFile findVirtualFile(FileIFace file) {
        return findVirtualFile(file.getContainer(), file.getName());
    }

    public void cleanup() {
        for (VirtualFile c : containers.values()) {
            if (c.getContainer() != null)
                c.getContainer().removeFileListener(this);
            c.delete();
        }
        containers.clear();
        for (VirtualFile f : virtualFiles.values())
            f.delete();
        virtualFiles.clear();
        model.clear();
        virtualFileNotifier.removeElementChangedListener(this);
    }

    /**
     * Find the given Virtual File if any
     * 
     * @param file
     * @return The Virtual File
     */
    public VirtualFile findVirtualFile(ContainerIFace container, String fileName) {
        return virtualFiles.get(container.getName() + VIRTUAL_PATH_SEPARATOR + fileName);
    }

    public VirtualFile createNewDirectory(VirtualFile parent, String dirName) {
        VirtualFile f = findVirtualFile(parent.getContainer(), parent.getUnixPathWithoutContainer()
                                                               + VirtualFile.VIRTUAL_FILE_SEPARATOR + dirName);
        if (f != null)
            return f;
        SwiftFile fx = FileIfaceFactory.getFile(parent.getContainer(), parent.getUnixPathWithoutContainer()
                                                                       + VIRTUAL_PATH_SEPARATOR + dirName);
        fx.setSize(0);
        fx.setContentType(VirtualFile.INODE_DIRECTORY_MIME_TYPE);
        fx.setLastModified(System.currentTimeMillis());
        f = new VirtualFile(virtualFileNotifier, parent, dirName, fx);
        virtualFiles.put(f.getUnixPathWithContainer(), f);
        model.addItems(Collections.singleton(f), SwingUtilities.isEventDispatchThread());
        return f;
    }

    private Collection<VirtualFile> buildsFilesFromSwiftFile(ContainerIFace container, FileIFace newFile) {
        Set<VirtualFile> added = new HashSet<VirtualFile>();
        VirtualFile parent = containers.get(container);
        final String[] paths = newFile.getName().split(VIRTUAL_PATH_SEPARATOR);
        String fullName = parent.getName() + VIRTUAL_PATH_SEPARATOR + newFile.getName();
        if (fullName.endsWith(VIRTUAL_PATH_SEPARATOR))
            fullName = fullName.substring(0, fullName.length() - 1);
        StringBuilder sb = new StringBuilder(container.getName());
        for (String s : paths) {
            sb.append(VIRTUAL_PATH_SEPARATOR).append(s);
            String theP = sb.toString();
            VirtualFile fx = virtualFiles.get(theP);
            if (fx == null) {
                fx = new VirtualFile(virtualFileNotifier, parent, s, fullName.equals(theP) ? newFile : null);
                virtualFiles.put(theP, fx);
                added.add(fx);
            } else {
                if (fx.getChildren().isEmpty()) {
                    final VirtualFile wf = fx;
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            model.updateVirtualFile(wf);
                        }

                    });
                }
            }
            parent = fx;
        }
        return added;
    }

    /**
     * Get a container from its name
     * 
     * @param name
     * @return the container object
     */
    public ContainerIFace getContainer(String name) {
        for (ContainerIFace c : containers.keySet()) {
            if (c.getName().equals(name))
                return c;
        }
        return null;
    }

    /**
     * Adds the given container
     * 
     * @param iface container to add
     */
    public void addContainer(ContainerIFace iface) {
        VirtualFile f = containers.get(iface);
        if (f != null)
            return;
        f = new ContainerVirtualFile(virtualFileNotifier, model.getRoot(), iface);
        containers.put(iface, f);
        onFilesAdded(iface, iface.getFiles());
        iface.addFileListener(this);
        model.updateRoot(f);
    }

    /**
     * Adds the given container
     * 
     * @param iface container to add
     */
    public void deleteContainer(ContainerIFace iface) {
        VirtualFile f = containers.remove(iface);
        if (f == null)
            return;
        iface.removeFileListener(this);
        // onFilesRemoved(iface, iface.getFiles());
        model.removeItems(Collections.singleton(f));
    }

    private List<FileIFace> filesToRefresh = Collections.synchronizedList(new ArrayList<FileIFace>());

    /**
     * Get the files to refresh
     * 
     * @return the files to refresh, removes all existing files to refresh
     */
    public List<FileIFace> getFilesToRefresh() {
        ArrayList<FileIFace> toRefresh = new ArrayList<ContainerIFace.FileIFace>(filesToRefresh);
        filesToRefresh.clear();
        return toRefresh;
    }

    /**
     * @see net.souchay.swift.gui.ContainerIFace.FileListener#onFilesAdded(net.souchay.swift.gui.ContainerIFace,
     *      java.util.Collection)
     */
    @Override
    public void onFilesAdded(ContainerIFace source, Collection<? extends FileIFace> filesAdded) {
        // final long start = System.currentTimeMillis();
        final int MAX_ADDED_EACH_TIME = 5000;
        ArrayList<VirtualFile> added = new ArrayList<VirtualFile>(MAX_ADDED_EACH_TIME + 100);
        for (FileIFace f : filesAdded) {
            final String fullPath = source.getName() + VIRTUAL_PATH_SEPARATOR + f.getName();
            VirtualFile vf = virtualFiles.get(fullPath);
            if (vf == null) {
                added.addAll(buildsFilesFromSwiftFile(source, f));
                if (f.getSize() == 0 && !VirtualFile.INODE_DIRECTORY_MIME_TYPE.equals(f.getContentType())) {
                    filesToRefresh.add(f);
                }
            } else {
                vf.setFile(f);
            }
            if (added.size() > MAX_ADDED_EACH_TIME) {
                final ArrayList<VirtualFile> copy = new ArrayList<VirtualFile>(added);
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        model.addItems(copy, true);
                    }

                });
                added.clear();
            }
        }
        if (!added.isEmpty()) {
            model.addItems(added, false);
        }
        // System.err.println("Spent : " + (System.currentTimeMillis() - start) + " for " + filesAdded.size() +
        // " items");
    }

    private void recDeleteParent(final VirtualFile parent) {
        if (parent.getChildren().isEmpty() && parent.getParent() != null && parent.getParent().getParent() != null) {
            final VirtualFile pp = parent.getParent();
            virtualFiles.remove(parent.getUnixPathWithContainer());
            parent.delete();
            model.removeItems(Collections.singleton(parent));
            recDeleteParent(pp);
        }
    }

    /**
     * @see net.souchay.swift.gui.ContainerIFace.FileListener#onFilesRemoved(net.souchay.swift.gui.ContainerIFace,
     *      java.util.Collection)
     */
    @Override
    public void onFilesRemoved(ContainerIFace source, Collection<? extends FileIFace> filesRemoved) {
        final Set<VirtualFile> parentsRemoved = new HashSet<VirtualFile>();
        LinkedList<VirtualFile> removed = new LinkedList<VirtualFile>();
        for (FileIFace f : filesRemoved) {
            String x = source.getName() + VIRTUAL_PATH_SEPARATOR + f.getName();
            VirtualFile existing = virtualFiles.remove(x);
            if (existing != null) {
                VirtualFile parent = existing.getParent();
                existing.delete();
                parentsRemoved.add(parent);
                removed.add(existing);
            }
        }
        if (!removed.isEmpty()) {
            model.removeItems(removed);
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    for (VirtualFile f : parentsRemoved) {
                        recDeleteParent(f);
                    }
                }

            });

        }
    }

    /**
     * @see net.souchay.swift.gui.ElementChangedListener#onElementChanged(java.lang.Object)
     */
    @Override
    public void onElementChanged(VirtualFile source) {
        model.updateVirtualFile(source);
    }

    @Override
    public void onContainerMetaDataChanged(ContainerIFace source) {
        final VirtualFile f = containers.get(source);
        final Runnable r = new Runnable() {

            @Override
            public void run() {
                model.updateVirtualFile(f);
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }

    }

    private final ElementChangedSupport<VirtualFile> virtualFileNotifier = new ElementChangedSupport<VirtualFile>();

    /**
     * get the virtualFileNotifier
     * 
     * @return the virtualFileNotifier
     */
    public ElementChangedListenerRegistration<VirtualFile> getVirtualFileNotifier() {
        return virtualFileNotifier;
    }
}
