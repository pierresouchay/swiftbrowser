package net.souchay.swift.gui.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.security.InvalidKeyException;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import net.souchay.swift.gui.ErrorsHandler;
import net.souchay.swift.gui.Messages;
import net.souchay.swift.gui.SharingDuration;
import net.souchay.swift.gui.VirtualFile;
import net.souchay.swift.gui.VirtualFileAction;
import net.souchay.swift.gui.configuration.SwiftConfigurationEditor;
import net.souchay.swift.gui.dnd.JTransferableTree;
import net.souchay.swift.net.SwiftConnections;
import net.souchay.utilities.URIOpen;
import net.souchay.utilities.URLParamEncoder;

/**
 * 
 * @author Pierre Souchay <pierre@souchay.net> (last changed by $LastChangedBy: souchay $)
 * @version $Revision: 3882 $
 * 
 */
public class ShareAction extends VirtualFileAction {

    private final JTransferableTree table;

    private final SwiftConnections conn;

    /**
     * 
     */
    private static final long serialVersionUID = -8532124415472997944L;

    private final String name = Messages.getString("share"); //$NON-NLS-1$

    private final ErrorsHandler errorsHandler;

    private final JCheckBox useSecondaryKey = new JCheckBox(Messages.getString("sharingKeyUseSecondaryKey"), true);//$NON-NLS-1$

    {
        useSecondaryKey.setToolTipText(Messages.getString("sharingKeyUseSecondaryKeyTooltip")); //$NON-NLS-1$
    }

    private final Icon shareIcon = SwiftConfigurationEditor.loadIcon("share", name);//$NON-NLS-1$
    {
        setEnabled(false);
        putValue(Action.NAME, name);
        putValue(Action.SHORT_DESCRIPTION, name);
        putValue(Action.SMALL_ICON, shareIcon);
    }

    /**
     * Constructor
     * 
     * @param errorsHandler
     * @param table
     * @param conn
     */
    public ShareAction(ErrorsHandler errorsHandler, JTransferableTree table, SwiftConnections conn) {
        this.table = table;
        this.conn = conn;
        this.errorsHandler = errorsHandler;
    }

    private void doShare(final long expires, StringBuilder sb, VirtualFile f, boolean useSecondaryKey) {
        if (f == null)
            return;
        if (!f.isDirectory()) {
            try {
                sb.append("\n - ") //$NON-NLS-1$
                  .append(conn.generateTempUrlWithExpirationInMs("GET", //$NON-NLS-1$ 
                                                                 expires,
                                                                 f,
                                                                 useSecondaryKey).toURL().toExternalForm())
                  //$NON-NLS-1$ //$NON-NLS-2$
                  .append('\n');
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (InvalidKeyException err) {
                errorsHandler.handleInvalidKeyException(table, err);
            }
        } else {
            for (VirtualFile child : f.getChildren()) {
                doShare(expires, sb, child, useSecondaryKey);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            int srow = table.getSelectedRow();
            if (srow > 0) {
                final long now = System.currentTimeMillis();
                Long duration = SharingDuration.getExpirationDate(table,
                                                                  SharingDuration.MAIL_SHARING_DEFAULT,
                                                                  now,
                                                                  useSecondaryKey,
                                                                  shareIcon);
                if (duration == null)
                    return;
                final long expires = now + duration.longValue();
                StringBuilder sb = new StringBuilder();
                sb.append(Messages.getString("share.main", SharingDuration.computeDurationAsString(now, duration))).append('\n'); //$NON-NLS-1$
                for (int row : table.getSelectedRows()) {
                    VirtualFile f = (VirtualFile) table.getValueAt(row, 0);
                    doShare(expires, sb, f, useSecondaryKey.isSelected());
                }
                sb.append("\n"); //$NON-NLS-1$
                URIOpen.mail(new URI("mailto:?subject=" //$NON-NLS-1$
                                     + URLParamEncoder.encode(Messages.getString("share.subject")) //$NON-NLS-1$
                                     + "&body=" + URLParamEncoder.encode(sb.toString()))); //$NON-NLS-1$
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    @Override
    public void enable(VirtualFile... selectedPaths) {
        setEnabled(selectedPaths.length > 0);
    }

}
