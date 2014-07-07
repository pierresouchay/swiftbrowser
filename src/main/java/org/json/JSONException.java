package org.json;

/**
 * The JSONException is thrown by the JSON.org classes when things are amiss.
 * 
 * @author Pierre Souchay <pierre@souchay.net> $LastChangedBy: souchay $
 * @version 2008-09-18
 */
public class JSONException extends Exception {

    /**
	 * 
	 */
    private static final long serialVersionUID = 0;

    /**
     * Constructs a JSONException with an explanatory message.
     * 
     * @param message Detail about the reason for the exception.
     */
    public JSONException(String message) {
        super(message);
    }

    /**
     * Constructor
     * 
     * @param t
     */
    public JSONException(Throwable t) {
        super(t.getMessage(), t);
    }
}
