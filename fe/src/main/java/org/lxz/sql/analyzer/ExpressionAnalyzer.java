package org.lxz.sql.analyzer;


import org.lxz.ConnectContext;
import org.lxz.analysis.Expr;
import org.lxz.analysis.SlotRef;
import org.lxz.sql.ast.AstVisitor;

public class ExpressionAnalyzer {


    private final ConnectContext session;

    public ExpressionAnalyzer(ConnectContext session) {
        if (session == null) {
            // For some load requests, the ConnectContext will be null
            session = new ConnectContext();
        }
        this.session = session;
    }

    public void analyze(Expr expression, AnalyzeState analyzeState, Scope scope) {
        Visitor visitor = new Visitor(analyzeState, session);
        bottomUpAnalyze(visitor, expression, scope);
    }

    private void bottomUpAnalyze(Visitor visitor, Expr expression, Scope scope) {
       // ignore lambda function

        for(Expr expr : expression.getChildren()) {
            bottomUpAnalyze(visitor, expr, scope);
        }
        visitor.visit(expression, scope);
    }



    public static class Visitor implements AstVisitor<Void, Scope> {
        private final AnalyzeState analyzeState;
        private final ConnectContext session;

        public Visitor(AnalyzeState analyzeState, ConnectContext session) {
            this.analyzeState = analyzeState;
            this.session = session;
        }

        // todo visit others

        @Override
        public Void visitSlot(SlotRef node, Scope scope) {
            ResolvedField resolvedField = scope.resolvedField(node);
            node.setType(resolvedField.getField().getType());
            node.setTblName(resolvedField.getField().getRelationAlias());
            // help to get nullable info in Analyzer phase
            // not it is used in creating mv to decide nullable of fields
            node.setNullable(resolvedField.getField().isNullable());
            // process struct
            handleResolvedField(node, resolvedField);

            return null;
        }

        protected void handleResolvedField(SlotRef slot, ResolvedField resolvedField) {
            analyzeState.addColumnReference(slot, FieldId.from(resolvedField));
        }
    }




    public static void analyzeExpression(Expr expression, AnalyzeState state, Scope scope, ConnectContext session) {
        ExpressionAnalyzer analyzer = new ExpressionAnalyzer(session);
        analyzer.analyze(expression, state, scope);
    }

}
