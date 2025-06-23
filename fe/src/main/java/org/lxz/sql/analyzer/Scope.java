package org.lxz.sql.analyzer;

import org.lxz.analysis.SlotRef;
import org.lxz.common.PrimitiveType;

import java.util.List;
import java.util.Optional;

/**
 * Scope represent the namespace used for resolved
 * scope include all fields in this namespace
 */
public class Scope {
    private Scope parent;
    private final RelationId relationId;
    private final RelationFields relationFields;
    // cteQueries

    // lambdaInputs


    public Scope(RelationId relationId, RelationFields relationFields) {
        this.relationId = relationId;
        this.relationFields = relationFields;
    }

    public void setParent(Scope parent) {
        this.parent = parent;
    }

    public ResolvedField resolvedField(SlotRef expression) {
        return resolvedField(expression, RelationId.anonymous());
    }


    public ResolvedField resolvedField(SlotRef expression, RelationId outerRelationId) {
        Optional<ResolvedField> resolvedField = resolvedField(expression, 0, outerRelationId);
        if (!resolvedField.isPresent()) {
            throw new RuntimeException(String.format("Column '%s' cannot be resolved", expression.toSql()));
        }
        return resolvedField.get();
    }

    private Optional<ResolvedField> resolvedField(SlotRef expression, int fieldIndexOffset, RelationId outerRelationId) {
        List<Field> matchFields = relationFields.resolveFields(expression);
        if (matchFields.size() > 1) {
            throw new RuntimeException(String.format("Column '%s' is ambiguous", expression.toSql()));
        } else if (matchFields.size() == 1) {
            if (matchFields.get(0).getType().getPrimitiveType().equals(PrimitiveType.UNKNOWN_TYPE)) {
                throw new RuntimeException(String.format("Datatype of external table column [%s]", matchFields.get(0).getName()));
            } else {
                return Optional.of(asResolvedField(matchFields.get(0), fieldIndexOffset));
            }
        } else {
         if (parent != null
         // Correlated subqueries currently only support accessing properties in the first level outer layer
                 && !relationId.equals(outerRelationId)
         ) {
             return parent.resolvedField(expression, fieldIndexOffset + relationFields.getAllFields().size(), outerRelationId);
         }
         return Optional.empty();
        }
    }

    public ResolvedField asResolvedField(Field filed, int fieldIndexOffset) {
        int hierarchyFieldIndex = relationFields.indexOf(filed) + fieldIndexOffset;
        return new ResolvedField(this, filed, hierarchyFieldIndex);
    }

    public RelationFields getRelationFields() {
        return relationFields;
    }

    public RelationId getRelationId() {
        return relationId;
    }

    public Scope getParent() {
        return parent;
    }
}
