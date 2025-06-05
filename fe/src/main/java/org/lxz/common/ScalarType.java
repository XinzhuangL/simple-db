package org.lxz.common;

import org.lxz.catalog.Type;

public class ScalarType extends Type {

    private int len = -1;
    private final PrimitiveType type;
    private int precision;

    private int scale;

    public static ScalarType createVarcharType(int len) {
        // length checked in analysis
        ScalarType type = new ScalarType(PrimitiveType.VARCHAR);
        type.len = len;
        return type;
    }

    @Override
    public PrimitiveType getPrimitiveType() {
        return type;
    }

    public ScalarType(PrimitiveType type) {
        this.type = type;
    }

    public int getScalarPrecision() {
        return precision;
    }

    public int getScalarScale() {

        return scale;
    }

    public int getLength() {

        return len;
    }
}
