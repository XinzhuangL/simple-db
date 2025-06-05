package org.lxz.catalog;

public enum AggregateType {

    SUM("SUM"),
    MIN("MIN");
    // todo etc


    private final String sqlName;

    AggregateType(String sqlName) {
        this.sqlName = sqlName;
    }

}
