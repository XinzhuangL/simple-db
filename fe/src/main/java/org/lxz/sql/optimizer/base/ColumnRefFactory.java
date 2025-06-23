package org.lxz.sql.optimizer.base;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.lxz.catalog.Column;
import org.lxz.catalog.Table;
import org.lxz.catalog.Type;
import org.lxz.sql.optimizer.operator.scalar.ColumnRefOperator;

import java.util.List;
import java.util.Map;

public class ColumnRefFactory {
    private int nextId = 1;
    // The unique id for each scan operator
    // For table a join table a, the two unique ids for table a is different
    private int nextRelationId = 1;
    private final List<ColumnRefOperator> columnRefs = Lists.newArrayList();
    private final Map<Integer, Integer> columnToRelationIds = Maps.newHashMap();
    private final Map<ColumnRefOperator, Column> columnRefToColumns = Maps.newHashMap();
    private final Map<ColumnRefOperator, Table> columnRefToTable = Maps.newHashMap();

    // introduced to used to get unique id for query,
    // now used to identify nondeterministic function.
    // do not reuse nextId because it will affect many UTs
    private int id = -1;

    public int getNextRelationId() {
        return nextRelationId++;
    }

    public ColumnRefOperator create(String name, Type type, boolean nullable) {
        ColumnRefOperator columnRef = new ColumnRefOperator(nextId++, type, name, nullable);
        columnRefs.add(columnRef);
        return columnRef;
    }

    public void updateColumnRefToColumns(ColumnRefOperator columnRef, Column column, Table table) {
        columnRefToColumns.put(columnRef, column);
        columnRefToTable.put(columnRef, table);
    }

    public void updateColumnToRelationIds(int columnId, int tableId) {
        columnToRelationIds.put(columnId, tableId);
    }
}
