package net.souchay.swift.gui.table;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import net.souchay.swift.gui.PropertyChangeRegistration;
import net.souchay.swift.net.DownloadProgressMonitor;

public class ProgressMonitorResult implements DownloadProgressMonitor, PropertyChangeRegistration {

    /**
     * Constructor
     * 
     * @param msg
     * @param note
     * @param start
     * @param max
     */
    public ProgressMonitorResult(String msg, String note, int start, int max) {
        this.message = msg;
        this.note = note;
        this.progress = start;
        this.maximum = max;
    }

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    @Override
    public void close() {
        // noop
    }

    private final String message;

    /**
     * get the message
     * 
     * @return the message
     */
    @Override
    public String getMessage() {
        return message;
    }

    private int maximum = 100;

    private int progress = 0;

    /**
     * get the maximum
     * 
     * @return the maximum
     */
    public int getMaximum() {
        return maximum;
    }

    /**
     * get the progress
     * 
     * @return the progress
     */
    public int getProgress() {
        return progress;
    }

    /**
     * get the note
     * 
     * @return the note
     */
    public String getNote() {
        return note;
    }

    private String note = null;

    /**
     * Property fired
     */
    public final static String PROPERTY_NOTE = "note"; //$NON-NLS-1$

    /**
     * Property fired
     */
    public final static String PROPERTY_MAXIMUM = "maximum"; //$NON-NLS-1$

    /**
     * Property fired
     */
    public final static String PROPERTY_PROGRESS = "progress"; //$NON-NLS-1$

    /**
     * Property fired
     */
    public final static String PROPERTY_CANCELED = "canceled"; //$NON-NLS-1$

    public boolean canceled = false;

    @Override
    public void setMaximum(int max) {
        int oldValue = this.maximum;
        this.maximum = max;
        support.firePropertyChange(PROPERTY_MAXIMUM, oldValue, max);
    }

    @Override
    public void setNote(final String note) {
        final String oldValue = this.note;
        this.note = note;
        support.firePropertyChange(PROPERTY_NOTE, oldValue, note);
    }

    @Override
    public void setProgress(int progress) {
        final int oldValue = this.progress;
        this.progress = progress;
        support.firePropertyChange(PROPERTY_PROGRESS, oldValue, progress);
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        boolean oldValue = this.canceled;
        this.canceled = canceled;
        support.firePropertyChange(PROPERTY_CANCELED, oldValue, canceled);
    }

    @Override
    public void cancel() {
        setCanceled(true);
    }

}
