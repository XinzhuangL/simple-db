package org.lxz.sql.parser;

import org.antlr.v4.runtime.tree.TerminalNode;

public class TokenNumberListener extends StarRocksBaseListener {

    private final int maxTokensNum;
    private final int maxExprChildCount;

    public TokenNumberListener(int maxTokensNum, int maxExprChildCount) {
        this.maxTokensNum = maxTokensNum;
        this.maxExprChildCount = maxExprChildCount;
    }

    @Override
    public void visitTerminal(TerminalNode node) {
        int index = node.getSymbol().getTokenIndex();
        if (index >= maxTokensNum) {
            throw new ParsingException("Statement exceeds maximum length limit, please consider modify " +
                    "parse_tokens_limit variable.");
        }
    }

    @Override
    public void exitExpressionList(StarRocksParser.ExpressionListContext ctx) {
        long childCunt = ctx.children.stream().filter(child -> child instanceof StarRocksParser.ExpressionContext).count();
        if (childCunt > maxExprChildCount) {
            throw new ParsingException(String.format("Expression child number %d exceeded the maximum %d",
                    childCunt, maxExprChildCount));
        }
    }

    @Override
    public void exitExpressionsWithDefault(StarRocksParser.ExpressionsWithDefaultContext ctx) {
        long childCount = ctx.expressionOrDefault().size();
        if (childCount > maxExprChildCount) {
            throw new ParsingException(String.format("Expression child number % d exceeded the maximum %d",
                    childCount, maxExprChildCount));
        }
    }


}
