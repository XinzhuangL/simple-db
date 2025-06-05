package org.lxz.analysis;

import org.lxz.sql.parser.NodePosition;

public class NullLiteral extends LiteralExpr {

    protected NullLiteral(NodePosition pos) {
        super(pos);
    }

    public NullLiteral() {
    }

    public static NullLiteral create() {
        return new NullLiteral();
    }
}
