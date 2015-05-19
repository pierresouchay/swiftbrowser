package net.souchay.swift.gui.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import net.souchay.swift.gui.ContainerIFace.FileIFace;
import net.souchay.swift.gui.ContainerObject;
import net.souchay.swift.gui.ContainerVirtualFile;
import net.souchay.swift.gui.Messages;
import net.souchay.swift.gui.SwiftToVirtualFiles;
import net.souchay.swift.gui.VirtualFile;
import net.souchay.swift.gui.VirtualFileAction;
import net.souchay.swift.gui.configuration.SwiftConfigurationEditor;
import net.souchay.swift.gui.dnd.JTransferableTree;
import net.souchay.swift.net.DefaultSwiftConnectionResult;
import net.souchay.swift.net.SwiftConnections;

/**
 * 
 * @author Pierre Souchay <pierre@souchay.net> (last changed by $LastChangedBy: souchay $)
 * @version $Revision: 3830 $
 * 
 */
public class DeleteVirtualFileAction extends VirtualFileAction {

    private final static Logger LOG = java.util.logging.Logger.getLogger("swift.DeleteVirtualFileAction"); //$NON-NLS-1$

    private final ScheduledExecutorService executor;

    private final SwiftToVirtualFiles guiBindings;

    private final DefaultSwiftConnectionResult fileHandler;

    private final JTransferableTree table;

    private final SwiftConnections conn;

    /**
     * 
     */
    private static final long serialVersionUID = -8532124415472997944L;

    final String name = Messages.getString("delete"); //$NON-NLS-1$
    {
        setEnabled(false);
        putValue(Action.NAME, name);
        putValue(Action.SHORT_DESCRIPTION, name);
        putValue(Action.SMALL_ICON, SwiftConfigurationEditor.loadIcon("delete", name)); //$NON-NLS-1$
    }

    public DeleteVirtualFileAction(ScheduledExecutorService executor, SwiftToVirtualFiles guiBindings,
            DefaultSwiftConnectionResult fileHandler, JTransferableTree table, SwiftConnections conn) {
        this.executor = executor;
        this.guiBindings = guiBindings;
        this.fileHandler = fileHandler;
        this.table = table;
        this.conn = conn;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        int srow = table.getSelectedRow();
        if (srow >= 0) {
            final List<ContainerVirtualFile> containersToDelete = new LinkedList<ContainerVirtualFile>();
            final Set<VirtualFile> files = new HashSet<VirtualFile>();
            for (int row : table.getSelectedRows()) {
                final VirtualFile f = (VirtualFile) table.getValueAt(row, 0);
                f.getAllFileChildren(files);
                if (f instanceof ContainerVirtualFile) {
                    containersToDelete.add((ContainerVirtualFile) f);
                }
            }
            final String msg;
            if (files.size() < 0)
                return;
            else if (files.size() == 1) {
                final VirtualFile fx = files.iterator().next();
                msg = Messages.getString("deleteFile.confirm", //$NON-NLS-1$
                                         fx.getName());
            } else {
                msg = Messages.getString("deleteFile.confirmFiles", files.size() + containersToDelete.size()); //$NON-NLS-1$
            }

            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null,
                                                                        msg,
                                                                        Messages.getString("deleteFile.title"), //$NON-NLS-1$
                                                                        JOptionPane.YES_NO_OPTION)) {
                executor.execute(new Runnable() {

                    @Override
                    public void run() {
                        boolean noError = true;
                        for (VirtualFile fv : files) {
                            try {
                                FileIFace fx = fv.getFile();
                                conn.del(fileHandler, fx.getContainer().getName(), fx.getName());
                                ((ContainerObject) fx.getContainer()).deleteFile(fx);
                                // conn.list(fileHandler, fx.getContainer());
                            } catch (IOException e) {
                                LOG.log(Level.WARNING, "Failed to delete file " + fv.getName(), e); //$NON-NLS-1$
                                noError = false;
                                e.printStackTrace();
                            }
                        }
                        if (noError) {
                            for (ContainerVirtualFile f : containersToDelete) {
                                try {
                                    conn.del(fileHandler, f.getContainerName(), null);
                                    final ContainerVirtualFile fx = f;
                                    SwingUtilities.invokeLater(new Runnable() {

                                        @Override
                                        public void run() {
                                            guiBindings.deleteContainer(fx.getContainer());
                                        }

                                    });

                                } catch (IOException e) {
                                    noError = false;
                                    LOG.log(Level.WARNING, "Failed to delete container " + f.getName(), e); //$NON-NLS-1$
                                }

                            }
                        }
                    }
                });
            }

        }

    }

    @Override
    public void enable(VirtualFile... selectedPaths) {
        setEnabled(selectedPaths.length > 0);
    }

}
