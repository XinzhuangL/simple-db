package org.lxz.sql.ast;

import org.lxz.analysis.ParseNode;
import org.lxz.analysis.TableName;
import org.lxz.sql.analyzer.Scope;
import org.lxz.sql.parser.NodePosition;

import java.util.List;

public abstract class Relation implements ParseNode {
    private Scope scope;
    protected TableName alias;

    protected final NodePosition pos;
    protected List<String> explicitColumnNames;

    protected Relation(NodePosition pos) {
        this.pos = pos;
    }
}
