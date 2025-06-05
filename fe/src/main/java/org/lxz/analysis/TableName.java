package org.lxz.analysis;

import org.lxz.sql.parser.NodePosition;

public class TableName {

    private String catalog;
    private String tbl;
    private String db;

    private final NodePosition pos;

    public TableName() {
        pos = NodePosition.ZERO;
    }

    public TableName(String db, String tbl) {
        this(null, db, tbl, NodePosition.ZERO);
    }

    public TableName(String catalog, String db, String tbl) {
        this(catalog, db, tbl, NodePosition.ZERO);
    }

    public TableName(String catalog, String db, String tbl, NodePosition pos) {
        this.pos = pos;
        this.catalog = catalog;
        this.db = db;
        this.tbl = tbl;
    }


}
