package org.lxz.packet;

import org.lxz.MysqlSerializer;

import java.nio.ByteBuffer;

// class for MySQL protocol packet
public abstract class MysqlPacket {
    public boolean readFrom(ByteBuffer deserializer) {
        // Only used to read authenticate packet from client
        return false;
    }

    public abstract void writeTo(MysqlSerializer serializer);
}
