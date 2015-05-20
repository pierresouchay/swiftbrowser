package net.souchay.swift.gui.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import net.souchay.swift.gui.ContainerIFace.FileIFace;
import net.souchay.swift.gui.ContainerObject;
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
 * @version $Revision: 3922 $
 * 
 */
public class RenameVirtualFileAction extends VirtualFileAction {

    private final static Logger LOG = java.util.logging.Logger.getLogger("swift.RenameVirtualFileAction"); //$NON-NLS-1$

    private final ScheduledExecutorService executor;

    private final SwiftToVirtualFiles guiBindings;

    private final DefaultSwiftConnectionResult fileHandler;

    private final JTransferableTree table;

    private final SwiftConnections conn;

    /**
     * 
     */
    private static final long serialVersionUID = -8532124415472997944L;

    final String name = Messages.getString("rename"); //$NON-NLS-1$

    private final ImageIcon icon = SwiftConfigurationEditor.loadIcon("rename", name);//$NON-NLS-1$
    {
        setEnabled(false);
        putValue(Action.NAME, name);
        putValue(Action.SHORT_DESCRIPTION, name);
        putValue(Action.SMALL_ICON, icon);
    }

    public RenameVirtualFileAction(ScheduledExecutorService executor, SwiftToVirtualFiles guiBindings,
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
            final VirtualFile fx = (VirtualFile) table.getValueAt(srow, 0);
            final String newName = (String) JOptionPane.showInputDialog(null,
                                                                        Messages.getString("renameFile.message", fx.getName()), //$NON-NLS-1$
                                                                        Messages.getString("renameFile.title", fx.getName()), //$NON-NLS-1$
                                                                        JOptionPane.OK_CANCEL_OPTION,
                                                                        icon,
                                                                        null,
                                                                        fx.getUnixPathWithoutContainer());
            if (newName != null && !newName.trim().equals(fx.getUnixPathWithoutContainer())) {
                executor.execute(new Runnable() {

                    @Override
                    public void run() {
                        final String renameTo;
                        {
                            String _renameTo = newName.trim();
                            while (_renameTo.startsWith("/") && _renameTo.length() > 1) { //$NON-NLS-1$
                                _renameTo = _renameTo.substring(1);
                            }
                            renameTo = _renameTo;
                        }
                        final String containerName = fx.getContainerName().toString();
                        try {
                            conn.copy(fileHandler,
                                      containerName,
                                      fx.getUnixPathWithoutContainer(),
                                      containerName,
                                      renameTo);
                            SwingUtilities.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    ContainerObject container = (ContainerObject) fx.getContainer();
                                    FileIFace newFile = container.getOrCreateFile(renameTo, true);
                                    newFile.setContentType(fx.getFile().getContentType());
                                    newFile.setLastModified(fx.getLastModified());
                                    newFile.setSize(fx.getFile().getSize());
                                }
                            });
                            conn.del(fileHandler, fx.getContainer().getName(), fx.getUnixPathWithoutContainer());
                            SwingUtilities.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    guiBindings.onFilesRemoved(fx.getContainer(), Collections.singleton(fx.getFile()));
                                }
                            });
                        } catch (IOException err) {
                            LOG.log(Level.WARNING, "Failed to rename file: " + err.getMessage(), err); //$NON-NLS-1$
                        }
                    }
                });
            }

        }

    }

    @Override
    public void enable(VirtualFile... selectedPaths) {
        setEnabled(selectedPaths.length == 1 && !(selectedPaths[0].isDirectory()));
    }

}
