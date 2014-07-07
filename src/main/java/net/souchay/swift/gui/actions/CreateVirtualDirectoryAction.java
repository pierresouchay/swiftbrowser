package net.souchay.swift.gui.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.tree.TreePath;
import net.souchay.swift.gui.EmptyInputStream;
import net.souchay.swift.gui.Messages;
import net.souchay.swift.gui.SwiftToVirtualFiles;
import net.souchay.swift.gui.VirtualFile;
import net.souchay.swift.gui.VirtualFileAction;
import net.souchay.swift.gui.dnd.JTransferableTree;
import net.souchay.swift.net.DefaultSwiftConnectionResult;
import net.souchay.swift.net.FsConnection;
import net.souchay.swift.net.SwiftConnections;
import org.jdesktop.swingx.JXFrame;

public class CreateVirtualDirectoryAction extends VirtualFileAction {

    private final DefaultSwiftConnectionResult fileHandler;

    private final SwiftConnections conn;

    private final SwiftToVirtualFiles guiBindings;

    private final JTransferableTree table;

    private final JXFrame jf;

    /**
     * 
     */
    private static final long serialVersionUID = -2082716046190289234L;
    {
        final String name = Messages.getString("createVirtualDirectory"); //$NON-NLS-1$
        putValue(Action.NAME, name);
        putValue(Action.SHORT_DESCRIPTION, name);
        putValue(Action.SMALL_ICON, UIManager.getIcon("FileChooser.newFolderIcon")); //$NON-NLS-1$
        setEnabled(false);
    }

    public CreateVirtualDirectoryAction(DefaultSwiftConnectionResult fileHandler, SwiftConnections conn,
            SwiftToVirtualFiles guiBindings, JTransferableTree table, JXFrame jf) {
        this.fileHandler = fileHandler;
        this.conn = conn;
        this.guiBindings = guiBindings;
        this.table = table;
        this.jf = jf;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        VirtualFile fx0 = (VirtualFile) table.getValueAt(table.getSelectedRow(), 0);
        if (!fx0.isDirectory())
            fx0 = fx0.getParent();
        final VirtualFile fx = fx0;
        String value = JOptionPane.showInputDialog(jf, Messages.getString("pleaseEnterDirectoryName"), //$NON-NLS-1$
                                                   Messages.getString("exampleDirectoryName", fx.getUnixPathWithoutContainer())); //$NON-NLS-1$
        if (value != null && !value.trim().isEmpty()) {
            value = value.trim();
            final String dirName = value.trim();
            // = value;
            final VirtualFile newFile = guiBindings.createNewDirectory(fx, dirName);

            Thread t = new Thread() {

                @Override
                public void run() {
                    Map<String, String> headers = new HashMap<String, String>();
                    headers.put(FsConnection.CONTENT_TYPE, VirtualFile.INODE_DIRECTORY_MIME_TYPE);
                    EmptyInputStream in = new EmptyInputStream();
                    try {
                        conn.put(fileHandler,
                                 newFile.getContainerName(),
                                 0,
                                 newFile.getUnixPathWithoutContainer() + VirtualFile.VIRTUAL_FILE_SEPARATOR,
                                 in,
                                 headers);
                    } catch (IOException err) {
                        err.printStackTrace();
                    } finally {
                        in.close();
                    }
                }
            };
            t.setDaemon(true);
            t.start();

            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    TreePath path = newFile.getPath();
                    table.expandPath(path);
                    table.scrollPathToVisible(path);
                    table.getTreeSelectionModel().setSelectionPath(path);
                }
            });

        }
    }

    @Override
    public void enable(VirtualFile... selectedPaths) {
        setEnabled(selectedPaths.length == 1 && selectedPaths[0].isDirectory());

    }

}
