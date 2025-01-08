package org.lxz.mysql.packet;

import com.google.common.base.Strings;
import org.lxz.mysql.server.MysqlCapability;
import org.lxz.mysql.server.MysqlSerializer;
import org.lxz.common.QueryState;

// MySQL protocol OK packet
public class MysqlOkPacket extends MysqlPacket {

    private static final int PACKET_OK_INDICATOR = 0x00;

    // todo following are not used
    private static final long LAST_INTERT_ID = 0;
    private final String infoMessage;
    private long affectedRows = 0;
    private int warningRows = 0;
    private int serverStatus = 0;

    public MysqlOkPacket(QueryState state) {
        infoMessage = state.getInfoMessage();
        affectedRows = state.getAffectedRows();
        warningRows = state.getWarningRows();
        serverStatus = state.serverStatus;
    }

    @Override
    public void writeTo(MysqlSerializer serializer) {
        // used to check
        MysqlCapability capability = serializer.getCapability();
        serializer.writeInt1(PACKET_OK_INDICATOR);
        serializer.writeVInt(affectedRows);
        serializer.writeVInt(LAST_INTERT_ID);
        if (capability.isProtocol41()) {
            serializer.writeInt2(serverStatus);
            serializer.writeInt2(warningRows);
        } else if (capability.isTransactions()) {
            serializer.writeInt2(serverStatus);
        }

        if (capability.isSessionTrack()) {
            serializer.writeLenEncodedString(infoMessage);
            // TODO STATUS_FALGS
        } else {
            if (!Strings.isNullOrEmpty(infoMessage)) {
                // NOTE: in datasheet, use EOF string, but in the code, mysql use length encoded string
                serializer.writeLenEncodedString(infoMessage);
            }
        }

    }
}
