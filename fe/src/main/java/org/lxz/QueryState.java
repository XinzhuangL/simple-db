package org.lxz;

import com.google.common.collect.ImmutableMap;
import org.lxz.packet.MysqlEofPacket;
import org.lxz.packet.MysqlPacket;
import org.lxz.packet.MysqlErrPacket;
import org.lxz.packet.MysqlOkPacket;

// query state used to record state of query, maybe query status is better
public class QueryState {
    public enum MysqlStateType {
        NOOP, //send nothing to remote
        OK, // send OK packet to remote
        EOF, // send EOF package to remote
        ERR; // send ERROR packet to remote

        private static final ImmutableMap<String, MysqlStateType> STATES =
                new ImmutableMap.Builder<String, MysqlStateType>()
                        .put("NOOP", NOOP)
                        .put("OK", OK)
                        .put("EOF", EOF)
                        .put("ERR", ERR)
                        .build();
        public static MysqlStateType fromString(String state) {
            return STATES.get(state);
        }
    }

    public enum ErrType {
        ANALYSIS_ERR,
        OTHER_ERR
    }

    private MysqlStateType stateType = MysqlStateType.OK;
    private String errorMessage = "";
    private ErrorCode errorCode;
    private String infoMessage;
    private ErrType errType = ErrType.OTHER_ERR;
    private boolean isQuery = false;
    private long affectedRows = 0;
    private int warningRows = 0;
    // make it public for easy to use
    public int serverStatus = 0;

    public QueryState() {}

    public void reset() {
        stateType = MysqlStateType.OK;
       // errorCode = null;
        infoMessage = null;
        serverStatus = 0;
        isQuery = false;
    }
    public MysqlStateType getStateType() {
        return stateType;
    }

    public void setEof() {
        stateType = MysqlStateType.EOF;
    }

    public void setOk() {
        setOk(0, 0, null);
    }

    public void setOk(long affectedRows, int warningRows, String infoMessage) {
        this.affectedRows = affectedRows;
        this.warningRows = warningRows;
        this.infoMessage = infoMessage;
        stateType = MysqlStateType.OK;
    }

    public void setError(String errorMsg) {
        this.stateType = MysqlStateType.ERR;
        this.errorMessage = errorMsg;
    }

    public boolean isError() {
        return stateType == MysqlStateType.ERR;
    }

    public boolean isRunning() {
        return stateType == MysqlStateType.OK;
    }

    public void setStateType(MysqlStateType stateType) {
        this.stateType = stateType;
    }

    public void setMsg(String msg) {
        this.errorMessage = msg;
    }

    public void setErrType(ErrType errType) {
        this.errType = errType;
    }

    public ErrType getErrType() {
        return errType;
    }

    public void setIsQuery(boolean isQuery) {
        this.isQuery = isQuery;
    }

    public boolean isQuery() {
        return isQuery;
    }

    public String getInfoMessage() {
        return infoMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public long getAffectedRows() {
        return affectedRows;
    }

    public int getWarningRows() {
        return warningRows;
    }

    public MysqlPacket toResponsePacket() {
        MysqlPacket packet = null;
        switch (stateType) {
             case OK:
                 packet = new MysqlOkPacket(this);
                 break;
            case EOF:
                packet = new MysqlEofPacket(this);
                break;
            case ERR:
                packet = new MysqlErrPacket(this);
                break;
            default:
                break;
        }
        return packet;
    }

    @Override
    public String toString() {
        return String.valueOf(stateType);
    }
}
