package org.lxz.sql.ast;

import org.lxz.analysis.AnalyticExpr;
import org.lxz.analysis.Expr;
import org.lxz.analysis.FunctionCallExpr;
import org.lxz.analysis.GroupByClause;
import org.lxz.analysis.OrderByElement;
import org.lxz.sql.analyzer.AnalyzeState;
import org.lxz.sql.analyzer.FieldId;
import org.lxz.sql.analyzer.Scope;
import org.lxz.sql.parser.NodePosition;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.lxz.analysis.SlotRef;
import org.lxz.analysis.LimitElement;

public class SelectRelation extends QueryRelation {

    /**
     * selectList is created by parser
     * and will be converted to outputExpr in Analyzer
     *
     */
    private SelectList selectList;

    /**
     *
     * out fields is different with output expr
     * output fields is represent externally visible resolved names
     * such as "k + 1 as alias_name", k+1 is ArithmericExpr, alias_name is the output field
     */
    private List<Expr> outputExpr;

    private Expr predicate;

    /**
     * groupByClause is created by parser
     * and will be converted to groupBy and groupingSetsList in Analyzer
     */
    private GroupByClause groupByClause;
    private List<Expr> groupBy;
    private List<FunctionCallExpr> aggregate;
    private List<List<Expr>> groupingSetsList;
    private Expr having;
    private List<Expr> groupingFunctionCallExprs;

    private boolean isDistinct;

    private List<AnalyticExpr> outputAnalytic;
    private List<AnalyticExpr> orderByAnalytic;

    private Scope orderScope;

    /**
     * order by expression resolve source expression,
     * column ref map will build in project operator when aggregation present
     *
     */
    private List<Expr> orderSourceExpressions;

    private List<Integer> outputExprInorderByScope;

    /**
     * Relations referenced in From clause. The Relation can be a CTE/table
     * reference a subquery or two relation joined together.
     */
    private Relation relation;

    private Map<Expr, FieldId> columnReferences;

    /**
     *
     * materializeExpressionToColumnRef stores the mapping relationship
     * between generated expressions and generated columns
     */
    private Map<Expr, SlotRef> generatedExprToColumnRef = new HashMap<>();


    public SelectRelation(
            SelectList selectList,
            Relation fromRelation,
            Expr predicate,
            GroupByClause groupByClause,
            Expr having
    ) {
        this(selectList, fromRelation, predicate, groupByClause, having, NodePosition.ZERO);
    }

    public SelectRelation(
            SelectList selectList,
            Relation fromRelation,
            Expr predicate,
            GroupByClause groupByClause,
            Expr having, NodePosition pos
    ) {
        super(pos);
        this.selectList = selectList;
        this.relation = fromRelation;
        this.predicate = predicate;
        this.groupByClause = groupByClause;
        this.having = having;
    }


    public SelectRelation(List<Expr> outputExpr, boolean isDistinct,
                          Scope orderScope, List<Expr> orderSourceExpressions,
                          Relation relation, Expr predicate, LimitElement limit,
                          List<Expr> groupBy, List<FunctionCallExpr> aggregate, List<List<Expr>> groupingSetsList,
                          List<Expr> groupingFunctionCallExprs,
                          List<OrderByElement> orderBy, Expr having,
                          List<AnalyticExpr> outputAnalytic, List<AnalyticExpr> orderByAnalytic,
                          Map<Expr, FieldId> columnReferences) {

        this.outputExpr = outputExpr;
        this.isDistinct = isDistinct;
        this.orderScope = orderScope;
        this.relation = relation;
        this.predicate = predicate;
        this.limit = limit;
        this.groupBy = groupBy;
        this.aggregate = aggregate;
        this.groupingSetsList = groupingSetsList;
        this.having = having;
        this.groupingFunctionCallExprs = groupingFunctionCallExprs;

        this.sortClause = orderBy;
        this.orderSourceExpressions = orderSourceExpressions;

        this.outputAnalytic = outputAnalytic;
        this.orderByAnalytic = orderByAnalytic;

        this.columnReferences = columnReferences;
    }

    public void fillResolvedAST(AnalyzeState analyzesState) {
        this.outputExpr = analyzesState.getOutputExpressions();
        // this.predicate = analyzesState.getPredicate();
        this.columnReferences = analyzesState.getColumnReferences();
        this.setScope(analyzesState.getOutputScope());

    }

    public Relation getRelation() {
        return relation;
    }

    public void setRelation(Relation relation) {
        this.relation = relation;
    }

    public SelectList getSelectList() {
        return selectList;
    }

    public List<Expr> getOutputExpr() {
        return outputExpr;
    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
        return visitor.visitSelect(this, context);
    }
}
