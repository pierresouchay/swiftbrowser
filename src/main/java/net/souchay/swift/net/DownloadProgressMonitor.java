package net.souchay.swift.net;

/**
 * 
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3830 $
 * 
 */
public interface DownloadProgressMonitor {

    /**
     * Get the message
     * 
     * @return
     */
    public String getMessage();

    /**
     * Close
     */
    public void close();

    /**
     * Set maximum value
     * 
     * @param max the max to set
     */
    public void setMaximum(int max);

    /**
     * Set the note
     * 
     * @param note
     */
    public void setNote(String note);

    /**
     * Set the progress
     * 
     * @param progress
     */
    public void setProgress(int progress);

    /**
     * true if canceled
     * 
     * @return
     */
    public boolean isCanceled();

    /**
     * Cancels
     */
    public void cancel();
}
