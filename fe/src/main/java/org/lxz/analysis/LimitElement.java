package org.lxz.analysis;

import org.lxz.sql.parser.NodePosition;

/**
 * Combination of limit nad offset expressions
 */
public class LimitElement implements ParseNode {
    @Override
    public NodePosition getPos() {
        return null;
    }
}
