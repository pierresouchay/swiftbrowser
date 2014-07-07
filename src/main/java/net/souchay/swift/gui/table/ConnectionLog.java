package net.souchay.swift.gui.table;

import java.net.URL;
import java.util.Date;

/**
 * Connection logs
 * 
 * @copyright Pierre Souchay - 2013,2014
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version $Revision: 3830 $
 * 
 */
public class ConnectionLog {

    private final String method;

    private int httpResponseCode;

    private Throwable error;

    private long len;

    private String msg;

    private final URL url;

    private long latency;

    private Date startTime;

    /**
     * Constructor
     * 
     * @param method
     * @param httpResponseCode
     * @param url
     * @param startTime
     */
    public ConnectionLog(String method, URL url, Date startTime) {
        super();
        this.method = method;
        this.url = url;
        this.startTime = startTime;
    }

    public int getHttpResponseCode() {
        return httpResponseCode;
    }

    public void setHttpResponseCode(int httpResponseCode) {
        this.httpResponseCode = httpResponseCode;
    }

    public Throwable getError() {
        return error;
    }

    public String getMsg() {
        return msg;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public long getLatency() {
        return latency;
    }

    public void updateWithError(Throwable t) {
        updateWithSuccess(-1, 0, t.getClass().getSimpleName() + ": " + t.getLocalizedMessage()); //$NON-NLS-1$
        this.error = t;
    }

    /**
     * Called when success
     * 
     * @param httpResponseCode
     * @param len
     * @param msg
     */
    public void updateWithSuccess(int httpResponseCode, long len, String msg) {
        this.latency = System.currentTimeMillis() - startTime.getTime();
        this.error = null;
        this.len = len;
        this.msg = msg;
        this.httpResponseCode = httpResponseCode;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getMethod() {
        return method;
    }

    public long getLen() {
        return len;
    }

    public URL getUrl() {
        return url;
    }
}
