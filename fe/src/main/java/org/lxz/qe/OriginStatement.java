package org.lxz.qe;

/**
 * This class represents an origin statement
 * in multiple statements
 */
public class OriginStatement {
    // the origin stmt from client. this may include more than one statement.
    // eg: "select 1; select 2; select 3"
    public final String originStmt;
    // the idx of the specified statement in "originStmt", start from 0.
    public final int idx;

    public OriginStatement(String originStmt, int idx) {
        this.originStmt = originStmt;
        this.idx = idx;
    }
}
