package org.lxz.mysql.packet;

import com.google.common.collect.Maps;
import org.lxz.mysql.server.MysqlCapability;
import org.lxz.mysql.server.MysqlProto;
import org.lxz.mysql.server.MysqlSerializer;

import java.nio.ByteBuffer;
import java.util.Map;

// MySQL protocol handshake response packet, which contain authenticate information.
public class MysqlAuthPacket extends MysqlPacket{

    private int maxPacketSize;
    private int characterSet;
    private String userName;
    private byte[] authResponse;
    private String database;
    private String pluginName;
    private MysqlCapability capability;
    private Map<String, String> connectAttributes;
    private byte[] randomString;

    public String getUser() {
        return userName;
    }

    public byte[] getAuthResponse() {
        return authResponse;
    }

    public void setAuthResponse(byte[] bytes) {
        authResponse = bytes;
    }

    public String getDb() {
        return database;
    }

    public byte[] getRandomString() {
        return randomString;
    }

    public MysqlCapability getCapability() {
        return capability;
    }

    public String getPluginName() {
        return pluginName;
    }

    public boolean isSSLConnRequest() {
        return capability.isSSL();
    }

    @Override
    public boolean readFrom(ByteBuffer buffer) {
        // read capability four byte, which CLIENT_PROTOCOL_41 must be set
        capability = new MysqlCapability(MysqlProto.readInt4(buffer));
        if (!capability.isProtocol41()) {
            return false;
        }

        // max packet size
        maxPacketSize = MysqlProto.readInt4(buffer);
        // character set. only support 33(utf-8)
        characterSet = MysqlProto.readInt1(buffer);
        // reserved 23 bytes
        buffer.position(buffer.position() + 23);

        // if the request is a ssl request, the package is truncated here.
        if (buffer.remaining() <= 0 && capability.isSSL()) {
            return true;
        }
        // user name
        userName = new String(MysqlProto.readNulTerminateString(buffer));
        if (capability.isPluginAuthDataLengthEncoded()) {
            authResponse = MysqlProto.readLenEncodedString(buffer);
        } else if (capability.isSecureConnection()) {
            int len = MysqlProto.readInt1(buffer);
            authResponse = MysqlProto.readFixedString(buffer, len);
        } else {
            authResponse = MysqlProto.readNulTerminateString(buffer);
        }
        // DB to use
        if (buffer.remaining() > 0 && capability.isConnectedWithDb()) {
            database = new String(MysqlProto.readNulTerminateString(buffer));
        }
        // plugin name to plugin
        if (buffer.remaining() > 0 && capability.isPluginAuth()) {
            pluginName = new String(MysqlProto.readNulTerminateString(buffer));
        }
        // connect attrs, no use now.
        if (buffer.remaining() > 0 && capability.isConnectAttrs()) {
            connectAttributes = parseConnectAttrs(buffer);
        }

        // Commented for JDBC
        // if (buffer.remaining() != 0) {
        //     return false;
        // }
        return true;
    }

    private Map<String, String> parseConnectAttrs(ByteBuffer buffer) {
        String key = "";
        String value = "";
        connectAttributes = Maps.newHashMap();
        try {
            long allAttrLength = MysqlProto.readVInt(buffer);
            long curDealLen = 0;
            while (buffer.remaining() > 0 && allAttrLength - curDealLen > 0) {
                key = value = "";
                long keyLength = MysqlProto.readVInt(buffer);

                if (buffer.remaining() >= keyLength) {
                    key = new String(MysqlProto.readFixedString(buffer, (int)keyLength));
                } else {
                    return connectAttributes;
                }
                curDealLen += keyLength;
                long valLength = MysqlProto.readVInt(buffer);
                if (buffer.remaining() >= valLength) {
                    value = new String(MysqlProto.readFixedString(buffer, (int)valLength));
                } else {
                    // only parse key success
                    connectAttributes.put(key, "");
                    return connectAttributes;
                }
            }
        } catch (Exception e) {
            connectAttributes.put(key, value);
        }
        return connectAttributes;
    }



    @Override
    public void writeTo(MysqlSerializer serializer) {

    }
}
