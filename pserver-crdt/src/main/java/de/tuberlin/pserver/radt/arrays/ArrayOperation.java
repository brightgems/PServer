package de.tuberlin.pserver.radt.arrays;

import de.tuberlin.pserver.radt.RADTOperation;
import de.tuberlin.pserver.radt.S4Vector;

public class ArrayOperation<T> extends RADTOperation<T> {

    // ---------------------------------------------------
    // Fields.
    // ---------------------------------------------------

    private final int index;

    // ---------------------------------------------------
    // Constructors.
    // ---------------------------------------------------

    // No arg constructor for serialization
    public ArrayOperation() {

        super();

        this.index = 0;

    }

    public ArrayOperation(OpType type, T value, int index, int[] vectorClock, S4Vector s4) {

        super(type, value, vectorClock, s4);

        this.index = index;

    }

    // ---------------------------------------------------
    // Public Methods.
    // ---------------------------------------------------

    public int getIndex() {

        return index;

    }

}
