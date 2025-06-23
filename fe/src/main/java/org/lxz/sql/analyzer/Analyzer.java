package org.lxz.sql.analyzer;

import org.lxz.ConnectContext;
import org.lxz.server.GlobalStateMgr;
import org.lxz.sql.ast.AstVisitor;
import org.lxz.sql.ast.QueryStatement;
import org.lxz.sql.ast.StatementBase;

public class Analyzer {

    private final AnalyzerVisitor analyzerVisitor;

    public Analyzer(AnalyzerVisitor analyzerVisitor) {
        this.analyzerVisitor = analyzerVisitor;
    }

    public static void analyze(StatementBase statement, ConnectContext context) {
        GlobalStateMgr.getAnalyzer().analyzerVisitor.visit(statement, context);
    }

    public static class AnalyzerVisitor implements AstVisitor<Void, ConnectContext> {
        private static final AnalyzerVisitor INSTANCE = new AnalyzerVisitor();

        public static Analyzer.AnalyzerVisitor getInstance() {
            return INSTANCE;
        }

        @Override
        public Void visitQueryStatement(QueryStatement statement, ConnectContext session) {
            new QueryAnalyzer(session).analyze(statement);
            return null;
        }
    }


}
