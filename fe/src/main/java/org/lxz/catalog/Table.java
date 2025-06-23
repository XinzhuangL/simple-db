package org.lxz.catalog;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.annotations.SerializedName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class Table implements BasicTable {

    private static final Logger LOG = LogManager.getLogger(Table.class);

    // enum of TableType
    //  1. Native table:
    //    1.1 Local: OLAP, MATERIALIZED_VIEW
    //    1.2 Cloud native: LAKE, LAKE_MATERIALIZED_VIEW
    //  2. System table: SCHEMA
    //  3. View: INLINE_VIEW, VIEW
    //  4. External table: MYSQL, OLAP_EXTERNAL, BROKER, ELASTICSEARCH, HIVE, ICEBERG, HUDI, ODBC, JDBC
    public enum TableType {
        @SerializedName("OLAP")
        OLAP,
    }


    @SerializedName(value = "id")
    protected long id;
    @SerializedName(value = "name")
    protected String name;
    @SerializedName(value = "type")
    protected TableType type;
    @SerializedName(value = "createTime")
    protected long createTime;

    /**
     *
     */

    // todo Column
    @SerializedName(value = "fullSchema")
    protected List<Column> fullSchema = new CopyOnWriteArrayList<>();

    /**
     * nameToColumn and idToColumn are both indexes of cullSchema.
     * nameToColumn is the index of column name, idToColumn is the index of column id,
     * column names can change, but the column ID of a specific column will never change.
     * Use case-insensitive tree map, because the column name is case-insensitive in the system.
     *
     */
    protected Map<String, Column> nameToColumn;
    protected Map<ColumnId, Column> idToColumn;

    // table(view)'s comment
    @SerializedName(value = "comment")
    protected String comment = "";

    // not serialized filed
    // record all materialized views based on this Table
    // todo relatedMaterializedViews

    // foreign key constraint for mv rewrite
    // todo foreignKeyConstraints

    static Object o = new Object();

    protected static Map<String, Map<PartitionKey, Long>> tblToPartitionKey = Maps.newHashMap();

    public List<Column> getFullSchema() {
        return fullSchema;
    }

    // should override in subclass if necessary
    // full schema may have mv column ?
    // visiable
    public List<Column> getBaseSchema() {
        return fullSchema;
    }

    @Override
    public String getCatalogName() {
        return "";
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public String getComment() {
        return "";
    }

    @Override
    public String getMysqlType() {
        return "";
    }

    @Override
    public String getEngine() {
        return "";
    }

    @Override
    public Table.TableType getType() {
        return null;
    }

    @Override
    public boolean isOlapView() {
        return false;
    }

    @Override
    public boolean isMaterializedView() {
        return false;
    }

    @Override
    public boolean isNativeTableOrMaterializedView() {
        return false;
    }

    @Override
    public long getCreateTime() {
        return 0;
    }

    @Override
    public long getLastCheckTime() {
        return 0;
    }

    public Set<String> getDistributionColumnNames() {
        return Collections.emptySet();
    }

    public List<String> getPartitionColumnNames() {
        return Lists.newArrayList();
    }

    public void setName(String name) {
        this.name = name;
    }
}
