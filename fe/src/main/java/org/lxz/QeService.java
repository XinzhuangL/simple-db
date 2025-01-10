package org.lxz;

import org.lxz.mysql.server.MysqlServer;
import org.lxz.mysql.server.nio.NMysqlServer;

import javax.net.ssl.SSLContext;

/**
 * A Mysql Server
 */
public class QeService {
    private MysqlServer mysqlServer;

    public QeService(int port, boolean nioEnabled, ConnectScheduler scheduler) {
        SSLContext sslContext = null;

        if (nioEnabled) {
            mysqlServer = new NMysqlServer(port, scheduler, sslContext);
        } else {
            mysqlServer = new MysqlServer(port, scheduler, sslContext);
        }

    }

    public void start() {
        if (!mysqlServer.start()) {
            System.out.println("mysql server start failed");
            System.exit(-1);
        }
        System.out.println("QE service start.");
    }

    private SSLContext createSSLContext() {
        // todo not impl
        return null;
    }
}
