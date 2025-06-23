package org.lxz.sql.optimizer.operator;

/**
 * RowOutputInfo is used to describe the info of output columns returned by an operator.
 * It consists of a set of columnOutputInfo. Because of the Projection field in Operator,
 * an operator with a not null projection may take the original output of this operator
 * and project it to a new output.
 *
 * To unify the output info of an operator, we use the RowOutputInfo to describe the output
 * row of this operator.
 * When an operator with a not null projection, the RowOutputInfo records the projection info
 * and the output info of the operator itself.
 * When an operator without a not null projection, the RowOutInfo only records the set of
 * columnOutputInfo of itself.
 *
 */
public class RowOutputInfo {
}
