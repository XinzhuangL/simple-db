package org.lxz.analysis;

public abstract class Expr {

    /**
     * Resets the internal analysis state of this expr tree. Removes implicit casts.
     */
    public Expr reset() {
        // todo do nothing
        return this;
    }

}
