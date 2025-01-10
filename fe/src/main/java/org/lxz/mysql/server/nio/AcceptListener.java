package org.lxz.mysql.server.nio;

import org.lxz.ConnectContext;
import org.lxz.ConnectProcessor;
import org.lxz.ConnectScheduler;
import org.lxz.NConnectContext;
import org.lxz.common.NegotiateState;
import org.lxz.mysql.server.MysqlProto;
import org.xnio.ChannelListener;
import org.xnio.StreamConnection;
import org.xnio.channels.AcceptingChannel;

import java.io.IOException;
import java.net.SocketAddress;
import javax.net.ssl.SSLContext;

public class AcceptListener implements ChannelListener<AcceptingChannel<StreamConnection>> {

    private ConnectScheduler connectScheduler;
    private SSLContext sslContext;

    public AcceptListener(ConnectScheduler connectScheduler, SSLContext sslContext) {
        this.connectScheduler = connectScheduler;
        this.sslContext = sslContext;
    }

    @Override
    public void handleEvent(AcceptingChannel<StreamConnection> channel) {
        try {
            StreamConnection connection = channel.accept();
            if (connection == null) {
                return;
            }


            // connection has been established, so need to call context.cleanup()
            // if exception happens.
            NConnectContext context = new NConnectContext(connection, sslContext);

            int connectionId = context.getConnectionId();
            SocketAddress remoteAddr = connection.getPeerAddress();
            System.out.println("Connection established. remoteAddr=" + remoteAddr + ", connectionId=" + connectionId);

            // init
            connectScheduler.submit(context);


            try {
                channel.getWorker().execute(() -> {
                    try {
                        // Set thread local info
                        context.setThreadLocalInfo();
                        context.setConnectScheduler(connectScheduler);

                        // authenticate check failed.
                        if(!MysqlProto.negotiate(context)) {
                            throw new Exception("mysql negotiate failed");
                        }
                        if (connectScheduler.registerConnection(context)) {
                            MysqlProto.sendResponsePacket(context);
                            connection.setCloseListener(
                                    streamConnection -> { connectScheduler.unregisterConnection(context); }
                            );
                        } else {
                            context.getState().setError("Reach limit of connections");
                            MysqlProto.sendResponsePacket(context);
                            throw new Exception("Reach limit of connections");
                        }
                        context.setStartTime();
                        ConnectProcessor processor = new ConnectProcessor(context);
                        context.startAcceptQuery(processor);

                    } catch (Throwable e) {
                        System.out.println("connect processor exception because, " + e.getMessage());
                        context.cleanup();
                        context.getState().setError(e.getMessage());
                    } finally {
                        ConnectContext.remove();
                    }
                });

            } catch (Throwable e) {
                System.out.println("connect processor exception because " + e.getMessage());
                context.cleanup();
                ConnectContext.remove();
            }

        } catch (IOException e) {
            System.out.println("Connection accept failed. " + e.getMessage());
        }

    }
}
