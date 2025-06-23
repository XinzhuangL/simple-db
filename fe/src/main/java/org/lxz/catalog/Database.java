package org.lxz.catalog;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Internal representation of db-related metadata. Owned by GlobalStateMgr instance.
 * Not thread safe.
 * The static initialization method loadDb is the only way to construct a Db
 * object.
 *
 * Tables are stored in a map from the table name to the table object. They may
 * be loaded 'eagerly' at construction or 'lazily' on first reference. Tables
 * are accessed via getTable which may trigger a metadata read in two cases: *
 * if the table has never been loaded * if the table loading failed on the previous
 * attempt
 */
public class Database {
    private static final Logger LOG = LogManager.getLogger(Database.class);

    private final Map<String, Table> nameToTable;

    private static final String TEST_TBL_NAME = "test_tbl";


    public Database() {
        this.nameToTable = Maps.newHashMap();
        this.nameToTable.put(TEST_TBL_NAME, initTestTable(TEST_TBL_NAME));
    }

    // empirical value.
    // assume that the time a lock is held by thread is less than 100ms


    public Table getTable(String tableName) {
        return nameToTable.get(tableName);
    }

    // for test
    public Table initTestTable(String tableName) {
        Table table = new Table();
        List<Column> cols = Lists.newArrayList();
        cols.add(new Column("col1", Type.VARCHAR));
        cols.add(new Column("col2", Type.INT));
        table.getFullSchema().addAll(cols);
        table.setName(tableName);
        return table;
    }
}
