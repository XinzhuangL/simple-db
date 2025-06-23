package org.lxz.sql.optimizer.operator.scalar;

import org.lxz.catalog.Type;
import org.lxz.sql.optimizer.operator.Operator;
import org.lxz.sql.optimizer.operator.OperatorType;

import static java.util.Objects.requireNonNull;

/**
 * Scalar operator support variable, represent reference of column variable
 */
public final class ColumnRefOperator extends ScalarOperator {
    private final int id;
    private final String name;
    private boolean nullable;

    public ColumnRefOperator(int id, Type type, String name, boolean nullable) {
        super(OperatorType.VARIABLE, type);
        this.id = id;
        this.name = requireNonNull(name, "name is null");;
        this.nullable = nullable;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isNullable() {
        return nullable;
    }
}
