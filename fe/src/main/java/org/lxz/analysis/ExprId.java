package org.lxz.analysis;

import org.lxz.common.Id;
import org.lxz.common.IdGenerator;

public class ExprId extends Id<ExprId> {

    // Construction only allowed via an IdGenerator.
    public ExprId(int id) {
        super(id);
    }

    public static IdGenerator<ExprId> createGenerator() {
        return new IdGenerator<ExprId>() {

            @Override
            public ExprId getNextId() {
                return new ExprId(nextId++);
            }

            @Override
            public ExprId getMaxId() {
                return new ExprId(nextId - 1);
            }
        };
    }


}
