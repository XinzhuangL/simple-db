package org.lxz.sql.optimizer.transformer;

import org.lxz.ConnectContext;
import org.lxz.analysis.Expr;
import org.lxz.sql.analyzer.RelationFields;
import org.lxz.sql.analyzer.RelationId;
import org.lxz.sql.analyzer.Scope;
import org.lxz.sql.ast.Relation;
import org.lxz.sql.ast.SelectRelation;
import org.lxz.sql.optimizer.base.ColumnRefFactory;
import org.lxz.sql.optimizer.operator.scalar.ColumnRefOperator;

import java.util.ArrayList;
import java.util.List;

public class QueryTransformer {
    private final ColumnRefFactory columnRefFactory;
    private final ConnectContext session;
    private final List<ColumnRefOperator> correlation = new ArrayList<>();
    private final boolean inlineView;

    public QueryTransformer(ColumnRefFactory columnRefFactory, ConnectContext session, boolean inlineView) {
        this.columnRefFactory = columnRefFactory;
        this.session = session;
        this.inlineView = inlineView;
    }


    public LogicalPlan plan(SelectRelation queryBlock, ExpressionMapping outer) {
        OptExprBuilder builder = planFrom(queryBlock.getRelation());
        builder.setExpressionMapping(new ExpressionMapping(builder.getScope(), builder.getFieldMappings(), outer));
        // ExpressionMapping expressionMapping = builder.getExpressionMapping();

        // add genExpr to generate
        // filter
        // aggregate
        // filter
        // window
        // order by
        // distinct
        // project
        // sort
        // limit
        List<ColumnRefOperator> outputColumns = computeOutputs(builder, queryBlock.getOutputExpr(), columnRefFactory);

        // Add project operator to prune order by columns

        return new LogicalPlan(builder, outputColumns, correlation);

    }

    private static List<ColumnRefOperator> computeOutputs(OptExprBuilder builder, List<Expr> outputExpressions,
                                                          ColumnRefFactory columnRefFactory) {
        List<ColumnRefOperator> outputs = new ArrayList<>();
        for (Expr expression : outputExpressions) {
            outputs.add((ColumnRefOperator) SqlToScalarOperatorTranslator.translate(expression, builder.getExpressionMapping(), columnRefFactory));
        }
        return outputs;
    }

    private OptExprBuilder planFrom(Relation node) {
        TransformerContext transformerContext = new TransformerContext(
                columnRefFactory, session, new ExpressionMapping(new Scope(RelationId.anonymous(), new RelationFields()))
        );
        return new RelationTransformer(transformerContext).visit(node).getRootBuilder();
    }


}
