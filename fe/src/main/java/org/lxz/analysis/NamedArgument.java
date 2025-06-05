package org.lxz.analysis;

public class NamedArgument extends Expr {

    private final String name;

    private Expr expr;


    public NamedArgument(String name, Expr expr) {
        this.name = name;
        this.expr = expr;
    }

    public NamedArgument(NamedArgument arg) {
        this.name = arg.name;
        this.expr = arg.expr;
    }

    public String getName() {
        return name;
    }

    public Expr getExpr() {
        return expr;
    }

    public void setExpr(Expr expr) {
        this.expr = expr;
    }

}
