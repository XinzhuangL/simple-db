package org.lxz;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import javax.net.ssl.SSLContext;

public class MysqlServer {

    protected int port;
    protected volatile boolean running;
    private ServerSocketChannel serverChannel = null;
    private ConnectScheduler scheduler = null;
    // used to accept connect request from client
    private ThreadPoolExecutor listener;
    private Future listenerFuture;
    private SSLContext sslContext;

    public MysqlServer(int port, ConnectScheduler scheduler, SSLContext sslContext) {
        this.port = port;
        this.scheduler = scheduler;
        this.sslContext = sslContext;
    }

    protected MysqlServer() {}

    // start Mysql protocol service
    // return true if success, otherwise false
    public boolean start() {
        if (scheduler == null) {
            System.out.println("scheduler is null");
            return false;
        }

        // open server socket
        try {
            serverChannel = ServerSocketChannel.open();
            serverChannel.socket().bind(new InetSocketAddress("0.0.0.0", port), 2048);
            serverChannel.configureBlocking(true);
        } catch (IOException e) {
            System.out.println("Open MySQL network service failed. " + e.getMessage());
            return false;
        }
        // start accept thread
        listener = ThreadPoolManager.newDaemonCacheThreadPool(1, "MySQL-Protocol-Listener");
        running = true;
        listenerFuture = listener.submit(new Listener());
        return true;
    }

    public void stop() {
        if (running) {
            running = false;
            // close server channel, make accept throw exception
            try {
                serverChannel.close();
            } catch (IOException e) {
                System.out.println("close server channel failed. " + e.getMessage());
            }
        }
    }

    public void join() {
        try {
            listenerFuture.get();
        } catch (Exception e) {
            // just return
            System.out.println("Join MySQL server exception. " + e.getMessage());
        }
    }

    private class Listener implements Runnable {
        @Override
        public void run() {
            while (running && serverChannel.isOpen()) {
                SocketChannel clientChannel;
                try {
                    clientChannel = serverChannel.accept();
                    if (clientChannel == null) {
                        continue;
                    }
                    // submit this context to scheduler
                    ConnectContext context = new ConnectContext(clientChannel, sslContext);
                    if (!scheduler.submit(context)) {
                        System.out.println("Submit one connect request failed. Client = " + clientChannel.toString());
                        // clear up context
                        context.cleanup();
                    }

                } catch (IOException e) {
                    // ClosedChannelException
                    // AsynchronousCloseException
                    // ClosedByInterruptException
                    // Other IOException, for example "to many open files"...
                    System.out.println("Query server encounter exception. " + e.getMessage());
                    try {
                        Thread.sleep(100);

                    } catch (InterruptedException e1) {
                        // do nothing
                    }

                } catch (Throwable e) {
                    // NotYetBoundException
                    // SecurityException
                    System.out.println("Query server failed when calling accept. " + e.getMessage());
                }
            }
        }
    }
}
