package org.lxz.common;

public enum PrimitiveType {

    INVALID_TYPE("INVALID_TYPE", -1),
    // NULL_TYPE = used only in LiteralPredicate and NullLiteral to make NULLs compatible
    // with all other types
    NULL_TYPE("NULL_TYPE", 1),
    BOOLEAN("BOOLEAN", 1),
    TINYINT("TINYINT", 2),
    SMALLINT("SMALLINT", 2),
    INT("INT", 4),
    BIGINT("BIGINT", 8),
    LARGEINT("LARGEINT", 16),
    FLOAT("FLOAT", 4),
    DOUBLE("DOUBLE", 8),
    DATE("DATE", 16),
    DATETIME("DATETIME", 16),
    // Fixed length char array.
    CHAR("CHAR", 16),
    // 8-byte pointer and 4-byte length indicator(12 bytes total).
    // Aligning to 8 bytes so 16 total.
    VARCHAR("VARCHAR", 16),
    DECIMALV2("DECIMALV2", 16),

    HLL("HLL", 16),
    TIME("TIME", 8),
    // we use OBJECT type represent BITMAP type in backend
    BITMAP("BITMAP", 16),
    PERCENTILE("PERCENTILE", 16),
    DECIMAL32("DECIMAL32", 4),
    DECIMAL64("DECIMAL64", 8),
    DECIMAL128("DECIMAL128", 16),

    JSON("JSON", 16),

    FUNCTION("FUNCTION", 8),

    // Unsupported scalar types
    BINARY("BINARY", -1),
    // If external table column type is unsupported, it will be converted to UNKNOWN_TYPE
    UNKNOWN_TYPE("UNKNOWN_TYPE", -1);


    private final String description;
    private final int slotSize;

    PrimitiveType(String description, int slotSize) {
        this.description = description;
        this.slotSize = slotSize;
    }

    // TODO: Add Mysql Type to it's private field
    public MysqlColType toMysqlType() {
        switch (this) {
            // MySQL use Tinyint(1) to represent boolean
            case BOOLEAN:
            case TINYINT:
                return MysqlColType.MYSQL_TYPE_TINY;
            case SMALLINT:
                return MysqlColType.MYSQL_TYPE_SHORT;
            case INT:
                return MysqlColType.MYSQL_TYPE_LONG;
            case BIGINT:
                return MysqlColType.MYSQL_TYPE_LONGLONG;
            case FLOAT:
                return MysqlColType.MYSQL_TYPE_FLOAT;
            case DOUBLE:
                return MysqlColType.MYSQL_TYPE_DOUBLE;
            case TIME:
                return MysqlColType.MYSQL_TYPE_TIME;
            case DATE:
                return MysqlColType.MYSQL_TYPE_DATE;
            case DATETIME:
                // todo return date time now()
                return MysqlColType.MYSQL_TYPE_DATETIME;
            case DECIMALV2:
            case DECIMAL32:
            case DECIMAL64:
            case DECIMAL128:
                return MysqlColType.MYSQL_TYPE_NEWDECIMAL;
            case VARCHAR:
                return MysqlColType.MYSQL_TYPE_VAR_STRING;
            default:
                return MysqlColType.MYSQL_TYPE_STRING;
        }
    }
}
