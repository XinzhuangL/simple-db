package org.lxz.sql.ast;

public class QueryStatement extends StatementBase {
    private final QueryRelation queryRelation;


    public QueryStatement(QueryRelation queryRelation) {
        super(queryRelation.getPos());
        this.queryRelation = queryRelation;
    }


    public QueryRelation getQueryRelation() {
        return queryRelation;
    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
        return visitor.visitQueryStatement(this, context);
    }
}
