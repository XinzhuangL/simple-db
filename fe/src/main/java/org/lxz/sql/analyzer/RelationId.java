package org.lxz.sql.analyzer;

import org.lxz.sql.ast.Relation;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * RelationId is used to uniquely mark a relation, which is mainly used in
 * FieldId to distinguish which relation the field comes from when resolve
 */
public class RelationId {
    private final Relation sourceNode;

    private RelationId(Relation sourceNode) {
        this.sourceNode = sourceNode;
    }

    public static RelationId anonymous() {
        return new RelationId(null);
    }
    public static RelationId of(Relation sourceNode) {
        return new RelationId(requireNonNull(sourceNode, "source cannot be null"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RelationId that = (RelationId) o;
        return Objects.equals(sourceNode, that.sourceNode);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(sourceNode);
    }
}
