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

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getTbl() {
        return tbl;
    }

    public void setTbl(String tbl) {
        this.tbl = tbl;
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public NodePosition getPos() {
        return pos;
    }
}
