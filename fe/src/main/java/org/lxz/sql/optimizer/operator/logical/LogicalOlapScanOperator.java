package org.lxz.sql.optimizer.operator.logical;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.lxz.catalog.Column;
import org.lxz.catalog.Table;
import org.lxz.sql.ast.PartitionNames;
import org.lxz.sql.optimizer.base.DistributionSpec;
import org.lxz.sql.optimizer.operator.OperatorType;
import org.lxz.sql.optimizer.operator.scalar.ColumnRefOperator;
import org.lxz.sql.optimizer.operator.scalar.ScalarOperator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LogicalOlapScanOperator extends LogicalScanOperator {

    private DistributionSpec distributionSpec;

    private long selectedIndexId;

    private List<Long> selectedPartitionId;

    private PartitionNames partitionNames;
    private List<Long> selectedTabletId;

    private List<ScalarOperator> prunedPartitionPredicates;

    private boolean usePkIndex;




    public LogicalOlapScanOperator() {
        super(OperatorType.LOGICAL_OLAP_SCAN);
        this.prunedPartitionPredicates = ImmutableList.of();
    }

    public LogicalOlapScanOperator(
            Table table,
            Map<ColumnRefOperator, Column> colRefToColumnMetaMap,
            Map<Column, ColumnRefOperator> columnMetaToColRefMap,
            DistributionSpec distributionSpec,
            long limit,
            ScalarOperator predicate
    ) {
        this(table,
                colRefToColumnMetaMap,
                columnMetaToColRefMap,
                distributionSpec,
                limit,
                predicate,
                0,
                Lists.newArrayList(),
                null,
                Lists.newArrayList(),
                false
                );

    }

    public LogicalOlapScanOperator(
            Table table,
            Map<ColumnRefOperator, Column> colRefToColumnMetaMap,
            Map<Column, ColumnRefOperator> columnMetaToColRefMap,
            DistributionSpec distributionSpec,
            long limit,
            ScalarOperator predicate,
            long selectedIndexId,
            List<Long> selectedPartitionId,
            PartitionNames partitionNames,
            List<Long> selectedTabletId,
            boolean usePkIndex
    ) {
        super(OperatorType.LOGICAL_OLAP_SCAN, table, colRefToColumnMetaMap, columnMetaToColRefMap, limit, predicate);
        // check table is olap
        this.distributionSpec = distributionSpec;
        this.selectedIndexId = selectedIndexId;
        this.selectedPartitionId = selectedPartitionId;
        this.partitionNames = partitionNames;
        this.selectedTabletId = selectedTabletId;
        this.usePkIndex = usePkIndex;
    }

    public static Builder builder() {
        return new Builder();
    }


    public static class Builder extends LogicalScanOperator.Builder<LogicalOlapScanOperator, LogicalOlapScanOperator.Builder> {

        @Override
        protected LogicalOlapScanOperator newInstance() {
            return new LogicalOlapScanOperator();
        }

        @Override
        public Builder withOperator(LogicalOlapScanOperator scanOperator) {
            super.withOperator(scanOperator);

            builder.distributionSpec = scanOperator.distributionSpec;
            builder.selectedIndexId = scanOperator.selectedIndexId;
            builder.selectedPartitionId = scanOperator.selectedPartitionId;
            builder.partitionNames = scanOperator.partitionNames;
            builder.selectedTabletId = scanOperator.selectedTabletId;
            builder.usePkIndex = scanOperator.usePkIndex;
            return this;
        }

        public Builder setSelectedIndexId(long selectedIndexId) {
            builder.selectedIndexId = selectedIndexId;
            return this;
        }

        public Builder setSelectedTabletId(List<Long> selectedTabletId) {
            builder.selectedTabletId = ImmutableList.copyOf(selectedTabletId);
            return this;
        }

        public Builder setSelectedPartitionId(List<Long> selectedPartitionId) {
            if (selectedPartitionId == null) {
                builder.selectedPartitionId = null;
            } else {
                builder.selectedPartitionId = ImmutableList.copyOf(selectedPartitionId);
            }
            return this;
        }

        public Builder setPrunedPartitionPredicates(List<ScalarOperator> prunedPartitionPredicates) {
            builder.prunedPartitionPredicates = ImmutableList.copyOf(prunedPartitionPredicates);
            return this;
        }

        public Builder setDistributionSpec(DistributionSpec distributionSpec) {
            builder.distributionSpec = distributionSpec;
            return this;
        }

        public Builder setPartitionNames(PartitionNames partitionNames) {
            builder.partitionNames = partitionNames;
            return this;
        }


        public Builder setUsePkIndex(boolean usePkIndex) {
            builder.usePkIndex = usePkIndex;
            return this;
        }
    }
}
