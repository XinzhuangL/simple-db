package org.lxz.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lxz.ConnectContext;
import org.lxz.catalog.Database;
import org.lxz.catalog.Table;
import org.lxz.connector.ConnectorMetadata;

import java.util.Optional;

public class MetadataMgr {
    private static final Logger LOG = LogManager.getLogger(MetadataMgr.class);


    private final LocalMetastore localMetastore;

    public MetadataMgr(LocalMetastore localMetastore) {
        this.localMetastore = localMetastore;
    }

    public Database getDb(String catalogName, String dbName) {
        Optional<ConnectorMetadata> connectorMetadata = Optional.of(localMetastore);
        Database db = connectorMetadata.map(metadata -> metadata.getDb(dbName)).orElse(null);
        return db;
    }

    public Table getTable(String catalogName, String dbName, String tblName) {
        Optional<ConnectorMetadata> connectorMetadata = Optional.of(localMetastore);
        Table connectorTable = connectorMetadata.map(metadata -> metadata.getTable(dbName, tblName)).orElse(null);
        return connectorTable;
    }


}
