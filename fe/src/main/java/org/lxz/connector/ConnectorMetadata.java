package org.lxz.connector;

import org.lxz.catalog.Database;
import org.lxz.catalog.Table;

public interface ConnectorMetadata {


    default Database getDb(String name) {
        return null;
    }

    /**
     * Get Table descriptor for the table specific by `dbName`.`tblName`
     *
     * @param dbName - the string represents the database name
     * @param tblName - the string represents the table name
     * @return a Table instance
     */
    default Table getTable(String dbName, String tblName) {
        return null;
    }

}
