package org.lxz.sql.analyzer;

import org.lxz.ConnectContext;
import org.lxz.analysis.Expr;
import org.lxz.analysis.SlotRef;
import org.lxz.analysis.TableName;
import org.lxz.catalog.Type;

import java.util.LinkedList;
import java.util.List;

public class Field {

    // The name here is a column name, not qualified name.
    private final String name;
    private Type type;
    // shadow column is not visible, e.g. schema change column and materialized column
    private final boolean visible;

    /**
     * TableName of field
     * relationAlias is origin table which table name is explicit, such as t0.a
     * Field come from scope is resolved by scope relation alias,
     * such as subquery alias and table relation name
     */
    private final TableName relationAlias;
    private final Expr originExpression;
    private boolean isNullable;

    // Record tmp match record.
    private final List<Integer> tmpUsedStructFieldPos = new LinkedList<>();

    public Field(String name, Type type, TableName relationAlias, Expr originExpression, boolean visible, boolean isNullable) {
        this.name = name;
        this.type = type;
        this.relationAlias = relationAlias;
        this.originExpression = originExpression;
        this.visible = visible;
        this.isNullable = isNullable;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public boolean canResolve(SlotRef expr) {
        // struct

        TableName tableName = expr.getTblNameWithoutAnalyzed();
        if (tableName != null) {
            if (relationAlias == null) {
                return false;
            }
            return matchesPrefix(expr.getTblNameWithoutAnalyzed()) && expr.getColName().equalsIgnoreCase(this.name);
        } else {
            return expr.getColName().equalsIgnoreCase(this.name);
        }
    }

    public boolean matchesPrefix(TableName tableName) {
        if (tableName.getCatalog() != null && relationAlias.getCatalog() != null &&
        !tableName.getCatalog().equals(relationAlias.getCatalog())) {
            return false;
        }
        if (tableName.getDb() != null && !tableName.getDb().equals(relationAlias.getDb())) {
            return false;
        }
        return tableName.getTbl().equalsIgnoreCase(relationAlias.getTbl());
    }

    public TableName getRelationAlias() {
        return relationAlias;
    }

    public boolean isNullable() {
        return isNullable;
    }
}
