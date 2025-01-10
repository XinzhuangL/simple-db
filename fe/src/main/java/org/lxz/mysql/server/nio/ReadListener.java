package org.lxz.mysql.server.nio;

import org.lxz.ConnectContext;
import org.lxz.ConnectProcessor;
import org.lxz.NConnectContext;
import org.xnio.ChannelListener;
import org.xnio.XnioIoThread;
import org.xnio.conduits.ConduitStreamSourceChannel;

/**
 * listener for handle mysql cmd.
 */
public class ReadListener implements ChannelListener<ConduitStreamSourceChannel> {

    private NConnectContext ctx;
    private ConnectProcessor connectProcessor;


    public ReadListener(NConnectContext nConnectContext, ConnectProcessor connectProcessor) {
        this.ctx = nConnectContext;
        this.connectProcessor = connectProcessor;
    }


    @Override
    public void handleEvent(ConduitStreamSourceChannel channel) {
        // suspend must be call sync in current thread (the IO-Thread notify the read event),
        // otherwise multi handler(task thread) would be waked up by once query.
        XnioIoThread.requireCurrentThread();
        ctx.suspendAcceptQuery();
        // start async query handle in task thread.
        try {
            channel.getWorker().execute(
                    () -> {
                        ctx.setThreadLocalInfo();
                        try {
                            connectProcessor.processOnce();
                            if (!ctx.isKilled()) {
                                ctx.resumeAcceptQuery();
                            } else {
                                ctx.stopAcceptQuery();
                                ctx.cleanup();
                            }

                        } catch (Exception e) {
                            System.out.println("Exception happened in one session(" + ctx + "). " + e.getMessage());
                        } finally {
                            ConnectContext.remove();
                        }
                    }
            );

        } catch (Throwable e) {
            System.out.println("connect processor exception because " + e.getMessage());
            ctx.setKilled();
            ctx.cleanup();
            ConnectContext.remove();
        }
    }
}
