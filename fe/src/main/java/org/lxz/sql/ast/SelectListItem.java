package org.lxz.sql.ast;

import com.google.common.base.Preconditions;
import org.lxz.analysis.Expr;
import org.lxz.analysis.ParseNode;
import org.lxz.analysis.TableName;
import org.lxz.sql.parser.NodePosition;

public class SelectListItem implements ParseNode {
    private Expr expr;
    // for "[name.]*"
    private final TableName tblName;
    private final boolean isStar;
    private String alias;

    private final NodePosition pos;

    public SelectListItem(Expr expr, String alias) {
        this(expr, alias, NodePosition.ZERO);
    }

    public SelectListItem(TableName tblName) {
        this(tblName, NodePosition.ZERO);
    }

    public SelectListItem(Expr expr, String alias, NodePosition pos) {
        Preconditions.checkNotNull(expr);
        this.pos = pos;
        this.expr = expr;
        this.alias = alias;
        this.tblName = null;
        this.isStar = false;
    }

    public SelectListItem(TableName tblName, NodePosition pos) {
        this.pos = pos;
        this.expr = null;
        this.tblName = tblName;
        this.isStar = true;
    }

    protected SelectListItem(SelectListItem other) {
        pos = other.pos;
        if (other.expr == null) {
            expr = null;
        } else {
           // expr = other.expr.clone().reset();
        }
        tblName = other.tblName;
        isStar = other.isStar;
        alias = other.alias;
    }

    @Override
    public NodePosition getPos() {
        return pos;
    }

    @Override
    protected SelectListItem clone() {
        return new SelectListItem(this);
    }

    public boolean isStar() {
        return isStar;
    }

    public TableName getTblName() {
        return tblName;
    }

    public Expr getExpr() {
        return expr;
    }

    public void setExpr(Expr expr) {
        this.expr = expr;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
