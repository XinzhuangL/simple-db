package org.lxz.sql.analyzer;

import org.lxz.analysis.Expr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AnalyzeState is used to record some temporary variables that may be used during translate.
 * temporary variables for namespace resolution,
 * hierarchical relationship of namespace and with clause
 */
public class AnalyzeState {

    /**
     * Structure used to build QueryBlock
     */
    private List<Expr> outputExpressions;
    private Scope outputScope;

    /**
     * Mapping of slotref to fieldid, used to determine
     * whether two expressions come from the same column
     */
    private final Map<Expr, FieldId> columnReferences = new HashMap<>();



    public void addColumnReference(Expr expr, FieldId fieldId) {
        columnReferences.put(expr, fieldId);
    }

    public Map<Expr, FieldId> getColumnReferences() {
        return columnReferences;
    }

    public void setOutputExpression(List<Expr> outputExpressions) {
        this.outputExpressions = outputExpressions;
    }

    public void setOutputScope(Scope outputScope) {
        this.outputScope = outputScope;
    }

    public List<Expr> getOutputExpressions() {
        return outputExpressions;
    }

    public Scope getOutputScope() {
        return outputScope;
    }
}
