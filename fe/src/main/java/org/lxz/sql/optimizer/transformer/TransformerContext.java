package org.lxz.sql.optimizer.transformer;

import org.lxz.ConnectContext;
import org.lxz.sql.analyzer.RelationFields;
import org.lxz.sql.analyzer.RelationId;
import org.lxz.sql.analyzer.Scope;
import org.lxz.sql.optimizer.base.ColumnRefFactory;

public class TransformerContext {

    private final ColumnRefFactory columnRefFactory;
    private final ConnectContext session;
    private final ExpressionMapping outer;

    // cteContext;

    // whether to expand view in logical plan
    // the origin strategy is true, means will inline view by default.
    // remove this property, set default true
    private final boolean inlineView;

    // enableViewBasedMvRewrite

    // mvTransformerContext





    public TransformerContext(
            ColumnRefFactory columnRefFactory,
            ConnectContext session
    ) {
        this(columnRefFactory, session,
                new ExpressionMapping(new Scope(RelationId.anonymous(), new RelationFields())));
    }

    public TransformerContext(
            ColumnRefFactory columnRefFactory,
            ConnectContext session,
            ExpressionMapping outer
    ) {
        this.columnRefFactory = columnRefFactory;
        this.session = session;
        this.outer = outer;
        this.inlineView = true;

    }

    public ColumnRefFactory getColumnRefFactory() {
        return columnRefFactory;
    }

    public ConnectContext getSession() {
        return session;
    }

    public ExpressionMapping getOuter() {
        return outer;
    }

    public boolean isInlineView() {
        return inlineView;
    }
}
