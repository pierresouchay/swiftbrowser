/**
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-07-07 12:12:06 +0200 (Lun 07 jul 2014) $
 */
package net.souchay.swift.gui.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.tree.TreePath;
import net.souchay.swift.gui.Messages;
import net.souchay.swift.gui.SwiftConnectionsDownload;
import net.souchay.swift.gui.VirtualFile;
import net.souchay.swift.gui.VirtualFileAction;
import net.souchay.swift.gui.configuration.SwiftConfigurationEditor;
import net.souchay.swift.gui.dnd.JTransferableTree;

/**
 * @author Pierre Souchay <pierre@souchay.net> (last changed by $LastChangedBy: souchay $)
 * @version $Revision: 3835 $
 * 
 */
public class SaveAsAction extends VirtualFileAction {

    private final SwiftConnectionsDownload importListener;

    private final JTransferableTree table;

    /**
     * 
     */
    private static final long serialVersionUID = -5835962952154325307L;
    {
        final String name = Messages.getString("saveAs"); //$NON-NLS-1$
        putValue(Action.NAME, name);
        putValue(Action.SHORT_DESCRIPTION, name);
        setEnabled(false);
        putValue(Action.SMALL_ICON, SwiftConfigurationEditor.loadIcon("saveas", name)); //$NON-NLS-1$
    }

    private File currentDir = new File(System.getProperty("user.home")); //$NON-NLS-1$

    /**
     * Constructor
     * 
     * @param importListener
     * @param table
     */
    public SaveAsAction(SwiftConnectionsDownload importListener, JTransferableTree table) {
        this.importListener = importListener;
        this.table = table;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        setEnabled(false);
        JFileChooser fileChooser = new JFileChooser(currentDir);
        // fileChooser.setFileFilter(filter);
        TreePath p = table.getTreeSelectionModel().getLeadSelectionPath();
        VirtualFile n = (VirtualFile) p.getLastPathComponent();
        if (n.isDirectory()) {
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setSelectedFile(new File(currentDir, n.getName()));
        } else {
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setSelectedFile(new File(currentDir, n.getName()));
        }

        if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(table)) {

            final File file = fileChooser.getSelectedFile();
            if (file.isDirectory()) {
                currentDir = file;
            } else {
                currentDir = file.getParentFile();
            }
            Thread t = new Thread("SaveAsThread") { //$NON-NLS-1$

                @Override
                public void run() {
                    final List<Future<File>> results = new LinkedList<Future<File>>();
                    try {
                        if (file.isDirectory()) {

                            for (int row : table.getSelectedRows()) {
                                VirtualFile fx = (VirtualFile) table.getValueAt(row, 0);
                                results.addAll(importListener.saveAs(fx, new File(file, fx.getName())));
                            }
                        } else {
                            for (int row : table.getSelectedRows()) {
                                VirtualFile fx = (VirtualFile) table.getValueAt(row, 0);
                                results.addAll(importListener.saveAs(fx, file));
                            }
                        }
                    } finally {
                        setEnabled(true);
                    }
                }
            };
            t.setDaemon(true);
            t.start();
        }
    }

    @Override
    public void enable(VirtualFile... selectedPaths) {
        setEnabled(selectedPaths.length == 1);
    }
}