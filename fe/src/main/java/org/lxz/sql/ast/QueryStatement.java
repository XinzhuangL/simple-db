package org.lxz.sql.ast;

public class QueryStatement extends StatementBase {
    private final QueryRelation queryRelation;

    public QueryStatement(QueryRelation queryRelation) {
        super(queryRelation.getPos());
        this.queryRelation = queryRelation;
    }
}
