package de.tuberlin.pserver.math.delegates;

import com.google.common.base.Preconditions;
import de.tuberlin.pserver.math.Matrix;
import de.tuberlin.pserver.math.Vector;

public class MathLibFactory {

    // Disallow instantiation.
    private MathLibFactory() {}

    // ---------------------------------------------------

    public static enum DMathLibrary {

        EJML_LIBRARY,

        MTJ_LIBRARY,

        JBLAS_LIBRARY
    }

    // ---------------------------------------------------

    public static enum SMathLibrary {

        UJMP_LIBRARY
    }

    // ---------------------------------------------------

    public static enum SMatrixEncodingSchemes { // Not used at the moment...

        DOK_ENCODING,

        LIL_ENCODING,

        YALE_ENCODING,

        CRS_ENCODING,

        CDS_ENCODING
    }

    // ---------------------------------------------------

    public static LibraryMatrixOps<Matrix, Vector> delegateDMatrixOpsTo(final DMathLibrary lib) {
        switch (Preconditions.checkNotNull(lib)) {
            case EJML_LIBRARY:
                return new de.tuberlin.pserver.math.delegates.dense.ejml.EJMLMatrixOps();
            case MTJ_LIBRARY:
                throw new UnsupportedOperationException();
            case JBLAS_LIBRARY:
                throw new UnsupportedOperationException();
        }
        throw new IllegalStateException();
    }

    // ---------------------------------------------------

    public static LibraryVectorOps<Vector> delegateDVectorOpsTo(final DMathLibrary lib) {
        switch (Preconditions.checkNotNull(lib)) {
            case EJML_LIBRARY:
                return new de.tuberlin.pserver.math.delegates.dense.ejml.EJMLVectorOps();
            case MTJ_LIBRARY:
                throw new UnsupportedOperationException();
            case JBLAS_LIBRARY:
                throw new UnsupportedOperationException();
        }
        throw new IllegalStateException();
    }

    // ---------------------------------------------------
/*
    public static de.tuberlin.pserver.math.delegates.LibraryMatrixOps<SMatrix, SVector> delegateSMatrixOpsTo(final SMathLibrary lib) {
        switch (Preconditions.checkNotNull(lib)) {
            case UJMP_LIBRARY:
                return new UJMPMatrixOps();
        }
        throw new IllegalStateException();
    }

    // ---------------------------------------------------

    public static Object createSMatrixInternalObject(final SMathLibrary lib, final SMatrix matrix) {
        switch (Preconditions.checkNotNull(lib)) {
            case UJMP_LIBRARY:
                return SparseMatrix.factory.zeros(matrix.numRows(), matrix.numCols());
        }
        throw new IllegalStateException();
    }


    // ---------------------------------------------------

    public static LibraryVectorOps<SVector> delegateSVectorOpsTo(final SMathLibrary lib) {
        switch (Preconditions.checkNotNull(lib)) {
            case UJMP_LIBRARY:
                return new de.tuberlin.pserver.math.delegates.sparse.ujmp.UJMPVectorOps();
        }
        throw new IllegalStateException();
    }

    // ---------------------------------------------------

    public static Object createSVectorInternalObject(final SMathLibrary lib, final SVector vector) {
        switch (Preconditions.checkNotNull(lib)) {
            case UJMP_LIBRARY:
                return SparseMatrix.factory.zeros(1, vector.size());
        }
        throw new IllegalStateException();
    }
    */
}
