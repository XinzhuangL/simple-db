package org.lxz.mysql.server;

import com.google.common.base.Strings;
import org.lxz.ConnectContext;
import org.lxz.common.NegotiateState;
import org.lxz.mysql.packet.MysqlAuthPacket;
import org.lxz.mysql.packet.MysqlHandshakePacket;
import org.lxz.mysql.packet.MysqlPacket;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lxz.mysql.packet.MysqlHandshakePacket.AUTHENTICATION_KERBEROS_CLIENT;

// MySQL protocol util
public class MysqlProto {

    // scramble: data receive from server.
    // randomString: data send by server in plug-in data failed
    // user_name#HIGH@cluster_name
    private static boolean authenticate(ConnectContext context, byte[] scramble, byte[] randomString, String user) {
        String usePasswd = scramble.length == 0 ? "NO" : "YES";

        if (user == null || user.isEmpty()) {
            System.out.println("user is empty");
            return false;
        }
        // todo real check randomString

        String remoteIp = context.getMysqlChannel().getRemoteIp();
        // use new privilege
        // todo return true now
        return MockUser.checkPassword(user, remoteIp, scramble, randomString);
    }



    // send response packet(ok/EOF/ERR).
    // before call this function, should set information in state of ConnectContext
    public static void sendResponsePacket(ConnectContext context) throws IOException {
        MysqlSerializer serializer = context.getMysqlSerializer();
        MysqlChannel channel = context.getMysqlChannel();
        MysqlPacket packet = context.getState().toResponsePacket();

        // send response packet to client
        serializer.reset();
        packet.writeTo(serializer);
        channel.sendAndFlush(serializer.toByteBuffer());
    }

    /**
     * negotiate with client, use MySQL protocol
     * server ---handshake---> client
     * server <--- authenticate --- client
     * server --- response(OK/ERR) ---> client
     * Exception:
     * IOException:
     */
    public static boolean negotiate(ConnectContext context) throws IOException {
        MysqlSerializer serializer = context.getMysqlSerializer();
        MysqlChannel channel = context.getMysqlChannel();
        context.getState().setOk();

        // Server send handshake packet to client.
        serializer.reset();
        MysqlHandshakePacket handshakePacket = new MysqlHandshakePacket(context.getConnectionId(),
                context.supportSSL());
        handshakePacket.writeTo(serializer);
        channel.sendAndFlush(serializer.toByteBuffer());
        MysqlAuthPacket authPacket = readAuthPacket(context);
        if (authPacket == null) {
            return false;
        }

        if (authPacket.isSSLConnRequest()) {
            // change to ssl session
            System.out.println("start to enable ssl connection");
            // todo support later
        }

        // check capability
        if (!MysqlCapability.isCompatible(context.getServerCapability(), authPacket.getCapability())) {
            // TODO: client return capability can not support
            System.out.println("err not supported auth mode");
            sendResponsePacket(context);
            return false;
        }

        // Starting with MySQL 8.0.4, MySQL changed the default authentication plugin for MySQL client
        // from mysql_native_password to caching_sha2_password.
        // ref: https://mysqlserverteam.com/mysql-8-0-4-new-default-authentication-plugin-caching_sha2_password/
        // So, User use mysql client or ODBC Driver after 8.0.4 have problem to connect to StarRocks
        // with password.
        // So StarRocks support the Protocol::AuthSwitchRequest to tell client to keep the default password plugin
        // which StarRocks is using now.
        //
        // Older version mysql client does not send auth plugin info, like 5.1 version.
        // So we check if auth plugin name is null and treat as mysql_native_password if is null.
        String authPluginName = authPacket.getPluginName();
        if (authPluginName != null && !handshakePacket.checkAuthPluginSameAsStarRocks(authPluginName)) {
            // 1. clear the serializer
            serializer.reset();;
            // 2. build the auth switch request and send to the client
            if (authPluginName.equals(AUTHENTICATION_KERBEROS_CLIENT)) {
                // todo support later
                return false;
            } else {
                handshakePacket.buildAuthSwitchRequest(serializer);
            }

            channel.sendAndFlush(serializer.toByteBuffer());
            // Server receive auth switch response packet from client.
            ByteBuffer authSwitchResponse = channel.fetchOnePacket();
            if (authSwitchResponse == null) {
                // receive response failed.
                return false;
            }
            // 3. the client use default password plugin of StarRocks to dispose
            // password
            authPacket.setAuthResponse(readEofString(authSwitchResponse));
        }

        // change the capability of serializer
        context.setCapability(context.getServerCapability());
        serializer.setCapability(context.getCapability());

        // NOTE: when we behind proxy, we need random string sent by proxy.
        byte[] randomString = handshakePacket.getAuthPluginData();
        // check authenticate
        if (!authenticate(context, authPacket.getAuthResponse(), randomString, authPacket.getUser())) {
            sendResponsePacket(context);
            return false;
        }

        // set database
        String db = authPacket.getDb();
        if (!Strings.isNullOrEmpty(db)) {
            // todo we should change db
        }
        return true;

    }


    private static MysqlAuthPacket readAuthPacket(ConnectContext context) throws IOException {
        // Server receive authenticate packet from client.
        ByteBuffer handshakeResponse = context.getMysqlChannel().fetchOnePacket();
        if (handshakeResponse == null) {
            // receive response failed.
            return null;
        }
        MysqlAuthPacket authPacket = new MysqlAuthPacket();
        if (!authPacket.readFrom(handshakeResponse)) {
            System.out.println("not supported auth mode");
            sendResponsePacket(context);
            return null;
        }
        return authPacket;
    }

    public static byte readByte(ByteBuffer buffer) {
        return buffer.get();
    }

    public static int readInt1(ByteBuffer buffer) {
        return readByte(buffer) & 0XFF;
    }

    public static int readInt2(ByteBuffer buffer) {
        return (readByte(buffer) & 0xFF) | ((readByte(buffer) & 0xFF) << 8);
    }

    public static int readInt3(ByteBuffer buffer) {
        return (readByte(buffer) & 0xFF) | ((readByte(buffer) & 0xFF) << 8) | ((readByte(
                buffer) & 0xFF) << 16);
    }

    public static int readInt4(ByteBuffer buffer) {
        return (readByte(buffer) & 0xFF) | ((readByte(buffer) & 0xFF) << 8) | ((readByte(
                buffer) & 0xFF) << 16) | ((readByte(buffer) & 0XFF) << 24);
    }

    public static long readInt6(ByteBuffer buffer) {
        return (readInt4(buffer) & 0XFFFFFFFFL) | (((long) readInt2(buffer)) << 32);
    }

    public static long readInt8(ByteBuffer buffer) {
        return (readInt4(buffer) & 0XFFFFFFFFL) | (((long) readInt4(buffer)) << 32);
    }

    public static long readVInt(ByteBuffer buffer) {
        int b = readInt1(buffer);

        if (b < 251) {
            return b;
        }
        if (b == 252) {
            return readInt2(buffer);
        }
        if (b == 253) {
            return readInt3(buffer);
        }
        if (b == 254) {
            return readInt8(buffer);
        }
        if (b == 251) {
            throw new NullPointerException();
        }
        return 0;
    }

    public static byte[] readFixedString(ByteBuffer buffer, int len) {
        byte[] buf = new byte[len];
        buffer.get(buf);
        return buf;
    }

    public static byte[] readEofString(ByteBuffer buffer) {
        byte[] buf = new byte[buffer.remaining()];
        buffer.get(buf);
        return buf;
    }

    public static byte[] readLenEncodedString(ByteBuffer buffer) {
        long length = readVInt(buffer);
        byte[] buf = new byte[(int) length];
        buffer.get(buf);
        return buf;
    }

    public static byte[] readNulTerminateString(ByteBuffer buffer) {
        int oldPos = buffer.position();
        int nullPos = oldPos;
        for (nullPos = oldPos; nullPos < buffer.limit(); ++nullPos) {
            if (buffer.get(nullPos) == 0) {
                break;
            }
        }
        byte[] buf = new byte[nullPos - oldPos];
        buffer.get(buf);
        // skip null byte.
        buffer.get();
        return buf;
    }


}
