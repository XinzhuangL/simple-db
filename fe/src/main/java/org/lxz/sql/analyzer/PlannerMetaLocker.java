package org.lxz.sql.analyzer;

import org.lxz.ConnectContext;
import org.lxz.sql.ast.StatementBase;

/**
 * QueryLocker is used to obtain the lock corresponding to the metadata used in Query
 * Will acquire the read locks of all tables involved in the query
 * and the intention read locks of the database where the tables are located.
 *
 * In terms of compatibility, when Config.use_lock_manager is turned off,
 * it will be consistent with the original db-lock logic
 * and obtain the db-read-lock of all dbs involved in the query.
 *
 */
public class PlannerMetaLocker {
    // todo

    public PlannerMetaLocker(ConnectContext session, StatementBase statementBase) {

    }
}
