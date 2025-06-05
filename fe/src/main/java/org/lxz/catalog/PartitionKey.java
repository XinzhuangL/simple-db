package org.lxz.catalog;

import org.lxz.analysis.LiteralExpr;
import org.lxz.common.PrimitiveType;

import java.util.List;

public class PartitionKey implements Comparable<PartitionKey> {

    private List<LiteralExpr> keys;
    private List<PrimitiveType> types;

    // Records the string corresponding to partition value when partition value is null
    // for hive, it's __HIVE_DEFAULT_PARTITION__
    // for hudi, it's __HIVE_DEFAULT_PARTITION__ or default

    private String nullPartitionValue = "";

    // todo DateLiteral SHADOW_DATE_LITERAL



    @Override
    public int compareTo(PartitionKey o) {
        return 0;
    }
}
