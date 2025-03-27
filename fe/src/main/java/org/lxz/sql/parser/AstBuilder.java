package org.lxz.sql.parser;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.lxz.analysis.Expr;
import org.lxz.analysis.GroupByClause;
import org.lxz.analysis.HintNode;
import org.lxz.analysis.ParseNode;
import org.lxz.sql.ast.QueryRelation;
import org.lxz.sql.ast.QueryStatement;
import org.lxz.sql.ast.Relation;
import org.lxz.sql.ast.SelectList;
import org.lxz.sql.ast.SelectListItem;
import org.lxz.sql.ast.SelectRelation;
import org.lxz.sql.ast.ValuesRelation;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.lxz.sql.common.ErrorMsgProxy.PARSER_ERROR_MSG;

public class AstBuilder extends StarRocksBaseVisitor<ParseNode> {

    private final long sqlMode;

    private final IdentityHashMap<ParserRuleContext, List<HintNode>> hitMap;

    public AstBuilder() {
        this(32L, new IdentityHashMap<>());
    }
    protected AstBuilder(long sqlMode, IdentityHashMap<ParserRuleContext, List<HintNode>> hintMap) {
        this.hitMap = hintMap;
        long hintSqlMode = 0L;
        for (Map.Entry<ParserRuleContext, List<HintNode>> entry : hintMap.entrySet()) {
            for (HintNode hint : entry.getValue()) {
                // todo SetVarHint
            }
        }
        this.sqlMode = sqlMode | hintSqlMode;
    }

    @Override
    public ParseNode visitSingleStatement(StarRocksParser.SingleStatementContext ctx) {
        if (ctx.statement() != null) {
            return visit(ctx.statement());
        } else {
            return visit(ctx.emptyStatement());
        }
    }

    @Override
    public ParseNode visitQueryStatement(StarRocksParser.QueryStatementContext ctx) {
        QueryRelation queryRelation = (QueryRelation) visit(ctx.queryRelation());
        QueryStatement queryStatement = new QueryStatement(queryRelation);
        return queryStatement;
    }

    @Override
    public ParseNode visitQueryRelation(StarRocksParser.QueryRelationContext ctx) {
        QueryRelation queryRelation = (QueryRelation) visit(ctx.queryNoWith());
        return queryRelation;
    }

    @Override
    public ParseNode visitQueryNoWith(StarRocksParser.QueryNoWithContext ctx) {
        // visit order by limit
        QueryRelation queryRelation = (QueryRelation) visit(ctx.queryPrimary());

        return queryRelation;
    }

    // impl simple query
    @Override
    public ParseNode visitQuerySpecification(StarRocksParser.QuerySpecificationContext ctx) {
        // create select stmt
        Relation from = null;
        List<SelectListItem> selectItems = visit(ctx.selectItem(), SelectListItem.class);

        // select 1 + 1 from Dual
        // Dual是虚拟维表，所以必须由特定内容
        if (ctx.fromClause() instanceof StarRocksParser.DualContext) {
            for (SelectListItem item : selectItems) {
                if (item.isStar()) {
                    throw new ParsingException(PARSER_ERROR_MSG.noTableUsed(), item.getPos());
                }
            }
        } else {
            StarRocksParser.FromContext fromContext = (StarRocksParser.FromContext) ctx.fromClause();
            if (fromContext.relations() != null) {
                List<Relation> relations = visit(fromContext.relations().relation(), Relation.class);
                Iterator<Relation> iterator = relations.iterator();
                Relation relation = iterator.next();
                // todo Process Join
                from = relation;
            }

        }
        /*
            from == null means a statement without from or from dual, add a single row of null values here,
            so that the semantics are the same, and the processing of subsequent query logic can be simplified,
            such as select sum(1) or select sum(1) from dual, will be converted to select sum(1) from (value(null)) t.
            This can share the same logic as select sum(1) from table
         */
        if (from == null) {
            from = ValuesRelation.newDualRelation();
        }
        boolean isDistinct = ctx.setQuantifier() != null && ctx.setQuantifier().DISTINCT() != null;
        SelectList selectList = new SelectList(selectItems, isDistinct);
        selectList.setHitNodes(hitMap.get(ctx));

        SelectRelation resultSelectRelation = new SelectRelation(
                selectList,
                from,
                (Expr) visitIfPresent(ctx.where),
                (GroupByClause) visitIfPresent(ctx.groupingElement()),
                (Expr) visitIfPresent(ctx.having),
                createPos(ctx));

        // todo extend Query with QUALIFY to nested queries with filter.


        return resultSelectRelation;
    }

    @Override
    public ParseNode visitSelectSingle(StarRocksParser.SelectSingleContext ctx) {
        // todo
        return null;
    }

    @Override
    public ParseNode visitTableAtom(StarRocksParser.TableAtomContext ctx) {
        // todo
        return null;
    }

    // ------------------------------------------- Util Functions -------------------------------------------
    protected <T> List<T> visit(List<? extends ParserRuleContext> contexts, Class<T> clazz) {
//        return contexts.stream()
//                .map(this::visit)
//                .map(clazz::cast)
//                .collect(toList());
        return contexts.stream()
                .map(each -> {
                    return this.visit(each);
                }).map(
                        each -> {
                            return clazz.cast(each);
                        }
                ).collect(toList());
    }

    private ParseNode visitIfPresent(ParserRuleContext ctx) {
        if (ctx != null) {
            return visit(ctx);
        } else {
            return null;
        }
    }

    protected NodePosition createPos(ParserRuleContext ctx) {
        return createPos(ctx.start, ctx.stop);
    }

    protected NodePosition createPos(Token start, Token stop) {
        if (start == null) {
            return NodePosition.ZERO;
        }

        if (stop == null) {
            return new NodePosition(start.getLine(), start.getCharPositionInLine());
        }
        return new NodePosition(start, stop);
    }
}
