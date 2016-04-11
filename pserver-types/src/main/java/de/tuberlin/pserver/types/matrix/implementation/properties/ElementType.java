package de.tuberlin.pserver.types.matrix.implementation.properties;

import de.tuberlin.pserver.types.matrix.implementation.Matrix32F;
import de.tuberlin.pserver.types.matrix.implementation.matrix32f.dense.DenseMatrix32F;
import de.tuberlin.pserver.types.matrix.implementation.matrix32f.sparse.CSRMatrix32F;
import de.tuberlin.pserver.types.matrix.implementation.matrix32f.sparse.SparseMatrix32F;

public enum ElementType {

    FLOAT_MATRIX,

    DOUBLE_MATRIX;

    public static ElementType getElementTypeFromClass(final Class<?> clazz) {
        if (clazz == Matrix32F.class
                || clazz == SparseMatrix32F.class
                || clazz == DenseMatrix32F.class
                || clazz == CSRMatrix32F.class)
            return FLOAT_MATRIX;
        else
            throw new IllegalStateException();
    }
}