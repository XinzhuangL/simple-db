package org.lxz;

import org.lxz.common.Type;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

// used for serialize memory data to byte stream of MySQL protocol
public class MysqlSerializer {
    private ByteArrayOutputStream out;
    private MysqlCapability capability;

    private MysqlSerializer(ByteArrayOutputStream out) {
        this(out, MysqlCapability.DEFAULT_CAPABILITY);
    }

    private MysqlSerializer(ByteArrayOutputStream out, MysqlCapability capability) {
        this.out = out;
        this.capability = capability;
    }

    public static MysqlSerializer newInstance() {
        return new MysqlSerializer(new ByteArrayOutputStream());
    }

    public static MysqlSerializer newInstance(MysqlCapability capability) {
        return new MysqlSerializer(new ByteArrayOutputStream(), capability);
    }

    // used after success handshake
    public void setCapability(MysqlCapability capability) {
        this.capability = capability;
    }

    public MysqlCapability getCapability() {
        return capability;
    }

    public void writeByte(byte value) {
        out.write(value);
    }

    public void writeNull() {
        writeByte((byte) (251 & 0xff));
    }

    public void writeBytes(byte[] value, int offset, int length) {
        out.write(value, offset, length);
    }

    public void reset() {
        out.reset();
    }

    public byte[] toArray() {
        return out.toByteArray();
    }

    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(out.toByteArray());
    }

    public void writeBytes(byte[] value) { writeBytes(value, 0, value.length); }

    public void writeInt1(int value) {
        writeByte((byte)(value & 0xFF));
    }

    public void writeInt2(int value) {
        writeByte((byte) value);
        writeByte((byte) (value >> 8));
    }

    public void writeInt3(int value) {
        writeByte((byte) value);
        writeByte((byte) (value >> 8));
        writeByte((byte) (value >> 16));
    }

    public void writeInt4(int value) {
        writeByte((byte) value);
        writeByte((byte) (value >> 8));
        writeByte((byte) (value >> 16));
        writeByte((byte) (value >> 24));
    }

    public void writeInt6(long value) {
        writeInt4((int) value);
        writeInt2((byte) (value >> 32));
    }

    public void writeInt8(long value) {
        writeInt4((int) value);
        writeInt4((int) (value >> 32));
    }

    public void writeVInt(long value) {
        if (value < 251) {
            writeByte((byte) value);
        } else if (value < 0x10000) {
            writeInt1(252);
            writeInt2((int) value);
        } else if (value < 0x1000000) {
            writeInt1(253);
            writeInt3((int) value);
        } else {
            writeInt1(254);
            writeInt8(value);
        }
    }

    public void writeLenEncodedString(String value) {
        byte[] buf = value.getBytes(StandardCharsets.UTF_8);
        writeVInt(buf.length);
        writeBytes(buf);
    }

    public void writeEofString(String value) {
        byte[] buf = value.getBytes(StandardCharsets.UTF_8);
        writeBytes(buf);
    }

    public void writeNulTerminateString(String value) {
        byte[] buf = value.getBytes(StandardCharsets.UTF_8);
        writeBytes(buf);
        writeByte((byte)0);
    }

    // writeField Column

    /**
     * Format field with name and type using Protocol:ColumnDefinition41
     * https://dev.mysql.com/doc/internals/en/com-query-response.html#column-definition
     *
     */
    public void writeField(String colName, Type type) {

        // GlobalStateMgr Name: length encoded string
        writeLenEncodedString("def");
        // Schema: length encoded string
        writeLenEncodedString("");
        // Table: length encoded string
        writeLenEncodedString("");
        // Origin Table: length encoded string
        writeLenEncodedString("");
        // Name: length encoded string
        writeLenEncodedString(colName);
        // Original Name: length encoded string
        writeLenEncodedString(colName);
        // length of the following fields(always 0x0c)
        writeVInt(0x0c);
        // Character set: two byte integer
        writeInt2(type.getMysqlResultSetFieldCharsetIndex());
        // Column length: four byte integer
        writeInt4(type.getMysqlResultSetFieldLength());
        // Column type: one byte integer
        writeInt1(type.getMysqlResultType().getCode());
        // Flags: two byte integer
        writeInt2(0);
        // Decimals: one byte integer
        writeInt1(type.getMysqlResultSetFieldDecimals());
        // filler: two byte integer
        writeInt2(0);

    }





}
