package net.souchay.swift.gui;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A simple ACL parser
 * 
 * @author Pierre Souchay <pierre@souchay.net> (last changed by $LastChangedBy: souchay $)
 * @version $Revision: 3830 $
 * 
 */
public class ACLParser {

    /**
     * Read for everyone
     */
    public static final String R_FOR_ALL = ".r:*"; //$NON-NLS-1$

    /**
     * Listings
     */
    public static final String R_LISTINGS = ".rlistings"; //$NON-NLS-1$

    /**
     * Constructor
     * 
     * @param originalData
     */
    public ACLParser(String originalData) {
        if (originalData == null || originalData.isEmpty()) {
            acls = Collections.emptyList();
        }
        acls = parseAcls(originalData);
    }

    /**
     * Return true if ACL is modified
     * 
     * @param acl
     * @return
     */
    public boolean appendACL(String acl) {
        acl = acl.trim();
        List<String> newAcls = new LinkedList<String>();
        for (String s : acls) {
            if (acl.equals(s))
                return false;
            newAcls.add(s);
        }
        newAcls.add(acl);
        acls = newAcls;
        return true;
    }

    public boolean containsACL(String aclToCheck) {
        for (String s : acls) {
            if (s.equals(aclToCheck))
                return true;
        }
        return false;
    }

    public boolean removeACL(String acl) {
        boolean found = false;
        acl = acl.trim();
        List<String> newAcls = new LinkedList<String>();
        for (String s : acls) {
            if (acl.equals(s))
                found = true;
            else
                newAcls.add(s);
        }
        if (found) {
            acls = newAcls;
            return true;
        } else {
            return false;
        }
    }

    private final static void appendAcl(StringBuilder sb, String toAppend) {
        if (sb.length() != 0) {
            sb.append(", "); //$NON-NLS-1$
        }
        sb.append(toAppend);
    }

    /**
     * Get the new ACL once applied
     * 
     * @param acl
     * @return
     */
    public String toggleACL(String acl) {
        boolean found = false;
        acl = acl.trim();
        StringBuilder sb = new StringBuilder();
        List<String> newAcls = new LinkedList<String>();
        for (String s : acls) {
            if (acl.equals(s)) {
                found = true;
            } else {
                appendAcl(sb, s);
                newAcls.add(s);
            }
        }
        if (!found) {
            appendAcl(sb, acl);
            newAcls.add(acl);
        }
        acls = newAcls;
        return sb.toString();
    }

    private List<String> acls;

    public final static String ACL_SEPARATOR = ","; //$NON-NLS-1$

    public void updateAllACLs(String data) {
        this.acls = parseAcls(data);
    }

    public static List<String> parseAcls(String data) {
        if (data == null || data.isEmpty())
            return Collections.emptyList();
        String split[] = data.trim().split(ACL_SEPARATOR);
        LinkedList<String> newAcls = new LinkedList<String>();
        for (String s : split) {
            String sx = s.trim();
            if (sx.contains(sx)) {
                newAcls.add(sx);
            }
        }
        return newAcls;
    }

    public String aclToString() {
        StringBuilder sb = new StringBuilder();
        for (String s : acls) {
            appendAcl(sb, s);
            sb.append(s.trim());
        }
        return sb.toString();
    }
}
