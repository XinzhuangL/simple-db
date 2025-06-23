package org.lxz.sql.ast;

import org.lxz.analysis.Expr;
import org.lxz.analysis.ParseNode;
import org.lxz.analysis.SlotRef;

public interface AstVisitor<R, C> {

    default R visit(ParseNode node) {
        return visit(node, null);
    }

    default R visit(ParseNode node, C context) {
        return node.accept(this, context);
    }

    default R visitNode(ParseNode node, C context) {
        return null;
    }

    default R visitStatement(StatementBase statement, C context) {
        return visitNode(statement, context);
    }

    default R visitQueryStatement(QueryStatement statement, C context) {
        return visitStatement(statement, context);
    }

    // ------------------------------------------- Relation ----------------------------------==------------------------
    default R visitRelation(Relation node, C context) {
        return visitNode(node, context);
    }

    default R visitTable(TableRelation node, C context) {
        return visitRelation(node, context);
    }

    default R visitQueryRelation(QueryRelation node, C context) {
        return visitRelation(node, context);
    }

    default R visitSelect(SelectRelation node, C context) {
        return visitRelation(node, context);
    }


    // ------------------------------------------- Expression --------------------------------==------------------------
    default R visitExpression(Expr node, C context) {
        return visitNode(node, context);
    }

    default R visitSlot(SlotRef node, C context) {
        return visitExpression(node, context);
    }

}
