package org.lxz.analysis;

import com.google.common.collect.ImmutableList;
import org.lxz.sql.ast.QualifiedName;
import org.lxz.sql.parser.NodePosition;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public class SlotRef extends Expr {
    private TableName tblName;
    private String colName;
    // private ColumnId columnId;
    // Used in toSql
    private String label;

    private QualifiedName qualifiedName;



    // Only Struct Type need this field
    // Record access struct subfield path position
    // Example: struct type: col : STRUCT<c1: INT, c2: STRUCT<c1: INT, c2: DOUBLE>>,
    // We execute sql: `SELECT col FROM table`, the usedStructField value is [].
    // We execute sql: `SELECT col.c2 FROM table`, the usedStructFieldPos value is [1]/
    // We execute sql: `SELECT col.c2.c1 FROM table`, the usedStructFieldPos value is [1,0].
    private ImmutableList<Integer> usedStructFieldPos;

    // now it is used in Analyzer phase of creating mv to decide the field nullable of mv
    // can not use desc because the slotId is unknown is Analyzer phase
    private boolean nullable = true;

    // Only used write
    private SlotRef() {
        super();
    }

    public SlotRef(TableName tblName, String col) {
        super();
        this.tblName = tblName;
        this.colName = col;
        this.label = "`" + col + "`";
    }

    public SlotRef(TableName tblName, String col, String label) {
        super();
        this.tblName = tblName;
        this.colName = col;
        this.label = label;
    }

    public SlotRef(QualifiedName qualifiedName) {
        super(qualifiedName.getPos());
        List<String> parts = qualifiedName.getParts();
        // If parts.size() == 1 it must be a column name. LIKE `Select a FROM table`.
        // If parts.size() = [2, 3, 4], it maybe a column name or specific struct subfield name.
        checkArgument(parts.size() > 0);
        this.qualifiedName = QualifiedName.of(qualifiedName.getParts(), qualifiedName.getPos());
        if (parts.size() == 1) {
            this.colName = parts.get(0);
            this.label = parts.get(0);
        } else if (parts.size() == 2) {
            this.tblName = new TableName();
            this.colName = parts.get(1);
            this.label = parts.get(1);
        } else if (parts.size() == 3) {
            this.tblName = new TableName(null, parts.get(0), parts.get(1), qualifiedName.getPos());
            this.colName = parts.get(2);
            this.label = parts.get(2);
        } else if (parts.size() == 4) {
            this.tblName = new TableName(parts.get(0), parts.get(1), parts.get(2), qualifiedName.getPos());
            this.colName = parts.get(3);
            this.label = parts.get(3);
        } else {
            // If parts.size() > 4, it must refer to a struct subfield name, so we set SlotRef's TableName null value,
            // set col, label a qualified name here[Of course it's a wrong value].
            // Correct value will be parsed in Analyzer according context.
            this.tblName = null;
            this.colName = qualifiedName.toString();
            this.label = qualifiedName.toString();
        }
    }

    @Override
    public NodePosition getPos() {
        return null;
    }
}
