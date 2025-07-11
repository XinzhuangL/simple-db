package org.lxz.sql.ast;

import org.lxz.analysis.Expr;
import org.lxz.analysis.TableName;
import org.lxz.catalog.Column;
import org.lxz.catalog.Table;
import org.lxz.sql.analyzer.Field;
import org.lxz.sql.parser.NodePosition;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TableRelation extends Relation {

    public enum TableHint {
        _META_,
        _BINLOG_,
        _SYNC_MV,
        _USE_PK_INDEX_,
    }

    private final TableName name;

    private Table table;

    private Map<Field, Column> columns;

    // Support temporary partition
    private PartitionNames partitionNames;

    private final List<Long> tabletIds;
    private final List<Long> replicaIds;
    private final Set<TableHint> tableHints = new HashSet<>();
    // optional temporal clause for external MySQL tables that support this syntax
    private String temporalClause;

    private Expr partitionPredicate;

    // todo generatedExprToColumnRef

    private List<String> pruneScanColumns = Collections.emptyList();

    public TableRelation(TableName name, PartitionNames partitionNames, List<Long> tabletIds, List<Long> replicaIds, NodePosition pos) {
        super(pos);
        this.name = name;
        this.partitionNames = partitionNames;
        this.tabletIds = tabletIds;
        this.replicaIds = replicaIds;
    }

    public TableName getName() {
        return name;
    }

    public Table getTable() {
        return table;
    }

    public List<String> getPruneScanColumns() {
        return pruneScanColumns;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    @Override
    public NodePosition getPos() {
        return null;
    }

    @Override
    public TableName getResolveTableName() {
        if (alias != null) {
            if (name.getDb() != null) {
                if (name.getCatalog() != null) {
                    return new TableName(name.getCatalog(), name.getDb(), alias.getTbl(), name.getPos());
                } else {
                    return new TableName(null, name.getDb(), alias.getTbl(), name.getPos());
                }
            } else {
                return alias;
            }
        } else {
            return name;
        }
    }

    public void setColumns(Map<Field, Column> columns) {
        this.columns = columns;
    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
        return visitor.visitTable(this, context);
    }

    public Map<Field, Column> getColumns() {
        return columns;
    }
}
