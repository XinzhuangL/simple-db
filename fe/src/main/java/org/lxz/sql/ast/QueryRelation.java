package org.lxz.sql.ast;

import org.lxz.analysis.LimitElement;
import org.lxz.analysis.OrderByElement;
import org.lxz.analysis.ParseNode;
import org.lxz.sql.analyzer.Field;
import org.lxz.sql.parser.NodePosition;

import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public List<String> getColumnOutputNames() {
        if (explicitColumnNames != null) {
            return explicitColumnNames;
        } else {
            return getScope().getRelationFields().getAllFields().stream().map(Field::getName).collect(Collectors.toList());
        }
    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
        return visitor.visitQueryRelation(this, context);
    }


}
