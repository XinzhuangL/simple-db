package org.lxz.sql.optimizer.operator.logical;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.lxz.catalog.Column;
import org.lxz.catalog.Table;
import org.lxz.sql.optimizer.ScanOptimizeOption;
import org.lxz.sql.optimizer.operator.Operator;
import org.lxz.sql.optimizer.operator.OperatorType;
import org.lxz.sql.optimizer.operator.scalar.ColumnRefOperator;
import org.lxz.sql.optimizer.operator.scalar.ScalarOperator;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public abstract class LogicalScanOperator extends LogicalOperator {

    protected LogicalScanOperator(OperatorType opType) {
        super(opType);
        this.colRefToColumnMetaMap = ImmutableMap.of();
        this.columnMetaToColRefMap = ImmutableMap.of();
        this.scanOptimizeOption = new ScanOptimizeOption();
    }

    protected Table table;

    /**
     * colRefToColumnMetaMap is the map from column reference to StarRocks column in meta
     * The ColumnRefMap contains Scan output columns and predicate used columns
     */
    protected ImmutableMap<ColumnRefOperator, Column> colRefToColumnMetaMap;
    protected ImmutableMap<Column, ColumnRefOperator> columnMetaToColRefMap;
    // columnFilters

    // partitionColumns
    protected Set<String> partitionColumns = Sets.newHashSet();
    // for struct map etc..
    // protected ImmutableList<ColumnAccessPath> columnAccessPaths;
    protected ScanOptimizeOption scanOptimizeOption;


    public LogicalScanOperator(
            OperatorType type,
            Table table,
            Map<ColumnRefOperator, Column> colRefToColumnMetaMap,
            Map<Column, ColumnRefOperator> columnMetaToColRefMap,
            long limit,
            ScalarOperator predicate

    ) {
        super(type);
        this.table = Objects.requireNonNull(table, "table is null");
        this.colRefToColumnMetaMap = ImmutableMap.copyOf(colRefToColumnMetaMap);
        this.columnMetaToColRefMap = ImmutableMap.copyOf(columnMetaToColRefMap);
        this.scanOptimizeOption = new ScanOptimizeOption();
    }

    public Table getTable() {
        return table;
    }

    public ImmutableMap<ColumnRefOperator, Column> getColRefToColumnMetaMap() {
        return colRefToColumnMetaMap;
    }

    public ImmutableMap<Column, ColumnRefOperator> getColumnMetaToColRefMap() {
        return columnMetaToColRefMap;
    }

    public Set<String> getPartitionColumns() {
        return partitionColumns;
    }

    public ScanOptimizeOption getScanOptimizeOption() {
        return scanOptimizeOption;
    }

    public abstract static class Builder<O extends LogicalScanOperator, B extends LogicalScanOperator.Builder<O, B>> extends
            Operator.Builder<O, B> {

        @Override
        public B withOperator(O scanOperator) {
            super.withOperator(scanOperator);
            builder.table = scanOperator.table;
            builder.columnMetaToColRefMap = scanOperator.columnMetaToColRefMap;
            builder.colRefToColumnMetaMap = scanOperator.colRefToColumnMetaMap;
            builder.scanOptimizeOption = scanOperator.scanOptimizeOption;
            builder.partitionColumns = scanOperator.partitionColumns;
            return (B) this;
        }

        @Override
        public O build() {
            return super.build();
        }

        public B setColRefToColumnMetaMap(Map<ColumnRefOperator, Column> colRefToColumnMetaMap) {
            builder.colRefToColumnMetaMap = ImmutableMap.copyOf(colRefToColumnMetaMap);
            return (B) this;
        }

        public B setColumnMetaToColRefMap(Map<Column, ColumnRefOperator> columnMetaToColRefMap) {
            builder.columnMetaToColRefMap = ImmutableMap.copyOf(columnMetaToColRefMap);
            return (B) this;
        }


        public B setTable(Table table) {
            builder.table = table;
            return (B) this;
        }
    }
}
