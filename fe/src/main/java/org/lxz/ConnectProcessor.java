package org.lxz;

import org.lxz.common.ScalarType;
import org.lxz.mysql.packet.MysqlEofPacket;
import org.lxz.mysql.packet.MysqlPacket;
import org.lxz.mysql.server.MysqlChannel;
import org.lxz.mysql.server.MysqlCommand;
import org.lxz.mysql.server.MysqlSerializer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Process one mysql connection, receive one packet, process, send one packet.
 */
public class ConnectProcessor {

    private final ConnectContext ctx;
    private ByteBuffer packetBuf;

    // stmt executor

    public static final String INIT_STMT= "select @@version_comment limit 1";

    public static final String INIT_STMT2= "select $$";

    public ConnectProcessor(ConnectContext ctx) {
        this.ctx = ctx;
    }

    // handle one process
    public void processOnce() throws IOException {
        // set status of query to OK.
        ctx.getState().reset();
        // executor = null

        // reset sequence id of MySQL protocol
        final MysqlChannel channel = ctx.getMysqlChannel();
        channel.setSequenceId(0);
        // read packet from channel
        try {
            packetBuf = channel.fetchOnePacket();
            if (packetBuf == null) {
                throw new IOException("Err happened when receiving packet. ");
            }

        } catch (IOException e) {
            // todo why no AsynchronousCloseException
            // when this happened, timeout checker close this channel
            // killed flag in ctx has been already set, just return
            return;
        }

        // dispatch
        dispatch();
        // finalize
        finalizeCommand();

        ctx.setCommand(MysqlCommand.COM_SLEEP);


    }

    // use to return result packet to user
    private void finalizeCommand() throws IOException {
        ByteBuffer packet = null;

        // todo judge executor and return Result

        packet = getResultPacket();
        if (packet == null) {
            System.out.println("packet == null");
            return;
        }

        MysqlChannel channel = ctx.getMysqlChannel();
        channel.sendAndFlush(packet);

        // only change lastQueryId when current command is COM_QUERY
        if (ctx.getCommand() == MysqlCommand.COM_QUERY) {
            ctx.setLastQueryId(ctx.queryId);
            ctx.setQueryId(null);
        }
    }

    private ByteBuffer getResultPacket() {
        MysqlPacket packet = ctx.getState().toResponsePacket();
        if (packet == null) {
            // possible two cases:
            // 1. handler has send response
            // 2. this command need not to send response
            return null;
        }
        MysqlSerializer serializer = ctx.getMysqlSerializer();
        serializer.reset();
        packet.writeTo(serializer);
        return serializer.toByteBuffer();
    }

    private void dispatch() throws IOException {
        int code = packetBuf.get();
        MysqlCommand command = MysqlCommand.fromCode(code);
        if (command == null) {
            ctx.getState().setError("Unknown command(" + command + ")");
            System.out.println("Unknown command(" + command + ")");
            return;
        }
        ctx.setCommand(command);
        ctx.setStartTime();
        // set resource group
        ctx.setErrorCode("");

        switch (command) {
            case COM_INIT_DB:
                break;
            case COM_QUIT:
                handleQuit();
                break;
            case COM_QUERY:
                handleQuery();
                ctx.setStartTime();
                break;
            case COM_FIELD_LIST:
                break;
            case COM_CHANGE_USER:
                break;
            case COM_RESET_CONNECTION:
                break;
            case COM_PING:
                break;
            default:
                ctx.getState().setError("Unsupported command(" + command + ")");
                System.out.println("Unsupported command: " + command);
                break;
        }
    }

    // COM_QUIT: set killed flag and then return OK packet.
    private void handleQuit() {
        ctx.setKilled();
        ctx.getState().setOk();
    }

    // process COM_QUERY statement
    private void handleQuery() throws IOException {
        // convert statement to java string
        String originStmt = null;
        byte[] bytes = packetBuf.array();
        int ending = packetBuf.limit() - 1;
        while (ending >= 1 && bytes[ending] == '\0') {
            ending--;
        }
        originStmt = new String(bytes, 1, ending, StandardCharsets.UTF_8);
        System.out.println("we exec " + originStmt + "\n");

        if (originStmt.equals(INIT_STMT) || originStmt.equals(INIT_STMT2)) {
            return;
        }
        // send result
        ctx.getMysqlChannel().reset();
        sendOneColumn();
        ctx.getMysqlSerializer().reset();
        ctx.getMysqlSerializer().writeLenEncodedString("Customer#000000010");
        ctx.getMysqlChannel().sendOnePacket(ctx.getMysqlSerializer().toByteBuffer());
        ctx.getState().setEof();

    }

    // send fields there
    private void sendOneColumn() throws IOException {
        MysqlSerializer serializer = ctx.getMysqlSerializer();
        MysqlChannel mysqlChannel = ctx.getMysqlChannel();


        serializer.reset();
        serializer.writeVInt(1);
        mysqlChannel.sendOnePacket(serializer.toByteBuffer());

        // write one
        serializer.reset();
        serializer.writeField("c_name", ScalarType.createVarcharType(25));
        mysqlChannel.sendOnePacket(serializer.toByteBuffer());

        // send EOF
        serializer.reset();
        MysqlEofPacket eofPacket =  new MysqlEofPacket(ctx.getState());
        // write to
        eofPacket.writeTo(serializer);

        mysqlChannel.sendOnePacket(serializer.toByteBuffer());
    }




    public void loop() {
        while (!ctx.isKilled) {
            try {
                processOnce();
            } catch (Exception e) {
                System.out.println("Exception happened in one session(" + ctx + ")." + e.getMessage());
                ctx.setKilled();
                break;
            }
        }
    }

}
