package org.lxz.common;

import java.util.ArrayList;
import java.util.Objects;

public class Id<IdType extends Id<IdType>> {

    protected static int INVALID_ID = -1;
    protected final int id;

    public Id() {
        this.id = INVALID_ID;
    }

    public Id(int id) {
        this.id = id;
    }

    public boolean isValid() {
        return id != INVALID_ID;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        // only ids of the same subclass are comparable
        if (o.getClass() != this.getClass()) {
            return false;
        }
        return ((Id)o).id == id;
    }

    @Override
    public int hashCode() {
        return Integer.valueOf(id).hashCode();
    }

    public ArrayList<IdType> asList() {
        ArrayList<IdType> ids = new ArrayList<>();
        ids.add((IdType) this);
        return ids;
    }

    @Override
    public String toString() {
        return Integer.toString(id);
    }
}
