package org.lxz.sql.ast;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import org.lxz.analysis.Expr;
import org.lxz.analysis.NullLiteral;
import org.lxz.sql.parser.NodePosition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ValuesRelation extends QueryRelation {
    private final List<List<Expr>> rows;

    /*
        isNullValues means a statement without from or from dual, add a single row of null values here,
        so that the semantics are the same, and the processing of subsequent query logic can be simplified,
        such as select sum(1) or select sum(1) from dual, will be converted to select sum(1) from (values(null)) t.
        This can share the same logic as select sum(1) from table
     */
    private boolean isNullValues;

    public static ValuesRelation newDualRelation() {
        return newDualRelation(NodePosition.ZERO);
    }

    public static ValuesRelation newDualRelation(NodePosition pos) {
        ImmutableList.Builder<Expr> row = ImmutableList.builder();
        // todo impl later
        row.add(NullLiteral.create());
        ImmutableList.Builder<List<Expr>> rows = ImmutableList.builder();
        rows.add(row.build());
        ValuesRelation valuesRelation = new ValuesRelation(rows.build(), Collections.singletonList(""), pos);
        valuesRelation.setNullValues(true);
        return valuesRelation;
    }
    public ValuesRelation(List<List<Expr>> rows, List<String> explicitColumnNames) {
        this(rows, explicitColumnNames, NodePosition.ZERO);
    }

    public ValuesRelation(List<List<Expr>> rows, List<String> explicitColumnNames, NodePosition pos) {
        super(pos);
        this.rows = new ArrayList<>(rows);
        this.explicitColumnNames = explicitColumnNames;
    }

    public void setNullValues(boolean nullValues) {
        isNullValues = nullValues;
    }

}
