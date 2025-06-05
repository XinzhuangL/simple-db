package org.lxz.analysis;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Return value of the grammar production that parses function
 * parameters. These parameters can be for scalar or aggregate functions.
 */
public class FunctionParams {
    private boolean isStar;
    private List<Expr> exprs;

    private List<String> exprsNames;
    private boolean isDistinct;

    // use for window function ?
    private List<OrderByElement> orderByElements;

    // c'tor for non-star params
    public FunctionParams(boolean isDistinct, List<Expr> exprs) {
        if (exprs.stream().anyMatch(e -> e instanceof NamedArgument)) {
            this.exprs = exprs.stream().map(e -> (e instanceof NamedArgument ? ((NamedArgument) e).getExpr() : e)).collect(
                    Collectors.toList());
            this.exprsNames = exprs.stream().map(e -> (e instanceof NamedArgument ? ((NamedArgument) e).getName() : "")).collect(
                    Collectors.toList());
        } else {
            this.exprs = exprs;
        }
        isStar = false;
        this.isDistinct = isDistinct;
        this.orderByElements = null;
    }

    public List<Expr> getExprs() {
        return exprs;
    }
}
