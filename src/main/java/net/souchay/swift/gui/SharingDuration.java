/**
 * $LastChangedBy: souchay $ - $LastChangedDate: 2014-07-07 12:12:06 +0200 (Lun 07 jul 2014) $
 */
package net.souchay.swift.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

/**
 * @author Pierre Souchay <pierre@souchay.net> (last changed by $LastChangedBy: souchay $)
 * @version $Revision: 3835 $
 * 
 */
public class SharingDuration {

    private static Map<String, Long> defaultDurations = new Hashtable<String, Long>();

    private static final Vector<Long> durations = new Vector<Long>();

    public final static String MAIL_SHARING_DEFAULT = "mail.download"; //$NON-NLS-1$

    public final static String MAIL_UPLOAD_SHARING_DEFAULT = "mail.upload"; //$NON-NLS-1$

    public final static long HOUR_DURATION = 3600000;

    public final static long DAY_DURATION = HOUR_DURATION * 24;

    public final static long WEEK_DURATION = DAY_DURATION * 7;

    public final static long MONTH_DURATION = DAY_DURATION * 31;

    static {
        Long[] values = new Long[] { 30 * 60000L, HOUR_DURATION, 6 * HOUR_DURATION, 12 * HOUR_DURATION, DAY_DURATION,
                                    2 * DAY_DURATION, 3 * DAY_DURATION, 5 * DAY_DURATION, WEEK_DURATION,
                                    2 * WEEK_DURATION, MONTH_DURATION, 3 * MONTH_DURATION, 6 * MONTH_DURATION,
                                    12 * MONTH_DURATION };
        for (Long v : values) {
            durations.add(v);
        }
        defaultDurations.put(MAIL_SHARING_DEFAULT, MONTH_DURATION);
        defaultDurations.put(MAIL_UPLOAD_SHARING_DEFAULT, WEEK_DURATION);
    }

    public static final String computeDurationAsString(long now, long offet) {
        final Date endTime = new Date(now + offet);
        if (offet < HOUR_DURATION) {
            return Messages.getString("sharingDurationInMinutes", endTime, offet / 60000L); //$NON-NLS-1$
        } else if (offet < (DAY_DURATION)) {
            return Messages.getString("sharingDurationInHours", endTime, offet / HOUR_DURATION); //$NON-NLS-1$
        } else if (offet < (WEEK_DURATION)) {
            return Messages.getString("sharingDurationInDays", endTime, offet / DAY_DURATION); //$NON-NLS-1$
        } else if (offet < (MONTH_DURATION)) {
            return Messages.getString("sharingDurationInWeeks", endTime, offet / WEEK_DURATION);//$NON-NLS-1$
        } else {
            return Messages.getString("sharingDurationInMonths", endTime, offet / MONTH_DURATION);//$NON-NLS-1$
        }
    }

    public static Long getExpirationDate(Component parentComponent, String type, final long now,
            JComponent southAdditionalComponent, Icon icon) {
        JPanel jp = new JPanel(new BorderLayout(5, 5));
        String durationTypeString = Messages.getString(type);
        jp.add(new JLabel(Messages.getString("sharingForHowLong", durationTypeString)), BorderLayout.NORTH); //$NON-NLS-1$

        JList<Long> availableDurations = new JList<Long>(durations);
        availableDurations.setSelectedValue(defaultDurations.get(type), true);
        availableDurations.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jp.add(availableDurations);
        if (southAdditionalComponent != null) {
            jp.add(southAdditionalComponent, BorderLayout.SOUTH);
        }
        availableDurations.setCellRenderer(new DefaultListCellRenderer() {

            /**
             * 
             */
            private static final long serialVersionUID = -2960500813595091095L;

            /**
             * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList,
             *      java.lang.Object, int, boolean, boolean)
             */
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                return super.getListCellRendererComponent(list,
                                                          value == null || !(value instanceof Long) ? null : computeDurationAsString(now,
                                                                                                                                     (Long) value),
                                                          index,
                                                          isSelected,
                                                          cellHasFocus);
            }

        });
        int option = JOptionPane.showConfirmDialog(parentComponent, jp, Messages.getString("sharingPreferencesTitle"), //$NON-NLS-1$
                                                   JOptionPane.OK_CANCEL_OPTION,
                                                   JOptionPane.QUESTION_MESSAGE,
                                                   icon);
        if (option != JOptionPane.OK_OPTION)
            return null;
        Long val = availableDurations.getSelectedValue();
        if (val != null)
            defaultDurations.put(type, val);
        return val;
    }
}
