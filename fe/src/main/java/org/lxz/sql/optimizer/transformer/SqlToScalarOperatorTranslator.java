package org.lxz.sql.optimizer.transformer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lxz.ConnectContext;
import org.lxz.analysis.Expr;
import org.lxz.analysis.SlotRef;
import org.lxz.sql.analyzer.ResolvedField;
import org.lxz.sql.ast.AstVisitor;
import org.lxz.sql.optimizer.base.ColumnRefFactory;
import org.lxz.sql.optimizer.operator.scalar.ColumnRefOperator;
import org.lxz.sql.optimizer.operator.scalar.ScalarOperator;

import static java.util.Objects.requireNonNull;

/**
 * Translator from Expr to ScalarOperator
 */
public class SqlToScalarOperatorTranslator {
    private static final Logger LOG = LogManager.getLogger(SqlToScalarOperatorTranslator.class);

    private SqlToScalarOperatorTranslator() {

    }


    public static ScalarOperator translate(Expr expression, ExpressionMapping expressionMapping, ColumnRefFactory columnRefFactory) {
        ColumnRefOperator columnRefOperator = expressionMapping.get(expression);
        if (columnRefOperator != null) {
            return columnRefOperator;
        }
        Visitor visitor = new Visitor(expressionMapping, columnRefFactory);
        ScalarOperator result = visitor.visit(expression, new Context());

        requireNonNull(result, "translated expression is null");
        return result;

    }


    private static final class Context{

    }


    private static class Visitor implements AstVisitor<ScalarOperator, Context> {
        private ExpressionMapping expressionMapping;
        private final ColumnRefFactory columnRefFactory;

        public Visitor(ExpressionMapping expressionMapping, ColumnRefFactory columnRefFactory) {
            this.expressionMapping = expressionMapping;
            this.columnRefFactory = columnRefFactory;
        }

        @Override
        public ScalarOperator visitSlot(SlotRef node, Context context) {
            ResolvedField resolvedField =
                    expressionMapping.getScope().resolvedField(node, expressionMapping.getOuterScopeRelationId());
            ColumnRefOperator columnRefOperator =
                    expressionMapping.getColumnRefWithIndex(resolvedField.getRelationFieldIndex());

            return columnRefOperator;
        }
    }

}
