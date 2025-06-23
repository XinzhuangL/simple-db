package org.lxz.server;

import org.lxz.catalog.Database;
import org.lxz.catalog.Table;
import org.lxz.connector.ConnectorMetadata;

import java.util.concurrent.ConcurrentHashMap;

public class LocalMetastore implements ConnectorMetadata {

    private final ConcurrentHashMap<String, Database> fullNameToDb = new ConcurrentHashMap<>();

    public LocalMetastore() {
        // add test db
        fullNameToDb.put("test_db", new Database());
    }

    @Override
    public Database getDb(String name) {
        if (name == null) {
            return null;
        }
        if (fullNameToDb.containsKey(name)) {
            return fullNameToDb.get(name);
        }
        return null;
    }

    @Override
    public Table getTable(String dbName, String tblName) {
        Database database = getDb(dbName);
        if (database == null) {
            return null;
        }
        return database.getTable(tblName);
    }
}
