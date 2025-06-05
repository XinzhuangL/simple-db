package org.lxz.sql.parser;


import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.lxz.analysis.Expr;
import org.lxz.analysis.GroupByClause;
import org.lxz.analysis.HintNode;
import org.lxz.analysis.IntLiteral;
import org.lxz.analysis.ParseNode;
import org.lxz.analysis.SlotRef;
import org.lxz.analysis.TableName;
import org.lxz.sql.ast.Identifier;
import org.lxz.sql.ast.QualifiedName;
import org.lxz.sql.ast.QueryRelation;
import org.lxz.sql.ast.QueryStatement;
import org.lxz.sql.ast.Relation;
import org.lxz.sql.ast.SelectList;
import org.lxz.sql.ast.SelectListItem;
import org.lxz.sql.ast.SelectRelation;
import org.lxz.sql.ast.TableRelation;
import org.lxz.sql.ast.ValuesRelation;

import java.math.BigInteger;
import java.util.ArrayList;
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

    // expression (AS? (identifier | string))?   #selectSingle
    @Override
    public ParseNode visitSelectSingle(StarRocksParser.SelectSingleContext ctx) {
        String alias = null;
        // visit identifier later
        if (ctx.identifier() != null) {

        } else if (ctx.string() != null) {

        }
        return new SelectListItem((Expr) visit(ctx.expression()), alias, createPos(ctx));
    }

    @Override
    public ParseNode visitTableAtom(StarRocksParser.TableAtomContext ctx) {
        Token start = ctx.getStart();
        Token stop = ctx.getStop();
        QualifiedName qualifiedName = getQualifiedName(ctx.qualifiedName());
        TableName tableName = qualifiedNametoTableName(qualifiedName);
        // todo PartitionNames
        // todo tabletIds
        // todo replicaLists

        TableRelation tableRelation = new TableRelation(tableName, null, null, null, createPos(start, stop));
        // todo ignore tabletContent
        // todo ignore bracketHint  table hit

        // alias
        if (ctx.alias != null) {
            Identifier identifier = (Identifier) visit(ctx.alias);
            tableRelation.setAlias(new TableName(null, identifier.getValue()));
        }

        // temporalClause
        return tableRelation;
    }

    // catalog.database.table
    private TableName qualifiedNametoTableName(QualifiedName qualifiedName) {
        List<String> parts = qualifiedName.getParts();
        if (parts.size() == 3) {
            return new TableName(parts.get(0), parts.get(1), parts.get(2), qualifiedName.getPos());
        } else if (parts.size() == 2) {
            return new TableName(null, qualifiedName.getParts().get(0), qualifiedName.getParts().get(1),
                    qualifiedName.getPos());
        } else if (parts.size() == 1) {
            return new TableName(null, null, qualifiedName.getParts().get(0), qualifiedName.getPos());
        } else {
            throw new ParsingException(PARSER_ERROR_MSG.invalidTableFormat(qualifiedName.toString()));
        }
    }

    private QualifiedName getQualifiedName(StarRocksParser.QualifiedNameContext ctx) {
        List<String> parts = new ArrayList<>();
        NodePosition pos = createPos(ctx);
        for (ParseTree c : ctx.children) {
            if (c instanceof TerminalNode) {
                TerminalNode t = (TerminalNode) c;
                if (t.getSymbol().getType() == StarRocksParser.DOT_IDENTIFIER) {
                    parts.add(t.getText().substring(1));
                }
            } else if (c instanceof StarRocksParser.IdentifierContext) {
                StarRocksParser.IdentifierContext identifierContext = (StarRocksParser.IdentifierContext) c;
                Identifier identifier = (Identifier) visit(identifierContext);
                parts.add(identifier.getValue());
            }
        }
        return QualifiedName.of(parts, pos);
    }

    @Override
    public ParseNode visitPredicate(StarRocksParser.PredicateContext ctx) {
        return super.visitPredicate(ctx);
    }

    @Override
    public ParseNode visitColumnReference(StarRocksParser.ColumnReferenceContext ctx) {
        Identifier identifier = (Identifier) visit(ctx.identifier());
        List<String> parts = new ArrayList<>();
        parts.add(identifier.getValue());
        QualifiedName qualifiedName = QualifiedName.of(parts, createPos(ctx));
        return new SlotRef(qualifiedName);
    }

    @Override
    public ParseNode visitUnquotedIdentifier(StarRocksParser.UnquotedIdentifierContext ctx) {
        return new Identifier(ctx.getText(), createPos(ctx));
    }

    @Override
    public ParseNode visitIntegerValue(StarRocksParser.IntegerValueContext ctx) {
        NodePosition pos = createPos(ctx);
        try {
            BigInteger intLiteral = new BigInteger(ctx.getText());
            return new IntLiteral(intLiteral.longValue(), pos);
        } catch (Exception e) {
            throw new ParsingException(PARSER_ERROR_MSG.invalidNumFormat(ctx.getText()), pos);
        }

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
