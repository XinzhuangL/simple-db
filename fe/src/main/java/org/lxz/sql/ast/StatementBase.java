package org.lxz.sql.ast;

import com.google.common.base.Preconditions;
import org.lxz.analysis.ParseNode;
import org.lxz.qe.OriginStatement;
import org.lxz.sql.parser.NodePosition;

public class StatementBase implements ParseNode {

    private OriginStatement origStmt;
    private final NodePosition pos;

    public void setOrigStmt(OriginStatement origStmt) {
        Preconditions.checkState(origStmt != null);
        this.origStmt = origStmt;
    }

    protected StatementBase(NodePosition pos) {
        this.pos = pos;
    }

    public OriginStatement getOrigStmt() {
        return origStmt;
    }

    @Override
    public NodePosition getPos() {
        return null;
    }
}
