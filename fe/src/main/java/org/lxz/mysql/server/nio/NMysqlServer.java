package org.lxz.mysql.server.nio;

import org.lxz.ConnectScheduler;
import org.lxz.ThreadPoolManager;
import org.lxz.common.Config;
import org.lxz.mysql.server.MysqlServer;
import org.xnio.OptionMap;
import org.xnio.Options;
import org.xnio.StreamConnection;
import org.xnio.Xnio;
import org.xnio.XnioWorker;
import org.xnio.channels.AcceptingChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import javax.net.ssl.SSLContext;

/**
 * mysql protocol implementation based on nio.
 */
public class NMysqlServer extends MysqlServer {

    private XnioWorker xnioWorker;

    private AcceptListener acceptListener;

    private AcceptingChannel<StreamConnection> server;

    // default task service.
    private ExecutorService taskService = ThreadPoolManager
            .newDaemonCacheThreadPool(Config.max_mysql_service_task_threads_num, "starrocks-mysql-nio-pool");

    public NMysqlServer(int port, ConnectScheduler connectScheduler, SSLContext sslContext) {
        this.port = port;
        this.xnioWorker = Xnio.getInstance().createWorkerBuilder()
                .setWorkerName("starrocks-mysql-nio")
                .setWorkerIoThreads(Config.mysql_service_io_threads_num)
                .setExternalExecutorService(taskService).build();

        // connectScheduler only used for idle check.
        this.acceptListener = new AcceptListener(connectScheduler, sslContext);
    }

    // start MySQL protocol service
    // return true if success, otherwise false
    @Override
    public boolean start() {
        try {
            server = xnioWorker.createStreamConnectionServer(new InetSocketAddress("0.0.0.0", port),
                    acceptListener,
                    OptionMap.create(Options.TCP_NODELAY, true, Options.BACKLOG, Config.mysql_nio_backlog_num)
                    );
            server.resumeAccepts();
            running = true;
            System.out.println("Open mysql server success on " + port);
            return true;
        } catch (IOException e) {
            System.out.println("Open MySQL network service failed. " + e.getMessage());
            return false;
        }
    }

    @Override
    public void stop() {
        if (running) {
            running = false;
            // close server channel, make accept throw exception
            try {
                server.close();
            } catch (IOException e) {
                System.out.println("close server channel failed.");
            }
        }
    }



}
