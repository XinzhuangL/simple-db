package org.lxz.sql.parser;

import com.google.common.collect.Lists;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.lxz.common.Config;
import org.lxz.qe.OriginStatement;
import org.lxz.sql.ast.StatementBase;

import java.util.List;

public class SqlParser {

    public static List<StatementBase> parse(String sql) {
        StarRocksParser parser = parserBuilder(sql);
        List<StatementBase> statements = Lists.newArrayList();
        List<StarRocksParser.SingleStatementContext> singleStatementContexts = parser.sqlStatements().singleStatement();
        for (int idx = 0; idx < singleStatementContexts.size(); ++idx) {
            StatementBase statement = (StatementBase) new AstBuilder()
                    .visitSingleStatement(singleStatementContexts.get(idx));
            statement.setOrigStmt(new OriginStatement(sql, idx));
            statements.add(statement);
        }
        return statements;
    }


    private static StarRocksParser parserBuilder(String sql) {
        StarRocksLexer lexer = new StarRocksLexer(new CaseInsensitiveStream(CharStreams.fromString(sql)));
        // default = 32L
//        lexer.setSqlMode();
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        StarRocksParser parser = new StarRocksParser(tokenStream);

        parser.removeErrorListeners();
        parser.addErrorListener(new ErrorHandler());
        parser.removeParseListeners();
        parser.addParseListener(new TokenNumberListener(Config.parse_tokens_limit, Config.expr_children_limit));
        return parser;
    }
}
