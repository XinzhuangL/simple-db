package org.lxz.sql.optimizer.operator.logical;

import org.lxz.sql.optimizer.operator.Operator;
import org.lxz.sql.optimizer.operator.OperatorType;

public abstract class LogicalOperator extends Operator {

    protected LogicalOperator(OperatorType opType) {
        super(opType);
    }





}
