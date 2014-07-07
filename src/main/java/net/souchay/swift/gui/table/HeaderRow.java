package net.souchay.swift.gui.table;

/**
 * 
 * @author Pierre Souchay <pierre@souchay.net> (last changed by $LastChangedBy: souchay $)
 * @version $Revision: 3830 $
 * 
 */
public class HeaderRow implements Comparable<HeaderRow> {

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(HeaderRow o) {
        return headerDisplayName.compareTo(o.headerDisplayName);
    }

    private final String headerName;

    private final String headerDisplayName;

    private volatile String headerValue;

    private final String initialValue;

    /**
     * get the modified
     * 
     * @return the modified
     */
    public boolean isModified() {
        return !initialValue.equals(headerValue);
    }

    /**
     * get the headerValue
     * 
     * @return the headerValue
     */
    public String getHeaderValue() {
        return headerValue;
    }

    /**
     * Set the headerValue
     * 
     * @param headerValue the headerValue to set
     */
    public void setHeaderValue(String headerValue) {
        this.headerValue = headerValue;
    }

    /**
     * get the headerName
     * 
     * @return the headerName
     */
    public String getHeaderName() {
        return headerName;
    }

    /**
     * get the initialValue
     * 
     * @return the initialValue
     */
    public String getInitialValue() {
        return initialValue;
    }

    /**
     * get the headerDisplayName
     * 
     * @return the headerDisplayName
     */
    public String getHeaderDisplayName() {
        return headerDisplayName;
    }

    /**
     * get the isEditable
     * 
     * @return the isEditable
     */
    public boolean isEditable() {
        return isEditable;
    }

    private final boolean isEditable;

    public HeaderRow(String headerName, String headerValue, boolean isEditable) {
        super();
        this.headerName = headerName;
        this.headerDisplayName = Messages.getStringIfOk(headerName);
        this.headerValue = headerValue;
        this.initialValue = headerValue;
        this.isEditable = isEditable;
    }
}
