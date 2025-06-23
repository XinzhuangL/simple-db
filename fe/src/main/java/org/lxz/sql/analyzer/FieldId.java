package org.lxz.sql.analyzer;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

/**
 * FieldId is used to uniquely mark a Field
 * FieldId contains relationId and field index in relation
 */
public final class FieldId {
    private final RelationId relationId;
    private final int fieldIndex;


    public FieldId(RelationId relationId, int fieldIndex) {
        this.relationId = relationId;

        checkArgument(fieldIndex >= 0, "fieldIndex must be non-negative, got: %s", fieldIndex);
        this.fieldIndex = fieldIndex;
    }

    /**
     * Obtain the FieldId of the original field in the original scope(excluding the parent level)
     * So here to find field from the source, not getRelationFieldIndex
     */
    public static FieldId from(ResolvedField field) {
        requireNonNull(field, "field is null");
        Scope sourceScope = field.getScope();
        RelationFields relationFields = sourceScope.getRelationFields();
        return new FieldId(sourceScope.getRelationId(), relationFields.indexOf(field.getField()));
    }
}
