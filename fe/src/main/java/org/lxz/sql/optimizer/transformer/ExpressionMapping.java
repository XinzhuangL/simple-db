package org.lxz.sql.optimizer.transformer;

import org.lxz.analysis.Expr;
import org.lxz.sql.analyzer.RelationId;
import org.lxz.sql.analyzer.Scope;
import org.lxz.sql.optimizer.operator.scalar.ColumnRefOperator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressionMapping {

    /**
     * This structure is responsible for the translation map from Expr to operator
     */
    private Map<Expr, ColumnRefOperator> expressionToColumns = new HashMap<>();

    /**
     * The purpose of below property is to hold the current plan built so far,
     * and the mapping to indicate how the fields (by position) in the relation map to
     * the outputs of the plan.
     *
     * fieldMappings are resolved within scopes hierarchy.
     * Indexes of resolved parent scope fields start from "total number of child scope fields".
     * For instance if a child scope has n fields, then first parent scope field
     * will have index n.
     */
    private final Scope scope;
    private ColumnRefOperator[] fieldMappings;
    private RelationId outerScopeRelationId;


    public ExpressionMapping(Scope scope, List<ColumnRefOperator> fieldMappings) {
        this.scope = scope;
        this.fieldMappings = new ColumnRefOperator[fieldMappings.size()];
        fieldMappings.toArray(this.fieldMappings);
    }

    public ExpressionMapping(Scope scope) {
        this.scope = scope;
        int fieldMappingSize = 0;
        while (scope != null) {
            fieldMappingSize += scope.getRelationFields().getAllFields().size();
            scope = scope.getParent();
        }
        this.fieldMappings = new ColumnRefOperator[fieldMappingSize];
    }

    public ExpressionMapping(Scope scope, List<ColumnRefOperator> fieldMappings, ExpressionMapping outer) {
        this.scope = scope;
        List<ColumnRefOperator> fieldsList = new ArrayList<>(fieldMappings);
        if (outer != null) {
            this.scope.setParent(outer.getScope());
            fieldsList.addAll(outer.getFieldMappings());
            this.outerScopeRelationId = outer.getScope().getRelationId();
            // lambda
        }
        this.fieldMappings = new ColumnRefOperator[fieldMappings.size()];
        fieldMappings.toArray(this.fieldMappings);
    }

    public Map<Expr, ColumnRefOperator> getExpressionToColumns() {
        return expressionToColumns;
    }

    public void setExpressionToColumns(
            Map<Expr, ColumnRefOperator> expressionToColumns) {
        this.expressionToColumns = expressionToColumns;
    }

    public Scope getScope() {
        return scope;
    }

    public List<ColumnRefOperator> getFieldMappings() {
        return Arrays.asList(fieldMappings);
    }

    public void setFieldMappings(ColumnRefOperator[] fieldMappings) {
        this.fieldMappings = fieldMappings;
    }

    public RelationId getOuterScopeRelationId() {
        return outerScopeRelationId;
    }

    public void setOuterScopeRelationId(RelationId outerScopeRelationId) {
        this.outerScopeRelationId = outerScopeRelationId;
    }

    public ColumnRefOperator get(Expr expression) {
        return expressionToColumns.get(expression);
    }

    public ColumnRefOperator getColumnRefWithIndex(int fieldIndex) {
        if (fieldIndex > fieldMappings.length) {
            throw new RuntimeException(
                    String.format("Get columnRef with index %d out fieldMappings length", fieldIndex));
        }
        return fieldMappings[fieldIndex];
    }

}
