package de.tuberlin.pserver.test.core.programs;

/*import com.google.common.base.Preconditions;
import de.tuberlin.pserver.client.PServerExecutor;
import de.tuberlin.pserver.compiler.Program;
import de.tuberlin.pserver.dsl.state.annotations.State;
import de.tuberlin.pserver.dsl.state.properties.Scope;
import de.tuberlin.pserver.dsl.unit.annotations.Unit;
import de.tuberlin.pserver.dsl.unit.controlflow.lifecycle.Lifecycle;
import de.tuberlin.pserver.math.matrix.MatrixFormat;
import de.tuberlin.pserver.math.matrix.Matrix64F;
import de.tuberlin.pserver.runtime.filesystem.recordold.RowRecordIteratorProducer;
import de.tuberlin.pserver.runtime.state.mtxentries.MutableMatrixEntry;
import de.tuberlin.pserver.runtime.state.mtxentries.ReusableMatrixEntry;
import de.tuberlin.pserver.runtime.state.matrix.partitioner.RowPartitioner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class MatrixDenseLoadingRowTestJob extends Program {

    private static final long ROWS = 1000;
    private static final long COLS = 2;

    // use this, if you want to run this test directly outside the IntegrationTestSuite
    private final String FILE = "pserver-test/src/main/resources/stripes2.csv";

    //private final String FILE = "src/main/resources/stripes2.csv";

    @State(
            path = FILE,
            rows = ROWS,
            cols = COLS,
            scope = Scope.PARTITIONED,
            recordFormat = RowRecordIteratorProducer.class,
            matrixFormat = MatrixFormat.DENSE_FORMAT
    )
    public Matrix64F matrix;

    @Unit
    public void main(final Lifecycle lifecycle) {

        lifecycle.process(() -> {

            matrix = runtimeManager.getDHT("matrix");
            int nodeId = programContext.nodeID;
            int numNodes = programContext.nodeDOP;
            RowPartitioner partitioner = new RowPartitioner(ROWS, COLS, nodeId, numNodes);
            ReusableMatrixEntry<Double> entry = new MutableMatrixEntry<>(-1, -1, Double.NaN);
            BufferedReader br = null;

            try {
                br = new BufferedReader(new FileReader(FILE));
                String line = null;
                int lineNumber = -1;
                while ((line = br.readLine()) != null) {
                    lineNumber++;
                    String[] parts = line.split(",");
                    Preconditions.checkState(parts.length == COLS);
                    for (int col = 0; col < COLS; col++) {
                        double val = Double.parseDouble(parts[col]);
                        if (partitioner.getPartitionOfEntry(entry.set(lineNumber, col, Double.NaN)) == nodeId) {
                            double matrixVal = matrix.get(lineNumber, col);
                            if (matrixVal != val) {
                                System.out.println(nodeId + ": matrix(" + lineNumber + "," + col + ") is " + matrixVal + " but should be " + val);
                            }
                            Preconditions.checkState(matrixVal == val);
                        }
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

    public static void main(String[] args) {
        System.setProperty("simulation.numNodes", "4");
        PServerExecutor.LOCAL
                .run(MatrixDenseLoadingRowTestJob.class)
                .done();
    }
}*/
