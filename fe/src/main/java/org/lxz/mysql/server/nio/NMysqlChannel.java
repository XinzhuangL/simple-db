package org.lxz.mysql.server.nio;

import org.lxz.ConnectProcessor;
import org.lxz.NConnectContext;
import org.lxz.mysql.server.MysqlChannel;
import org.xnio.StreamConnection;
import org.xnio.channels.Channels;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * mysql Channel based on nio
 */
public class NMysqlChannel extends MysqlChannel {

    private StreamConnection conn;


    public NMysqlChannel(StreamConnection connection) {

        super();
        this.conn = connection;

        if (connection.getPeerAddress() instanceof InetSocketAddress) {
            InetSocketAddress address = (InetSocketAddress) connection.getPeerAddress();
            remoteHostPortString = address.getHostString() + ":" + address.getPort();
            remoteIp = address.getAddress().getHostAddress();
        } else {
            // Reach here, what's it?
            remoteHostPortString = connection.getPeerAddress().toString();
            remoteIp = connection.getPeerAddress().toString();
        }
    }

    @Override
    public int realNetRead(ByteBuffer dstBuf) throws IOException {
        return Channels.readBlocking(conn.getSourceChannel(), dstBuf);
    }

    /**
     * write packet until no data is remained, unless block.
     */
    @Override
    public void realNetSend(ByteBuffer buffer) throws IOException {
        long bufLen = buffer.remaining();
        long writeLen = Channels.writeBlocking(conn.getSinkChannel(), buffer);
        if (bufLen != writeLen) {
            throw new IOException("Write mysql packet failed.[write=" + writeLen
            + ",needToWrite=" + bufLen + "]");
        }
        Channels.flushBlocking(conn.getSinkChannel());
        isSend = true;
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        try {
            conn.close();
        } catch (IOException e) {
            System.out.println("Close channel exception, ignore.");
        } finally {
            closed = true;
        }
    }

    public void startAcceptQuery(NConnectContext nConnectContext, ConnectProcessor connectProcessor) {
        conn.getSourceChannel().setReadListener(new ReadListener(nConnectContext, connectProcessor));
        conn.getSourceChannel().resumeReads();
    }

    public void suspendAcceptQuery() {
        conn.getSourceChannel().suspendReads();
    }

    public void resumeAcceptQuery() {
        conn.getSourceChannel().resumeReads();
    }

    public void stopAcceptQuery() throws IOException {
        conn.getSourceChannel().shutdownReads();
    }

}
