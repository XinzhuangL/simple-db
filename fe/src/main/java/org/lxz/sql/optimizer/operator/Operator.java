package org.lxz.sql.optimizer.operator;

import org.lxz.sql.optimizer.operator.scalar.ScalarOperator;

public abstract class Operator {

    public static final long DEFAULT_LIMIT = -1;
    public static final long DEFAULT_OFFSET = 0;
    protected final OperatorType opType;

    protected long limit = DEFAULT_LIMIT;

    protected ScalarOperator predicate = null;

    protected static long saltGenerator = 0;

    /**
     * Before entering the Cascades search framework.
     * we need to merge LogicalProject and child children into one node
     * to reduce the impact of LogicalProject on RULE matching
     * such as Join reorder
     */

    protected Projection projection;

    protected RowOutputInfo rowOutputInfo;

    // Add salt make the original equivalent operators nonequivalent to avoid Group
    // mutual reference in Memo.
    // Only LogicalScanOperator/PhysicalScanOperator yielded by CboTablePruneRule has salt.
    // if no salt, two different Groups will be merged into one, that leads to mutual reference
    // or self reference of groups
    protected long salt = 0;

    protected int opRuleMask = 0;

    // an operator logically equivalent to 'this' operator
    // used by view based mv rewrite
    // eg: LogicalViewScanOperator is logically equivalent to the operator build from the view
    protected Operator equivalentOp;



    public Operator(OperatorType opType) {
        this.opType = opType;
    }

    public Operator(OperatorType opType, long limit, ScalarOperator predicate, Projection projection) {
        this.opType = opType;
        this.limit = limit;
        this.predicate = predicate;
        this.projection = projection;
    }


    public abstract static class Builder<O extends Operator, B extends Builder<O, B>> {
        protected  O builder = newInstance();

        protected abstract O newInstance();

        public B withOperator(O operator) {
            builder.limit = operator.limit;
            builder.predicate = operator.predicate;
            builder.projection = operator.projection;
            builder.salt = operator.salt;
            builder.opRuleMask = operator.opRuleMask;
            builder.equivalentOp = operator.equivalentOp;
            return (B) this;
        }

        public O build() {
            O newOne = builder;
            builder = null;
            return newOne;
        }

        public OperatorType getOpType() {
            return builder.opType;
        }

        public long getLimit() {
            return builder.limit;
        }

        public B setLimit(long limit) {
            builder.limit = limit;
            return (B) this;
        }

        public ScalarOperator getPredicate() {
            return builder.predicate;
        }

        public B setPredicate(ScalarOperator predicate) {
            builder.predicate = predicate;
            return (B) this;
        }

        public Projection getProjection() {
            return builder.projection;
        }

        public B setProjection(Projection projection) {
            builder.projection = projection;
            return (B) this;
        }

        public B addSalt() {
            builder.salt = ++saltGenerator;
            return (B) this;
        }

        public B setOpBitSet(int opRuleMask) {
            builder.opRuleMask = opRuleMask;
            return (B) this;
        }




    }
}
