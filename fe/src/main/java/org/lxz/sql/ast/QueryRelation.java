package org.lxz.sql.ast;

import org.lxz.analysis.LimitElement;
import org.lxz.analysis.OrderByElement;
import org.lxz.analysis.ParseNode;
import org.lxz.sql.parser.NodePosition;

import java.util.List;

public class QueryRelation extends Relation implements ParseNode {

    protected List<OrderByElement> sortClause;
    protected LimitElement limit;

    protected QueryRelation() {
        this(NodePosition.ZERO);
    }

    protected QueryRelation(NodePosition pos) {
        super(pos);
    }

    @Override
    public NodePosition getPos() {
        return null;
    }
}
