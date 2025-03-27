package org.lxz.analysis;


import org.lxz.sql.ast.AstVisitor;
import org.lxz.sql.parser.NodePosition;
import org.lxz.sql.common.UserException;


public interface ParseNode {

    /**
     * Perform semantic analysis of node and all of its children.
     * Throws exception if any errors found.
     */
    default void analyze(Analyzer analyzer) throws UserException {
        throw new RuntimeException("New AST not support analyze function");
    }

    /**
     * @return SQL syntax corresponding to this node.
     */
    default String toSql() { throw new RuntimeException("New AST not implement toSql function"); }

    NodePosition getPos();

    default <R, C> R accept(AstVisitor<R, C> visitor, C context) {
        throw new RuntimeException("Not implement accept function");
    }
}
