package org.lxz.sql.analyzer;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.lxz.ConnectContext;
import org.lxz.analysis.ParseNode;
import org.lxz.analysis.SlotRef;
import org.lxz.analysis.TableName;
import org.lxz.catalog.Column;
import org.lxz.catalog.Database;
import org.lxz.catalog.Table;
import org.lxz.common.ErrorCode;
import org.lxz.server.GlobalStateMgr;
import org.lxz.server.MetadataMgr;
import org.lxz.sql.ast.AstVisitor;
import org.lxz.sql.ast.QueryRelation;
import org.lxz.sql.ast.QueryStatement;
import org.lxz.sql.ast.Relation;
import org.lxz.sql.ast.SelectRelation;
import org.lxz.sql.ast.StatementBase;
import org.lxz.sql.ast.TableRelation;
import org.lxz.sql.common.AnalysisException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QueryAnalyzer {

    private final ConnectContext session;
    private final MetadataMgr metadataMgr;

    public QueryAnalyzer(ConnectContext session) {
        this.session = session;
        this.metadataMgr = GlobalStateMgr.getMetadataMgr();
    }

    public void analyze(StatementBase node) {
        new Visitor().process(node, new Scope(RelationId.anonymous(), new RelationFields()));
    }

    private class Visitor implements AstVisitor<Scope, Scope> {

        public Visitor() {

        }

        public Scope process(ParseNode node, Scope scope) {
            return node.accept(this, scope);
        }

        @Override
        public Scope visitQueryStatement(QueryStatement statement, Scope parent) {

            Scope scope = visitQueryRelation(statement.getQueryRelation(), parent);
            // todo hasOutFileClause
            return scope;
        }

        // 此时 QueryRelation的实例一般为SelectRelation 实际Process调用的是SelectRelation
        @Override
        public Scope visitQueryRelation(QueryRelation node, Scope parent) {
            // todo analyze CTE
            return process(node, parent);
        }

        // visit column info
        @Override
        public Scope visitTable(TableRelation node, Scope outerScope) {
            TableName tableName = node.getResolveTableName();
            Table table = node.getTable();
            ImmutableList.Builder<Field> fields = ImmutableList.builder();
            ImmutableMap.Builder<Field, Column> columns = ImmutableMap.builder();
            // not sync mv query
            List<Column> fullSchema = table.getFullSchema();
            Set<Column> baseSchema = new HashSet<>(table.getBaseSchema());

            List<String> pruneScanColumns = node.getPruneScanColumns();
            boolean needPruneScanColumns = pruneScanColumns != null && !pruneScanColumns.isEmpty();
            if (needPruneScanColumns) {
                Set<String> fullColumnNames = new HashSet<>(fullSchema.size());
                for (Column column : fullSchema) {
                    if (column.isGeneratedColumn()) {
                        needPruneScanColumns = false;
                        break;
                    }
                    fullColumnNames.add(column.getName());
                }
                needPruneScanColumns &= fullColumnNames.containsAll(pruneScanColumns);
            }

            Set<String> bucketColumns = table.getDistributionColumnNames();
            List<String> partitionColumns = table.getPartitionColumnNames();

            for (Column column : fullSchema) {
                // TODO: avoid analyze visible or not each time, cache it in chame
                if (needPruneScanColumns && !column.isKey() &&
                !bucketColumns.contains(column.getName()) &&
                !partitionColumns.contains(column.getName()) &&
                !pruneScanColumns.contains(column.getName().toLowerCase())) {
                    // reduce unnecessary columns init, but must init key columns/bucket columns/partition columns
                    continue;
                }
                // column belong to table， filed & slot belong to query
                boolean visible = baseSchema.contains(column);
                SlotRef slot = new SlotRef(tableName, column.getName(), column.getName());
                Field field = new Field(column.getName(), column.getType(), tableName, slot, visible, column.isAllowNull());
                columns.put(field, column);
                fields.add(field);
            }

            //
            node.setColumns(columns.build());
            String dbName = node.getName().getDb();


            Scope scope = new Scope(RelationId.of(node), new RelationFields(fields.build()));
            node.setScope(scope);

            // process generatedExpr
            return scope;

        }

        @Override
        public Scope visitSelect(SelectRelation selectRelation, Scope scope) {
            AnalyzeState analyzeState = new AnalyzeState();
            // Record aliases at this level to prevent alias conflicts
            Set<TableName> aliasSet = new HashSet<>();
            Relation resolvedRelation = resolveTableRef(selectRelation.getRelation(), scope, aliasSet);

            selectRelation.setRelation(resolvedRelation);


            // epoch process
            Scope sourceScope = process(resolvedRelation, scope);
            sourceScope.setParent(scope);

            SelectAnalyzer selectAnalyzer = new SelectAnalyzer(session);

            selectAnalyzer.analyze(
                    analyzeState,
                    selectRelation.getSelectList(),
                    selectRelation.getRelation(),
                    sourceScope,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            selectRelation.fillResolvedAST(analyzeState);
            return analyzeState.getOutputScope();
        }


        // actual to get table from sr meta
        private Relation resolveTableRef(Relation relation, Scope scope, Set<TableName> aliasSet) {
            if (relation instanceof TableRelation) {
                TableRelation tableRelation = (TableRelation) relation;
                TableName tableName = tableRelation.getName();
                // todo process cte
                TableName resolveTableName = relation.getResolveTableName();
                // todo reset catalogName
                if (aliasSet.contains(resolveTableName)) {
                    throw new RuntimeException("resolveTableName already exists");
                } else {
                    aliasSet.add(new TableName(resolveTableName.getCatalog(),
                            resolveTableName.getDb(),
                            resolveTableName.getTbl()));
                }

                Table table = resolveTable(tableRelation);
                Relation r;

                tableRelation.setTable(table);
                r = tableRelation;
                return r;
            } else {

                // process later
                return relation;
            }
        }

        public Table resolveTable(TableRelation tableRelation) {
            TableName tableName = tableRelation.getName();
            try {
                String catalogName = tableName.getCatalog();
                String dbName = tableName.getDb();
                String tbName = tableName.getTbl();
                if (Strings.isNullOrEmpty(dbName)) {
                    throw new AnalysisException(ErrorCode.ERR_NO_DB_ERROR.formatErrorMsg());
                }
                // todo check catalogName exist
                Database db;
                db = metadataMgr.getDb(catalogName, dbName);

                Table table = null;

                table = metadataMgr.getTable(catalogName, dbName, tbName);

                if (table == null) {
                    throw new AnalysisException(ErrorCode.ERR_BAD_TABLE_ERROR.formatErrorMsg());
                }

                return table;
            } catch (AnalysisException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

}
