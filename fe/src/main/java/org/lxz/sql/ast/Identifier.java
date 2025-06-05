package org.lxz.sql.ast;

import org.lxz.analysis.ParseNode;
import org.lxz.sql.parser.NodePosition;

public class Identifier implements ParseNode {
    private final String value;
    private final NodePosition pos;

    public Identifier(String value) {
        this(value, NodePosition.ZERO);
    }

    public Identifier(String value, NodePosition pos) {
        this.pos = pos;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public NodePosition getPos() {
        return pos;
    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
        return null;
    }
}
