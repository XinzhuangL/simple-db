package org.lxz.sql.analyzer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;
import org.lxz.analysis.SlotRef;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class RelationFields {
    private final List<Field> allFields;

    // NOTE: sort fields by name to speedup resolve performance
    private final Multimap<String, Field> names;
    // private final boolean resolveStruct;

    public RelationFields(Field... fields) {
        this(ImmutableList.copyOf(fields));
    }

    public RelationFields(List<Field> fields) {
        requireNonNull(fields, "fields is null");
        this.allFields = ImmutableList.copyOf(fields);
        // this.resolveStruct = fields.stream().anyMatch(x -> x.getType().isStructType());
        // struct
        this.names = this.allFields.stream().collect(ImmutableListMultimap.toImmutableListMultimap(
                x -> x.getName().toLowerCase(), x -> x
        ));
    }

    /**
     * Gets the index of all columns matching the specified name
     */
    public List<Field> resolveFields(SlotRef name) {
        // struct content
        // Resolve the slot based on column name first, then table name
        // For the case a table with thousands of columns, resolve by table name could not reduce the cardinality,
        // but resolve by column name first could reduce it a lot
        List<Field> resolved =
                names.get(name.getColName().toLowerCase()).stream().collect(ImmutableList.toImmutableList());
        if (name.getTblNameWithoutAnalyzed() == null) {
            return resolved;
        } else {
            return resolved.stream().filter(input -> input.canResolve(name)).collect(ImmutableList.toImmutableList());
        }
    }

    /**
     * Gets the index of the specified field
     */
    public int indexOf(Field field) {
        return allFields.indexOf(field);
    }

    public List<Field> getAllFields() {
        return allFields;
    }
}
