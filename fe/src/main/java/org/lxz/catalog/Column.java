package org.lxz.catalog;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;
import org.lxz.analysis.Expr;

/**
 * This class represents the column-related metadata.
 */
public class Column {

    public static final String CAN_NOT_CHANGE_DEFAULT_VALUE = "Can not change default value";
    public static final int COLUMN_UNIQUE_ID_INIT_VALUE = -1;

    // logical name, rename will change this name.
    @SerializedName(value = "name")
    private String name;


    // For OLAP Table and its sub classed
    // When column is created, columnId is same to name.
    // If the column name is changed, the value of name will be updated to the new column name,
    // and the value of columnId remains unchanged.

    // Fot other tables: columnId is same to name.
    // All references to Column should use columnId instead of name.
    @SerializedName(value = "columnId")
    private ColumnId columnId;

    @SerializedName(value = "type")
    private Type type;

    // column is key: aggregate type is null
    // column is not key and has no aggregate type: aggregate type is none
    // column is not key and has aggregate type: aggregate type is name of aggregate function.
    @SerializedName(value = "aggregationType")
    private AggregateType aggregationType;

    // if isAggregationTypeImplicit is true, the actual aggregation type will not be shown in show create table
    // the key type of table is duplicate or unique: the isAggregationTypeImplicit of value columns are true
    // other cases: the isAggregationTypeImplicit is false
    @SerializedName(value = "isAggregationTypeImplicit")
    private boolean isAggregationTypeImplicit;

    @SerializedName(value = "isKey")
    private boolean isKey;

    @SerializedName(value = "isAllowNull")
    private boolean isAllowNull;

    @SerializedName(value = "isAutoIncrement")
    private boolean isAutoIncrement;

    @SerializedName(value = "defaultValue")
    private String defaultValue;

    // this handle function like now() or simple exprssion
    @SerializedName(value = "defaultExpr")
    private DefaultExpr defaultExpr;

    @SerializedName(value = "comment")
    private String comment;

    @SerializedName(value = "stats")
    private ColumnStats stats; // cardinality and selectivity etc.

    // Define expr may exist in two forms, one is analyzed, and the other is not analyzed.
    // Currently, analyzed define expr is only used when creating materialized views, so the define expr in RollupJob must be analyzed.
    // In other cases, such as define expr in `MaterializedIndexMeta`, it may not be analyzed after being replayed
    // use to define column in materialize view
    private Expr defineExpr;
    @SerializedName(value = "uniqueId")
    private int uniqueId;

    // todo materializedColumnExpr

    // todo ColumnIdExpr

    public String getName() {
        return name;
    }

    public boolean isGeneratedColumn() {
        return false;
    }

    public boolean isKey() {
        return isKey;
    }

    public boolean isAllowNull() {
        return isAllowNull;
    }

    public Type getType() {
        return type;
    }

    public Column(String name, Type dataType) {
        this.name = name;
        if (this.name == null) {
            this.name = "";
        }
        this.columnId = ColumnId.create(this.name);
        this.type = dataType;
    }

}
