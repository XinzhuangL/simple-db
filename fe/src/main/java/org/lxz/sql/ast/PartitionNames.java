package org.lxz.sql.ast;

import com.google.gson.annotations.SerializedName;
import org.lxz.analysis.Expr;
import org.lxz.analysis.ParseNode;
import org.lxz.sql.parser.NodePosition;

import java.util.List;

/**
 * To represent following stmt:
 *  PARTITION p1
 *  TEMPORARY PARTITION p1
 *  PARTITION (p1, p2)
 *  TEMPORARY PARTITION (p1, p2)
 *  PARTITIONS (p1, p2)
 *  TEMPORARY PARTITIONS (p1, p2)
 */
public class PartitionNames implements ParseNode {

    @SerializedName(value = "partitionNames")
    private final List<String> partitionNames;
    // trie of these partitions are temp partitions
    @SerializedName(value = "isTemp")
    private final boolean isTemp;

    /**
     *  partition_names is ["p1=1/p2=2", "p1=5/p2=6"] in the hive or spark. The concept isn't the same as the partition_names
     *  in current StarRocks. So we use partitionColNames to denote the names of partition columns,
     *  and partitionColValues to denote their corresponding values.
     *
     *  Static partition insert hive/iceberg/hudi table
     *  for example:
     *      create external target_external_table (c1 int, c2 int, p1 int, p2 int) partition by (p1, p2);
     *      insert into target_external_table partition(p1=1,p2=2) select a1, a2 from source_table;
     *
     *  The partitionColNames is ["p1", "p2"]
     *  The partitionColValues is [expr(1), expr(2)]
     *
     */
    private final List<String> partitionColNames;
    private final List<Expr> partitionColValues;

    private final NodePosition pos;

    public PartitionNames(boolean isTemp, List<String> partitionNames, List<String> partitionColNames,
                          List<Expr> partitionColValues, NodePosition pos) {
        this.pos = pos;
        this.partitionNames = partitionNames;
        this.partitionColNames = partitionColNames;
        this.partitionColValues = partitionColValues;
        this.isTemp = isTemp;
    }


    @Override
    public NodePosition getPos() {
        return null;
    }
}
