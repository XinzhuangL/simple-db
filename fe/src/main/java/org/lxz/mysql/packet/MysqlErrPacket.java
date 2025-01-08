package org.lxz.mysql.packet;

import org.lxz.common.ErrorCode;
import org.lxz.mysql.server.MysqlCapability;
import org.lxz.mysql.server.MysqlSerializer;
import org.lxz.common.QueryState;

// MySQL protocol err packet
public class MysqlErrPacket extends MysqlPacket {

    private static final int ERROR_PACKET_INDICATOR = 0xFF;

    // only first FIVE char is useful in SQL STATE
    private byte[] sqlState = {'H', 'Y', '0', '0', '0'};
    private int errorCode = 1064;
    private String errorMessage;

    public MysqlErrPacket(QueryState state) {
        errorMessage = state.getErrorMessage();
        ErrorCode code = state.getErrorCode();
        if (code != null) {
            errorCode = code.getCode();
            sqlState = code.getSqlState();
        }
    }

    @Override
    public void writeTo(MysqlSerializer serializer) {

        MysqlCapability capability = serializer.getCapability();
        serializer.writeInt1(ERROR_PACKET_INDICATOR);
        serializer.writeInt2(errorCode);
        if (capability.isProtocol41()) {
            serializer.writeByte((byte)'#');
            serializer.writeBytes(sqlState, 0, 5);
        }
        if (errorMessage == null || errorMessage.isEmpty()) {
            // NOTICE: if write "" or "\0", the client will be show "Query OK"
            // SO we need write no-empty string
            serializer.writeEofString("Unknown error");
        } else {
            serializer.writeEofString(errorMessage);
        }

    }
}
