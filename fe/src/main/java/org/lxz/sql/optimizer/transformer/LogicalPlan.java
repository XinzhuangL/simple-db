package org.lxz.sql.optimizer.transformer;

import org.lxz.sql.optimizer.operator.scalar.ColumnRefOperator;

import java.util.List;

public class LogicalPlan {

    private final OptExprBuilder root;
    private final List<ColumnRefOperator> outputColumn;

    private final List<ColumnRefOperator> correlation;

    public LogicalPlan(OptExprBuilder root, List<ColumnRefOperator> outputColumn,
                       List<ColumnRefOperator> correlation) {
        this.root = root;
        this.outputColumn = outputColumn;
        this.correlation = correlation;
    }

    public OptExprBuilder getRoot() {
        return root;
    }

    public List<ColumnRefOperator> getOutputColumn() {
        return outputColumn;
    }

    public List<ColumnRefOperator> getCorrelation() {
        return correlation;
    }

    public OptExprBuilder getRootBuilder() {
        return root;
    }

}
