package de.tuberlin.pserver.runtime.partitioning.partitioner;


import de.tuberlin.pserver.math.matrix.Matrix;
import de.tuberlin.pserver.math.matrix.partitioning.PartitionShape;
import de.tuberlin.pserver.runtime.partitioning.mtxentries.MatrixEntry;

public class NoPartitioner extends IMatrixPartitioner {

    private final PartitionShape shape;

    public NoPartitioner(long rows, long cols, int nodeId, int[] atNodes) {
        super(rows, cols, nodeId, atNodes);
        shape = new PartitionShape(rows, cols, 0, 0);
    }

    @Override
    public int getPartitionOfEntry(MatrixEntry entry) {
        return -1;
    }

    @Override
    public PartitionShape getPartitionShape() {
        return shape;
    }

    @Override
    public long translateGlobalToLocalRow(long row) throws IllegalArgumentException {
        return row;
    }

    @Override
    public long translateGlobalToLocalCol(long col) throws IllegalArgumentException {
        return col;
    }

    @Override
    public long translateLocalToGlobalRow(long row) throws IllegalArgumentException {
        return row;
    }

    @Override
    public long translateLocalToGlobalCol(long col) throws IllegalArgumentException {
        return col;
    }

    @Override
    public int getNumRowPartitions() {
        return 1;
    }

    @Override
    public int getNumColPartitions() {
        return 1;
    }

    @Override
    public IMatrixPartitioner ofNode(int nodeId) {
        return new VirtualRowPartitioner(rows, cols, nodeId, new int[] {nodeId});
    }
}
