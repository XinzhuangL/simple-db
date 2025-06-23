package org.lxz.sql;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lxz.ConnectContext;
import org.lxz.sql.analyzer.Analyzer;
import org.lxz.sql.analyzer.PlannerMetaLocker;
import org.lxz.sql.ast.QueryRelation;
import org.lxz.sql.ast.QueryStatement;
import org.lxz.sql.ast.StatementBase;
import org.lxz.sql.optimizer.base.ColumnRefFactory;
import org.lxz.sql.optimizer.transformer.LogicalPlan;
import org.lxz.sql.optimizer.transformer.RelationTransformer;
import org.lxz.sql.optimizer.transformer.TransformerContext;
import org.lxz.sql.plan.ExecPlan;

import java.util.List;

public class StatementPlanner {


    private static final Logger LOG = LogManager.getLogger(StatementPlanner.class);

    // todo ignore resultType
    public static ExecPlan plan(StatementBase stmt, ConnectContext session) throws Exception {

        try (ConnectContext.ScopeGuard guard = session.bindScope()) {

            // Analyze
            analyzeStatement(stmt, session);

            // Authorization check

            // create query plan
            if (stmt instanceof QueryStatement) {
                ExecPlan plan = createQueryPlan(stmt, session);

                // setOutfileSink()
                return plan;
            } else {
                throw new RuntimeException("unsupported statement type: " + stmt.getClass());
            }


        } catch (Throwable e) {
            // abort transaction
            throw e;
        }

    }

    /**
     * Analyze the statement.
     * 1. Optimization for INSERT_SELECT: if the SELECT doesn't need the lock, we can defer the lock acquisition
     * after analyzing the SELECT. That can help the case which SELECT is a time-consuming external table access.
     */
    private static void analyzeStatement(StatementBase statement, ConnectContext session) {
        Analyzer.analyze(statement, session);
    }

    private static ExecPlan createQueryPlan(StatementBase stmt, ConnectContext session) {
        QueryStatement queryStmt = (QueryStatement) stmt;
        QueryRelation query = queryStmt.getQueryRelation();
        List<String> colNames = query.getColumnOutputNames();
        // 1. Build Logical plan
        ColumnRefFactory columnRefFactory = new ColumnRefFactory();
        LogicalPlan logicalPlan;

        // Transformer StatementBase --> LogicalPlan
        // get a logicalPlan without inlining views
        TransformerContext transformerContext = new TransformerContext(columnRefFactory, session);
        logicalPlan = new RelationTransformer(transformerContext).transformWithSelectLimit(query);
        return null;
    }
}
