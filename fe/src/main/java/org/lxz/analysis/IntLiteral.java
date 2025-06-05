package org.lxz.analysis;

import org.lxz.sql.parser.NodePosition;

public class IntLiteral extends LiteralExpr {
    private long value;
    private String stringValue = null;

    public IntLiteral(long value, NodePosition pos) {
        super(pos);
        // todo init
        // todo
    }
}
