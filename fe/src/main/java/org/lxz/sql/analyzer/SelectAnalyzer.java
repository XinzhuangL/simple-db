package org.lxz.sql.analyzer;

import com.google.common.collect.ImmutableList;
import org.lxz.ConnectContext;
import org.lxz.analysis.Expr;
import org.lxz.analysis.GroupByClause;
import org.lxz.analysis.LimitElement;
import org.lxz.analysis.OrderByElement;
import org.lxz.analysis.SlotRef;
import org.lxz.sql.ast.SelectList;
import org.lxz.sql.ast.Relation;
import org.lxz.sql.ast.SelectListItem;

import java.util.ArrayList;
import java.util.List;

public class SelectAnalyzer {

    private final ConnectContext session;

    public SelectAnalyzer(ConnectContext session) {
        this.session = session;
    }

    public void analyze(AnalyzeState analyzeState,
                        SelectList selectList,
                        Relation fromRelation,
                        Scope sourceScope,
                        GroupByClause groupByClause,
                        Expr havingClause,
                        Expr whereClause,
                        List<OrderByElement> sortClause,
                        LimitElement limitElement
                        ) {
        // analyzeWhere

        List<Expr> outputExpressions =
                analyzeSelect(selectList, fromRelation, groupByClause != null, analyzeState, sourceScope);
        Scope outputScope = analyzeState.getOutputScope();

        // groupByExpressions
        // analyzeHaving

        // orderScope

        // todo other



    }

    private List<Expr> analyzeSelect(SelectList selectList, Relation fromRelation, boolean hasGroupByClause,
                                     AnalyzeState analyzeState, Scope scope) {
        ImmutableList.Builder<Expr> outputExpressionBuilder = ImmutableList.builder();
        ImmutableList.Builder<Field> outputFields = ImmutableList.builder();

        for (SelectListItem item: selectList.getItems()) {
            // star later
            if(!item.isStar()) {
                String name;
                if (item.getExpr() instanceof SlotRef) {
                    name = item.getAlias() == null ? ((SlotRef) item.getExpr()).getColName() : item.getAlias();
                } else {
                    // not slot
                    // a generate function by AstToStringBuilder
                    name = item.getAlias();
                }
                analyzeExpression(item.getExpr(), analyzeState, scope);
                outputExpressionBuilder.add(item.getExpr());
                if (item.getExpr() instanceof SlotRef) {
                    outputFields.add(new Field(name, item.getExpr().getType(),
                            ((SlotRef) item.getExpr()).getTblNameWithoutAnalyzed(), item.getExpr(),
                            true, item.getExpr().isNullable()));
                } else {
                    outputFields.add(new Field(name, item.getExpr().getType(), null, item.getExpr(),
                            true, item.getExpr().isNullable()));
                }



            }

        }
        List<Expr> outputExpressions = outputExpressionBuilder.build();
        analyzeState.setOutputExpression(outputExpressions);
        analyzeState.setOutputScope(new Scope(RelationId.anonymous(), new RelationFields(outputFields.build())));
        return outputExpressions;
    }

    private void analyzeExpression(Expr expr, AnalyzeState analyzeState, Scope scope) {
        ExpressionAnalyzer.analyzeExpression(expr, analyzeState, scope, session);
    }
}
