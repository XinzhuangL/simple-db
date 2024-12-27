package org.lxz;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolManager {

    public static ThreadPoolExecutor newDaemonCacheThreadPool(int maxNumThread, String poolName) {
        return newDaemonThreadPool(0, maxNumThread, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(),
                new LogDiscardPolicy(poolName), poolName);
    }

    public static ThreadPoolExecutor newDaemonThreadPool(int corePoolSize,
                                                         int maximumPoolSize,
                                                         long keepAliveTime,
                                                         TimeUnit unit,
                                                         BlockingQueue<Runnable> workQueue,
                                                         RejectedExecutionHandler handler,
                                                         String poolName) {
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setDaemon(true).setNameFormat(poolName + "-%d").build();
        ThreadPoolExecutor threadPool =
                new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory,
                        handler);
        return threadPool;
    }

    // Now, we have no delay task num limit and thread num limit in ScheduledThreadPoolExecutor,
    // so it may cause oom when there are too many delay tasks or threads in ScheduledThreadPoolExecutor
    // Please use this api only for scheduling short task at fix rate.
    public static ScheduledThreadPoolExecutor newDaemonScheduledThreadPool(int corePoolSize, String poolName) {
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat(poolName + "-%d").build();
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor =
                new ScheduledThreadPoolExecutor(corePoolSize, threadFactory);
        return scheduledThreadPoolExecutor;
    }

    static class LogDiscardPolicy implements RejectedExecutionHandler {

        private String threadPoolName;

        public LogDiscardPolicy(String threadPoolName) {
            this.threadPoolName = threadPoolName;
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            System.out.println("Task " + r.toString() + " rejected from " + threadPoolName + " " + executor.toString());
        }
    }
}
