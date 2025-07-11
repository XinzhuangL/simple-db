package org.lxz;

import org.lxz.common.Config;
import org.lxz.common.QueryState;
import org.lxz.mysql.server.MysqlCapability;
import org.lxz.mysql.server.MysqlChannel;
import org.lxz.mysql.server.MysqlCommand;
import org.lxz.mysql.server.MysqlSerializer;
import org.lxz.qe.StmtExecutor;

import java.nio.channels.SocketChannel;
import java.util.UUID;
import javax.net.ssl.SSLContext;

public class ConnectContext {

    protected static ThreadLocal<ConnectContext> threadLocalInfo = new ThreadLocal<>();


    // Timestamp in millisecond last command starts at
    protected long startTime = System.currentTimeMillis();

    // set this before analyze
    protected long stmtId;

    // Command this connection is processing.
    protected MysqlCommand command;

    protected volatile boolean isPending = false;

    // Indicate if this client is killed.
    protected volatile boolean isKilled;

    // The queryId of the last query processed by this session.
    // In some scenarios, the user can get the output of a request by queryId,
    // such as Insert, export requests
    protected UUID lastQueryId;
    // The queryId is used to track a user's request. A user request will only have one queryId
    // in the entire StarRocks system. in some scenarios, a user request may be forwarded to multiple
    // nodes for processing or be processed repeatedly, but each execution instance will have
    // the same queryId
    protected UUID queryId;



    // user@host of current login user
    protected String qualifiedUser = "root@127.0.0.1";

    // the protocol capability which server say it can support
    protected MysqlCapability serverCapability;

    // the protocol capability after server and client negotiate
    protected MysqlCapability capability;

    protected volatile boolean closed;

    protected volatile boolean isForward = false;

    // Time when the connection is make
    protected long connectionStartTime;

    // Scheduler this connection belongs to
    protected ConnectScheduler connectScheduler;

    // error code
    protected String errorCode = "";

    // state
    protected QueryState state;

    // mysql net
    protected MysqlChannel mysqlChannel;

    protected MysqlSerializer mysqlSerializer;

    // id for this connectionId
    protected int connectionId;

    protected SSLContext sslContext;

    protected String remoteIP;

    StmtExecutor executor;

    private ConnectContext parent;

    public ConnectContext() {
        this(null, null);
    }

    public ConnectContext(SocketChannel channel) {
        this(channel, null);
    }

    public ConnectContext(SocketChannel channel, SSLContext sslContext) {
        closed = false;
        state = new QueryState();
        serverCapability = MysqlCapability.DEFAULT_CAPABILITY;
        isKilled = false;
        mysqlSerializer = MysqlSerializer.newInstance();
        command = MysqlCommand.COM_SLEEP;
        mysqlChannel = new MysqlChannel(channel);
        this.sslContext = sslContext;
    }

    public synchronized void cleanup() {
        if (closed) {
            return;
        }
        closed = true;
        mysqlChannel.close();
        threadLocalInfo.remove();
    }


    public void checkTimeout(long now) {
        if (startTime <= 0) {
            return;
        }

        long delta = now -startTime;
        boolean killFlag = false;
        boolean killConnection = false;
        if (command == MysqlCommand.COM_SLEEP) {
            // an impossible time
            if (delta > Config.connection_wait_timeout_milliseconds) {
                // Need kill this connection.
                // todo print mysqlChannel name  and timeout config
                System.out.println("kill wait timeout connection, remote:" + mysqlChannel.getRemoteHostPortString() + ", wait timeout: " + delta + "ms");
                killFlag = true;
                killConnection = true;
            }
        } else {
            long timeoutSecond = Config.query_timeout_milliseconds;
            if (isPending) {
                timeoutSecond += Config.query_pending_timeout_milliseconds;
            }
            if (delta > timeoutSecond) {
                System.out.println("kill query timeout, remote: {}, query timeout: {}");
                // Only kill
                killFlag = true;
            }

        }
        if (killFlag) {
            kill(killConnection);
        }
    }

    // kill operation with no protect.
    // two part  killConnection And kill query
    public void kill(boolean killConnection) {
        System.out.println("kill query, {}, kill connection: {}");
        // Now, cancel running process
        //
        if (killConnection) {
            isKilled = true;
        }

        // cancel query

        if (killConnection) {
            int times = 0;
            while (!closed) {
                try {
                    Thread.sleep(10);;
                    times++;
                    if (times > 100) {
                        System.out.println("wait fore close fail, break.");
                        break;
                    }
                } catch (InterruptedException e) {
                    System.out.println("sleep exception, ignore.");
                    break;
                }
            }
            // Close channel to break connection with client
            mysqlChannel.close();
        }
    }

    public boolean isKilled() {
        return (parent != null && parent.isKilled()) || isKilled;
    }


    // Set kill flag to true;
    public void setKilled() {
        isKilled = true;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    public void resetConnectionStartTime() {
        this.connectionStartTime = System.currentTimeMillis();
    }

    public void setThreadLocalInfo() {
        threadLocalInfo.set(this);
    }

    /**
     * Set this connect to thread-local if not exists
     * return set or not
     */

    public boolean setThreadLocalInfoIfNotExists() {
        if (threadLocalInfo.get() == null) {
            threadLocalInfo.set(this);
            return true;
        }
        return false;
    }

    public static void remove() {
        threadLocalInfo.remove();
    }

    public static ConnectContext get() {
        return threadLocalInfo.get();
    }

    public void setConnectScheduler(ConnectScheduler connectScheduler) {
        this.connectScheduler = connectScheduler;
    }

    public String getQualifiedUser() {
        return qualifiedUser;
    }

    public QueryState getState() {
        return state;
    }

    public void setState(QueryState state) {
        this.state = state;
    }

    public MysqlChannel getMysqlChannel() {
        return mysqlChannel;
    }

    public void setMysqlChannel(MysqlChannel mysqlChannel) {
        this.mysqlChannel = mysqlChannel;
    }

    public MysqlSerializer getMysqlSerializer() {
        return mysqlSerializer;
    }

    public void setMysqlSerializer(MysqlSerializer mysqlSerializer) {
        this.mysqlSerializer = mysqlSerializer;
    }

    public boolean supportSSL() {
        return sslContext != null;
    }

    public MysqlCapability getServerCapability() {
        return serverCapability;
    }

    public MysqlCapability getCapability() {
        return capability;
    }

    public void setCapability(MysqlCapability capability) {
        this.capability = capability;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime() {
        startTime = System.currentTimeMillis();
       // returnRows = 0;
    }

    public MysqlCommand getCommand() {
        return command;
    }

    public void setCommand(MysqlCommand command) {
        this.command = command;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public UUID getQueryId() {
        return queryId;
    }

    public void setQueryId(UUID queryId) {
        this.queryId = queryId;
    }


    public UUID getLastQueryId() {
        return lastQueryId;
    }

    public void setLastQueryId(UUID queryId) {
        this.lastQueryId = queryId;
    }

    public void setExecutor(StmtExecutor executor) {
        this.executor = executor;
    }

    public long getStmtId() {
        return stmtId;
    }

    public void setStmtId(long stmtId) {
        this.stmtId = stmtId;
    }

    public void setIsForward(boolean forward) {
        isForward = forward;
    }

    public boolean isForward() {
        return isForward;
    }

    public ScopeGuard bindScope() {
        return ScopeGuard.setIfNotExists(this);
    }

    /**
     * Set thread-local context for the scope, and remove it after leaving the scope
     */
    public static class ScopeGuard implements AutoCloseable {

        private boolean set = false;
        protected ScopeGuard() {
        }

        public static ScopeGuard setIfNotExists(ConnectContext session) {
            ScopeGuard res = new ScopeGuard();
            res.set = session.setThreadLocalInfoIfNotExists();
            return res;
        }

        @Override
        public void close() throws Exception {
            if (set) {
                ConnectContext.remove();
            }
        }
    }


}
