package de.tuberlin.pserver.test.core.programs;

import com.google.common.base.Preconditions;
import de.tuberlin.pserver.dsl.controlflow.annotations.Unit;
import de.tuberlin.pserver.dsl.controlflow.program.Lifecycle;
import de.tuberlin.pserver.dsl.state.properties.GlobalScope;
import de.tuberlin.pserver.math.Format;
import de.tuberlin.pserver.math.Layout;
import de.tuberlin.pserver.math.matrix.Matrix;
import de.tuberlin.pserver.runtime.Program;
import de.tuberlin.pserver.runtime.filesystem.record.config.RowColValRecordFormatConfig;
import de.tuberlin.pserver.runtime.partitioning.MatrixByRowPartitioner;
import de.tuberlin.pserver.runtime.partitioning.mtxentries.MutableMatrixEntry;
import de.tuberlin.pserver.runtime.partitioning.mtxentries.ReusableMatrixEntry;
import de.tuberlin.pserver.types.PartitionType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class MatrixDenseLoadingTestJob extends Program {

    private static final long ROWS = 1000;
    private static final long COLS = 250;

    public Matrix matrix;

    private final String FILE;

    public MatrixDenseLoadingTestJob() {
        FILE = getClass().getClassLoader().getResource("rowcolval_dataset_1000_250_shuffeled.csv").getFile();
    }

    @Unit
    public void main(final Lifecycle lifecycle) {

        lifecycle.process(() -> {

            dataManager.loadAsMatrix(
                    programContext,
                    FILE,
                    "matrix",
                    ROWS, COLS,
                    GlobalScope.PARTITIONED,
                    PartitionType.ROW_PARTITIONED,
                    new RowColValRecordFormatConfig(),
                    Format.DENSE_FORMAT,
                    Layout.ROW_LAYOUT
            );

            matrix = dataManager.getObject("matrix");

            int nodeId = programContext.runtimeContext.nodeID;
            int numNodes = programContext.nodeDOP;
            MatrixByRowPartitioner partitioner = new MatrixByRowPartitioner(nodeId, numNodes, ROWS, COLS);
            ReusableMatrixEntry entry = new MutableMatrixEntry(-1, -1, Double.NaN);
            BufferedReader br = null;

            try {
                br = new BufferedReader(new FileReader(FILE));
                String line = null;
                while ((line = br.readLine()) != null) {

                    String[] parts = line.split(",");
                    int row = Integer.parseInt(parts[0]);
                    int col = Integer.parseInt(parts[1]);
                    double val = Double.parseDouble(parts[2]);

                    if (partitioner.getPartitionOfEntry(entry.set(row, col, Double.NaN)) == nodeId) {
                        double matrixVal = matrix.get(row, col);
                        if (matrixVal != val) {
                            System.out.println(nodeId + ": matrix(" + row + "," + col + ") is " + matrixVal + " but should be " + val);
                        }
                        Preconditions.checkState(matrixVal == val);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
