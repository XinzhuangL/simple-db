package org.lxz.sql.optimizer.transformer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.lxz.ConnectContext;
import org.lxz.catalog.Column;
import org.lxz.sql.analyzer.Field;
import org.lxz.sql.ast.AstVisitor;
import org.lxz.sql.ast.QueryStatement;
import org.lxz.sql.ast.Relation;
import org.lxz.sql.ast.SelectRelation;
import org.lxz.sql.ast.TableRelation;
import org.lxz.sql.optimizer.base.ColumnRefFactory;
import org.lxz.sql.optimizer.operator.logical.LogicalOlapScanOperator;
import org.lxz.sql.optimizer.operator.logical.LogicalScanOperator;
import org.lxz.sql.optimizer.operator.scalar.ColumnRefOperator;
import org.lxz.sql.optimizer.operator.scalar.ScalarOperator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RelationTransformer implements AstVisitor<LogicalPlan, ExpressionMapping> {

    private final ColumnRefFactory columnRefFactory;
    private final ConnectContext session;
    private final ExpressionMapping outer;
    private final List<ColumnRefOperator> correlation = new ArrayList<>();
    private final boolean inlineView;

    public RelationTransformer(TransformerContext context) {
        this.columnRefFactory = context.getColumnRefFactory();
        this.session = context.getSession();
        this.outer = context.getOuter();
        this.inlineView = context.isInlineView();

    }

    // transform relation to plan with session variable sql_select_limit
    // only top relation need set limit, transform method used by CTE/Subquery/Insert
    public LogicalPlan transformWithSelectLimit(Relation relation) {
        LogicalPlan plan = transform(relation);
        OptExprBuilder root = plan.getRootBuilder();
        // add limit later
        return plan;
    }


    // transform relation without considering the sql_select_limit
    public LogicalPlan transform(Relation relation) {
        // ignore cte
        return visit(relation);
    }

    @Override
    public LogicalPlan visitSelect(SelectRelation node, ExpressionMapping context) {
        QueryTransformer queryTransformer = new QueryTransformer(columnRefFactory, session, inlineView);
        LogicalPlan logicalPlan = queryTransformer.plan(node, outer);
        return logicalPlan;
    }

    @Override
    public LogicalPlan visitQueryStatement(QueryStatement node, ExpressionMapping context) {
        return visit(node.getQueryRelation());
    }

    @Override
    public LogicalPlan visitTable(TableRelation node, ExpressionMapping context) {
        int size = node.getColumns().size();
        ImmutableMap.Builder<ColumnRefOperator, Column> colRefToColumnMetaMapBuilder =
                ImmutableMap.builderWithExpectedSize(size);
        ImmutableMap.Builder<Column, ColumnRefOperator> columnMetaToColRefMapBuilder =
                ImmutableMap.builderWithExpectedSize(size);
        ImmutableList.Builder<ColumnRefOperator> outputVariablesBuilder =
                ImmutableList.builderWithExpectedSize(size);

        int relationId = columnRefFactory.getNextRelationId();
        for (Map.Entry<Field, Column> column : node.getColumns().entrySet()) {
            ColumnRefOperator columnRef = columnRefFactory.create(column.getKey().getName(),
                    column.getKey().getType(),
                    column.getValue().isAllowNull());
            columnRefFactory.updateColumnToRelationIds(columnRef.getId(), relationId);
            columnRefFactory.updateColumnRefToColumns(columnRef, column.getValue(), node.getTable());
            outputVariablesBuilder.add(columnRef);
            colRefToColumnMetaMapBuilder.put(columnRef, column.getValue());
            columnMetaToColRefMapBuilder.put(column.getValue(), columnRef);
        }
        Map<Column, ColumnRefOperator> columnMetaToColRefMap = columnMetaToColRefMapBuilder.build();
        List<ColumnRefOperator> outputVariables = outputVariablesBuilder.build();

        ScalarOperator partitionPredicate = null;
        // process predicate

        LogicalScanOperator scanOperator;

        // olap only
        // todo getDistributionSpec
        scanOperator = LogicalOlapScanOperator.builder()
                .setTable(node.getTable())
                .setColRefToColumnMetaMap(colRefToColumnMetaMapBuilder.build())
                .setColumnMetaToColRefMap(columnMetaToColRefMap)
                .setDistributionSpec(null)
                .setSelectedIndexId(-1)
                // todo get partitionName
                .setPartitionNames(null)
                .setSelectedTabletId(Lists.newArrayList())
                .setUsePkIndex(false)
                .build();

        OptExprBuilder scanBuilder = new OptExprBuilder(scanOperator, Collections.emptyList(),
                new ExpressionMapping(node.getScope(), outputVariables));


        return new LogicalPlan(scanBuilder, outputVariables, List.of());

    }
}
