package org.lxz;

import com.google.common.collect.Maps;
import org.lxz.common.Config;
import org.lxz.mysql.server.MysqlProto;

import java.util.ArrayList;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectScheduler {


    private final AtomicInteger maxConnections;
    private final AtomicInteger numberConnection;
    private final AtomicInteger nextConnectionId;

    private final Map<Long, ConnectContext> connectionMap = Maps.newConcurrentMap();
    private final Map<String, AtomicInteger> connByUser = Maps.newConcurrentMap();
    private final ExecutorService executor = ThreadPoolManager
            .newDaemonCacheThreadPool(Config.max_connection_scheduler_threads_num, "connect_scheduler_pool");


    public ConnectScheduler(int maxConnections) {
        this.maxConnections = new AtomicInteger(maxConnections);
        numberConnection = new AtomicInteger(0);
        nextConnectionId = new AtomicInteger(0);
        // Use a thread to check whether connection is timeout. Because
        // 1. If use a scheduler, the task maybe a huge number when query is messy.
        //    Let timeout is 10m, and 5000 qps, then there are up to 3000000 tasks in scheduler.
        // 2. Use a thread to poll maybe lose some accurate, but is enough to us.
        ScheduledExecutorService checkTimer = ThreadPoolManager.newDaemonScheduledThreadPool(1, "Connect-Scheduler-Check-Timer");
        checkTimer.scheduleAtFixedRate(new TimeoutChecker(), 0, 1000L, TimeUnit.MILLISECONDS);
    }


    private class TimeoutChecker extends TimerTask {

        @Override
        public void run() {
            try {
                long now = System.currentTimeMillis();
                synchronized (ConnectScheduler.this) {
                    // Because unregisterConnection will be callback in NMysqlChannel's close,
                    // unregisterConnection will remove connectionMap (in the same thread)
                    // This will result in a concurrentModifyException.
                    // So here we copied the connectionIds to avoid removing iterator during operate iterator
                    ArrayList<Long> connectionIds = new ArrayList<>(connectionMap.keySet());
                    for (Long connectId : connectionIds) {
                        ConnectContext context = connectionMap.get(connectId);
                        context.checkTimeout(now);
                    }
                }

            } catch (Throwable e) {
                // Catch Exception to avoid thread exit
                System.out.println("Timeout checker exception, Internal error : " + e.getMessage());
            }
        }
    }




    // submit one MysqlContext to this scheduler.
    // return true, if this connection has been successfully submitted, otherwise return false.
    // Caller should close ConnectContext if return false.
    public boolean submit(ConnectContext context) {
        if (context == null) {
            return false;
        }
        context.setConnectionId(nextConnectionId.getAndAdd(1));
        context.resetConnectionStartTime();

        // no necessary for nio
        // todo noimpl nio

        if (executor.submit(new LoopHandler(context)) == null) {
            System.out.println("Submit one thread failed.");
            return false;
        }
        return true;
    }


    // Register && Unregister
    // Register one connection with its connection id.
    public boolean registerConnection(ConnectContext context) {
        if (numberConnection.get() >= maxConnections.get()) {
            return false;
        }
        // Check user
        if (connByUser.get(context.getQualifiedUser()) == null) {
            connByUser.put(context.getQualifiedUser(), new AtomicInteger(0));
        }
        int conns = connByUser.get(context.getQualifiedUser()).get();
        long currentConns = Long.MAX_VALUE;
        // check userConn
        if (conns >= currentConns) {
            return false;
        }

        numberConnection.incrementAndGet();
        connByUser.get(context.getQualifiedUser()).incrementAndGet();
        connectionMap.put((long)context.getConnectionId(), context);
        return true;
    }

    public void unregisterConnection(ConnectContext context) {
        if (connectionMap.remove((long)context.getConnectionId()) != null) {
            numberConnection.decrementAndGet();
            AtomicInteger conns = connByUser.get(context.getQualifiedUser());
            if (conns != null) {
                conns.decrementAndGet();
            }
        }
    }

    // process connection

    private class LoopHandler implements Runnable {
        ConnectContext context;

        LoopHandler(ConnectContext context) {
            this.context = context;
        }

        @Override
        public void run() {
            try {
                // Set thread local info
                context.setThreadLocalInfo();
                context.setConnectScheduler(ConnectScheduler.this);
                // authenticate check failed.
                if (!MysqlProto.negotiate(context)) {
                    return;
                }

                if (registerConnection(context)) {
                    MysqlProto.sendResponsePacket(context);
                } else {
                    context.getState().setError("Reach limit of connections");
                    MysqlProto.sendResponsePacket(context);
                    return;
                }
                context.setStartTime();
                ConnectProcessor processor = new ConnectProcessor(context);
                processor.loop();
            } catch (Exception e) {
                System.out.println("connect processor exception because " + e.getMessage());
            } finally {
                unregisterConnection(context);
                context.cleanup();
            }
        }
    }
}
