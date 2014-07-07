package net.souchay.swift.gui.dnd;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import net.souchay.swift.gui.Messages;
import net.souchay.swift.gui.configuration.SwiftConfigurationEditor;
import net.souchay.swift.gui.dnd.FileTreeDragSource.FileTreeNode;

public class FileTreeDragSource implements DragGestureListener, DragSourceListener {

    /**
     * File Tree Node
     * 
     * @copyright Pierre Souchay - 2013,2014
     * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
     * @version $Revision: 3836 $
     * 
     */
    public static class FileTreeNode extends DefaultMutableTreeNode {

        /**
         * 
         */
        private static final long serialVersionUID = -7952589646189512156L;

        private final boolean showHiddenFiles;

        public FileTreeNode(String parent, String name, boolean showHiddenFiles) throws SecurityException,
                FileNotFoundException {
            this.name = name;
            this.showHiddenFiles = showHiddenFiles;
            // See if this node exists and whether it is a directory
            fullName = parent == null ? name : parent + File.separator + name;

            File f = new File(fullName);
            if (f.exists() == false) {
                throw new FileNotFoundException("File " + fullName + " does not exist"); //$NON-NLS-1$//$NON-NLS-2$
            }

            isDir = f.isDirectory();

            // Hack for Windows which doesn't consider a drive to be a
            // directory!
            if (isDir == false && f.isFile() == false) {
                isDir = true;
            }
        }

        // Override isLeaf to check whether this is a directory
        @Override
        public boolean isLeaf() {
            return !isDir;
        }

        // Override getAllowsChildren to check whether this is a directory
        @Override
        public boolean getAllowsChildren() {
            return isDir;
        }

        // Return whether this is a directory
        public boolean isDir() {
            return isDir;
        }

        // Get full path
        public String getFullName() {
            return fullName;
        }

        // For display purposes, we return our own name
        @Override
        public String toString() {
            return name;
        }

        // If we are a directory, scan our contents and populate
        // with children. In addition, populate those children
        // if the "descend" flag is true. We only descend once,
        // to avoid recursing the whole subtree.
        // Returns true if some nodes were added
        boolean populateDirectories(boolean descend) {
            boolean addedNodes = false;

            // Do this only once
            if (populated == false) {
                File f;
                try {
                    f = new File(fullName);
                } catch (SecurityException e) {
                    populated = true;
                    return false;
                }

                if (interim == true) {
                    // We have had a quick look here before:
                    // remove the dummy node that we added last time
                    removeAllChildren();
                    interim = false;
                }

                String[] names = f.list(); // Get list of contents

                // Process the contents
                ArrayList<FileTreeNode> list = new ArrayList<FileTreeNode>(name.length());
                for (int i = 0; i < names.length; i++) {
                    String name = names[i];
                    File d = new File(fullName, name);
                    if (d.isHidden() == showHiddenFiles) {
                        try {
                            FileTreeNode node = new FileTreeNode(fullName, name, showHiddenFiles);
                            list.add(node);
                            if (descend && d.isDirectory()) {
                                node.populateDirectories(false);
                            }
                            addedNodes = true;
                            if (descend == false) {
                                // Only add one node if not descending
                                break;
                            }
                        } catch (Throwable t) {
                            // Ignore phantoms or access problems
                        }
                    }
                }

                if (addedNodes == true) {
                    // Now sort the list of contained files and directories
                    FileTreeNode[] nodes = list.toArray(new FileTreeNode[0]);
                    Arrays.sort(nodes, new Comparator<FileTreeNode>() {

                        @Override
                        public int hashCode() {
                            return fullName.hashCode();
                        }

                        @Override
                        public boolean equals(Object o) {
                            if (o == this)
                                return true;
                            if (!(o instanceof FileTreeNode))
                                return false;
                            FileTreeNode other = (FileTreeNode) o;
                            return fullName.equals(other.fullName);
                        }

                        @Override
                        public int compare(FileTreeNode node1, FileTreeNode node2) {

                            // Directories come first
                            if (node1.isDir != node2.isDir) {
                                return node1.isDir ? -1 : +1;
                            }

                            // Both directories or both files -
                            // compare based on pathname
                            return node1.fullName.compareTo(node2.fullName);
                        }
                    });

                    // Add sorted items as children of this node
                    for (int j = 0; j < nodes.length; j++) {
                        this.add(nodes[j]);
                    }
                }

                // If we were scanning to get all subdirectories,
                // or if we found no content, there is no
                // reason to look at this directory again, so
                // set populated to true. Otherwise, we set interim
                // so that we look again in the future if we need to
                if (descend == true || addedNodes == false) {
                    populated = true;
                } else {
                    // Just set interim state
                    interim = true;
                }
            }
            return addedNodes;
        }

        public FileTreeNode getNode(String name) {
            for (Object n : children) {
                FileTreeNode fn = (FileTreeNode) n;
                if (name.equals(fn.name))
                    return fn;
            }
            return null;
        }

        // Adding a new file or directory after
        // constructing the FileTree. Returns
        // the index of the inserted node.
        public int addNode(String name) {
            // If not populated yet, do nothing
            if (populated == true) {
                // Do not add a new node if
                // the required node is already there
                int childCount = getChildCount();
                for (int i = 0; i < childCount; i++) {
                    FileTreeNode node = (FileTreeNode) getChildAt(i);
                    if (node.name.equals(name)) {
                        // Already exists - ensure
                        // we repopulate
                        if (node.isDir()) {
                            node.interim = true;
                            node.populated = false;
                        }
                        return -1;
                    }
                }

                // Add a new node
                try {
                    FileTreeNode node = new FileTreeNode(fullName, name, showHiddenFiles);
                    add(node);
                    return childCount;
                } catch (Exception e) {
                }
            }
            return -1;
        }

        protected String name; // Name of this component

        protected String fullName; // Full pathname

        protected boolean populated;// true if we have been populated

        protected boolean interim; // true if we are in interim state

        protected boolean isDir; // true if this is a directory
    }

    Action refreshAction = new AbstractAction() {

        /**
         * 
         */
        private static final long serialVersionUID = -145795151618667170L;

        {
            final String name = Messages.getString("refresh"); //$NON-NLS-1$
            putValue(Action.NAME, name);
            putValue(Action.SHORT_DESCRIPTION, name);
            putValue(Action.SMALL_ICON, SwiftConfigurationEditor.loadIcon("refresh", "refresh")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            tree.refresh();
        }
    };

    /**
     * get the refreshAction
     * 
     * @return the refreshAction
     */
    public Action getRefreshAction() {
        return refreshAction;
    }

    public FileTreeDragSource(String home, boolean showHiddenFiles) {
        try {
            this.tree = new FileTree(home, showHiddenFiles);
        } catch (FileNotFoundException err) {
            err.printStackTrace();
        }

        // Use the default DragSource
        DragSource dragSource = DragSource.getDefaultDragSource();

        // Create a DragGestureRecognizer and
        // register as the listener
        dragSource.createDefaultDragGestureRecognizer(tree, DnDConstants.ACTION_COPY_OR_MOVE, this);
    }

    /**
     * get the tree
     * 
     * @return the tree
     */
    public FileTree getTree() {
        return tree;
    }

    // Implementation of DragGestureListener interface.
    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
        // Get the mouse location and convert it to
        // a location within the tree.
        Point location = dge.getDragOrigin();
        TreePath dragPath = tree.getPathForLocation(location.x, location.y);
        if (dragPath != null && tree.isPathSelected(dragPath)) {
            // Get the list of selected files and create a Transferable
            // The list of files and the is saved for use when
            // the drop completes.
            paths = tree.getSelectionPaths();
            if (paths != null && paths.length > 0) {
                dragFiles = new File[paths.length];
                for (int i = 0; i < paths.length; i++) {
                    String pathName = tree.getPathName(paths[i]);
                    dragFiles[i] = new File(pathName);
                }
                Transferable transferable = new FileListTransferable(dragFiles);
                dge.startDrag(null, transferable, this);
            }
        }
    }

    // Implementation of DragSourceListener interface
    @Override
    public void dragEnter(DragSourceDragEvent dsde) {
    }

    @Override
    public void dragOver(DragSourceDragEvent dsde) {
    }

    @Override
    public void dragExit(DragSourceEvent dse) {
    }

    @Override
    public void dropActionChanged(DragSourceDragEvent dsde) {
    }

    @Override
    public void dragDropEnd(DragSourceDropEvent dsde) {
        // If the drop action was ACTION_MOVE,
        // the tree might need to be updated.
        if (dsde.getDropAction() == DnDConstants.ACTION_MOVE) {
            final File[] draggedFiles = dragFiles;
            final TreePath[] draggedPaths = paths;

            Timer tm = new Timer(200, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent evt) {
                    // Check whether each of the dragged files exists.
                    // If it does not, we need to remove the node
                    // that represents it from the tree.
                    for (int i = 0; i < draggedFiles.length; i++) {
                        if (draggedFiles[i].exists() == false) {
                            // Remove this node
                            DefaultMutableTreeNode node = (DefaultMutableTreeNode) draggedPaths[i].getLastPathComponent();
                            ((DefaultTreeModel) tree.getModel()).removeNodeFromParent(node);
                        }
                    }
                }
            });
            tm.setRepeats(false);
            tm.start();
        }
    }

    protected FileTree tree; // The associated tree

    protected File[] dragFiles; // Dragged files

    protected TreePath[] paths; // Dragged paths
}

/**
 * FileTree implementation
 * 
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3836 $
 * 
 */
class FileTree extends JTree implements Autoscroll {

    public void refresh() {
        final Rectangle r = getVisibleRect();
        Enumeration<TreePath> expandedElements = getExpandedDescendants(getPathForRow(0));
        // Create the first node
        // FileTreeNode rootNode;
        try {
            final FileTreeNode rootNode = new FileTreeNode(null, rootPath, false);
            // Populate the root node with its subdirectories
            boolean addedNodes = rootNode.populateDirectories(true);
            DefaultTreeModel model = (DefaultTreeModel) getModel();
            model.setRoot(rootNode);
            if (expandedElements != null) {
                collapsePath(getPathForRow(0));
                while (expandedElements.hasMoreElements()) {
                    final TreePath t = expandedElements.nextElement();
                    // SwingUtilities.invokeLater(new Runnable() {
                    //
                    // @Override
                    // public void run() {
                    FileTreeNode n = rootNode;
                    int i = 0;
                    Object toExpand[] = new Object[t.getPath().length];
                    for (Object s : t.getPath()) {
                        if (i > 0) {
                            n.populateDirectories(true);
                            FileTreeNode nx = n.getNode(String.valueOf(s));
                            if (nx == null)
                                break;
                            n = nx;
                        }
                        toExpand[i++] = n;
                    }
                    expandPath(new TreePath(toExpand));
                    // }
                    //
                    // });

                }
            }
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    scrollRectToVisible(r);
                }

            });
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * 
     */
    private static final long serialVersionUID = 3865363361061907445L;

    public static final Insets defaultScrollInsets = new Insets(8, 8, 8, 8);

    protected Insets scrollInsets = defaultScrollInsets;

    private final String rootPath;

    public FileTree(String path, boolean showHiddenFiles) throws FileNotFoundException, SecurityException {
        super((TreeModel) null); // Create the JTree itself
        rootPath = path;
        // Use horizontal and vertical lines
        putClientProperty("JTree.lineStyle", "Angled"); //$NON-NLS-1$ //$NON-NLS-2$

        // Create the first node
        FileTreeNode rootNode = new FileTreeNode(null, path, showHiddenFiles);

        // Populate the root node with its subdirectories
        boolean addedNodes = rootNode.populateDirectories(true);
        setModel(new DefaultTreeModel(rootNode));

        // Listen for Tree Selection Events
        addTreeExpansionListener(new TreeExpansionHandler());
    }

    // Returns the full pathname for a path, or null if not a known path
    public String getPathName(TreePath path) {
        Object o = path.getLastPathComponent();
        if (o instanceof FileTreeNode) {
            return ((FileTreeNode) o).fullName;
        }
        return null;
    }

    // Adds a new node to the tree after construction.
    // Returns the inserted node, or null if the parent
    // directory has not been expanded.
    public FileTreeNode addNode(FileTreeNode parent, String name) {
        int index = parent.addNode(name);
        if (index != -1) {
            ((DefaultTreeModel) getModel()).nodesWereInserted(parent, new int[] { index });
            return (FileTreeNode) parent.getChildAt(index);
        }

        // No node was created
        return null;
    }

    // Autoscrolling support
    public void setScrollInsets(Insets insets) {
        this.scrollInsets = insets;
    }

    public Insets getScrollInsets() {
        return scrollInsets;
    }

    // Implementation of Autoscroll interface
    @Override
    public Insets getAutoscrollInsets() {
        Rectangle r = getVisibleRect();
        Dimension size = getSize();
        Insets i = new Insets(r.y + scrollInsets.top,
                              r.x + scrollInsets.left,
                              size.height - r.y - r.height + scrollInsets.bottom,
                              size.width - r.x - r.width + scrollInsets.right);
        return i;
    }

    @Override
    public void autoscroll(Point location) {
        JScrollPane scroller = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
        if (scroller != null) {
            JScrollBar hBar = scroller.getHorizontalScrollBar();
            JScrollBar vBar = scroller.getVerticalScrollBar();
            Rectangle r = getVisibleRect();
            if (location.x <= r.x + scrollInsets.left) {
                // Need to scroll left
                hBar.setValue(hBar.getValue() - hBar.getUnitIncrement(-1));
            }
            if (location.y <= r.y + scrollInsets.top) {
                // Need to scroll up
                vBar.setValue(vBar.getValue() - vBar.getUnitIncrement(-1));
            }
            if (location.x >= r.x + r.width - scrollInsets.right) {
                // Need to scroll right
                hBar.setValue(hBar.getValue() + hBar.getUnitIncrement(1));
            }
            if (location.y >= r.y + r.height - scrollInsets.bottom) {
                // Need to scroll down
                vBar.setValue(vBar.getValue() + vBar.getUnitIncrement(1));
            }
        }
    }

    // Inner class that represents a node in this file system tree

    // Inner class that handles Tree Expansion Events
    protected class TreeExpansionHandler implements TreeExpansionListener {

        @Override
        public void treeExpanded(TreeExpansionEvent evt) {
            TreePath path = evt.getPath(); // The expanded path
            JTree tree = (JTree) evt.getSource(); // The tree

            // Get the last component of the path and
            // arrange to have it fully populated.
            FileTreeNode node = (FileTreeNode) path.getLastPathComponent();
            if (node.populateDirectories(true)) {
                ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(node);
            }
        }

        @Override
        public void treeCollapsed(TreeExpansionEvent evt) {
            // Nothing to do
        }
    }
}

class FileListTransferable implements Transferable {

    public FileListTransferable(File[] files) {
        fileList = new ArrayList<File>();
        for (int i = 0; i < files.length; i++) {
            fileList.add(files[i]);
        }
    }

    // Implementation of the Transferable interface
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { DataFlavor.javaFileListFlavor };
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor fl) {
        return fl.equals(DataFlavor.javaFileListFlavor);
    }

    @Override
    public Object getTransferData(DataFlavor fl) {
        if (!isDataFlavorSupported(fl)) {
            return null;
        }

        return fileList;
    }

    List<File> fileList; // The list of files
}

@SuppressWarnings("nls")
class DnDUtils {

    public static String showActions(int action) {
        String actions = ""; //$NON-NLS-1$
        if ((action & (DnDConstants.ACTION_LINK | DnDConstants.ACTION_COPY_OR_MOVE)) == 0) {
            return "None"; //$NON-NLS-1$
        }

        if ((action & DnDConstants.ACTION_COPY) != 0) {
            actions += "Copy ";
        }

        if ((action & DnDConstants.ACTION_MOVE) != 0) {
            actions += "Move ";
        }

        if ((action & DnDConstants.ACTION_LINK) != 0) {
            actions += "Link";
        }

        return actions;
    }
}
