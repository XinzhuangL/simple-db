package org.lxz.qe;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.UuidUtil;
import org.lxz.ConnectContext;
import org.lxz.common.util.TimeUtils;
import org.lxz.mysql.server.MysqlSerializer;
import org.lxz.sql.StatementPlanner;
import org.lxz.sql.ast.QueryStatement;
import org.lxz.sql.ast.StatementBase;
import org.lxz.sql.plan.ExecPlan;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

// Do one COM_QUERY process.
// first: Parse receive byte array to statement struct.
// second: Do handle function for statement.
public class StmtExecutor {
    private static final Logger LOG = LogManager.getLogger(StmtExecutor.class);
    private static final Logger PROFILE_LOG = LogManager.getLogger("profile");
    private static final Gson GSON = new Gson();

    private static final AtomicLong STMT_ID_GENERATOR = new AtomicLong(0);

    private final ConnectContext context;

    private final MysqlSerializer serializer;

    private final OriginStatement originStmt;
    private StatementBase parsedStmt;


    /**
     * When isProxy is true, it proves that the current FE is acting as the
     * Leader and executing the statement forwarded by the Follower
     */
    private boolean isProxy;

    // todo some property

    public StmtExecutor(ConnectContext ctx, StatementBase parsedStmt) {
        this.context = ctx;
        this.parsedStmt = Preconditions.checkNotNull(parsedStmt);
        this.originStmt = parsedStmt.getOrigStmt();
        this.serializer = ctx.getMysqlSerializer();
        this.isProxy = false;
    }

    // Execute one statement
    // Exception:
    //  IOException: talk with client failed.
    public void execute() throws Exception {
        long beginTimeInNanoSecond = TimeUtils.getStartTime();
        context.setStmtId(STMT_ID_GENERATOR.incrementAndGet());
        context.setIsForward(false);

        // set execution id.
        // Try to use query id as execution id when execute first time.
        UUID uuid = context.getQueryId();
        // todo support thrift uuid later
        // context.setExecutionId()
        // todo support session variables later

        try {
            boolean isQuery = parsedStmt instanceof QueryStatement;
            // set isQuery before `forwardToLeader` to make it right for audit log.
            context.getState().setIsQuery(isQuery);

            // todo process query scope hint

            // todo process explain

            ExecPlan execPlan = null;

            // total range
            try {
                if(parsedStmt instanceof QueryStatement) {

                    execPlan = StatementPlanner.plan(parsedStmt, context);

                    System.out.println(execPlan);

                } else {
                    LOG.warn("it's not a QueryStatement");
                }


            } catch (Exception e) {
                LOG.warn("cache exception", e);
            }



        } catch (Exception e) {
            LOG.error("catch all exception", e);

        }
    }





}
