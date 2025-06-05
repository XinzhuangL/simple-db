package org.lxz.analysis;

import com.google.common.collect.ImmutableSet;
import org.lxz.catalog.Function;
import org.lxz.catalog.FunctionSet;
import org.lxz.catalog.Type;
import org.lxz.sql.parser.NodePosition;

import java.util.List;

public class FunctionCallExpr extends Expr {
    private FunctionName fnName;

    private FunctionParams fnParams;

    // check analytic function
    private boolean isAnalyticFnCall =false;


    // Indicates whether this is a merge aggregation function that should use the merge
    // instead of the update symbol. This flag also affects the behavior of
    // resetAnalysisState() which is used during expr substitution.
    private boolean isMergeAggFn;

    // Indicates merge aggregation function whether has nullable child
    // because when create merge agg fn from update agg fn,
    // The slot SlotDescriptor nullable info will lost or change
    private boolean mergeAggFnHasNullableChild = true;

    // TODO(yan): add more known functions which are monotonic.
    private static final ImmutableSet<String> MONOTONIC_FUNCTION_SET =
            new ImmutableSet.Builder<String>().add(FunctionSet.YEAR).build();

    public boolean isAnalyticFnCall() {
        return isAnalyticFnCall;
    }

    public void setIsAnalyticFnCall(boolean v) {
        isAnalyticFnCall = v;
    }

    public Function getFn() {
        return fn;
    }


    public FunctionCallExpr(FunctionName fnName, List<Expr> params) {
        this(fnName, new FunctionParams(false, params), NodePosition.ZERO);
    }


    public FunctionCallExpr(FunctionName fnName, FunctionParams params, NodePosition pos) {
        this(fnName, params, false, pos);
    }

    private FunctionCallExpr(
            FunctionName fnName, FunctionParams params, boolean isMergeAggFn, NodePosition pos
    ) {
        super(pos);
        this.fnName = fnName;
        fnParams = params;
        this.isMergeAggFn = isMergeAggFn;
        if (params.getExprs() != null) {
            children.addAll(params.getExprs());
        }
    }



    @Override
    public NodePosition getPos() {
        return null;
    }

    public void setFn(Function fn) {
        this.fn = fn;
    }

    public void setType(Type retType) {
        this.type =  retType;
    }
}
