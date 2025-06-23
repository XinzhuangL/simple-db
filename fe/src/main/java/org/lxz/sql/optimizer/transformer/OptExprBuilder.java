package org.lxz.sql.optimizer.transformer;

import org.lxz.sql.analyzer.Scope;
import org.lxz.sql.optimizer.OptExpression;
import org.lxz.sql.optimizer.operator.Operator;
import org.lxz.sql.optimizer.operator.scalar.ColumnRefOperator;

import java.util.List;

/**
 * OptExprBuilder is used to build OptExpression tree
 */
public class OptExprBuilder {
    private final Operator root;
    private final List<OptExprBuilder> inputs;
    private ExpressionMapping expressionMapping;

    public OptExprBuilder(Operator root, List<OptExprBuilder> inputs, ExpressionMapping expressionMapping) {
        this.root = root;
        this.inputs = inputs;
        this.expressionMapping = expressionMapping;
    }

    public Scope getScope() {
        return expressionMapping.getScope();
    }

    public OptExpression getRoot() {
        // todo later
        return null;
    }

    public List<ColumnRefOperator> getFieldMappings() {
        return expressionMapping.getFieldMappings();
    }

    public List<OptExprBuilder> getInputs() {
        return inputs;
    }

    public ExpressionMapping getExpressionMapping() {
        return expressionMapping;
    }

    public void setExpressionMapping(ExpressionMapping expressionMapping) {
        this.expressionMapping = expressionMapping;
    }
}
