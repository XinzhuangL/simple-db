package org.lxz.mysql.packet;

import org.lxz.mysql.server.MysqlCapability;
import org.lxz.mysql.server.MysqlSerializer;
import org.lxz.common.QueryState;

// MySQL protocol EOF packet
public class MysqlEofPacket extends MysqlPacket {

    private static final int EOF_INDICATOR = 0xFE;
    private static final int WARNINGS = 0;
    private int serverStatus = 0;

    public MysqlEofPacket(QueryState state) {
        this.serverStatus = state.serverStatus;
    }

    @Override
    public void writeTo(MysqlSerializer serializer) {
        MysqlCapability capability = serializer.getCapability();

        serializer.writeInt1(EOF_INDICATOR);
        if (capability.isProtocol41()) {
            serializer.writeInt2(WARNINGS);
            serializer.writeInt2(serverStatus);
        }
    }
}
