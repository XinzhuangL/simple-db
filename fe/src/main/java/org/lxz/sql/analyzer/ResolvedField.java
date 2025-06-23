package org.lxz.sql.analyzer;

/**
 * ResolvedField is represented resolved field,
 * contains scope which fields belong to and
 * the field index of relation
 */

public class ResolvedField {
    private final Scope scope;
    private final Field field;

    /**
     * relationFieldIndex is an index with a hierarchical relationship.
     * When the resolved scope contains a parent, an offset of the parent scope will be added
     */
    private final int relationFieldIndex;

    public ResolvedField(Scope scope, Field field, int relationFieldIndex) {
        this.scope = scope;
        this.field = field;
        this.relationFieldIndex = relationFieldIndex;
    }

    public Scope getScope() {
        return scope;
    }

    public Field getField() {
        return field;
    }

    public int getRelationFieldIndex() {
        return relationFieldIndex;
    }

}
