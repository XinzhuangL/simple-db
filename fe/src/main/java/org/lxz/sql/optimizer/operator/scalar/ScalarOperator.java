package org.lxz.sql.optimizer.operator.scalar;

import org.lxz.catalog.Type;
import org.lxz.sql.optimizer.operator.OperatorType;

import static java.util.Objects.requireNonNull;

public abstract class ScalarOperator {

    protected final OperatorType opType;
    protected Type type;

    public ScalarOperator(OperatorType opType, Type type) {
        this.opType = requireNonNull(opType, "opType is nul");
        this.type = requireNonNull(type, "type is null");
    }

    public OperatorType getOpType() {
        return opType;
    }

    public Type getType() {
        return type;
    }
}
