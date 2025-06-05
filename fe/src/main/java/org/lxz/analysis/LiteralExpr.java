package org.lxz.analysis;

import org.lxz.sql.parser.NodePosition;

public class LiteralExpr extends Expr implements Comparable<LiteralExpr> {

    public LiteralExpr() {
    }

    protected LiteralExpr(NodePosition pos) {
        super(pos);
    }

    @Override
    public int compareTo(LiteralExpr o) {
        return 0;
    }

    @Override
    public NodePosition getPos() {
        return null;
    }
}
