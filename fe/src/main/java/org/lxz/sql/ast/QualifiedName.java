package org.lxz.sql.ast;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import org.lxz.analysis.ParseNode;
import org.lxz.sql.parser.NodePosition;

import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.isEmpty;
import static java.util.Objects.requireNonNull;

/**
 * QualifiedName is used to represent a string connected by "."
 * Often used to represent an unresolved Table Name such as db.table
 */
public class QualifiedName implements ParseNode {
    private final ImmutableList<String> parts;

    private final NodePosition pos;

    public static QualifiedName of(Iterable<String> originalParts) {
        return of(originalParts, NodePosition.ZERO);
    }

    public static QualifiedName of(Iterable<String> originalParts, NodePosition pos) {
        requireNonNull(originalParts, "originalParts is null");
        checkArgument(!isEmpty(originalParts), "originalParts is empty");
        return new QualifiedName(ImmutableList.copyOf(originalParts), pos);
    }

    // Make sure QualifiedName is immutable.
    private QualifiedName(ImmutableList<String> originalParts, NodePosition pos) {
        this.pos = pos;
        this.parts = originalParts;
    }

    public List<String> getParts() {
        return parts;
    }

    @Override
    public NodePosition getPos() {
        return pos;
    }

    @Override
    public String toSql() {
        return Joiner.on(".").join(parts);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return parts.equals(((QualifiedName) o).parts);
    }

    @Override
    public int hashCode() {
        return parts.hashCode();
    }
}
