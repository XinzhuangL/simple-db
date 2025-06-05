package org.lxz.catalog;

import org.lxz.common.MysqlColType;
import org.lxz.common.PrimitiveType;
import org.lxz.common.ScalarType;

// may abstract
public class Type {


    public static final int BINARY = 63;
    public static final int CHARSET_UTF8 = 33;



    // todo other type
    public static final ScalarType VARCHAR = ScalarType.createVarcharType(-1);



    public boolean isScalarType() {
        return this instanceof ScalarType;
    }

    public PrimitiveType getPrimitiveType() {
        return PrimitiveType.INVALID_TYPE;
    }

    /**
     *
     * @return 33 (utf8_general_ci) if type is char varchar hll or bitmap
     * 63 (binary) others
     * https://dev.mysql.com/doc/internals/en/com-query-response.html#column-definition
     * character_set (2) -- is the column character set and is defined in Protocol::CharacterSet.
     */
    public int getMysqlResultSetFieldCharsetIndex() {
        switch (this.getPrimitiveType()) {
            case CHAR:
            case VARCHAR:
            case HLL:
            case BITMAP:
            // Because mysql does not have a large int type, mysql will treat it as hex after exceeding bigint
            case LARGEINT:
            case JSON:
                return CHARSET_UTF8;
            default:
                return BINARY;
        }
    }

    /**
     * https://dev.mysql.com/doc/internals/en/com-query-response.html#column-definition
     * column_length (4) -- maximum length of the field
     * Maximum length of result of evaluating this item, in number of bytes.
     * - For character or blob data types, max char length multiplied by max
     * character size (collation.mbmaxlen).
     * - For decimal type, it is the precision in digits plus sign (unless
     * unsigned) plus decimal point (unless it has zero decimals).
     * - For other numeric types, the default or specific display length.
     * - For date/time types, the display length (10 for DATE, 10 +optional FSP
     * for Time, 19 + optional fsp for datetime/timestamp). fsp is the fractional seconds precision.
     *
     */
    public int getMysqlResultSetFieldLength() {
        switch (this.getPrimitiveType()) {
            case BOOLEAN:
                return 1;
            case TINYINT:
                return 4;
            case SMALLINT:
                return 6;
            case INT:
                return 11;
            case BIGINT:
                return 20;
            case LARGEINT:
                return 40;
            case DATE:
                return 10;
            case DATETIME:
                return 19;
            case FLOAT:
                return 12;
            case DOUBLE:
                return 22;
            case DECIMALV2:
            case DECIMAL32:
            case DECIMAL64:
            case DECIMAL128:
                // precision + (scale > 0 ? 1 : 0) + (unsigned_flag || !precision ? 0 : 1));
                ScalarType decimalType = (ScalarType) this;
                int length = decimalType.getScalarPrecision();
                // when precision is 0 it means that original length was also 0.
                if (length == 0) {
                    return 0;
                }
                // scale > 0
                if (decimalType.getScalarScale() > 0) {
                    length += 1;
                }
                // one byte for sign
                // one byte for zero, if precision == scale
                // one byte for overflow but valid decimal
                return length + 3;
            case CHAR:
            case VARCHAR:
            case HLL:
            case BITMAP:
                ScalarType charType = (ScalarType) this;
                int charLength = charType.getLength();
                if (charLength == -1) {
                    charLength = 64;
                }
                // utf8 charset
                return charLength * 3;
            default:
                // Treat ARRAY/MAP/STRUCT as VARCHAR(-1)
                return 60;
        }
    }

    public MysqlColType getMysqlResultType() {
        if (isScalarType()) {
            return getPrimitiveType().toMysqlType();
        }
        return Type.VARCHAR.getPrimitiveType().toMysqlType();
    }

    /** @return scalar scale if type is decimal
     *  31 if type is float or double
     *  0 others
     *  <p>
     *  https://dev.mysql.com/doc/internals/en/com-query-response.html#column-definition
     *  decimals (1) -- max shown decimal digits
     *  0x00 for integers and static strings
     *  0x1f for dynamic strings, double float
     *  0x00 to 0x51 for decimals
     */
    public int getMysqlResultSetFieldDecimals() {
        switch (this.getPrimitiveType()) {
            case DECIMALV2:
            case DECIMAL32:
            case DECIMAL64:
            case DECIMAL128:
                return ((ScalarType) this).getScalarScale();
            case FLOAT:
            case DOUBLE:
                return 31;
            default:
                return 0;
        }
    }


}
