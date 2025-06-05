package org.lxz.common;

/**
 * Generator of consecutively numbered integers to be used as ids by subclasses of Id.
 * Subclasses of Id should be able to create a generator for their Id type.
 */
public abstract class IdGenerator<IdType extends Id<IdType>> {
    protected int nextId = 0;

    public abstract IdType getNextId();

    public abstract IdType getMaxId();
}
