package org.lxz.catalog;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;
import org.lxz.analysis.Expr;
import org.lxz.analysis.FunctionCallExpr;
import org.lxz.analysis.FunctionName;

import java.util.Set;

public class DefaultExpr {

    public static final Set<String> SUPPORTED_DEFAULT_FNS = ImmutableSet.of("now()", "uuid()", "uuid_numeric()");

    @SerializedName("expr")
    private String expr;

    public DefaultExpr(String expr) {
        this.expr = expr;
    }

    public String getExpr() {
        return expr;
    }

    public void setExpr(String expr) {
        this.expr = expr;
    }

    public Expr obtainExpr() {
        if (SUPPORTED_DEFAULT_FNS.contains(expr)) {
            String functionName = expr.replace("()", "");
            FunctionCallExpr functionCallExpr = new FunctionCallExpr(new FunctionName(functionName), Lists.newArrayList());
            Function fn = Expr.getBuiltinFunction(functionName, new Type[] {}, Function.CompareMode.IS_IDENTICAL);
            functionCallExpr.setFn(fn);
            functionCallExpr.setType(fn.getRetType());
            return functionCallExpr;

        }
        return null;
    }
}
