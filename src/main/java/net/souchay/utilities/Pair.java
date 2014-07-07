package net.souchay.utilities;

import java.io.Serializable;

/**
 * A generic representation of a pair.
 * 
 * @author souchay
 * @param <S> Left side element in pair object
 * @param <T> Right size element in Pair Object
 * 
 * 
 */
public final class Pair<S extends Comparable<S>, T> implements Serializable, Comparable<Pair<S, T>> {

    /**
     * SerialVersion UID.
     */
    private static final long serialVersionUID = -5761088187265175560L;

    /**
     * The left member of the pair
     */
    private final S left;

    /**
     * The right member of the pair
     */
    private final T right;

    /**
     * Creates a new {@link Pair} from replicating the members of the provided other pair.
     * 
     * @param otherPair
     * 
     */
    public Pair(Pair<S, T> otherPair) {
        this.left = otherPair.left;
        this.right = otherPair.right;
    }

    /**
     * Creates a new {@link Pair}.
     * 
     * @param left - left
     * @param right - right
     */
    public Pair(S left, T right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Two Pairs are the same if both arguments are equals.
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof Pair<?, ?>))
            return false;
        Pair<?, ?> pair = (Pair<?, ?>) obj;
        return safeEquals(pair.left, this.left) && safeEquals(pair.right, this.right);
    }

    /**
     * Performs null safe comparison
     * 
     * @param <T>
     * @param one
     * @param other
     * @return true if both objects are equal, including if both are null
     */
    public final static <T> boolean safeEquals(final T one, final T other) {
        return one == other || (one != null && one.equals(other));
    }

    /**
     * Returns the left member of the pair
     * 
     * @return - the left member
     */
    public S getLeft() {
        return left;
    }

    /**
     * Returns the right member of the pair
     * 
     * @return - the right member
     */
    public T getRight() {
        return right;
    }

    /**
     * Overridden hashcode.
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (left == null ? 0 : left.hashCode()) + 13 * (right == null ? 0 : right.hashCode());
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "<" + left + "," + right + ">"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Override
    public int compareTo(Pair<S, T> o) {
        if (o == null)
            return 1;
        return getLeft().compareTo(o.getLeft());
    }

}
