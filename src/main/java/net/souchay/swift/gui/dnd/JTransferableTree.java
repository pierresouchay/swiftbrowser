package net.souchay.swift.gui.dnd;

import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DragSourceMotionListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.TreePath;
import net.souchay.swift.gui.ContainerIFace;
import net.souchay.swift.gui.ContainerObject;
import net.souchay.swift.gui.FileImport.FileOrURLListener;
import net.souchay.swift.gui.Messages;
import net.souchay.swift.gui.ObjectIFace;
import net.souchay.swift.gui.VirtualFile;
import net.souchay.swift.gui.configuration.SwiftConfigurationEditor;
import net.souchay.swift.net.DefaultSwiftConnectionResult;
import net.souchay.swift.net.SwiftConnections;
import net.souchay.utilities.Application;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.TreeTableModel;

/**
 * JTree with support for DnD
 * 
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3830 $
 * 
 */
public class JTransferableTree extends JXTreeTable implements ClipboardOwner {

    public final static java.util.logging.Logger LOG = java.util.logging.Logger.getLogger("gui.tree"); //$NON-NLS-1$

    /**
     * 
     */
    private static final long serialVersionUID = -8714620903675186901L;

    /**
     * Constructor
     * 
     * @param model
     */
    public JTransferableTree(SwiftConnections connections, TreeTableModel model, FileOrURLListener fileListener) {
        super(model);
        setTransferHandler(null);
        setDragEnabled(false);
        setDropMode(DropMode.ON_OR_INSERT_ROWS);
        this.fileListener = fileListener;
        DragSource dragSource = DragSource.getDefaultDragSource();
        this.connections = connections;
        dragSource.createDefaultDragGestureRecognizer(this,
                                                      DnDConstants.ACTION_COPY_OR_MOVE,
                                                      new TreeDragGestureListener());
        DropTarget dropTarget = new DropTarget(this, new TreeDropTargetListener());
        setDropTarget(dropTarget);
        getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                int numRows = getSelectedRowCount();
                if (numRows > 0) {
                    copyAction.setEnabled(true);
                    if (numRows == 1)
                        pasteAction.setEnabled(true);
                }
            }
        });
    }

    /**
     * Returns true if we can import
     * 
     * @param node
     * @return the drop action, NONE if no drop
     */
    private int doPaste(VirtualFile node, Transferable tr, DropTargetDropEvent drop) throws IOException,
            UnsupportedFlavorException {
        boolean accept = false;
        int action = drop == null ? DnDConstants.ACTION_COPY_OR_MOVE : drop.getDropAction();
        if (tr.isDataFlavorSupported(Constants.virtualFilesList)) {
            LOG.fine("Requesting VirtualFilesList clipboard..."); //$NON-NLS-1$
            TenantDragAndDrop tenantAndFiles = (TenantDragAndDrop) tr.getTransferData(Constants.virtualFilesList);
            if (connections.getTenant().getPublicUrl().equals(tenantAndFiles.getTenantPublicURL())) {
                LOG.info("Using direct copy since source and destination are in the same tenant..."); //$NON-NLS-1$
                // We can provide a direct copy !
                List<String> commonPaths = null;
                for (String f : tenantAndFiles.getFiles()) {
                    String subPaths[] = f.split(VirtualFile.VIRTUAL_FILE_SEPARATOR);
                    if (commonPaths == null) {
                        commonPaths = new LinkedList<String>();
                        for (int i = 1; i < subPaths.length - 1; i++) {
                            commonPaths.add(subPaths[i]);
                        }
                    } else {
                        if (subPaths.length < 2) {
                            commonPaths = Collections.emptyList();
                            break;
                        }
                        int j = -1;
                        for (int i = 1; i < subPaths.length - 1; i++) {
                            if ((i - 1) >= commonPaths.size()) {
                                break;
                            }
                            String sub = subPaths[i];
                            if (!sub.equals(commonPaths.get(i - 1))) {
                                break;
                            }
                            j = i - 1;
                        }
                        List<String> ret = new LinkedList<String>();
                        for (int k = 0; k <= j; k++)
                            ret.add(commonPaths.get(k));
                        commonPaths = ret;
                    }
                }
                StringBuilder commonPath = new StringBuilder(VirtualFile.VIRTUAL_FILE_SEPARATOR);
                if (commonPaths != null) {
                    for (String s : commonPaths) {
                        commonPath.append(s).append(VirtualFile.VIRTUAL_FILE_SEPARATOR);
                    }
                }
                List<Future<?>> files = new LinkedList<Future<?>>();
                for (String f : tenantAndFiles.getFiles()) {
                    String pathWithoutContainer = f.substring(f.indexOf(VirtualFile.VIRTUAL_FILE_SEPARATOR));
                    String dir;
                    if (pathWithoutContainer.indexOf(commonPath.toString()) != 0) {
                        LOG.warning("Invalid directory found, did not find " + commonPath.toString() + " into " //$NON-NLS-1$ //$NON-NLS-2$
                                    + pathWithoutContainer);
                        dir = ""; //$NON-NLS-1$
                    } else {
                        dir = pathWithoutContainer.substring(commonPath.length());
                        final int idx = dir.lastIndexOf(VirtualFile.VIRTUAL_FILE_SEPARATOR);
                        if (idx > 0 && idx < dir.length() - 1) {
                            dir = dir.substring(0, idx) + VirtualFile.VIRTUAL_FILE_SEPARATOR;
                        } else {
                            dir = ""; //$NON-NLS-1$
                        }
                    }
                    if (!accept && drop != null) {
                        drop.acceptDrop(action);
                    }
                    accept = true;
                    files.add(fileListener.copyVirtualFile(node, dir, f));
                }
                {
                    final ContainerObject containerToRefresh = (ContainerObject) node.getContainer();
                    refreshContainerOnCompletion(containerToRefresh, files);
                }
                if ((action & DnDConstants.ACTION_MOVE) == DnDConstants.ACTION_MOVE) {
                    //
                    action = DnDConstants.ACTION_MOVE;
                }
                if (accept) {
                    return action;
                } else
                    return DnDConstants.ACTION_NONE;
            }
        }
        if (action == DnDConstants.ACTION_COPY_OR_MOVE) {
            action = DnDConstants.ACTION_COPY;
        }
        if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            LOG.info("Requesting FileList clipboard..."); //$NON-NLS-1$
            if (!accept && drop != null) {
                drop.acceptDrop(action);
            }
            accept = true;
            @SuppressWarnings("unchecked")
            List<File> userObject = (List<File>) tr.getTransferData(DataFlavor.javaFileListFlavor);
            List<Future<?>> files = new ArrayList<Future<?>>(userObject.size());
            for (File f : userObject) {
                files.add(fileListener.addFile(node, f));
            }
            {
                final ContainerObject containerToRefresh = (ContainerObject) node.getContainer();
                refreshContainerOnCompletion(containerToRefresh, files);
            }
            if (accept)
                return action;
            else
                return DnDConstants.ACTION_NONE;
        } else if (tr.isDataFlavorSupported(Constants.uriList)) {
            LOG.info("Requesting uri-list clipboard..."); //$NON-NLS-1$
            if (drop != null)
                drop.acceptDrop(action);
            String string = (String) tr.getTransferData(Constants.uriList);
            return parseURIs(node, action, string);
        } else if (tr.isDataFlavorSupported(Constants.urlFlavor)) {
            LOG.info("Requesting URL clipboard..."); //$NON-NLS-1$
            URL stream = (URL) tr.getTransferData(Constants.urlFlavor);
            if (!accept && drop != null) {
                drop.acceptDrop(action);
            }
            accept = true;
            Future<ContainerIFace> file = fileListener.addUrl(node, stream);
            try {
                file.get(500, TimeUnit.MILLISECONDS);
            } catch (Throwable ignored) {
                //
            }
            {
                final ContainerObject containerToRefresh = (ContainerObject) node.getContainer();
                GlobalExecutorService.getExecutorService().submit(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            Collection<ObjectIFace> newFiles = connections.list(getSwiftConnectionResult(),
                                                                                containerToRefresh);
                            containerToRefresh.setFiles(newFiles);
                        } catch (IOException e) {
                            LOG.log(Level.WARNING, "Failed to refresh container " + containerToRefresh, e); //$NON-NLS-1$
                        }
                    }
                });
            }
            if (accept)
                return action;
            else
                return DnDConstants.ACTION_NONE;
        } else if (tr.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            LOG.info("Requesting String clipboard..."); //$NON-NLS-1$
            if (!accept && drop != null) {
                drop.acceptDrop(action);
            }
            accept = true;
            BufferedReader r = new BufferedReader(DataFlavor.stringFlavor.getReaderForText(tr));
            StringBuilder sb = new StringBuilder();
            try {
                String s;
                // List<Future<VirtualFile>> files = new LinkedList<Future<VirtualFile>>();
                int MAX_SIZE = 32 * 1024 * 1024;
                while (null != (s = r.readLine()) && sb.length() < MAX_SIZE) {
                    if (s.startsWith("http") || s.startsWith("ftp") || s.startsWith("file") || s.startsWith("/")) //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                        sb.append(s).append('\n');
                }
            } finally {
                r.close();
            }
            return parseURIs(node, action, sb.toString());
        } else {
            LOG.info("Rejected " + Arrays.toString(tr.getTransferDataFlavors())); //$NON-NLS-1$
            return DnDConstants.ACTION_NONE;
        }
    }

    private static final void executeOnceAllDone(final Runnable toDoOnceDone, final Collection<Future<?>> toWaitFor) {
        GlobalExecutorService.getExecutorService().submit(new Runnable() {

            @Override
            public void run() {
                try {
                    for (Future<?> x : toWaitFor) {
                        x.get();
                    }
                } catch (Throwable ignored) {
                    //
                }
                toDoOnceDone.run();
            }
        });
    };

    public void refreshContainerOnCompletion(final ContainerObject container, final Collection<Future<?>> toWaitFor) {
        executeOnceAllDone(new Runnable() {

            @Override
            public void run() {
                try {
                    Collection<ObjectIFace> files = connections.list(getSwiftConnectionResult(), container);
                    container.setFiles(files);
                } catch (IOException err) {
                    LOG.log(Level.WARNING, "Failed to refresh container " + container, err); //$NON-NLS-1$
                }
            }
        }, toWaitFor);
    }

    private int parseURIs(VirtualFile node, int action, String string) {
        boolean accept = false;
        String split[] = string.split("\n"); //$NON-NLS-1$
        LinkedList<Future<?>> files = new LinkedList<Future<?>>();
        for (String s : split) {
            final String u = s.trim();
            if (!u.startsWith("#")) { //$NON-NLS-1$
                try {
                    accept = true;
                    files.add(fileListener.addUrl(node, new URL(u)));
                } catch (MalformedURLException err) {
                    LOG.log(Level.INFO, "Malformed URL " + u, err); //$NON-NLS-1$
                }
            }
        }

        {
            final ContainerObject containerToRefresh = (ContainerObject) node.getContainer();
            refreshContainerOnCompletion(containerToRefresh, files);
        }

        if (accept)
            return action;
        else
            return DnDConstants.ACTION_NONE;
    }

    public RemoteFilesTransferable createTransferable() {
        int numRows = getSelectedRowCount();
        if (numRows < 1) {
            // Nothing selected, nothing to drag
            getToolkit().beep();
            return null;
        } else {
            Set<VirtualFile> files = new HashSet<VirtualFile>();
            for (int row : getSelectedRows()) {
                VirtualFile f = (VirtualFile) getValueAt(row, 0);
                f.getAllFileChildren(files);
            }
            RemoteFilesTransferable transferable = new RemoteFilesTransferable(getSwiftConnectionResult(),
                                                                               connections,
                                                                               files);
            return transferable;
        }
    }

    @Override
    public TransferHandler getTransferHandler() {
        return null;
    }

    private FileOrURLListener fileListener;

    private DefaultSwiftConnectionResult swiftConnectionResult;

    /**
     * get the swiftConnectionResult
     * 
     * @return the swiftConnectionResult
     */
    public DefaultSwiftConnectionResult getSwiftConnectionResult() {
        return swiftConnectionResult;
    }

    /**
     * Set the swiftConnectionResult
     * 
     * @param swiftConnectionResult the swiftConnectionResult to set
     */
    public void setSwiftConnectionResult(DefaultSwiftConnectionResult swiftConnectionResult) {
        this.swiftConnectionResult = swiftConnectionResult;
    }

    public FileOrURLListener getFileListener() {
        return fileListener;
    }

    public void setFileListener(FileOrURLListener fileListener) {
        this.fileListener = fileListener;
    }

    private final SwiftConnections connections;

    private Insets insets;

    private int top = 0, bottom = 0, topRow = 0, bottomRow = 0;

    public Insets getAutoscrollInsets() {
        return insets;
    }

    public void autoscroll(Point p) {
        // Only support up/down scrolling
        top = Math.abs(getLocation().y) + 10;
        bottom = top + getParent().getHeight() - 20;
        int next;
        if (p.y < top) {
            next = topRow--;
            bottomRow++;
            scrollRowToVisible(next);
        } else if (p.y > bottom) {
            next = bottomRow++;
            topRow--;
            scrollRowToVisible(next);
        }
    }

    private class TreeDragGestureListener implements DragGestureListener {

        private final MyDragSourceListener dragListener = new MyDragSourceListener();

        @Override
        public void dragGestureRecognized(DragGestureEvent dragGestureEvent) {
            RemoteFilesTransferable transferable = createTransferable();
            if (transferable != null) {
                dragGestureEvent.startDrag(DragSource.DefaultCopyNoDrop,
                                           dragIcon.getImage(),
                                           new Point(5, 5),
                                           transferable,
                                           dragListener);
            }
        }

    }

    private class TreeDropTargetListener implements DropTargetListener {

        private int defaultActions = DnDConstants.ACTION_COPY_OR_MOVE;

        @Override
        public void dragEnter(DropTargetDragEvent dropTargetDragEvent) {
            // Setup positioning info for auto-scrolling
            top = Math.abs(getLocation().y);
            bottom = top + getParent().getHeight();

            topRow = rowAtPoint(new Point(0, top));
            bottomRow = rowAtPoint(new Point(0, bottom));
            insets = new Insets(top + 10, 0, bottom - 10, getWidth());
            List<DataFlavor> flavors = dropTargetDragEvent.getCurrentDataFlavorsAsList();
            if (flavors.contains(Constants.virtualFilesList)) {
                defaultActions = DnDConstants.ACTION_COPY_OR_MOVE;
                dropTargetDragEvent.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
            } else {
                if (null != Constants.supportsDataFlavor(flavors)) {
                    defaultActions = DnDConstants.ACTION_COPY;
                    dropTargetDragEvent.acceptDrag(DnDConstants.ACTION_COPY);
                } else {
                    defaultActions = DnDConstants.ACTION_NONE;
                    dropTargetDragEvent.rejectDrag();
                }
            }
        }

        @Override
        public void dragExit(DropTargetEvent dropTargetEvent) {
        }

        private TreePath lastPathSelected = null;

        private long lastPathSelectedAt = 0;

        @Override
        public void dragOver(DropTargetDragEvent dropTargetDragEvent) {
            Point location = dropTargetDragEvent.getLocation();
            TreePath path = getPathForLocation(location.x, location.y);
            if (path == lastPathSelected && path != null) {
                if (System.currentTimeMillis() - lastPathSelectedAt > 250) {
                    VirtualFile node = (VirtualFile) path.getLastPathComponent();
                    if (node != null && !node.isLeaf()) {
                        expandPath(path);
                        scrollPathToVisible(path);
                    }
                }
            } else {
                lastPathSelected = path;
                lastPathSelectedAt = System.currentTimeMillis();
            }
            if (path != null) {
                scrollPathToVisible(path);
                if (defaultActions == DnDConstants.ACTION_NONE)
                    dropTargetDragEvent.rejectDrag();
                else
                    dropTargetDragEvent.acceptDrag(defaultActions);
            } else {
                dropTargetDragEvent.rejectDrag();
            }
            autoscroll(location);
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent dropTargetDragEvent) {
        }

        @Override
        public synchronized void drop(DropTargetDropEvent dropTargetDropEvent) {
            // Only support dropping over nodes that aren't leafs
            Point location = dropTargetDropEvent.getLocation();
            TreePath path = getPathForLocation(location.x, location.y);
            VirtualFile node = (VirtualFile) path.getLastPathComponent();
            if (node != null) {

                if (!node.isDirectory()) {
                    node = node.getParent();
                    if (node == null)
                        dropTargetDropEvent.rejectDrop();
                }
                try {
                    Transferable tr = dropTargetDropEvent.getTransferable();
                    // dropTargetDropEvent.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    int dropAction = doPaste(node, tr, dropTargetDropEvent);
                    if (dropAction == DnDConstants.ACTION_NONE) {
                        dropTargetDropEvent.rejectDrop();
                    } else {
                        dropTargetDropEvent.dropComplete(true);
                    }
                } catch (IOException err) {
                    LOG.log(Level.WARNING, err.getClass().getSimpleName() + ": " + err.getLocalizedMessage(), err); //$NON-NLS-1$
                    dropTargetDropEvent.rejectDrop();
                } catch (UnsupportedFlavorException err) {
                    LOG.log(Level.WARNING, err.getClass().getSimpleName() + ": " + err.getLocalizedMessage(), err); //$NON-NLS-1$
                    dropTargetDropEvent.rejectDrop();
                }
            } else {
                LOG.info("Can't drop on a null node"); //$NON-NLS-1$
                dropTargetDropEvent.rejectDrop();
            }
        }
    }

    class MyDragSourceListener implements DragSourceListener, DragSourceMotionListener {

        @Override
        public void dragDropEnd(DragSourceDropEvent dragSourceDropEvent) {
            if (dragSourceDropEvent.getDropSuccess()) {
                int dropAction = dragSourceDropEvent.getDropAction();
                if (dropAction == DnDConstants.ACTION_MOVE) {
                    LOG.info("MOVE: remove node"); //$NON-NLS-1$
                    // RemoteFilesTransferable tr = (RemoteFilesTransferable) dragSourceDropEvent.getDragSourceContext()
                    // .getTransferable();
                    // fileListener.deleteVirtualFiles(tr.getFiles());
                }
            }

        }

        @Override
        public void dragEnter(DragSourceDragEvent dragSourceDragEvent) {
        }

        @Override
        public void dragExit(DragSourceEvent dragSourceEvent) {
            dragSourceEvent.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
        }

        @Override
        public void dragOver(DragSourceDragEvent dragSourceDragEvent) {
        }

        @Override
        public void dropActionChanged(DragSourceDragEvent dragSourceDragEvent) {
        }

        @Override
        public void dragMouseMoved(DragSourceDragEvent dragSourceDragEvent) {
        }
    }

    public AbstractAction copyAction = new AbstractAction() {

        /**
         * 
         */
        private static final long serialVersionUID = 4967996026448276641L;

        {
            final String name = Messages.getString("copy"); //$NON-NLS-1$
            putValue(Action.NAME, name);
            putValue(Action.SHORT_DESCRIPTION, name);
            putValue(Action.LARGE_ICON_KEY, dragIcon);
            setEnabled(false);
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, Application.getMetaOrControl()));
            putValue(Action.SMALL_ICON, SwiftConfigurationEditor.loadIcon("copy", name)); //$NON-NLS-1$
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            clipboard.setContents(createTransferable(), JTransferableTree.this);
        }
    };

    public AbstractAction pasteAction = new AbstractAction() {

        /**
         * 
         */
        private static final long serialVersionUID = 3901654998468005146L;

        {
            final String name = Messages.getString("paste");//$NON-NLS-1$
            putValue(Action.NAME, name);
            putValue(Action.SHORT_DESCRIPTION, name);
            putValue(Action.LARGE_ICON_KEY, dragIcon);
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, Application.getMetaOrControl()));
            putValue(Action.SMALL_ICON, SwiftConfigurationEditor.loadIcon("paste", name)); //$NON-NLS-1$
            // setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final int numRows = getSelectedRowCount();
            if (numRows != 1) {
                // Nothing selected, nothing to drag
                getToolkit().beep();
                return;
            } else {
                int row = getSelectedRow();

                VirtualFile f = (VirtualFile) getValueAt(row, 0);
                try {
                    doPaste(f, clipboard.getContents(this), null);
                } catch (IOException err) {
                    LOG.log(Level.WARNING, err.getClass().getSimpleName() + ": " + err.getLocalizedMessage(), err); //$NON-NLS-1$
                } catch (UnsupportedFlavorException err) {
                    LOG.log(Level.WARNING, err.getClass().getSimpleName() + ": " + err.getLocalizedMessage(), err); //$NON-NLS-1$
                }
            }
        }
    };

    /**
     * get the pasteAction
     * 
     * @return the pasteAction
     */
    public AbstractAction getPasteAction() {
        return pasteAction;
    }

    /**
     * get the copyAction
     * 
     * @return the copyAction
     */
    public AbstractAction getCopyAction() {
        return copyAction;
    }

    private final static Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    private final static ImageIcon dragIcon = SwiftConfigurationEditor.loadIconFromFullPath("/net/souchay/swift/gui/dnd/_blank.png", //$NON-NLS-1$
                                                                                            "DnD"); //$NON-NLS-1$

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        int numRows = getSelectedRowCount();
        copyAction.setEnabled(numRows > 0);
        pasteAction.setEnabled(numRows == 1);
    }
}
