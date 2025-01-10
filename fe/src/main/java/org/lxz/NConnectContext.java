package org.lxz;

import org.lxz.mysql.server.nio.NMysqlChannel;
import org.xnio.StreamConnection;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import javax.net.ssl.SSLContext;

/**
 * connect context based on nio.
 */
public class NConnectContext extends ConnectContext {

    public NConnectContext(StreamConnection connection, SSLContext sslContext) {
        super();
        super.sslContext = sslContext;
        mysqlChannel = new NMysqlChannel(connection);
        remoteIP = mysqlChannel.getRemoteIp();
    }

    @Override
    public synchronized void cleanup() {
        if (closed) {
            return;
        }
        closed = true;
        mysqlChannel.close();
    }


    public void startAcceptQuery(ConnectProcessor connectProcessor) {
        ((NMysqlChannel) mysqlChannel).startAcceptQuery(this, connectProcessor);
    }

    public void suspendAcceptQuery() {
        ((NMysqlChannel) mysqlChannel).suspendAcceptQuery();
    }

    public void resumeAcceptQuery() {
        ((NMysqlChannel)mysqlChannel).resumeAcceptQuery();
    }

    public void stopAcceptQuery() throws IOException {
        ((NMysqlChannel)mysqlChannel).stopAcceptQuery();
    }

}
