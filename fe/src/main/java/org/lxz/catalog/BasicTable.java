package org.lxz.catalog;

/**
 * BasicTable declares a subset of methods in {@link Table}, which aims to provide basic information about table while
 * eliminating network interactions with external services.
 */
public interface BasicTable {
    String getCatalogName();

    String getName();

    String getComment();

    String getMysqlType();

    String getEngine();

    Table.TableType getType();

    boolean isOlapView();

    boolean isMaterializedView();

    boolean isNativeTableOrMaterializedView();

    long getCreateTime();

    long getLastCheckTime();
}
